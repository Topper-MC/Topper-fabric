package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;

import java.util.Map;

public class FabricTopTemplateSettings implements TopPlayerNumberTemplate.Settings {
    private final TopperFabric mod;
    private final FabricDataStorageSupplierSettings dataStorageSupplierSettings;

    public FabricTopTemplateSettings(TopperFabric mod) {
        this.mod = mod;
        this.dataStorageSupplierSettings = new FabricDataStorageSupplierSettings(mod);
    }

    @Override
    public String storageType() {
        return "";
    }

    @Override
    public DataStorageSupplier.Settings storageSettings() {
        return dataStorageSupplierSettings;
    }

    @Override
    public Map<String, NumberTopHolder.Settings> holders() {
        return Map.of();
    }

    @Override
    public int taskSaveDelay() {
        return 0;
    }

    @Override
    public int taskSaveEntryPerTick() {
        return 0;
    }

    @Override
    public int taskUpdateEntryPerTick() {
        return 0;
    }

    @Override
    public int taskUpdateDelay() {
        return 0;
    }

    @Override
    public int taskUpdateSetDelay() {
        return 0;
    }

    @Override
    public int taskUpdateMaxSkips() {
        return 0;
    }
}
