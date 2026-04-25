package net.enelson.soptelephones.service;

import java.text.DecimalFormat;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.model.SmsMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class SmsService {
    private final SopTelephonesPlugin plugin;
    private final ProviderService providerService;
    private final PhoneService phoneService;
    private final TowerService towerService;
    private final EconomyService economyService;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");

    public SmsService(SopTelephonesPlugin plugin, ProviderService providerService, PhoneService phoneService, TowerService towerService, EconomyService economyService) {
        this.plugin = plugin;
        this.providerService = providerService;
        this.phoneService = phoneService;
        this.towerService = towerService;
        this.economyService = economyService;
    }

    public String send(Player sender, String toNumber, String content) {
        if (!this.economyService.isEnabled()) {
            return ChatColor.RED + "Economy is unavailable.";
        }

        PhoneAccount senderAccount = this.phoneService.getPrimaryAccount(sender.getUniqueId());
        if (senderAccount == null) {
            return ChatColor.RED + "You do not have an active phone.";
        }

        PhoneAccount recipientAccount = this.phoneService.getByNumber(toNumber);
        if (recipientAccount == null) {
            return ChatColor.RED + "Unknown number.";
        }

        Provider senderProvider = this.providerService.getProvider(senderAccount.getProviderId());
        if (senderProvider == null) {
            return ChatColor.RED + "Your provider is missing.";
        }

        if (this.providerService.findRange(senderProvider.getId(), senderAccount.getNumber()) == null) {
            return ChatColor.RED + "Your number is outside the provider ranges.";
        }

        if (!this.towerService.isCovered(senderProvider.getId(), sender.getLocation())) {
            return ChatColor.RED + "No coverage from your provider.";
        }

        Player recipient = Bukkit.getPlayer(recipientAccount.getOwnerId());
        if (recipient == null && this.plugin.getConfig().getBoolean("messages.require-recipient-online", true)) {
            return ChatColor.RED + "Recipient is offline.";
        }
        if (recipient != null && !this.towerService.isCovered(recipientAccount.getProviderId(), recipient.getLocation())) {
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
        double providerShare = price * Math.max(0.0D, (100.0D - taxPercent)) / 100.0D;
        senderProvider.addBalance(providerShare);
        this.providerService.save();

        SmsMessage message = new SmsMessage(senderAccount.getNumber(), recipientAccount.getNumber(), content, System.currentTimeMillis());
        if (recipient != null) {
            recipient.sendMessage(ChatColor.AQUA + "[SMS] " + message.getFromNumber() + ": " + ChatColor.WHITE + message.getContent());
        }
        sender.sendMessage(ChatColor.GREEN + "SMS sent to " + message.getToNumber() + " for $" + this.priceFormat.format(price) + ".");
        return null;
    }
}

