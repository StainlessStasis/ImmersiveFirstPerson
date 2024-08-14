package dev.stasistheshattered.immersivefirstperson.mixins;

import dev.stasistheshattered.immersivefirstperson.data.ModAttachments;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @ModifyArg(method = "renderEntityInInventoryFollowsMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"),
            index = 9)
    private static LivingEntity modifyEntity(LivingEntity entity) {
        entity.setData(ModAttachments.FAKE_PLAYER, true);
        return entity;
    }
}
