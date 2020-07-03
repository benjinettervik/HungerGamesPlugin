package com.benjnet.hungergames;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HGMatch implements Listener {
    Main main;
    List<HGPlayer> hgPlayers;
    List<HGTeam> hgTeams;
    List<Block> netherrackBlocks;
    List<Block> fireBlocks;
    List<Block> unlitFireBlocks;
    World hgWorld;

    public HGLobbyManager.Stage matchStage;

    Location worldSpawnLocation;

    int roamingTime;
    int invincibilityTime;
    int matchTime;
    int fightPeriodElapsedTime;
    int invincibilityElapsedTime;
    int radius;
    int zoneDamageIntensity;
    int postMatchTime;

    Boolean fiveMinWarning = false;
    Boolean threeMinWarning = false;
    Boolean oneMinWarning = false;

    Boolean hasFinished = false;

    BukkitTask roamingCountdownTask;
    BukkitTask invincibilityCountdownTask;
    BukkitTask matchCountdownTask;
    BukkitTask fightPeriodTask;
    BukkitTask fightPeriodCountupTask;
    BukkitTask checkPlayerOutsideZoneTask;
    BukkitTask createFireTask;
    BukkitTask finishGameTask;
    BukkitTask fireworkTask;

    int xSpawn = 0;

    public HGMatch(Main _main, List<HGPlayer> _hgPlayers, List<HGTeam> _hgTeams, World _hgWorld, int _roamingTime, int _invincibilityTime, int _matchTime, int _radius, int _zoneDamageIntensity) {
        main = _main;
        hgPlayers = _hgPlayers;
        hgTeams = _hgTeams;
        hgWorld = _hgWorld;

        roamingTime = _roamingTime;
        invincibilityTime = _invincibilityTime;
        matchTime = _matchTime;
        radius = _radius;
        zoneDamageIntensity = _zoneDamageIntensity;

        netherrackBlocks = new ArrayList<Block>();
        fireBlocks = new ArrayList<Block>();
        unlitFireBlocks = new ArrayList<Block>();

        main.pm.registerEvents(this, main);

        hgWorld.setGameRule(GameRule.DO_FIRE_TICK, false);

        setup();
    }

    @EventHandler
    void onHit(EntityDamageByEntityEvent e) {
        if (matchStage == HGLobbyManager.Stage.ROAMING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent e) {
        if (matchStage == HGLobbyManager.Stage.ROAMING) {
            e.setCancelled(true);
        } else if (e.getBlock().getType() == Material.DIAMOND_BLOCK || e.getBlock().getType() == Material.BEACON) {
            Location loc = e.getBlock().getLocation();
            if (Math.abs(loc.getX()) < Math.abs(worldSpawnLocation.getX()) + 4 && Math.abs(loc.getZ()) < Math.abs(worldSpawnLocation.getZ()) + 4) {
                e.getPlayer().sendMessage("nice try bitch");
                e.setCancelled(true);
            }
        }else if(e.getBlock().getType() == Material.NETHERRACK){
            for (Block netherrackBlock : netherrackBlocks) {
                if(e.getBlock().getLocation().getX() == netherrackBlock.getLocation().getX() && e.getBlock().getLocation().getZ() == netherrackBlock.getLocation().getZ()){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent e) {
        if(matchStage == HGLobbyManager.Stage.ROAMING){
            e.setCancelled(true);
        }

        Location loc = e.getBlock().getLocation();
        if (Math.abs(loc.getX()) < 2 && Math.abs(loc.getZ()) < 2) {
            e.setCancelled(true);
        } else if (e.getBlockReplacedState().getType() == Material.FIRE) {
            for (Block block : fireBlocks) {
                if (block.getLocation().getX() == e.getBlockReplacedState().getLocation().getX() && block.getLocation().getZ() == e.getBlockReplacedState().getLocation().getZ()) {
                    e.setCancelled(true);
                }
            }
        } else if (e.getBlockPlaced().getType() == Material.FIRE || e.getBlockPlaced().getType() == Material.GRAVEL) {
            if (Math.abs(e.getBlock().getLocation().getX()) < radius + 1 && Math.abs(e.getBlock().getLocation().getZ()) < radius + 1) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onPlayerHit(EntityDamageByEntityEvent e) {
        if (matchStage == HGLobbyManager.Stage.INVINCIBILITY) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onFoodEat(FoodLevelChangeEvent e){
        if(matchStage == HGLobbyManager.Stage.ROAMING){
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        if (e.getMaterial() == Material.WATER_BUCKET || e.getMaterial() == Material.LAVA_BUCKET) {
            return;
        }

        Block clickedBlock = e.getClickedBlock();

        if (clickedBlock.getRelative(BlockFace.UP) != null) {
            Block aboveBlock = clickedBlock.getRelative(BlockFace.UP);
            if (aboveBlock.getType() == Material.FIRE) {
                for (Block fireBlock : fireBlocks) {
                    if (fireBlock.getLocation().getX() == aboveBlock.getLocation().getX() && fireBlock.getLocation().getZ() == aboveBlock.getLocation().getZ()) {
                        aboveBlock.setType(Material.FIRE);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    //cancel water flow if it hits zone fire
    @EventHandler
    void onFlow(BlockFromToEvent e) {
        if (e.getToBlock().getType() == Material.FIRE) {
            for (Block block : fireBlocks) {
                if (block.getLocation().getX() == e.getToBlock().getLocation().getX() && block.getLocation().getZ() == e.getToBlock().getLocation().getZ()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onLiquidPlace(PlayerBucketEmptyEvent e) {
        Block nextBlock = e.getBlockClicked().getRelative(e.getBlockFace());

        if (nextBlock.getType() == Material.FIRE) {
            for (Block fireBlock : fireBlocks) {
                if (fireBlock.getLocation().getX() == nextBlock.getLocation().getX() && fireBlock.getLocation().getZ() == nextBlock.getLocation().getZ()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        HGPlayer hgPlayer = main.hgPlayersManager.findHGPlayer((Player) e.getEntity());

        if(matchStage == HGLobbyManager.Stage.ROAMING){
            e.setCancelled(true);
        }

        else if(matchStage == HGLobbyManager.Stage.INVINCIBILITY && invincibilityElapsedTime < 5){
            e.setCancelled(true);
        }

        double health = hgPlayer.player.getHealth() - e.getFinalDamage();
        if (health < 0.5 && hgPlayer.isAlive) {
            onPlayerDeathOrLeave(hgPlayer);
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerLeave(PlayerQuitEvent e) {
        onPlayerDeathOrLeave(main.hgPlayersManager.findHGPlayer(e.getPlayer()));
    }

    void setup() {
        worldSpawnLocation = new Location(hgWorld, 0, hgWorld.getHighestBlockAt(0, 0).getY(), 0);
        for (int i = 0; i < 100; i++) {
            Block spawnBlock = worldSpawnLocation.clone().add(0, -1, 0).getBlock();
            if (spawnBlock.getType() == Material.WATER) {
                Random rand = new Random();
                worldSpawnLocation.add(rand.nextInt(300), 0, rand.nextInt(250));
            } else {
                int y = hgWorld.getHighestBlockAt((int) worldSpawnLocation.getX(), (int) worldSpawnLocation.getZ()).getY();
                worldSpawnLocation.setY(y - 1);
                break;
            }
        }

        for (HGPlayer hgPlayer : hgPlayers) {
            Location spawnLocation = worldSpawnLocation.clone();
            spawnLocation.add(0, 2, 0);

            Player player = hgPlayer.player;

            player.teleport(worldSpawnLocation.add(0, 2, 0));

            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);

            player.setHealth(20);

            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));
            player.setHealth(20);
            player.setFoodLevel(20);

            //if any player would be invisible
            for (HGPlayer visiblePlayer : hgPlayers) {
                player.showPlayer(main.plugin, visiblePlayer.player);
            }

            hgPlayer.isAlive = true;
            hgPlayer.isInGame = true;
        }

        createBeacon();
        roamingPeriod();

        //create dangerzone circle without fire
        createDangerzone(radius, false);
    }

    void roamingPeriod() {
        matchStage = HGLobbyManager.Stage.ROAMING;

        roamingCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                roamingTime--;
                main.hgScoreboardManager.setScoreboardTimer(matchStage, roamingTime);

                if (roamingTime <= 0) {
                    invincibilityPeriod();
                }
            }
        }.runTaskTimer(main.plugin, 0, 20);
    }

    void invincibilityPeriod() {
        Bukkit.getServer().getScheduler().cancelTask(roamingCountdownTask.getTaskId());

        matchStage = HGLobbyManager.Stage.INVINCIBILITY;
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Invincibility time has started.");

        for (HGPlayer hgPlayer : hgPlayers) {
            Location spawnLocation = worldSpawnLocation.clone();
            spawnLocation.add(0, 5, 0);
            hgPlayer.player.setHealth(20);
            hgPlayer.player.setFoodLevel(20);
            hgPlayer.player.teleport(worldSpawnLocation.add(0, 3, 0));
        }

        invincibilityCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                invincibilityTime--;
                invincibilityElapsedTime++;
                main.hgScoreboardManager.setScoreboardTimer(matchStage, invincibilityTime);

                if (invincibilityTime <= 0) {
                    matchPeriod();
                }
            }
        }.runTaskTimer(main.plugin, 0, 20);

    }

    void matchPeriod() {
        Bukkit.getServer().getScheduler().cancelTask(invincibilityCountdownTask.getTaskId());

        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Match has started!");

        matchStage = HGLobbyManager.Stage.MATCH;
        matchCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                matchTime--;
                main.hgScoreboardManager.setScoreboardTimer(matchStage, matchTime);

                if (matchTime <= 0) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Firezone has started!");
                    fightPeriod();
                } else if (matchTime <= 5) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Firezone starting in " + matchTime + " seconds!");
                } else if (matchTime <= 60 && !oneMinWarning) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Firezone starting in 1 minute!");
                    oneMinWarning = true;
                } else if (matchTime <= 3 * 60 && !threeMinWarning) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Firezone starting in 3 minute!");
                    threeMinWarning = true;
                } else if (matchTime <= 5 * 60 && !fiveMinWarning) {
                    Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Firezone starting in 5 minutes!");
                    fiveMinWarning = true;
                }
            }
        }.runTaskTimer(main.plugin, 0, 20);
    }

    void fightPeriod() {
        Bukkit.getServer().getScheduler().cancelTask(matchCountdownTask.getTaskId());

        matchStage = HGLobbyManager.Stage.FIGHT_PERIOD;

        createFire();
        checkForPlayerOutsideZone();

        fightPeriodCountupTask = new BukkitRunnable() {
            @Override
            public void run() {
                fightPeriodElapsedTime++;
                main.hgScoreboardManager.setScoreboardTimer(matchStage, fightPeriodElapsedTime);
            }
        }.runTaskTimer(main.plugin, 0, 20);

        fightPeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasFinished) {
                    radius -= 1;
                    createDangerzone(radius, true);
                }
            }
        }.runTaskTimer(main.plugin, 700, 700);
    }

    void createBeacon() {
        //great
        Location beaconLoc = worldSpawnLocation.clone();
        beaconLoc.add(0, -1, 0);
        beaconLoc.getBlock().setType(Material.BEACON);

        beaconLoc.add(0, -1, 0).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(-1, 0, 0).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(0, 0, -1).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(1, 0, 0).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(1, 0, 0).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(0, 0, 1).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(0, 0, 1).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(-1, 0, 0).getBlock().setType(Material.DIAMOND_BLOCK);
        beaconLoc.add(-1, 0, 0).getBlock().setType(Material.DIAMOND_BLOCK);
    }

    void createDangerzone(int rad, Boolean createFire) {
        Location loc = worldSpawnLocation.clone();
        Location originalLocation = loc.clone();

        unlitFireBlocks = new ArrayList<Block>();

        for (int i = -rad; i < rad + 1; i++) {
            loc = originalLocation.clone();
            loc.add(rad, 0, i);
            setBlockAtSurface(loc.clone());

            loc = originalLocation.clone();
            loc.add(-rad, 0, i);
            setBlockAtSurface(loc.clone());

            loc = originalLocation.clone();
            loc.add(i, 0, rad);
            setBlockAtSurface(loc.clone());

            loc = originalLocation.clone();
            loc.add(i, 0, -rad);
            setBlockAtSurface(loc.clone());
        }

        if (createFire) {
            createFire();
        }
    }

    void createFire() {
        createFireTask = new BukkitRunnable() {
            int fireIndex = 0;

            @Override
            public void run() {
                unlitFireBlocks.get(fireIndex).setType(Material.FIRE);
                fireIndex++;

                if (fireIndex >= unlitFireBlocks.size()) {
                    Bukkit.getServer().getScheduler().cancelTask(createFireTask.getTaskId());
                }
            }
        }.runTaskTimer(main.plugin, 0, 1);
    }

    void setBlockAtSurface(Location loc) {
        Block block = loc.getWorld().getHighestBlockAt((int) loc.getX(), (int) loc.getZ());
        for (int i = 0; i < 30; i++) {
            if (!block.getType().toString().toLowerCase().contains("leaves") &&
                    !block.getType().toString().toLowerCase().contains("log") &&
                    !block.getType().toString().toLowerCase().contains("air") &&
                    !block.getType().toString().toLowerCase().contains("fern") &&
                    block.getType() != Material.GRASS &&
                    block.getType() != Material.TALL_GRASS)
            {
                block.setType(Material.NETHERRACK);
                netherrackBlocks.add(block);
                Block fireBlock = block.getRelative(BlockFace.UP);
                fireBlocks.add(fireBlock);
                unlitFireBlocks.add(fireBlock);
                break;
            } else {
                block = block.getLocation().add(0, -1, 0).getBlock();
            }
        }
    }

    void checkForPlayerOutsideZone() {
        checkPlayerOutsideZoneTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (HGPlayer hgPlayer : hgPlayers) {
                    if (hgPlayer.isAlive) {

                        Player player = hgPlayer.player;

                        double xDistance = Math.abs(player.getLocation().getX() - worldSpawnLocation.getX());
                        double zDistance = Math.abs(player.getLocation().getZ() - worldSpawnLocation.getZ());

                        if (xDistance > radius || zDistance > radius || hgPlayer.player.getWorld().getEnvironment() == World.Environment.NETHER) {
                            if (hgPlayer.player.getHealth() > 0.5) {
                                hgPlayer.player.damage(0.5);
                            } else {
                                onPlayerDeathOrLeave(hgPlayer);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(main.plugin, 0, 10);
    }

    void finishGame(HGTeam winnerTeam) {
        postMatchTime = 0;
        finishGameTask = new BukkitRunnable() {
            @Override
            public void run() {
                postMatchTime++;

                Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + winnerTeam.name + ChatColor.GOLD + " is the winner!");

                if (postMatchTime >= 60) {
                    for (HGPlayer hgPlayer : hgPlayers) {
                        hgPlayer.isInGame = false;
                        hgPlayer.isAlive = null;
                        hgPlayer.ready = false;
                        hgPlayer.player.getInventory().clear();
                        hgPlayer.player.setHealth(20);
                        hgPlayer.player.setFoodLevel(20);
                        hgPlayer.player.teleport(main.hgLobbyManager.spawn);

                        for (HGPlayer visiblePlayer : hgPlayers) {
                            hgPlayer.player.showPlayer(main.plugin, visiblePlayer.player);
                        }

                        hgPlayer.player.setGameMode(GameMode.SURVIVAL);

                        main.hgLobbyManager.endGame();
                    }
                }
            }
        }.runTaskTimer(main.plugin, 0, 20);

        for (HGPlayer hgPlayer : winnerTeam.hgPlayersInTeam) {
            generateFirework(hgPlayer.player);
        }
    }

    void generateFirework(Player player) {
        fireworkTask = new BukkitRunnable() {
            @Override
            public void run() {
                Firework fw = (Firework) player.getLocation().getWorld().spawn(player.getLocation(), Firework.class);
                FireworkMeta fm = fw.getFireworkMeta();
                fm.addEffect(FireworkEffect.builder().flicker(true).trail(true).with(FireworkEffect.Type.BALL_LARGE).withColor(randomColor()).withFade(randomColor()).build());
                fm.setPower(3);
                fw.setFireworkMeta(fm);
            }
        }.runTaskTimer(main.plugin, 0, 5);
    }

    Color randomColor() {
        Random rand = new Random();
        int r = rand.nextInt(6);

        switch (r) {
            case 1:
                return Color.ORANGE;

            case 2:
                return Color.WHITE;

            case 3:
                return Color.RED;

            case 4:
                return Color.YELLOW;

            case 5:
                return Color.LIME;
        }

        return Color.AQUA;
    }

    void onPlayerDeathOrLeave(HGPlayer hgPlayer) {
        hgPlayer.isAlive = false;

        List<HGTeam> aliveTeams = new ArrayList<HGTeam>();

        Bukkit.broadcastMessage(ChatColor.AQUA + hgPlayer.name + ChatColor.RED + " has been slain!");

        for (HGTeam hgTeam : hgTeams) {
            if (hgTeam.isTeamDead()) {
                Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + hgTeam.name + ChatColor.RED + " has been eliminated!");
            } else {
                aliveTeams.add(hgTeam);
            }
        }

        hgPlayer.player.setGameMode(GameMode.SPECTATOR);

        for (ItemStack itemStack : hgPlayer.player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                hgPlayer.player.getWorld().dropItemNaturally(hgPlayer.player.getLocation(), itemStack);
            }
        }
        for (ItemStack itemStack : hgPlayer.player.getInventory().getArmorContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                hgPlayer.player.getWorld().dropItemNaturally(hgPlayer.player.getLocation(), itemStack);
            }
        }

        for (HGPlayer _hgPlayer : hgPlayers) {
            _hgPlayer.player.hidePlayer(main.plugin, hgPlayer.player);
        }

        if (aliveTeams.size() == 1) {
            finishGame(aliveTeams.get(0));
        } else if (aliveTeams.size() == 0) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "It's a tie!");
        }

        main.hgScoreboardManager.updateScoreBoardMatch();
    }
}
