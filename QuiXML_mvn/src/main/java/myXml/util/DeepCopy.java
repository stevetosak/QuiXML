package myXml.util;

import myXml.components.XMLComponent;
import myXml.components.XMLContainer;

public interface DeepCopy {
    static XMLComponent replicate(XMLComponent original) {
        XMLContainer copy = new XMLContainer(original.getTag());
        copy.addAllAttributes(original.getAttributes());
        for (XMLComponent child : original.getChildren()) {
            copy.addChild(replicate(child));
        }
        return copy;
    }
}
