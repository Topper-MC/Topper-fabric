package me.hsgamer.topper.fabric.hook.miniplaceholders;

import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.utils.Tags;
import me.hsgamer.topper.query.forward.QueryForwardContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MiniPlaceholdersQueryForwarder implements Consumer<QueryForwardContext<UUID>> {
    private final List<Expansion> expansions = new ArrayList<>();

    @Override
    public void accept(QueryForwardContext<UUID> queryContext) {
        BiFunction<@Nullable UUID, ArgumentQueue, Tag> queryFunction = (uuid, queue) -> {
            if (!queue.hasNext()) {
                return Tag.selfClosingInserting(Component.text("You need to specify the query"));
            }

            List<String> args = new ArrayList<>();
            while (queue.hasNext()) {
                args.add(queue.pop().value());
            }

            String query = String.join(":", args);
            String result = queryContext.getQuery().apply(uuid, query).result;
            if (result == null) {
                return Tags.EMPTY_TAG;
            } else {
                return Tag.selfClosingInserting(Component.text(result));
            }
        };

        Expansion expansion = Expansion.builder(queryContext.getName())
                .globalPlaceholder("global", (queue, context) -> queryFunction.apply(null, queue))
                .audiencePlaceholder(ServerPlayerEntity.class, "player", (audience, queue, ctx) -> queryFunction.apply(audience.getUuid(), queue))
                .build();

        expansion.register();
        expansions.add(expansion);
    }

    public void unregister() {
        expansions.forEach(Expansion::unregister);
        expansions.clear();
    }
}
