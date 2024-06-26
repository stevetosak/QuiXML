package myXml.editor;

import myXml.commands.CommandHelper;
import myXml.main.XMLMain;
import myXml.util.DocumentStateWrapper;
import myXml.util.Log;
import myXml.util.Messenger;
import myXml.components.XMLComponent;
import myXml.components.XMLContainer;
import myXml.components.XMLLeaf;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XMLEditor {
    //region Data and Initialization
    private final static int MAX_STACK_CAPACITY = 100;
    public static String documentName = "XMLDocument";
    private final Deque<DocumentStateWrapper> undoStack;
    private final Deque<DocumentStateWrapper> redoStack;
    private final Map<String, Consumer<String[]>> commandMap = new HashMap<>();
    private XMLComponent mainRoot;// sentinel
    private XMLComponent currentNode;
    private XMLComponent currentRoot;
    private boolean toPrint = true;
    public XMLEditor() throws IOException {
        undoStack = new ArrayDeque<>(MAX_STACK_CAPACITY);
        redoStack = new ArrayDeque<>(MAX_STACK_CAPACITY);
        this.mainRoot = new XMLContainer(documentName);
        currentNode = currentRoot = mainRoot;
        CommandHelper.init("src/main/resources/command_list.ssv");
        loadCommands();
    }

    public void processCommands(String commandName, String[] params, boolean logStateAndPrint) {
        Consumer<String[]> commandHandler = commandMap.get(commandName);
        if (commandHandler != null) {
            Log.logCommand(commandName, params); // zaradi clear
            if (logStateAndPrint) {
                logDocumentState(commandName, undoStack, "undo");
            }
            commandHandler.accept(params);
            if (toPrint) printEditor();
        } else Messenger.invalidCommandMsg();
    }

    private void loadCommands() {
        commandMap.put("add", this::addNode);
        commandMap.put("atb", this::addAttribute);
        commandMap.put("leaf", this::addLeaf);
        commandMap.put("cont", this::addContainer);
        commandMap.put("root", this::addRoot);
        commandMap.put("help", CommandHelper::getCommandHelp);
        commandMap.put("up", (params) -> stepOut());
        commandMap.put("down", (params) -> stepIn());
        commandMap.put("next", (params) -> nextInLevel());
        commandMap.put("rmc", (params) -> removeCurrent());
        commandMap.put("pr", (params) -> printCurrentRoot());
        commandMap.put("pc", (params) -> printCurrent());
        commandMap.put("pa", (params) -> printEditor());
        commandMap.put("cmd", (params) -> CommandHelper.displayAllCommands());
        commandMap.put("nav", this::nav);
        commandMap.put("tpc", (params) -> top());
        commandMap.put("clear", (params) -> clear());
        commandMap.put("swap", this::swap);
        commandMap.put("lt", this::loadTemplate);
        commandMap.put("write", (params) -> writeToFile());
        commandMap.put("save", this::createTemplate);
        commandMap.put("rmt", this::deleteTemplate);
        commandMap.put("st", (params) -> Log.showTemplates());
        commandMap.put("pt", (params) -> togglePrint());
        commandMap.put("tpd", (params) -> resetToTop());
        commandMap.put("cl", (params) -> Log.clearLog());
        commandMap.put("rl", (params) -> Log.revertLog());
        commandMap.put("sl", (params) -> Log.showLoggedCommands());
        commandMap.put("back", (params) -> previousInLevel());
        commandMap.put("rma", this::removeAttrib);
        commandMap.put("undo", (params) -> undo());
        commandMap.put("redo", (params) -> redo());
        commandMap.put("clog", (params) -> clearLog());
    }
    //endregion

    //region Add Methods
    private void addAttribute(String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        currentNode.addAttribute(params[0], params[1]);
        Messenger.attributeAddedMsg(params[0], params[1]);
    }

    private void addLeaf(String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
        currentNode.addChild(new XMLLeaf(params[0], val));
        XMLComponent node = findByTag(currentNode, params[0]);
        if (currentNode == mainRoot) currentRoot = node;
        currentNode = node;
        Messenger.leafAddedMsg(params[0], val);
    }

    private void addContainer(String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        currentNode.addChild(new XMLContainer(params[0]));
        XMLComponent node = findByTag(currentNode, params[0]);
        if (currentNode == mainRoot) currentRoot = node;
        currentNode = node;
        Messenger.containerAddedMsg(params[0]);
    }

    private void addRoot(String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        XMLContainer element = new XMLContainer(params[0]);
        element.setParent(mainRoot);
        mainRoot.addChild(element);
        Messenger.rootAdded(params[0]);
    }

    private void addNode(String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        if (params.length == 1) addContainer(params);
        else addLeaf(params);
    }
    //endregion

    //region Helper and Search Methods

    private XMLComponent findByTag(XMLComponent node, String targetTag) {
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

    private boolean documentEmpty() {
        return mainRoot.getChildren().isEmpty();
    }

    private void findRoot(XMLComponent node) {
        if (node.getParent().equals(mainRoot) || node.getParent() == null) {
            currentRoot = node;
            return;
        }
        findRoot(node.getParent());
    }

    private void swap(String[] tags) {
        if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        XMLComponent node1 = findByTag(mainRoot, tags[0]);
        XMLComponent node2 = findByTag(mainRoot, tags[1]);

        if (node1 == null || node2 == null) return;

        node1.setTag(tags[1]);
        node2.setTag(tags[0]);
    }

    private void findFromMainRoot(String tagName) { //Niz cel dokument bfs
        XMLComponent target = findByTag(mainRoot, tagName);
        if (target != null) {
            currentNode = target;
            findRoot(currentNode);
        } else {
            System.out.println("Element not found");
        }
    }

    private void findFromRoot(String targetNodeTagName, String rootTagName) { // od daden root node bfs
        XMLComponent targetRoot = findByTag(mainRoot, rootTagName);
        if (targetRoot != null) {
            XMLComponent targetNode = findByTag(targetRoot, targetNodeTagName);
            if (targetNode != null) {
                currentNode = targetNode;
                currentRoot = targetRoot;
            } else {
                System.out.println("Target node not found");
            }
        } else {
            System.out.println("Target root not found");
        }
    }

    //endregion

    //region Printing and ToString
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (XMLComponent child : mainRoot.getChildren()){
            sb.append(child.generateXml(0,currentNode));
        }
        return sb.toString();
    }

    public void printEditor() {
        System.out.println("------------------------------");
        System.out.print(this);
        System.out.println("------------------------------");
        Messenger.currentNodeMsg(currentNode.getTagNameFormatted(),
                (currentRoot.equals(currentNode) ? mainRoot.getTagNameFormatted() : currentRoot.getTagNameFormatted()));
    }

    public void printCurrent() {
        System.out.println(currentNode.getTagNameFormatted());
    }

    public void printCurrentRoot() {
        System.out.println("Current root is: " + currentRoot.getTag());
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

    private void togglePrint() {
        toPrint = !toPrint;
        System.out.println("Printing the document after every command is: " + toPrint);
    }

    //endregion

    //region Backtracking

    void logDocumentState(String commandName, Deque<DocumentStateWrapper> stack, String dontLog) {
        if (redoStack.isEmpty() && commandName.equals("redo") || commandName.equals(dontLog)) return;

        if (stack.size() == MAX_STACK_CAPACITY) stack.removeLast();
        DocumentStateWrapper wrapper = new DocumentStateWrapper(mainRoot.deepCopy(), currentRoot.getTag(), currentNode.getTag(), Log.getCommandLog());
        stack.push(wrapper);

    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            logDocumentState("undo", redoStack, "");
            DocumentStateWrapper prevState = undoStack.pop();
            mainRoot = prevState.mainRoot();
            currentRoot = findByTag(mainRoot, prevState.currentRootTag());
            currentNode = findByTag(mainRoot, prevState.currentNodeTag());// optimize
            Log.updateCommandLog(prevState.commandLog());
        }
    }

    private void redo() {
        if (Log.getLastCommandName().equals("undo") || Log.getLastCommandName().equals("redo")) {
            if (!redoStack.isEmpty() && !undoStack.isEmpty()) {
                DocumentStateWrapper prevState = redoStack.pop();
                mainRoot = prevState.mainRoot();
                currentRoot = findByTag(mainRoot, prevState.currentRootTag());
                currentNode = findByTag(mainRoot, prevState.currentNodeTag());
                Log.updateCommandLog(prevState.commandLog());
            }
        } else {
            redoStack.clear();
        }
    }

    private void clearLog() {
        System.out.println("Are you sure you want to delete all templates? This action can not be undone.\nType 'y' to confirm.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            char ans = (char) br.read();
            if (ans == 'y') {
                Log.clearTemplates();
                System.out.println("Deleted all templates.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    //endregion

    //region Template functionality
    private void createTemplate(String[] name) {
        try {
            Log.saveTemplate(name[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTemplate(String[] name) {
        try (FileInputStream fileIs = new FileInputStream("templates/" + name[0] + ".txt")) {
            clear();
            XMLMain.commandLoop(XMLEditor.this, fileIs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTemplate(String[] name) {
        try {
            Log.deleteTemplate(name[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region Traversal Methods

    private void resetToTop() {
        currentNode = currentRoot = mainRoot;
    }

    private void stepOut() {
        currentNode = currentNode.getParent();
        findRoot(currentNode);
    }

    private void stepIn() {
        currentNode = currentNode.getFirstChild();
        findRoot(currentNode);
    }

    private void nextInLevel() {
        if (!documentEmpty()) {
            currentNode = currentNode.getNext();
            findRoot(currentNode);
        } else {
            System.out.println("Document is empty");
        }
    }

    private void previousInLevel() {
        if (!documentEmpty()) {
            currentNode = currentNode.getPrev();
            findRoot(currentNode);
        } else {
            System.out.println("Document is empty");
        }
    }

    private void top() {
        currentNode = currentRoot;
    }

    private void nav(String[] params) {
        if (params.length == 0) throw new InvalidParameterException("Invalid number of parameters");
        else if (params.length == 1) findFromMainRoot(params[0]);
        else findFromRoot(params[0], params[1]);

    }


    //endregion

    //region Removal Utility


    private void removeAttrib(String[] name) {
        if (name.length == 0) {
            currentNode.removeLastAttribute();
        } else {
            if (!currentNode.removeAttributeWithName(name[0])) {
                System.out.println("No attribute with name: " + name[0] + " found");
            }
        }
    }


    private void removeCurrent() {
        currentNode.getParent().removeChildNode(currentNode);
        currentNode = currentNode.getParent();
    }


    private void clear() {
        if (!documentEmpty()) {
            mainRoot = new XMLContainer(documentName);
            currentNode = currentRoot = mainRoot;
            Log.clearLog();
            Messenger.emptyDocumentMsg();
        }
    }


    //endregion


}