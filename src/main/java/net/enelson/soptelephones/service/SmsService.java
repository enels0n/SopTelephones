package net.enelson.soptelephones.service;

import java.text.DecimalFormat;
import java.util.List;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.event.SmsDeliveredEvent;
import net.enelson.soptelephones.event.SmsQueuedEvent;
import net.enelson.soptelephones.event.SmsSentEvent;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.model.SimCard;
import net.enelson.soptelephones.model.SmsMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SmsService {
    private final SopTelephonesPlugin plugin;
    private final ProviderService providerService;
    private final PhoneService phoneService;
    private final PhoneItemService phoneItemService;
    private final MessageHistoryService messageHistoryService;
    private final TowerService towerService;
    private final EconomyService economyService;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");

    public SmsService(
        SopTelephonesPlugin plugin,
        ProviderService providerService,
        PhoneService phoneService,
        PhoneItemService phoneItemService,
        MessageHistoryService messageHistoryService,
        TowerService towerService,
        EconomyService economyService
    ) {
        this.plugin = plugin;
        this.providerService = providerService;
        this.phoneService = phoneService;
        this.phoneItemService = phoneItemService;
        this.messageHistoryService = messageHistoryService;
        this.towerService = towerService;
        this.economyService = economyService;
    }

    public String send(Player sender, PhoneDevice senderDevice, String toNumber, String content) {
        if (!this.economyService.isEnabled()) {
            return ChatColor.RED + "Economy is unavailable.";
        }

        SimCard senderSim = this.phoneService.getInstalledSim(senderDevice);
        if (senderSim == null) {
            return ChatColor.RED + "No SIM is installed in that phone.";
        }

        PhoneAccount recipientAccount = this.phoneService.getByNumber(toNumber);
        if (recipientAccount == null) {
            return ChatColor.RED + "Unknown number.";
        }

        Provider senderProvider = this.providerService.getProvider(senderSim.getProviderId());
        if (senderProvider == null) {
            return ChatColor.RED + "Your provider is missing.";
        }

        if (this.providerService.findRange(senderProvider.getId(), senderSim.getNumber()) == null) {
            return ChatColor.RED + "Your number is outside the provider ranges.";
        }

        if (!this.towerService.isCovered(senderProvider.getId(), sender.getLocation(), this.phoneItemService.getSignalBonus(senderDevice))) {
            return ChatColor.RED + "No coverage from your provider.";
        }

        Player recipient = Bukkit.getPlayer(recipientAccount.getOwnerId());
        boolean queueOffline = this.plugin.getConfig().getBoolean("messages.queue-offline-delivery", true);
        if (recipient == null && this.plugin.getConfig().getBoolean("messages.require-recipient-online", true) && !queueOffline) {
            return ChatColor.RED + "Recipient is offline.";
        }

        PhoneDevice recipientDevice = recipient == null ? null : findRecipientDevice(recipient, recipientAccount.getNumber());
        if (recipient != null && recipientDevice == null) {
            return ChatColor.RED + "Recipient does not have that phone available.";
        }
        if (recipient != null && !this.towerService.isCovered(recipientAccount.getProviderId(), recipient.getLocation(), this.phoneItemService.getSignalBonus(recipientDevice))) {
            return ChatColor.RED + "Recipient is currently out of coverage.";
        }

        double price = Math.max(0.0D, senderProvider.getSmsPrice());
        if (!this.economyService.has(sender, price)) {
            return ChatColor.RED + "You need $" + this.priceFormat.format(price) + " to send this message.";
        }
        if (!this.economyService.withdraw(sender, price)) {
            return ChatColor.RED + "Failed to withdraw the SMS cost.";
        }

        double taxPercent = this.plugin.getConfig().getDouble("economy.server-tax-percent", 10.0D);
        double providerShare = price * Math.max(0.0D, 100.0D - taxPercent) / 100.0D;
        senderProvider.addBalance(providerShare);
        this.providerService.save();

        SmsMessage message = new SmsMessage(senderSim.getNumber(), recipientAccount.getNumber(), content, System.currentTimeMillis());
        this.messageHistoryService.recordMessage(message);
        Bukkit.getPluginManager().callEvent(new SmsSentEvent(sender, recipientAccount, message, price));

        if (recipient != null) {
            deliverToOnlineRecipient(recipientAccount, recipient, message, false);
        } else if (queueOffline) {
            this.messageHistoryService.queuePendingMessage(message);
            Bukkit.getPluginManager().callEvent(new SmsQueuedEvent(recipientAccount, message));
        }

        sender.sendMessage(ChatColor.GREEN + (recipient == null && queueOffline
            ? "SMS queued for " + message.getToNumber() + " for $" + this.priceFormat.format(price) + "."
            : "SMS sent to " + message.getToNumber() + " for $" + this.priceFormat.format(price) + "."));
        return null;
    }

    public void deliverQueuedMessages(Player recipient) {
        List<PhoneAccount> accounts = this.phoneService.getAccounts(recipient.getUniqueId());
        if (accounts.isEmpty()) {
            return;
        }

        boolean deliveredAny = false;
        for (PhoneAccount account : accounts) {
            List<SmsMessage> pendingMessages = this.messageHistoryService.consumePendingMessages(account.getNumber());
            if (pendingMessages.isEmpty()) {
                continue;
            }
            this.phoneService.markUnread(account.getNumber());
            for (SmsMessage message : pendingMessages) {
                recipient.sendMessage(ChatColor.AQUA + "[Queued SMS] " + message.getFromNumber() + ": " + ChatColor.WHITE + message.getContent());
                Bukkit.getPluginManager().callEvent(new SmsDeliveredEvent(recipient, message, true));
            }
            deliveredAny = true;
        }

        if (deliveredAny) {
            recipient.playSound(recipient.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8F, 1.7F);
            this.phoneItemService.syncPlayerInventory(recipient);
        }
    }

    private PhoneDevice findRecipientDevice(Player player, String number) {
        for (PhoneDevice device : this.phoneItemService.findDevicesByNumber(player, number)) {
            return device;
        }
        return null;
    }

    private void deliverToOnlineRecipient(PhoneAccount recipientAccount, Player recipient, SmsMessage message, boolean queued) {
        recipient.sendMessage(ChatColor.AQUA + (queued ? "[Queued SMS] " : "[SMS] ") + message.getFromNumber() + ": " + ChatColor.WHITE + message.getContent());
        recipient.playSound(recipient.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8F, 1.7F);
        this.phoneService.markUnread(recipientAccount.getNumber());
        this.phoneItemService.syncPlayerInventory(recipient);
        Bukkit.getPluginManager().callEvent(new SmsDeliveredEvent(recipient, message, queued));
    }
}
