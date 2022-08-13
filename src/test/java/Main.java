import nl.kiipdevelopment.simplestore.SimpleStore;
import nl.kiipdevelopment.simplestore.stores.ObjectStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Set;

public class Main {
    public static final int TEST_COUNT = 500_000;
    private static final Random random = new Random();
    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        testWrite();
        testRead();
        System.gc();

        TimingsUtils.startTiming("write");
        testWrite();
        TimingsUtils.reportTiming("write", TEST_COUNT + " writing test finished in %s");

        testWrite();
        testRead();
        System.gc();

        TimingsUtils.startTiming("read");
        testRead();
        TimingsUtils.reportTiming("read", TEST_COUNT + " reading test finished in %s");
    }

    private static void testWrite() {
        SimpleStore<Object> store = SimpleStore.builder()
            .store(new ObjectStore())
            .build();

        for (int i = 0; i < TEST_COUNT; i++) {
            String next = Long.toString(random.nextLong());
            while (store.exists(next)) {
                next = Long.toString(random.nextLong());
            }

            store.set(next, hash(next));
        }

        if (store.data().size() < TEST_COUNT) {
            System.out.println("Write key size is only " + store.data().size() + ", expected " + TEST_COUNT);
        }

        store.save();
    }

    private static void testRead() {
        SimpleStore<Object> store = SimpleStore.builder()
            .store(new ObjectStore())
            .build();
        store.load();

        Set<String> keys = store.data().keySet();
        for (String key : keys) {
            if (store.get(key) instanceof String string) {
                if (!hash(key).equals(string)) {
                    System.out.println("Fail hash at " + key);
                }
            } else {
                System.out.println("Fail type at " + key);
            }
        }

        if (keys.size() < TEST_COUNT) {
            System.out.println("Read key size is only " + keys.size() + ", expected " + TEST_COUNT);
        }
    }

    private static String hash(String input) {
        final byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        final StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            final String hex = Integer.toHexString(0xff & b);

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }
}
