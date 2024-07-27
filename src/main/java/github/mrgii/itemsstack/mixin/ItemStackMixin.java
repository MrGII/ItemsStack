package github.mrgii.itemsstack.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyExpressionValue(method = "method_57371", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/dynamic/Codecs;rangedInt(II)Lcom/mojang/serialization/Codec;"))
    private static Codec<Integer> replaceCodec(Codec<Integer> orig) {
        return Codecs.rangedInt(1, Integer.MAX_VALUE);
    }
}