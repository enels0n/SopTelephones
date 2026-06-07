package net.enelson.soptelephones.event;

import net.enelson.soptelephones.model.PhoneAccount;
import net.enelson.soptelephones.model.SmsMessage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class SmsQueuedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final PhoneAccount recipientAccount;
    private final SmsMessage message;

    public SmsQueuedEvent(PhoneAccount recipientAccount, SmsMessage message) {
        this.recipientAccount = recipientAccount;
        this.message = message;
    }

    public PhoneAccount getRecipientAccount() {
        return recipientAccount;
    }

    public SmsMessage getMessage() {
        return message;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
