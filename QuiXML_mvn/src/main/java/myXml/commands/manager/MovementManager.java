package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.components.XmlNode;
import myXml.util.DocumentStateWrapper;

import java.security.InvalidParameterException;

public class MovementManager implements CommandManager {
    private final UtilityManager utilityManager = new UtilityManager();

    @CommandHandler(names = "top")
    public void resetToTop(DocumentStateWrapper document, String[] params) {
        document.setNode("cn",document.mainRoot());
        document.setNode("cr",document.mainRoot());
    }

    @CommandHandler(names = {"mu","up"})
    public void stepOut(DocumentStateWrapper document, String[] params) {
        document.setNode("cn",document.currentNode().getParent());
        utilityManager.findRoot(document,document.currentNode());
    }

    @CommandHandler(names = {"md","down"})
    public void stepIn(DocumentStateWrapper document, String[] params) {
        document.setNode("cn",document.currentNode().getFirstChild());
        utilityManager.findRoot(document,document.currentNode());
    }

    @CommandHandler(names = {"mn","next"})
    public void nextInLevel(DocumentStateWrapper document, String[] params) {
        if (!utilityManager.documentEmpty(document)) {
            document.setNode("cn",document.currentNode().getNext());
            utilityManager.findRoot(document,document.currentNode());
        } else {
            System.out.println("Document is empty");
        }
    }
    @CommandHandler(names = {"mb","back","prev"})
    public void previousInLevel(DocumentStateWrapper document, String[] params) {
        if (!utilityManager.documentEmpty(document)) {
            document.setNode("cn",document.currentNode().getPrev());
            utilityManager.findRoot(document,document.currentNode());
        } else {
            System.out.println("Document is empty");
        }
    }
    @CommandHandler(names = {"nav","navigate","seek"})
    public void nav(DocumentStateWrapper document,String[] params) {
        if (params.length == 0) throw new InvalidParameterException("Invalid number of parameters");
        else if (params.length == 1) utilityManager.findFromMainRoot(document,params[0]);
        else utilityManager.findFromRoot(document,params[0], params[1]);

    }

    @CommandHandler(names = "swap")
    public void swap(DocumentStateWrapper document,String[] tags) {
        if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        XmlNode node1 = utilityManager.findByTag(document.mainRoot(), tags[0]);
        XmlNode node2 = utilityManager.findByTag(document.mainRoot(), tags[1]);

        if (node1 == null || node2 == null) return;

        node1.setTag(tags[1]);
        node2.setTag(tags[0]);
    }


}
