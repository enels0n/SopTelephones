package net.enelson.soptelephones.event;

import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.SmsMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class SmsSentEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player sender;
    private final PhoneAccount recipientAccount;
    private final SmsMessage message;
    private final double price;

    public SmsSentEvent(Player sender, PhoneAccount recipientAccount, SmsMessage message, double price) {
        this.sender = sender;
        this.recipientAccount = recipientAccount;
        this.message = message;
        this.price = price;
    }

    public Player getSender() {
        return sender;
    }

    public PhoneAccount getRecipientAccount() {
        return recipientAccount;
    }

    public SmsMessage getMessage() {
        return message;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
