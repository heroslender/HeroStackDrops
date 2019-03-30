package com.heroslender.herostackdrops.services;

import com.heroslender.herostackdrops.StackDrops;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class ConfigurationServiceImpl implements ConfigurationService {

    @Override
    public void init() {
        StackDrops main = StackDrops.getInstance();
        main.saveDefaultConfig();
        main.reloadConfig();
        final FileConfiguration configuration = main.getConfig();

        if (!configuration.contains("restringir-itens.method"))
            configuration.set("restringir-itens.method", "DESATIVADO");
        if (!configuration.contains("restringir-itens.itens"))
            configuration.set("restringir-itens.itens", Arrays.asList("STONE", "DIRT"));
        if (!configuration.contains("holograma.ativado"))
            configuration.set("holograma.ativado", true);
        if (!configuration.contains("holograma.texto"))
            configuration.set("holograma.texto", "&7{quantidade}x &e{nome}");
        if (!configuration.contains("stack-on-spawn"))
            configuration.set("stack-on-spawn", false);
        if (!configuration.contains("raio-de-stack"))
            configuration.set("raio-de-stack", 5);

        main.saveConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        return StackDrops.getInstance().getConfig();
    }

    @Override
    public void stop() {

    }
}
