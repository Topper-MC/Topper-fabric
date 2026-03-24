package me.hsgamer.topper.fabric.template;

import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.topper.agent.update.UpdateAgent;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.util.PermissionUtil;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class FabricTopHolderSettings extends NumberTopHolder.MapSettings {
    private final List<String> ignorePermissions;
    private final List<String> resetPermissions;

    public FabricTopHolderSettings(Map<String, Object> map) {
        super(map);
        ignorePermissions = CollectionUtils.createStringListFromObject(map.get("ignore-permission"), true);
        resetPermissions = CollectionUtils.createStringListFromObject(map.get("reset-permission"), true);
    }

    public String defaultLine() {
        return Objects.toString(map.get("line"), null);
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
}
