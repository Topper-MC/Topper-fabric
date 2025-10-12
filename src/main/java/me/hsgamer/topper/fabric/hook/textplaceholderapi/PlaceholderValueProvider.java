package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record PlaceholderValueProvider(String placeholder) implements ValueProvider<ServerPlayerEntity, String> {
    @Override
    public @NotNull ValueWrapper<String> apply(@NotNull ServerPlayerEntity key) {
        Text text = Placeholders.parseText(Text.of(placeholder), PlaceholderContext.of(key));
        String string = text.getLiteralString();
        return placeholder.equals(string) ? ValueWrapper.notHandled() : ValueWrapper.handled(string);

    }
}
