package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.agent.core.AgentHolder;
import me.hsgamer.topper.fabric.TopperFabric;
import me.hsgamer.topper.fabric.TopperPlatform;
import me.hsgamer.topper.query.core.QueryResult;
import me.hsgamer.topper.query.forward.QueryForward;
import me.hsgamer.topper.query.forward.QueryForwardContext;
import me.hsgamer.topper.storage.core.DataStorage;
import me.hsgamer.topper.template.topplayernumber.TopPlayerNumberTemplate;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;
import me.hsgamer.topper.value.core.ValueProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FabricTopTemplate extends TopPlayerNumberTemplate {
    private final TopperFabric mod;
    private final TopperPlatform platform;
    private final QueryForward<UUID, QueryForwardContext<UUID>> queryForward;

    public FabricTopTemplate(TopperFabric mod, TopperPlatform platform) {
        super(new Settings() {
            @Override
            public Map<String, NumberTopHolder.Settings> holders() {
                return mod.getMainConfig().getHolders();
            }

            @Override
            public int taskSaveEntryPerTick() {
                return mod.getMainConfig().getTaskSaveEntryPerTick();
            }

            @Override
            public int taskUpdateEntryPerTick() {
                return mod.getMainConfig().getTaskUpdateEntryPerTick();
            }

            @Override
            public int taskUpdateMaxSkips() {
                return mod.getMainConfig().getTaskUpdateMaxSkips();
            }
        });
        this.mod = mod;
        this.platform = platform;
        this.queryForward = new QueryForward<>();
        this.queryForward.addContext(new QueryForwardContext<UUID>() {
            @Override
            public String getName() {
                return NumberTopHolder.GROUP;
            }

            @Override
            public BiFunction<UUID, @NotNull String, @NotNull QueryResult> getQuery() {
                return getTopQueryManager();
            }
        });
    }

    @Override
    public Function<String, DataStorage<UUID, Double>> getStorageSupplier() {
        return mod.getStorageSupplierTemplate().getNumberStorageSupplier();
    }

    @Override
    public Optional<ValueProvider<UUID, Double>> createValueProvider(Map<String, Object> settings) {
        return mod.getValueProviderManager().build(settings);
    }

    @Override
    public Runnable bindTask(AgentHolder<UUID, Double> holder, Runnable runnable, NumberTopHolder.TaskType taskType, Map<String, Object> settings) {
        return switch (taskType) {
            case SET -> mod.getTaskManager().schedule(runnable, mod.getMainConfig().getTaskUpdateSetDelay());
            case STORAGE -> mod.getTaskManager().schedule(runnable, mod.getMainConfig().getTaskSaveDelay());
            case UPDATE -> mod.getTaskManager().schedule(runnable, mod.getMainConfig().getTaskUpdateDelay());
            default -> mod.getTaskManager().schedule(runnable, 20L);
        };
    }

    @Override
    public void logWarning(String message, @Nullable Throwable throwable) {
        TopperFabric.LOGGER.warn(message, throwable);
    }

    @Override
    public String getName(UUID uuid) {
        return platform.getPlayerName(uuid);
    }

    public QueryForward<UUID, QueryForwardContext<UUID>> getQueryForward() {
        return queryForward;
    }
}
