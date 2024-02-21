package myXml.xmlComponents;

import java.util.Objects;

public
class Attribute {
    private final String name;
    private final String val;

    public Attribute(String name, String val) {
        this.name = name;
        this.val = val;
    }

    @Override
    public String toString() {
        return " " + name + "=\"" + val + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return Objects.equals(name, attribute.name) && Objects.equals(val, attribute.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, val);
    }
}
