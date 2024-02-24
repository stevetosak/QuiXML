package myXml.util;

import myXml.xmlComponents.XMLComponent;
import myXml.xmlComponents.XMLContainer;

public interface DeepCopy {
    static XMLComponent replicate(XMLComponent original) {
        XMLContainer copy = new XMLContainer(original.getTag());
        for (XMLComponent child : original.getChildren()) {
            copy.addChild(replicate(child));
        }
        return copy;
    }
}
