package net.draycia.toggleoptions.commands;

import com.google.common.collect.ImmutableList;
import net.draycia.toggleoptions.ToggleOptions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CommandToggle implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 2) return true;

        if (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
            String notOnOff = ToggleOptions.instance.getConfig().getString("Messages.NotOnOff");
            if (notOnOff == null) notOnOff = "&cInvalid argument! Must supply \"on\" or \"off\".";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', notOnOff));
            return true;
        }

        ConfigurationSection configSection = ToggleOptions.instance.getConfig().getConfigurationSection("Toggles");

        if (configSection == null) {
            String missingSection = ChatColor.translateAlternateColorCodes('&', "Toggles section missing!");
            sender.sendMessage(missingSection);
            ToggleOptions.instance.getLogger().log(Level.WARNING, missingSection);
            return true;
        }

        ConfigurationSection toggleEntry = null;

        for (String entry : configSection.getKeys(false)) {
            if (entry.equalsIgnoreCase(args[0])) toggleEntry = configSection.getConfigurationSection(entry);
        }

        if (toggleEntry == null) {
            String invalidOption = ToggleOptions.instance.getConfig().getString("Messages.InvalidOption");
            if (invalidOption == null) invalidOption = "&cThat option is not valid!";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidOption));
            return true;
        }

        Player player = (Player)sender;

        if (args[1].equalsIgnoreCase("on")) {
            String requiredPermission = toggleEntry.getString("RequiredOn");

            if (requiredPermission == null || requiredPermission.isEmpty() || sender.hasPermission(requiredPermission)) {
                System.out.println("on");
                if (toggleEntry.getBoolean("WorldSpecific")) {
                    ToggleOptions.permissions.playerAdd(player.getWorld().getName(), player, toggleEntry.getString("Toggle"));
                } else {
                    ToggleOptions.permissions.playerAdd(null, player, toggleEntry.getString("Toggle"));
                }
            }

        } else if (args[1].equalsIgnoreCase("off")) {
            String requiredPermission = toggleEntry.getString("RequiredOff");

            if (requiredPermission == null || requiredPermission.isEmpty() || sender.hasPermission(requiredPermission)) {
                System.out.println("off");
                if (toggleEntry.getBoolean("WorldSpecific")) {
                    ToggleOptions.permissions.playerRemove(player.getWorld().getName(), player, toggleEntry.getString("Toggle"));
                } else {
                    ToggleOptions.permissions.playerRemove(null, player, toggleEntry.getString("Toggle"));
                }
            }
        }

        System.out.println("return true;");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ConfigurationSection configSection = ToggleOptions.instance.getConfig().getConfigurationSection("Toggles");

        if (configSection == null) return null;

        ArrayList<String> suggestions = new ArrayList<>();

        if (args.length >= 2) {
            suggestions.add("on");
            suggestions.add("off");
            return ImmutableList.copyOf(suggestions);
        }

        for (String key : configSection.getKeys(false)) {
            if (args.length == 1 && key.startsWith(args[0])) {
                suggestions.add(key);
                continue;
            }

            ConfigurationSection toggle = configSection.getConfigurationSection(key);
            if (toggle == null) return null;

            String required = toggle.getString("required");

            if (required != null && sender.hasPermission(required)) {
                String permToggle = toggle.getString("toggle");
                if (permToggle != null) suggestions.add(key);
            }
        }

        return ImmutableList.copyOf(suggestions);
    }
}
