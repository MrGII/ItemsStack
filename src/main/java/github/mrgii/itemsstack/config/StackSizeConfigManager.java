package github.mrgii.itemsstack.config;

import github.mrgii.itemsstack.ItemsStack;
import github.mrgii.itemsstack.util.StackSizeOverride;
import io.wispforest.owo.config.Option;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class StackSizeConfigManager {
    private StackSizeConfigManager() {}

    public static void rebuildOverrides() {
        rebuildOverrides(ItemsStack.CONFIG.overrides());
    }

    public static int reloadFromDisk() {
        ItemsStack.CONFIG.load();
        rebuildOverrides();

        return ItemsStack.CONFIG.overrides().size();
    }

    public static int saveToDisk() {
        ItemsStack.CONFIG.save();

        return ItemsStack.CONFIG.overrides().size();
    }

    public static void registerCallback() {
        Option<List<String>> overridesOption = ItemsStack.CONFIG.optionForKey(ItemsStack.CONFIG.keys.overrides);

        if (overridesOption == null) {
            ItemsStack.LOGGER.error(
                    "Could not register stack size config callback"
            );
            return;
        }

        overridesOption.observe(StackSizeConfigManager::rebuildOverrides);
    }

    public enum RuleFilter {
        ALL,
        ITEMS,
        TAGS
    }

    private static RuleFilter getRuleType(String rule) {
        String selector = getRuleSelector(rule);

        if (selector == null)
            selector = rule.trim();

        return selector.startsWith("#") ? RuleFilter.TAGS : RuleFilter.ITEMS;
    }

    private static boolean matchesFilter(String rule, RuleFilter filter) {
        return filter == RuleFilter.ALL
                || getRuleType(rule) == filter;
    }

    public static List<String> getRules(RuleFilter filter) {
        return ItemsStack.CONFIG.overrides().stream()
                .filter(rule -> matchesFilter(rule, filter))
                .toList();
    }

    public static void setRule(String selector, int maxCount) {
        if (maxCount < 1) {
            ItemsStack.LOGGER.error(
                    "Invalid max stack size {} for '{}'",
                    maxCount, selector
            );
            return;
        }

        replaceRule(selector, Integer.toString(maxCount));
    }

    public static void resetRule(String selector) {
        replaceRule(selector, "default");
    }

    public static int removeRule(String selector) {
        List<String> rules = new ArrayList<>(ItemsStack.CONFIG.overrides());
        int removed = removeRules(rules, selector);

        if (removed != 0) {
            ItemsStack.CONFIG.overrides(List.copyOf(rules));
        }

        return removed;
    }

    public static int clearRules(RuleFilter filter) {
        List<String> rules = new ArrayList<>(ItemsStack.CONFIG.overrides());
        int oldSize = rules.size();

        rules.removeIf(rule -> matchesFilter(rule, filter));

        int removed = oldSize - rules.size();

        if (removed != 0) {
            ItemsStack.CONFIG.overrides(List.copyOf(rules));
        }

        return removed;
    }

    private static void replaceRule(String selector, String value) {
        List<String> rules = new ArrayList<>(ItemsStack.CONFIG.overrides());

        removeRules(rules, selector);
        rules.add(selector + "=" + value);

        ItemsStack.CONFIG.overrides(List.copyOf(rules));
    }

    private static int removeRules(List<String> rules, String selector) {
        int oldSize = rules.size();

        rules.removeIf(rule -> selector.equals(getRuleSelector(rule)));

        return oldSize - rules.size();
    }

    private static String getRuleSelector(String rule) {
        int separator = rule.indexOf('=');

        if (separator == -1) return null;

        return rule.substring(0, separator).trim();
    }

    private static void rebuildOverrides(List<String> rules) {
        Map<Item, Integer> itemOverrides = new HashMap<>();
        Map<Item, Integer> tagOverrides = new HashMap<>();

        for (String rule : rules)
            parseRule(rule, itemOverrides, tagOverrides);

        StackSizeOverride.setOverrides(itemOverrides, tagOverrides);
    }

    private static void parseRule(String rule, Map<Item, Integer> itemOverrides, Map<Item, Integer> tagOverrides) {
        String[] parts = rule.split("=", 2);

        if (parts.length != 2) {
            ItemsStack.LOGGER.error(
                    "Invalid stack size rule '{}': expected selector=value",
                    rule
            );
            return;
        }

        String selector = parts[0].trim();
        OverrideValue value = parseValue(parts[1].trim(), rule);

        if (value == null) return;

        if (selector.startsWith("#"))
            addTagOverride(selector.substring(1), value, tagOverrides);
        else
            addItemOverride(selector, value, itemOverrides);
    }

    private static OverrideValue parseValue(String value, String rule) {
        if (value.equalsIgnoreCase("default")) {
            return new OverrideValue(true, 0);
        }

        int maxCount;

        try {
            maxCount = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            ItemsStack.LOGGER.error(
                    "Invalid stack size '{}' in rule '{}'",
                    value, rule
            );
            return null;
        }

        if (maxCount < 1) {
            ItemsStack.LOGGER.error(
                    "Invalid max stack size {} in rule '{}'",
                    maxCount, rule
            );
            return null;
        }

        return new OverrideValue(false, maxCount);
    }

    private static void addItemOverride(String itemName, OverrideValue value, Map<Item, Integer> itemOverrides) {
        Identifier identifier = Identifier.tryParse(itemName);

        if (identifier == null) {
            ItemsStack.LOGGER.error(
                    "Invalid item identifier '{}'",
                    itemName
            );
            return;
        }

        BuiltInRegistries.ITEM.get(identifier).ifPresentOrElse(
                itemRef -> {
                    Item item = itemRef.value();
                    itemOverrides.put(item, value.resolve(item));
                },
                () -> ItemsStack.LOGGER.warn(
                        "Unknown item '{}'",
                        itemName
                )
        );
    }

    private static void addTagOverride(String tagName, OverrideValue value, Map<Item, Integer> tagOverrides) {
        Identifier identifier = Identifier.tryParse(tagName);

        if (identifier == null) {
            ItemsStack.LOGGER.error(
                    "Invalid item tag identifier '#{}'",
                    tagName
            );
            return;
        }

        TagKey<Item> tag = TagKey.create(Registries.ITEM, identifier);

        boolean found = false;

        for (Holder<Item> entry : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            Item item = entry.value();

            tagOverrides.put(item, value.resolve(item));

            found = true;
        }

        if (!found) {
            ItemsStack.LOGGER.warn(
                    "Item tag '#{}' is empty or does not exist",
                    tagName
            );
        }
    }

    private record OverrideValue(boolean useDefault, int count) {
        int resolve(Item item) {
            return useDefault ? item.getDefaultMaxStackSize() : count;
        }
    }
}