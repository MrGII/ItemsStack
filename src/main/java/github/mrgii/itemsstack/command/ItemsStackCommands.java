package github.mrgii.itemsstack.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import github.mrgii.itemsstack.config.StackSizeConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

public final class ItemsStackCommands {
    private static int rulesPerPage = 10;

    private ItemsStackCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        CommandManager.literal("itemsstack")
                                .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("target", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.ITEM))
                                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> {
                                                            var target = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "target", RegistryKeys.ITEM);

                                                            int count = IntegerArgumentType.getInteger(context, "count");
                                                            StackSizeConfigManager.setRule(target.asString(), count);

                                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                                    "command.itemsstack.set.success",
                                                                    "Set %s max stack size to %s",
                                                                    target.asString(), count
                                                            ), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("target", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.ITEM))
                                                .executes(context -> {
                                                    var target = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "target", RegistryKeys.ITEM);

                                                    int removed = StackSizeConfigManager.removeRule(target.asString());

                                                    if (removed == 0) {
                                                        context.getSource().sendError(Text.translatableWithFallback(
                                                                "command.itemsstack.remove.missing",
                                                                "No rule exists for %s",
                                                                target.asString()
                                                        ));

                                                        return 0;
                                                    }

                                                    context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                            "command.itemsstack.remove.success",
                                                            "Removed %s rule(s) for %s",
                                                            removed, target.asString()
                                                    ), true);

                                                    return removed;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("reset")
                                        .then(CommandManager.argument("target", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.ITEM))
                                                .executes(context -> {
                                                    var target = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "target", RegistryKeys.ITEM);

                                                    int affected = getItems(target).size();
                                                    StackSizeConfigManager.resetRule(target.asString());

                                                    context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                            "command.itemsstack.reset.success",
                                                            "Reset %s to its default stack size, affecting %s item(s)",
                                                            target.asString(), affected
                                                    ), true);

                                                    return affected;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("clear")
                                        .executes(context -> clearRules(
                                                context,
                                                StackSizeConfigManager.RuleFilter.ALL
                                        ))
                                        .then(CommandManager.literal("items")
                                                .executes(context -> clearRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.ITEMS
                                                ))
                                        )
                                        .then(CommandManager.literal("tags")
                                                .executes(context -> clearRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.TAGS
                                                ))
                                        )
                                )
                                .then(CommandManager.literal("reload")
                                        .executes(context -> {
                                            int loaded = StackSizeConfigManager.reloadFromDisk();

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.reload.success",
                                                    "Reloaded %s stack size rule(s) from disk",
                                                    loaded
                                            ), true);

                                            return loaded;
                                        })
                                )
                                .then(CommandManager.literal("save")
                                        .executes(context -> {
                                            int saved = StackSizeConfigManager.saveToDisk();

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.save.success",
                                                    "Saved %s stack size rule(s) to disk",
                                                    saved
                                            ), true);

                                            return saved;
                                        })
                                )
                                .then(CommandManager.literal("list")
                                        .then(CommandManager.literal("size")
                                                .executes(context -> {
                                                    context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                            "command.itemsstack.list.size.current",
                                                            "Rules per page is currently set to: %s",
                                                            rulesPerPage
                                                    ), false);

                                                    return rulesPerPage;
                                                })
                                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 100))
                                                        .executes(context -> {
                                                            rulesPerPage = IntegerArgumentType.getInteger(context, "count");

                                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                                    "command.itemsstack.list.size.changed",
                                                                    "Rules per page set to: %s",
                                                                    rulesPerPage
                                                            ), false);

                                                            return rulesPerPage;
                                                        })
                                                )
                                        )
                                        .executes(context -> listRules(
                                                context,
                                                StackSizeConfigManager.RuleFilter.ALL,
                                                1
                                        ))
                                        .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                                .executes(context -> listRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.ALL,
                                                        IntegerArgumentType.getInteger(context, "page")
                                                ))
                                        )
                                        .then(CommandManager.literal("items")
                                                .executes(context -> listRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.ITEMS,
                                                        1
                                                ))
                                                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(context -> listRules(
                                                                context,
                                                                StackSizeConfigManager.RuleFilter.ITEMS,
                                                                IntegerArgumentType.getInteger(context, "page")
                                                        ))
                                                )
                                        )
                                        .then(CommandManager.literal("tags")
                                                .executes(context -> listRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.TAGS,
                                                        1
                                                ))
                                                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(context -> listRules(
                                                                context,
                                                                StackSizeConfigManager.RuleFilter.TAGS,
                                                                IntegerArgumentType.getInteger(context, "page")
                                                        ))
                                                )
                                        )
                                )
                                .then(CommandManager.literal("help")
                                        .executes(context -> {
                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.title",
                                                    "Items Stack commands:"
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.set",
                                                    "/itemsstack set <item|#tag> <count> - Set or replace a stack size rule."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.remove",
                                                    "/itemsstack remove <item|#tag> - Remove that exact item or tag rule."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.reset",
                                                    "/itemsstack reset <item|#tag> - Force that item or tag back to its default stack size."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.clear",
                                                    "/itemsstack clear [items|tags] - Remove all stack size rules."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.reload",
                                                    "/itemsstack reload - Discard unsaved changes and reload the config from disk."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.save",
                                                    "/itemsstack save - Save the current config to disk."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.list",
                                                    "/itemsstack list [items|tags] [page] - List configured rules."
                                                    ), false
                                            );

                                            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                                                    "command.itemsstack.help.list_page_size",
                                                    "/itemsstack list size [count] - Show or set how many rules are displayed per page for this session."
                                                    ), false
                                            );

                                            return 1;
                                        })
                                )
                )
        );
    }

    private static int clearRules(
            CommandContext<ServerCommandSource> context,
            StackSizeConfigManager.RuleFilter filter
    ) {
        int cleared = StackSizeConfigManager.clearRules(filter);

        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "command.itemsstack.clear.success",
                "Cleared %s %s stack size rule(s)",
                cleared, getClearFilterName(filter)
        ), true);

        return cleared;
    }

    private static Text getClearFilterName(StackSizeConfigManager.RuleFilter filter) {
        return switch (filter) {
            case ALL -> Text.translatableWithFallback("command.itemsstack.clear.filter.all", "general");
            case ITEMS -> Text.translatableWithFallback("command.itemsstack.clear.filter.items", "item");
            case TAGS -> Text.translatableWithFallback("command.itemsstack.clear.filter.tags", "tag");
        };
    }

    private static int listRules(
            CommandContext<ServerCommandSource> context,
            StackSizeConfigManager.RuleFilter filter,
            int page
    ) {
        List<String> rules = StackSizeConfigManager.getRules(filter);

        if (rules.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                    "command.itemsstack.list.empty",
                    "No %s stack size rules are configured",
                    getListFilterName(filter)), false
            );

            return 0;
        }

        int pages = (rules.size() + rulesPerPage - 1) / rulesPerPage;

        if (page > pages) {
            context.getSource().sendError(Text.translatableWithFallback(
                    "command.itemsstack.list.invalid_page",
                    "Page %s does not exist. Last page is %s",
                    page, pages
                    )
            );

            return 0;
        }

        int first = (page - 1) * rulesPerPage;
        int last = Math.min(first + rulesPerPage, rules.size());

        context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                "command.itemsstack.list.header",
                "Items Stack %s rules - page %s/%s (%s total):",
                getListFilterName(filter), page, pages, rules.size()
                ), false
        );

        for (int i = first; i < last; i++) {
            int ruleNumber = i + 1;
            String rule = rules.get(i);

            context.getSource().sendFeedback(() -> Text.translatableWithFallback(
                    "command.itemsstack.list.entry",
                    "%s. %s",
                    String.format("%3d", ruleNumber), rule
                    ), false
            );
        }

        return last - first;
    }

    private static Text getListFilterName(StackSizeConfigManager.RuleFilter filter) {
        return switch (filter) {
            case ALL -> Text.translatableWithFallback("command.itemsstack.list.filter.all", "general");
            case ITEMS -> Text.translatableWithFallback("command.itemsstack.list.filter.items", "item");
            case TAGS -> Text.translatableWithFallback("command.itemsstack.list.filter.tags", "tag");
        };
    }

    private static List<Item> getItems(
            RegistryEntryPredicateArgumentType.EntryPredicate<Item> target
    ) {
        return target.getEntry().map(
                entry -> List.of(entry.value()),
                tag -> tag.stream()
                        .map(RegistryEntry::value)
                        .toList()
        );
    }
}