package dev.bountyplugin.commands;

import dev.bountyplugin.BountyPlugin;
import dev.bountyplugin.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class BountyAdminCommand implements CommandExecutor, TabCompleter {

    private final BountyPlugin plugin;

    public BountyAdminCommand(BountyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager config = plugin.getConfigManager();

        if (!sender.hasPermission("bountyplugin.admin")) {
            config.send(sender, "no-permission");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(config.color("&cVerwendung: /bountyad reload"));
            return true;
        }

        config.reload();
        boolean economyReady = plugin.getEconomyManager().setup();

        config.send(sender, "reload-success");

        if (!economyReady) {
            config.send(sender, "no-economy");
        } else {
            sender.sendMessage(config.color("&7Economy-Provider: &f" + plugin.getEconomyManager().getProviderName()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                result.add("reload");
            }
            return result;
        }
        return new ArrayList<>();
    }
}
