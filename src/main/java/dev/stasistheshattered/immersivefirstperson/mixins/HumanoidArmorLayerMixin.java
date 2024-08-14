package dev.stasistheshattered.immersivefirstperson.mixins;

import com.mojang.math.Axis;
import dev.stasistheshattered.immersivefirstperson.Config;
import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Unique private boolean immersiveFirstPerson$renderHelmet = false;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void renderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBufferSource, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel, CallbackInfo ci) {
        if (!(pLivingEntity instanceof Player player)) return;
        final Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) return;
        // fake player means its the player being rendered in the inventory
        if (player.getData(ModAttachments.FAKE_PLAYER)) return;

        // dont render helmet so it dont block ur vision while u run around slaughtering the innocent wildlife and enslaving villagers
        if (pSlot == EquipmentSlot.HEAD /* && !renderHelmet*/) {
            if (immersiveFirstPerson$renderHelmet) {
                return;
            }
            if (!Config.renderHelmet) {
                ci.cancel();
                return;
            }
            // failed attempt at rendering helmet that i may fix at some point
            immersiveFirstPerson$renderHelmet = true;
            final float headPitch = mc.getEntityRenderDispatcher().camera.getXRot();
            final Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

            PoseStack poseStack = new PoseStack();
            poseStack.last().pose().set(pPoseStack.last().pose());
            poseStack.last().normal().set(pPoseStack.last().normal());

            final float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            final Vec3 eyePos = player.getEyePosition(partialTick);
//            final Vec3 sub = cameraPos.subtract(eyePos).scale(-1);

            poseStack.mulPose(Axis.XP.rotationDegrees(mc.getEntityRenderDispatcher().camera.getXRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(mc.getEntityRenderDispatcher().camera.getYRot()));

            Vec3 sub;
            // this one works
            if (headPitch >= 0) {
//                System.out.println(">= 0");
                sub = cameraPos.subtract(eyePos).scale(-1);
                final float scalar = 0.25f+(headPitch/60);
                poseStack.translate(sub.x-(sub.x*scalar), sub.y-(sub.y*scalar), sub.z-(sub.z*scalar));
//                System.out.println("SCALAR: "+scalar);
//                System.out.println("TRANSLATION: "+(sub.x-(sub.x*scalar))+", "+(sub.y-(sub.y*scalar))+", "+(sub.z-(sub.z*scalar)));
            // this shit dont work
            } else {
//                System.out.println("< 0");
                sub = cameraPos.subtract(eyePos).scale(-1);
                float scalar = 0.25f+(headPitch/-60);
//                final float headDirectionScalar = 0.5f;
//                final Vector3f scaledHeadDirection = headDirection.mul(headDirectionScalar, headDirectionScalar, headDirectionScalar);
//                poseStack.translate(scaledHeadDirection.x, scaledHeadDirection.y, scaledHeadDirection.z);
                poseStack.translate(sub.x-(sub.x/scalar), sub.y-(sub.y/scalar), sub.z-(sub.z/scalar));
//                System.out.println("SCALAR: "+scalar);
//                System.out.println("TRANSLATION: "+(sub.x-(sub.x*scalar))+", "+(sub.y-(sub.y*scalar))+", "+(sub.z-(sub.z*scalar)));
            }
//            System.out.println(sub);

            poseStack.mulPose(Axis.YP.rotationDegrees(-mc.getEntityRenderDispatcher().camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(-mc.getEntityRenderDispatcher().camera.getXRot()));

            // Render the helmet with the modified copied PoseStack
            invokeRenderArmorPiece(poseStack, pBufferSource, pLivingEntity, pSlot, pPackedLight, pModel);
//            System.out.println();
            immersiveFirstPerson$renderHelmet = false;
            ci.cancel();
        }

    }

    @Invoker("renderArmorPiece")
    abstract void invokeRenderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBufferSource, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel);
}
