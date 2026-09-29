package github.mrgii.itemsstack;

import github.mrgii.itemsstack.config.StackSizeConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ItemsStackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, client) ->
                        StackSizeConfigManager.rebuildOverrides()
        );
    }
}