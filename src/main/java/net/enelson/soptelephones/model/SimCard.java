package net.enelson.soptelephones.model;

import java.util.UUID;

public final class SimCard {
    private final String simId;
    private final String number;
    private final String providerId;
    private final UUID ownerId;

    public SimCard(String simId, String number, String providerId, UUID ownerId) {
        this.simId = simId;
        this.number = number;
        this.providerId = providerId;
        this.ownerId = ownerId;
    }

    public String getSimId() {
        return this.simId;
    }

    public String getNumber() {
        return this.number;
    }

    public String getProviderId() {
        return this.providerId;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }
}
