package dev.jetlag;

import dev.jetlag.events.JetLagEvent;
import dev.jetlag.util.Messages;
import dev.jetlag.util.Stats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * /jetlag help | stats | reload | on | off | chance &lt;0-100&gt; | test &lt;event&gt; [player]
 */
public final class JetLagCommand implements CommandExecutor, TabCompleter {

    private static final List<String> ADMIN_SUBS = Arrays.asList("reload", "on", "off", "chance", "test");

    private final JetLagPlugin plugin;

    public JetLagCommand(JetLagPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages msg = plugin.messages();
        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);

        if (ADMIN_SUBS.contains(sub) && !sender.hasPermission("jetlag.admin")) {
            msg.send(sender, "no-permission");
            return true;
        }

        switch (sub) {
            case "stats":
                if (!sender.hasPermission("jetlag.stats")) {
                    msg.send(sender, "no-permission");
                    return true;
                }
                showStats(sender);
                return true;
            case "reload":
                plugin.reloadEverything();
                msg.send(sender, "reloaded", "count", String.valueOf(plugin.registry().enabledCount()));
                return true;
            case "on":
            case "off":
                plugin.getConfig().set("enabled", sub.equals("on"));
                plugin.saveConfig();
                plugin.reloadEverything();
                msg.send(sender, sub.equals("on") ? "enabled" : "disabled");
                return true;
            case "chance":
                setChance(sender, args);
                return true;
            case "test":
                test(sender, args);
                return true;
            default:
                for (String line : msg.get("help").split("\n")) {
                    sender.sendMessage(line);
                }
                return true;
        }
    }

    private void showStats(CommandSender sender) {
        Messages msg = plugin.messages();
        if (sender instanceof Player) {
            msg.send(sender, "stats-self", "count", String.valueOf(plugin.stats().get((Player) sender)));
        }
        sender.sendMessage(msg.get("stats-header"));
        int rank = 1;
        for (Stats.Entry e : plugin.stats().top(5)) {
            sender.sendMessage(msg.get("stats-line", "rank", String.valueOf(rank++),
                    "player", e.name, "count", String.valueOf(e.count)));
        }
    }

    private void setChance(CommandSender sender, String[] args) {
        Messages msg = plugin.messages();
        if (args.length < 2) {
            msg.send(sender, "chance-current", "chance", format(plugin.settings().chancePercent));
            return;
        }
        double value;
        try {
            value = Double.parseDouble(args[1].replace("%", "").replace(',', '.'));
        } catch (NumberFormatException ex) {
            msg.send(sender, "chance-invalid");
            return;
        }
        if (value < 0 || value > 100) {
            msg.send(sender, "chance-invalid");
            return;
        }
        plugin.getConfig().set("chance", value);
        plugin.saveConfig();
        plugin.reloadEverything();
        msg.send(sender, "chance-set", "chance", format(value));
    }

    private void test(CommandSender sender, String[] args) {
        Messages msg = plugin.messages();
        if (args.length < 2) {
            msg.send(sender, "test-usage", "events", String.join(", ", plugin.registry().ids()));
            return;
        }
        JetLagEvent event = plugin.registry().byId(args[1].toLowerCase(Locale.ROOT));
        if (event == null) {
            msg.send(sender, "test-unknown", "events", String.join(", ", plugin.registry().ids()));
            return;
        }
        Player target;
        if (args.length >= 3) {
            target = plugin.getServer().getPlayerExact(args[2]);
        } else {
            target = sender instanceof Player ? (Player) sender : null;
        }
        if (target == null) {
            msg.send(sender, "player-not-found");
            return;
        }
        if (plugin.registry().trigger(event, target)) {
            msg.send(sender, "test-ok", "event", event.id(), "player", target.getName());
        } else {
            msg.send(sender, "test-cannot-run", "event", event.id());
        }
    }

    private static String format(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        boolean admin = sender.hasPermission("jetlag.admin");

        if (args.length == 1) {
            options.add("help");
            options.add("stats");
            if (admin) options.addAll(ADMIN_SUBS);
        } else if (args.length == 2 && admin && args[0].equalsIgnoreCase("test")) {
            options.addAll(plugin.registry().ids());
        } else if (args.length == 2 && admin && args[0].equalsIgnoreCase("chance")) {
            options.addAll(Arrays.asList("5", "10", "15", "25", "50", "100"));
        } else if (args.length == 3 && admin && args[0].equalsIgnoreCase("test")) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                options.add(p.getName());
            }
        } else {
            return Collections.emptyList();
        }

        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(typed)) matches.add(o);
        }
        return matches;
    }
}
