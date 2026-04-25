package net.enelson.soptelephones.command;

import java.util.UUID;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.NumberRange;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.model.Tower;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class MainCommand implements CommandExecutor {
    private final SopTelephonesPlugin plugin;

    public MainCommand(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("soptelephones.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadPlugin();
            sender.sendMessage(ChatColor.GREEN + "SopTelephones reloaded.");
            return true;
        }

        if (args.length >= 4 && args[0].equalsIgnoreCase("provider") && args[1].equalsIgnoreCase("create")) {
            String id = args[2];
            StringBuilder displayName = new StringBuilder();
            for (int index = 3; index < args.length; index++) {
                if (displayName.length() > 0) {
                    displayName.append(' ');
                }
                displayName.append(args[index]);
            }
            Provider provider = this.plugin.getProviderService().createProvider(id, displayName.toString());
            sender.sendMessage(ChatColor.GREEN + "Provider created: " + provider.getId());
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("provider") && args[1].equalsIgnoreCase("price")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[2]);
            if (provider == null) {
                sender.sendMessage(ChatColor.RED + "Unknown provider.");
                return true;
            }
            try {
                provider.setSmsPrice(Double.parseDouble(args[3]));
                this.plugin.getProviderService().save();
                sender.sendMessage(ChatColor.GREEN + "Provider SMS price updated.");
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Price must be numeric.");
            }
            return true;
        }

        if (args.length == 6 && args[0].equalsIgnoreCase("range") && args[1].equalsIgnoreCase("add")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[2]);
            if (provider == null) {
                sender.sendMessage(ChatColor.RED + "Unknown provider.");
                return true;
            }
            try {
                NumberRange range = this.plugin.getProviderService().addRange(args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                sender.sendMessage(ChatColor.GREEN + "Range added: " + range.getPrefix() + "-" + range.getFrom() + ".." + range.getTo());
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Range bounds must be numbers.");
            }
            return true;
        }

        if (args.length == 9 && args[0].equalsIgnoreCase("tower") && args[1].equalsIgnoreCase("add")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(ChatColor.RED + "Unknown provider.");
                return true;
            }
            try {
                Tower tower = this.plugin.getTowerService().addTower(
                    args[2],
                    args[3],
                    args[4],
                    Double.parseDouble(args[5]),
                    Double.parseDouble(args[6]),
                    Double.parseDouble(args[7]),
                    Double.parseDouble(args[8]),
                    Double.parseDouble(args[8])
                );
                sender.sendMessage(ChatColor.GREEN + "Tower added: " + tower.getId());
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Coordinates and radius must be numeric.");
            }
            return true;
        }

        if (args.length == 10 && args[0].equalsIgnoreCase("tower") && args[1].equalsIgnoreCase("add")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(ChatColor.RED + "Unknown provider.");
                return true;
            }
            try {
                Tower tower = this.plugin.getTowerService().addTower(
                    args[2],
                    args[3],
                    args[4],
                    Double.parseDouble(args[5]),
                    Double.parseDouble(args[6]),
                    Double.parseDouble(args[7]),
                    Double.parseDouble(args[8]),
                    Double.parseDouble(args[9])
                );
                sender.sendMessage(ChatColor.GREEN + "Tower added: " + tower.getId());
            } catch (NumberFormatException exception) {
                sender.sendMessage(ChatColor.RED + "Coordinates and radii must be numeric.");
            }
            return true;
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("phone") && args[1].equalsIgnoreCase("assign")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            UUID ownerId = target.getUniqueId();
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(ChatColor.RED + "Unknown provider.");
                return true;
            }
            if (this.plugin.getProviderService().findRange(provider.getId(), args[4]) == null) {
                sender.sendMessage(ChatColor.RED + "Number is not inside provider ranges.");
                return true;
            }
            if (this.plugin.getPhoneService().getByNumber(args[4]) != null) {
                sender.sendMessage(ChatColor.RED + "That number is already assigned.");
                return true;
            }
            PhoneAccount account = this.plugin.getPhoneService().assignPhone(ownerId, provider.getId(), args[4]);
            sender.sendMessage(ChatColor.GREEN + "Assigned " + account.getNumber() + " to " + target.getName() + ".");
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("phone") && args[1].equalsIgnoreCase("primary")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            PhoneAccount account = this.plugin.getPhoneService().getByNumber(args[3]);
            if (account == null || !account.getOwnerId().equals(target.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "That number does not belong to the player.");
                return true;
            }
            this.plugin.getPhoneService().setPrimary(target.getUniqueId(), args[3]);
            sender.sendMessage(ChatColor.GREEN + "Primary number updated.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "/soptelephones reload");
        sender.sendMessage(ChatColor.YELLOW + "/soptelephones provider create <id> <displayName...>");
        sender.sendMessage(ChatColor.YELLOW + "/soptelephones provider price <id> <amount>");
        sender.sendMessage(ChatColor.YELLOW + "/soptelephones range add <providerId> <prefix> <from> <to>");
        sender.sendMessage(ChatColor.YELLOW + "/soptelephones tower add <id> <providerId> <world> <x> <y> <z> <coverageRadius> [linkRadius]");
        sender.sendMessage(ChatColor.YELLOW + "/soptelephones phone assign <player> <providerId> <number>");
        sender.sendMessage(ChatColor.YELLOW + "/soptelephones phone primary <player> <number>");
        sender.sendMessage(ChatColor.YELLOW + "/phone");
        return true;
    }
}
