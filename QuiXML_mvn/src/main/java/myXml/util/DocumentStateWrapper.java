package myXml.util;

import myXml.commands.help.Command;
import myXml.components.XmlNode;
import myXml.components.ElementNode;

import java.util.LinkedList;

public final class DocumentStateWrapper {
    private String documentName;
    private XmlNode mainRoot;
    private XmlNode currentRoot;
    private XmlNode currentNode;
    private final LinkedList<Command> commandLog;

    public DocumentStateWrapper(String documentName) {
        this.documentName = documentName;
        commandLog = new LinkedList<>();
    }

    public DocumentStateWrapper(XmlNode mainRoot, XmlNode currentRoot, XmlNode currentNode, LinkedList<Command> commandLog) {
        this.mainRoot = mainRoot;
        this.currentRoot = currentRoot;
        this.currentNode = currentNode;
        this.commandLog = commandLog;
    }

    public XmlNode mainRoot() {
        return mainRoot;
    }

    public XmlNode currentRoot() {
        return currentRoot;
    }

    public XmlNode currentNode() {
        return currentNode;
    }

    public void setNode(String type, XmlNode node){
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
        this.mainRoot = new ElementNode(documentName);
        currentNode = currentRoot = mainRoot;
    }
}
