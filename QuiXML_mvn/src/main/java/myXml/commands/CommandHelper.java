package myXml.commands;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHelper {
    private static final Map<String, InfoCommand> commandToInfoMap = new HashMap<>();
    private static List<InfoCommand> commandList;

    public static void init(String path) throws IOException {
        commandList = initCommands(path);
        commandList.forEach(command -> commandToInfoMap.put(command.getName(), command));

    }

    public static List<InfoCommand> getCommandList() {
        return new ArrayList<>(commandList);
    }

    public static void getCommandHelp(String[] name) {
        System.out.println(commandToInfoMap.get(name[0]).commandFormat());
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

    private static List<InfoCommand> initExec(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        List<InfoCommand> commandList = new ArrayList<>();
        while (line != null) {
            String[] parts = line.split(";");
            if (parts.length >= 2) commandList.add(new InfoCommand(parts[0], parts[1]));
            line = br.readLine();
        }
        return commandList.stream().sorted(Comparator.comparing(InfoCommand::getName)).collect(Collectors.toList());
    }

    private static List<InfoCommand> initCommands(String path) throws IOException {
        try (InputStream is = new FileInputStream(path)) {
            return initExec(is);
        }
    }

}

