package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public interface InventoryMixin {
    @ModifyExpressionValue(method = "getMaxCountPerStack", at = @At(value = "CONSTANT", args="intValue=99"))
    private int changeMaxSlotStackSize(int orig) {
        return Integer.MAX_VALUE;
    }
}