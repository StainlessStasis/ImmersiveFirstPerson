package dev.stasistheshattered.immersivefirstperson.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.stasistheshattered.immersivefirstperson.Config;
import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public class ElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing,
                       float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {

        if (!(pLivingEntity instanceof Player player)) return;
        final Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) return;
        // fake player means its the player being rendered in the inventory
        final boolean isFakePlayer = player.getData(ModAttachments.FAKE_PLAYER);

        // dont render elytra while flying
        if (!isFakePlayer && (player.isFallFlying() && !Config.renderElytraWhenGlide)) {
            ci.cancel();
        }
    }
}
