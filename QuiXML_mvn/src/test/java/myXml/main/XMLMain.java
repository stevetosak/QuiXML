package myXml.main;

import myXml.editor.XMLEditor;
import myXml.util.Messenger;

import java.io.*;
import java.util.Arrays;

public class XMLMain {

    public static void commandLoop(XMLEditor editor, InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            String[] parts = line.split("\\s+");
            String command = parts[0];
            if (command.equals("END")) break;
            String[] params = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                editor.processCommands(command, params, !(is instanceof FileInputStream)); //Ako e od file - false.
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            line = br.readLine();
        }
    }

    public static void main(String[] args) throws IOException {
        Messenger.emptyDocumentMsg();
        commandLoop(new XMLEditor(), System.in);

    }
}