package team.foxed.foxzy;

public final class DistanceControllerTest {
    private static void equal(int actual, int expected, String message) {
        if (actual != expected) throw new AssertionError(message + ": " + actual + " != " + expected);
    }
    public static void main(String[] args) {
        DistanceController c = new DistanceController(10, 0, 10);
        equal(c.sample(60, 45, 30, 6, 3, 6, 5, 10), 10, "warmup");
        for (long now = 10; now < 12; now++) equal(c.sample(60, 45, 30, 6, 3, 6, now, 10), 10, "sustained overload needed");
        equal(c.sample(60, 45, 30, 6, 3, 6, 12, 10), 9, "reduce one chunk");
        for (long now = 13; now < 22; now++) equal(c.sample(60, 45, 30, 6, 3, 6, now, 10), 9, "cooldown");
        for (long now = 22; now < 100; now++) c.sample(60, 45, 30, 6, 3, 6, now, 10);
        equal(c.current(), 6, "minimum bound");
        for (long now = 100; now < 250; now++) c.sample(20, 45, 30, 6, 3, 6, now, 10);
        equal(c.current(), 10, "recovery and maximum bound");
        c.rebase(4, 300, 0);
        for (long now = 300; now < 400; now++) c.sample(70, 45, 30, 6, 3, 6, now, 1);
        equal(c.current(), 4, "never increase below configured floor");
        c.rebase(12, 500, 0);
        c.sample(70, 45, 30, 6, 3, 6, 501, 1);
        c.sample(70, 45, 30, 6, 3, 6, 502, 1);
        c.sample(35, 45, 30, 6, 3, 6, 503, 1);
        equal(c.sample(70, 45, 30, 6, 3, 6, 504, 1), 12, "deadband resets streak");
        equal(c.sample(Double.NaN, 45, 30, 6, 3, 6, 505, 1), 12, "invalid sample ignored");
        equal(c.sample(70, 30, 45, 6, 3, 6, 506, 1), 12, "invalid thresholds ignored");
        c.rebase(16, 600, 20);
        equal(c.baseline(), 16, "manual rebase updates maximum");
        equal(c.sample(100, 45, 30, 6, 1, 1, 601, 1), 16, "rebase warmup");
        c.sample(100, 45, 30, 6, 1, 1, 621, 1);
        c.defer(622, 10);
        equal(c.baseline(), 16, "pause retains original baseline");
        equal(c.current(), 15, "pause retains reduced distance");
        equal(c.sample(20, 45, 30, 6, 1, 1, 633, 1), 16, "recovery after pause");
        System.out.println("Foxzy controller tests passed");
    }
}
