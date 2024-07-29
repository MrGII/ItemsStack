package github.mrgii.itemsstack;

import github.mrgii.itemsstack.config.ItemsStackSizeConfig;
import github.mrgii.itemsstack.util.ItemsStackSizeModifier;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemsStack implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("itemsstack");
	public static final ItemsStackSizeConfig CONFIG =  ItemsStackSizeConfig.createAndLoad();

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
			ItemsStackSizeModifier.updateValuesAndModifyStackSizes();
			ItemsStackSizeModifier.registerCallbacks();
		});
	}
}