package CSE4705_final.Console;

import CSE4705_final.State.NodeSet;
import java.io.*;
import java.util.*;

/**
 *
 * @author Ethan Levine
 */
public class MainConsole {

    public static final String VERSION = "v0.1";

    private static final String _mainMenu = "main";
    private static final Map<String, ConsoleMenu> _menus = new HashMap<String, ConsoleMenu>();

    private static String _nextMenu;
    private static BufferedReader _in;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("AmazAI " + VERSION + "\n\nLoading...");
             _in = new BufferedReader(new InputStreamReader(System.in));
            buildMenus();
            _nextMenu = _mainMenu;
            while (!_nextMenu.equals("") && _menus.containsKey(_nextMenu)) {
                System.out.println("\n");
                System.out.println(_menus.get(_nextMenu).getPrintout());
                String input = "";
                while (input.equals("")) {
                System.out.print("\n > ");
                    input = _in.readLine();
                }
                String next = _menus.get(_nextMenu).handleInput(input);
                if (!next.equals("REPEAT")) {
                    _nextMenu = next;
                }
            }
            System.out.println("\nExiting AmazAI...\n");
        } catch (IOException e) {
            System.out.println("An IO excpetion has occurred!  Exiting...\n");
        }
    }

    private static void buildMenus() {
        final Runnable emptyRunnable = new Runnable() { public void run() {} };
        // main menu
        ConsoleMenu mainMenu = new ConsoleMenu("Main Menu");

        mainMenu.addOption(new ConsoleOption("[H]uman player", "Run the Human AI", "Allows you to connect to a remote Amazons server\nand play a game manually.", new Runnable() {
            public void run() {
                System.out.println("CLOSURES!");
            }
        }, "main"));
        mainMenu.addOption(new ConsoleOption("Mis[c]ellaneous", "Misc. commands, including odd tests", "Contains miscellaneous commands that are not related to the normal operation of AmazAI.", emptyRunnable, "misc"));
        mainMenu.addOption(new ConsoleOption("[Q]uit", "Exit AmazAI", emptyRunnable, ""));
        
        ConsoleMenu miscMenu = new ConsoleMenu("Miscellaneous Commands");
        
        miscMenu.addOption(new ConsoleOption("[M]ain Menu", "Return to the main menu", emptyRunnable, "main"));
        miscMenu.addOption(new ConsoleOption("Test NodeSet printout", "Prints a test printout to the console", new Runnable() {
            public void run() {
              //  NodeSet.main(new String[0]);
            }
        }, "misc"));
        
        _menus.put("main", mainMenu);
        _menus.put("misc", miscMenu);
    }

}
