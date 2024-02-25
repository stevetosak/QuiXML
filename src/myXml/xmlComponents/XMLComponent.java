package myXml.xmlComponents;

import java.util.*;
import java.util.stream.Collectors;

public abstract class XMLComponent {
    protected String tag;
    protected Set<Attribute> attributes = new LinkedHashSet<>();
    protected List<XMLComponent> children = new ArrayList<>();
    protected XMLComponent parent = null;

    public XMLComponent(String tag) {
        this.tag = tag;
    }

    abstract String generateXml(int depth);

    public abstract XMLComponent deepCopy();

    public void addAttribute(String name, String val) {
        attributes.add(new Attribute(name, val));
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<XMLComponent> getSiblings() {
        if (parent == null) {
            return new ArrayList<>();
        }
        List<XMLComponent> siblings = parent.getChildren();
        siblings.remove(this);
        return siblings;
    }

    public String getTagNameFormatted() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag);
        attributes.forEach(atb -> sb.append(atb.toString()));
        sb.append(">");
        return sb.toString();
    }

    public void addAllAttributes(Collection<Attribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public Set<Attribute> getAttributes() {
        return attributes;
    }

    public XMLComponent getNext() {
        if (parent == null) return null;
        List<XMLComponent> siblings = parent.getChildren();
        int idx = siblings.indexOf(this);
        if (++idx > siblings.size() - 1) idx = 0;
        return siblings.get(idx);

    }

    public XMLComponent getPrev() {
        if (parent == null) return null;
        List<XMLComponent> siblings = parent.getChildren();
        int idx = siblings.indexOf(this);
        if (--idx < 0) idx = siblings.size() - 1;
        return siblings.get(idx);
    }

    public List<XMLComponent> getChildren() {
        return children;
    }

    public void setChildren(List<XMLComponent> children) {
        this.children = children;
    }

    public XMLComponent getParent() {
        if (parent == null) return this;
        return parent;
    }

    public void setParent(XMLComponent parent) {
        this.parent = parent;
    }

    public void addChild(XMLComponent node) {
        node.setParent(this);
        children.add(node);
    }

    public XMLComponent getFirstChild() {
        if (children.isEmpty()) return this;
        return children.getFirst();
    }

    public void removeChildNode(XMLComponent current) {
        children.remove(current);
    }

    public boolean removeAttributeWithName(String attribName) {
        return attributes.removeIf(atb -> atb.getName().equals(attribName));
    }

    public void removeLastAttribute() {
        if (!attributes.isEmpty()) {
            attributes.remove(attributes
                    .stream()
                    .collect(Collectors.toList())
                    .getLast()
            );
        }

    }

}

