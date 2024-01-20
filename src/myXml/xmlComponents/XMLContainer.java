package myXml.xmlComponents;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class XMLContainer implements XMLComponent {
    private final String tag;
    private final Set<Attribute> attributes = new LinkedHashSet<>();
    private XMLContainer parent;
    private final List<XMLContainer> children = new ArrayList<>();
    private final List<XMLLeaf> elements = new ArrayList<>();

    public XMLContainer(){
        this.tag = "";
        parent = null;
    }
    public XMLContainer(String tag) {
        this.tag = tag;
        parent = null;
    }

    public String getTag() {
        return tag;
    }

    public void addValue(XMLLeaf value) {
        elements.add(value);
    }

    @Override
    public void addAttribute(String name, String val) {
        attributes.add(new Attribute(name, val));
    }

    @Override
    public String toString() {
        return xmlString(0);
    }

    public List<XMLContainer> getSiblings() {
        if (parent == null) {
            return new ArrayList<>();
        }
        List<XMLContainer> siblings = parent.getChildren();
        siblings.remove(this);
        return siblings;
    }

    public XMLContainer getNext() {
        if (parent == null) return null;
        List<XMLContainer> siblings = parent.getChildren();
        int idx = siblings.indexOf(this);
        if (++idx > siblings.size() - 1) idx = 0;
        return siblings.get(idx);

    }

    public List<XMLContainer> getChildren() {
        return children;
    }

    public XMLContainer getParent() {
        if (parent == null) return this;
        return parent;
    }

    public void setParent(XMLContainer parent) {
        this.parent = parent;
    }

    public List<XMLLeaf> getElements() {
        return elements;
    }

    public void addChild(XMLContainer xmlContainer) {
        xmlContainer.parent = this;
        children.add(xmlContainer);
    }

    public XMLContainer getFirstChild() {
        if (children.isEmpty()) return this;
        return children.getFirst();
    }

    public void remove(XMLContainer current) {
        children.remove(current);
    }

    @Override
    public String xmlString(int depth) {
        StringBuilder sb = new StringBuilder();
        //open tag
        sb.append("\t".repeat(depth));
        sb.append("<").append(tag);
        attributes.forEach(atb -> sb.append(atb.toString()));
        sb.append(">").append("\n");

        elements.forEach(elem -> sb.append(elem.xmlString(depth + 1)));

        //val
        children.forEach(component -> sb.append(component.xmlString(depth + 1)));

        //closed tag
        sb.append("\t".repeat(depth)).append("</").append(tag).append(">").append("\n");

        return sb.toString();
    }
}
