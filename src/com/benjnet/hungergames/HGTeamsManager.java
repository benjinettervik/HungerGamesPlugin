package com.benjnet.hungergames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class HGTeamsManager implements Listener {
    Main main;
    public List<HGTeam> hgTeams = new ArrayList<HGTeam>();
    String[] possibleCommands = {"info", "invite", "leave"};

    public HGTeamsManager(Main _main) {
        main = _main;
    }

    public void onCommand(String[] args, HGPlayer senderHgPlayer) {
        if (args.length > 1) {

            if (args[1].equalsIgnoreCase("create")) {
                if (args.length > 2) {
                    createTeam(args[2], senderHgPlayer);

                } else {
                    senderHgPlayer.player.sendMessage(ChatColor.RED + "Cannot create team without name.");
                }
            }

            if (senderHgPlayer.team != null) {

                if (args[1].equalsIgnoreCase("info")) {
                    senderHgPlayer.team.info(senderHgPlayer);

                } else if (args[1].equalsIgnoreCase("invite")) {

                    if (args.length > 2) {

                        if ((main.hgPlayersManager.findHGPlayer(Bukkit.getPlayer(args[2]))) != null) {
                            HGPlayer invitedHgPlayer = main.hgPlayersManager.findHGPlayer(Bukkit.getPlayer(args[2]));
                            senderHgPlayer.team.invitePlayer(senderHgPlayer, invitedHgPlayer);
                        } else {
                            senderHgPlayer.player.sendMessage(ChatColor.RED + "Player is not online.");
                        }
                    }
                }

                if (args[1].equalsIgnoreCase("leave")) {
                    senderHgPlayer.team.removePlayer(senderHgPlayer);
                }
            } else {
                senderHgPlayer.player.sendMessage(ChatColor.RED + "You are not in a team.");
            }
        }
    }

    void createTeam(String name, HGPlayer creator) {

        for (HGTeam hgTeam : hgTeams) {
            if (hgTeam.name.equalsIgnoreCase(name)) {
                creator.player.sendMessage("" + ChatColor.BOLD + ChatColor.RED + name + " already exists!");
                return;
            }
        }

        if (creator.team != null) {
            creator.team.removePlayer(creator);
        }
        HGTeam hgTeam = new HGTeam(name, creator, main);
    }

    HGTeam findHGTeam(String teamName) {
        for (HGTeam hgTeam : hgTeams) {
            if (hgTeam.name.equalsIgnoreCase(teamName)) {
                return hgTeam;
            }
        }

        return null;
    }
}
