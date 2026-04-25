package net.enelson.soptelephones.model;

public final class PhoneDevice {
    private final String deviceId;
    private final String modelId;
    private String simId;
    private boolean unread;

    public PhoneDevice(String deviceId, String modelId, String simId, boolean unread) {
        this.deviceId = deviceId;
        this.modelId = modelId;
        this.simId = simId;
        this.unread = unread;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getModelId() {
        return this.modelId;
    }

    public String getSimId() {
        return this.simId;
    }

    public void setSimId(String simId) {
        this.simId = simId;
    }

    public boolean hasSim() {
        return this.simId != null && !this.simId.isEmpty();
    }

    public boolean hasUnread() {
        return this.unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
