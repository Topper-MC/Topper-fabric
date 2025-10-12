package me.hsgamer.topper.fabric.hook.miniplaceholders;

import io.github.miniplaceholders.api.MiniPlaceholders;
import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public record MiniPlaceholderValueProvider(String placeholder) implements ValueProvider<ServerPlayerEntity, String> {
    @Override
    public @NotNull ValueWrapper<String> apply(@NotNull ServerPlayerEntity player) {
        String parsed;
        try {
            TagResolver tagResolver = MiniPlaceholders.audienceGlobalPlaceholders();
            Component component = MiniMessage.miniMessage().deserialize(placeholder, player, tagResolver);
            parsed = PlainTextComponentSerializer.plainText().serialize(component).trim();
        } catch (Exception e) {
            return ValueWrapper.error("Error while parsing the placeholder: " + placeholder, e);
        }

        if (placeholder.equals(parsed)) {
            return ValueWrapper.notHandled();
        }

        return ValueWrapper.handled(parsed);
    }
}
