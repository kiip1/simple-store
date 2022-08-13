import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import nl.kiipdevelopment.simplestore.utils.MathUtils;

public final class TimingsUtils {
    private static final Object2LongOpenHashMap<String> timings = new Object2LongOpenHashMap<>();
    private static final Object2DoubleOpenHashMap<String> stoppedTimings = new Object2DoubleOpenHashMap<>();

    private TimingsUtils() {}

    /**
     * Starts a timing.
     *
     * @param id ID of the timing to start
     */
    public static void startTiming(String id) {
        timings.putIfAbsent(id, System.nanoTime());
    }

    /**
     * Stops a timing.
     *
     * @param id ID of the timing to stop
     */
    public static void stopTiming(String id) {
        stoppedTimings.putIfAbsent(id, endTiming(id));
    }

    /**
     * Ends a timing.
     *
     * @param id ID of the timing to end
     * @return Nanoseconds the timing took
     */
    public static double endTiming(String id) {
        if (stoppedTimings.containsKey(id)) {
            return stoppedTimings.removeDouble(id);
        }

        final long start = timings.removeLong(id);
        final long end = System.nanoTime();

        if (start == 0) {
            return 0;
        }

        return end - start;
    }

    /**
     * Ends a timing and reports it.
     *
     * @param id ID of the timing to report
     * @param message Message to format
     */
    public static void reportTiming(String id, String message) {
        final double took = endTiming(id);
        final double formattedTook = MathUtils.round(took / 1_000_000D, 2);

        if (took == 0) {
            System.out.printf(message + "%n", "<DIDN'T START>");
        } else {
            System.out.printf(message + "%n", formattedTook + "ms");
        }
    }
}