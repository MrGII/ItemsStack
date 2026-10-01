package github.mrgii.itemsstack.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Inject(
            method = "drawStackCount(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V"
            )
    )
    private void scaleLargeStackCount(
            TextRenderer textRenderer,
            ItemStack stack,
            int x,
            int y,
            @Nullable String countOverride,
            CallbackInfo ci
    ) {
        String string = String.valueOf(stack.getCount());
        if (countOverride != null || string.length() <= 2) {
            return;
        }

        float scale = Math.max(2.75F / string.length(), 0.5f);

        Matrix3x2fStack matrices = ((DrawContext) (Object) this).getMatrices();

        float right = x + 17.0F;
        float bottom = y + 9.0F + textRenderer.fontHeight;

        matrices.translate(right, bottom);
        matrices.scale(scale, scale);
        matrices.translate(-right, -bottom);
    }

}