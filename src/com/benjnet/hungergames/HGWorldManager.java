package com.benjnet.hungergames;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class HGWorldManager {
    MultiverseCore core;
    ArrayList<File> hgWorlds = new ArrayList<File>();
    int hgWorldsCount;

    public HGWorldManager(Main main){
        core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        File serverDirectory = new File(System.getProperty("user.dir"));
        //Get all files in directory
        File[] allFiles = serverDirectory.listFiles();

        //Get all HGWorlds
        for(File file : allFiles){
            if(file.getName().toLowerCase().contains("hgworld")){
                if(!file.getName().toLowerCase().contains("nether")){
                    hgWorldsCount++;
                }
                System.out.println("adding FILES ::::::::::::::::::::::::::::::::::::::::::::::");
                hgWorlds.add(file);
            }
        }
    }

    public String createHGWorld(){
        String worldName = hgWorldsCount == 0 ? "HGWorld" : "HGWorld" + hgWorldsCount;

        core.getMVWorldManager().addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, false, null, false);
        core.getMVWorldManager().addWorld(worldName + "_nether", World.Environment.NETHER, null, WorldType.NORMAL, false, null, false);

        Bukkit.getWorld(worldName).setAutoSave(false);
        Bukkit.getWorld(worldName + "_nether").setAutoSave(false);

        hgWorldsCount++;

        return worldName;
    }

    public void deleteHGWorlds(){
        Collection<MultiverseWorld> mvWorlds = core.getMVWorldManager().getMVWorlds();
        for(MultiverseWorld world : mvWorlds){
            if(world.getName().toLowerCase().contains("hg")){
                core.getMVWorldManager().deleteWorld(world.getName());
            }
        }
    }
}