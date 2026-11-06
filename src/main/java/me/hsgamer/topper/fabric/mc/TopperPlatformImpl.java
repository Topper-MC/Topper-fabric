package me.hsgamer.topper.fabric.mc;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import io.github.projectunified.faststats.core.Metric;
import io.github.projectunified.faststats.core.Metrics;
import io.github.projectunified.faststats.fabric.FabricPlatform;
import io.github.projectunified.faststats.gson.GsonSerializer;
import io.github.projectunified.faststats.httpclient.HttpClientSubmitter;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.topper.fabric.Permissions;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.TopperPlatform;
import me.hsgamer.topper.fabric.hook.miniplaceholders.MiniPlaceholdersHook;
import me.hsgamer.topper.fabric.hook.textplaceholderapi.TextPlaceholderAPIHook;
import me.hsgamer.topper.fabric.provider.StatisticValueProvider;
import me.hsgamer.topper.fabric.template.FabricTopDisplayLine;
import me.hsgamer.topper.fabric.util.PermissionUtil;
import me.hsgamer.topper.fabric.util.ProfileUtil;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

public class TopperPlatformImpl implements TopperPlatform {
    private static TopperPlatformImpl instance;

    private final List<Runnable> disableRunnables = new ArrayList<>();
    private TopperFabric mod;
    private MinecraftServer server;

    public TopperPlatformImpl() {
        instance = this;
    }

    public static TopperPlatformImpl getInstance() {
        return instance;
    }

    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public void initialize(TopperFabric mod) {
        this.mod = mod;

        mod.getValueProviderManager().register(map -> {
            String statType = Optional.ofNullable(map.get("statistic-type")).map(Object::toString).orElse("minecraft:custom");
            List<String> statNames = CollectionUtils.createStringListFromObject(map.get("statistic"), true);
            return new StatisticValueProvider(statType, statNames).beforeApply(uuid -> {
                PlayerList playerManager = server.getPlayerList();
                return playerManager.getPlayer(uuid);
            });
        }, "statistic");

        disableRunnables.add(TextPlaceholderAPIHook.addHook(mod));
        if (MiniPlaceholdersHook.isAvailable()) {
            disableRunnables.add(MiniPlaceholdersHook.addHook(mod));
        }

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStop);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> mod.onPlayerJoin(handler.getPlayer().getUUID()));
        CommandRegistrationCallback.EVENT.register(this::registerCommand);
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
        mod.getTopTemplate().enable();

        Metrics metrics = Metrics.builder()
                .platform(new FabricPlatform(server, TopperFabric.MOD_ID))
                .serializer(new GsonSerializer())
                .submitter(new HttpClientSubmitter("314aeec477ff85ca7e547c506bebf24b"))
                .addMetric(Metric.number("holders", () -> mod.getTopTemplate().getTopManager().getHolderNames().size()))
                .build();
        metrics.start();
        disableRunnables.add(metrics::shutdown);
    }

    private void onServerStop(MinecraftServer server) {
        disableRunnables.forEach(Runnable::run);
        disableRunnables.clear();
        mod.onServerStop();
        this.server = null;
    }

    @Override
    public String getPlayerName(UUID uuid) {
        ServerPlayer player = ProfileUtil.getPlayer(server, uuid);
        return player != null ? ProfileUtil.getName(player.getGameProfile()) : ProfileUtil.getOfflineName(server, uuid);
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return PermissionUtil.hasPermission(server, uuid, permission);
    }

    private void registerCommand(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        commandDispatcher.register(Commands.literal("gettop")
                .requires(source -> PermissionUtil.hasPermissionLevel(source, 2) || PermissionUtil.hasPermission(source, Permissions.GET_TOP_LINES))
                .then(
                        Commands.argument("holder", StringArgumentType.word()).suggests((context, builder) -> {
                                    mod.getTopTemplate().getTopManager().getHolderNames().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> sendTopLines(StringArgumentType.getString(context, "holder"), 1, 10, context))
                                .then(
                                        Commands.argument("i1", IntegerArgumentType.integer(1))
                                                .executes(context -> sendTopLines(StringArgumentType.getString(context, "holder"), 1, IntegerArgumentType.getInteger(context, "i1"), context))
                                                .then(Commands.argument("i2", IntegerArgumentType.integer(1))
                                                        .executes(context -> sendTopLines(StringArgumentType.getString(context, "holder"), IntegerArgumentType.getInteger(context, "i1"), IntegerArgumentType.getInteger(context, "i2"), context))
                                                )
                                )
                ));
        commandDispatcher.register(Commands.literal("reloadtop")
                .requires(source -> PermissionUtil.hasPermissionLevel(source, 4) || PermissionUtil.hasPermission(source, Permissions.RELOAD))
                .executes(context -> {
                    mod.reloadTemplate();
                    sendMessage(context.getSource(), mod.getMessageConfig().getSuccess());
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private int sendTopLines(String holderName, int fromIndex, int toIndex, CommandContext<CommandSourceStack> context) {
        Optional<NumberTopHolder> optional = mod.getTopTemplate().getTopManager().getHolder(holderName);
        if (optional.isEmpty()) {
            sendMessage(context.getSource(), mod.getMessageConfig().getTopHolderNotFound());
            return 0;
        }
        NumberTopHolder topHolder = optional.get();
        if (fromIndex > toIndex) {
            sendMessage(context.getSource(), mod.getMessageConfig().getIllegalFromToIndex());
            return 0;
        }
        FabricTopDisplayLine topDisplayLine = new FabricTopDisplayLine(topHolder);
        List<String> topList = IntStream.rangeClosed(fromIndex, toIndex).mapToObj(topDisplayLine::display).toList();
        if (topList.isEmpty()) {
            sendMessage(context.getSource(), mod.getMessageConfig().getTopEmpty());
        } else {
            topList.forEach(s -> sendMessage(context.getSource(), s));
        }
        return Command.SINGLE_SUCCESS;
    }

    private void sendMessage(CommandSourceStack serverCommandSource, String message) {
        String prefix = mod.getMessageConfig().getPrefix();
        //? if >= 26.1 {
        Component component = TagParser.QUICK_TEXT.parseComponent(prefix + message, ParserContext.of());
        //? } else {
        //Component component = TagParser.QUICK_TEXT.parseText(prefix + message, ParserContext.of());
        //? }
        serverCommandSource.sendSystemMessage(component);
    }
}
