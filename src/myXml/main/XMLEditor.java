package myXml.main;

import myXml.util.CommandHelper;
import myXml.util.Log;
import myXml.util.XMLWrapper;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

class XMLEditor {
    private final XMLContainer mainRoot;// points to all the root nodes;
    private final Stack<XMLWrapper> previousStack = new Stack<>(); // where you were
    private XMLContainer currentNode;
    private XMLContainer currentRoot;
    public CommandHelper commandHelper = new CommandHelper();

    public XMLEditor() throws IOException {
        this.mainRoot = new XMLContainer("xmlDOCUMENT");
        this.currentNode = new XMLContainer();
    }

    public boolean init(String cmdLine) {
        String[] parts = cmdLine.split("\\s+");
        if(parts.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        String command = parts[0];
        if (command.equals("addroot")) {
            addRoot(parts[1]);
            currentNode = currentRoot = getFirstRoot();
            return true;
        }
        else return false;
    }

    public void addRoot(String tagName) {
        XMLContainer element = new XMLContainer(tagName);
        element.setParent(mainRoot);
        mainRoot.addChild(element);
    }

    public XMLContainer getFirstRoot() {
        XMLContainer result = mainRoot.getFirstChild();
        if (result.equals(mainRoot)) return null;
        return result;
    }

    public XMLContainer getNextRoot() {
        previousStack.push(new XMLWrapper(currentRoot, currentNode));
        List<XMLContainer> children = mainRoot.getChildren();
        int index = children.indexOf(currentRoot);
        if (++index > children.size() - 1) index = 0;
        return children.get(index);
    }

    //todo da ne sa vrakjat ako si vo istiot elem
    public XMLWrapper getPreviousState() {
        if (!previousStack.empty()) {
            System.out.println("Went back");
            return previousStack.pop();
        }
        throw new EmptyStackException();
    }

    public XMLContainer getCurrentNode() {
        return currentNode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("------------------------------").append("\n");
        sb.append(mainRoot.getTag()).append("\n");
        sb.append("------------------------------").append("\n");
        mainRoot.getChildren().forEach(child -> {
            sb.append(child.toString());
            sb.append("------------------------------").append("\n");
        });
        return sb.toString();
    }
    public void processCommands(String command, String[] params) {
        switch (command) {
            case "atrib" -> AddAttribute(params);
            case "leaf" -> AddLeaf(params);
            case "container" -> AddContainer(params);
            case "addroot" -> AddRoot(params);
            case "help" -> getHelp(params);
            case "nextroot" -> nextRoot();
            case "prevroot" -> prevRoot();
            case "up" -> stepOut();
            case "down" -> stepIn();
            case "next" -> nextElem();
            case "removec" -> removeCurrent();
            case "printall" -> printAll();
            case "printcrnt" -> printCurrent();
            case "printeditor" -> printEditor();
            case "showcommands" -> showCommandList();
        }
    }

    private void getHelp(String [] params) {
        if(params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        commandHelper.getCommandHelp(params[0]);
    }

    private void showCommandList() {
        commandHelper.displayAllCommands();
    }

    private void printEditor() {
        System.out.println(this);
    }

    private void printCurrent() {
        System.out.println(currentNode);
    }
    private void printAll() {
        System.out.println(currentRoot);
    }

    private void removeCurrent() {
        currentNode.getParent().remove(currentNode);
        currentNode = currentNode.getParent();
        Log.currentNodeMsg(currentNode.getTag());
    }

    private void nextElem() {
        currentNode = currentNode.getNext();
        Log.currentNodeMsg(currentNode.getTag());
    }

    private void stepIn() {
        currentNode = currentNode.getFirstChild();
        Log.currentNodeMsg(currentNode.getTag());
    }

    private void stepOut() {
        currentNode = currentNode.getParent();
        Log.currentNodeMsg(currentNode.getTag());
    }

    private void prevRoot() {
        XMLWrapper prevState = getPreviousState();
        currentRoot = prevState.getRoot();
        currentNode = prevState.getElement();
        Log.currentNodeMsg(currentNode.getTag());
    }

    private void nextRoot() {
        currentNode = currentRoot = getNextRoot();
        Log.currentNodeMsg(currentNode.getTag());
    }

    private void AddRoot(String[] params) {
        if(params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        addRoot(params[0]);
        Log.rootAdded(params[0]);
    }

    private void AddContainer(String[] params) {
        if(params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        currentNode.addChild(new XMLContainer(params[0]));
        Log.containerAddedMsg(params[0]);
    }

    private void AddAttribute(String[] params) {
        if(params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        currentNode.addAttribute(params[0], params[1]);
        Log.attributeAddedMsg(params[0],params[1]);
    }

    private void AddLeaf(String[] params) {
        if(params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
        currentNode.addValue(new XMLLeaf(params[0], val));
        Log.leafAddedMsg(params[0],val);
    }

}
