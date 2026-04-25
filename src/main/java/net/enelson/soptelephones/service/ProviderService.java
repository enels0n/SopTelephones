package net.enelson.soptelephones.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.enelson.soptelephones.model.NumberRange;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.storage.StorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ProviderService {
    private final StorageManager storageManager;
    private final Map<String, Provider> providers = new LinkedHashMap<String, Provider>();
    private final Map<String, NumberRange> ranges = new LinkedHashMap<String, NumberRange>();

    public ProviderService(StorageManager storageManager) {
        this.storageManager = storageManager;
        reload();
    }

    public void reload() {
        this.providers.clear();
        this.ranges.clear();

        YamlConfiguration config = this.storageManager.getProvidersConfig();
        ConfigurationSection providersSection = config.getConfigurationSection("providers");
        if (providersSection != null) {
            for (String providerId : providersSection.getKeys(false)) {
                String base = "providers." + providerId;
                Provider provider = new Provider(
                    providerId,
                    config.getString(base + ".display-name", providerId),
                    config.getDouble(base + ".sms-price", 10.0D),
                    config.getDouble(base + ".balance", 0.0D)
                );
                this.providers.put(providerId.toLowerCase(), provider);
            }
        }

        ConfigurationSection rangesSection = config.getConfigurationSection("ranges");
        if (rangesSection != null) {
            for (String rangeId : rangesSection.getKeys(false)) {
                String base = "ranges." + rangeId;
                NumberRange range = new NumberRange(
                    rangeId,
                    config.getString(base + ".provider-id", ""),
                    config.getString(base + ".prefix", ""),
                    config.getInt(base + ".from"),
                    config.getInt(base + ".to")
                );
                this.ranges.put(rangeId.toLowerCase(), range);
            }
        }
    }

    public Provider createProvider(String id, String displayName) {
        Provider provider = new Provider(id, displayName, 10.0D, 0.0D);
        this.providers.put(id.toLowerCase(), provider);
        save();
        return provider;
    }

    public NumberRange addRange(String providerId, String prefix, int from, int to) {
        String rangeId = providerId.toLowerCase() + "-" + prefix.toLowerCase() + "-" + from + "-" + to;
        NumberRange range = new NumberRange(rangeId, providerId, prefix, from, to);
        this.ranges.put(rangeId.toLowerCase(), range);
        save();
        return range;
    }

    public Provider getProvider(String providerId) {
        return providerId == null ? null : this.providers.get(providerId.toLowerCase());
    }

    public NumberRange findRange(String providerId, String number) {
        for (NumberRange range : this.ranges.values()) {
            if (!range.getProviderId().equalsIgnoreCase(providerId)) {
                continue;
            }
            if (range.contains(number)) {
                return range;
            }
        }
        return null;
    }

    public void save() {
        YamlConfiguration config = this.storageManager.getProvidersConfig();
        config.set("providers", null);
        config.set("ranges", null);

        for (Provider provider : this.providers.values()) {
            String base = "providers." + provider.getId();
            config.set(base + ".display-name", provider.getDisplayName());
            config.set(base + ".sms-price", provider.getSmsPrice());
            config.set(base + ".balance", provider.getBalance());
        }

        for (NumberRange range : this.ranges.values()) {
            String base = "ranges." + range.getId();
            config.set(base + ".provider-id", range.getProviderId());
            config.set(base + ".prefix", range.getPrefix());
            config.set(base + ".from", range.getFrom());
            config.set(base + ".to", range.getTo());
        }

        this.storageManager.saveProviders();
    }

    public Collection<Provider> getProviders() {
        return providers.values();
    }

    public List<NumberRange> getRangesForProvider(String providerId) {
        List<NumberRange> result = new ArrayList<NumberRange>();
        for (NumberRange range : this.ranges.values()) {
            if (range.getProviderId().equalsIgnoreCase(providerId)) {
                result.add(range);
            }
        }
        return result;
    }
}

