package net.enelson.soptelephones.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.PhoneModel;
import net.enelson.soptelephones.model.SimCard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PhoneItemService {
    private final SopTelephonesPlugin plugin;
    private final Map<String, PhoneModel> phoneModels = new LinkedHashMap<String, PhoneModel>();
    private final NamespacedKey itemTypeKey;
    private final NamespacedKey phoneIdKey;
    private final NamespacedKey simIdKey;
    private final NamespacedKey modelIdKey;
    private final NamespacedKey itemKeyKey;

    public PhoneItemService(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
        this.itemTypeKey = new NamespacedKey(plugin, "item-type");
        this.phoneIdKey = new NamespacedKey(plugin, "phone-id");
        this.simIdKey = new NamespacedKey(plugin, "sim-id");
        this.modelIdKey = new NamespacedKey(plugin, "model-id");
        this.itemKeyKey = new NamespacedKey(plugin, "item-key");
        reload();
    }

    public void reload() {
        this.phoneModels.clear();
        ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("phone-models");
        if (section == null) {
            return;
        }

        for (String modelId : section.getKeys(false)) {
            String base = "phone-models." + modelId;
            PhoneModel model = new PhoneModel(
                modelId,
                ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString(base + ".display-name", "&bPhone")),
                this.plugin.getConfig().getString(base + ".item-key", "item.custom.telephone." + modelId.toLowerCase()),
                this.plugin.getConfig().getInt(base + ".custom-model-data", 0),
                this.plugin.getConfig().getInt(base + ".unread-custom-model-data", this.plugin.getConfig().getInt(base + ".custom-model-data", 0)),
                this.plugin.getConfig().getInt(base + ".no-sim-custom-model-data", this.plugin.getConfig().getInt(base + ".custom-model-data", 0)),
                this.plugin.getConfig().getDouble(base + ".signal-bonus", 0.0D),
                this.plugin.getConfig().getInt(base + ".history-size", 10)
            );
            this.phoneModels.put(modelId.toLowerCase(), model);
        }
    }

    public PhoneModel getPhoneModel(String modelId) {
        return modelId == null ? null : this.phoneModels.get(modelId.toLowerCase());
    }

    public ItemStack createPhoneItem(PhoneDevice device) {
        PhoneModel model = getPhoneModel(device.getModelId());
        if (model == null) {
            return new ItemStack(Material.BRICK);
        }

        ItemStack item = new ItemStack(Material.BRICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(model.getDisplayName());
        meta.setLocalizedName(model.getItemKey());
        meta.setCustomModelData(resolveCustomModelData(device, model));
        List<String> lore = new ArrayList<String>();
        if (device.hasSim()) {
            SimCard sim = this.plugin.getPhoneService().getSim(device.getSimId());
            lore.add(ChatColor.GRAY + "SIM: " + ChatColor.WHITE + (sim == null ? "unknown" : sim.getNumber()));
        } else {
            lore.add(ChatColor.RED + "No SIM installed");
        }
        if (device.hasUnread()) {
            lore.add(ChatColor.RED + "Unread messages");
        }
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.itemTypeKey, PersistentDataType.STRING, "phone");
        container.set(this.phoneIdKey, PersistentDataType.STRING, device.getDeviceId());
        container.set(this.modelIdKey, PersistentDataType.STRING, device.getModelId());
        container.set(this.itemKeyKey, PersistentDataType.STRING, model.getItemKey());
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createSimItem(SimCard simCard) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "SIM " + ChatColor.WHITE + simCard.getNumber());
        meta.setLocalizedName("item.custom.sim");
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Provider: " + ChatColor.WHITE + simCard.getProviderId());
        lore.add(ChatColor.GRAY + "Number: " + ChatColor.WHITE + simCard.getNumber());
        meta.setLore(lore);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.itemTypeKey, PersistentDataType.STRING, "sim");
        container.set(this.simIdKey, PersistentDataType.STRING, simCard.getSimId());
        item.setItemMeta(meta);
        return item;
    }

    public boolean isProtectedCustomItem(ItemStack item) {
        return isPhone(item) || isSim(item);
    }

    public boolean isPhone(ItemStack item) {
        return hasItemType(item, "phone");
    }

    public boolean isSim(ItemStack item) {
        return hasItemType(item, "sim");
    }

    private boolean hasItemType(ItemStack item, String type) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }
        String value = item.getItemMeta().getPersistentDataContainer().get(this.itemTypeKey, PersistentDataType.STRING);
        return type.equalsIgnoreCase(value);
    }

    public String getPhoneId(ItemStack item) {
        if (!isPhone(item)) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(this.phoneIdKey, PersistentDataType.STRING);
    }

    public String getSimId(ItemStack item) {
        if (!isSim(item)) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(this.simIdKey, PersistentDataType.STRING);
    }

    public void syncPlayerInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!isPhone(item)) {
                continue;
            }
            String deviceId = getPhoneId(item);
            PhoneDevice device = this.plugin.getPhoneService().getDevice(deviceId);
            if (device == null) {
                continue;
            }
            inventory.setItem(slot, createPhoneItem(device));
        }
        player.updateInventory();
    }

    public ItemStack findPhoneInHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return isPhone(item) ? item : null;
    }

    public PhoneDevice getPhoneDeviceInHand(Player player) {
        ItemStack item = findPhoneInHand(player);
        if (item == null) {
            return null;
        }
        return this.plugin.getPhoneService().getDevice(getPhoneId(item));
    }

    public List<PhoneDevice> getPhoneDevices(Player player) {
        List<PhoneDevice> result = new ArrayList<PhoneDevice>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (!isPhone(item)) {
                continue;
            }
            PhoneDevice device = this.plugin.getPhoneService().getDevice(getPhoneId(item));
            if (device != null) {
                result.add(device);
            }
        }
        return result;
    }

    public List<PhoneDevice> findDevicesByNumber(Player player, String number) {
        List<PhoneDevice> result = new ArrayList<PhoneDevice>();
        for (PhoneDevice device : getPhoneDevices(player)) {
            SimCard sim = this.plugin.getPhoneService().getInstalledSim(device);
            if (sim != null && sim.getNumber().equalsIgnoreCase(number)) {
                result.add(device);
            }
        }
        return result;
    }

    public double getSignalBonus(PhoneDevice device) {
        PhoneModel model = device == null ? null : getPhoneModel(device.getModelId());
        return model == null ? 0.0D : model.getSignalBonus();
    }

    public ItemStack createPhoneItem(String deviceId) {
        PhoneDevice device = this.plugin.getPhoneService().getDevice(deviceId);
        return device == null ? null : createPhoneItem(device);
    }

    public ItemStack createSimItem(String simId) {
        SimCard simCard = this.plugin.getPhoneService().getSim(simId);
        return simCard == null ? null : createSimItem(simCard);
    }

    private int resolveCustomModelData(PhoneDevice device, PhoneModel model) {
        if (!device.hasSim()) {
            return model.getNoSimCustomModelData();
        }
        if (device.hasUnread()) {
            return model.getUnreadCustomModelData();
        }
        return model.getCustomModelData();
    }
}
