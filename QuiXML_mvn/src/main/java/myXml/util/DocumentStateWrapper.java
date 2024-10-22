package myXml.util;

import myXml.commands.help.Command;
import myXml.components.XMLComponent;
import myXml.components.XMLContainer;

import java.util.LinkedList;

public final class DocumentStateWrapper {
    private String documentName;
    private XMLComponent mainRoot;
    private XMLComponent currentRoot;
    private XMLComponent currentNode;
    private final LinkedList<Command> commandLog;

    public DocumentStateWrapper(String documentName) {
        this.documentName = documentName;
        commandLog = new LinkedList<>();
    }

    public DocumentStateWrapper(XMLComponent mainRoot, XMLComponent currentRoot, XMLComponent currentNode, LinkedList<Command> commandLog) {
        this.mainRoot = mainRoot;
        this.currentRoot = currentRoot;
        this.currentNode = currentNode;
        this.commandLog = commandLog;
    }

    public XMLComponent mainRoot() {
        return mainRoot;
    }

    public XMLComponent currentRoot() {
        return currentRoot;
    }

    public XMLComponent currentNode() {
        return currentNode;
    }

    public void setNode(String type,XMLComponent node){
        switch (type) {
            case "mr" -> mainRoot = node;
            case "cr" -> currentRoot = node;
            case "cn" -> currentNode = node;
        }
    }

    public String currentRootTag() {
        return currentRoot.getTag();
    }

    public String currentNodeTag() {
        return currentNode.getTag();
    }

    public LinkedList<Command> commandLog() {
        return commandLog;
    }


    public void init() {
        this.mainRoot = new XMLContainer(documentName);
        currentNode = currentRoot = mainRoot;
    }
}
