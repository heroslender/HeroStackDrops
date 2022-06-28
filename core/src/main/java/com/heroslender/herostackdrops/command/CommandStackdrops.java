package com.heroslender.herostackdrops.command;

import com.heroslender.herostackdrops.StackDrops;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class CommandStackdrops implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("herostackdrops.admin") || sender.getName().equals("Heroslender")) {
            if (args.length <= 0) {
                sender.sendMessage("§b[HeroStackDrops] §e" + StackDrops.getInstance().getDescription().getFullName());
                return true;
            }

            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("herostackdrops.admin")) {
                // Verificar a permissão outra vez, para bloquear "abusos" de alguem que use o meu nick
                StackDrops.getInstance().reloadConfiguration();
                sender.sendMessage("§b[HeroStackDrops] §aConfig reiniciada com sucesso!");
                return true;
            } else if (args[0].equalsIgnoreCase("item") && sender instanceof Player) {
                val item = ((Player) sender).getInventory().getItemInHand();

                val sb = new StringBuilder(ChatColor.GREEN.toString());
                sb.append("Item: ");
                sb.append(item.getType().name());
                sb.append(ChatColor.YELLOW);
                if (item.getDurability() != 0) {
                    sb.append(":").append(item.getDurability());
                }

                sender.sendMessage(sb.toString());
                return true;
            }
        }

        return false;
    }
}
