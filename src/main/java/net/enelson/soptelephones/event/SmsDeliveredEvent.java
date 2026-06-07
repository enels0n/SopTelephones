package net.enelson.soptelephones.event;

import net.enelson.soptelephones.model.SmsMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class SmsDeliveredEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player recipient;
    private final SmsMessage message;
    private final boolean queued;

    public SmsDeliveredEvent(Player recipient, SmsMessage message, boolean queued) {
        this.recipient = recipient;
        this.message = message;
        this.queued = queued;
    }

    public Player getRecipient() {
        return recipient;
    }

    public SmsMessage getMessage() {
        return message;
    }

    public boolean isQueued() {
        return queued;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
