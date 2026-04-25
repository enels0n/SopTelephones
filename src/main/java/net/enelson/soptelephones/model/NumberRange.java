package net.enelson.soptelephones.model;

public final class NumberRange {
    private final String id;
    private final String providerId;
    private final String prefix;
    private final int from;
    private final int to;

    public NumberRange(String id, String providerId, String prefix, int from, int to) {
        this.id = id;
        this.providerId = providerId;
        this.prefix = prefix;
        this.from = from;
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public boolean contains(String number) {
        if (number == null || !number.startsWith(this.prefix + "-")) {
            return false;
        }
        String suffix = number.substring((this.prefix + "-").length());
        try {
            int value = Integer.parseInt(suffix);
            return value >= this.from && value <= this.to;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}

