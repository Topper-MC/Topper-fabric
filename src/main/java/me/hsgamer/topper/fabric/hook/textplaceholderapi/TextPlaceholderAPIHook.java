package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.util.ProfileUtil;
import me.hsgamer.topper.query.core.QueryResult;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;

public interface TextPlaceholderAPIHook {
    static void addHook(TopperFabric mod) {
        mod.getTopTemplate().getQueryForwardManager().addForwarder(context ->
                Placeholders.register(Identifier.of(context.getName().toLowerCase(Locale.ROOT), "query"), (ctx, arg) -> {
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
                    .beforeApply(uuid -> ProfileUtil.getPlayer(mod.getServer(), uuid));
        }, "placeholder");
    }
}
