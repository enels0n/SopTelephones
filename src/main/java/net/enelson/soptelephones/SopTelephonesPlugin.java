package net.enelson.soptelephones;

import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.text.TextUtils;
import net.enelson.soptelephones.api.SopTelephonesApi;
import net.enelson.soptelephones.command.MainCommand;
import net.enelson.soptelephones.command.PhoneCommand;
import net.enelson.soptelephones.command.SmsCommand;
import net.enelson.soptelephones.listener.PlayerSessionListener;
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
    private SopTelephonesApi api;
    private TextUtils textUtils;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.textUtils = SopLib.getInstance() != null ? SopLib.getInstance().getTextUtils() : new TextUtils();

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
        this.api = new SopTelephonesApi(this);

        MainCommand mainCommand = new MainCommand(this);
        getCommand("soptelephones").setExecutor(mainCommand);
        getCommand("sms").setExecutor(new SmsCommand(this));
        getCommand("phone").setExecutor(new PhoneCommand(this));
        getServer().getPluginManager().registerEvents(new PhoneItemListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);
    }

    public static SopTelephonesPlugin getInstance() {
        return instance;
    }

    public void reloadPlugin() {
        reloadConfig();
        this.textUtils = SopLib.getInstance() != null ? SopLib.getInstance().getTextUtils() : new TextUtils();
        this.storageManager.reload();
        this.providerService.reload();
        this.phoneService.reload();
        this.phoneItemService.reload();
        this.contactService.reload();
        this.messageHistoryService.reload();
        this.towerService.reload();
        this.economyService.reload();
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

    public SopTelephonesApi getApi() {
        return api;
    }

    public String message(String path, String... replacements) {
        String value = getConfig().getString("messages." + path, "&cMissing message: " + path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(replacements[i], replacements[i + 1]);
        }
        return this.textUtils.color(value);
    }
}
