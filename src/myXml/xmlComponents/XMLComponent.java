package myXml.xmlComponents;

import java.util.LinkedHashSet;
import java.util.Set;

abstract class XMLComponent {
    protected String tag;
    Set<Attribute> attributes = new LinkedHashSet<>();

    public XMLComponent(String tag) {
        this.tag = tag;
    }

    public void addAttribute(String name, String val) {
        attributes.add(new Attribute(name, val));
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    abstract String xmlString(int depth);

}

