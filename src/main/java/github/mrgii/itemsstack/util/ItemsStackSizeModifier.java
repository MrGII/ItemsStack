package github.mrgii.itemsstack.util;

import github.mrgii.itemsstack.ItemsStack;
import net.fabricmc.fabric.mixin.item.ItemAccessor;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.List;
import java.util.Map;

public class ItemsStackSizeModifier {
    public static void ModifyPotionStackSize(List<String> itemNames, List<Integer> maxStackSizes) {
        Map<String, Integer> itemToMaxStackSize = createItemToStackSizeMap(itemNames, maxStackSizes);
        ItemsStack.LOGGER.info("Changing stack size of minecraft potions.");
        for (Item item : Registries.ITEM) {
            String name = item.getName().getString();
            if (itemToMaxStackSize.containsKey(name)) {
                ((ItemAccessor) item).setComponents(ComponentMap.of(item.getComponents(), ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, itemToMaxStackSize.get(name)).build()));
            }
        }
    }

    private static Map<String, Integer> createItemToStackSizeMap(List<String> itemNames, List<Integer> maxStackSizes) {
        Map<String, Integer> itemToMaxStackSize = new java.util.HashMap<>(Map.of());
        for (int i = 0; i < Math.min(itemNames.size(), maxStackSizes.size()); i++) {
            itemToMaxStackSize.put(itemNames.get(i), maxStackSizes.get(i));
        }
        return itemToMaxStackSize;
    }
}
