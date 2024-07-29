package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @ModifyExpressionValue(method = "Lnet/minecraft/entity/ItemEntity;merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "CONSTANT", args = "intValue=64"))
    private static int replaceCodec(int orig) {
        return Integer.MAX_VALUE;
    }
}