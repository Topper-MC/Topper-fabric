package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import me.hsgamer.topper.template.topplayernumber.storage.DataStorageSupplier;

import java.util.Map;

public class FabricTopTemplateSettings implements TopPlayerNumberTemplate.Settings {
    private final FabricDataStorageSupplierSettings dataStorageSupplierSettings;

    public FabricTopTemplateSettings(TopperFabric mod) {
        this.dataStorageSupplierSettings = new FabricDataStorageSupplierSettings(mod);
    }

    @Override
    public String storageType() {
        return TopperFabric.MAIN_CONFIG.getStorageType();
    }

    @Override
    public DataStorageSupplier.Settings storageSettings() {
        return dataStorageSupplierSettings;
    }

    @Override
    public Map<String, NumberTopHolder.Settings> holders() {
        return TopperFabric.MAIN_CONFIG.getHolders();
    }

    @Override
    public int taskSaveDelay() {
        return TopperFabric.MAIN_CONFIG.getTaskSaveDelay();
    }

    @Override
    public int taskSaveEntryPerTick() {
        return TopperFabric.MAIN_CONFIG.getTaskSaveEntryPerTick();
    }

    @Override
    public int taskUpdateEntryPerTick() {
        return TopperFabric.MAIN_CONFIG.getTaskUpdateEntryPerTick();
    }

    @Override
    public int taskUpdateDelay() {
        return TopperFabric.MAIN_CONFIG.getTaskUpdateDelay();
    }

    @Override
    public int taskUpdateSetDelay() {
        return TopperFabric.MAIN_CONFIG.getTaskUpdateSetDelay();
    }

    @Override
    public int taskUpdateMaxSkips() {
        return TopperFabric.MAIN_CONFIG.getTaskUpdateMaxSkips();
    }
}
