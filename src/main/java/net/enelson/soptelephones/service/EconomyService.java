package net.enelson.soptelephones.service;

import net.enelson.soptelephones.SopTelephonesPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyService {
    private final SopTelephonesPlugin plugin;
    private Economy economy;

    public EconomyService(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        RegisteredServiceProvider<Economy> provider = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        this.economy = provider == null ? null : provider.getProvider();
    }

    public boolean isEnabled() {
        if (this.economy == null) {
            return !this.plugin.getConfig().getBoolean("economy.require-vault", true);
        }
        return true;
    }

    public boolean has(Player player, double amount) {
        return this.economy == null || this.economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        return this.economy == null || this.economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}

