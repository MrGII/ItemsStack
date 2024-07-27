package github.mrgii.itemsstack.util;

import github.mrgii.itemsstack.ItemsStack;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ItemsStackSizeModifier {
    public static void ModifyPotionStackSize(List<String> itemNames, List<Integer> maxStackSizes) {
        Map<String, Integer> itemToMaxStackSize = createItemToStackSizeMap(itemNames, maxStackSizes);
        ItemsStack.LOGGER.info("Changing stack size of minecraft potions.");
        for (Item item : Registries.ITEM) {
            String name = item.getName().getString();
            if (itemNames.contains(name)) {
                Field components;
                try {
                    components = Item.class.getDeclaredField("components");
                } catch (NoSuchFieldException e) {
                    ItemsStack.LOGGER.error("Could not find 'components' field of class 'Item'.");
                    throw  new RuntimeException(e);
                }
                components.setAccessible(true);
                try {
                    components.set(item, ComponentMap.of(item.getComponents(), ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, itemToMaxStackSize.get(name)).build()));
                } catch (IllegalAccessException e) {
                    ItemsStack.LOGGER.error("Could not access 'components' field of class 'Item'.");
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static Map<String, Integer> createItemToStackSizeMap(List<String> itemNames, List<Integer> maxStackSizes) {
        Map<String, Integer> itemToMaxStackSize = new java.util.HashMap<>(Map.of());
        for (int i = 0; i < itemNames.size(); i++) {
            itemToMaxStackSize.put(itemNames.get(i), maxStackSizes.get(i));
        }
        return itemToMaxStackSize;
    }
}
