package me.hsgamer.topper.fabric.config;

import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.util.Identifier;

public class MainConfig extends Config {
    public String storageType = "flat";


    public MainConfig() {
        super(Identifier.of("topper", "main_config"));
    }
}
