package me.hsgamer.topper.fabric.provider;

import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record StatisticValueProvider(String type, String name) implements ValueProvider<ServerPlayerEntity, Double> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public @NotNull ValueWrapper<Double> apply(@NotNull ServerPlayerEntity key) {
        Identifier typeIdentifier = Identifier.tryParse(type);
        if (typeIdentifier == null) {
            return ValueWrapper.notHandled();
        }
        Identifier nameIdentifier = Identifier.tryParse(name);
        if (nameIdentifier == null) {
            return ValueWrapper.notHandled();
        }

        StatType statType = Registries.STAT_TYPE.get(typeIdentifier);
        if (statType == null) {
            return ValueWrapper.notHandled();
        }
        Registry statRegistry = statType.getRegistry();
        Object item = statRegistry.get(nameIdentifier);
        if (item == null || !statType.hasStat(item)) {
            return ValueWrapper.notHandled();
        }
        Stat stat = statType.getOrCreateStat(item);

        return ValueWrapper.handled((double) key.getStatHandler().getStat(stat));
    }
}
