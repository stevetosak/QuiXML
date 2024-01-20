package myXml.util;

import myXml.xmlComponents.XMLContainer;

public class XMLWrapper {
    private final XMLContainer root;
    private final XMLContainer element;

    public XMLWrapper(XMLContainer root, XMLContainer element) {
        this.root = root;
        this.element = element;
    }

    public XMLContainer getRoot() {
        return root;
    }

    public XMLContainer getElement() {
        return element;
    }
}
