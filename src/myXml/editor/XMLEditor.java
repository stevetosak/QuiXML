package myXml.editor;

import myXml.main.XMLMain;
import myXml.util.*;
import myXml.xmlComponents.XMLComponent;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


//da nemat previous root state se da sa push vo eden stack

public class XMLEditor {


    //region Data and Constructor
    public static String documentName = "XMLDocument";
    private final int DOCUMENT_STATE_CAPACITY = 30;
    private final Deque<DocumentStateWrapper> documentState;
    private XMLContainer mainRoot;// points to all the root nodes;
    private XMLComponent currentNode;
    private XMLContainer currentRoot;
    private boolean initialized;
    private boolean toPrint = false;

    public XMLEditor() throws IOException {
        documentState = new ArrayDeque<>(DOCUMENT_STATE_CAPACITY);
        this.mainRoot = new XMLContainer(documentName);
        this.currentNode = new XMLContainer();
        CommandHelper.init("commands/commandList");
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

            addRootUtil(params[0]);
            resetToTop();
            initialized = true;
            toPrint = false;
            Log.logCommand(command, params);
            Log.initSuccessMsg();
            Log.currentNodeMsg(currentNode.getTag());
        }
    }

    private void resetToTop() {
        currentNode = currentRoot = getFirstRoot();
    }

    private XMLComponent findNode(XMLContainer crnt, String targetTag) {
        if (crnt.getTag().equals(targetTag)) return crnt;
        Queue<XMLComponent> queue = new LinkedList<>();
        queue.add(crnt);
        while (!queue.isEmpty()) {
            XMLComponent curr = queue.poll();
            if (curr.getTag().equals(targetTag)) return curr;
            queue.addAll(curr.getChildren());
        }
        return null;
    }

    private XMLContainer getNextRoot() {
        List<XMLComponent> children = mainRoot.getChildren();
        int index = children.indexOf(currentRoot);
        if (++index > children.size() - 1) index = 0;
        return (XMLContainer) children.get(index);
    }

    //todo da ne sa vrakjat ako si vo istiot elem
    private DocumentStateWrapper getPreviousRootState() {
        if (documentState.isEmpty()) throw new EmptyStackException();
        System.out.println("Went back");
        return documentState.pop();

    }

    private XMLContainer getFirstRoot() {
        XMLContainer result = (XMLContainer) mainRoot.getFirstChild();
        if (result.equals(mainRoot)) return null;
        return result;
    }

    public void run(String command, String[] params) {
        CommandProcessor processor = new CommandProcessor();
        if (CommandHelper.getAvailableCommands().contains(command)) {
            processor.processCommand(command, params);
            return;
        }

        if (!initialized) {
            initEditor(command, params);
            return;
        }

        processor.processCommand(command, params);

    }

    private void addRootUtil(String tagName) {
        XMLContainer element = new XMLContainer(tagName);
        element.setParent(mainRoot);
        mainRoot.addChild(element);
    }
    //endregion

    //region Printing and ToString
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        mainRoot.getChildren().forEach(child -> sb.append(child.toString()));
        return sb.toString();
    }

    public void printEditor() {
        System.out.println("------------------------------");
        System.out.print(this);
        System.out.println("------------------------------");
        Log.currentNodeMsg(currentNode.getTag());
    }

    public void printCurrent() {
        System.out.println(currentNode.toString());
    }

    public void printCurrentRoot() {
        System.out.println(currentRoot.getTag());
    }

    private void writeToFile() {
        File output = new File("C:\\Users\\stefa\\OneDrive\\Desktop\\xmlOutput.txt");
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));
            bufferedWriter.write(this.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    private class CommandProcessor {

        //region Data and Constructor
        private static final Map<String, Consumer<String[]>> commandMap = new HashMap<>();

        public CommandProcessor() {
            initializeCommandMap();
        }
        //endregion

        //region Add Methods
        private void addAttribute(String[] params) {
            if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addAttribute(params[0], params[1]);
            Log.attributeAddedMsg(params[0], params[1]);
        }

        private void addLeaf(String[] params) {
            if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
            currentNode.addChild(new XMLLeaf(params[0], val));
            Log.leafAddedMsg(params[0], val);
        }

        private void addContainer(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addChild(new XMLContainer(params[0]));
            stepIn();
            Log.containerAddedMsg(params[0]);
        }

        private void addRoot(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            addRootUtil(params[0]);
            Log.rootAdded(params[0]);
        }
        //endregion

        //region Command Processing and Help

        private void processCommand(String command, String[] params) {
            Consumer<String[]> commandHandler = commandMap.get(command);
            if (commandHandler != null) {
                Log.logCommand(command, params);
                commandHandler.accept(params);
                if (toPrint) printEditor();
            } else Log.invalidCommandMsg();
        }

        private void createTemplate(String[] name) {
            try {
                Log.saveTemplate(name[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        //endregion


        //region Traversal Methods

        private void nextRoot() {
            DocumentStateWrapper currentWrap = new DocumentStateWrapper(currentRoot, currentNode, initialized);
            if (documentState.size() == DOCUMENT_STATE_CAPACITY) documentState.removeLast();
            documentState.addFirst(currentWrap);
            currentNode = currentRoot = getNextRoot();
        }

        private void prevRoot() {
            DocumentStateWrapper prevState = getPreviousRootState();
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
            if (currentNode == null) {
                System.out.println("Document is empty");
                return;
            }
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void top() {
            currentNode = currentRoot;
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void changeCurrentNode(String[] tagName) {
            XMLComponent target = findNode(currentRoot, tagName[0]);
            if (target == null) {
                System.out.println("Element not found");
                return;
            }
            currentNode = target;
            Log.currentNodeMsg(currentNode.getTag());
        }


        //endregion


        //region Initialization and Removal Utility

        private void initializeCommandMap() {
            commandMap.put("atrib", this::addAttribute);
            commandMap.put("leaf", this::addLeaf);
            commandMap.put("container", this::addContainer);
            commandMap.put("root", this::addRoot);
            commandMap.put("help", CommandHelper::getCommandHelp);
            commandMap.put("next-r", (params) -> nextRoot());
            commandMap.put("prev-r", (params) -> prevRoot());
            commandMap.put("up", (params) -> stepOut());
            commandMap.put("down", (params) -> stepIn());
            commandMap.put("next", (params) -> nextInLevel());
            commandMap.put("remove-c", (params) -> removeCurrent());
            commandMap.put("print-r", (params) -> printCurrentRoot());
            commandMap.put("print-c", (params) -> printCurrent());
            commandMap.put("print-a", (params) -> printEditor());
            commandMap.put("cmd-all", (params) -> CommandHelper.displayAllCommands());
            commandMap.put("cmd-avb", (params) -> CommandHelper.displayAvailableCommands());
            commandMap.put("nav", this::changeCurrentNode);
            commandMap.put("top-c", (params) -> top());
            commandMap.put("clear", (params) -> clear());
            commandMap.put("revert", (params) -> revert());
            commandMap.put("swap", this::swap);
            commandMap.put("load-t", this::loadTemplate);
            commandMap.put("write", (params) -> writeToFile());
            commandMap.put("save-t", this::createTemplate);
            commandMap.put("delete-t", this::deleteTemplate);
            commandMap.put("show-t", (params) -> Log.showTemplates());
            commandMap.put("ptog", (params) -> togglePrint());
            commandMap.put("top-d", (params) -> resetToTop());
            commandMap.put("clear-log", (params) -> Log.clearLog());
        }

        private void revert() {
            if (!documentState.isEmpty()) {
                DocumentStateWrapper prevstate = documentState.pop();
                mainRoot = prevstate.root();
                initialized = prevstate.initialized();
                Log.revertLog();
                System.out.println("Reverted");
                printEditor();
            } else System.out.println("Can't revert");
        }

        private void swap(String[] tags) {
            if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            XMLComponent node1 = findNode(mainRoot, tags[0]);
            XMLComponent node2 = findNode(mainRoot, tags[1]);

            if (node1 == null || node2 == null) return;

            node1.setTag(tags[1]);
            node2.setTag(tags[0]);
        }

        private void loadTemplate(String[] name) {
            try {
                XMLMain.commandLoop(XMLEditor.this, new FileInputStream("templates/" + name[0] + ".txt"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void removeCurrent() {
            currentNode.getParent().removeChildNode(currentNode);
            currentNode = currentNode.getParent();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void deleteTemplate(String[] name) {
            try {
                Log.deleteTemplate(name[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void clear() {
            if (documentState.size() == DOCUMENT_STATE_CAPACITY) documentState.removeLast();
            documentState.addFirst(new DocumentStateWrapper(mainRoot, currentNode, initialized));
            initialized = false;
            mainRoot = new XMLContainer(documentName);
            currentNode = currentRoot = mainRoot;
            Log.clearLog();
            Log.documentClearedMsg();
        }

        private void togglePrint() {
            toPrint = !toPrint;
            System.out.println("Printing the document after every command is: " + toPrint);
        }

        //endregion

    }

}