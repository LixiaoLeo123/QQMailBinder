package org.drantiss;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BanCommandListener implements Listener {
    Main plugin;
    public BanCommandListener(Main plugin) {
        this.plugin = plugin;
    }
    public boolean addBannedQQ(String EM){
        plugin.bannedQQs.add(EM);
        try {
            Files.writeString(Path.of("plugins/MailBox/BannedQQs"), EM + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe(e.getMessage());
            return false;
        }
    }
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase();

        if (command.startsWith("ban ")) {
            String[] args = command.split(" ", 3);
            String playerId = args[1];
            if (Bukkit.getPlayer(playerId) != null) {
                Bukkit.getPlayer(playerId).kickPlayer("您已被封禁: " + args[1]);
            }
            if(plugin.verifiedPlayers.containsKey(playerId)){
                addBannedQQ(plugin.verifiedPlayers.get(playerId));
            }
        }
    }
}