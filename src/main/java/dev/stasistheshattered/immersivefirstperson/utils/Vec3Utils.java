package dev.stasistheshattered.immersivefirstperson.utils;

import net.minecraft.world.phys.Vec3;

public class Vec3Utils {
    public static Vec3 clone(Vec3 vectorToClone) {
        return new Vec3(vectorToClone.x, vectorToClone.y, vectorToClone.z);
    }
    public static Vec3 getDirection(float yawRadians, float pitchRadians) {
        return new Vec3((float) (-Math.cos(pitchRadians) * Math.sin(yawRadians)),
                (float) -Math.sin(pitchRadians),
                (float) (Math.cos(pitchRadians) * Math.cos(yawRadians))).normalize();
    }

    public static Vec3 getDirectionFromYaw(float yawRadians) {
        return new Vec3((float)
                -Math.sin(yawRadians),
                0.0f,
                (float) Math.cos(yawRadians)).normalize();
    }
}
