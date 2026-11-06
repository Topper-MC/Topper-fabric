package me.hsgamer.topper.fabric.template;

import me.hsgamer.topper.agent.snapshot.SnapshotAgent;
import me.hsgamer.topper.query.simple.SimpleQueryDisplay;
import me.hsgamer.topper.template.snapshotdisplayline.SnapshotDisplayLine;
import me.hsgamer.topper.template.topplayernumber.holder.NumberTopHolder;

import java.util.Optional;
import java.util.UUID;

public class FabricTopDisplayLine implements SnapshotDisplayLine<UUID, Double> {
    private final NumberTopHolder holder;

    public FabricTopDisplayLine(NumberTopHolder holder) {
        this.holder = holder;
    }

    @Override
    public SimpleQueryDisplay<UUID, Double> getDisplay() {
        return holder.getValueDisplay();
    }

    @Override
    public SnapshotAgent<UUID, Double> getSnapshotAgent() {
        return holder.getSnapshotAgent();
    }

    @Override
    public String getDisplayLine() {
        return Optional.of(holder.getSettings())
                .filter(FabricTopHolderSettings.class::isInstance)
                .map(FabricTopHolderSettings.class::cast)
                .map(FabricTopHolderSettings::defaultLine)
                .orElse("<gray>[<aqua>{index}<gray>] <aqua>{name} <gray>- <aqua>{value}");
    }
}
