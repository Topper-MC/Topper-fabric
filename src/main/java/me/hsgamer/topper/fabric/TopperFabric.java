package me.hsgamer.topper.fabric;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.TextParserUtils;
import me.hsgamer.hscore.config.configurate.ConfigurateConfig;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.topper.fabric.config.MainConfig;
import me.hsgamer.topper.fabric.config.MessageConfig;
import me.hsgamer.topper.fabric.hook.miniplaceholders.MiniPlaceholdersHook;
import me.hsgamer.topper.fabric.hook.textplaceholderapi.TextPlaceholderAPIHook;
import me.hsgamer.topper.fabric.manager.TaskManager;
import me.hsgamer.topper.fabric.manager.ValueProviderManager;
import me.hsgamer.topper.fabric.template.FabricStorageSupplierTemplate;
import me.hsgamer.topper.fabric.template.FabricTopTemplate;
import me.hsgamer.topper.fabric.util.PermissionUtil;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import me.hsgamer.topper.template.topplayernumber.holder.display.ValueDisplay;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class TopperFabric implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(TopperFabric.class);
    private static TopperFabric instance;
    private final List<Runnable> disableRunnables = new ArrayList<>();

    private MinecraftServer server;
    private Path configFolder;
    private Path dataFolder;

    private MainConfig mainConfig;
    private MessageConfig messageConfig;

    private ValueProviderManager valueProviderManager;
    private TaskManager taskManager;

    private FabricStorageSupplierTemplate storageSupplierTemplate;
    private FabricTopTemplate topTemplate;

    public TopperFabric() {
        instance = this;
    }

    public static TopperFabric getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        configFolder = FabricLoader.getInstance().getConfigDir().resolve("topper");
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdirs();
        }
        dataFolder = FabricLoader.getInstance().getGameDir().resolve("topper_data");
        if (!dataFolder.toFile().exists()) {
            dataFolder.toFile().mkdirs();
        }

        mainConfig = ConfigGenerator.newInstance(MainConfig.class, new ConfigurateConfig(
                configFolder.resolve("config.json").toFile(),
                GsonConfigurationLoader.builder().indent(2)
        ));
        messageConfig = ConfigGenerator.newInstance(MessageConfig.class, new ConfigurateConfig(
                configFolder.resolve("messages.json").toFile(),
                GsonConfigurationLoader.builder().indent(2)
        ));

        valueProviderManager = new ValueProviderManager(this);
        taskManager = new TaskManager();

        storageSupplierTemplate = new FabricStorageSupplierTemplate(this);
        topTemplate = new FabricTopTemplate(this);

        disableRunnables.add(TextPlaceholderAPIHook.addHook(this));
        if (MiniPlaceholdersHook.isAvailable()) {
            disableRunnables.add(MiniPlaceholdersHook.addHook(this));
        }

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
        CommandRegistrationCallback.EVENT.register(this::registerCommand);
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
        topTemplate.enable();
    }

    private void onServerStop(MinecraftServer server) {
        disableRunnables.forEach(Runnable::run);
        taskManager.shutdown();
        topTemplate.disable();
        this.server = null;
    }

    private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        topTemplate.getTopManager().create(handler.getPlayer().getUuid());
    }

    private void registerCommand(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(CommandManager.literal("gettop")
                .requires(source -> source.hasPermissionLevel(2) || PermissionUtil.hasPermission(source, Permissions.GET_TOP_LINES))
                .then(
                        CommandManager.argument("holder", StringArgumentType.word()).suggests((context, builder) -> {
                                    topTemplate.getTopManager().getHolderNames().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> sendTopLines(StringArgumentType.getString(context, "holder"), 1, 10, context))
                                .then(
                                        CommandManager.argument("i1", IntegerArgumentType.integer(1))
                                                .executes(context -> sendTopLines(StringArgumentType.getString(context, "holder"), 1, IntegerArgumentType.getInteger(context, "i1"), context))
                                                .then(CommandManager.argument("i2", IntegerArgumentType.integer(1))
                                                        .executes(context -> sendTopLines(StringArgumentType.getString(context, "holder"), IntegerArgumentType.getInteger(context, "i1"), IntegerArgumentType.getInteger(context, "i2"), context))
                                                )
                                )
                ));
        commandDispatcher.register(CommandManager.literal("reloadtop")
                .requires(source -> source.hasPermissionLevel(4) || PermissionUtil.hasPermission(source, Permissions.RELOAD))
                .executes(context -> {
                    topTemplate.getTopManager().disable();
                    mainConfig.reloadConfig();
                    messageConfig.reloadConfig();
                    topTemplate.getTopManager().enable();
                    sendMessage(context.getSource(), messageConfig.getSuccess());
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private int sendTopLines(String holderName, int fromIndex, int toIndex, CommandContext<ServerCommandSource> context) {
        Optional<NumberTopHolder> optional = topTemplate.getTopManager().getHolder(holderName);
        if (optional.isEmpty()) {
            sendMessage(context.getSource(), messageConfig.getTopHolderNotFound());
            return 0;
        }
        NumberTopHolder topHolder = optional.get();
        if (fromIndex > toIndex) {
            sendMessage(context.getSource(), messageConfig.getIllegalFromToIndex());
            return 0;
        }
        ValueDisplay valueDisplay = topHolder.getValueDisplay();
        List<String> topList = IntStream.rangeClosed(fromIndex, toIndex).mapToObj(index -> valueDisplay.getDisplayLine(index, topHolder)).toList();
        if (topList.isEmpty()) {
            sendMessage(context.getSource(), messageConfig.getTopEmpty());
        } else {
            topList.forEach(s -> sendMessage(context.getSource(), s));
        }
        return Command.SINGLE_SUCCESS;
    }

    private void sendMessage(ServerCommandSource serverCommandSource, String message) {
        String prefix = messageConfig.getPrefix();
        serverCommandSource.sendMessage(TextParserUtils.formatText(prefix + message));
    }

    public MinecraftServer getServer() {
        return server;
    }

    public Path getConfigFolder() {
        return configFolder;
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public ValueProviderManager getValueProviderManager() {
        return valueProviderManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public FabricStorageSupplierTemplate getStorageSupplierTemplate() {
        return storageSupplierTemplate;
    }

    public FabricTopTemplate getTopTemplate() {
        return topTemplate;
    }
}