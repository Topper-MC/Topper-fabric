package me.hsgamer.topper.fabric.config.converter;

import me.hsgamer.hscore.common.MapUtils;
import me.hsgamer.topper.fabric.template.FabricTopHolderSettings;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;

public class HolderMapConverter extends StringMapConverter<NumberTopHolder.Settings> {
    @Override
    protected NumberTopHolder.Settings toValue(Object value) {
        return MapUtils.castOptionalStringObjectMap(value).map(FabricTopHolderSettings::new).orElse(null);
    }

    @Override
    protected Object toRawValue(Object value) {
        if (value instanceof FabricTopHolderSettings) {
            return ((FabricTopHolderSettings) value).map();
        }
        return null;
    }
}
