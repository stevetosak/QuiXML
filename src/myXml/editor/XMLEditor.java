package myXml.editor;

import myXml.util.*;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XMLEditor {

    //region Data and Constructor
    private final Stack<XMLWrapper> previousRootState = new Stack<>(); // where you were
    private final Stack<RootStateWrapper> previousDocumentState = new Stack<>();
    private final List<String> uninitPermmitedCommands = new ArrayList<>();
    public CommandHelper commandHelper = new CommandHelper();
    private XMLContainer mainRoot;// points to all the root nodes;
    private XMLContainer currentNode;
    private XMLContainer currentRoot;
    private boolean initialized;

    public XMLEditor() throws IOException {
        this.mainRoot = new XMLContainer("xmlDOCUMENT");
        this.currentNode = new XMLContainer();
        //privremeno
        uninitPermmitedCommands.add("revert");
        uninitPermmitedCommands.add("showcommands");
        uninitPermmitedCommands.add("help");
        uninitPermmitedCommands.add("template");
        //
        initialized = false;
    }
    //endregion

    //region Initialization and Helper Methods

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

    private XMLContainer findNode(XMLContainer crnt, String targetTag) {
        if (crnt.getTag().equals(targetTag)) return crnt;
        Queue<XMLContainer> queue = new LinkedList<>();
        queue.add(crnt);
        while (!queue.isEmpty()) {
            XMLContainer curr = queue.poll();
            if (curr.getTag().equals(targetTag)) return curr;
            queue.addAll(curr.getChildren());
        }
        return null;
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

    private XMLContainer getFirstRoot() {
        XMLContainer result = mainRoot.getFirstChild();
        if (result.equals(mainRoot)) return null;
        return result;
    }

    public void run(String command, String[] params) {
        CommandProcessor processor = new CommandProcessor();
        if (uninitPermmitedCommands.contains(command)) {
            processor.processCommand(command, params, false);
            return;
        }

        if (!initialized) {
            initEditor(command, params);
            return;
        }

        processor.processCommand(command, params, true);

    }

    private void addRoot(String tagName) {
        XMLContainer element = new XMLContainer(tagName);
        element.setParent(mainRoot);
        mainRoot.addChild(element);
    }
    //endregion

    //region Printing and ToString
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("------------------------------").append("\n");
        mainRoot.getChildren().forEach(child -> sb.append(child.toString()));
        sb.append("------------------------------");
        return sb.toString();
    }

    public void printEditor() {
        System.out.println(this);
        Log.currentNodeMsg(currentNode.getTag());
    }

    public void printCurrent() {
        System.out.println(currentNode);
    }

    public void printCurrentRoot() {
        System.out.println(currentRoot);
    }

    //endregion

    private class CommandProcessor {

        //region Data and Constructor
        private final Map<String, Consumer<String[]>> commandMap;

        public CommandProcessor() {
            commandMap = new HashMap<>();
            initializeCommandMap();
        }
        //endregion

        //region Add Methods
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

        private void AddContainer(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addChild(new XMLContainer(params[0]));
            stepIn();
            Log.containerAddedMsg(params[0]);
        }

        private void AddRoot(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            addRoot(params[0]);
            nextRoot();
            Log.rootAdded(params[0]);
        }
        //endregion

        //region Command Processing and Help
        private void getHelp(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            commandHelper.getCommandHelp(params[0]);
        }

        //region Traversal Methods
        private void nextRoot() {
            XMLWrapper currentWrap = new XMLWrapper(currentRoot, currentNode);
            if (!previousRootState.contains(currentWrap))
                previousRootState.push(currentWrap);

            currentNode = currentRoot = getNextRoot();
        }

        private void prevRoot() {
            XMLWrapper prevState = getPreviousRootState();
            currentRoot = prevState.root();
            currentNode = prevState.element();
        }

        private void stepOut() {
            currentNode = currentNode.getParent();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void stepIn() {
            currentNode = currentNode.getFirstChild();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void nextInLevel() {
            currentNode = currentNode.getNext();
            Log.currentNodeMsg(currentNode.getTag());
        }

        //region Removal methods
        private void removeCurrent() {
            currentNode.getParent().remove(currentNode);
            currentNode = currentNode.getParent();
            Log.currentNodeMsg(currentNode.getTag());
        }

        //endregion

        private void showCommandList() {
            commandHelper.displayAllCommands();
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
        //endregion

        private void top() {
            currentNode = currentRoot;
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void clear() {
            previousDocumentState.push(new RootStateWrapper(mainRoot, initialized));
            initialized = false;
            mainRoot = new XMLContainer();
            Log.documentClearedMsg();
        }
        //endregion

        //region Initialization and Utility

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
            commandMap.put("next", (params) -> nextInLevel());
            commandMap.put("removec", (params) -> removeCurrent());
            commandMap.put("printroot", (params) -> printCurrentRoot());
            commandMap.put("printcurr", (params) -> printCurrent());
            commandMap.put("printall", (params) -> printEditor());
            commandMap.put("showcommands", (params) -> showCommandList());
            commandMap.put("current", this::changeCurrentNode);
            commandMap.put("top", (params) -> top());
            commandMap.put("clear", (params) -> clear());
            commandMap.put("revert", (params) -> revert());
            commandMap.put("swap", this::swap);
            commandMap.put("template", this::loadTemplate);
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

        private void swap(String[] tags) {
            if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            XMLContainer node1 = findNode(mainRoot, tags[0]);
            XMLContainer node2 = findNode(mainRoot, tags[1]);

            if (node1 == null || node2 == null) return;

            int id1 = node1.getParent().getChildren().indexOf(node1);
            int id2 = node2.getParent().getChildren().indexOf(node2);

            node1.getParent().getChildren().remove(id1);
            node1.getParent().getChildren().add(id1, node2);
            node2.getParent().getChildren().remove(id2);
            node2.getParent().getChildren().add(id2, node1);
        }

        //privremeno
        // todo da sa sredit za pojke templates
        private void loadTemplate(String[] name) {
            clear();
            if (name[0].equals("html")) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader
                            (new FileInputStream("C:\\Users\\stefa\\IdeaProjects\\XMLEditor_v1\\templates\\htmlTemplate")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                String line = null;
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                while (line != null && !line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    String command = parts[0];
                    String[] params = Arrays.copyOfRange(parts, 1, parts.length);
                    processCommand(command, params, false);
                    try {
                        line = br.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            initialized = true;
            System.out.println("Generated HTML template");
            printEditor();
        }

        private void processCommand(String command, String[] params, boolean toPrint) {
            Consumer<String[]> commandHandler = commandMap.get(command);
            if (commandHandler != null) {
                commandHandler.accept(params);
                if (toPrint) printEditor();
            } else Log.invalidCommandMsg();
        }

        //endregion


    }

}