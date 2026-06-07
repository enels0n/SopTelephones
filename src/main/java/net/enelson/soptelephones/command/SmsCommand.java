package net.enelson.soptelephones.command;

import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.ContactEntry;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.SimCard;
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
            sender.sendMessage(this.plugin.message("only-players-sms"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(this.plugin.message("usage.sms"));
            return true;
        }

        Player player = (Player) sender;
        PhoneDevice device = this.plugin.getPhoneItemService().getPhoneDeviceInHand(player);
        if (device == null) {
            player.sendMessage(this.plugin.message("hold-phone"));
            return true;
        }
        SimCard sim = this.plugin.getPhoneService().getInstalledSim(device);
        if (sim == null) {
            player.sendMessage(this.plugin.message("insert-sim-first"));
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
