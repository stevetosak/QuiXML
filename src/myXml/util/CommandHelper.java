package myXml.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CommandInitializer {
    private static List<InfoCommand> initExec(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        List<InfoCommand> commandList = new ArrayList<>();
        while (line != null) {
            String[] parts = line.split(";");
            if (parts.length < 2) {
                line = br.readLine();
                continue;
            }
            boolean available = false;
            if (parts.length >= 3) {
                if (parts[2].equals("permitted")) available = true;
            }
            commandList.add(new InfoCommand(parts[0], parts[1], available));
            line = br.readLine();
        }
        return commandList;
    }

    static List<InfoCommand> initCommands(String path) throws IOException {
        try (InputStream is = new FileInputStream(path)) {
            return initExec(is);
        }
    }
}

public class CommandHelper {
    public static final String INIT_COMMAND = "root";
    private static final Map<String, InfoCommand> commandToInfoMap = new HashMap<>();
    private static final List<InfoCommand> uninitCommands = new ArrayList<>();
    private static List<InfoCommand> commandList;

    public static void init(String path) throws IOException {
        commandList = CommandInitializer.initCommands(path);
        commandList.forEach(command -> {
            commandToInfoMap.put(command.getName(), command);
            if (command.isPermittedWhenNotInit()) uninitCommands.add(command);
        });
    }

    public static List<String> getCommandList() {
        return commandList.stream().map(InfoCommand::getName).collect(Collectors.toList());
    }

    public static void getCommandHelp(String[] name) {
        System.out.println(commandToInfoMap.get(name[0]).commandFormat());
    }

    public static List<String> getAvailableCommands() {
        return uninitCommands.stream().map(InfoCommand::getName).collect(Collectors.toList());
    }

    public static void displayAvailableCommands() {
        print(uninitCommands);
    }

    public static void displayAllCommands() {
        print(commandList);
    }

    private static void print(List<InfoCommand> list) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).getName()).append(" | ");
            if (i > 0 && i % 4 == 0) sb.append("\n");
        }
        sb.append("\n");
        System.out.println(sb);
        System.out.println("To get info about a command type \"help (command name)\"");
    }

}

