package net.enelson.soptelephones.api;

import java.util.List;
import java.util.UUID;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.PhoneDevice;
import org.bukkit.entity.Player;

public final class SopTelephonesApi {
    private static SopTelephonesApi instance;

    private final SopTelephonesPlugin plugin;

    public SopTelephonesApi(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static SopTelephonesApi getInstance() {
        return instance;
    }

    public List<PhoneAccount> getAccounts(UUID ownerId) {
        return this.plugin.getPhoneService().getAccounts(ownerId);
    }

    public PhoneAccount getAccountByNumber(String number) {
        return this.plugin.getPhoneService().getByNumber(number);
    }

    public PhoneDevice getPhoneDeviceInHand(Player player) {
        return this.plugin.getPhoneItemService().getPhoneDeviceInHand(player);
    }

    public String sendSms(Player sender, PhoneDevice senderDevice, String toNumber, String content) {
        return this.plugin.getSmsService().send(sender, senderDevice, toNumber, content);
    }
}
