package com.benjnet.hungergames;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    PluginManager pm = getServer().getPluginManager();
    Plugin plugin;

    public HGTeamsManager hgTeamsManager = new HGTeamsManager(this);
    public HGPlayersManager hgPlayersManager = new HGPlayersManager(this);
    public HGScoreboardManager hgScoreboardManager = new HGScoreboardManager(this);
    public HGCommandsManager hgCommandsManager = new HGCommandsManager(this);
    public HGLobbyManager hgLobbyManager = new HGLobbyManager(this);
    public PluginConfig pluginConfig = new PluginConfig(this);
    public HGWorldManager hgWorldManager = new HGWorldManager(this);

    @Override
    public void onEnable() {
        getCommand("hg").setExecutor(hgCommandsManager);
        getCommand("ready").setExecutor(hgPlayersManager);
        getCommand("a").setExecutor(hgPlayersManager);
        getCommand("d").setExecutor(hgPlayersManager);

        plugin = pm.getPlugin("HungerGames");

        pm.registerEvents(hgPlayersManager, this);
        pm.registerEvents(hgLobbyManager, this);
        pm.registerEvents(hgCommandsManager, this);

        hgScoreboardManager.setupScoreboard();

        hgPlayersManager.assignCurrentPlayers();
    }

    @Override
    public void onDisable(){
        hgWorldManager.deleteHGWorlds();
    }
}