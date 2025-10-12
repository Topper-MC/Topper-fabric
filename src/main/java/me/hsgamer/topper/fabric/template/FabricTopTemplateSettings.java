package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;

import java.util.Map;

public record FabricTopTemplateSettings(TopperFabric mod) implements TopPlayerNumberTemplate.Settings {
    @Override
    public Map<String, NumberTopHolder.Settings> holders() {
        return mod.getMainConfig().getHolders();
    }

    @Override
    public int taskSaveDelay() {
        return mod.getMainConfig().getTaskSaveDelay();
    }

    @Override
    public int taskSaveEntryPerTick() {
        return mod.getMainConfig().getTaskSaveEntryPerTick();
    }

    @Override
    public int taskUpdateEntryPerTick() {
        return mod.getMainConfig().getTaskUpdateEntryPerTick();
    }

    @Override
    public int taskUpdateDelay() {
        return mod.getMainConfig().getTaskUpdateDelay();
    }

    @Override
    public int taskUpdateSetDelay() {
        return mod.getMainConfig().getTaskUpdateSetDelay();
    }

    @Override
    public int taskUpdateMaxSkips() {
        return mod.getMainConfig().getTaskUpdateMaxSkips();
    }
}
