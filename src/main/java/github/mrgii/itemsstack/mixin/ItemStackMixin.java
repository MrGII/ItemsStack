package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import github.mrgii.itemsstack.util.StackSizeOverride;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyExpressionValue(
            method = "method_57371",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ExtraCodecs;intRange(II)Lcom/mojang/serialization/Codec;")
    )
    private static Codec<Integer> replaceCodecLimit(Codec<Integer> orig) {
        return ExtraCodecs.intRange(1, Integer.MAX_VALUE);
    }

    @ModifyReturnValue(method = "getMaxStackSize", at = @At("RETURN"))
    private int replaceMaxCountIfModified(int vanillaMaxCount) {
        ItemStack stack = (ItemStack) (Object) this;

        return StackSizeOverride.getMaxCount(stack, vanillaMaxCount);
    }
}