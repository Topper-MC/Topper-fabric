package me.hsgamer.topper.fabric.hook.miniplaceholders;

import io.github.miniplaceholders.api.MiniPlaceholders;
import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public record MiniPlaceholderValueProvider(String placeholder) implements ValueProvider<ServerPlayerEntity, String> {
    @Override
    public void accept(ServerPlayerEntity serverPlayerEntity, Consumer<ValueWrapper<String>> callback) {
        String parsed;
        try {
            TagResolver tagResolver = MiniPlaceholders.audienceGlobalPlaceholders();
            Component component = MiniMessage.miniMessage().deserialize(placeholder, serverPlayerEntity, tagResolver);
            parsed = PlainTextComponentSerializer.plainText().serialize(component).trim();
        } catch (Exception e) {
            callback.accept(ValueWrapper.error("Error while parsing the placeholder: " + placeholder, e));
            return;
        }

        if (placeholder.equals(parsed)) {
            callback.accept(ValueWrapper.notHandled());
            return;
        }

        callback.accept(ValueWrapper.handled(parsed));
    }
}
