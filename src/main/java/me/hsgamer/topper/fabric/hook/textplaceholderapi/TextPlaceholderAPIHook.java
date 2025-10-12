package me.hsgamer.topper.fabric.hook.textplaceholderapi;

import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.util.ProfileUtil;

import java.util.Optional;

public interface TextPlaceholderAPIHook {
    static Runnable addHook(TopperFabric mod) {
        TextPlaceholdersQueryForwarder forwarder = new TextPlaceholdersQueryForwarder();
        mod.getTopTemplate().getQueryForwardManager().addForwarder(forwarder);
        mod.getValueProviderManager().register(map -> {
            String placeholder = Optional.ofNullable(map.get("placeholder")).map(Object::toString).orElse("");
            return new TextPlaceholderValueProvider(placeholder)
                    .thenApply(Double::parseDouble)
                    .beforeApply(uuid -> ProfileUtil.getPlayer(mod.getServer(), uuid));
        }, "placeholder");
        return forwarder::unregister;
    }
}
