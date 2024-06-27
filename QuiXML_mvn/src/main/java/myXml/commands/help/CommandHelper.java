package myXml.commands.help;

import myXml.commands.manager.ClassIdentifier;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHelper {
    private static final String COMMAND_LIST_PATH = "src/main/resources/command_list.ssv";
    private final static List<Command> commandList;

    static {
        try {
            commandList = initCommands();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getCommandHelp(String[] name) throws InvalidCommandException {
       Command cmd = getCommand(name[0]);
       if(cmd != null) System.out.println(cmd.infoCommandFormat());

    }

    public static Command getCommand(String name){
        for(Command cmd : commandList){
            if(cmd.contains(name)){
                return cmd;
            }
        }
        return null;
    }

    public static void displayAllCommands() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandList.size(); i++) {
            sb.append(commandList.get(i).getShortName()).append(" | ");
            if (i > 0 && i % 4 == 0) sb.append("\n");
        }
        sb.append("\n");
        System.out.println(sb);
        System.out.println("To get info about a command type \"help (command name)\"");
    }


    private static List<Command> initExec(InputStream is) throws IOException {
        List<Command> commands = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            String[] parts = line.split(";");
            if(parts.length < 2){
                System.out.println("Zgrese negde gojdo proveri go fajlot");
            }
            String name = parts[1];
            String classID = parts[0].substring(1);
            String [] aliases = new String[0];
            if(parts.length -1 > 1){
               aliases = Arrays.copyOfRange(parts,2,parts.length -1);
            }
            ClassIdentifier cid = ClassIdentifier.NONE;
            for(ClassIdentifier c : ClassIdentifier.values()){
                if(classID.equals(c.name())) cid = c;
            }
            String info = parts[parts.length-1];
            commands.add(new Command(name,aliases,info,cid));
            line = br.readLine();
        }
        return commands.stream().sorted(Comparator.comparing(Command::getShortName)).collect(Collectors.toList());
    }

    private static List<Command> initCommands() throws IOException {
        try (InputStream is = new FileInputStream(CommandHelper.COMMAND_LIST_PATH)) {
            return initExec(is);
        }
    }

}

