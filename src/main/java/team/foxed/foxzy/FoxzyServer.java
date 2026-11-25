package team.foxed.foxzy;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class FoxzyServer {
    private MinecraftServer server;
    private DistanceController controller;
    private long tickStart;
    private long windowStart;
    private double elapsedMs;
    private int ticks;
    private boolean active = true;
    private double lastAverage;

    @SubscribeEvent
    public void started(ServerStartedEvent event) {
        server = event.getServer();
        long now = System.nanoTime();
        controller = new DistanceController(server.getPlayerList().getViewDistance(), now, FoxzyConfig.cooldownNanos());
        windowStart = now;
        elapsedMs = 0;
        ticks = 0;
        active = true;
    }

    @SubscribeEvent
    public void stopped(ServerStoppedEvent event) {
        server = null;
        controller = null;
        tickStart = 0;
    }

    @SubscribeEvent
    public void preTick(ServerTickEvent.Pre event) {
        tickStart = System.nanoTime();
    }

    @SubscribeEvent
    public void postTick(ServerTickEvent.Post event) {
        if (server != event.getServer() || controller == null || tickStart == 0) return;
        long now = System.nanoTime();
        int actual = server.getPlayerList().getViewDistance();
        // Respect administrative/other-mod changes instead of repeatedly overriding them.
        if (actual != controller.current()) controller.rebase(actual, now, FoxzyConfig.cooldownNanos());
        elapsedMs += (now - tickStart) / 1_000_000.0;
        tickStart = 0;
        ticks++;
        if (now - windowStart < 5_000_000_000L) return;
        lastAverage = elapsedMs / ticks;
        elapsedMs = 0;
        ticks = 0;
        windowStart = now;
        if (!active || !FoxzyConfig.SERVER_ENABLED.get()) {
            restore(now);
            return;
        }
        // Do not react to idle servers or their startup preparation.
        if (server.getPlayerCount() == 0) {
            controller.defer(now, FoxzyConfig.cooldownNanos());
            return;
        }
        int next = controller.sample(lastAverage, FoxzyConfig.SERVER_HIGH.get(), FoxzyConfig.SERVER_LOW.get(),
                FoxzyConfig.SERVER_MIN.get(), 3, 6, now, FoxzyConfig.cooldownNanos());
        if (next != actual) {
            server.getPlayerList().setViewDistance(next);
            Foxzy.LOGGER.info("Foxzy server view distance: {} -> {}; tick work: {} ms", actual, next, lastAverage);
        }
    }

    private void restore(long now) {
        if (controller == null || server == null) return;
        int baseline = controller.baseline();
        if (server.getPlayerList().getViewDistance() != baseline) server.getPlayerList().setViewDistance(baseline);
        controller.rebase(baseline, now, FoxzyConfig.cooldownNanos());
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("foxzy").requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("Foxzy: "
                            + (active && FoxzyConfig.SERVER_ENABLED.get() ? "enabled" : "disabled")
                            + ", view=" + context.getSource().getServer().getPlayerList().getViewDistance()
                            + ", baseline=" + (controller == null ? "pending" : controller.baseline())
                            + ", sampled tick work=" + String.format(java.util.Locale.ROOT, "%.2f", lastAverage) + " ms"), false);
                    return 1;
                }))
                .then(Commands.literal("disable").executes(context -> {
                    active = false;
                    restore(System.nanoTime());
                    context.getSource().sendSuccess(() -> Component.literal("Foxzy server adaptation disabled; baseline restored."), true);
                    return 1;
                }))
                .then(Commands.literal("enable").executes(context -> {
                    active = true;
                    if (controller != null) controller.rebase(context.getSource().getServer().getPlayerList().getViewDistance(),
                            System.nanoTime(), FoxzyConfig.cooldownNanos());
                    context.getSource().sendSuccess(() -> Component.literal("Foxzy enabled. The config serverEnabled setting must also be true."), true);
                    return 1;
                })));
    }
}
