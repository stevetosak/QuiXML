package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.editor.XMLEditor;
import myXml.main.Main;
import myXml.util.DocumentStateWrapper;
import myXml.util.Log;
import org.w3c.dom.Node;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class TemplateManager implements CommandManager {

    private final String TEMPLATE_PATH = "src/main/resources/templates/";
    private final NodeManager nodeManager = new NodeManager();

    @CommandHandler(names = {"lt", "load-template", "ldtmp"})
    public XMLEditor loadTemplate(DocumentStateWrapper document, String[] name) {
        try (FileInputStream fileIs = new FileInputStream( TEMPLATE_PATH + name[0] + ".txt")) {
            nodeManager.clear(document, name);
            return Main.commandLoop(new XMLEditor(), fileIs); // ova jako e hehe
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @CommandHandler(names = {"svtmp", "save-template"})
    public void createTemplate(DocumentStateWrapper document, String[] name) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(createFileTemplate(name[0])));
            StringBuilder sb = new StringBuilder();
            Log.getCommandLog().forEach(command -> {
                String commandName = command.getShortName();
                if (Log.isLoggable(commandName)) {
                    sb.append(command.rawCommandFormat()).append('\n');
                }
            });
            sb.append("td").append("\n");
            sb.deleteCharAt(sb.length() - 1);
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @CommandHandler(names = {"rmtmp", "remove-template"})
    public void deleteTemplate(DocumentStateWrapper document, String[] name) throws IOException {
        Path filePath = Path.of(TEMPLATE_PATH + name[0] + ".txt");
        Files.delete(filePath);
        System.out.println("Template " + name[0] + " successfully deleted");
    }

    @CommandHandler(names = {"shtmp","show-templates"})
    public void showTemplates(DocumentStateWrapper document, String [] params){
        Path directory = Path.of(TEMPLATE_PATH);
        System.out.println("Available templates: ");
        try {
            try (Stream<Path> files = Files.list(directory)) {
                files.forEach(file -> System.out.println(file.getFileName()));
            }
        } catch (IOException ignored) {
        }
    }

    @CommandHandler(names = {"clrtmp","clear-templates"})
    public void clearTemplates(DocumentStateWrapper document, String [] params){
        System.out.println("Are you sure you want to delete all templates? This action can not be undone.\nType 'y' to confirm.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            char ans = (char) br.read();
            if (ans == 'y') {
                Path templatesDir = Paths.get(TEMPLATE_PATH);
                if (!Files.exists(templatesDir)) return;
                try (Stream<Path> filesInDir = Files.walk(templatesDir)) {
                    filesInDir.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                    System.out.println("Deleted all templates.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String createFileTemplate(String name) {
        String dirPath = "src/main/resources/templates";
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
            } else {
                System.out.println("Template with name: \"" + name + "\"" + " already exists!");
            }
        } catch (Exception ignored) {
        }

        return filePath.toString();
    }




}
