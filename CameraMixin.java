package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.Config;
import dev.stasistheshattered.immersivefirstperson.utils.Vec3Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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

    @Shadow private float partialTickTime;
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
                .add(sideways.multiply(sidewaysOffset, 0d, sidewaysOffset))
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
        final Vec3 hitLoc = result.getLocation();
        final float directionYaw = result.getDirection().toYRot();
        System.out.println("direction yaw: "+directionYaw);
        adjustedPos = hitLoc;

        // don't ask, it just works
        final double yawDif = directionYaw - Math.abs(player.getViewYRot(partialTickTime));
        final double distanceCheck = Mth.square(Vec3Utils.greatestAbsoluteValue(offset)) * (Math.abs(yawDif)/32);
        double offsetX = offset.x;
        double offsetY = offset.y;
        double offsetZ = offset.z;

        AABB cameraBox = new AABB(adjustedPos.subtract(offsetX, offsetY, offsetZ), adjustedPos.add(offsetX, offsetY, offsetZ));
        Iterable<VoxelShape> blockCollisions = world.getBlockCollisions(player, cameraBox);
        for (VoxelShape shape : blockCollisions) {
            if (!shape.isEmpty() && shape.bounds().intersects(cameraBox)) {
                System.out.println("COLLISION");
                collisionDetected = true;

                // Adjust camera pos when collision
                double adjustmentFactor = 0.99;
                final double scalar = 0.01;
                while ((shape.bounds().intersects(cameraBox) || adjustedPos.distanceToSqr(hitLoc) <= distanceCheck) && adjustmentFactor > 0) {
                    System.out.println("ADJUSTMENT FACTOR: "+adjustmentFactor);
                    System.out.println("DISTANCE: "+adjustedPos.distanceToSqr(hitLoc));
                    System.out.println("DISTANCE CHECK: "+distanceCheck);
                    System.out.println("TRUE?: "+(adjustedPos.distanceToSqr(hitLoc) <= distanceCheck));
                    adjustedPos = cameraPos.add(Vec3Utils.clone(offset).scale(adjustmentFactor));
                    offsetX -= offset.x * scalar;
                    offsetY -= offset.y * scalar;
                    offsetZ -= offset.z * scalar;
                    cameraBox = new AABB(adjustedPos.subtract(offsetX, offsetY, offsetZ), adjustedPos.add(offsetX, offsetY, offsetZ));
                    adjustmentFactor -= scalar; // Slowly reduce until no more collision
                }
                System.out.println("LOOP BROKEN");
                break;
            }
        }

        return adjustedPos;
    }

    @Unique
    public Vec3 immersiveFirstPerson$rotatePoint(Vec3 point, Vec3 center, double yaw) {
        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double dx = point.x - center.x;
        double dz = point.z - center.z;

        double newX = center.x + (dx * cos - dz * sin);
        double newZ = center.z + (dx * sin + dz * cos);

        return new Vec3(newX, point.y, newZ);
    }
}
