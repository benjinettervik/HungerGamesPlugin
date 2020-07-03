package com.benjnet.hungergames;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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

        if (main.hgPlayersManager.findHGPlayer(sender) == null) {
            return false;
        }

        HGPlayer senderHgPlayer = main.hgPlayersManager.findHGPlayer(sender);

        System.out.println(senderHgPlayer.pendingInvite);

        if (command.getName().equalsIgnoreCase("a") || command.getName().equalsIgnoreCase("d")) {
            if (senderHgPlayer.pendingInvite != null) {
                if (command.getName().equalsIgnoreCase("a")) {
                    senderHgPlayer.teamInviteResponse(true);
                } else if (command.getName().equalsIgnoreCase("d")) {
                    senderHgPlayer.teamInviteResponse(false);
                }
            } else {
                senderHgPlayer.player.sendMessage(ChatColor.RED + "You have no pending invite.");
            }
        }

        if (command.getName().equalsIgnoreCase("ready")) {
            if (senderHgPlayer.team == null) {
                senderHgPlayer.player.sendMessage(ChatColor.RED + "You must be in a team to ready up.");
                return true;
            }
            senderHgPlayer.setReadyStatus(!senderHgPlayer.ready);
            main.hgLobbyManager.startGame();
        }

        return true;
    }

    //Check if player has HGPlayer profile
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (findHGPlayer(player) == null) {
            addNewHGPlayer(player);
        }

        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        main.hgLobbyManager.teleportToSpawn(player);
        main.hgLobbyManager.giveWelcomeBook(findHGPlayer(player));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        HGPlayer hgPlayer = findHGPlayer(e.getPlayer());
        if(hgPlayer.team != null){
            hgPlayer.team.removePlayer(hgPlayer);
        }

        int index = 0;
        for (HGPlayer hgPlayerInArray : hgPlayers) {
            if (hgPlayerInArray.name.equals(hgPlayer.name)) {
                hgPlayers.remove(index);
                break;
            }
            index++;
        }
    }

    //Find HGPlayer related to player
    public HGPlayer findHGPlayer(Player player) {
        for (HGPlayer hgPlayer : hgPlayers) {
            if (hgPlayer.player == player) {
                return hgPlayer;
            }
        }
        return null;
    }

    public void addNewHGPlayer(Player player) {
        HGPlayer hgPlayer = new HGPlayer(player, main);
        hgPlayers.add(hgPlayer);
        System.out.println("adding player to HGPlayers");
    }

    public void assignCurrentPlayers() {
        for (Player player : main.getServer().getOnlinePlayers()) {
            if (findHGPlayer(player) == null) {
                addNewHGPlayer(player);
            }
        }
    }
}
