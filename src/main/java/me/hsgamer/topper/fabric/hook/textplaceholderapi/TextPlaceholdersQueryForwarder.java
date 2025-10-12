package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.hsgamer.topper.query.core.QueryResult;
import me.hsgamer.topper.query.forward.QueryForwardContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public class TextPlaceholdersQueryForwarder implements Consumer<QueryForwardContext<UUID>> {
    private final List<Identifier> identifiers = new ArrayList<>();

    @Override
    public void accept(QueryForwardContext<UUID> context) {
        Identifier identifier = Identifier.of(context.getName().toLowerCase(Locale.ROOT), "query");
        Placeholders.register(identifier, (ctx, arg) -> {
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
        );
        identifiers.add(identifier);
    }

    public void unregister() {
        identifiers.forEach(Placeholders::remove);
        identifiers.clear();
    }
}
