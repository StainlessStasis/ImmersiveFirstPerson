package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.utils.Vector3fUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow @Final private ItemRenderer itemRenderer;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    public void renderHandsWithItems(float pPartialTicks, PoseStack pPoseStack, MultiBufferSource.BufferSource pBuffer, LocalPlayer pPlayerEntity, int pCombinedLight, CallbackInfo ci) {
        immersiveFirstPerson$renderHandsWithItems(pPartialTicks, pPoseStack, pBuffer, pPlayerEntity, pCombinedLight);
        ci.cancel();
    }

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    public void renderItem(LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pSeed, CallbackInfo ci) {
        immersiveFirstPerson$renderItem(pEntity, pItemStack, pDisplayContext, pLeftHand, pPoseStack, pBuffer, pSeed);
        ci.cancel();
    }

    @Unique
    private void immersiveFirstPerson$renderHandsWithItems(float pPartialTicks, PoseStack pPoseStack, MultiBufferSource.BufferSource pBuffer, LocalPlayer pPlayerEntity, int pCombinedLight) {
        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (player == null)
            throw new NullPointerException("Somehow the player is null and you can't render the body with a null player");

        if (!mc.options.getCameraType().isFirstPerson()) return;
        // Fix rendering 2 bodies at once when using camera offsets
        if (mc.gameRenderer.getMainCamera().isDetached()) {
            return;
        }

        final EntityRenderer<? super Player> renderer = mc.getEntityRenderDispatcher().getRenderer(player);

        // Pose setup
        final float bodyYaw = player.getPreciseBodyRotation(pPartialTicks);
        final float headYaw = player.getYRot();
        final float headPitchRadians = (float) Math.toRadians(mc.getEntityRenderDispatcher().camera.getXRot());
        final float headYawRadians = (float) Math.toRadians(headYaw);

        Quaternionf rotation = new Quaternionf().rotateX(headPitchRadians);
        pPoseStack.mulPose(rotation);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180 + headYaw));

        final Vector3f headDirection = Vector3fUtils.getDirection(headYawRadians, headPitchRadians);
        final Vector3f bodyDirectionPitchLocked = Vector3fUtils.getDirectionFromYaw((float) Math.toRadians(player.getPreciseBodyRotation(pPartialTicks)));
        Vector3f translation = new Vector3f(0f, 0f, 0f);

        // Swimming
        if (player.isVisuallySwimming()) {
            translation = Vector3fUtils.clone(headDirection).mul(-0.3f);
        }

        // Crawling
        if (player.isVisuallyCrawling()) {
            translation = Vector3fUtils.clone(bodyDirectionPitchLocked).mul(-0.6f);
            translation.add(0, -0.4f, 0);
        }

        // Elytra/Riptide
        // STOP TRYING TO AUTOCORRECT TO FLYTRAP
        if (player.isFallFlying() || player.isAutoSpinAttack()) {
            translation = Vector3fUtils.clone(headDirection).mul(-1.5f);
        }

        pPoseStack.translate(translation.x, translation.y, translation.z);
        renderer.render(player, bodyYaw, pPartialTicks, pPoseStack, pBuffer, pCombinedLight);
        pBuffer.endBatch();
    }

    @Unique
    private void immersiveFirstPerson$renderItem(LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pSeed) {
        // Don't render spyglass in hand when using it
        // i tried making this shit only hide the one in the hand using it
        // but for some reason pLeftHand is ALWAYS false in the if statement below
        if (pEntity instanceof Player player && player.isScoping() && pItemStack.getItem() instanceof SpyglassItem) {
            return;
        }
        if (!pItemStack.isEmpty()) {
            itemRenderer.renderStatic(pEntity, pItemStack, pDisplayContext, pLeftHand, pPoseStack,
                    pBuffer, pEntity.level(), pSeed, OverlayTexture.NO_OVERLAY, pEntity.getId() + pDisplayContext.ordinal());
        }
    }
}