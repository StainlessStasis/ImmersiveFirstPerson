package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import dev.stasistheshattered.immersivefirstperson.utils.Vec3Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
        if (mc.options.getCameraType().isFirstPerson() && pPlayer == mc.player) {
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

        Vec3 bodyDirection = Vec3Utils.getDirectionFromYaw(
                (float) Math.toRadians(pPlayer.getPreciseBodyRotation(pPartialTick)))
                .multiply(1, 0, 1);
        Vec3 sideways = new Vec3(-bodyDirection.z(), 0, bodyDirection.x());

        final float bobIntensity = -0.05F;
        final float bobFrequency = 0.06666667F;
        final float bob = pPlayer.tickCount + pPartialTick;
        final float bob2 = Mth.sin(bob * bobFrequency) * bobIntensity * i;

        final float handPitch = i == 1 ? pPlayer.getData(ModAttachments.RIGHT_HAND_PITCH) : pPlayer.getData(ModAttachments.LEFT_HAND_PITCH);
        final float upwardOffset = -Mth.sin((float) Math.toRadians(handPitch)) * 0.6666667f;

        // i give up
//        final Entity vehicle = pPlayer.getVehicle();
//        if (vehicle != null) {
//            bodyDirection = Vec3Utils.getDirectionFromYaw(
//                            (float) Math.toRadians(vehicle.getPreciseBodyRotation(pPartialTick)))
//                    .multiply(1, 0, 1);
//            sideways = new Vec3(-bodyDirection.z(), 0, bodyDirection.x());
//            return pPlayer.position()
//                    .add(bodyDirection.scale(0.85f))
//                    .add(sideways.scale(0.325f * i))
//                    .add(0, upwardOffset + bob2 + 0.8, 0);
//        }
        return pPlayer.getEyePosition(pPartialTick)
                .add(bodyDirection.scale(0.85f))
                .add(sideways.scale(0.325f * i))
                .add(0, upwardOffset + bob2 - 0.8, 0);
    }
}
