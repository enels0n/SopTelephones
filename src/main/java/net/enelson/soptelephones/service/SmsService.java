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
            return this.plugin.message("economy-unavailable");
        }

        SimCard senderSim = this.phoneService.getInstalledSim(senderDevice);
        if (senderSim == null) {
            return this.plugin.message("sim-missing-phone");
        }

        PhoneAccount recipientAccount = this.phoneService.getByNumber(toNumber);
        if (recipientAccount == null) {
            return this.plugin.message("unknown-number");
        }

        Provider senderProvider = this.providerService.getProvider(senderSim.getProviderId());
        if (senderProvider == null) {
            return this.plugin.message("sender-provider-missing");
        }

        if (this.providerService.findRange(senderProvider.getId(), senderSim.getNumber()) == null) {
            return this.plugin.message("sender-number-outside-range");
        }

        if (!this.towerService.isCovered(senderProvider.getId(), sender.getLocation(), this.phoneItemService.getSignalBonus(senderDevice))) {
            return this.plugin.message("sender-no-coverage");
        }

        Player recipient = Bukkit.getPlayer(recipientAccount.getOwnerId());
        boolean queueOffline = this.plugin.getConfig().getBoolean("messages.queue-offline-delivery", true);
        if (recipient == null && this.plugin.getConfig().getBoolean("messages.require-recipient-online", true) && !queueOffline) {
            return this.plugin.message("recipient-offline");
        }

        PhoneDevice recipientDevice = recipient == null ? null : findRecipientDevice(recipient, recipientAccount.getNumber());
        if (recipient != null && recipientDevice == null) {
            return this.plugin.message("recipient-no-phone");
        }
        if (recipient != null && !this.towerService.isCovered(recipientAccount.getProviderId(), recipient.getLocation(), this.phoneItemService.getSignalBonus(recipientDevice))) {
            return this.plugin.message("recipient-no-coverage");
        }

        double price = Math.max(0.0D, senderProvider.getSmsPrice());
        if (!this.economyService.has(sender, price)) {
            return this.plugin.message("not-enough-money", "{price}", this.priceFormat.format(price));
        }
        if (!this.economyService.withdraw(sender, price)) {
            return this.plugin.message("withdraw-failed");
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

        sender.sendMessage(recipient == null && queueOffline
            ? this.plugin.message("sms-queued", "{number}", message.getToNumber(), "{price}", this.priceFormat.format(price))
            : this.plugin.message("sms-sent", "{number}", message.getToNumber(), "{price}", this.priceFormat.format(price)));
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
                recipient.sendMessage(this.plugin.message("queued-sms-format", "{from}", message.getFromNumber(), "{content}", message.getContent()));
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
        recipient.sendMessage(this.plugin.message(queued ? "queued-sms-format" : "sms-format", "{from}", message.getFromNumber(), "{content}", message.getContent()));
        recipient.playSound(recipient.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8F, 1.7F);
        this.phoneService.markUnread(recipientAccount.getNumber());
        this.phoneItemService.syncPlayerInventory(recipient);
        Bukkit.getPluginManager().callEvent(new SmsDeliveredEvent(recipient, message, queued));
    }
}
