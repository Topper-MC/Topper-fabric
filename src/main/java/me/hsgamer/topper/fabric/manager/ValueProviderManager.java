package me.hsgamer.topper.fabric.manager;

import me.hsgamer.hscore.builder.FunctionalMassBuilder;
import me.hsgamer.topper.value.core.ValueProvider;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ValueProviderManager extends FunctionalMassBuilder<Map<String, Object>, ValueProvider<UUID, Double>> {
    public ValueProviderManager() {

    }

    @Override
    protected String getType(Map<String, Object> map) {
        return Objects.toString(map.get("type"), "");
    }
}
