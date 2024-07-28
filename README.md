Allows you to change any item's stack size.

Requires owolib, recommended to use modmenu.

Open the config screen/file (if not using through modmenu the file name should be items-stack-size-config.json5) and add the wanted item name to the items list and its stack size to the Max Stack Sizes list, an example of how the json would look, this is the json that used for the mod's icon:
```
{
	"items": [
		"Potion",
		"Oak Boat",
		"Stone",
		"Diamond Sword"
	],
	"maxStackSizes": [
		8,
		16,
		32,
		4
	]
}
```
notice that after any change, such as adding/removing and item or changing a stack size the game must be restarted for the changes to take effect.

This mod is intended to be used as client and server though it has limited compatibility for server only (it works only in survival and then some features may still not work).
