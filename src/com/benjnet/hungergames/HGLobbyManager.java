package com.benjnet.hungergames;

import com.onarandombox.MultiverseCore.MultiverseCore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class HGLobbyManager implements Listener {
    Main main;
    MultiverseCore core;

    public boolean gameIsStarted;
    Location spawn;

    //in seconds
    public int matchTime = 0 * 60;
    public int invincibilityTime = 0 * 60;
    public int roamingTime = 1 * 60;
    public int radius = 50;
    public int zoneDamageIntensity = 1;
    int waitTime = 0;
    int countdownTimer = 4;

    HGMatch hgMatch;
    ItemStack welcomeBook;

    public enum Stage {
        ROAMING,
        INVINCIBILITY,
        MATCH,
        FIGHT_PERIOD
    }

    BukkitTask countdown;
    BukkitTask waitForWorldCreation;

    public HGLobbyManager(Main _main) {
        main = _main;

        //using Multiverse to create worlds, because its a pain to manually link nether worlds


        welcomeBook = createWelcomeBook();
    }

    @EventHandler
    void foodLevelChangeEvent(FoodLevelChangeEvent e) {
        if (!gameIsStarted) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer() != null) {
            if (e.getPlayer().getGameMode() == GameMode.SURVIVAL && !gameIsStarted) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onPlayerDamage(EntityDamageEvent e) {
        if (!gameIsStarted && e.getEntityType() == EntityType.PLAYER) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onMobSpawn(EntitySpawnEvent e){
        if(!gameIsStarted){
            e.setCancelled(true);
        }
    }

    public void onCommand(String[] args, HGPlayer senderHgPlayer) {
        if (args.length > 1) {
            if (StringUtils.isNumeric(args[1])) {
                int value = Integer.parseInt(args[1]);
                int seconds = value * 60;

                if (args[0].equalsIgnoreCase("matchtime")) {
                    matchTime = seconds;
                    main.getServer().broadcastMessage(ChatColor.GREEN + "Match time has been set to " + seconds / 60 + " minutes.");
                } else if (args[0].equalsIgnoreCase("safetime")) {
                    invincibilityTime = seconds;
                    main.getServer().broadcastMessage(ChatColor.GREEN + "Invincibility time has been set to " + invincibilityTime / 60 + " minutes.");
                } else if (args[0].equalsIgnoreCase("roamingtime")) {
                    roamingTime = seconds;
                    main.getServer().broadcastMessage(ChatColor.GREEN + "Roaming time has been set to " + roamingTime / 60 + " minutes.");
                } else if (args[0].equalsIgnoreCase("radius")) {
                    radius = value;
                    main.getServer().broadcastMessage(ChatColor.GREEN + "Radius time has been set to " + value + " blocks");

                main.hgScoreboardManager.updateScoreboardLobby();
            }
        } else if (args[0].equalsIgnoreCase("default")) {
            matchTime = 60 * 60;
            invincibilityTime = 10 * 60;
            roamingTime = 2 * 60;
            radius = 50;
            zoneDamageIntensity = 1;

            senderHgPlayer.player.sendMessage(ChatColor.GREEN + "Settings have been set to default.");

            main.hgScoreboardManager.updateScoreboardLobby();
        }

        } else if (args[0].equalsIgnoreCase("setspawn")) {
            main.pluginConfig.setSpawn(senderHgPlayer.player);
        } else if (args[0].equalsIgnoreCase("spawn")) {
            teleportToSpawn(senderHgPlayer.player);
        }
    }

    public ItemStack createWelcomeBook() {
        ItemStack welcomeBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) welcomeBook.getItemMeta();
        bookMeta.setAuthor("ZweetDreams");
        bookMeta.setTitle(ChatColor.RED + "" + ChatColor.BOLD + "Hunger Games");
        bookMeta.addPage(
                "Welcome to" + ChatColor.RED + "" + ChatColor.BOLD + " Hunger Games!\n\n" +
                        ChatColor.RESET + "The game starts by spawning everyone into a completely random world, together.\n\n" +
                        "The game consists of" + ChatColor.BOLD + " four stages.\n\n"
        );
        bookMeta.addPage(
                ChatColor.RED + "" + ChatColor.BOLD + "Roaming stage\n\n" + ChatColor.RESET +
                        "The first stage - Roaming Stage - is where every player is able to run around freely without being able to break blocks, damage, or take damage."
        );
        bookMeta.addPage(
                ChatColor.RED + "" + ChatColor.BOLD + "Invincibility stage\n\n" + ChatColor.RESET +
                        "When the Roaming stage ends, everyone gets teleported to the center of the world.\n\n" +
                        "During the invincibility stage you are able to break blocks freely, but no player can harm you."
        );
        bookMeta.addPage(
                ChatColor.RED + "" + ChatColor.BOLD + "Match stage\n\n" + ChatColor.RESET +
                        "When the Invincibility stage ends, the real match starts. Players can now kill each other."
        );
        bookMeta.addPage(
                ChatColor.RED + "" + ChatColor.BOLD + "Firezone stage\n\n" + ChatColor.RESET +
                        "When the match timer reaches zero, every player should be inside the Firezone.\n\n" +
                        "The Firezone will slowly shrink until there is only one team standing."
        );
        bookMeta.addPage(
                ChatColor.AQUA + "" + ChatColor.BOLD + "How to play\n\n" + ChatColor.RESET +
                        "To see all commands, type" + ChatColor.BOLD + " /hg help" + ChatColor.RESET + ".\n\n" +
                        "When everyone is in a team and readied up, the match will prepare to start."
        );

        welcomeBook.setItemMeta(bookMeta);

        return welcomeBook;
    }

    public void giveWelcomeBook(HGPlayer hgPlayer) {
        hgPlayer.player.getInventory().addItem(welcomeBook);
    }

    public void startGame() {
        List<HGPlayer> hgPlayers = main.hgPlayersManager.hgPlayers;

        for (HGPlayer hgPlayer : hgPlayers) {
            if (!hgPlayer.ready) {
                return;
            }
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "Generating world...");

        String worldName = main.hgWorldManager.createHGWorld();

        World hgWorld = Bukkit.getWorld(worldName);

        //pretty ugly solution for now but whatever
        waitForWorldCreation = new BukkitRunnable() {
            @Override
            public void run() {
                waitTime++;
                if(waitTime > 10){
                    startRunnable(hgWorld);
                }
            }
        }.runTaskTimer(main.plugin, 0, 20);
    }

    public void endGame() {
        hgMatch = null;
        gameIsStarted = false;

        //placeholder
        Bukkit.getServer().reload();
        //
        main.hgScoreboardManager.updateScoreboardLobby();
        countdownTimer = 4;
        Bukkit.getScheduler().cancelTasks(main.plugin);
    }

    public void teleportToSpawn(Player player){
        if (main.pluginConfig.config.get("spawn") != null) {
            player.teleport(new Location(
                    Bukkit.getWorld(main.pluginConfig.config.getString("spawn.world")),
                    main.pluginConfig.config.getInt("spawn.x"),
                    main.pluginConfig.config.getInt("spawn.y"),
                    main.pluginConfig.config.getInt("spawn.z"),
                    (float)main.pluginConfig.config.getDouble("spawn.yaw"),
                    (float)main.pluginConfig.config.getDouble("spawn.pitch")
            ));
        }
    }

    //apparently bukkit doesnt like creating runnables inside runnables : )
    void startRunnable(World hgWorld){
        countdown = new BukkitRunnable() {
            @Override
            public void run() {
                countdownTimer--;

                Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Starting game in " + countdownTimer + " seconds!");

                if (countdownTimer <= 0) {

                    hgMatch = new HGMatch(main, main.hgPlayersManager.hgPlayers, main.hgTeamsManager.hgTeams, hgWorld, roamingTime, invincibilityTime, matchTime, radius, zoneDamageIntensity);
                    gameIsStarted = true;

                    main.hgScoreboardManager.updateScoreBoardMatch();
                    Bukkit.getServer().getScheduler().cancelTask(countdown.getTaskId());
                }
            }
        }.runTaskTimer(main.plugin, 0, 20);

        Bukkit.getServer().getScheduler().cancelTask(waitForWorldCreation.getTaskId());
    }
}
