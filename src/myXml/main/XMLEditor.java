package myXml.main;

import myXml.util.*;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class XMLEditor {
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

    private void initEditor(String command, String[] params) {
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

    public void run(String command, String[] params){
        CommandProcessor processor = new CommandProcessor();
        if (uninitPermmitedCommands.contains(command)) {
            processor.processCommand(command, params);
            return;
        }

        if (!initialized) {
            initEditor(command, params);
            return;
        }

        processor.processCommand(command, params);

    }

    private class CommandProcessor {
        private final Map<String, Consumer<String[]>> commandMap;

        public CommandProcessor() {
            commandMap = new HashMap<>();
            initializeCommandMap();
        }

        private void processCommand(String command, String[] params) {
            Consumer<String[]> commandHandler = commandMap.get(command);
            if (commandHandler != null) commandHandler.accept(params);
            else Log.invalidCommandMsg();
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

        private void changeCurrentNode(String[] tagName) {
            XMLContainer target = findNode(currentRoot, tagName[0]);
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

        private void initializeCommandMap() {
            commandMap.put("atrib", this::AddAttribute);
            commandMap.put("leaf", this::AddLeaf);
            commandMap.put("container", this::AddContainer);
            commandMap.put("addroot", this::AddRoot);
            commandMap.put("help", this::getHelp);
            commandMap.put("nextroot", (params) -> nextRoot());
            commandMap.put("prevroot", (params) -> prevRoot());
            commandMap.put("up", (params) -> stepOut());
            commandMap.put("down", (params) -> stepIn());
            commandMap.put("next", (params) -> nextElem());
            commandMap.put("removec", (params) -> removeCurrent());
            commandMap.put("printroot", (params) -> printCurrentRoot());
            commandMap.put("printcurr", (params) -> printCurrent());
            commandMap.put("printall", (params) -> printEditor());
            commandMap.put("showcommands", (params) -> showCommandList());
            commandMap.put("current", this::changeCurrentNode);
            commandMap.put("top", (params) -> top());
            commandMap.put("clear", (params) -> clear());
            commandMap.put("revert", (params) -> revert());
        }
    }

}