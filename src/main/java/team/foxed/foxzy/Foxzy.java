package team.foxed.foxzy;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Foxzy.MOD_ID)
public final class Foxzy {
    public static final String MOD_ID = "foxzy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Foxzy(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, FoxzyConfig.SPEC);
        NeoForge.EVENT_BUS.register(new FoxzyServer());
        LOGGER.info("Foxzy loaded. Sable present: {}. Compatibility requires in-game testing.",
                ModList.get().isLoaded("sable"));
    }
}
