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
import java.util.List;
import java.util.Map;

public class ItemsStackSizeModifier {
    public static List<String> itemNames;
    public static List<Integer> maxStackSizes;
    public static List<String> itemTagNames;
    public static List<Integer> maxTagStackSizes;

    public static void ModifyItemStackSizes() {
        Map<Item, Integer> itemToMaxStackSize = createItemToStackSizeMap(itemNames, maxStackSizes);
        Map<TagKey<Item>, Integer> itemTagToMaxStackSize = createItemTagToStackSizeMap(itemTagNames, maxTagStackSizes);
        ItemsStack.LOGGER.info("Changing stack size of minecraft item tags.");
        for (TagKey<Item> tagKey : itemTagToMaxStackSize.keySet()) {
            Registries.ITEM.iterateEntries(tagKey).forEach((itemRegistryEntry) -> {
                Item item = itemRegistryEntry.value();
                ((ItemAccessor) item).setComponents(ComponentMap.of(item.getComponents(), ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, itemTagToMaxStackSize.get(tagKey)).build()));
            });
        }
        ItemsStack.LOGGER.info("Changing stack size of minecraft items.");
        for (Item item : itemToMaxStackSize.keySet()) {
            ((ItemAccessor) item).setComponents(ComponentMap.of(item.getComponents(), ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, itemToMaxStackSize.get(item)).build()));
        }
    }

    private static Map<Item, Integer> createItemToStackSizeMap(List<String> itemNames, List<Integer> maxStackSizes) {
        Map<Item, Integer> itemToMaxStackSize = new java.util.HashMap<>(Map.of());
        for (int i = 0; i < Math.min(itemNames.size(), maxStackSizes.size()); i++) {
            itemToMaxStackSize.put(Registries.ITEM.get(Identifier.of(itemNames.get(i))), maxStackSizes.get(i));
        }
        return itemToMaxStackSize;
    }

    private static Map<TagKey<Item>, Integer> createItemTagToStackSizeMap(List<String> itemTagNames, List<Integer> maxStackSizes) {
        Map<String, Integer> itemTagToMaxStackSize = new java.util.HashMap<>(Map.of());
        for (int i = 0; i < Math.min(itemTagNames.size(), maxStackSizes.size()); i++) {
            itemTagToMaxStackSize.put(itemTagNames.get(i), maxStackSizes.get(i));
        }
        Map<TagKey<Item>, Integer> itemTagKeyToMaxStackSize = new java.util.HashMap<>(Map.of());
        Field[] conTagKeyFields = ConventionalItemTags.class.getDeclaredFields();
        for (Field conTagKeyField : conTagKeyFields) {
            conTagKeyField.setAccessible(true);
            TagKey<Item> conTagKey;
            try {
                conTagKey = (TagKey<Item>) conTagKeyField.get(null);
            } catch (IllegalAccessException e) {
                ItemsStack.LOGGER.error("Could not access tagKeyField {}!", conTagKeyField.getName());
                throw new RuntimeException(e);
            }
            String conTagName = "#" + conTagKey.id().toString();
            if (itemTagToMaxStackSize.containsKey(conTagName)) {
                itemTagKeyToMaxStackSize.put(conTagKey, itemTagToMaxStackSize.get(conTagName));
            }
        }
        Field[] tagKeyFields = ItemTags.class.getDeclaredFields();
        for (Field tagKeyField : tagKeyFields) {
            tagKeyField.setAccessible(true);
            TagKey<Item> tagKey;
            try {
                tagKey = (TagKey<Item>) tagKeyField.get(null);
            } catch (IllegalAccessException e) {
                ItemsStack.LOGGER.error("Could not access tagKeyField {}!", tagKeyField.getName());
                throw new RuntimeException(e);
            }
            String tagName = tagKey.getName().getString();
            if (itemTagToMaxStackSize.containsKey(tagName)) {
                itemTagKeyToMaxStackSize.put(tagKey, itemTagToMaxStackSize.get(tagName));
            }
        }
        return itemTagKeyToMaxStackSize;
    }

    public static void updateValuesAndModifyStackSizes() {
        ItemsStackSizeModifier.itemNames = ItemsStack.CONFIG.items();
        ItemsStackSizeModifier.maxStackSizes = ItemsStack.CONFIG.maxStackSizes();
        ItemsStackSizeModifier.itemTagNames = ItemsStack.CONFIG.itemTags();
        ItemsStackSizeModifier.maxTagStackSizes = ItemsStack.CONFIG.maxTagStackSizes();

        ModifyItemStackSizes();
    }

    public static void registerCallbacks() {
        Keys keys = ItemsStack.CONFIG.keys;
        Option<List<String>> itemsOption = ItemsStack.CONFIG.optionForKey(keys.items);
        Option<List<String>> maxStackSizesOption = ItemsStack.CONFIG.optionForKey(keys.maxStackSizes);
        Option<List<String>> itemTagsOption = ItemsStack.CONFIG.optionForKey(keys.itemTags);
        Option<List<String>> maxTagStackSizesOption = ItemsStack.CONFIG.optionForKey(keys.maxTagStackSizes);

        itemsOption.observe(newValue -> ItemsStackSizeModifier.updateValuesAndModifyStackSizes());
        maxStackSizesOption.observe(newValue -> ItemsStackSizeModifier.updateValuesAndModifyStackSizes());
        itemTagsOption.observe(newValue -> ItemsStackSizeModifier.updateValuesAndModifyStackSizes());
        maxTagStackSizesOption.observe(newValue -> ItemsStackSizeModifier.updateValuesAndModifyStackSizes());
    }
}
