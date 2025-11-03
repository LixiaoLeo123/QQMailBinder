package org.drantiss;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Main extends JavaPlugin implements Listener {
    public Map<String, String> verifiedPlayers = new HashMap<>();
    public Set<String> bannedQQs = new HashSet<>();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("qq").setExecutor(new QQCommand(this));
        this.getCommand("bq").setExecutor(new BQCommand(this));
        getServer().getPluginManager().registerEvents(new BanCommandListener(this), this);
        try{
            createFile("plugins/MailBox/VerifiedPlayers");
            createFile("plugins/MailBox/VerifiedQQs");
            createFile("plugins/MailBox/BannedQQs");
            List<String> players = Files.readAllLines(Path.of("plugins/MailBox/VerifiedPlayers"));
            List<String> qqs = Files.readAllLines(Path.of("plugins/MailBox/VerifiedQQs"));
            int size = Math.min(players.size(), qqs.size());
            for (int i = 0; i < size; i++) {
                verifiedPlayers.put(players.get(i).trim(), qqs.get(i).trim());
            }
            BufferedReader reader = new BufferedReader(new FileReader(new File("plugins/MailBox/BannedQQs")));
            reader.lines().map(String::trim).forEach(bannedQQs::add);
        }catch(Exception e){
            this.getLogger().severe(e.getMessage());
        }
        this.getLogger().info("MailBox v1.0 has been enabled!");
        this.getLogger().info("Author: Drantiss");
    }
    public void createFile(String filePath) throws IOException {
        File playersFile = new File(filePath);
        if(!playersFile.exists()){
            File parentDir = playersFile.getParentFile();
            if(parentDir != null){
                if(!parentDir.exists()){
                    boolean success = parentDir.mkdirs();
                    if(!success){
                        this.getLogger().severe("Failed to create the directory!");
                    }
                }
            }
            boolean fileCreated = playersFile.createNewFile();
            if(!fileCreated){
                this.getLogger().severe("Failed to create the file!");
            }
        }
    }
    public boolean refresh(){
        try{
            verifiedPlayers.clear();
            bannedQQs.clear();
            createFile("plugins/MailBox/VerifiedPlayers");
            createFile("plugins/MailBox/VerifiedQQs");
            createFile("plugins/MailBox/BannedQQs");
            List<String> players = Files.readAllLines(Path.of("plugins/MailBox/VerifiedPlayers"));
            List<String> qqs = Files.readAllLines(Path.of("plugins/MailBox/VerifiedQQs"));
            int size = Math.min(players.size(), qqs.size());
            for (int i = 0; i < size; i++) {
                verifiedPlayers.put(players.get(i).trim(), qqs.get(i).trim());
            }
            BufferedReader reader = new BufferedReader(new FileReader(new File("plugins/MailBox/BannedQQs")));
            reader.lines().map(String::trim).forEach(bannedQQs::add);
            return true;
        }catch(Exception e){
            this.getLogger().severe(e.getMessage());
            return false;
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        CompletableFuture.supplyAsync(() -> {
            if(verifiedPlayers.containsKey(playerName)){
                return true;
            }else{
                return false;
            }
        }).thenAccept(isVerified -> {
            this.getServer().getScheduler().runTask(this,() -> {
                if(isVerified){
                    event.getPlayer().setGameMode(GameMode.SURVIVAL);
                }else{
                    event.getPlayer().setGameMode(GameMode.ADVENTURE);
                    event.getPlayer().sendMessage(Component.text("请登录后输入", NamedTextColor.GREEN)
                            .append(Component.text("/qq", NamedTextColor.YELLOW))
                            .append(Component.text("绑定qq邮箱以开启生存模式~",NamedTextColor.GREEN)));
                }
            });
        });
    }
}