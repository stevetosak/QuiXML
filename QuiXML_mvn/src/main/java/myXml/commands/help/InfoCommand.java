package myXml.commands.help;

/**
 * This is a command format that is used when using the
 * {@link CommandHelper#getCommandHelp(String[])}, through the "help" command.
 * This command format displays the command name and a description of what the command functionality is
 */
public class InfoCommand implements CommandFormat {
    private final String name;
    private final String info;

    public InfoCommand(String name, String info) {
        this.name = name;
        this.info = info;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String commandFormat() {
        String[] words = info.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if ((i + 1) % 15 == 0) sb.append("\n");
            sb.append(words[i]).append(" ");
        }

        int id = sb.lastIndexOf("]");
        if (id != -1) sb.insert(id + 1, "\n");
        return name + " - " + sb;
    }

}
