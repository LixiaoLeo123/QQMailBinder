package org.drantiss;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
public class QQCommand implements CommandExecutor {
    private final Main plugin;
    private final Map<String, Long> lastUsed = new HashMap<>();
    private final Map<String, Long> lastSend = new HashMap<>();
    private final Map<String, String> verificationCode = new HashMap<>();
    private final Map<String, String> preEM = new HashMap<>();
    private final long cooldownMillis = 4000; // 4 seconds cooldown
    private final long cooldownMillisEM = 300000; // 5 minuts cooldown
    public QQCommand(Main plugin) {
        this.plugin = plugin;
    }
    public boolean sendMail(String text, String to){
        final String from = "xxx@xxx.com";
        final String password = "xxx"; // for Gmail, generate App Password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.host", "smtp.126.com");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject("懒狗服邮件绑定");
            message.setText("欢迎您加入懒狗服！您的验证码为: " + text + "。此邮件请不要转发给他人！欢迎加入懒狗服qq群: xxx !");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            plugin.getLogger().severe(e.getMessage());
            return false;
        }
    }
    public boolean addVerifiedPlayer(String playerId, String EM){
        plugin.verifiedPlayers.put(playerId, EM);
        try {
            Files.writeString(Path.of("plugins/MailBox/VerifiedPlayers"), playerId + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.writeString(Path.of("plugins/MailBox/VerifiedQQs"), EM + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe(e.getMessage());
            return false;
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        String playerId = player.getName();
        //OP pass a player
        if(sender.isOp() && args.length == 2 && args[0].equals("pass")) {
            String passPlayerId = args[1];
            if(plugin.verifiedPlayers.containsKey(passPlayerId)) {
                player.sendMessage(Component.text("Nothing changed.The player has been passed.",NamedTextColor.YELLOW));
                return true;
            }else{
                addVerifiedPlayer(passPlayerId, "pass");
                plugin.getServer().getPlayer(passPlayerId).setGameMode(GameMode.SURVIVAL);
                player.sendMessage(Component.text("PASSED",NamedTextColor.GREEN));
                return true;
            }
        }else if(sender.isOp() && args.length == 1 && args[0].equals("refresh")){
            plugin.refresh();
            player.sendMessage(Component.text("SUCCESSFULLY REFRESHED",NamedTextColor.GREEN));
            return true;
        }
        //check the verification state
        if(plugin.verifiedPlayers.containsKey(playerId)){
            player.sendMessage(Component.text("您已经绑定过邮箱了!", NamedTextColor.RED));
            return true;
        }
        //start the logic
        if(args.length == 0){
            player.sendMessage(Component.text("请输入", NamedTextColor.GREEN)
                            .append(Component.text("/qq [你的qq邮箱]", NamedTextColor.YELLOW))
                            .append(Component.text("来开始绑定~", NamedTextColor.GREEN))
            );
            return true;
        }else if(args.length == 1){
            //set a colldown
            long now = System.currentTimeMillis();
            if (lastUsed.containsKey(playerId)) {
                long lastTime = lastUsed.get(playerId);
                long timeLeft = cooldownMillis - (now - lastTime);
                if (timeLeft > 0) {
                    player.sendMessage(Component.text("您输入得太快子!", NamedTextColor.RED));
                    return true;
                }
            }
            lastUsed.put(playerId, now);
            String arg = args[0];
            if(arg.contains("@qq.com")){
                if(plugin.bannedQQs.contains(arg)){
                    player.sendMessage(Component.text("此邮箱已被封禁!", NamedTextColor.RED));
                    return true;
                }
                if(plugin.verifiedPlayers.containsValue(arg)){
                    player.sendMessage(Component.text("此邮箱已被绑定过了!", NamedTextColor.RED));
                    return true;
                }
                if(!lastSend.containsKey(playerId)||(60000 - (now - lastSend.get(playerId)) <= 0)){
                    Random random = new Random();
                    int code = 100000 + random.nextInt(900000);
                    verificationCode.put(playerId, Integer.valueOf(code).toString());
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        if(sendMail(Integer.valueOf(code).toString(), args[0])){
                            lastSend.put(playerId, now);
                            preEM.put(playerId, args[0]);
                            player.sendMessage(Component.text("验证码已发送!请输入", NamedTextColor.GREEN)
                                    .append(Component.text("/qq [您邮箱里收到的验证码]",NamedTextColor.YELLOW))
                                    .append(Component.text("来完成绑定!验证码5分钟内有效", NamedTextColor.GREEN)));
                        }else{
                            player.sendMessage(Component.text("发送失败!请检查邮箱是否正确!", NamedTextColor.RED));
                        }
                    });
                }else {
                    player.sendMessage(Component.text("请等待" + (60000-(now - lastSend.get(playerId)))/1000 + "秒冷却期过后再发送~", NamedTextColor.RED));
                }
            }else if(arg.contains("@")){
                player.sendMessage(Component.text("服务器仅支持绑定qq邮箱哦!", NamedTextColor.RED));
            }else{
                if(!preEM.containsKey(playerId)){
                    player.sendMessage(Component.text("请先发送验证码!", NamedTextColor.RED));
                    return true;
                }
                long lastTime = lastUsed.get(playerId);
                long timeLeft = cooldownMillisEM - (now - lastTime);
                if(timeLeft > 0){
                    if(args[0].equals(verificationCode.get(playerId))){
                        addVerifiedPlayer(playerId, preEM.get(playerId));
                        player.sendMessage(Component.text("绑定成功!欢迎加入懒狗服~", NamedTextColor.GREEN));
                        player.setGameMode(GameMode.SURVIVAL);
                    }else{
                        player.sendMessage(Component.text("验证码错误,请重新输入", NamedTextColor.GREEN));
                    }
                }else{
                    player.sendMessage(Component.text("验证码过期辣,请重新验证~", NamedTextColor.RED));
                }
            }
        }else{
            player.sendMessage(Component.text("请输入", NamedTextColor.GREEN)
                            .append(Component.text("/qq [你的qq邮箱]", NamedTextColor.YELLOW))
                            .append(Component.text("来开始绑定,您输入了多个参数~", NamedTextColor.GREEN))
            );
            return true;
        }
        return true;
    }
}