package myXml.xmlComponents;

import java.util.ArrayList;
import java.util.List;

public class XMLContainer extends XMLComponent {
    public XMLContainer() {
        super("");
    }

    public XMLContainer(String tag) {
        super(tag);
    }

    @Override
    public String toString() {
        return generateXml(0);
    }


    @Override
    public String generateXml(int depth) {
        StringBuilder sb = new StringBuilder();
        //open tag
        sb.append("\t".repeat(depth));
        sb.append("<").append(tag);
        attributes.forEach(atb -> sb.append(atb.toString()));
        sb.append(">").append("\n");

        //val
        children.forEach(component -> sb.append(component.generateXml(depth + 1)));

        //closed tag
        sb.append("\t".repeat(depth)).append("</").append(tag).append(">").append("\n");

        return sb.toString();
    }

}
