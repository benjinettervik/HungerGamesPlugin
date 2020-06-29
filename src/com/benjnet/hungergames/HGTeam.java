package com.benjnet.hungergames;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class HGTeam {

    public String name;
    public HGPlayer owner;
    public List<HGPlayer> hgPlayersInTeam = new ArrayList<HGPlayer>();

    public Boolean teamIsDead;

    Main main;

    public HGTeam(String _name, HGPlayer _owner, Main _main) {
        main = _main;

        name = _name;
        owner = _owner;

        main.hgTeamsManager.hgTeams.add(this);

        //do not want to send join message to player, adding manually
        _owner.team = this;
        AddPlayer(_owner);

        owner.player.sendMessage(ChatColor.GREEN + "Team " + ChatColor.AQUA + ChatColor.BOLD + name + ChatColor.GREEN + " has been created!");
    }

    public void AddPlayer(HGPlayer hgPlayer) {

        for (HGPlayer hgPlayerInTeam : hgPlayersInTeam) {
            hgPlayerInTeam.player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + hgPlayer.name + ChatColor.GREEN + " has joined your team!");
        }

        hgPlayer.team = this;
        hgPlayersInTeam.add(hgPlayer);

        if (hgPlayer != owner){
            hgPlayer.player.sendMessage(ChatColor.GREEN + "You have joined " + ChatColor.AQUA + ChatColor.BOLD + name + ChatColor.GREEN + ".");
        }

        main.hgScoreboardManager.UpdateScoreboardLobby();
    }

    public void RemovePlayer(HGPlayer hgPlayer) {
        hgPlayersInTeam.remove(hgPlayer);
        hgPlayer.team = null;
        hgPlayer.player.sendMessage(ChatColor.RED + "You have left " + ChatColor.AQUA + ChatColor.BOLD + name + ChatColor.RED + ".");

        for (HGPlayer hgPlayerInTeam : hgPlayersInTeam) {
            hgPlayerInTeam.player.sendMessage(ChatColor.RED + hgPlayer.name + " has left your team.");
        }

        if (hgPlayersInTeam.size() == 0) {
            System.out.println("team empty");
            main.hgTeamsManager.hgTeams.remove(this);
        }

        main.hgScoreboardManager.UpdateScoreboardLobby();
    }

    public void InvitePlayer(HGPlayer sender, HGPlayer receiver) {
        if (receiver == null) {
            sender.player.sendMessage(ChatColor.RED + "Player is not online.");
            return;
        }

        for (HGPlayer hgPlayer : hgPlayersInTeam) {
            if (hgPlayer == receiver) {
                sender.player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + receiver.name + ChatColor.RED + " is already in team.");
                return;
            }
        }

        sender.player.sendMessage(ChatColor.GREEN + "You have invited "
                + ChatColor.AQUA + ChatColor.BOLD + receiver.name + ChatColor.GREEN + " to join " + ChatColor.AQUA + ChatColor.BOLD + sender.team.name + ChatColor.GREEN + ".");

        receiver.player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + sender.name + ChatColor.GREEN + " has invited you to join " + ChatColor.BOLD + name + "."
                + ChatColor.RESET + ChatColor.GREEN + "\nType " + ChatColor.GOLD + "/a" + ChatColor.GREEN + " to accept or " + ChatColor.GOLD + "/d" + ChatColor.GREEN + " to decline.");

        receiver.pendingInvite = this;
        receiver.pendingInviter = sender;
    }

    public void Info(HGPlayer hgPlayer) {
        hgPlayer.player.sendMessage("");
        hgPlayer.player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "Owner: " + ChatColor.AQUA + owner.player.getName());
        hgPlayer.player.sendMessage("");

        hgPlayer.player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "Players in team: ");
        for (HGPlayer hgPlayerInTeam : hgPlayersInTeam) {
            hgPlayer.player.sendMessage(ChatColor.AQUA + hgPlayerInTeam.player.getName());
        }
    }

    public Boolean IsTeamDead(){
        for (HGPlayer hgPlayer: hgPlayersInTeam) {
            if(hgPlayer.isAlive){
                return false;
            }
        }
        teamIsDead = true;
        return true;
    }
}
