package dev.stasistheshattered.immersivefirstperson.events;

import dev.stasistheshattered.immersivefirstperson.ImmersiveFirstPerson;
import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = ImmersiveFirstPerson.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onPostRenderFrame(RenderFrameEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.setData(ModAttachments.FAKE_PLAYER, false);
        player.setData(ModAttachments.RIGHT_HAND_PITCH, 0f);
        player.setData(ModAttachments.LEFT_HAND_PITCH, 0f);
    }
}
