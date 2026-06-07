package net.enelson.soptelephones.command;

import java.util.UUID;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.NumberRange;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.model.SimCard;
import net.enelson.soptelephones.model.Tower;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class MainCommand implements CommandExecutor {
    private final SopTelephonesPlugin plugin;

    public MainCommand(SopTelephonesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("soptelephones.admin")) {
            sender.sendMessage(this.plugin.message("no-permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadPlugin();
            sender.sendMessage(this.plugin.message("reloaded"));
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
            sender.sendMessage(this.plugin.message("provider-created", "{id}", provider.getId()));
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("provider") && args[1].equalsIgnoreCase("price")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[2]);
            if (provider == null) {
                sender.sendMessage(this.plugin.message("unknown-provider"));
                return true;
            }
            try {
                provider.setSmsPrice(Double.parseDouble(args[3]));
                this.plugin.getProviderService().save();
                sender.sendMessage(this.plugin.message("provider-price-updated"));
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.plugin.message("numeric-price"));
            }
            return true;
        }

        if (args.length == 6 && args[0].equalsIgnoreCase("range") && args[1].equalsIgnoreCase("add")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[2]);
            if (provider == null) {
                sender.sendMessage(this.plugin.message("unknown-provider"));
                return true;
            }
            try {
                NumberRange range = this.plugin.getProviderService().addRange(args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                sender.sendMessage(this.plugin.message("range-added", "{prefix}", range.getPrefix(), "{from}", String.valueOf(range.getFrom()), "{to}", String.valueOf(range.getTo())));
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.plugin.message("numeric-range"));
            }
            return true;
        }

        if (args.length == 9 && args[0].equalsIgnoreCase("tower") && args[1].equalsIgnoreCase("add")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(this.plugin.message("unknown-provider"));
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
                sender.sendMessage(this.plugin.message("tower-added", "{id}", tower.getId()));
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.plugin.message("numeric-coordinates-radius"));
            }
            return true;
        }

        if (args.length == 10 && args[0].equalsIgnoreCase("tower") && args[1].equalsIgnoreCase("add")) {
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(this.plugin.message("unknown-provider"));
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
                sender.sendMessage(this.plugin.message("tower-added", "{id}", tower.getId()));
            } catch (NumberFormatException exception) {
                sender.sendMessage(this.plugin.message("numeric-coordinates-radii"));
            }
            return true;
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("phone") && args[1].equalsIgnoreCase("assign")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            UUID ownerId = target.getUniqueId();
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(this.plugin.message("unknown-provider"));
                return true;
            }
            if (this.plugin.getProviderService().findRange(provider.getId(), args[4]) == null) {
                sender.sendMessage(this.plugin.message("number-outside-provider-range"));
                return true;
            }
            if (this.plugin.getPhoneService().getByNumber(args[4]) != null) {
                sender.sendMessage(this.plugin.message("number-already-assigned"));
                return true;
            }
            PhoneAccount account = this.plugin.getPhoneService().assignPhone(ownerId, provider.getId(), args[4]);
            sender.sendMessage(this.plugin.message("assigned-number", "{number}", account.getNumber(), "{player}", String.valueOf(target.getName())));
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("phone") && args[1].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(this.plugin.message("target-online-required"));
                return true;
            }
            if (this.plugin.getPhoneItemService().getPhoneModel(args[3]) == null) {
                sender.sendMessage(this.plugin.message("unknown-phone-model"));
                return true;
            }
            PhoneDevice device = this.plugin.getPhoneService().createDevice(args[3]);
            ItemStack item = this.plugin.getPhoneItemService().createPhoneItem(device);
            target.getInventory().addItem(item);
            sender.sendMessage(this.plugin.message("phone-given-admin", "{id}", device.getDeviceId()));
            target.sendMessage(this.plugin.message("phone-received", "{model}", args[3]));
            return true;
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("sim") && args[1].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(this.plugin.message("target-online-required"));
                return true;
            }
            Provider provider = this.plugin.getProviderService().getProvider(args[3]);
            if (provider == null) {
                sender.sendMessage(this.plugin.message("unknown-provider"));
                return true;
            }
            if (this.plugin.getProviderService().findRange(provider.getId(), args[4]) == null) {
                sender.sendMessage(this.plugin.message("number-outside-provider-range"));
                return true;
            }
            if (this.plugin.getPhoneService().getByNumber(args[4]) != null) {
                sender.sendMessage(this.plugin.message("number-already-assigned"));
                return true;
            }

            this.plugin.getPhoneService().assignPhone(target.getUniqueId(), provider.getId(), args[4]);
            SimCard simCard = this.plugin.getPhoneService().createSim(target.getUniqueId(), provider.getId(), args[4]);
            target.getInventory().addItem(this.plugin.getPhoneItemService().createSimItem(simCard));
            sender.sendMessage(this.plugin.message("sim-given-admin", "{number}", simCard.getNumber()));
            target.sendMessage(this.plugin.message("sim-received", "{number}", simCard.getNumber()));
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("phone") && args[1].equalsIgnoreCase("primary")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            PhoneAccount account = this.plugin.getPhoneService().getByNumber(args[3]);
            if (account == null || !account.getOwnerId().equals(target.getUniqueId())) {
                sender.sendMessage(this.plugin.message("number-not-player"));
                return true;
            }
            this.plugin.getPhoneService().setPrimary(target.getUniqueId(), args[3]);
            sender.sendMessage(this.plugin.message("primary-updated"));
            return true;
        }

        sender.sendMessage(this.plugin.message("usage.main-reload"));
        sender.sendMessage(this.plugin.message("usage.main-provider-create"));
        sender.sendMessage(this.plugin.message("usage.main-provider-price"));
        sender.sendMessage(this.plugin.message("usage.main-range-add"));
        sender.sendMessage(this.plugin.message("usage.main-tower-add"));
        sender.sendMessage(this.plugin.message("usage.main-phone-assign"));
        sender.sendMessage(this.plugin.message("usage.main-phone-give"));
        sender.sendMessage(this.plugin.message("usage.main-phone-primary"));
        sender.sendMessage(this.plugin.message("usage.main-sim-give"));
        sender.sendMessage(this.plugin.message("usage.phone-root"));
        return true;
    }
}
