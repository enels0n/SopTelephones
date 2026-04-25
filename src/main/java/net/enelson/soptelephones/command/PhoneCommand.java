package net.enelson.soptelephones.command;

import java.util.List;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.ContactEntry;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.model.SimCard;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PhoneCommand implements CommandExecutor {
    private final SopTelephonesPlugin plugin;

    public PhoneCommand(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can manage phones.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            showOverview(player);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("use")) {
            PhoneAccount account = this.plugin.getPhoneService().getByNumber(args[1]);
            if (account == null || !account.getOwnerId().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "That phone number does not belong to you.");
                return true;
            }
            this.plugin.getPhoneService().setPrimary(player.getUniqueId(), account.getNumber());
            player.sendMessage(ChatColor.GREEN + "Primary phone switched to " + account.getNumber() + ".");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("contact") && args[1].equalsIgnoreCase("list")) {
            List<ContactEntry> contacts = this.plugin.getContactService().getContacts(player.getUniqueId());
            if (contacts.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You do not have saved contacts.");
                return true;
            }
            player.sendMessage(ChatColor.AQUA + "Contacts:");
            for (ContactEntry contact : contacts) {
                player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + contact.getName() + ChatColor.DARK_GRAY + " -> " + ChatColor.AQUA + contact.getNumber());
            }
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("contact") && args[1].equalsIgnoreCase("remove")) {
            if (!this.plugin.getContactService().removeContact(player.getUniqueId(), args[2])) {
                player.sendMessage(ChatColor.RED + "Contact not found.");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Contact removed.");
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("contact") && args[1].equalsIgnoreCase("add")) {
            String name = args[2];
            String number = args[3];
            if (this.plugin.getPhoneService().getByNumber(number) == null) {
                player.sendMessage(ChatColor.RED + "Unknown phone number.");
                return true;
            }
            this.plugin.getContactService().setContact(player.getUniqueId(), name, number);
            player.sendMessage(ChatColor.GREEN + "Contact saved: " + name + " -> " + number);
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "/phone");
        player.sendMessage(ChatColor.YELLOW + "/phone use <number>");
        player.sendMessage(ChatColor.YELLOW + "/phone contact list");
        player.sendMessage(ChatColor.YELLOW + "/phone contact add <name> <number>");
        player.sendMessage(ChatColor.YELLOW + "/phone contact remove <name>");
        return true;
    }

    private void showOverview(Player player) {
        PhoneDevice device = this.plugin.getPhoneItemService().getPhoneDeviceInHand(player);
        if (device == null) {
            player.sendMessage(ChatColor.YELLOW + "Hold a phone in your main hand.");
            return;
        }

        SimCard sim = this.plugin.getPhoneService().getInstalledSim(device);
        if (sim == null) {
            player.sendMessage(ChatColor.AQUA + "Phone status");
            player.sendMessage(ChatColor.GRAY + "Model: " + ChatColor.WHITE + device.getModelId());
            player.sendMessage(ChatColor.GRAY + "SIM: " + ChatColor.RED + "not installed");
            player.sendMessage(ChatColor.GRAY + "Tip: " + ChatColor.WHITE + "sneak-right-click with a SIM in offhand");
            return;
        }

        Provider provider = this.plugin.getProviderService().getProvider(sim.getProviderId());
        boolean covered = this.plugin.getTowerService().isCovered(sim.getProviderId(), player.getLocation(), this.plugin.getPhoneItemService().getSignalBonus(device));

        player.sendMessage(ChatColor.AQUA + "Phone status");
        player.sendMessage(ChatColor.GRAY + "Number: " + ChatColor.WHITE + sim.getNumber());
        player.sendMessage(ChatColor.GRAY + "Provider: " + ChatColor.WHITE + (provider == null ? sim.getProviderId() : provider.getDisplayName()));
        player.sendMessage(ChatColor.GRAY + "Coverage: " + (covered ? ChatColor.GREEN + "online" : ChatColor.RED + "offline"));
        player.sendMessage(ChatColor.GRAY + "Contacts: " + ChatColor.WHITE + this.plugin.getContactService().getContacts(player.getUniqueId()).size());
    }
}
