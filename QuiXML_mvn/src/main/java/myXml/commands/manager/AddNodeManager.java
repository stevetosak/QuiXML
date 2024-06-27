package myXml.commands.manager;

import myXml.annotations.nodeHandler;
import myXml.util.DocumentStateWrapper;
import myXml.util.Messenger;

import java.security.InvalidParameterException;

public class AddNodeManager  implements CommandManager{


    @Override
    public void exec(DocumentStateWrapper document, String[] params) {

    }
    @nodeHandler(command = "atb")
    private void addAttribute(DocumentStateWrapper document, String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        document.currentNode().addAttribute(params[0], params[1]);
        Messenger.attributeAddedMsg(params[0], params[1]);
    }

//    private void addLeaf(String[] params) {
//        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
//        String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
//        document.currentNode().addChild(new XMLLeaf(params[0], val));
//        XMLComponent node = findByTag(document.currentNode(), params[0]);
//        if (document.currentNode() == document.mainRoot()) document.setNode("cr",node);
//        document.setNode("cn",node);
//        Messenger.leafAddedMsg(params[0], val);
//    }
//
//    private void addContainer(String[] params) {
//        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
//        document.currentNode().addChild(new XMLContainer(params[0]));
//        XMLComponent node = findByTag(document.currentNode(), params[0]);
//        if (document.currentNode() == document.mainRoot()) document.setNode("cr",node);
//        document.setNode("cn",node);
//        Messenger.containerAddedMsg(params[0]);
//    }
//
//    private void addRoot(String[] params) {
//        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
//        XMLContainer element = new XMLContainer(params[0]);
//        element.setParent(document.mainRoot());
//        document.mainRoot().addChild(element);
//        Messenger.rootAdded(params[0]);
//    }
//
//    private void addNode(String[] params) {
//        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
//        if (params.length == 1) addContainer(params);
//        else addLeaf(params);
//    }
}
