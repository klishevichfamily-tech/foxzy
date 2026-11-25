package team.foxed.foxzy;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FoxzyConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue SERVER_ENABLED = BUILDER
            .comment("Adaptive server view distance. Simulation distance and forced chunk tickets are not changed.")
            .define("serverEnabled", true);
    public static final ModConfigSpec.IntValue SERVER_MIN = BUILDER.defineInRange("serverMinimumDistance", 6, 2, 32);
    public static final ModConfigSpec.DoubleValue SERVER_HIGH = BUILDER
            .comment("Reduce distance above this average tick work time, in milliseconds.")
            .defineInRange("serverOverloadMs", 45.0, 10.0, 1000.0);
    public static final ModConfigSpec.DoubleValue SERVER_LOW = BUILDER
            .comment("Recover below this value. Must be lower than serverOverloadMs.")
            .defineInRange("serverRecoveryMs", 30.0, 1.0, 999.0);
    public static final ModConfigSpec.BooleanValue CLIENT_ENABLED = BUILDER
            .comment("Adaptive client render distance. Original setting is restored on disconnect.")
            .define("clientEnabled", true);
    public static final ModConfigSpec.IntValue CLIENT_MIN = BUILDER.defineInRange("clientMinimumDistance", 6, 2, 32);
    public static final ModConfigSpec.IntValue CLIENT_LOW_FPS = BUILDER.defineInRange("clientReduceBelowFps", 30, 5, 240);
    public static final ModConfigSpec.IntValue CLIENT_GOOD_FPS = BUILDER
            .comment("Recover above this FPS; should exceed clientReduceBelowFps. Lower for a 30 FPS cap.")
            .defineInRange("clientRecoverAboveFps", 55, 6, 500);
    public static final ModConfigSpec.IntValue COOLDOWN = BUILDER
            .comment("Minimum seconds between one-chunk changes. Also used as initial warmup.")
            .defineInRange("cooldownSeconds", 60, 10, 600);
    public static final ModConfigSpec SPEC = BUILDER.build();
    public static long cooldownNanos() { return COOLDOWN.get() * 1_000_000_000L; }
    private FoxzyConfig() {}
}
