package net.enelson.soptelephones;

import net.enelson.soptelephones.command.MainCommand;
import net.enelson.soptelephones.command.PhoneCommand;
import net.enelson.soptelephones.command.SmsCommand;
import net.enelson.soptelephones.listener.PhoneItemListener;
import net.enelson.soptelephones.storage.StorageManager;
import net.enelson.soptelephones.service.ContactService;
import net.enelson.soptelephones.service.EconomyService;
import net.enelson.soptelephones.service.MessageHistoryService;
import net.enelson.soptelephones.service.PhoneService;
import net.enelson.soptelephones.service.PhoneItemService;
import net.enelson.soptelephones.service.PhoneMenuService;
import net.enelson.soptelephones.service.ProviderService;
import net.enelson.soptelephones.service.SmsService;
import net.enelson.soptelephones.service.TowerService;
import org.bukkit.plugin.java.JavaPlugin;

public final class SopTelephonesPlugin extends JavaPlugin {
    private static SopTelephonesPlugin instance;

    private StorageManager storageManager;
    private ProviderService providerService;
    private PhoneService phoneService;
    private PhoneItemService phoneItemService;
    private PhoneMenuService phoneMenuService;
    private ContactService contactService;
    private MessageHistoryService messageHistoryService;
    private TowerService towerService;
    private EconomyService economyService;
    private SmsService smsService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.storageManager = new StorageManager(this);
        this.providerService = new ProviderService(this.storageManager);
        this.phoneService = new PhoneService(this.storageManager);
        this.phoneItemService = new PhoneItemService(this);
        this.phoneMenuService = new PhoneMenuService(this);
        this.contactService = new ContactService(this.storageManager);
        this.messageHistoryService = new MessageHistoryService(this.storageManager);
        this.towerService = new TowerService(this);
        this.economyService = new EconomyService(this);
        this.smsService = new SmsService(this, this.providerService, this.phoneService, this.phoneItemService, this.messageHistoryService, this.towerService, this.economyService);

        MainCommand mainCommand = new MainCommand(this);
        getCommand("soptelephones").setExecutor(mainCommand);
        getCommand("sms").setExecutor(new SmsCommand(this));
        getCommand("phone").setExecutor(new PhoneCommand(this));
        getServer().getPluginManager().registerEvents(new PhoneItemListener(this), this);
    }

    public static SopTelephonesPlugin getInstance() {
        return instance;
    }

    public void reloadPlugin() {
        reloadConfig();
        this.storageManager.reload();
        this.providerService.reload();
        this.phoneService.reload();
        this.phoneItemService.reload();
        this.contactService.reload();
        this.messageHistoryService.reload();
        this.towerService.reload();
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ProviderService getProviderService() {
        return providerService;
    }

    public PhoneService getPhoneService() {
        return phoneService;
    }

    public PhoneItemService getPhoneItemService() {
        return phoneItemService;
    }

    public PhoneMenuService getPhoneMenuService() {
        return phoneMenuService;
    }

    public ContactService getContactService() {
        return contactService;
    }

    public MessageHistoryService getMessageHistoryService() {
        return messageHistoryService;
    }

    public TowerService getTowerService() {
        return towerService;
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public SmsService getSmsService() {
        return smsService;
    }
}
