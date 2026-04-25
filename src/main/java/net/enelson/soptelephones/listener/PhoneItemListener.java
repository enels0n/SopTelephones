package net.enelson.soptelephones.listener;

import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.ui.PhoneMenuHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class PhoneItemListener implements Listener {
    private final SopTelephonesPlugin plugin;

    public PhoneItemListener(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhoneUse(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!this.plugin.getPhoneItemService().isPhone(event.getItem())) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        String deviceId = this.plugin.getPhoneItemService().getPhoneId(event.getItem());
        if (deviceId == null) {
            return;
        }

        if (player.isSneaking()) {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand != null && offhand.getType() != Material.AIR && this.plugin.getPhoneItemService().isSim(offhand)) {
                String error = this.plugin.getPhoneService().installSimFromItem(player, deviceId, offhand);
                if (error != null) {
                    player.sendMessage(error);
                } else {
                    if (offhand.getAmount() <= 1) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        offhand.setAmount(offhand.getAmount() - 1);
                    }
                    player.sendMessage(ChatColor.GREEN + "SIM inserted into the phone.");
                }
                return;
            }

            ItemStack simItem = this.plugin.getPhoneService().ejectSimToItem(deviceId);
            if (simItem == null) {
                player.sendMessage(ChatColor.RED + "There is no SIM installed.");
                return;
            }
            player.getInventory().addItem(simItem);
            this.plugin.getPhoneItemService().syncPlayerInventory(player);
            player.sendMessage(ChatColor.YELLOW + "SIM ejected from the phone.");
            return;
        }

        this.plugin.getPhoneService().clearUnread(deviceId);
        this.plugin.getPhoneItemService().syncPlayerInventory(player);
        player.openInventory(this.plugin.getPhoneMenuService().createMainMenu(player, deviceId));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8F, 1.0F);
    }

    @EventHandler
    public void onPhoneMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof PhoneMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        PhoneMenuHolder holder = (PhoneMenuHolder) event.getInventory().getHolder();
        String targetNumber = holder.getNumberBySlot(event.getRawSlot());
        if (targetNumber == null || targetNumber.isEmpty()) {
            return;
        }

        player.closeInventory();
        this.plugin.getPhoneMenuService().suggestSmsCommand(player, targetNumber);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack left = event.getInventory().getItem(0);
        ItemStack right = event.getInventory().getItem(1);
        if (this.plugin.getPhoneItemService().isProtectedCustomItem(left) || this.plugin.getPhoneItemService().isProtectedCustomItem(right)) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (this.plugin.getPhoneItemService().isProtectedCustomItem(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
