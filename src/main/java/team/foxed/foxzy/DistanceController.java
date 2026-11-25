package team.foxed.foxzy;

/** Pure state machine; no game classes, chunk tickets, worker threads, or allocations per sample. */
public final class DistanceController {
    private int baseline;
    private int current;
    private int badSamples;
    private int goodSamples;
    private long nextChangeNanos;

    public DistanceController(int baseline, long now, long warmupNanos) {
        rebase(baseline, now, warmupNanos);
    }

    public void rebase(int distance, long now, long warmupNanos) {
        baseline = current = Math.max(2, distance);
        badSamples = goodSamples = 0;
        nextChangeNanos = now + warmupNanos;
    }

    public int sample(double cost, double high, double low, int floor,
                      int badRequired, int goodRequired, long now, long cooldownNanos) {
        if (!Double.isFinite(cost) || cost < 0 || low >= high) return current;
        if (now < nextChangeNanos) {
            badSamples = goodSamples = 0;
            return current;
        }
        int minimum = Math.min(baseline, Math.max(2, floor));
        if (cost > high) {
            badSamples = Math.min(badSamples + 1, badRequired);
            goodSamples = 0;
        } else if (cost < low) {
            goodSamples = Math.min(goodSamples + 1, goodRequired);
            badSamples = 0;
        } else {
            badSamples = goodSamples = 0;
        }
        int next = current;
        if (badSamples >= badRequired) next = Math.max(minimum, current - 1);
        else if (goodSamples >= goodRequired) next = Math.min(baseline, current + 1);
        if (next != current) {
            current = next;
            badSamples = goodSamples = 0;
            nextChangeNanos = now + cooldownNanos;
        }
        return current;
    }

    public int current() { return current; }
    public int baseline() { return baseline; }
    public void defer(long now, long warmupNanos) {
        badSamples = goodSamples = 0;
        nextChangeNanos = now + warmupNanos;
    }
}
