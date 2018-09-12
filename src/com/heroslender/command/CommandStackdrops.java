package com.heroslender.command;

import com.heroslender.StackDrops;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.spigotmc.SpigotConfig;

import java.util.Collections;

public class CommandStackdrops extends Command {
    public CommandStackdrops() {
        super("herostackdrops");
        setAliases(Collections.singletonList("stackdrops"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.hasPermission("herostackdrops.admin") || sender.getName().equals("Heroslender")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("herostackdrops.admin")) {
                // Verificar a permissão outra vez, para bloquear "abusos" de alguem que use o meu nick
                StackDrops.getInstance().reloadConfiguration();
                sender.sendMessage("§b[HeroStackDrops] §aConfig reiniciada com sucesso!");
                return true;
            }
            sender.sendMessage("§b[HeroStackDrops] §e" + StackDrops.getInstance().getDescription().getFullName());
            return true;
        }
        // Se não tiver permissão, fala para o mlk que o comando não existe
        sender.sendMessage(SpigotConfig.unknownCommandMessage);
        return true;
    }
}
