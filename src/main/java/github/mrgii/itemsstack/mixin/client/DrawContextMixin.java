package github.mrgii.itemsstack.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Inject(
            method = "Lnet/minecraft/client/gui/DrawContext;drawStackCount(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
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

        MatrixStack matrices = ((DrawContext) (Object) this).getMatrices();

        float right = x + 17.0F;
        float bottom = y + 9.0F + textRenderer.fontHeight;

        matrices.translate(right, bottom, 0.0F);
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(-right, -bottom, 0.0F);
    }
}