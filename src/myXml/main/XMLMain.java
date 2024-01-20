package myXml.main;

import myXml.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.*;

public class XMLMain {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        XMLEditor editor = new XMLEditor();
        Log.emptyDocumentMsg();

        //todo ova vo proccess comamands vnatre trebit da e
        while (true) {
            boolean init = false;
            try {
                init = editor.init(br.readLine());
            } catch (InvalidParameterException ex) {
                System.out.println(ex.getMessage());
            }

            if(init) break;

            Log.emptyDocumentMsg();
        }

        Log.initSuccessMsg();
        Log.currentNodeMsg(editor.getCurrentNode().getTag());

        String line = br.readLine();

        while (true) {
            String[] parts = line.split("\\s+");
            String command = parts[0];
            if (command.equals("END")) break;
            String[] params = Arrays.copyOfRange(parts, 1, parts.length);
            try {
                editor.processCommands(command, params);
            } catch (InvalidParameterException ex) {
                System.out.println(ex.getMessage());
            }

            line = br.readLine();
        }
    }
}