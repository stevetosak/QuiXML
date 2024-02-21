package myXml.util;

import myXml.xmlComponents.XMLComponent;
import myXml.xmlComponents.XMLContainer;

import java.util.Objects;

public final class DocumentStateWrapper {
    private final XMLContainer root;
    private final XMLComponent element;
    private final boolean initialized;

    public DocumentStateWrapper(XMLContainer root, XMLComponent element, boolean initialized) {
        this.root = root;
        this.element = element;
        this.initialized = initialized;
    }

    public XMLContainer root() {
        return root;
    }

    public XMLComponent element() {
        return element;
    }

    public boolean initialized() {
        return initialized;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DocumentStateWrapper) obj;
        return Objects.equals(this.root, that.root) &&
                Objects.equals(this.element, that.element) &&
                this.initialized == that.initialized;
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, element, initialized);
    }

    @Override
    public String toString() {
        return "DocumentStateWrapper[" +
                "root=" + root + ", " +
                "element=" + element + ", " +
                "initialized=" + initialized + ']';
    }

}
