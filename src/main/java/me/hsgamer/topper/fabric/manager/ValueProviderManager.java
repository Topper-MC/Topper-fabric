package me.hsgamer.topper.fabric.manager;

import me.hsgamer.hscore.builder.FunctionalMassBuilder;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.provider.StatisticValueProvider;
import me.hsgamer.topper.value.core.ValueProvider;
import net.minecraft.server.PlayerManager;

import java.util.*;

public class ValueProviderManager extends FunctionalMassBuilder<Map<String, Object>, ValueProvider<UUID, Double>> {
    public ValueProviderManager(TopperFabric mod) {
        register(map -> {
            String statType = Optional.ofNullable(map.get("statistic-type")).map(Object::toString).orElse("minecraft:custom");
            List<String> statNames = CollectionUtils.createStringListFromObject(map.get("statistic"), true);
            return new StatisticValueProvider(statType, statNames).beforeApply(uuid -> {
                PlayerManager playerManager = mod.getServer().getPlayerManager();
                if (playerManager == null) {
                    return null;
                }
                return playerManager.getPlayer(uuid);
            });
        }, "statistic");
    }

    @Override
    protected String getType(Map<String, Object> map) {
        return Objects.toString(map.get("type"), "");
    }
}
