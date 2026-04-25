package net.enelson.soptelephones.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.storage.StorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PhoneService {
    private final StorageManager storageManager;
    private final Map<String, PhoneAccount> accountsByNumber = new LinkedHashMap<String, PhoneAccount>();

    public PhoneService(StorageManager storageManager) {
        this.storageManager = storageManager;
        reload();
    }

    public void reload() {
        this.accountsByNumber.clear();
        YamlConfiguration config = this.storageManager.getPhonesConfig();
        ConfigurationSection section = config.getConfigurationSection("accounts");
        if (section == null) {
            return;
        }
        for (String number : section.getKeys(false)) {
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

    public void save() {
        YamlConfiguration config = this.storageManager.getPhonesConfig();
        config.set("accounts", null);
        for (PhoneAccount account : this.accountsByNumber.values()) {
            String base = "accounts." + account.getNumber();
            config.set(base + ".provider-id", account.getProviderId());
            config.set(base + ".owner", account.getOwnerId().toString());
            config.set(base + ".primary", account.isPrimary());
        }
        this.storageManager.savePhones();
    }
}

