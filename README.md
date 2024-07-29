Allows you to change any item's stack size.

Requires owolib, recommended to use modmenu.

Open the config screen/file (if not using through modmenu the file name should be items-stack-size-config.json5) and add the wanted item name to the items list and its stack size to the Max Stack Sizes list, an example of how the json would look, this is a possible json to use for the mod's icon changes:
```
{
	"items": [
		"minecraft:potion",
		"minecraft:oak_boat",
		"minecraft:stone"
	],
	"maxStackSizes": [
		8,
		32
	],
	"itemTags": [
	    "#minecraft:boats"
	    "#c:tools"
	],
	"maxTagStackSizes": [
	    16,
	    4
	]
}
```

This mod is intended to be used as client and server though it has limited compatibility for server only (it works only in survival and then some features may still not work).
