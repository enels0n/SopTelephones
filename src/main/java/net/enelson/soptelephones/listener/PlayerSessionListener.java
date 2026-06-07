package net.enelson.soptelephones.listener;

import net.enelson.soptelephones.SopTelephonesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerSessionListener implements Listener {
    private final SopTelephonesPlugin plugin;

    public PlayerSessionListener(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.plugin.getSmsService().deliverQueuedMessages(event.getPlayer());
        this.plugin.getPhoneItemService().syncPlayerInventory(event.getPlayer());
    }
}
