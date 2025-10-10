package me.hsgamer.topper.fabric.manager;

import me.hsgamer.hscore.builder.FunctionalMassBuilder;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.provider.PlaceholderValueProvider;
import me.hsgamer.topper.fabric.provider.StatisticValueProvider;
import me.hsgamer.topper.value.core.ValueProvider;
import net.minecraft.server.PlayerManager;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ValueProviderManager extends FunctionalMassBuilder<Map<String, Object>, ValueProvider<UUID, Double>> {
    public ValueProviderManager(TopperFabric mod) {
        register(map -> {
            String statType = Optional.ofNullable(map.get("statistic-type")).map(Object::toString).orElse("minecraft:custom");
            String statName = Optional.ofNullable(map.get("statistic")).map(Object::toString).orElse("");
            return new StatisticValueProvider(statType, statName).beforeApply(uuid -> {
                PlayerManager playerManager = mod.getServer().getPlayerManager();
                if (playerManager == null) {
                    return null;
                }
                return playerManager.getPlayer(uuid);
            });
        }, "statistic");
        register(map -> {
            String placeholder = Optional.ofNullable(map.get("placeholder")).map(Object::toString).orElse("");
            return new PlaceholderValueProvider(placeholder)
                    .thenApply(Double::parseDouble)
                    .beforeApply(uuid -> {
                        PlayerManager playerManager = mod.getServer().getPlayerManager();
                        if (playerManager == null) {
                            return null;
                        }
                        return playerManager.getPlayer(uuid);
                    });
        }, "placeholder");
    }

    @Override
    protected String getType(Map<String, Object> map) {
        return Objects.toString(map.get("type"), "");
    }
}
