package me.hsgamer.topper.fabric.template;

import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.topper.agent.update.UpdateAgent;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.util.PermissionUtil;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import me.hsgamer.topper.template.topplayernumber.holder.display.ValueDisplay;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class FabricTopHolderSettings implements NumberTopHolder.Settings {
    private final Map<String, Object> map;
    private final FabricValueDisplaySettings valueDisplaySettings;
    private final List<String> ignorePermissions;
    private final List<String> resetPermissions;

    public FabricTopHolderSettings(Map<String, Object> map) {
        this.map = map;
        this.valueDisplaySettings = new FabricValueDisplaySettings(map);
        ignorePermissions = CollectionUtils.createStringListFromObject(map.get("ignore-permission"), true);
        resetPermissions = CollectionUtils.createStringListFromObject(map.get("reset-permission"), true);
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
    public UpdateAgent.FilterResult filter(UUID uuid) {
        Predicate<String> hasPermission = permission -> PermissionUtil.hasPermission(TopperFabric.getInstance().getServer(), uuid, permission);
        if (!resetPermissions.isEmpty() && resetPermissions.stream().anyMatch(hasPermission)) {
            return UpdateAgent.FilterResult.RESET;
        }
        if (!ignorePermissions.isEmpty() && ignorePermissions.stream().anyMatch(hasPermission)) {
            return UpdateAgent.FilterResult.SKIP;
        }
        return UpdateAgent.FilterResult.CONTINUE;
    }

    @Override
    public Map<String, Object> valueProvider() {
        return map;
    }

    public Map<String, Object> map() {
        return map;
    }
}
