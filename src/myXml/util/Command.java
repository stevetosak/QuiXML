package myXml.util;

public record Command(String name, String info, boolean availableWhenUninitialized) {
    @Override
    public String toString() {
        return name + " - " + info;
    }
}
