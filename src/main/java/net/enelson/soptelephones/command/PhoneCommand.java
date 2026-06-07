package net.enelson.soptelephones.command;

import java.util.List;
import net.enelson.soptelephones.SopTelephonesPlugin;
import net.enelson.soptelephones.model.ContactEntry;
import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.PhoneDevice;
import net.enelson.soptelephones.model.Provider;
import net.enelson.soptelephones.model.SimCard;
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
            sender.sendMessage(this.plugin.message("only-players-phone"));
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
                player.sendMessage(this.plugin.message("number-not-yours"));
                return true;
            }
            this.plugin.getPhoneService().setPrimary(player.getUniqueId(), account.getNumber());
            player.sendMessage(this.plugin.message("primary-switched", "{number}", account.getNumber()));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("contact") && args[1].equalsIgnoreCase("list")) {
            List<ContactEntry> contacts = this.plugin.getContactService().getContacts(player.getUniqueId());
            if (contacts.isEmpty()) {
                player.sendMessage(this.plugin.message("contacts-empty"));
                return true;
            }
            player.sendMessage(this.plugin.message("contacts-title"));
            for (ContactEntry contact : contacts) {
                player.sendMessage(this.plugin.message("contacts-entry", "{name}", contact.getName(), "{number}", contact.getNumber()));
            }
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("contact") && args[1].equalsIgnoreCase("remove")) {
            if (!this.plugin.getContactService().removeContact(player.getUniqueId(), args[2])) {
                player.sendMessage(this.plugin.message("contact-not-found"));
                return true;
            }
            player.sendMessage(this.plugin.message("contact-removed"));
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("contact") && args[1].equalsIgnoreCase("add")) {
            String name = args[2];
            String number = args[3];
            if (this.plugin.getPhoneService().getByNumber(number) == null) {
                player.sendMessage(this.plugin.message("unknown-number"));
                return true;
            }
            this.plugin.getContactService().setContact(player.getUniqueId(), name, number);
            player.sendMessage(this.plugin.message("contact-saved", "{name}", name, "{number}", number));
            return true;
        }

        player.sendMessage(this.plugin.message("usage.phone-root"));
        player.sendMessage(this.plugin.message("usage.phone-use"));
        player.sendMessage(this.plugin.message("usage.phone-contact-list"));
        player.sendMessage(this.plugin.message("usage.phone-contact-add"));
        player.sendMessage(this.plugin.message("usage.phone-contact-remove"));
        return true;
    }

    private void showOverview(Player player) {
        PhoneDevice device = this.plugin.getPhoneItemService().getPhoneDeviceInHand(player);
        if (device == null) {
            player.sendMessage(this.plugin.message("hold-phone"));
            return;
        }

        SimCard sim = this.plugin.getPhoneService().getInstalledSim(device);
        if (sim == null) {
            player.sendMessage(this.plugin.message("status.title"));
            player.sendMessage(this.plugin.message("status.model", "{model}", device.getModelId()));
            player.sendMessage(this.plugin.message("status.sim-missing"));
            player.sendMessage(this.plugin.message("status.sim-tip"));
            return;
        }

        Provider provider = this.plugin.getProviderService().getProvider(sim.getProviderId());
        boolean covered = this.plugin.getTowerService().isCovered(sim.getProviderId(), player.getLocation(), this.plugin.getPhoneItemService().getSignalBonus(device));

        player.sendMessage(this.plugin.message("status.title"));
        player.sendMessage(this.plugin.message("status.number", "{number}", sim.getNumber()));
        player.sendMessage(this.plugin.message("status.provider", "{provider}", provider == null ? sim.getProviderId() : provider.getDisplayName()));
        player.sendMessage(this.plugin.message("status.coverage", "{status}", covered ? this.plugin.message("state.online") : this.plugin.message("state.offline")));
        player.sendMessage(this.plugin.message("status.contacts", "{count}", String.valueOf(this.plugin.getContactService().getContacts(player.getUniqueId()).size())));
    }
}
