package CSE4705_final.Console;

/**
 *
 * @author Ethan Levine
 */
public class ConsoleOption implements Runnable {
    private final String _displayName;
    private final String _optionName;
    private final String _shortcut;
    private final String _shortDesc;
    private final String _longDesc;
    private final Runnable _action;
    private final String _nextMenu;

    public ConsoleOption(String displayName, Runnable action, String nextMenu) {
        _displayName = displayName;
        _optionName = stripShortcut(displayName);
        _shortcut = extractShortcut(displayName);
        _shortDesc = "";
        _longDesc = "";
        _action = action;
        _nextMenu = nextMenu;
    }

    public ConsoleOption(String displayName, String shortDesc, Runnable action,
            String nextMenu) {
        _displayName = displayName;
        _optionName = stripShortcut(displayName);
        _shortcut = extractShortcut(displayName);
        _shortDesc = shortDesc;
        _longDesc = "";
        _action = action;
        _nextMenu = nextMenu;
    }

    public ConsoleOption(String displayName, String shortDesc, String longDesc,
            Runnable action, String nextMenu) {
        _displayName = displayName;
        _optionName = stripShortcut(displayName);
        _shortcut = extractShortcut(displayName);
        _shortDesc = shortDesc;
        _longDesc = longDesc;
        _action = action;
        _nextMenu = nextMenu;
    }

    private static String extractShortcut(String name) {
        int lbpos = name.indexOf('[');
        int rbpos = name.indexOf(']', lbpos+1);
        if (lbpos >= 0 && rbpos == lbpos + 2) {
            return name.substring(lbpos+1, lbpos+2);
        } else {
            return "";
        }
    }

    private static String stripShortcut(String name) {
        return name.replace("[", "").replace("]", "");
    }

    public String getDisplayName() { return _displayName; }
    public String getOptionName() { return _optionName; }
    public String getShortcut() { return _shortcut; }
    public String getShortDesc() { return _shortDesc; }
    public String getLongDesc() { return _longDesc; }
    public String nextMenu() { return _nextMenu; }

    public void run() {
        _action.run();
    }
}
