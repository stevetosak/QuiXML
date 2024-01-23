package myXml.main;

import myXml.util.*;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class XMLEditor{
    private XMLContainer mainRoot;// points to all the root nodes;
    private final Stack<XMLWrapper> previousRootState = new Stack<>(); // where you were
    private final Stack<RootStateWrapper> previousDocumentState = new Stack<>();
    private XMLContainer currentNode;
    private XMLContainer currentRoot;
    public CommandHelper commandHelper = new CommandHelper();
    private boolean initialized;
    private final List<String> uninitPermmitedCommands = new ArrayList<>();

    public XMLEditor() throws IOException {
        this.mainRoot = new XMLContainer("xmlDOCUMENT");
        this.currentNode = new XMLContainer();
        //privremeno
        uninitPermmitedCommands.add("revert");
        uninitPermmitedCommands.add("showcommands");
        uninitPermmitedCommands.add("help");
        initialized = false;
    }

    private void init(String command, String[] params) {
        while (!initialized) {
            if (params.length < 1 || !command.equals(CommandHelper.INIT_COMMAND)) {
                Log.invalidCommandMsg();
                Log.emptyDocumentMsg();
                break;
            }

            addRoot(params[0]);
            currentNode = currentRoot = getFirstRoot();
            initialized = true;
            Log.initSuccessMsg();
            Log.currentNodeMsg(currentNode.getTag());
        }

    }

    private void addRoot(String tagName) {
        XMLContainer element = new XMLContainer(tagName);
        element.setParent(mainRoot);
        mainRoot.addChild(element);
    }

    private XMLContainer findNode(XMLContainer crnt, String targetTag) {
        Queue<XMLContainer> queue = new LinkedList<>();
        queue.add(crnt);
        while (!queue.isEmpty()) {
            XMLContainer curr = queue.poll();
            if (curr.getTag().equals(targetTag)) return curr;
            queue.addAll(curr.getChildren());
        }
        return null;
    }

    private XMLContainer getFirstRoot() {
        XMLContainer result = mainRoot.getFirstChild();
        if (result.equals(mainRoot)) return null;
        return result;
    }

    private XMLContainer getNextRoot() {
        List<XMLContainer> children = mainRoot.getChildren();
        int index = children.indexOf(currentRoot);
        if (++index > children.size() - 1) index = 0;
        return children.get(index);
    }

    //todo da ne sa vrakjat ako si vo istiot elem
    private XMLWrapper getPreviousRootState() {
        if (previousRootState.empty()) throw new EmptyStackException();
        System.out.println("Went back");
        return previousRootState.pop();

    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("------------------------------").append("\n");
        mainRoot.getChildren().forEach(child -> sb.append(child.toString()));
        sb.append("------------------------------").append("\n");
        return sb.toString();
    }

    public void printEditor() {
        System.out.println(this);
    }

    public void printCurrent() {
        System.out.println(currentNode);
    }

    public void printCurrentRoot() {
        System.out.println(currentRoot);
    }

    public void processCommands(String command, String[] params){
        CommandProcessor processor = new CommandProcessor();
        if (uninitPermmitedCommands.contains(command)) {
            processor.processCommand(command, params);
            return;
        }

        if (!initialized) {
            init(command, params);
            return;
        }

        processor.processCommand(command, params);

    }

    private class CommandProcessor {
        private void processCommand(String command, String[] params) {
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
                case "printroot" -> printCurrentRoot();
                case "printcurr" -> printCurrent();
                case "printall" -> printEditor();
                case "showcommands" -> showCommandList();
                case "current" -> changeCurrentNode(params[0]);
                case "top" -> top();
                case "clear" -> clear();
                case "revert" -> revert();
            }
        }

        private void revert() {
            if (!previousDocumentState.isEmpty()) {
                RootStateWrapper prevstate = previousDocumentState.pop();
                mainRoot = prevstate.root();
                initialized = prevstate.initialized();
                System.out.println("Reverted");
                printEditor();
            } else System.out.println("Can't revert");
        }

        private void clear() {
            previousDocumentState.push(new RootStateWrapper(mainRoot, initialized));
            initialized = false;
            mainRoot = new XMLContainer();
            Log.documentClearedMsg();
        }

        private void getHelp(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            commandHelper.getCommandHelp(params[0]);
        }

        private void showCommandList() {
            commandHelper.displayAllCommands();
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
            XMLWrapper prevState = getPreviousRootState();
            currentRoot = prevState.root();
            currentNode = prevState.element();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void nextRoot() {
            XMLWrapper currentWrap = new XMLWrapper(currentRoot, currentNode);
            if (!previousRootState.contains(currentWrap))
                previousRootState.push(currentWrap);

            currentNode = currentRoot = getNextRoot();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void AddRoot(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            addRoot(params[0]);
            Log.rootAdded(params[0]);
        }

        private void AddContainer(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addChild(new XMLContainer(params[0]));
            Log.containerAddedMsg(params[0]);
        }

        private void AddAttribute(String[] params) {
            if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addAttribute(params[0], params[1]);
            Log.attributeAddedMsg(params[0], params[1]);
        }

        private void AddLeaf(String[] params) {
            if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
            currentNode.addValue(new XMLLeaf(params[0], val));
            Log.leafAddedMsg(params[0], val);
        }

        private void changeCurrentNode(String tagName) {
            XMLContainer target = findNode(currentRoot, tagName);
            if (target == null) {
                System.out.println("Element not found");
                return;
            }
            currentNode = target;
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void top() {
            currentNode = currentRoot;
            Log.currentNodeMsg(currentNode.getTag());
        }
    }


}