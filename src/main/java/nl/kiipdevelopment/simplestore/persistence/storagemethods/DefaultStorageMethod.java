package nl.kiipdevelopment.simplestore.persistence.storagemethods;

import nl.kiipdevelopment.simplestore.CouldBeNull;
import nl.kiipdevelopment.simplestore.Store;
import nl.kiipdevelopment.simplestore.persistence.FileStrategy;
import nl.kiipdevelopment.simplestore.persistence.StorageMethod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DefaultStorageMethod<T> implements StorageMethod<T> {
    private final ExecutorService executor = Executors.newWorkStealingPool();
    private final Store<T> store;
    private final Path location;
    private final FileStrategy fileStrategy;

    public DefaultStorageMethod(Store<T> store, Path location, FileStrategy fileStrategy) {
        this.store = store;
        this.location = location;
        this.fileStrategy = fileStrategy;
    }

    @Override
    public Store<T> store() {
        return store;
    }

    @Override
    public Path location() {
        return location;
    }

    @Override
    public void save() {
        final Map<String, byte[]> map = rewriteMap(store);

        createLocation(location);
        cleanLocation(location);

        final List<String> keys = new ArrayList<>(map.keySet());
        final List<byte[]> values = new ArrayList<>(map.values());

        final int[] keysPerFile = keysPerFile(values);
        final CountDownLatch latch = new CountDownLatch(keysPerFile.length);

        int keyOffset = 0;
        int fileOffset = 0;
        for (int keyCount : keysPerFile) {
            final int offset = keyOffset;
            final int file = fileOffset++;
            keyOffset += keyCount;

            executor.execute(() -> {
                try (
                    FileOutputStream fileOutputStream = new FileOutputStream(location.resolve(Integer.toHexString(file)).toFile());
                    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
                    DataOutputStream dataOutputStream = new DataOutputStream(gzipOutputStream)
                ) {
                    writeVarInt(dataOutputStream, keyCount);
                    for (int i = 0; i < keyCount; i++) {
                        byte[] key = keys.get(offset + i).getBytes(StandardCharsets.UTF_8);
                        byte[] value = values.get(offset + i);

                        writeVarInt(dataOutputStream, key.length);
                        dataOutputStream.write(key);
                        writeVarInt(dataOutputStream, value.length);
                        dataOutputStream.write(value);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        try {
            if (Files.notExists(location)) {
                Files.createDirectory(location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Function<byte[], T> fromByteArray = store.fromByteArray();
        final List<Path> files = new CouldBeNull<>(() -> Files.list(location), Throwable::printStackTrace)
            .asNotNull()
            .collect(Collectors.toList());
        final CountDownLatch latch = new CountDownLatch(files.size());

        createLocation(location);

        for (Path path : files) {
            executor.execute(() -> {
                try (
                    FileInputStream fileInputStream = new FileInputStream(path.toFile());
                    GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                    DataInputStream dataInputStream = new DataInputStream(gzipInputStream)
                ) {
                    final int length = readVarInt(dataInputStream);
                    for (int i = 0; i < length; i++) {
                        int keyLength = readVarInt(dataInputStream);
                        String key = new String(dataInputStream.readNBytes(keyLength), StandardCharsets.UTF_8);
                        int valueLength = readVarInt(dataInputStream);
                        byte[] value = dataInputStream.readNBytes(valueLength);

                        store.set(key, fromByteArray.apply(value));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<String, byte[]> rewriteMap(Store<T> store) {
        final Function<T, byte[]> toByteArray = store.toByteArray();
        final Map<String, byte[]> rewrittenMap = new ConcurrentHashMap<>();
        final List<Map.Entry<String, T>> entries = new ArrayList<>(store.data().entrySet());
        final CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            final int finalI = i;

            executor.execute(() -> {
                for (int j = 0; j < entries.size() / 10; j++) {
                    int index = finalI * (entries.size() / 10) + j;
                    Map.Entry<String, T> entry = entries.get(index);

                    rewrittenMap.put(
                        entry.getKey(),
                        toByteArray.apply(entry.getValue())
                    );
                }

                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rewrittenMap;
    }

    private static void createLocation(Path location) {
        if (Files.exists(location)) {
            return;
        }

        try {
            Files.createDirectories(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cleanLocation(Path location) {
        if (!Files.exists(location)) {
            return;
        }

        try {
            Files.list(location).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] keysPerFile(List<byte[]> values) {
        List<Integer> keysPerFile = new ArrayList<>();
        int count = 0;
        int i = 0;
        int remaining = values.size();

        for (byte[] value : values) {
            count += value.length;
            i++;
            remaining--;

            if (count > fileStrategy.maxFileSize || remaining <= 0) {
                keysPerFile.add(i);

                count = 0;
                i = 0;
            }
        }

        return keysPerFile.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }

    private static int readVarInt(DataInputStream dataInputStream) throws IOException {
        int result = 0;
        for (int shift = 0; ; shift += 7) {
            byte b = dataInputStream.readByte();
            result |= (b & 0x7f) << shift;
            if (b >= 0) {
                return result;
            }
        }
    }

    private static void writeVarInt(DataOutputStream dataOutputStream, int i) throws IOException {
        if ((i & (0xFFFFFFFF << 7)) == 0) {
            dataOutputStream.writeByte((byte) i);
        } else if ((i & (0xFFFFFFFF << 14)) == 0) {
            dataOutputStream.writeShort((short) ((i & 0x7F | 0x80) << 8 | (i >>> 7)));
        } else if ((i & (0xFFFFFFFF << 21)) == 0) {
            dataOutputStream.writeByte((byte) (i & 0x7F | 0x80));
            dataOutputStream.writeByte((byte) ((i >>> 7) & 0x7F | 0x80));
            dataOutputStream.writeByte((byte) (i >>> 14));
        } else if ((i & (0xFFFFFFFF << 28)) == 0) {
            dataOutputStream.writeInt((i & 0x7F | 0x80) << 24 | (((i >>> 7) & 0x7F | 0x80) << 16)
                | ((i >>> 14) & 0x7F | 0x80) << 8 | (i >>> 21));
        } else {
            dataOutputStream.writeInt((i & 0x7F | 0x80) << 24 | ((i >>> 7) & 0x7F | 0x80) << 16
                | ((i >>> 14) & 0x7F | 0x80) << 8 | ((i >>> 21) & 0x7F | 0x80));
            dataOutputStream.writeByte((byte) (i >>> 28));
        }
    }
}
