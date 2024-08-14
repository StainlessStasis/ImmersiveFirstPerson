package dev.stasistheshattered.immersivefirstperson.utils;

import org.joml.Vector3f;

public class Vector3fUtils {
    public static Vector3f clone(Vector3f vectorToClone) {
        return new Vector3f(vectorToClone.x, vectorToClone.y, vectorToClone.z);
    }

    public static Vector3f getDirection(float yawRadians, float pitchRadians) {
        return new Vector3f((float) (-Math.cos(pitchRadians) * Math.sin(yawRadians)),
                (float) -Math.sin(pitchRadians),
                (float) (Math.cos(pitchRadians) * Math.cos(yawRadians))).normalize();
    }

    public static Vector3f getDirectionFromYaw(float yawRadians) {
        return new Vector3f((float)
                -Math.sin(yawRadians),
                0.0f,
                (float) Math.cos(yawRadians)).normalize();
    }
}
