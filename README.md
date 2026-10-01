# Items Stack

Allows you to change the maximum stack size of any item in Minecraft.

## Features

- Per-item stack size overrides
- Per-tag stack size overrides
- Exact item rules override tag rules
- Reset items or tags back to their default stack size
- In-game commands for editing, listing, saving, and reloading rules
- Stack sizes above 99
- Config editing through Mod Menu
- Server-only usage with vanilla clients for most normal inventory behavior

## Requirements

- Fabric Loader
- Fabric API
- owo-lib

Mod Menu is recommended, but not required.

## Configuration

The config file is:

```text
config/items-stack-size-config.json5
```

If Mod Menu is installed, you can edit the config through the generated config screen.

Rules are stored in a single list using this format:

```text
item_or_tag=value
```

Examples:

```text
minecraft:ender_pearl=64
minecraft:potion=16
#minecraft:boats=32
```

To force an item or tag back to its normal Minecraft stack size, use:

```text
default
```

For example:

```text
minecraft:oak_boat=default
```

### Example config

```json5
{
    "overrides": [
        "minecraft:potion=8",
        "minecraft:stone=32",
        "#minecraft:boats=16",
        "#c:tools=4"
    ]
}
```

## Rule Priority

Exact item rules always override tag rules.

For rules of the same type, later rules override earlier rules.

For example:

```text
#minecraft:logs=64
minecraft:oak_log=32
```

This gives:

- `minecraft:oak_log` a maximum stack size of `32`
- other items in `#minecraft:logs` a maximum stack size of `64`

## Commands

All commands require operator permission.

### Set a rule

```text
/itemsstack set <item|#tag> <count>
```

Examples:

```text
/itemsstack set minecraft:ender_pearl 128
/itemsstack set #minecraft:logs 96
```

### Remove a rule

```text
/itemsstack remove <item|#tag>
```

Removes the exact matching item or tag rule.

### Reset to default

```text
/itemsstack reset <item|#tag>
```

Adds a `default` rule so the selected item or tag uses its normal stack size.

### Clear rules

```text
/itemsstack clear
/itemsstack clear items
/itemsstack clear tags
```

### List rules

```text
/itemsstack list
/itemsstack list items
/itemsstack list tags
/itemsstack list <page>
```

You can also change how many rules are displayed per page for the current session:

```text
/itemsstack list size
/itemsstack list size <count>
```

### Save config

```text
/itemsstack save
```

Saves the current in-memory configuration to disk.

### Reload config

```text
/itemsstack reload
```

Discards unsaved changes and reloads the configuration from disk.

### Help

```text
/itemsstack help
```

## Saving Behavior

Changes made through commands or the Mod Menu config screen are applied immediately.

They are not automatically saved to disk.

Use:

```text
/itemsstack save
```

to save them permanently.

Use:

```text
/itemsstack reload
```

to discard unsaved changes and reload the saved config.

## Compatibility

The mod is intended to be installed on both the client and server.

It can also be used server-side with vanilla clients for most normal inventory behavior.

Some client-side behavior, especially Creative inventory interactions and custom stack count rendering, requires the mod to also be installed on the client.

## Notes

Items with different components, damage values, or other stack data are still kept separate according to normal Minecraft stacking rules.

Explicit per-stack `MAX_STACK_SIZE` component changes take priority over this mod's configuration.
