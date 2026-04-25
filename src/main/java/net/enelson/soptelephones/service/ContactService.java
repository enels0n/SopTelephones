package net.enelson.soptelephones.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.enelson.soptelephones.model.ContactEntry;
import net.enelson.soptelephones.storage.StorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ContactService {
    private final StorageManager storageManager;
    private final Map<UUID, Map<String, ContactEntry>> contactsByPlayer = new LinkedHashMap<UUID, Map<String, ContactEntry>>();

    public ContactService(StorageManager storageManager) {
        this.storageManager = storageManager;
        reload();
    }

    public void reload() {
        this.contactsByPlayer.clear();

        YamlConfiguration config = this.storageManager.getPhonesConfig();
        ConfigurationSection contactsSection = config.getConfigurationSection("contacts");
        if (contactsSection == null) {
            return;
        }

        for (String ownerRaw : contactsSection.getKeys(false)) {
            UUID ownerId;
            try {
                ownerId = UUID.fromString(ownerRaw);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            ConfigurationSection ownerSection = contactsSection.getConfigurationSection(ownerRaw);
            if (ownerSection == null) {
                continue;
            }

            Map<String, ContactEntry> playerContacts = new LinkedHashMap<String, ContactEntry>();
            for (String key : ownerSection.getKeys(false)) {
                String base = "contacts." + ownerRaw + "." + key;
                String name = config.getString(base + ".name", key);
                String number = config.getString(base + ".number");
                if (number == null || number.isEmpty()) {
                    continue;
                }
                playerContacts.put(key.toLowerCase(), new ContactEntry(name, number));
            }

            if (!playerContacts.isEmpty()) {
                this.contactsByPlayer.put(ownerId, playerContacts);
            }
        }
    }

    public List<ContactEntry> getContacts(UUID ownerId) {
        Map<String, ContactEntry> contacts = this.contactsByPlayer.get(ownerId);
        if (contacts == null || contacts.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<ContactEntry>(contacts.values());
    }

    public ContactEntry getContact(UUID ownerId, String name) {
        Map<String, ContactEntry> contacts = this.contactsByPlayer.get(ownerId);
        if (contacts == null || name == null) {
            return null;
        }
        return contacts.get(name.toLowerCase());
    }

    public void setContact(UUID ownerId, String name, String number) {
        Map<String, ContactEntry> contacts = this.contactsByPlayer.get(ownerId);
        if (contacts == null) {
            contacts = new LinkedHashMap<String, ContactEntry>();
            this.contactsByPlayer.put(ownerId, contacts);
        }
        contacts.put(name.toLowerCase(), new ContactEntry(name, number));
        save();
    }

    public boolean removeContact(UUID ownerId, String name) {
        Map<String, ContactEntry> contacts = this.contactsByPlayer.get(ownerId);
        if (contacts == null || name == null) {
            return false;
        }
        ContactEntry removed = contacts.remove(name.toLowerCase());
        if (contacts.isEmpty()) {
            this.contactsByPlayer.remove(ownerId);
        }
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    private void save() {
        YamlConfiguration config = this.storageManager.getPhonesConfig();
        config.set("contacts", null);
        for (Map.Entry<UUID, Map<String, ContactEntry>> playerEntry : this.contactsByPlayer.entrySet()) {
            String playerBase = "contacts." + playerEntry.getKey().toString();
            for (Map.Entry<String, ContactEntry> contactEntry : playerEntry.getValue().entrySet()) {
                ContactEntry contact = contactEntry.getValue();
                String base = playerBase + "." + contactEntry.getKey();
                config.set(base + ".name", contact.getName());
                config.set(base + ".number", contact.getNumber());
            }
        }
        this.storageManager.savePhones();
    }
}
