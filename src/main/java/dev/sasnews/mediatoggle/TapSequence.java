package dev.sasnews.mediatoggle;

/**
 * Pure logic helper that classifies a sequence of tap timestamps into a final
 * multi-tap count (1, 2, or 3). No Android dependencies: it operates on
 * explicit millisecond timestamps (e.g. {@code SystemClock.elapsedRealtime()})
 * so it can be unit-tested in plain JVM.
 *
 * <p>Burst semantics: taps that arrive close enough together are treated as a
 * single multi-tap burst. Two consecutive taps belong to the same burst when
 * the gap between them is {@code <= windowMs} (boundary inclusive). When a gap
 * exceeds the window, the burst resets and a new one begins. Each burst's count
 * is capped at 3 (the maximum multi-tap the app reacts to).
 */
public final class TapSequence {

    /** Default multi-tap window in milliseconds (matches app default of 300ms). */
    public static final long DEFAULT_WINDOW_MS = 300L;

    /** Maximum number of taps the app recognizes within a single burst. */
    public static final int MAX_TAPS = 3;

    /** Private constructor: pure static utility. */
    private TapSequence() {
    }

    /**
     * Classifies the given tap timestamps into bursts and returns the count of
     * the last/current burst, capped at {@link #MAX_TAPS}.
     *
     * <p>Examples with {@code windowMs = 300}:
     * <ul>
     *   <li>{@code [t0, t0+150, t0+280]} &rarr; {@code 3} (all in one burst)</li>
     *   <li>{@code [t0, t0+400]} &rarr; {@code 1} (gap 400 &gt; 300 splits;
     *       the trailing T0+400 tap is the last/current burst)</li>
     *   <li>{@code []} &rarr; {@code 0}</li>
     *   <li>{@code [t0]} &rarr; {@code 1}</li>
     * </ul>
     *
     * @param times    non-null tap timestamps in elapsedRealtime millis;
     *                 duplicates make a zero-gap burst
     * @param windowMs maximum allowed gap between consecutive taps of one burst
     * @return the number of taps in the final burst, capped at {@link #MAX_TAPS};
     *         {@code 0} when {@code times.length == 0}
     */
    public static int classifyTapCount(long[] times, long windowMs) {
        if (times.length == 0) {
            return 0;
        }

        int currentBurst = 1;
        for (int i = 1; i < times.length; i++) {
            long gap = times[i] - times[i - 1];
            if (gap <= windowMs) {
                // Same burst. Cap so we keep counting the existing burst
                // length without letting it exceed the recognized maximum.
                currentBurst = Math.min(currentBurst + 1, MAX_TAPS);
            } else {
                // Gap exceeds the window: a new burst starts at this tap.
                currentBurst = 1;
            }
        }

        return currentBurst;
    }
}