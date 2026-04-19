package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import me.hsgamer.topper.value.core.ValueProvider;
import me.hsgamer.topper.value.core.ValueWrapper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public record TextPlaceholderValueProvider(String placeholder) implements ValueProvider<ServerPlayer, String> {
    @Override
    public void accept(ServerPlayer serverPlayerEntity, Consumer<ValueWrapper<String>> callback) {
        //? if >= 26.1 {
        Component text = Placeholders.SERVER_PLACEHOLDER_PARSER.parseNode(placeholder).toComponent(PlaceholderContext.of(serverPlayerEntity));
        //? } else {
        //Component text = Placeholders.parseText(Component.literal(placeholder), PlaceholderContext.of(serverPlayerEntity));
        //? }
        String string = text.getString();
        if (placeholder.equals(string)) {
            callback.accept(ValueWrapper.notHandled());
        } else {
            callback.accept(ValueWrapper.handled(string));
        }
    }
}
