package dev.stasistheshattered.immersivefirstperson.data;

import dev.stasistheshattered.immersivefirstperson.ImmersiveFirstPerson;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ImmersiveFirstPerson.MODID);

    public static final Supplier<AttachmentType<Boolean>> FAKE_PLAYER = ATTACHMENT_TYPES.register(
            "is_fake_player", () -> AttachmentType.builder(() -> false).build());
}
