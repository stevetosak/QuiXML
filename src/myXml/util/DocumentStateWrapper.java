package myXml.util;

import myXml.xmlComponents.XMLComponent;

public final class DocumentStateWrapper {
    private final XMLComponent mainRoot;
    private final String currentRootTag;
    private final String currentNodeTag;

    public DocumentStateWrapper(XMLComponent mainRoot, String currentRootTag, String currentNode) {
        this.mainRoot = mainRoot;
        this.currentRootTag = currentRootTag;
        this.currentNodeTag = currentNode;
    }

    public XMLComponent mainRoot() {
        return mainRoot;
    }

    public String currentRoot() {
        return currentRootTag;
    }

    public String currentNode() {
        return currentNodeTag;
    }


}
