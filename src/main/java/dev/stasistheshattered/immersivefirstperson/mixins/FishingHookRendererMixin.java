package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.utils.Vec3Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    @Inject(method = "getPlayerHandPos", at = @At("HEAD"), cancellable = true)
    private void getPlayerHandPos(Player pPlayer, float p_340872_, float pPartialTick, CallbackInfoReturnable<Vec3> cir) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson()) {
            cir.setReturnValue(immersiveFirstPerson$getPlayerHandPos(pPlayer, pPartialTick));
            cir.cancel();
        }
    }

    @Unique
    private Vec3 immersiveFirstPerson$getPlayerHandPos(Player pPlayer, float pPartialTick) {
        int i = pPlayer.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemstack = pPlayer.getMainHandItem();
        if (!itemstack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST)) {
            i = -i;
        }

        final Vec3 bodyDirection = Vec3Utils.getDirectionFromYaw(
                (float) Math.toRadians(pPlayer.getPreciseBodyRotation(pPartialTick)))
                .multiply(1, 0, 1);
        final Vec3 sideways = new Vec3(-bodyDirection.z(), 0, bodyDirection.x());

        final float bobIntensity = -0.05F;
        final float bobFrequency = 0.06666667F;
        final float bob = pPlayer.tickCount + pPartialTick;
        final float bob2 = Mth.sin(bob * bobFrequency) * bobIntensity * i;
        if (pPlayer.isPassenger()) {
            return pPlayer.getEyePosition(pPartialTick)
                    .add(bodyDirection.scale(0.8f))
                    .add(sideways.scale(0.3f*i))
                    .add(0, bob2-0.15, 0);
        } else {
            return pPlayer.getEyePosition(pPartialTick)
                    .add(bodyDirection.scale(0.85f))
                    .add(sideways.scale(0.325f*i))
                    .add(0, bob2-0.4, 0);
        }
    }
}
