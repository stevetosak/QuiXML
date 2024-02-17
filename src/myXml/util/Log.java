package myXml.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;
import java.util.Stack;
import java.util.stream.Stream;

public class Log {
    private static final Stack<List<String>> previousLogState = new Stack<>();
    private static List<String> commandLog = new ArrayList<>();

    public static void logCommand(String command, String[] params) {
        String sb = command + " " + String.join(" ", params);
        commandLog.add(sb);
    }

    public static void initSuccessMsg() {
        System.out.println("Initialization successful");
        System.out.println("To see the all of the commands type \"commands-all\" :)");
    }

    public static void currentNodeMsg(String currentNodeTag) {
        System.out.println("Current node is: " + currentNodeTag);
    }

    public static void leafAddedMsg(String name, String value) {
        System.out.println("Added leaf element: " + name + " with the content inside: " + value);
    }

    public static void attributeAddedMsg(String name, String value) {
        System.out.println("Added attribute: " + name + " with value of: " + value);
    }

    public static void containerAddedMsg(String tagName) {
        System.out.println("Added container: " + tagName);
    }

    public static void rootAdded(String tagName) {
        System.out.println("Added root: " + tagName);
    }

    public static void invalidCommandMsg() {
        System.out.println("Invalid command");
    }

    public static void documentClearedMsg() {
        System.out.println("Document cleared");
        emptyDocumentMsg();
        System.out.println("You can revert to the previous state of the document by typing \"revert\"");
    }

    public static void emptyDocumentMsg() {
        System.out.println("Document is empty ");
        System.out.println("To get started you can either:");
        System.out.println("\t- Load from a template using \"template (name)\" ");
        System.out.println("\t- Type \"root (tagName)\"  to add a tag to the document");
        System.out.println("Use \"commands-avb\" to show available commands");
    }

    public static void showLoggedCommands() {
        commandLog.forEach(System.out::println);
    }

    public static String addFileTemplate(String name) {
        String dirPath = "C:\\Users\\stefa\\IdeaProjects\\XMLEditor_v1\\templates";
        String fileName = name + ".txt";

        Path directoryPath = Paths.get(dirPath);
        Path filePath = Paths.get(dirPath, fileName);

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                System.out.println("Template " + name + " was created");
            }
        } catch (Exception ignored) {
        }

        return filePath.toString();
    }


    public static void saveTemplate(String name) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(addFileTemplate(name)));
        StringBuilder sb = new StringBuilder();
        commandLog.forEach(s -> {
            String command = s.split("\\s+")[0];
            if (!command.equals("template") && !command.equals("save-template")) {
                sb.append(s).append('\n');
            }

        });
        sb.deleteCharAt(sb.length() - 1);
        bw.write(sb.toString());
        bw.close();
    }

    public static void deleteTemplate(String name) throws IOException {
        Path filePath = Path.of("C:\\Users\\stefa\\IdeaProjects\\XMLEditor_v1\\templates\\" + name + ".txt");
        Files.delete(filePath);
    }

    public static void showTemplates() {
        Path directory = Path.of("C:\\Users\\stefa\\IdeaProjects\\XMLEditor_v1\\templates");
        System.out.println("Available templates: ");

        try {
            try (Stream<Path> files = Files.list(directory)) {
                files.forEach(file -> System.out.println(file.getFileName()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void clearLog() {
        previousLogState.push(commandLog);
        commandLog = new ArrayList<>();
    }

    public static void revertLog() {
        if (previousLogState.empty()) {
            System.out.println("No previous logs");
            return;
        }

        commandLog = previousLogState.pop();

    }

    // save template (ime)

}
