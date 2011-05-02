package CSE4705_final.Console;

import CSE4705_final.Client.*;
import CSE4705_final.State.*;
import CSE4705_final.AI.*;
import CSE4705_final.AI.Eval.*;
import CSE4705_final.AI.Timers.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author Ethan Levine
 */
public class MainConsole {

    public static final String VERSION = "v0.2";
    private static final String DEFAULT_HOSTNAME = "icarus.engr.uconn.edu";
    private static final int DEFAULT_PORT = 3499;
    private final static String DEFAULT_USER_ONE = "5";
    private final static String DEFAULT_USER_TWO = "6";
    private final static String DEFAULT_PASSWORD_ONE = "733167";
    private final static String DEFAULT_PASSWORD_TWO = "431349";
    private final static String DEFAULT_OPPONENT = "0";

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
    
    private static String sai_hostname;
    private static int sai_port;
    private static String sai_username;
    private static String sai_password;
    private static String sai_opponent;
    private enum AIType { RANDOM, SEARCH }
    private static AIType sai_aitype;
    
    private static void singleAIGame_setDefaultServer() {
        sai_hostname = DEFAULT_HOSTNAME;
        sai_port = DEFAULT_PORT;
    }
    
    private static void singleAIGame_setCustomServer() {
        try {
            System.out.print("Custom hostname: ");
            sai_hostname = _in.readLine();
            System.out.print("Custom port: ");
            sai_port = Integer.parseInt(_in.readLine());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Using defaults.");
            singleAIGame_setDefaultServer();
        }
    }
    
    private static void singleAIGame_setDefaultUser() {
        sai_username = DEFAULT_USER_ONE;
        sai_password = DEFAULT_PASSWORD_ONE;
    }
    
    private static void singleAIGame_setAlternateUser() {
        sai_username = DEFAULT_USER_TWO;
        sai_password = DEFAULT_PASSWORD_TWO;
    }
    
    private static void singleAIGame_setCustomUser() {
        try {
            System.out.print("Custom user ID: ");
            sai_username = _in.readLine();
            System.out.print("Custom password: ");
            sai_password = _in.readLine();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Using default user.");
            singleAIGame_setDefaultUser();
        }
    }
    
    private static void singleAIGame_setDefaultOpponent() {
        sai_opponent = DEFAULT_OPPONENT;
    }
    
    private static void singleAIGame_setCustomOpponent() {
        try {
            System.out.print("Custom opponent: ");
            sai_opponent = _in.readLine();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.out.println("Using default opponent.");
            singleAIGame_setDefaultOpponent();
        }
    }
    
    private static void singleAIGame_play() {
        final boolean logging = true;
        // Create the client object.
        Client c = new Client();
        try {
            if (logging) {
                c.enableLog();
            }
            c.Connect(sai_hostname, sai_port);
            c.LoginAndMatch(sai_username, sai_password, sai_opponent);
            // make the new AI
            ClientInterface iface;
            BareAI ai;
            switch (sai_aitype) {
                case RANDOM:
                    ai = new RandomAI(c.isBlack());
                    iface = ai.getInterface();
                    break;
                case SEARCH:
                    Evaluator eval = new Heuristic();
                    AITimer timer = new ExpectedScaleTimer(2);
                    ai = new GenericSearchAI(c.isBlack(), eval, timer, 2, 5, false);
                    iface = ai.getInterface();
                    break;
                default:
                    throw new ClientException("Unknown AI type.");
            }
            boolean weWon = c.Play(iface);
            // print the log.
            if (logging) {
                System.out.println(c.getLog());
            }
            if (weWon) {
                System.out.println("We won!");
            } else {
                System.out.println("We lost :(");
            }
        } catch (ClientException e) {
            System.out.println(c.getLog());
            System.out.println("Client exception: " + e.getMessage());
//        } catch (Exception e) {
//            System.out.println(c.getLog());
//            System.out.println("General exception: " + e.getClass().toString() + ": " + e.getMessage());
        }
    }
    
    private static void singleAIGame_confirmPlay() {
        try {
            System.out.println("The current setup is:\n");
            System.out.println("Hostname:   " + sai_hostname);
            System.out.println("Port:       " + sai_port);
            System.out.println("User ID:    " + sai_username);
            System.out.println("Password:   " + sai_password);
            System.out.println("Opponent:   " + sai_opponent);
            System.out.println("AI Type:    " + sai_aitype.toString());
            System.out.println();
            System.out.print("Are you SURE you want to continue? (y/n): ");
            String d = _in.readLine();
            if (d.toLowerCase().startsWith("y")) {
                singleAIGame_play();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
    
    private static void addSingleAIMenus() {
        final Runnable emptyRunnable = new Runnable() { public void run() {} };
        
        final String defaultServerInfo = "The default server info is:\n\nHostname: " + DEFAULT_HOSTNAME + "\nPort:     " + DEFAULT_PORT;
        final String customServerInfo = "Allows you to specify the hostname and port of an Amazons server.";
        
        final String defaultUserInfo = "The default user info is:\n\nUser ID:  " + DEFAULT_USER_ONE + "\nPassword: " + DEFAULT_PASSWORD_ONE;
        final String alternateUserInfo = "The alternate user info is:\n\nUser ID:  " + DEFAULT_USER_TWO + "\nPassword: " + DEFAULT_PASSWORD_ONE;
        final String customUserInfo = "This allows you to specify a custom user ID and a custom password to play on the selected server.";
        
        ConsoleMenu singleAIMenu = new ConsoleMenu("Single AI Menu - Select Server");
        singleAIMenu.addOption(new ConsoleOption("[M]ain Menu", "Return to the main menu", emptyRunnable, "main"));
        singleAIMenu.addOption(new ConsoleOption("Play on [d]efault server", "Plays on the default Amazons server",
                defaultServerInfo, new Runnable() {
                    public void run() {
                        singleAIGame_setDefaultServer();
                    }
                }, "single_ai_select_user"));
        singleAIMenu.addOption(new ConsoleOption("Play on [c]ustom server", "Plays on a custom Amazons server",
                customServerInfo, new Runnable() {
                    public void run() {
                        singleAIGame_setCustomServer();
                    }
                }, "single_ai_select_user"));
        
        ConsoleMenu singleAIUser = new ConsoleMenu("Single AI Menu - Select User");
        singleAIUser.addOption(new ConsoleOption("[M]ain Menu", "Return to the main menu", emptyRunnable, "main"));
        singleAIUser.addOption(new ConsoleOption("[B]ack", "Return to the previous menu", emptyRunnable, "single_ai_select_server"));
        singleAIUser.addOption(new ConsoleOption("Use [d]efault user", "Uses the first default user",
                defaultUserInfo, new Runnable() {
                    public void run() {
                        singleAIGame_setDefaultUser();
                    }
                }, "single_ai_select_opponent"));
        singleAIUser.addOption(new ConsoleOption("Use [a]lternate user", "Uses the second default user",
                alternateUserInfo, new Runnable() {
                    public void run() {
                        singleAIGame_setAlternateUser();
                    }
                }, "single_ai_select_opponent"));
        singleAIUser.addOption(new ConsoleOption("Use [c]ustom user", "Lets you specify a custom user",
                customUserInfo, new Runnable() {
                    public void run() {
                        singleAIGame_setCustomUser();
                    }
                }, "single_ai_select_opponent"));
        
        ConsoleMenu singleAIOpponent = new ConsoleMenu("Single AI Menu - Select Opponent");
        singleAIOpponent.addOption(new ConsoleOption("[M]ain Menu", "Return to the main menu", emptyRunnable, "main"));
        singleAIOpponent.addOption(new ConsoleOption("[B]ack", "Return to the previous menu", emptyRunnable, "single_ai_select_user"));
        singleAIOpponent.addOption(new ConsoleOption("Play against [d]efault opponent", "Plays against the server's default opponent",
                new Runnable() {
                    public void run() {
                        singleAIGame_setDefaultOpponent();
                    }
                }, "single_ai_select_ai"));
        singleAIOpponent.addOption(new ConsoleOption("Play against a [c]ustom opponent", "Lets you specify a custom opponent",
                new Runnable() {
                    public void run() {
                        singleAIGame_setCustomOpponent();
                    }
                }, "single_ai_select_ai"));
        
        ConsoleMenu singleAIAI = new ConsoleMenu("Single AI Menu - Select AI");
        singleAIAI.addOption(new ConsoleOption("[M]ain Menu", "Return to the main menu", emptyRunnable, "main"));
        singleAIAI.addOption(new ConsoleOption("[B]ack", "Return to the previous menu", emptyRunnable, "single_ai_select_opponent"));
        singleAIAI.addOption(new ConsoleOption("Use [R]andom AI", "Uses the random AI to play",
                new Runnable() {
                    public void run() {
                        sai_aitype = AIType.RANDOM;
                        singleAIGame_confirmPlay();
                    }
                }, "main"));
        singleAIAI.addOption(new ConsoleOption("Use [S]earch AI", "Uses the generic search AI to play",
                new Runnable() {
                    public void run() {
                        sai_aitype = AIType.SEARCH;
                        singleAIGame_confirmPlay();
                    }
                }, "main")); // TODO - route this through the proper submenus!
        
        _menus.put("single_ai_select_server", singleAIMenu);
        _menus.put("single_ai_select_user", singleAIUser);
        _menus.put("single_ai_select_opponent", singleAIOpponent);
        _menus.put("single_ai_select_ai", singleAIAI);
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
        mainMenu.addOption(new ConsoleOption("[S]ingle AI Player", "Runs a single AI", "Allows you to connect to a remote Amazons server\nand play a game using a custom AI.",
                emptyRunnable, "single_ai_select_server"));
        mainMenu.addOption(new ConsoleOption("Mis[c]ellaneous", "Misc. commands, including odd tests", "Contains miscellaneous commands that are not related to the normal operation of AmazAI.", emptyRunnable, "misc"));
        mainMenu.addOption(new ConsoleOption("[Q]uit", "Exit AmazAI", emptyRunnable, ""));
        
        ConsoleMenu miscMenu = new ConsoleMenu("Miscellaneous Commands");
        
        miscMenu.addOption(new ConsoleOption("[M]ain Menu", "Return to the main menu", emptyRunnable, "main"));
        miscMenu.addOption(new ConsoleOption("Test NodeSet printout", "Prints a test printout to the console", new Runnable() {
            public void run() {
              //  NodeSet.main(new String[0]);
            }
        }, "misc"));
        miscMenu.addOption(new ConsoleOption("Test Search vs. Random", "Tests the search AI vs. the random AI", new Runnable() {
            
            public void run () {
                new Thread(new Runnable() {
                    public void run() {
                        
                        Client c = new Client();
                        try {
                            c.Connect(DEFAULT_HOSTNAME, DEFAULT_PORT);
                            c.LoginAndMatch(DEFAULT_USER_ONE, DEFAULT_PASSWORD_ONE, DEFAULT_USER_TWO);
                            // make the new AI
                            BareAI ai = new RandomAI(c.isBlack());
                            ClientInterface iface = ai.getInterface();
                            c.Play(iface);
                        } catch (ClientException e) {
                            
                        }
                    }
                }).start();
                new Thread(new Runnable() {
                    public void run() {
                        
                        Client c = new Client();
                        try {
                            c.enableLog();
                            c.Connect(DEFAULT_HOSTNAME, DEFAULT_PORT);
                            c.LoginAndMatch(DEFAULT_USER_TWO, DEFAULT_PASSWORD_TWO, DEFAULT_USER_ONE);
                            // make the new AI
                            Evaluator eval = new BasicEvaluator(650, 150, 350, -650, -150, -350);
//                            AITimer timer = new ExpectedScaleTimer(2);
                            AITimer timer = new ConstantTimer(2000);
                            BareAI ai = new GenericSearchAI(c.isBlack(), eval, timer, 2, 5, false);
                            ClientInterface iface = ai.getInterface();
                            boolean weWon = c.Play(iface);
                            System.out.println(c.getLog());
                            if (weWon) {
                                System.out.println("We won!");
                            } else {
                                System.out.println("We lost :(");
                            }
                        } catch (ClientException e) {
                            System.out.println(c.getLog());
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        }, "misc"));
        
        _menus.put("main", mainMenu);
        _menus.put("misc", miscMenu);
        
        addSingleAIMenus();
    }

}
