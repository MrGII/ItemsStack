package github.mrgii.itemsstack.config;

import io.wispforest.owo.config.Option.SyncMode;
import io.wispforest.owo.config.annotation.*;

import java.util.List;

@Modmenu(modId = "itemsstack")
@Config(name = "items-stack-size-config", wrapperName = "ItemsStackSizeConfig")
public class ItemsStackSizeConfigModel {
    @RestartRequired
    @Sync(SyncMode.OVERRIDE_CLIENT)
    public List<String> items = List.of();

    @RestartRequired
    @Sync(SyncMode.OVERRIDE_CLIENT)
    public List<Integer> maxStackSizes = List.of();
}
