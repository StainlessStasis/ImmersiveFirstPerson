package dev.stasistheshattered.immersivefirstperson.data;

import dev.stasistheshattered.immersivefirstperson.ImmersiveFirstPerson;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ImmersiveFirstPerson.MODID);

    public static final Supplier<AttachmentType<Boolean>> FAKE_PLAYER = ATTACHMENT_TYPES.register(
            "is_fake_player", () -> AttachmentType.builder(() -> false).build());
    public static final Supplier<AttachmentType<Float>> RIGHT_HAND_PITCH = ATTACHMENT_TYPES.register(
            "right_hand_pitch", () -> AttachmentType.builder(() -> 0f).build());
    public static final Supplier<AttachmentType<Float>> LEFT_HAND_PITCH = ATTACHMENT_TYPES.register(
            "left_hand_pitch", () -> AttachmentType.builder(() -> 0f).build());
}
