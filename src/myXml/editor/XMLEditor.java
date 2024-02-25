package myXml.editor;

import myXml.main.XMLMain;
import myXml.util.*;
import myXml.xmlComponents.XMLComponent;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XMLEditor {
    private final static int MAX_STACK_CAPACITY = 100;
    //region Data and Constructor
    public static String documentName = "XMLDocument";
    private final Deque<DocumentStateWrapper> undoStack;
    private final Deque<DocumentStateWrapper> redoStack;
    CommandProcessor commandProcessor = new CommandProcessor();
    private XMLComponent mainRoot;// sentinel
    private XMLComponent currentNode;
    private XMLComponent currentRoot;
    private boolean toPrint;

    public XMLEditor() throws IOException {
        undoStack = new ArrayDeque<>(MAX_STACK_CAPACITY);
        redoStack = new ArrayDeque<>(MAX_STACK_CAPACITY);
        this.mainRoot = new XMLContainer(documentName);
        currentNode = currentRoot = mainRoot;
        CommandHelper.init("commands/commandList");
    }
    //endregion

    //region Initialization and Helper Methods

    private void resetToTop() {
        currentNode = currentRoot = mainRoot;
    }

    private XMLComponent bfs(XMLComponent node, String targetTag) {
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

    public void run(String command, String[] params, boolean logAndPrint) {
        commandProcessor.processCommand(command, params, logAndPrint);
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
        Log.currentNodeMsg(currentNode.getTagNameFormatted(),
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

    private class CommandProcessor {

        //region Data and Constructor
        private final Map<String, Consumer<String[]>> commandMap = new HashMap<>();

        public CommandProcessor() {
            commandMap.put("atrib", this::addAttribute);
            commandMap.put("leaf", this::addLeaf);
            commandMap.put("container", this::addContainer);
            commandMap.put("root", this::addRoot);
            commandMap.put("help", CommandHelper::getCommandHelp);
            commandMap.put("up", (params) -> stepOut());
            commandMap.put("down", (params) -> stepIn());
            commandMap.put("next", (params) -> nextInLevel());
            commandMap.put("remove-c", (params) -> removeCurrent());
            commandMap.put("print-r", (params) -> printCurrentRoot());
            commandMap.put("print-c", (params) -> printCurrent());
            commandMap.put("print-a", (params) -> printEditor());
            commandMap.put("cmd-all", (params) -> CommandHelper.displayAllCommands());
            commandMap.put("nav", this::nav);
            commandMap.put("top-c", (params) -> top());
            commandMap.put("clear-d", (params) -> clear());
            commandMap.put("swap", this::swap);
            commandMap.put("load-t", this::loadTemplate);
            commandMap.put("write", (params) -> writeToFile());
            commandMap.put("save-t", this::createTemplate);
            commandMap.put("del-t", this::deleteTemplate);
            commandMap.put("show-t", (params) -> Log.showTemplates());
            commandMap.put("ptog", (params) -> togglePrint());
            commandMap.put("top-d", (params) -> resetToTop());
            commandMap.put("clear-log", (params) -> Log.clearLog());
            commandMap.put("revert-log", (params) -> Log.revertLog());
            commandMap.put("show-logs", (params) -> Log.showLoggedCommands());
            commandMap.put("add", this::addNode);
            commandMap.put("back", (params) -> previousInLevel());
            commandMap.put("del-a", this::removeAttrib);
            commandMap.put("undo", (params) -> undo());
            commandMap.put("redo", (params) -> redo());
            commandMap.put("clear-t", (params) -> clearLog());
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
            XMLComponent node = bfs(currentNode, params[0]);
            if (currentNode == mainRoot) currentRoot = node;
            currentNode = node;
            Log.leafAddedMsg(params[0], val);
        }

        private void addContainer(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addChild(new XMLContainer(params[0]));
            XMLComponent node = bfs(currentNode, params[0]);
            if (currentNode == mainRoot) currentRoot = node;
            currentNode = node;
            Log.containerAddedMsg(params[0]);
        }

        private void addRoot(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            XMLContainer element = new XMLContainer(params[0]);
            element.setParent(mainRoot);
            mainRoot.addChild(element);
            Log.rootAdded(params[0]);
        }

        private void addNode(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            if (params.length == 1) addContainer(params);
            else addLeaf(params);
        }
        //endregion

        //region Command Processing and Help


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
                currentRoot = bfs(mainRoot, prevState.currentRootTag());
                currentNode = bfs(mainRoot, prevState.currentNodeTag());// optimize
                Log.updateCommandLog(prevState.commandLog());
            }
        }

        private void redo() {
            if (Log.getLastCommandName().equals("undo") || Log.getLastCommandName().equals("redo")) {
                if (!redoStack.isEmpty() && !undoStack.isEmpty()) {
                    DocumentStateWrapper prevState = redoStack.pop();
                    mainRoot = prevState.mainRoot();
                    currentRoot = bfs(mainRoot, prevState.currentRootTag());
                    currentNode = bfs(mainRoot, prevState.currentNodeTag());
                    Log.updateCommandLog(prevState.commandLog());
                }
            } else {
                redoStack.clear();
            }
        }

        private void processCommand(String commandName, String[] params, boolean logStateAndPrint) {
            Consumer<String[]> commandHandler = commandMap.get(commandName);
            if (commandHandler != null) {
                Log.logCommand(commandName, params); // zaradi clear
                if (logStateAndPrint) {
                    logDocumentState(commandName, undoStack, "undo");
                }

                commandHandler.accept(params);
                toPrint = logStateAndPrint;
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

        private void top() {
            currentNode = currentRoot;
        }

        private void nav(String[] params) {
            if (params.length == 0) throw new InvalidParameterException("Invalid number of parameters");
            else if (params.length == 1) selectFromDocument(params[0]);
            else selectFromRoot(params[0], params[1]);

        }

        private void selectFromDocument(String tagName) {
            XMLComponent target = bfs(mainRoot, tagName);
            if (target != null) {
                currentNode = target;
                findRoot(currentNode);
            } else {
                System.out.println("Element not found");
            }
        }

        private void selectFromRoot(String targetNodeTagName, String rootTagName) {
            XMLComponent targetRoot = bfs(mainRoot, rootTagName);
            if (targetRoot != null) {
                XMLComponent targetNode = bfs(targetRoot, targetNodeTagName);
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

        private void swap(String[] tags) {
            if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            XMLComponent node1 = bfs(mainRoot, tags[0]);
            XMLComponent node2 = bfs(mainRoot, tags[1]);

            if (node1 == null || node2 == null) return;

            node1.setTag(tags[1]);
            node2.setTag(tags[0]);
        }

        private void loadTemplate(String[] name) {
            try (FileInputStream fileIs = new FileInputStream("templates/" + name[0] + ".txt")) {
                XMLMain.commandLoop(XMLEditor.this, fileIs);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void removeCurrent() {
            currentNode.getParent().removeChildNode(currentNode);
            currentNode = currentNode.getParent();
        }

        private void deleteTemplate(String[] name) {
            try {
                Log.deleteTemplate(name[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void clear() {
            if (!documentEmpty()) {
                mainRoot = new XMLContainer(documentName);
                currentNode = currentRoot = mainRoot;
                Log.clearLog();
                Log.emptyDocumentMsg();
            }
        }

        private void togglePrint() {
            toPrint = !toPrint;
            System.out.println("Printing the document after every command is: " + toPrint);
        }

        //endregion

    }

}