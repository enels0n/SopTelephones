package net.enelson.soptelephones.service;

import java.util.List;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.ContactEntry;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.SimCard;
import net.enelson.soptelephones.ui.PhoneMenuHolder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PhoneMenuService {
    private final SopTelephonesPlugin plugin;

    public PhoneMenuService(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    public Inventory createMainMenu(Player player, String deviceId) {
        PhoneDevice device = this.plugin.getPhoneService().getDevice(deviceId);
        SimCard sim = device == null ? null : this.plugin.getPhoneService().getInstalledSim(device);
        PhoneMenuHolder holder = new PhoneMenuHolder(deviceId);
        Inventory inventory = Bukkit.createInventory(holder, 27, ChatColor.DARK_AQUA + "Telephone");

        inventory.setItem(4, createStatusItem(deviceId));
        if (sim == null) {
            inventory.setItem(13, createInfoItem(Material.BARRIER, ChatColor.RED + "No SIM installed", ChatColor.GRAY + "Sneak-right-click with a SIM in offhand."));
            return inventory;
        }

        int slot = 9;
        List<ContactEntry> contacts = this.plugin.getContactService().getContacts(player.getUniqueId());
        for (ContactEntry contact : contacts) {
            if (slot >= 18) {
                break;
            }
            holder.bindNumber(slot, contact.getNumber());
            inventory.setItem(slot, createTargetItem(
                Material.NAME_TAG,
                ChatColor.AQUA + contact.getName(),
                ChatColor.GRAY + contact.getNumber(),
                ChatColor.YELLOW + "Click to write SMS"
            ));
            slot++;
        }

        slot = 18;
        List<String> recent = this.plugin.getMessageHistoryService().getRecentConversationTargets(sim.getNumber(), 9);
        for (String target : recent) {
            if (slot >= 27) {
                break;
            }
            holder.bindNumber(slot, target);
            inventory.setItem(slot, createTargetItem(
                Material.PAPER,
                ChatColor.WHITE + target,
                ChatColor.GRAY + "Recent conversation",
                ChatColor.YELLOW + "Click to write SMS"
            ));
            slot++;
        }

        return inventory;
    }

    public void suggestSmsCommand(Player player, String targetNumber) {
        TextComponent text = new TextComponent(ChatColor.GOLD + "Open SMS input");
        text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sms " + targetNumber + " "));
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Insert /sms " + targetNumber + " into chat").create()));
        player.sendMessage(ChatColor.AQUA + "Write to " + ChatColor.WHITE + targetNumber + ChatColor.AQUA + ":");
        player.spigot().sendMessage(text);
    }

    private ItemStack createStatusItem(String deviceId) {
        PhoneDevice device = this.plugin.getPhoneService().getDevice(deviceId);
        SimCard sim = device == null ? null : this.plugin.getPhoneService().getInstalledSim(device);
        String line = sim == null ? ChatColor.RED + "No number" : ChatColor.WHITE + sim.getNumber();
        return createInfoItem(Material.BRICK, ChatColor.AQUA + "Current phone", ChatColor.GRAY + "Line: " + line);
    }

    private ItemStack createInfoItem(Material material, String title, String loreLine) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(java.util.Collections.singletonList(loreLine));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTargetItem(Material material, String title, String firstLine, String secondLine) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(java.util.Arrays.asList(firstLine, secondLine));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}
