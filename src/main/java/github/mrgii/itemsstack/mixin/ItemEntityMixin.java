package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @ModifyExpressionValue(
            method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "CONSTANT", args = "intValue=64")
    )
    private static int replaceDroppedItemStackLimit(int orig) {
        return Integer.MAX_VALUE;
    }
}