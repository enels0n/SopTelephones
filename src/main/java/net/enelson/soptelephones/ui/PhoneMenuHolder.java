package net.enelson.soptelephones.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PhoneMenuHolder implements InventoryHolder {
    private final String deviceId;
    private final Map<Integer, String> numbersBySlot = new LinkedHashMap<Integer, String>();

    public PhoneMenuHolder(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void bindNumber(int slot, String number) {
        this.numbersBySlot.put(slot, number);
    }

    public String getNumberBySlot(int slot) {
        return this.numbersBySlot.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
