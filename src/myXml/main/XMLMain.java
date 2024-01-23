package myXml.main;

import myXml.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class XMLMain {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        XMLEditor editor = new XMLEditor();
        Log.emptyDocumentMsg();
        String line = br.readLine();
        while (true) {
            String[] parts = line.split("\\s+");
            String command = parts[0];
            if (command.equals("END")) break;
            String[] params = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                editor.processCommands(command, params);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            line = br.readLine();
        }
    }
}