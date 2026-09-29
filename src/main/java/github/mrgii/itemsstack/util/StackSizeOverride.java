package github.mrgii.itemsstack.util;

import github.mrgii.itemsstack.ItemsStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public final class StackSizeOverride {
    private static Map<Item, Integer> itemOverrides = Map.of();
    private static Map<Item, Integer> tagOverrides = Map.of();

    private StackSizeOverride() {
    }

    public static void setOverrides(
            Map<Item, Integer> newItemOverrides,
            Map<Item, Integer> newTagOverrides
    ) {
        itemOverrides = validateOverrides(newItemOverrides);
        tagOverrides = validateOverrides(newTagOverrides);
    }

    private static Map<Item, Integer> validateOverrides(
            Map<Item, Integer> newOverrides
    ) {
        Map<Item, Integer> checkedOverrides = new HashMap<>();

        for (Map.Entry<Item, Integer> entry : newOverrides.entrySet()) {
            if (entry.getValue() < 1) {
                ItemsStack.LOGGER.error(
                        "Invalid max stack size {} for item {}",
                        entry.getValue(),
                        Registries.ITEM.getId(entry.getKey())
                );
                continue;
            }

            checkedOverrides.put(entry.getKey(), entry.getValue());
        }

        return Map.copyOf(checkedOverrides);
    }

    private static boolean hasExplicitMaxStackSize(ItemStack stack) {
        // null means this stack did not explicitly change this component in any way,
        // present optional means modified value and empty optional means removed component.
        return stack.getComponentChanges()
                .get(DataComponentTypes.MAX_STACK_SIZE) != null;
    }

    public static OptionalInt getConfiguredMaxCount(ItemStack stack) {
        if (hasExplicitMaxStackSize(stack)) {
            return OptionalInt.empty();
        }

        Item item = stack.getItem();

        Integer maxCount = itemOverrides.get(item);

        if (maxCount == null) {
            maxCount = tagOverrides.get(item);
        }

        if (maxCount == null) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(maxCount);
    }

    public static int getMaxCount(ItemStack stack, int vanillaMaxCount) {
        return getConfiguredMaxCount(stack).orElse(vanillaMaxCount);
    }
}