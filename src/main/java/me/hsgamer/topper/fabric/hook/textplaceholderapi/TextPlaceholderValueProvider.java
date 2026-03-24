package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public record TextPlaceholderValueProvider(String placeholder) implements ValueProvider<ServerPlayerEntity, String> {
    @Override
    public void accept(ServerPlayerEntity serverPlayerEntity, Consumer<ValueWrapper<String>> callback) {
        Text text = Placeholders.parseText(Text.of(placeholder), PlaceholderContext.of(serverPlayerEntity));
        String string = text.getLiteralString();
        if (placeholder.equals(string)) {
            callback.accept(ValueWrapper.notHandled());
        } else {
            callback.accept(ValueWrapper.handled(string));
        }
    }
}
