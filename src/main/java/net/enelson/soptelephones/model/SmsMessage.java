package net.enelson.soptelephones.model;

public final class SmsMessage {
    private final String fromNumber;
    private final String toNumber;
    private final String content;
    private final long sentAt;

    public SmsMessage(String fromNumber, String toNumber, String content, long sentAt) {
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public String getToNumber() {
        return toNumber;
    }

    public String getContent() {
        return content;
    }

    public long getSentAt() {
        return sentAt;
    }
}

