package me.hsgamer.topper.fabric.provider;

import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public record StatisticValueProvider(String type,
                                     List<String> names) implements ValueProvider<ServerPlayerEntity, Double> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void accept(ServerPlayerEntity serverPlayerEntity, Consumer<ValueWrapper<Double>> callback) {
        StatHandler statHandler = serverPlayerEntity.getStatHandler();

        Identifier typeIdentifier = Identifier.tryParse(type);
        if (typeIdentifier == null) {
            callback.accept(ValueWrapper.notHandled());
            return;
        }

        StatType statType = Registries.STAT_TYPE.get(typeIdentifier);
        if (statType == null) {
            callback.accept(ValueWrapper.notHandled());
            return;
        }
        Registry statRegistry = statType.getRegistry();

        Stream<Object> itemStream = names.isEmpty() ? statRegistry.stream() : names.stream()
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .map(statRegistry::get)
                .filter(Objects::nonNull);

        callback.accept(
                ValueWrapper.handled(
                        itemStream
                                .mapToDouble(item -> statHandler.getStat(statType, item))
                                .sum()
                )
        );
    }
}
