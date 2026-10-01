package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import github.mrgii.itemsstack.util.StackSizeOverride;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInstance.class)
public interface ItemInstanceMixin {
    @ModifyReturnValue(method = "getMaxStackSize", at = @At("RETURN"))
    private int replaceMaxCountIfModified(int vanillaMaxCount) {
        ItemStack stack = (ItemStack) (Object) this;

        return StackSizeOverride.getMaxCount(stack, vanillaMaxCount);
    }
}