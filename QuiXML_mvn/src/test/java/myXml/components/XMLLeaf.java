package myXml.components;

public class XMLLeaf extends XMLComponent {
    private final String textValue;

    public XMLLeaf(String tag, String textValue) {
        super(tag);
        this.textValue = textValue;
    }

    @Override
    public String generateXml(int depth,XMLComponent currentNode) {
        StringBuilder sb = new StringBuilder();
        //open tag
        boolean color = getTag().equals(currentNode.getTag());
        if(color) sb.append("\u001B[34m");

        sb.append("\t".repeat(depth)).append("<").append(tag);
        attributes.forEach(atb -> sb.append(atb.toString()));
        sb.append(">").append("\n");

        //val
        sb.append("\t".repeat(depth)).append(textValue).append("\n");

        if(color) sb.append("\u001B[0m");
        children.forEach(child -> sb.append(child.generateXml(depth + 1,currentNode)));
        //closed tag

        if(color) sb.append("\u001B[34m");
        sb.append("\t".repeat(depth)).append("</").append(tag).append(">").append("\n");

        if(color) sb.append("\u001B[0m");


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

}

