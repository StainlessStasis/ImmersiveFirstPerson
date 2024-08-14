package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.Config;
import dev.stasistheshattered.immersivefirstperson.utils.Vec3Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Entity entity;
    @Shadow public abstract Vec3 getPosition();
    @Shadow protected abstract void setPosition(Vec3 pPos);
    @Shadow protected abstract void setRotation(float pYRot, float pXRot, float roll);
    @Shadow public abstract float getYRot();
    @Shadow public abstract float getXRot();
    @Shadow public abstract float getRoll();

    @Unique Vec3 attachedCameraPos = new Vec3(0, 0, 0);
    @Unique private boolean collisionDetected = false;

    // Used to interpolate the camera offset when using a shield
    @Unique private boolean alreadyInterpolated = false;
    @Unique private int interpolationTicksRemaining = 0;

    @Inject(method = "setup", at = @At("TAIL"), cancellable = true)
    public void setup(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
        final Minecraft mc = Minecraft.getInstance();
        if (!(entity instanceof Player player) || !mc.options.getCameraType().isFirstPerson()) return;
        // Swimming/Elytra/Riptide/Crawling use pose stack translation instead of camera offsets - see ItemInHandRendererMixin
        // why is it trying to autocorrect elytra to flytrap dumbass IDE
        if (player.isFallFlying() || player.isAutoSpinAttack() || player.isVisuallyCrawling() || player.isVisuallySwimming()) return;

        // sleepy
        if (player.isSleeping()) {
            setRotation(getYRot(), getXRot()-(float)Config.sleepPitchRotation, getRoll());
            ci.cancel();
            return;
        }

        final float yawRadians = (float) Math.toRadians(player.getViewYRot(pPartialTick));
        final Vec3 headDirectionPitchLocked = Vec3Utils.getDirectionFromYaw(yawRadians);

        if (!pDetached) {
            attachedCameraPos = getPosition();
            invokeSetup(pLevel, pEntity, true, pThirdPersonReverse, pPartialTick);
            ci.cancel();
            return;
        }

        double cameraForwardOffset = Config.cameraForwardOffset;

        // quick shield fix i made at 4 am
        if (player.isBlocking() && cameraForwardOffset > 0.2) {
            double shiftBack = cameraForwardOffset - 0.2;
            if (shiftBack > 0.2) shiftBack = 0.2;

            interpolationTicksRemaining--;
            if (interpolationTicksRemaining <= 0 && !alreadyInterpolated) {
                interpolationTicksRemaining = 5 + (int)(2.5d * (mc.options.framerateLimit().get()/60d));
//                System.out.println("fps limit: "+mc.options.framerateLimit());
//                System.out.println("interpolation thing: "+interpolationTicksRemaining);
                alreadyInterpolated = true;
            }

            if (interpolationTicksRemaining > 0) {
                cameraForwardOffset -= shiftBack / interpolationTicksRemaining;
            } else {
                cameraForwardOffset -= shiftBack;
            }
        } else if (!player.isBlocking()) {
            alreadyInterpolated = false;
        }

        // General movement/Riding
        double sidewaysOffset = Config.cameraSidewaysOffset;
        Vec3 sideways = new Vec3(-headDirectionPitchLocked.z(), 0, headDirectionPitchLocked.x());
        Vec3 offset = Vec3Utils.clone(headDirectionPitchLocked).multiply(cameraForwardOffset, 0d, cameraForwardOffset)
                .add(sideways.multiply(sidewaysOffset, 0d, sidewaysOffset)) // Apply the sideways offset
                .add(0d, Config.cameraUpwardOffset, 0d);

        Vec3 newCameraPos = immersiveFirstPerson$calculateCameraPosition(player, offset);
        if (collisionDetected) {
            setPosition(newCameraPos);
        } else {
            setPosition(attachedCameraPos.add(offset));
        }
    }

    @Invoker("setup")
    public abstract void invokeSetup(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick);

    @Unique
    public Vec3 immersiveFirstPerson$calculateCameraPosition(Player player, Vec3 offset) {
        collisionDetected = false;
        Level world = player.level();
        Vec3 cameraPos = Vec3Utils.clone(attachedCameraPos);
        Vec3 adjustedPos = cameraPos.add(offset);

        BlockHitResult result = world.clip(new ClipContext(cameraPos, adjustedPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        adjustedPos = result.getLocation();

        AABB cameraBox = new AABB(adjustedPos.subtract(0.25, 0.25, 0.25), adjustedPos.add(0.25, 0.25, 0.25));
        Iterable<VoxelShape> blockCollisions = world.getBlockCollisions(player, cameraBox);
        for (VoxelShape shape : blockCollisions) {
            if (!shape.isEmpty() && shape.bounds().intersects(cameraBox)) {
                collisionDetected = true;

                // Adjust camera pos when collision
                double adjustmentFactor = 0.25;
                while (shape.bounds().intersects(cameraBox) && adjustmentFactor > 0) {
                    adjustedPos = cameraPos.add(offset.scale(adjustmentFactor));
                    cameraBox = new AABB(adjustedPos.subtract(0.1, 0.1, 0.1), adjustedPos.add(0.1, 0.1, 0.1));
                    adjustmentFactor -= 0.01; // Slowly reduce until no more collision
                }
                break;
            }
        }

        return adjustedPos;
    }
}
