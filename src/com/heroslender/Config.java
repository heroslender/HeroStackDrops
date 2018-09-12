package com.heroslender;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {
    private final StackDrops.Metodo method;
    private final List<Material> itens;
    private final String itemName;
    private final Double stackRadius;

    Config(StackDrops stackDrops) {
        addDefault("restringir-itens.method", "DESATIVADO");
        addDefault("restringir-itens.itens", Arrays.asList("STONE", "DIRT"));
        addDefault("holograma.ativado", true);
        addDefault("holograma.texto", "&7{quantidade}x &e{nome}");
        addDefault("raio-de-stack", 5);

        System.out.println(stackDrops.getConfig().getString("restringir-itens.method", "nulo").toLowerCase());
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

        itens = new ArrayList<>();
        if (stackDrops.getConfig().contains("restringir-itens.itens"))
            for (String s : stackDrops.getConfig().getStringList("restringir-itens.itens")) {
                try {
                    itens.add(Material.valueOf(s));
                } catch (IllegalArgumentException e) {
                    stackDrops.getLogger().warning("O material '" + s + "' nao existe!");
                }
            }

        itemName = stackDrops.getConfig().getBoolean("holograma.ativado", true)
                ? stackDrops.getConfig().getString("holograma.texto", "&7{quantidade}x &e{nome}").replace('&', '§')
                : null;

        stackRadius = stackDrops.getConfig().getDouble("raio-de-stack", 5D);
    }

    public Boolean isItemAllowed(final ItemStack itemStack) {
        return (getMethod() == StackDrops.Metodo.BLACKLIST && !itens.contains(itemStack.getType()))
                || (getMethod() == StackDrops.Metodo.WHITELIST && itens.contains(itemStack.getType()))
                || getMethod() == StackDrops.Metodo.DESATIVADO;
    }

    public StackDrops.Metodo getMethod() {
        return method;
    }

    public String getItemName() {
        return itemName;
    }

    public Double getStackRadius() {
        return stackRadius;
    }

    private void addDefault(String path, Object value) {
        if (!StackDrops.getInstance().getConfig().contains(path)) {
            StackDrops.getInstance().getConfig().set(path, value);
            StackDrops.getInstance().saveConfig();
        }
    }
}
