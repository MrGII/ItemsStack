package github.mrgii.itemsstack.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import github.mrgii.itemsstack.config.StackSizeConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import java.util.List;

public final class ItemsStackCommands {
    private static int rulesPerPage = 10;

    private ItemsStackCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        Commands.literal("itemsstack")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("target", ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.ITEM))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> {
                                                            var target = ResourceOrTagArgument.getResourceOrTag(context, "target", Registries.ITEM);

                                                            int count = IntegerArgumentType.getInteger(context, "count");
                                                            StackSizeConfigManager.setRule(target.asPrintable(), count);

                                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                                    "command.itemsstack.set.success",
                                                                    "Set %s max stack size to %s",
                                                                    target.asPrintable(), count
                                                            ), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("target", ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.ITEM))
                                                .executes(context -> {
                                                    var target = ResourceOrTagArgument.getResourceOrTag(context, "target", Registries.ITEM);

                                                    int removed = StackSizeConfigManager.removeRule(target.asPrintable());

                                                    if (removed == 0) {
                                                        context.getSource().sendFailure(Component.translatableWithFallback(
                                                                "command.itemsstack.remove.missing",
                                                                "No rule exists for %s",
                                                                target.asPrintable()
                                                        ));

                                                        return 0;
                                                    }

                                                    context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                            "command.itemsstack.remove.success",
                                                            "Removed %s rule(s) for %s",
                                                            removed, target.asPrintable()
                                                    ), true);

                                                    return removed;
                                                })
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .then(Commands.argument("target", ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.ITEM))
                                                .executes(context -> {
                                                    var target = ResourceOrTagArgument.getResourceOrTag(context, "target", Registries.ITEM);

                                                    int affected = getItems(target).size();
                                                    StackSizeConfigManager.resetRule(target.asPrintable());

                                                    context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                            "command.itemsstack.reset.success",
                                                            "Reset %s to its default stack size, affecting %s item(s)",
                                                            target.asPrintable(), affected
                                                    ), true);

                                                    return affected;
                                                })
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .executes(context -> clearRules(
                                                context,
                                                StackSizeConfigManager.RuleFilter.ALL
                                        ))
                                        .then(Commands.literal("items")
                                                .executes(context -> clearRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.ITEMS
                                                ))
                                        )
                                        .then(Commands.literal("tags")
                                                .executes(context -> clearRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.TAGS
                                                ))
                                        )
                                )
                                .then(Commands.literal("reload")
                                        .executes(context -> {
                                            int loaded = StackSizeConfigManager.reloadFromDisk();

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.reload.success",
                                                    "Reloaded %s stack size rule(s) from disk",
                                                    loaded
                                            ), true);

                                            return loaded;
                                        })
                                )
                                .then(Commands.literal("save")
                                        .executes(context -> {
                                            int saved = StackSizeConfigManager.saveToDisk();

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.save.success",
                                                    "Saved %s stack size rule(s) to disk",
                                                    saved
                                            ), true);

                                            return saved;
                                        })
                                )
                                .then(Commands.literal("list")
                                        .then(Commands.literal("size")
                                                .executes(context -> {
                                                    context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                            "command.itemsstack.list.size.current",
                                                            "Rules per page is currently set to: %s",
                                                            rulesPerPage
                                                    ), false);

                                                    return rulesPerPage;
                                                })
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                                        .executes(context -> {
                                                            rulesPerPage = IntegerArgumentType.getInteger(context, "count");

                                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
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
                                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                .executes(context -> listRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.ALL,
                                                        IntegerArgumentType.getInteger(context, "page")
                                                ))
                                        )
                                        .then(Commands.literal("items")
                                                .executes(context -> listRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.ITEMS,
                                                        1
                                                ))
                                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(context -> listRules(
                                                                context,
                                                                StackSizeConfigManager.RuleFilter.ITEMS,
                                                                IntegerArgumentType.getInteger(context, "page")
                                                        ))
                                                )
                                        )
                                        .then(Commands.literal("tags")
                                                .executes(context -> listRules(
                                                        context,
                                                        StackSizeConfigManager.RuleFilter.TAGS,
                                                        1
                                                ))
                                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(context -> listRules(
                                                                context,
                                                                StackSizeConfigManager.RuleFilter.TAGS,
                                                                IntegerArgumentType.getInteger(context, "page")
                                                        ))
                                                )
                                        )
                                )
                                .then(Commands.literal("help")
                                        .executes(context -> {
                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.title",
                                                    "Items Stack commands:"
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.set",
                                                    "/itemsstack set <item|#tag> <count> - Set or replace a stack size rule."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.remove",
                                                    "/itemsstack remove <item|#tag> - Remove that exact item or tag rule."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.reset",
                                                    "/itemsstack reset <item|#tag> - Force that item or tag back to its default stack size."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.clear",
                                                    "/itemsstack clear [items|tags] - Remove all stack size rules."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.reload",
                                                    "/itemsstack reload - Discard unsaved changes and reload the config from disk."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.save",
                                                    "/itemsstack save - Save the current config to disk."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                                                    "command.itemsstack.help.list",
                                                    "/itemsstack list [items|tags] [page] - List configured rules."
                                                    ), false
                                            );

                                            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
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
            CommandContext<CommandSourceStack> context,
            StackSizeConfigManager.RuleFilter filter
    ) {
        int cleared = StackSizeConfigManager.clearRules(filter);

        context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                "command.itemsstack.clear.success",
                "Cleared %s %s stack size rule(s)",
                cleared, getClearFilterName(filter)
        ), true);

        return cleared;
    }

    private static Component getClearFilterName(StackSizeConfigManager.RuleFilter filter) {
        return switch (filter) {
            case ALL -> Component.translatableWithFallback("command.itemsstack.clear.filter.all", "general");
            case ITEMS -> Component.translatableWithFallback("command.itemsstack.clear.filter.items", "item");
            case TAGS -> Component.translatableWithFallback("command.itemsstack.clear.filter.tags", "tag");
        };
    }

    private static int listRules(
            CommandContext<CommandSourceStack> context,
            StackSizeConfigManager.RuleFilter filter,
            int page
    ) {
        List<String> rules = StackSizeConfigManager.getRules(filter);

        if (rules.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                    "command.itemsstack.list.empty",
                    "No %s stack size rules are configured",
                    getListFilterName(filter)), false
            );

            return 0;
        }

        int pages = (rules.size() + rulesPerPage - 1) / rulesPerPage;

        if (page > pages) {
            context.getSource().sendFailure(Component.translatableWithFallback(
                    "command.itemsstack.list.invalid_page",
                    "Page %s does not exist. Last page is %s",
                    page, pages
                    )
            );

            return 0;
        }

        int first = (page - 1) * rulesPerPage;
        int last = Math.min(first + rulesPerPage, rules.size());

        context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                "command.itemsstack.list.header",
                "Items Stack %s rules - page %s/%s (%s total):",
                getListFilterName(filter), page, pages, rules.size()
                ), false
        );

        for (int i = first; i < last; i++) {
            int ruleNumber = i + 1;
            String rule = rules.get(i);

            context.getSource().sendSuccess(() -> Component.translatableWithFallback(
                    "command.itemsstack.list.entry",
                    "%s. %s",
                    String.format("%3d", ruleNumber), rule
                    ), false
            );
        }

        return last - first;
    }

    private static Component getListFilterName(StackSizeConfigManager.RuleFilter filter) {
        return switch (filter) {
            case ALL -> Component.translatableWithFallback("command.itemsstack.list.filter.all", "general");
            case ITEMS -> Component.translatableWithFallback("command.itemsstack.list.filter.items", "item");
            case TAGS -> Component.translatableWithFallback("command.itemsstack.list.filter.tags", "tag");
        };
    }

    private static List<Item> getItems(
            ResourceOrTagArgument.Result<Item> target
    ) {
        return target.unwrap().map(
                entry -> List.of(entry.value()),
                tag -> tag.stream()
                        .map(Holder::value)
                        .toList()
        );
    }
}