package com.benjnet.hungergames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Set;

public class HGScoreboardManager{
    Main main;
    ScoreboardManager sm;
    Scoreboard board;

    Objective objMatch;

    int matchTimeSeconds;
    int invincibilityTimeSeconds;
    int roamingTimeSeconds;
    int radius;

    public void OnCommand(String[] args, HGPlayer senderHgPlayer) {
        if (args.length < 2) {
            if (senderHgPlayer.enableScoreBoard) {
                senderHgPlayer.player.setScoreboard(sm.getNewScoreboard());
                senderHgPlayer.enableScoreBoard = false;
            } else {
                senderHgPlayer.player.setScoreboard(board);
                senderHgPlayer.enableScoreBoard = true;
            }
        }
    }

    public HGScoreboardManager(Main _main) {
        main = _main;
    }

    public void UpdateScoreboardLobby() {
        if (main.hgLobbyManager.gameIsStarted) {
            return;
        }

        objMatch.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Hunger Games");

        //clear all scores
        Set<String> scores = board.getEntries();
        for (String score : scores) {
            board.resetScores(score);
        }

        int indexCount = 0;
        for (HGTeam hgTeam : main.hgTeamsManager.hgTeams) {
            indexCount++;
            for (HGPlayer _player : hgTeam.hgPlayersInTeam) {
                indexCount++;
            }
        }

        int index = indexCount;
        for (HGTeam hgTeam : main.hgTeamsManager.hgTeams) {
            index--;
            Score team = objMatch.getScore(ChatColor.AQUA + "" + ChatColor.BOLD + hgTeam.name);
            team.setScore(index);

            for (HGPlayer _hgPlayer : hgTeam.hgPlayersInTeam) {
                index--;

                String readyStatus;
                if (_hgPlayer.ready) {
                    readyStatus = ChatColor.GREEN + "Ready";
                } else {
                    readyStatus = ChatColor.RED + "Not ready";
                }

                Score _player = objMatch.getScore(_hgPlayer.name + " | " + readyStatus);
                _player.setScore(index);
            }
        }

        matchTimeSeconds = main.hgLobbyManager.matchTime;
        Score matchTime = objMatch.getScore(ChatColor.GOLD + "Time until Firezone: " + matchTimeSeconds / 60 + " minutes");
        matchTime.setScore(indexCount + 4);

        invincibilityTimeSeconds = main.hgLobbyManager.invincibilityTime;
        Score invincibilityTime = objMatch.getScore(ChatColor.GOLD + "Invincibility time: " + invincibilityTimeSeconds / 60 + " minutes");
        invincibilityTime.setScore(indexCount + 3);

        roamingTimeSeconds = main.hgLobbyManager.roamingTime;
        Score roamingTime = objMatch.getScore(ChatColor.GOLD + "Roaming time: " + roamingTimeSeconds / 60 + " minutes");
        roamingTime.setScore(indexCount + 2);

        radius = main.hgLobbyManager.radius;
        Score radiusBlocks = objMatch.getScore(ChatColor.GOLD + "Firezone radius: " + radius + " blocks");
        radiusBlocks.setScore(indexCount + 1);

        objMatch.getScore(ChatColor.RED + "=======================").setScore(indexCount);
    }

    public void UpdateScoreBoardMatch() {
        Set<String> scores = board.getEntries();
        for (String score : scores) {
            board.resetScores(score);
        }

        int indexCount = 0;
        for (HGTeam hgTeam : main.hgTeamsManager.hgTeams) {
            indexCount++;
            for (HGPlayer _player : hgTeam.hgPlayersInTeam) {
                indexCount++;
            }
        }

        int index = indexCount;
        for (HGTeam hgTeam : main.hgTeamsManager.hgTeams) {
            index--;
            if (hgTeam.IsTeamDead()) {
                Score team = objMatch.getScore(ChatColor.GRAY + "" + ChatColor.BOLD + hgTeam.name);
                team.setScore(index);
            } else {
                Score team = objMatch.getScore(ChatColor.AQUA + "" + ChatColor.BOLD + hgTeam.name);
                team.setScore(index);
            }

            for (HGPlayer _hgPlayer : hgTeam.hgPlayersInTeam) {
                index--;

                if (_hgPlayer.isAlive) {
                    objMatch.getScore(ChatColor.GREEN + _hgPlayer.name).setScore(index);
                } else {
                    objMatch.getScore(ChatColor.GRAY + _hgPlayer.name).setScore(index);
                }
            }
        }
    }

    void SetupScoreboard() {
        sm = Bukkit.getScoreboardManager();
        board = sm.getNewScoreboard();
        objMatch = board.registerNewObjective("HGSTATS", "dummy");
        objMatch.setDisplaySlot(DisplaySlot.SIDEBAR);

        UpdateScoreboardLobby();

        if (main.hgLobbyManager.gameIsStarted) {
            SetCountdownScheduler();
        }
    }

    int timer = 60;

    void SetCountdownScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                objMatch.setDisplayName(ChatColor.GOLD + "Match time: " + ChatColor.BOLD + String.valueOf(timer) + ChatColor.GOLD + " seconds");
                timer--;

                if (timer < 0) {
                    timer = 0;
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("HungerGames"), 20, 20);
    }

    public void SetScoreboardTimer(HGLobbyManager.Stage stage, int timer) {
        int minutes = timer / 60;
        int seconds = timer % 60;

        if (stage == HGLobbyManager.Stage.ROAMING) {
            if (minutes > 0) {
                objMatch.setDisplayName(ChatColor.GOLD + "Roaming time: " + minutes + "min " + seconds + "sec");
            } else {
                objMatch.setDisplayName(ChatColor.GOLD + "Roaming time: " + seconds + "sec");
            }
        } else if (stage == HGLobbyManager.Stage.INVINCIBILITY) {
            if (minutes > 0) {
                objMatch.setDisplayName(ChatColor.GOLD + "Invincibility time: " + minutes + "min " + seconds + "sec");
            } else {
                objMatch.setDisplayName(ChatColor.GOLD + "Invincibility time: " + seconds + "sec");
            }
        } else if (stage == HGLobbyManager.Stage.MATCH) {
            objMatch.setDisplayName(ChatColor.GOLD + "Match time: " + timer);
            if (minutes > 0) {
                objMatch.setDisplayName(ChatColor.GOLD + "Match time: " + minutes + "min " + seconds + "sec");
            } else {
                objMatch.setDisplayName(ChatColor.GOLD + "Match time: " + seconds + "sec");
            }
        } else if (stage == HGLobbyManager.Stage.FIGHT_PERIOD) {
            if (minutes > 0) {
                objMatch.setDisplayName(ChatColor.RED + "Fight time: " + minutes + "min " + seconds + "sec");
            } else {
                objMatch.setDisplayName(ChatColor.RED + "Fight time: " + seconds + "sec");
            }
        }
    }
}
