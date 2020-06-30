package com.benjnet.hungergames;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    PluginManager pm = getServer().getPluginManager();
    Plugin plugin;

    public HungerGames hungerGames = new HungerGames(this);
    public HGTeamsManager hgTeamsManager = new HGTeamsManager(this);
    public HGPlayersManager hgPlayersManager = new HGPlayersManager(this);
    public HGScoreboardManager hgScoreboardManager = new HGScoreboardManager(this);
    public HGCommandsManager hgCommandsManager = new HGCommandsManager(this);
    public HGLobbyManager hgLobbyManager = new HGLobbyManager(this);

    @Override
    public void onEnable() {
        //Classes

        getLogger().info("Enabling HG..");
        //Register commands
        getCommand("hg").setExecutor(hgCommandsManager);
        getCommand("ready").setExecutor(hgPlayersManager);
        getCommand("a").setExecutor(hgPlayersManager);
        getCommand("d").setExecutor(hgPlayersManager);

        plugin = pm.getPlugin("HungerGames");

        //Register events
        pm.registerEvents(hungerGames, this);
        pm.registerEvents(hgPlayersManager, this);
        pm.registerEvents(hgLobbyManager, this);
        pm.registerEvents(hgCommandsManager, this);

        hgScoreboardManager.SetupScoreboard();
        hgPlayersManager.AssignCurrentPlayers();
    }

    @Override
    public void onDisable(){

    }
}