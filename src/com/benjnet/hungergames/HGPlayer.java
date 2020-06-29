package com.benjnet.hungergames;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HGPlayer {

    Main main;

    Boolean isInGame = false;

    Player player;
    HGTeam team;
    String name;
    Boolean ready = false;
    Boolean enableScoreBoard = true;
    Boolean isAlive = true;
    HGTeam pendingInvite;
    HGPlayer pendingInviter;


    public HGPlayer(Player _player, Main _main){
        player = _player;
        name = player.getName();

        main = _main;

        player.setScoreboard(main.hgScoreboardManager.board);
    }

    public void TeamInviteResponse(boolean accept){
        if(accept){
            pendingInvite.AddPlayer(this);
        }
        else{
            pendingInviter.player.sendMessage(ChatColor.RED + name + " declined your invitation.");
        }

        pendingInvite = null;
        pendingInviter = null;
    }

    public void SetReadyStatus(Boolean _ready){
        ready = _ready;
        if(ready){
            player.sendMessage(ChatColor.GREEN + "You are ready!");
        }
        else{
            player.sendMessage(ChatColor.RED + "You are no longer ready.");
        }

        main.hgScoreboardManager.UpdateScoreboardLobby();
    }
}
