package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.commands.help.CommandHelper;
import myXml.commands.help.InvalidCommandException;
import myXml.components.XMLComponent;
import myXml.util.DocumentStateWrapper;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;

public class UtilityManager {

    public XMLComponent findByTag(XMLComponent node, String targetTag) {
        if (node.getTag().equals(targetTag)) return node;
        Queue<XMLComponent> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            XMLComponent curr = queue.poll();
            if (curr.getTag().equals(targetTag)) return curr;
            queue.addAll(curr.getChildren());
        }
        return null;
    }

    public boolean documentEmpty(DocumentStateWrapper document) {
        return document.mainRoot().getChildren().isEmpty();
    }

    public void findRoot(DocumentStateWrapper document,XMLComponent node) {
        if (node.getParent().equals(document.mainRoot()) || node.getParent() == null) {
            document.setNode("cr",node);
            return;
        }
        findRoot(document,node.getParent());
    }

    //ova vo movement

    public void findFromMainRoot(DocumentStateWrapper document ,String tagName) { //Niz cel dokument bfs
        XMLComponent target = findByTag(document.mainRoot(), tagName);
        if (target != null) {
            document.setNode("cn",target);
            findRoot(document,document.currentNode());
        } else {
            System.out.println("Element not found");
        }
    }

    public void findFromRoot(DocumentStateWrapper document,String targetNodeTagName, String rootTagName) { // od daden root node bfs
        XMLComponent targetRoot = findByTag(document.mainRoot(), rootTagName);
        if (targetRoot != null) {
            XMLComponent targetNode = findByTag(targetRoot, targetNodeTagName);
            if (targetNode != null) {
                document.setNode("cn",targetNode);
                document.setNode("cr",targetRoot);
            } else {
                System.out.println("Target node not found");
            }
        } else {
            System.out.println("Target root not found");
        }
    }
}
