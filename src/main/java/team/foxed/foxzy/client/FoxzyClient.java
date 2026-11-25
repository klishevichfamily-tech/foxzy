package team.foxed.foxzy.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import team.foxed.foxzy.DistanceController;
import team.foxed.foxzy.Foxzy;
import team.foxed.foxzy.FoxzyConfig;

/** Isolated from dedicated-server class loading by the physical-side subscriber filter. */
@EventBusSubscriber(modid = Foxzy.MOD_ID, value = Dist.CLIENT)
public final class FoxzyClient {
    private static DistanceController controller;
    private static long lastSample;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        int actual = mc.options.renderDistance().get();
        if (mc.level == null || !FoxzyConfig.CLIENT_ENABLED.get()) {
            if (controller != null && actual == controller.current()) {
                mc.options.renderDistance().set(controller.baseline());
            }
            controller = null;
            return;
        }
        if (controller == null) {
            controller = new DistanceController(actual, now, FoxzyConfig.cooldownNanos());
            lastSample = now;
        }
        if (actual != controller.current()) controller.rebase(actual, now, FoxzyConfig.cooldownNanos());
        // Menus, pause, and an unfocused window are not evidence of world rendering overload.
        if (mc.screen != null || mc.isPaused() || !mc.isWindowActive()) {
            controller.defer(now, FoxzyConfig.cooldownNanos());
            lastSample = now;
            return;
        }
        if (now - lastSample < 5_000_000_000L) return;
        lastSample = now;
        int fps = mc.getFps();
        if (fps <= 0) return;
        int lowFps = FoxzyConfig.CLIENT_LOW_FPS.get();
        int goodFps = FoxzyConfig.CLIENT_GOOD_FPS.get();
        int cap = mc.options.framerateLimit().get();
        // Avoid treating an explicit low FPS cap as overload.
        if (cap > 0) {
            lowFps = Math.min(lowFps, Math.max(1, (int) (cap * 0.8)));
            goodFps = Math.min(goodFps, Math.max(lowFps + 1, (int) (cap * 0.95)));
        }
        int next = controller.sample(1000.0 / fps, 1000.0 / lowFps, 1000.0 / goodFps,
                FoxzyConfig.CLIENT_MIN.get(), 3, 6, now, FoxzyConfig.cooldownNanos());
        if (next != actual) {
            mc.options.renderDistance().set(next);
            Foxzy.LOGGER.info("Foxzy client render distance: {} -> {}; FPS: {}", actual, next, fps);
        }
    }
    private FoxzyClient() {}
}
