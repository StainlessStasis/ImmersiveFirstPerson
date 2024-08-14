package dev.stasistheshattered.immersivefirstperson;

import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ImmersiveFirstPerson.MODID)
public class ImmersiveFirstPerson
{
    public static final String MODID = "immersivefirstperson";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ImmersiveFirstPerson(IEventBus modEventBus, ModContainer modContainer)
    {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
