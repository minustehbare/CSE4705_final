package CSE4705_final.Console;

import java.util.*;

/**
 *
 * @author Ethan Levine
 */
public class ConsoleMenu {

    private final List<ConsoleOption> _options;
    private final String _menuName;
    private int _longestOptionName = 0;

    private static final String GENERAL_HELP = "You can either:\n* Type the shortcut letter\n* Type enough of an option to make it unique\n* Prefix either of the above with '?' to get help.";

    public ConsoleMenu(String menuName) {
        _menuName = menuName;
        _options = new ArrayList<ConsoleOption>();
    }

    public void addOption(ConsoleOption opt) {
        _options.add(opt);
        if (opt.getDisplayName().length() > _longestOptionName) {
            _longestOptionName = opt.getDisplayName().length();
        }
    }

    private static String buildBuffer(char c, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    public String getPrintout() {
        StringBuilder builder = new StringBuilder();
        builder.append("--");
        builder.append(buildBuffer('-', _menuName.length()));
        builder.append("--");
        String menuHeader = builder.toString();
        builder.append("\n- ");
        builder.append(_menuName);
        builder.append(" -\n");
        builder.append(menuHeader);
        builder.append('\n');
        // items
        for (ConsoleOption opt : _options) {
            builder.append("\n ");
            builder.append(opt.getDisplayName());
            builder.append(buildBuffer(' ', _longestOptionName - opt.getDisplayName().length() + 1));
            if (!opt.getShortDesc().equals("")) {
                builder.append("- ");
                builder.append(opt.getShortDesc());
            }
        }
        return builder.toString();
    }

    public String handleInput(String input) {
        boolean helpReq = (input.charAt(0) == '?');
        String stdInput = (helpReq ? input.substring(1) : input);
        if (stdInput.equals("")) {
            // Genreal help
            System.out.println(GENERAL_HELP);
            return "REPEAT";
        } else {
            // check shortcuts
            for (ConsoleOption opt : _options) {
                if (stdInput.toLowerCase().equals(opt.getShortcut().toLowerCase())) {
                    if (helpReq) {
                        System.out.println(opt.getLongDesc());
                        return "REPEAT";
                    } else {
                        opt.run();
                        return opt.nextMenu();
                    }
                }
            }
            // Now we need to check for the most specific case...
            Set<ConsoleOption> opts = new HashSet<ConsoleOption>();
            for (ConsoleOption opt : _options) {
                if (opt.getOptionName().toLowerCase().startsWith(stdInput.toLowerCase())) {
                    opts.add(opt);
                }
            }
            if (opts.isEmpty()) {
                System.out.println("The input did not match any options.");
                return "REPEAT";
            } else if (opts.size() == 1) {
                ConsoleOption opt = opts.iterator().next();
                if (helpReq) {
                    System.out.println(opt.getLongDesc());
                    return "REPEAT";
                } else {
                    opt.run();
                    return opt.nextMenu();
                }
            } else {
                System.out.println("Too many possibilities:");
                for (ConsoleOption opt : opts) {
                    System.out.println(opt.getOptionName());
                }
                return "REPEAT";
            }
        }
    }
}
