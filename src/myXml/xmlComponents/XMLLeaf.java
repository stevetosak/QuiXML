package myXml.xmlComponents;

import myXml.util.DeepCopy;

public class XMLLeaf extends XMLComponent {
    private final String textValue;

    public XMLLeaf(String tag, String textValue) {
        super(tag);
        this.textValue = textValue;
    }

    @Override
    public String generateXml(int depth) {
        StringBuilder sb = new StringBuilder();
        //open tag
        sb.append("\t".repeat(depth)).append("<").append(tag);
        attributes.forEach(atb -> sb.append(atb.toString()));
        sb.append(">").append("\n");

        //val
        sb.append("\t".repeat(depth)).append(textValue).append("\n");
        children.forEach(child -> sb.append(child.generateXml(depth + 1)));
        //closed tag
        sb.append("\t".repeat(depth)).append("</").append(tag).append(">").append("\n");

        return sb.toString();
    }

    @Override
    public XMLComponent deepCopy() {
        XMLLeaf copy = new XMLLeaf(tag, textValue);
        copy.addAllAttributes(attributes);
        for (XMLComponent child : children) {
            copy.addChild(child.deepCopy());
        }
        return copy;
    }

    @Override
    public String toString() {
        return generateXml(0);
    }


}

