package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.components.XmlNode;
import myXml.components.ElementNode;
import myXml.components.TextNode;
import myXml.editor.XMLEditor;
import myXml.util.DocumentStateWrapper;
import myXml.util.Log;
import myXml.util.Messenger;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class NodeManager implements CommandManager {

    private final UtilityManager utilityManager = new UtilityManager();

    @CommandHandler(names = "add")
    public void addNode(DocumentStateWrapper document,String[] params) {
        if(document.currentNode() instanceof TextNode) return;
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        if (params.length == 1) addContainer(document,params);
        else addLeaf(document,params);
    }

    @CommandHandler(names = {"atb","attribute"})
    public void addAttribute(DocumentStateWrapper document, String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        document.currentNode().addAttribute(params[0], params[1]);
        Messenger.attributeAddedMsg(params[0], params[1]);
    }

    @CommandHandler(names = "leaf")
    public void addLeaf(DocumentStateWrapper document,String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
        XmlNode node = document.currentNode().addChild(new TextNode(params[0], val));
        if (document.currentNode() == document.mainRoot()) document.setNode("cr",node);
        document.setNode("cn",node);
        Messenger.leafAddedMsg(params[0], val);

    }

    @CommandHandler(names = {"cont","container"})
    public void addContainer(DocumentStateWrapper document,String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        XmlNode node = document.currentNode().addChild(new ElementNode(params[0]));
        if (document.currentNode() == document.mainRoot()) document.setNode("cr",node);
        document.setNode("cn",node);
        Messenger.containerAddedMsg(params[0]);
    }

    @CommandHandler(names = "root")
    public void addRoot(DocumentStateWrapper document,String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        ElementNode element = new ElementNode(params[0]);
        element.setParent(document.mainRoot());
        document.mainRoot().addChild(element);
        Messenger.rootAdded(params[0]);
    }

    @CommandHandler(names = "clear")
    public void clear(DocumentStateWrapper document, String[] params) {
        if (!utilityManager.documentEmpty(document)) {
            document.setNode("mr", new ElementNode(XMLEditor.documentName));
            document.setNode("cn", document.mainRoot());
            document.setNode("cr",document.mainRoot());
            Log.clearLog();
            Messenger.emptyDocumentMsg();
        }
    }
    @CommandHandler(names = {"rma","remove attribute"})
   public void removeAttrib(DocumentStateWrapper document, String[] name) {
        if (name.length == 0) {
            document.currentNode().removeLastAttribute();
        } else {
            if (!document.currentNode().removeAttributeWithName(name[0])) {
                System.out.println("No attribute with name: " + name[0] + " found");
            }
        }
    }
    @CommandHandler(names = {"rmc","remove current node"})
    public void removeCurrent(DocumentStateWrapper document, String [] params) {
        document.currentNode().getParent().removeChildNode(document.currentNode());
        document.setNode("cn",document.currentNode().getParent());
    }




}
