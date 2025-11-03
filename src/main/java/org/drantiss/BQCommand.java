package org.drantiss;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
//useless command
public class BQCommand implements CommandExecutor {
    Main plugin;
    public BQCommand(Main plugin) {
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
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(!((Player) sender).isOp()){
                return true;
            }
        }
        if(args.length != 2){
            sender.sendMessage(Component.text("Usage: /bq <player> <reason>", NamedTextColor.RED));
        }else{
            String playerId = args[0];
            Bukkit.getBanList(BanList.Type.NAME).addBan(playerId, args[1], null, null);
            if (Bukkit.getPlayer(playerId) != null) {
                Bukkit.getPlayer(playerId).kickPlayer("您已被封禁: " + args[1]);
            }
            if(plugin.verifiedPlayers.containsKey(playerId)){
                addBannedQQ(plugin.verifiedPlayers.get(playerId));
            }
        }
        return true;
    }
}
