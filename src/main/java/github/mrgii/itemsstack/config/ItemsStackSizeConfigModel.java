package github.mrgii.itemsstack.config;

import io.wispforest.owo.config.Option.SyncMode;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Sync;

import java.util.List;

@Modmenu(modId = "itemsstack")
@Config(name = "items-stack-size-config", wrapperName = "ItemsStackSizeConfig", saveOnModification = false)
public class ItemsStackSizeConfigModel {
    @Sync(SyncMode.OVERRIDE_CLIENT)
    public List<String> overrides = List.of();
}