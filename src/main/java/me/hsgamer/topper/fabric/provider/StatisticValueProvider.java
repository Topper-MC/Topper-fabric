package me.hsgamer.topper.fabric.provider;

import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.stats.StatType;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public record StatisticValueProvider(String type,
                                     List<String> names) implements ValueProvider<ServerPlayer, Double> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void accept(ServerPlayer serverPlayerEntity, Consumer<ValueWrapper<Double>> callback) {
        StatsCounter statHandler = serverPlayerEntity.getStats();

        Identifier typeIdentifier = Identifier.tryParse(type);
        if (typeIdentifier == null) {
            callback.accept(ValueWrapper.notHandled());
            return;
        }

        Optional<Holder.Reference<StatType<?>>> optionalStatTypeReference = BuiltInRegistries.STAT_TYPE.get(typeIdentifier);
        if (optionalStatTypeReference.isEmpty()) {
            callback.accept(ValueWrapper.notHandled());
            return;
        }
        StatType statType = optionalStatTypeReference.get().value();
        Registry statRegistry = statType.getRegistry();

        Stream<Object> itemStream = names.isEmpty() ? statRegistry.stream() : names.stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .map(statRegistry::getValue)
                .filter(Objects::nonNull);

        callback.accept(
                ValueWrapper.handled(
                        itemStream
                                .mapToDouble(item -> statHandler.getValue(statType, item))
                                .sum()
                )
        );
    }
}
