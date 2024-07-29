package github.mrgii.itemsstack;

import github.mrgii.itemsstack.util.ItemsStackSizeModifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ItemsStackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((clientPlayNetworkHandler, packetSender, client) -> {
            ItemsStackSizeModifier.updateValuesAndModifyStackSizes();
        });
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            ItemsStackSizeModifier.registerCallbacks();
        });
    }
}
