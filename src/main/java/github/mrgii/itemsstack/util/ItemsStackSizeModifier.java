package github.mrgii.itemsstack.util;

import github.mrgii.itemsstack.ItemsStack;
import github.mrgii.itemsstack.config.ItemsStackSizeConfig.Keys;
import io.wispforest.owo.config.Option;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.mixin.item.ItemAccessor;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.*;

public class ItemsStackSizeModifier {
    public static Map<Item, Integer> oldItemToMaxStackSize = new java.util.HashMap<>(Map.of());
    public static List<String> itemNames;
    public static List<Integer> maxStackSizes;
    public static List<String> itemTagNames;
    public static List<Integer> maxTagStackSizes;

    public static void ModifyItemStackSizes() {
        Map<Item, Integer> itemToMaxStackSize = createItemToStackSizeMap();
        ItemsStack.LOGGER.info("Changing stack size of items.");
        Iterator<Item> oldItemsIterator = oldItemToMaxStackSize.keySet().iterator();
        Set<Item> items = itemToMaxStackSize.keySet();
        while (oldItemsIterator.hasNext()) {
            Item oldItem = oldItemsIterator.next();
            if (!items.contains(oldItem)) {
                ((ItemAccessor) oldItem).setComponents(ComponentMap.builder()
                        .addAll(oldItem.getComponents())
                        .add(DataComponentTypes.MAX_STACK_SIZE, oldItemToMaxStackSize.get(oldItem))
                        .build());
                oldItemsIterator.remove();
            }
        }
        for (Item item : items) {
            oldItemToMaxStackSize.putIfAbsent(item, item.getMaxCount());
            ((ItemAccessor) item).setComponents(ComponentMap.builder()
                    .addAll(item.getComponents())
                    .add(DataComponentTypes.MAX_STACK_SIZE, itemToMaxStackSize.get(item))
                    .build());
        }
    }

    private static Map<Item, Integer> createItemToStackSizeMap() {
        Map<Item, Integer> itemToMaxStackSize = new java.util.HashMap<>(Map.of());
        for (int i = 0; i < Math.min(itemNames.size(), maxStackSizes.size()); i++) {
            itemToMaxStackSize.put(Registries.ITEM.get(Identifier.of(itemNames.get(i))), maxStackSizes.get(i));
        }
        Map<String, Integer> itemTagNameToMaxStackSize = new java.util.HashMap<>(Map.of());
        for (int i = 0; i < Math.min(itemTagNames.size(), maxTagStackSizes.size()); i++) {
            itemTagNameToMaxStackSize.put(itemTagNames.get(i), maxTagStackSizes.get(i));
        }
        List<Field> tagKeyFields = new java.util.ArrayList<>(List.of());
        tagKeyFields.addAll(Arrays.stream(ItemTags.class.getDeclaredFields()).toList());
        List<Field> conTagKeyFields = new java.util.ArrayList<>(List.of());
        conTagKeyFields.addAll(Arrays.stream(ConventionalItemTags.class.getDeclaredFields()).toList());
        for (Field tagKeyField : tagKeyFields) {
            tagKeyField.setAccessible(true);
            TagKey<Item> tagKey;
            try {
                tagKey = (TagKey<Item>) tagKeyField.get(null);
            } catch (IllegalAccessException e) {
                ItemsStack.LOGGER.error("Could not access Field TagKey<Item> '{}'!", tagKeyField.getName());
                throw new RuntimeException(e);
            }
            String tagName = "#" + tagKey.id().toString();
            if (itemTagNames.contains(tagName)) {
                Registries.ITEM.iterateEntries(tagKey).forEach((itemRegistryEntry) -> {
                    Item item = itemRegistryEntry.value();
                    itemToMaxStackSize.putIfAbsent(item, itemTagNameToMaxStackSize.get(tagName));
                });
            }
        }
        for (Field conTagKeyField : conTagKeyFields) {
            conTagKeyField.setAccessible(true);
            TagKey<Item> conTagKey;
            try {
                conTagKey = (TagKey<Item>) conTagKeyField.get(null);
            } catch (IllegalAccessException e) {
                ItemsStack.LOGGER.error("Could not access Field TagKey<Item> '{}'!", conTagKeyField.getName());
                throw new RuntimeException(e);
            }
            String conTagName = "#" + conTagKey.id().toString();
            if (itemTagNames.contains(conTagName)) {
                Registries.ITEM.iterateEntries(conTagKey).forEach((itemRegistryEntry) -> {
                    Item item = itemRegistryEntry.value();
                    itemToMaxStackSize.putIfAbsent(item, itemTagNameToMaxStackSize.get(conTagName));
                });
            }
        }
        return itemToMaxStackSize;
    }

    public static void updateValuesAndModifyStackSizes() {
        ItemsStackSizeModifier.itemNames = ItemsStack.CONFIG.items();
        ItemsStackSizeModifier.maxStackSizes = ItemsStack.CONFIG.maxStackSizes();
        ItemsStackSizeModifier.itemTagNames = ItemsStack.CONFIG.itemTags();
        ItemsStackSizeModifier.maxTagStackSizes = ItemsStack.CONFIG.maxTagStackSizes();

        ModifyItemStackSizes();
    }

    public static void registerCallbacks() {
        Keys optionKeys = ItemsStack.CONFIG.keys;
        Option<List<String>> itemsOption = ItemsStack.CONFIG.optionForKey(optionKeys.items);
        Option<List<Integer>> maxStackSizesOption = ItemsStack.CONFIG.optionForKey(optionKeys.maxStackSizes);
        Option<List<String>> itemTagsOption = ItemsStack.CONFIG.optionForKey(optionKeys.itemTags);
        Option<List<Integer>> maxTagStackSizesOption = ItemsStack.CONFIG.optionForKey(optionKeys.maxTagStackSizes);

        itemsOption.observe(newValue -> {
            ItemsStack.CONFIG.items(newValue);
            updateValuesAndModifyStackSizes();
        });
        maxStackSizesOption.observe(newValue -> {
            ItemsStack.CONFIG.maxStackSizes(newValue);
            updateValuesAndModifyStackSizes();
        });
        itemTagsOption.observe(newValue -> {
            ItemsStack.CONFIG.itemTags(newValue);
            updateValuesAndModifyStackSizes();
        });
        maxTagStackSizesOption.observe(newValue -> {
            ItemsStack.CONFIG.maxTagStackSizes(newValue);
            updateValuesAndModifyStackSizes();
        });
    }
}
