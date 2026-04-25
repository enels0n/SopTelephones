package net.enelson.soptelephones.command;

import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.ContactEntry;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.SimCard;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SmsCommand implements CommandExecutor {
    private final SopTelephonesPlugin plugin;

    public SmsCommand(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can send SMS.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /sms <number> <message>");
            return true;
        }

        Player player = (Player) sender;
        PhoneDevice device = this.plugin.getPhoneItemService().getPhoneDeviceInHand(player);
        if (device == null) {
            player.sendMessage(ChatColor.RED + "Hold a phone in your main hand.");
            return true;
        }
        SimCard sim = this.plugin.getPhoneService().getInstalledSim(device);
        if (sim == null) {
            player.sendMessage(ChatColor.RED + "Insert a SIM into the phone first.");
            return true;
        }

        String number = args[0];
        ContactEntry contact = this.plugin.getContactService().getContact(player.getUniqueId(), number);
        if (contact != null) {
            number = contact.getNumber();
        }
        StringBuilder message = new StringBuilder();
        for (int index = 1; index < args.length; index++) {
            if (message.length() > 0) {
                message.append(' ');
            }
            message.append(args[index]);
        }

        String error = this.plugin.getSmsService().send(player, device, number, message.toString());
        if (error != null) {
            sender.sendMessage(error);
        }
        return true;
    }
}
