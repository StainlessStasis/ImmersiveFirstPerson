package dev.stasistheshattered.immersivefirstperson;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ImmersiveFirstPerson.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue CAMERA_FORWARD_OFFSET = BUILDER
            .comment("Offsets the camera forward while in first person. Does not work with swimming, gliding, riptide, or crawling. May be buggy at higher values. WARNING: Using offsets may be considered cheating on some servers! Please set all offsets to default values unless your server says it's okay.")
            .defineInRange("cameraForwardOffset", 0.45d, -128d, 128d);
    private static final ModConfigSpec.DoubleValue CAMERA_SIDEWAYS_OFFSET = BUILDER
            .comment("Offsets the camera sideways while in first person. Does not work with swimming, gliding, riptide, or crawling. May be buggy at higher values. WARNING: Using offsets may be considered cheating on some servers! Please set all offsets to default values unless your server says it's okay.")
            .defineInRange("cameraSidewaysOffset", 0d, -128d, 128d);
    private static final ModConfigSpec.DoubleValue CAMERA_UPWARD_OFFSET = BUILDER
            .comment("Offsets the camera upward while in first person. Does not work with swimming, gliding, riptide, or crawling. May be buggy at higher values. WARNING: Using offsets may be considered cheating on some servers! Please set all offsets to default values unless your server says it's okay.")
            .defineInRange("cameraUpwardOffset", 0d, -128, 128);
    private static final ModConfigSpec.DoubleValue SLEEP_PITCH_ROTATION = BUILDER
            .comment("Rotates the camera's pitch when sleeping in a bed. 90 is straight up, and 0 is the same as vanilla.")
            .defineInRange("sleepPitchRotation", 90d, -90d, 90d);
    private static final ModConfigSpec.BooleanValue RENDER_BODY_IN_F1 = BUILDER
            .comment("Renders the model in cinematic (F1 key) mode.")
            .define("renderBodyInF1", true);
    private static final ModConfigSpec.BooleanValue RENDER_ELYTRA_WHEN_GLIDE = BUILDER
            .comment("Renders the elytra while gliding.")
            .define("renderElytraWhenGlide", false);
    private static final ModConfigSpec.BooleanValue FIX_POSE_TRANSITIONS = BUILDER
            .comment("Disables rendering body while transitioning between swimming/crawling/gliding and standing.")
            .define("fixPoseTransitions", true);
    private static final ModConfigSpec.BooleanValue RENDER_HEAD = BUILDER
            .comment("Renders the head, excluding hat layer/helmet.")
            .define("renderHead", false);
    private static final ModConfigSpec.BooleanValue RENDER_HAT = BUILDER
            .comment("Renders the hat layer, which is the outer layer of the head part of the skin.")
            .define("renderHat", false);
    private static final ModConfigSpec.BooleanValue RENDER_HELMET = BUILDER
            .comment("DOES NOT WORK PROPERLY. STILL A WORK IN PROGRESS.")
            .define("renderHelmet", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static double sleepPitchRotation;
    public static double cameraForwardOffset;
    public static double cameraSidewaysOffset;
    public static double cameraUpwardOffset;
    public static boolean renderBodyInF1;
    public static boolean renderHead;
    public static boolean renderHat;
    public static boolean renderHelmet;
    public static boolean renderElytraWhenGlide;
    public static boolean fixPoseTransitions;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        sleepPitchRotation = SLEEP_PITCH_ROTATION.get();
        cameraForwardOffset = CAMERA_FORWARD_OFFSET.get();
        cameraSidewaysOffset = CAMERA_SIDEWAYS_OFFSET.get();
        cameraUpwardOffset = CAMERA_UPWARD_OFFSET.get();
        renderBodyInF1 = RENDER_BODY_IN_F1.get();
        renderHead = RENDER_HEAD.get();
        renderHat = RENDER_HAT.get();
        renderHelmet = RENDER_HELMET.get();
        renderElytraWhenGlide = RENDER_ELYTRA_WHEN_GLIDE.get();
        fixPoseTransitions = FIX_POSE_TRANSITIONS.get();
    }
}
