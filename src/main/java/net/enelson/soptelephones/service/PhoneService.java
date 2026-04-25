package net.enelson.soptelephones.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.SimCard;
import net.enelson.soptelephones.storage.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PhoneService {
    private final StorageManager storageManager;
    private final Map<String, PhoneAccount> accountsByNumber = new LinkedHashMap<String, PhoneAccount>();
    private final Map<String, PhoneDevice> devicesById = new LinkedHashMap<String, PhoneDevice>();
    private final Map<String, SimCard> simsById = new LinkedHashMap<String, SimCard>();

    public PhoneService(StorageManager storageManager) {
        this.storageManager = storageManager;
        reload();
    }

    public void reload() {
        this.accountsByNumber.clear();
        this.devicesById.clear();
        this.simsById.clear();

        YamlConfiguration config = this.storageManager.getPhonesConfig();

        ConfigurationSection accountsSection = config.getConfigurationSection("accounts");
        if (accountsSection != null) {
            for (String number : accountsSection.getKeys(false)) {
                String base = "accounts." + number;
                String ownerRaw = config.getString(base + ".owner");
                if (ownerRaw == null || ownerRaw.isEmpty()) {
                    continue;
                }
                PhoneAccount account = new PhoneAccount(
                    number,
                    config.getString(base + ".provider-id", ""),
                    UUID.fromString(ownerRaw),
                    config.getBoolean(base + ".primary", false)
                );
                this.accountsByNumber.put(number.toLowerCase(), account);
            }
        }

        ConfigurationSection devicesSection = config.getConfigurationSection("devices");
        if (devicesSection != null) {
            for (String deviceId : devicesSection.getKeys(false)) {
                String base = "devices." + deviceId;
                PhoneDevice device = new PhoneDevice(
                    deviceId,
                    config.getString(base + ".model-id", "basic"),
                    config.getString(base + ".sim-id"),
                    config.getBoolean(base + ".unread", false)
                );
                this.devicesById.put(deviceId.toLowerCase(), device);
            }
        }

        ConfigurationSection simsSection = config.getConfigurationSection("sims");
        if (simsSection != null) {
            for (String simId : simsSection.getKeys(false)) {
                String base = "sims." + simId;
                String ownerRaw = config.getString(base + ".owner");
                if (ownerRaw == null || ownerRaw.isEmpty()) {
                    continue;
                }
                SimCard sim = new SimCard(
                    simId,
                    config.getString(base + ".number", ""),
                    config.getString(base + ".provider-id", ""),
                    UUID.fromString(ownerRaw)
                );
                this.simsById.put(simId.toLowerCase(), sim);
            }
        }
    }

    public PhoneAccount assignPhone(UUID ownerId, String providerId, String number) {
        PhoneAccount account = new PhoneAccount(number, providerId, ownerId, false);
        this.accountsByNumber.put(number.toLowerCase(), account);
        if (getPrimaryAccount(ownerId) == null) {
            setPrimary(ownerId, number);
        }
        save();
        return account;
    }

    public PhoneAccount getByNumber(String number) {
        return number == null ? null : this.accountsByNumber.get(number.toLowerCase());
    }

    public List<PhoneAccount> getAccounts(UUID ownerId) {
        List<PhoneAccount> result = new ArrayList<PhoneAccount>();
        for (PhoneAccount account : this.accountsByNumber.values()) {
            if (account.getOwnerId().equals(ownerId)) {
                result.add(account);
            }
        }
        return result;
    }

    public PhoneAccount getPrimaryAccount(UUID ownerId) {
        for (PhoneAccount account : this.accountsByNumber.values()) {
            if (account.getOwnerId().equals(ownerId) && account.isPrimary()) {
                return account;
            }
        }
        List<PhoneAccount> accounts = getAccounts(ownerId);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public void setPrimary(UUID ownerId, String number) {
        for (PhoneAccount account : this.accountsByNumber.values()) {
            if (account.getOwnerId().equals(ownerId)) {
                account.setPrimary(false);
            }
        }
        PhoneAccount selected = getByNumber(number);
        if (selected != null && selected.getOwnerId().equals(ownerId)) {
            selected.setPrimary(true);
        }
        save();
    }

    public PhoneDevice createDevice(String modelId) {
        String deviceId = "phone-" + UUID.randomUUID().toString().replace("-", "");
        PhoneDevice device = new PhoneDevice(deviceId, modelId, null, false);
        this.devicesById.put(deviceId.toLowerCase(), device);
        save();
        return device;
    }

    public SimCard createSim(UUID ownerId, String providerId, String number) {
        String simId = "sim-" + UUID.randomUUID().toString().replace("-", "");
        SimCard sim = new SimCard(simId, number, providerId, ownerId);
        this.simsById.put(simId.toLowerCase(), sim);
        save();
        return sim;
    }

    public PhoneDevice getDevice(String deviceId) {
        return deviceId == null ? null : this.devicesById.get(deviceId.toLowerCase());
    }

    public SimCard getSim(String simId) {
        return simId == null ? null : this.simsById.get(simId.toLowerCase());
    }

    public SimCard getInstalledSim(PhoneDevice device) {
        return device == null ? null : getSim(device.getSimId());
    }

    public String installSimFromItem(Player player, String deviceId, ItemStack simItem) {
        PhoneDevice device = getDevice(deviceId);
        if (device == null) {
            return ChatColor.RED + "Unknown phone device.";
        }
        if (device.hasSim()) {
            return ChatColor.RED + "This phone already has a SIM installed.";
        }

        String simId = SopTelephonesPlugin.getInstance().getPhoneItemService().getSimId(simItem);
        SimCard sim = getSim(simId);
        if (sim == null) {
            return ChatColor.RED + "Unknown SIM card.";
        }
        if (!sim.getOwnerId().equals(player.getUniqueId())) {
            return ChatColor.RED + "This SIM does not belong to you.";
        }

        device.setSimId(sim.getSimId());
        save();
        SopTelephonesPlugin.getInstance().getPhoneItemService().syncPlayerInventory(player);
        return null;
    }

    public ItemStack ejectSimToItem(String deviceId) {
        PhoneDevice device = getDevice(deviceId);
        if (device == null || !device.hasSim()) {
            return null;
        }
        String simId = device.getSimId();
        device.setSimId(null);
        save();
        return SopTelephonesPlugin.getInstance().getPhoneItemService().createSimItem(simId);
    }

    public void markUnread(String number) {
        for (PhoneDevice device : this.devicesById.values()) {
            SimCard sim = getInstalledSim(device);
            if (sim != null && sim.getNumber().equalsIgnoreCase(number)) {
                device.setUnread(true);
            }
        }
        save();
    }

    public void clearUnread(String deviceId) {
        PhoneDevice device = getDevice(deviceId);
        if (device == null || !device.hasUnread()) {
            return;
        }
        device.setUnread(false);
        save();
    }

    public void save() {
        YamlConfiguration config = this.storageManager.getPhonesConfig();
        config.set("accounts", null);
        config.set("devices", null);
        config.set("sims", null);

        for (PhoneAccount account : this.accountsByNumber.values()) {
            String base = "accounts." + account.getNumber();
            config.set(base + ".provider-id", account.getProviderId());
            config.set(base + ".owner", account.getOwnerId().toString());
            config.set(base + ".primary", account.isPrimary());
        }

        for (PhoneDevice device : this.devicesById.values()) {
            String base = "devices." + device.getDeviceId();
            config.set(base + ".model-id", device.getModelId());
            config.set(base + ".sim-id", device.getSimId());
            config.set(base + ".unread", device.hasUnread());
        }

        for (SimCard simCard : this.simsById.values()) {
            String base = "sims." + simCard.getSimId();
            config.set(base + ".number", simCard.getNumber());
            config.set(base + ".provider-id", simCard.getProviderId());
            config.set(base + ".owner", simCard.getOwnerId().toString());
        }

        this.storageManager.savePhones();
    }
}
