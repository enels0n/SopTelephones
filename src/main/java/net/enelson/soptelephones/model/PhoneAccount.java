package net.enelson.soptelephones.model;

import java.util.UUID;

public final class PhoneAccount {
    private final String number;
    private final String providerId;
    private final UUID ownerId;
    private boolean primary;

    public PhoneAccount(String number, String providerId, UUID ownerId, boolean primary) {
        this.number = number;
        this.providerId = providerId;
        this.ownerId = ownerId;
        this.primary = primary;
    }

    public String getNumber() {
        return number;
    }

    public String getProviderId() {
        return providerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}

