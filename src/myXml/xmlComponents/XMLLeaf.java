package myXml.xmlComponents;

import java.util.LinkedHashSet;
import java.util.Set;

public class XMLLeaf implements XMLComponent {
    private final String tag;
    private final Set<Attribute> attributes = new LinkedHashSet<>();
    private final String value;

    public XMLLeaf(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public void addAttribute(String name, String val) {
        attributes.add(new Attribute(name, val));
    }

    @Override
    public String xmlString(int depth) {
        StringBuilder sb = new StringBuilder();

        //open tag
        sb.append("\t".repeat(depth)).append("<").append(tag);
        attributes.forEach(atb -> sb.append(" ").append(atb.toString()));
        sb.append(">");

        //val
        sb.append(value);

        //closed tag
        sb.append("</").append(tag).append(">").append("\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return xmlString(0);
    }
}

