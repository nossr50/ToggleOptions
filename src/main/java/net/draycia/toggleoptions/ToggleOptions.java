package net.draycia.toggleoptions;

import net.draycia.toggleoptions.commands.CommandToggle;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ToggleOptions extends JavaPlugin {

    public static ToggleOptions instance;
    public static Permission permissions;

    @Override
    public void onEnable() {
        ToggleOptions.instance = this;

        saveDefaultConfig();

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permissions = rsp.getProvider();

        TabExecutor toggleCommand = new CommandToggle();
        getCommand("toggle").setExecutor(toggleCommand);
        getCommand("toggle").setTabCompleter(toggleCommand);
    }
}
