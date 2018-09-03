package com.heroslender;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    private final StackDrops stackDrops;

    private StackDrops.Metodo method;
    private List<Material> itens;
    private String itemName;
    private Double stackRadius;

    Config(StackDrops stackDrops) {
        this.stackDrops = stackDrops;

        itens = new ArrayList<>();

        verifyConfig();
        loadConfig();
    }

    private void verifyConfig() {
        addDefault("restringir-itens.method", "DESATIVADO");
        addDefault("restringir-itens.itens", Arrays.asList("STONE", "DIRT"));
        addDefault("holograma.ativado", true);
        addDefault("holograma.texto", "&7{quantidade}x &e{nome}");
        addDefault("raio-de-stack", 5);
    }

    private void loadConfig() {
        itens.clear();

        switch (stackDrops.getConfig().getString("restringir-itens.method", "DESATIVADO").toLowerCase()) {
            case "whitelist":
                method = StackDrops.Metodo.WHITELIST;
                break;
            case "blacklist":
                method = StackDrops.Metodo.BLACKLIST;
                break;
            default:
                method = StackDrops.Metodo.DESATIVADO;
                break;
        }

        if (stackDrops.getConfig().contains("restringir-itens.itens"))
            for (String s : stackDrops.getConfig().getStringList("restringir-itens.itens")) {
                try {
                    itens.add(Material.valueOf(s));
                } catch (NoSuchFieldError e) {
                    stackDrops.getLogger().warning("O material '" + s + "' nao existe!");
                }
            }

        itemName = stackDrops.getConfig().getBoolean("holograma.ativado", true)
                ? stackDrops.getConfig().getString("holograma.texto", "&7{quantidade}x &e{nome}").replace('&', '§')
                : null;

        stackRadius = stackDrops.getConfig().getDouble("raio-de-stack", 5D);
    }

    public StackDrops.Metodo getMethod() {
        return method;
    }

    public List<Material> getItens() {
        return itens;
    }

    public String getItemName() {
        return itemName;
    }

    public Double getStackRadius() {
        return stackRadius;
    }

    private void addDefault(String path, Object value) {
        if (!stackDrops.getConfig().contains(path)) {
            stackDrops.getConfig().set(path, value);
            stackDrops.saveConfig();
        }
    }
}
