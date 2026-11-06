package me.hsgamer.topper.fabric.config;

import me.hsgamer.hscore.config.Config;
import me.hsgamer.hscore.config.annotation.Comment;
import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.topper.fabric.config.converter.HolderMapConverter;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;

import java.util.Collections;
import java.util.Map;

public interface MainConfig {
    @ConfigPath(value = "holders", converter = HolderMapConverter.class, priority = 1)
    @Comment({
            "The settings for the Top Holders",
            "Check the wiki for more information on how to setup Top Holder using Value Provider",
            "https://topper-mc.github.io/Wiki/spigot/provider.html"
    })
    default Map<String, NumberTopHolder.Settings> getHolders() {
        return Collections.emptyMap();
    }

    @ConfigPath(value = {"task", "save", "entry-per-tick"}, priority = 4)
    @Comment("How many entries should be saved per tick")
    default int getTaskSaveEntryPerTick() {
        return 10;
    }

    @ConfigPath(value = {"task", "save", "delay"}, priority = 4)
    @Comment("How many ticks should the mod wait before saving the leaderboard")
    default int getTaskSaveDelay() {
        return 0;
    }

    @ConfigPath(value = {"task", "update", "entry-per-tick"}, priority = 5)
    @Comment("How many entries should be updated per tick")
    default int getTaskUpdateEntryPerTick() {
        return 10;
    }

    @ConfigPath(value = {"task", "update", "delay"}, priority = 5)
    @Comment("How many ticks should the mod wait before updating the leaderboard")
    default int getTaskUpdateDelay() {
        return 0;
    }

    @ConfigPath(value = {"task", "update", "max-skips"}, priority = 5)
    @Comment({
            "How many times should the mod skip updating the value for the entry if it fails to update",
            "This is useful to let the mod prioritize other active entries",
    })
    default int getTaskUpdateMaxSkips() {
        return 1;
    }

    @ConfigPath(value = {"task", "update", "set-delay"}, priority = 5)
    @Comment({
            "How many ticks should the mod wait before applying the updated value to the entry",
            "Since the holder is updated partially, this is useful to prevent the mod from applying the value too early",
            "and to allow the mod to apply the value in larger batches, creating the illusion of a single update",
    })
    default int getTaskUpdateSetDelay() {
        return 0;
    }

    @ConfigPath(value = "storage-type")
    @Comment({
            "The type of storage the mod will use to store the value",
            "Available: FLAT, SQLITE, MYSQL"
    })
    default String getStorageType() {
        return "flat";
    }

    void reloadConfig();

    Config getConfig();
}
