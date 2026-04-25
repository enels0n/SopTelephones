package net.enelson.soptelephones.model;

public final class PhoneModel {
    private final String id;
    private final String displayName;
    private final String itemKey;
    private final int customModelData;
    private final int unreadCustomModelData;
    private final int noSimCustomModelData;
    private final double signalBonus;
    private final int historySize;

    public PhoneModel(
        String id,
        String displayName,
        String itemKey,
        int customModelData,
        int unreadCustomModelData,
        int noSimCustomModelData,
        double signalBonus,
        int historySize
    ) {
        this.id = id;
        this.displayName = displayName;
        this.itemKey = itemKey;
        this.customModelData = customModelData;
        this.unreadCustomModelData = unreadCustomModelData;
        this.noSimCustomModelData = noSimCustomModelData;
        this.signalBonus = signalBonus;
        this.historySize = historySize;
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getItemKey() {
        return this.itemKey;
    }

    public int getCustomModelData() {
        return this.customModelData;
    }

    public int getUnreadCustomModelData() {
        return this.unreadCustomModelData;
    }

    public int getNoSimCustomModelData() {
        return this.noSimCustomModelData;
    }

    public double getSignalBonus() {
        return this.signalBonus;
    }

    public int getHistorySize() {
        return this.historySize;
    }
}
