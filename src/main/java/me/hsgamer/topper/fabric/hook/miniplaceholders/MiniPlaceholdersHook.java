package me.hsgamer.topper.fabric.hook.miniplaceholders;

import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.util.ProfileUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;

public interface MiniPlaceholdersHook {
    static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("miniplaceholders");
    }

    static Runnable addHook(TopperFabric mod) {
        MiniPlaceholdersQueryForwarder forwarder = new MiniPlaceholdersQueryForwarder();
        mod.getTopTemplate().getQueryForwardManager().addForwarder(forwarder);
        mod.getValueProviderManager().register(map -> {
            String placeholder = Optional.ofNullable(map.get("placeholder")).map(Object::toString).orElse("");
            return new MiniPlaceholderValueProvider(placeholder)
                    .thenApply(Double::parseDouble)
                    .beforeApply(uuid -> ProfileUtil.getPlayer(mod.getServer(), uuid));
        }, "placeholder");
        return forwarder::unregister;
    }
}
