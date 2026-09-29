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
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("target", RegistryEntryPredicateArgumentType.registryEntryPredicate(registryAccess, RegistryKeys.ITEM))
                                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> {
                                                            var target = RegistryEntryPredicateArgumentType.getRegistryEntryPredicate(context, "target", RegistryKeys.ITEM);

                                                            int count = IntegerArgumentType.getInteger(context, "count");
                                                            StackSizeConfigManager.setRule(target.asString(), count);

                                                            context.getSource().sendFeedback(() -> Text.translatable(
                                                                    "command.itemsstack.set.success",
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
                                                        context.getSource().sendError(Text.translatable(
                                                                "command.itemsstack.remove.missing",
                                                                target.asString()
                                                        ));

                                                        return 0;
                                                    }

                                                    context.getSource().sendFeedback(() -> Text.translatable(
                                                            "command.itemsstack.remove.success",
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

                                                    context.getSource().sendFeedback(() -> Text.translatable(
                                                            "command.itemsstack.reset.success",
                                                            target.asString(), affected
                                                    ), true);

                                                    return affected;
                                                })
                                        )
                                )
                                .then(CommandManager.literal("clear")
                                        .executes(context -> {
                                            int cleared = StackSizeConfigManager.clearRules();

                                            context.getSource().sendFeedback(() -> Text.translatable(
                                                    "command.itemsstack.clear.success",
                                                    cleared
                                            ), true);

                                            return cleared;
                                        })
                                )
                                .then(CommandManager.literal("reload")
                                        .executes(context -> {
                                            int loaded = StackSizeConfigManager.reloadFromDisk();

                                            context.getSource().sendFeedback(() -> Text.translatable(
                                                    "command.itemsstack.reload.success",
                                                    loaded
                                            ), true);

                                            return loaded;
                                        })
                                )
                                .then(CommandManager.literal("save")
                                        .executes(context -> {
                                            int saved = StackSizeConfigManager.saveToDisk();

                                            context.getSource().sendFeedback(() -> Text.translatable(
                                                    "command.itemsstack.save.success",
                                                    saved
                                                    ), true
                                            );

                                            return saved;
                                        })
                                )
                                .then(CommandManager.literal("list")
                                        .then(CommandManager.literal("size")
                                                .executes(context -> {
                                                    context.getSource().sendFeedback(() -> Text.translatable(
                                                            "command.itemsstack.list.size.current",
                                                            rulesPerPage
                                                            ), false
                                                    );

                                                    return rulesPerPage;
                                                })
                                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 100))
                                                        .executes(context -> {
                                                            rulesPerPage = IntegerArgumentType.getInteger(context, "count");

                                                            context.getSource().sendFeedback(() -> Text.translatable(
                                                                    "command.itemsstack.list.size.changed",
                                                                    rulesPerPage
                                                                    ), false
                                                            );

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
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.title"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.set"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.remove"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.reset"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.clear"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.reload"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.save"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.list"), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("command.itemsstack.help.list_page_size"), false);

                                            return 1;
                                        })
                                )
                )
        );
    }

    private static int listRules(
            CommandContext<ServerCommandSource> context,
            StackSizeConfigManager.RuleFilter filter,
            int page
    ) {
        List<String> rules = StackSizeConfigManager.getRules(filter);

        if (rules.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.translatable(
                    "command.itemsstack.list.empty",
                    getFilterName(filter)
                    ), false
            );

            return 0;
        }

        int pages = (rules.size() + rulesPerPage - 1) / rulesPerPage;

        if (page > pages) {
            context.getSource().sendError(Text.translatable(
                    "command.itemsstack.list.invalid_page",
                    page, pages
            ));

            return 0;
        }

        int first = (page - 1) * rulesPerPage;
        int last = Math.min(first + rulesPerPage, rules.size());

        context.getSource().sendFeedback(() -> Text.translatable(
                "command.itemsstack.list.header",
                getFilterName(filter), page, pages, rules.size()
                ), false
        );

        for (int i = first; i < last; i++) {
            int ruleNumber = i + 1;
            String rule = rules.get(i);

            context.getSource().sendFeedback(() -> Text.translatable(
                    "command.itemsstack.list.entry",
                    String.format("%3d", ruleNumber), rule
                    ), false
            );
        }

        return last - first;
    }

    private static Text getFilterName(StackSizeConfigManager.RuleFilter filter) {
        return switch (filter) {
            case ALL -> Text.translatable("command.itemsstack.list.filter.all");
            case ITEMS -> Text.translatable("command.itemsstack.list.filter.items");
            case TAGS -> Text.translatable("command.itemsstack.list.filter.tags");
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