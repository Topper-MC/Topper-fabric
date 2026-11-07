package me.hsgamer.topper.fabric.config;

import io.github.projectunified.craftconfig.annotation.ConfigNode;
import io.github.projectunified.craftconfig.annotation.ConfigPath;
import io.github.projectunified.craftconfig.common.Config;

@ConfigNode
public interface MessageConfig {
    @ConfigPath("prefix")
    default String getPrefix() {
        return "<gray>[<aqua>Topper<gray>] ";
    }

    @ConfigPath("success")
    default String getSuccess() {
        return "<green>Success";
    }

    @ConfigPath("number-required")
    default String getNumberRequired() {
        return "<red>Number is required";
    }

    @ConfigPath("illegal-from-to-index")
    default String getIllegalFromToIndex() {
        return "<red>The from index should be less than the to index";
    }

    @ConfigPath("top-empty")
    default String getTopEmpty() {
        return "<red>No top entry";
    }

    @ConfigPath("top-holder-not-found")
    default String getTopHolderNotFound() {
        return "<red>The top holder is not found";
    }

    void reloadConfig();

    Config getConfig();
}
