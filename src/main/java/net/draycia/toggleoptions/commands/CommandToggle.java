package net.draycia.toggleoptions.commands;

import com.google.common.collect.ImmutableList;
import net.draycia.toggleoptions.ToggleOptions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CommandToggle implements TabExecutor {
    
    private ToggleOptions pluginRef;
    private final ArrayList<String> tabArg1;
    private final ArrayList<String> tabArg2;

    public CommandToggle(ToggleOptions toggleOptions)
    {
        this.pluginRef = toggleOptions;
        tabArg1 = buildSuggestionsArgOne();
        tabArg2 = buildSuggestionsArgTwo();
    }

    private ArrayList<String> buildSuggestionsArgOne() {
        ArrayList<String> suggestions = new ArrayList<>();
        suggestions.addAll(pluginRef.getConfig().getConfigurationSection("Toggles").getKeys(false));
        return suggestions;
    }

    private ArrayList<String> buildSuggestionsArgTwo() {
        ArrayList<String> suggestions = new ArrayList<>();
        suggestions.add("on");
        suggestions.add("off");
        return suggestions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 2) return true;

        if (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
            String notOnOff = pluginRef.getConfig().getString("Messages.NotOnOff");
            if (notOnOff == null) notOnOff = "&cInvalid argument! Must supply \"on\" or \"off\".";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', notOnOff));
            return true;
        }

        ConfigurationSection configSection = pluginRef.getConfig().getConfigurationSection("Toggles");

        if (configSection == null) {
            String missingSection = ChatColor.translateAlternateColorCodes('&', "Toggles section missing!");
            sender.sendMessage(missingSection);
            pluginRef.getLogger().log(Level.WARNING, missingSection);
            return true;
        }

        ConfigurationSection toggleEntry = null;

        for (String entry : configSection.getKeys(false)) {
            if (entry.equalsIgnoreCase(args[0])) toggleEntry = configSection.getConfigurationSection(entry);
        }

        if (toggleEntry == null) {
            String invalidOption = pluginRef.getConfig().getString("Messages.InvalidOption");
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
                    ToggleOptions.getPermissions().playerAdd(player.getWorld().getName(), player, toggleEntry.getString("Toggle"));
                } else {
                    ToggleOptions.getPermissions().playerAdd(null, player, toggleEntry.getString("Toggle"));
                }
            }

        } else if (args[1].equalsIgnoreCase("off")) {
            String requiredPermission = toggleEntry.getString("RequiredOff");

            if (requiredPermission == null || requiredPermission.isEmpty() || sender.hasPermission(requiredPermission)) {
                System.out.println("off");
                if (toggleEntry.getBoolean("WorldSpecific")) {
                    ToggleOptions.getPermissions().playerRemove(player.getWorld().getName(), player, toggleEntry.getString("Toggle"));
                } else {
                    ToggleOptions.getPermissions().playerRemove(null, player, toggleEntry.getString("Toggle"));
                }
            }
        }

        System.out.println("return true;");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ConfigurationSection configSection = pluginRef.getConfig().getConfigurationSection("Toggles");

        if (configSection == null) return null;

        if(args.length > 0)
        {
            if(args.length == 2)
            {
                return StringUtil.copyPartialMatches(args[1], tabArg2, new ArrayList<String>());
            } else {
                return StringUtil.copyPartialMatches(args[0], tabArg1, new ArrayList<String>());
            }
        } else {
            return ImmutableList.copyOf(new ArrayList<String>());
        }
    }
}
