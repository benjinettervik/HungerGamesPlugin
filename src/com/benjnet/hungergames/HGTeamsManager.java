package com.benjnet.hungergames;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HGTeamsManager implements Listener {
    Main main;
    public List<HGTeam> hgTeams = new ArrayList<HGTeam>();
    String[] possibleCommands = {"info", "invite", "leave"};

    public HGTeamsManager(Main _main) {
        main = _main;
    }

    public void OnCommand(String[] args, HGPlayer senderHgPlayer) {
        if (args.length > 1) {

            if (args[1].equalsIgnoreCase("create")) {
                if (args.length > 2) {
                    CreateTeam(args[2], senderHgPlayer);

                } else {
                    senderHgPlayer.player.sendMessage(ChatColor.RED + "Cannot create team without name.");
                }
            }

            if (senderHgPlayer.team != null) {

                if (args[1].equalsIgnoreCase("info")) {
                    senderHgPlayer.team.Info(senderHgPlayer);

                } else if (args[1].equalsIgnoreCase("invite")) {

                    if (args.length > 2) {

                        if ((main.hgPlayersManager.FindHGPlayer(Bukkit.getPlayer(args[2]))) != null) {
                            HGPlayer invitedHgPlayer = main.hgPlayersManager.FindHGPlayer(Bukkit.getPlayer(args[2]));
                            senderHgPlayer.team.InvitePlayer(senderHgPlayer, invitedHgPlayer);
                        } else {
                            senderHgPlayer.player.sendMessage(ChatColor.RED + "Player is not online.");
                        }
                    }
                }

                if (args[1].equalsIgnoreCase("leave")) {
                    senderHgPlayer.team.RemovePlayer(senderHgPlayer);
                }
            } else {
                senderHgPlayer.player.sendMessage(ChatColor.RED + "You are not in a team.");
            }
        }
    }

    void CreateTeam(String name, HGPlayer creator) {

        for (HGTeam hgTeam : hgTeams) {
            if (hgTeam.name.equalsIgnoreCase(name)) {
                creator.player.sendMessage("" + ChatColor.BOLD + ChatColor.RED + name + " already exists!");
                return;
            }
        }

        if (creator.team != null) {
            creator.team.RemovePlayer(creator);
        }
        HGTeam hgTeam = new HGTeam(name, creator, main);
    }

    HGTeam FindHGTeam(String teamName) {
        for (HGTeam hgTeam : hgTeams) {
            if (hgTeam.name.equalsIgnoreCase(teamName)) {
                return hgTeam;
            }
        }

        return null;
    }
}
