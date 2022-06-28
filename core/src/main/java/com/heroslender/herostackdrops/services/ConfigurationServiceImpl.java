package com.heroslender.herostackdrops.services;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {
    private final JavaPlugin plugin;

    @Override
    public void init() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        final FileConfiguration configuration = plugin.getConfig();

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
        if (!configuration.contains("animacao"))
            configuration.set("animacao", true);

        plugin.saveConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    @Override
    public void stop() {

    }
}
