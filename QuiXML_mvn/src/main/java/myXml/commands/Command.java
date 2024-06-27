package myXml.commands;

public class Command {

    private String shortName;
    private String info;
    private String[] aliases;
    private String[] params;

    public Command(String shortName, String[] aliases, String info) {
        this.shortName = shortName;
        this.info = info;
        this.aliases = aliases;
    }

    public Command(String shortName, String[] params) {
        this.shortName = shortName;
        this.params = params;
    }

    public void getHandler(){

    }

    public boolean contains(String name){
        if(shortName.equals(name)) return true;
        for(String alias : aliases){
            if (name.equals(alias)) return true;
        }
        return false;
    }

    public String getShortName() {
        return shortName;
    }

    public String rawCommandFormat() {
        return shortName + (params.length == 0 ? "" : " " + String.join(" ", params));
    }

    public String infoCommandFormat() {
        String[] words = info.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if ((i + 1) % 15 == 0) sb.append("\n");
            sb.append(words[i]).append(" ");
        }

        int id = sb.lastIndexOf("]");
        if (id != -1) sb.insert(id + 1, "\n");

        StringBuilder wb = new StringBuilder();

        wb.append(shortName);

        for(String alias : aliases){
            wb.append(" | ");
            wb.append(alias);
        }

        wb.append("\n");
        wb.append(sb);

        return wb.toString();
    }
}
