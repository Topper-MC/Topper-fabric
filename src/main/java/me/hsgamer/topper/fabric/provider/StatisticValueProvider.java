package me.hsgamer.topper.fabric.provider;

import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record StatisticValueProvider(String type,
                                     List<String> names) implements ValueProvider<ServerPlayerEntity, Double> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public @NotNull ValueWrapper<Double> apply(@NotNull ServerPlayerEntity key) {
        StatHandler statHandler = key.getStatHandler();

        Identifier typeIdentifier = Identifier.tryParse(type);
        if (typeIdentifier == null) {
            return ValueWrapper.notHandled();
        }

        StatType statType = Registries.STAT_TYPE.get(typeIdentifier);
        if (statType == null) {
            return ValueWrapper.notHandled();
        }
        Registry statRegistry = statType.getRegistry();

        Stream<Object> itemStream = names.isEmpty() ? statRegistry.stream() : names.stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .map(statRegistry::get)
                .filter(Objects::nonNull);
        return ValueWrapper.handled(
                itemStream
                        .mapToDouble(item -> statHandler.getStat(statType, item))
                        .sum()
        );
    }
}
