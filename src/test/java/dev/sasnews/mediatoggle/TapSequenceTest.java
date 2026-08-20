package dev.sasnews.mediatoggle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the pure {@link TapSequence} classifier.
 *
 * <p>These run in a plain JVM (no Android runtime needed) because
 * {@code TapSequence} has no Android dependency. All tests use the default
 * window of 300ms unless stated otherwise.
 */
public class TapSequenceTest {

    private static final long WINDOW_MS = 300L;

    @Test
    public void emptyArrayReturnsZero() {
        assertEquals(0, TapSequence.classifyTapCount(new long[0], WINDOW_MS));
    }

    @Test
    public void singleTapIsOne() {
        long t0 = 10_000L;
        assertEquals(1, TapSequence.classifyTapCount(new long[]{t0}, WINDOW_MS));
    }

    @Test
    public void doubleTapIsTwo() {
        long t0 = 10_000L;
        assertEquals(2, TapSequence.classifyTapCount(
                new long[]{t0, t0 + 150L}, WINDOW_MS));
    }

    @Test
    public void tripleTapIsThree() {
        long t0 = 10_000L;
        assertEquals(3, TapSequence.classifyTapCount(
                new long[]{t0, t0 + 150L, t0 + 280L}, WINDOW_MS));
    }

    @Test
    public void wideGapSplitsIntoNewBurst() {
        long t0 = 10_000L;
        // gap 400 > 300 resets; the trailing tap is its own burst of 1.
        assertEquals(1, TapSequence.classifyTapCount(
                new long[]{t0, t0 + 400L}, WINDOW_MS));
    }

    @Test
    public void mixedBurstsReturnCountOfLastBurst() {
        long t0 = 10_000L;
        // first pair in-window -> 2; gap 400 resets; trailing pair ->
        // t0+500 to t0+600 gap 100 keeps one burst of 2.
        assertEquals(2, TapSequence.classifyTapCount(
                new long[]{t0, t0 + 100L, t0 + 500L, t0 + 600L}, WINDOW_MS));
    }

    @Test
    public void excessTapsCappedAtThree() {
        long t0 = 10_000L;
        // 4 in-window taps are capped at the max recognized burst of 3.
        assertEquals(3, TapSequence.classifyTapCount(
                new long[]{t0, t0 + 100L, t0 + 200L, t0 + 300L}, WINDOW_MS));
    }

    @Test
    public void gapExactlyAtWindowRemainsInWindow() {
        long t0 = 10_000L;
        // a gap of exactly windowMs is boundary-inclusive, so it is one burst.
        assertEquals(2, TapSequence.classifyTapCount(
                new long[]{t0, t0 + 300L}, WINDOW_MS));
    }
}