package myXml.components;

public class XMLContainer extends XMLComponent {
    public XMLContainer(String tag) {
        super(tag);
    }


    @Override
    public String generateXml(int depth,XMLComponent currentNode) {
        StringBuilder sb = new StringBuilder();
        boolean color = getTag().equals(currentNode.getTag());
        if(color) sb.append("\u001B[34m");


        //open tag
        sb.append("\t".repeat(depth));
        sb.append("<").append(tag);
        attributes.forEach(atb -> sb.append(atb.toString()));
        sb.append(">").append("\n");

        if (color) sb.append("\u001B[0m");


        //val
        children.forEach(component -> sb.append(component.generateXml(depth + 1,currentNode)));
        //closed tag
        if(color) sb.append("\u001B[34m");
        sb.append("\t".repeat(depth)).append("</").append(tag).append(">").append("\n");
        if (color) sb.append("\u001B[0m");

        return sb.toString();
    }

    @Override
    public XMLComponent deepCopy() {
        XMLContainer copy = new XMLContainer(tag);
        copy.addAllAttributes(attributes);
        for (XMLComponent child : children) {
            copy.addChild(child.deepCopy());
        }
        return copy;
    }


}
