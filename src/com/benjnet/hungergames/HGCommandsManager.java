package com.benjnet.hungergames;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HGCommandsManager implements CommandExecutor {
    Main main;

    public HGCommandsManager(Main _main) {
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

        if(main.hgLobbyManager.gameIsStarted){
            senderHgPlayer.player.sendMessage(ChatColor.RED + "Cannot perform commands when in-game.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("hg")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("team")) {
                    main.hgTeamsManager.OnCommand(args, senderHgPlayer);
                }
                else if(args[0].equalsIgnoreCase("matchtime") || args[0].equalsIgnoreCase("safetime") || args[0].equalsIgnoreCase("roamingtime") || args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("radius") || args[0].equalsIgnoreCase("default")){
                    main.hgLobbyManager.OnCommand(args, senderHgPlayer);
                }
                else if(args[0].equalsIgnoreCase("scoreboard")){
                    main.hgScoreboardManager.OnCommand(args, senderHgPlayer);
                }
                else if(args[0].equalsIgnoreCase("help")){
                    sender.sendMessage(ChatColor.GOLD + "Possible commands:");
                    sender.sendMessage(ChatColor.AQUA + "Team:");
                    sender.sendMessage(ChatColor.GREEN + "/hg team create <name>");
                    sender.sendMessage(ChatColor.GREEN + "/hg team leave");
                    sender.sendMessage(ChatColor.GREEN + "/hg team invite <player>");
                    sender.sendMessage(ChatColor.AQUA + "Lobby:");
                    sender.sendMessage(ChatColor.GREEN + "/hg roamingtime <time in minutes>");
                    sender.sendMessage(ChatColor.GREEN + "/hg safetime <time in minutes>");
                    sender.sendMessage(ChatColor.GREEN + "/hg matchtime <time in minutes>");
                    sender.sendMessage(ChatColor.GREEN + "/hg radius <radius in blocks>");
                    sender.sendMessage(ChatColor.AQUA + "Miscellaneous:");
                    sender.sendMessage(ChatColor.GREEN + "/hg scoreboard");
                    sender.sendMessage(ChatColor.GREEN + "/ready");
                }
            }
        }
        return true;
    }
}
