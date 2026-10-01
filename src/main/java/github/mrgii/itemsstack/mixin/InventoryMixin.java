package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Container.class)
public interface InventoryMixin {
    @ModifyExpressionValue(method = "getMaxStackSize()I", at = @At(value = "CONSTANT", args="intValue=99"))
    private int replaceSlotItemStackLimit(int orig) {
        return Integer.MAX_VALUE;
    }
}