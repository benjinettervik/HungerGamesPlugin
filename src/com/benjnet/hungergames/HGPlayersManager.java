package com.benjnet.hungergames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class HGPlayersManager implements Listener, CommandExecutor {

    Main main;

    public List<HGPlayer> hgPlayers = new ArrayList<HGPlayer>();

    public HGPlayersManager(Main _main) {
        main = _main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player sender = (Player) commandSender;

        if (main.hgPlayersManager.FindHGPlayer(sender) == null) {
            return false;
        }

        HGPlayer senderHgPlayer = main.hgPlayersManager.FindHGPlayer(sender);

        System.out.println(senderHgPlayer.pendingInvite);

        if (command.getName().equalsIgnoreCase("a") || command.getName().equalsIgnoreCase("d")) {
            if (senderHgPlayer.pendingInvite != null) {
                if (command.getName().equalsIgnoreCase("a")) {
                    senderHgPlayer.TeamInviteResponse(true);
                } else if (command.getName().equalsIgnoreCase("d")) {
                    senderHgPlayer.TeamInviteResponse(false);
                }
            } else {
                senderHgPlayer.player.sendMessage(ChatColor.RED + "You have no pending invite.");
            }
        }

        if (command.getName().equalsIgnoreCase("ready")) {
            if(senderHgPlayer.team == null){
                senderHgPlayer.player.sendMessage(ChatColor.RED + "You must be in a team to ready up.");
                return true;
            }
            senderHgPlayer.SetReadyStatus(!senderHgPlayer.ready);
            main.hgLobbyManager.StartGame();
        }

        return true;
    }

    //Check if player has HGPlayer profile
    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (FindHGPlayer(player) == null) {
            AddNewHGPlayer(player);
        }

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        //player.teleport(new Location(Bukkit.getWorld("world"), 742, 100, -910));
        main.hgLobbyManager.GiveWelcomeBook(FindHGPlayer(player));
    }

    //Find HGPlayer related to player
    public HGPlayer FindHGPlayer(Player player) {
        for (HGPlayer hgPlayer : hgPlayers) {
            if (hgPlayer.player == player) {
                return hgPlayer;
            }
        }
        return null;
    }

    public void AddNewHGPlayer(Player player) {
        HGPlayer hgPlayer = new HGPlayer(player, main);
        hgPlayers.add(hgPlayer);
        System.out.println("adding player to HGPlayers");
    }

    public void AssignCurrentPlayers() {
        for (Player player : main.getServer().getOnlinePlayers()) {
            if (FindHGPlayer(player) == null) {
                AddNewHGPlayer(player);
            }
        }
    }
}
