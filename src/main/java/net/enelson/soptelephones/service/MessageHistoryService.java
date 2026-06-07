package net.enelson.soptelephones.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.enelson.soptelephones.model.SmsMessage;
import net.enelson.soptelephones.storage.StorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageHistoryService {
    private final StorageManager storageManager;
    private final List<SmsMessage> messages = new ArrayList<SmsMessage>();
    private final Map<String, List<SmsMessage>> pendingMessagesByNumber = new LinkedHashMap<String, List<SmsMessage>>();

    public MessageHistoryService(StorageManager storageManager) {
        this.storageManager = storageManager;
        reload();
    }

    public void reload() {
        this.messages.clear();
        this.pendingMessagesByNumber.clear();
        YamlConfiguration config = this.storageManager.getPhonesConfig();
        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section == null) {
            return;
        }

        for (String messageId : section.getKeys(false)) {
            String base = "messages." + messageId;
            String from = config.getString(base + ".from");
            String to = config.getString(base + ".to");
            String content = config.getString(base + ".content");
            if (from == null || to == null || content == null) {
                continue;
            }
            this.messages.add(new SmsMessage(from, to, content, config.getLong(base + ".sent-at")));
        }

        Collections.sort(this.messages, new Comparator<SmsMessage>() {
            @Override
            public int compare(SmsMessage first, SmsMessage second) {
                return Long.compare(first.getSentAt(), second.getSentAt());
            }
        });

        ConfigurationSection pendingSection = config.getConfigurationSection("pending-messages");
        if (pendingSection == null) {
            return;
        }

        for (String number : pendingSection.getKeys(false)) {
            ConfigurationSection numberSection = pendingSection.getConfigurationSection(number);
            if (numberSection == null) {
                continue;
            }
            List<SmsMessage> pendingMessages = new ArrayList<SmsMessage>();
            for (String messageId : numberSection.getKeys(false)) {
                String base = "pending-messages." + number + "." + messageId;
                String from = config.getString(base + ".from");
                String to = config.getString(base + ".to");
                String content = config.getString(base + ".content");
                if (from == null || to == null || content == null) {
                    continue;
                }
                pendingMessages.add(new SmsMessage(from, to, content, config.getLong(base + ".sent-at")));
            }
            if (!pendingMessages.isEmpty()) {
                Collections.sort(pendingMessages, new Comparator<SmsMessage>() {
                    @Override
                    public int compare(SmsMessage first, SmsMessage second) {
                        return Long.compare(first.getSentAt(), second.getSentAt());
                    }
                });
                this.pendingMessagesByNumber.put(number.toLowerCase(), pendingMessages);
            }
        }
    }

    public void recordMessage(SmsMessage message) {
        this.messages.add(message);
        save();
    }

    public void queuePendingMessage(SmsMessage message) {
        String key = message.getToNumber().toLowerCase();
        List<SmsMessage> pendingMessages = this.pendingMessagesByNumber.get(key);
        if (pendingMessages == null) {
            pendingMessages = new ArrayList<SmsMessage>();
            this.pendingMessagesByNumber.put(key, pendingMessages);
        }
        pendingMessages.add(message);
        save();
    }

    public List<SmsMessage> consumePendingMessages(String number) {
        if (number == null) {
            return Collections.emptyList();
        }
        List<SmsMessage> removed = this.pendingMessagesByNumber.remove(number.toLowerCase());
        if (removed == null || removed.isEmpty()) {
            return Collections.emptyList();
        }
        save();
        return new ArrayList<SmsMessage>(removed);
    }

    public List<String> getRecentConversationTargets(String number, int limit) {
        Set<String> targets = new LinkedHashSet<String>();
        for (int index = this.messages.size() - 1; index >= 0; index--) {
            SmsMessage message = this.messages.get(index);
            if (message.getFromNumber().equalsIgnoreCase(number)) {
                targets.add(message.getToNumber());
            } else if (message.getToNumber().equalsIgnoreCase(number)) {
                targets.add(message.getFromNumber());
            }
            if (targets.size() >= limit) {
                break;
            }
        }
        return new ArrayList<String>(targets);
    }

    public List<SmsMessage> getConversation(String firstNumber, String secondNumber, int limit) {
        List<SmsMessage> result = new ArrayList<SmsMessage>();
        for (int index = this.messages.size() - 1; index >= 0; index--) {
            SmsMessage message = this.messages.get(index);
            boolean forward = message.getFromNumber().equalsIgnoreCase(firstNumber) && message.getToNumber().equalsIgnoreCase(secondNumber);
            boolean backward = message.getFromNumber().equalsIgnoreCase(secondNumber) && message.getToNumber().equalsIgnoreCase(firstNumber);
            if (forward || backward) {
                result.add(message);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        Collections.reverse(result);
        return result;
    }

    private void save() {
        YamlConfiguration config = this.storageManager.getPhonesConfig();
        config.set("messages", null);
        config.set("pending-messages", null);
        for (int index = 0; index < this.messages.size(); index++) {
            SmsMessage message = this.messages.get(index);
            String base = "messages." + index;
            config.set(base + ".from", message.getFromNumber());
            config.set(base + ".to", message.getToNumber());
            config.set(base + ".content", message.getContent());
            config.set(base + ".sent-at", message.getSentAt());
        }
        for (Map.Entry<String, List<SmsMessage>> entry : this.pendingMessagesByNumber.entrySet()) {
            List<SmsMessage> pendingMessages = entry.getValue();
            for (int index = 0; index < pendingMessages.size(); index++) {
                SmsMessage message = pendingMessages.get(index);
                String base = "pending-messages." + entry.getKey() + "." + index;
                config.set(base + ".from", message.getFromNumber());
                config.set(base + ".to", message.getToNumber());
                config.set(base + ".content", message.getContent());
                config.set(base + ".sent-at", message.getSentAt());
            }
        }
        this.storageManager.savePhones();
    }
}
