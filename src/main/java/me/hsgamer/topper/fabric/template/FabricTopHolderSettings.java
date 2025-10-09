package me.hsgamer.topper.fabric.template;

import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import me.hsgamer.topper.template.topplayernumber.holder.display.ValueDisplay;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FabricTopHolderSettings implements NumberTopHolder.Settings {
    private final Map<String, Object> map;
    private final FabricValueDisplaySettings valueDisplaySettings;

    public FabricTopHolderSettings(Map<String, Object> map) {
        this.map = map;
        this.valueDisplaySettings = new FabricValueDisplaySettings(map);
    }

    @Override
    public Double defaultValue() {
        return Optional.ofNullable(map.get("default-value"))
                .map(Object::toString)
                .map(s -> {
                    try {
                        return Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        TopperFabric.LOGGER.warn("Invalid default value: {}. Fallback to null", s, e);
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public ValueDisplay.Settings displaySettings() {
        return valueDisplaySettings;
    }

    @Override
    public boolean async() {
        return Optional.ofNullable(map.get("async"))
                .map(Object::toString)
                .map(String::toLowerCase)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    @Override
    public boolean showErrors() {
        return Optional.ofNullable(map.get("show-errors"))
                .map(Object::toString)
                .map(String::toLowerCase)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    @Override
    public boolean resetOnError() {
        return Optional.ofNullable(map.get("reset-on-error"))
                .map(Object::toString)
                .map(String::toLowerCase)
                .map(Boolean::parseBoolean)
                .orElse(true);
    }

    @Override
    public boolean reverse() {
        return Optional.ofNullable(map.get("reverse"))
                .map(String::valueOf)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    @Override
    public List<String> ignorePermissions() {
        return CollectionUtils.createStringListFromObject(map.get("ignore-permission"), true);
    }

    @Override
    public List<String> resetPermissions() {
        return CollectionUtils.createStringListFromObject(map.get("reset-permission"), true);
    }

    @Override
    public Map<String, Object> valueProvider() {
        return map;
    }

    public Map<String, Object> map() {
        return map;
    }
}
