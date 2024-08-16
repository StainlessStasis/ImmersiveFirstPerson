package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.Config;
import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerRendererMixin(EntityRendererProvider.Context pContext, PlayerModel<AbstractClientPlayer> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void render(AbstractClientPlayer pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (pEntity != Minecraft.getInstance().player) {
            return;
        }
        immersiveFirstPerson$render(Minecraft.getInstance().player, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        ci.cancel();
    }

    @Unique
    private void immersiveFirstPerson$render(AbstractClientPlayer player, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        invokeSetModelProperties(player);
        final Minecraft mc = Minecraft.getInstance();
        final Options options = mc.options;
        if (options.getCameraType().isFirstPerson()) {
            final boolean isFakePlayer = player.getData(ModAttachments.FAKE_PLAYER);
            final boolean shouldRender = isFakePlayer || !options.hideGui || Config.renderBodyInF1;
            model.setAllVisible(shouldRender);

            // test thing
            Vec3 armPos = new Vec3(model.rightArm.x, model.rightArm.y, model.rightArm.z);
            System.out.println("HAND POS: "+armPos);


            if (!shouldRender) {
                return;
            }

            // Hide head/hat when in first person unless you for some reason want to see it
            model.head.visible = isFakePlayer || Config.renderHead;
            model.hat.visible = isFakePlayer || Config.renderHat;

            // Hide arm when using spyglass
            if (player.isScoping()) {
                final HumanoidArm armUsingSpyglass = player.getMainHandItem().getItem() instanceof SpyglassItem
                        ? player.getMainArm() : player.getMainArm().getOpposite();
                if (armUsingSpyglass == HumanoidArm.RIGHT) {
                    model.rightArm.visible = false;
                    model.rightSleeve.visible = false;
                } else {
                    model.leftArm.visible = false;
                    model.leftSleeve.visible = false;
                }
            }
        }

        final PlayerRenderer renderer = ((PlayerRenderer)(Object)this);
        if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre(player, renderer, pPartialTicks, pPoseStack, pBuffer, pPackedLight)).isCanceled()) return;
        super.render(player, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.RenderPlayerEvent.Post(player, renderer, pPartialTicks, pPoseStack, pBuffer, pPackedLight));
    }

    @Invoker("setModelProperties")
    abstract void invokeSetModelProperties(AbstractClientPlayer pClientPlayer);


}
