package myXml.util;

import myXml.commands.RawCommand;
import myXml.components.XMLComponent;

import java.util.LinkedList;

public final class DocumentStateWrapper {
    private final XMLComponent mainRoot;
    private final String currentRootTag;
    private final String currentNodeTag;
    private final LinkedList<RawCommand> commandLog;

    public DocumentStateWrapper(XMLComponent mainRoot, String currentRootTag, String currentNode, LinkedList<RawCommand> commandLog) {
        this.mainRoot = mainRoot;
        this.currentRootTag = currentRootTag;
        this.currentNodeTag = currentNode;
        this.commandLog = commandLog;
    }

    public XMLComponent mainRoot() {
        return mainRoot;
    }

    public String currentRootTag() {
        return currentRootTag;
    }

    public String currentNodeTag() {
        return currentNodeTag;
    }

    public LinkedList<RawCommand> commandLog() {
        return commandLog;
    }


}
