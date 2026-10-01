package github.mrgii.itemsstack.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class DrawContextMixin {
    @Inject(
            method = "renderItemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"
            )
    )
    private void scaleLargeStackCount(
            Font textRenderer,
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

        Matrix3x2fStack matrices = ((GuiGraphics) (Object) this).pose();

        float right = x + 17.0F;
        float bottom = y + 9.0F + textRenderer.lineHeight;

        matrices.translate(right, bottom);
        matrices.scale(scale, scale);
        matrices.translate(-right, -bottom);
    }

}