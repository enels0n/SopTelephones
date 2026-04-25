package net.enelson.soptelephones.model;

public final class ContactEntry {
    private final String name;
    private final String number;

    public ContactEntry(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.number;
    }
}
