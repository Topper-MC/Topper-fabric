package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.query.core.QueryResult;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;

public interface TextPlaceholderAPIHook {
    static void addHook(TopperFabric mod) {
        Placeholders.register(Identifier.of("topper", "query"), (context, argument) -> {
            QueryResult result = mod.getTopTemplate().getTopQueryManager().apply(context.hasPlayer() ? context.player().getUuid() : null, argument);
            if (result.handled) {
                return PlaceholderResult.value(result.result);
            } else {
                return PlaceholderResult.invalid();
            }
        });
        mod.getTopTemplate().getQueryForwardManager().addForwarder(context ->
                Placeholders.register(Identifier.of("topper", context.getName().toLowerCase(Locale.ROOT)), (ctx, arg) -> {
                            if (arg == null) {
                                return PlaceholderResult.invalid("No query");
                            }
                            QueryResult result = context.getQuery().apply(ctx.hasPlayer() ? ctx.player().getUuid() : null, arg);
                            if (result.handled) {
                                return PlaceholderResult.value(result.result);
                            } else {
                                return PlaceholderResult.invalid();
                            }
                        }
                )
        );
        mod.getValueProviderManager().register(map -> {
            String placeholder = Optional.ofNullable(map.get("placeholder")).map(Object::toString).orElse("");
            return new PlaceholderValueProvider(placeholder)
                    .thenApply(Double::parseDouble)
                    .beforeApply(uuid -> {
                        PlayerManager playerManager = mod.getServer().getPlayerManager();
                        if (playerManager == null) {
                            return null;
                        }
                        return playerManager.getPlayer(uuid);
                    });
        }, "placeholder");
    }
}
