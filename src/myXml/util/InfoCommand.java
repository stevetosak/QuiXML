package myXml.util;

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
            if ((i + 1) % 10 == 0) sb.append("\n");
            sb.append(words[i]).append(" ");
        }
        return name + " - " + sb;
    }

}
