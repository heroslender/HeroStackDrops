package com.heroslender.herostackdrops.controller;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.services.ConfigurationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @Getter private StackMethod method;
    @Getter private List<MaterialData> itens;
    @Getter private String itemName;
    @Getter private Double stackRadius;
    @Getter private Boolean stackOnSpawn;

    public void init() {
        val config = configurationService.getConfig();
        method = getStackMethodFrom(config.getString("restringir-itens.method", "DESATIVADO"));

        itens = new ArrayList<>(getItems(config.getStringList("restringir-itens.itens")));

        itemName = config.getBoolean("holograma.ativado", true)
                ? config.getString("holograma.texto", "&7{quantidade}x &e{nome}").replace('&', '§')
                : null;

        stackOnSpawn = config.getBoolean("stack-on-spawn", false);
        stackRadius = config.getDouble("raio-de-stack", 5D);
    }

    /**
     * Is the item allowed to be over stacked
     *
     * @param itemStack Item to check
     * @return {@code true} if the item is allowed to stack,
     * {@code false} otherwise.
     */
    public Boolean isItemAllowed(final ItemStack itemStack) {
        val itensContains = itens.contains(itemStack.getData());

        return (method == StackMethod.BLACKLIST && !itensContains)
                || (method == StackMethod.WHITELIST && itensContains)
                || method == StackMethod.DESATIVADO;
    }

    public List<Item> getNearby(final Item source) {
        return source.getNearbyEntities(getStackRadius(), getStackRadius(), getStackRadius())
                .stream()
                .filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity)
                .collect(Collectors.toList());
    }

    /**
     * Get the {@link StackMethod} for the given string
     *
     * @param method The string to read the method
     * @return The method
     */
    private StackMethod getStackMethodFrom(final String method) {
        switch (method.toLowerCase()) {
            case "whitelist":
                return StackMethod.WHITELIST;
            case "blacklist":
                return StackMethod.BLACKLIST;
            default:
                return StackMethod.DESATIVADO;
        }
    }

    /**
     * Parse a list of strings containing material names and data
     * to a list of {@link MaterialData}.
     *
     * @param items list of material names and data, separated by ":"
     * @return {@link List} of {@link MaterialData} containing all
     * items parsed.
     */
    private List<MaterialData> getItems(final List<String> items) {
        return items.stream()
                .map(this::getMaterialDataFor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Parse a string containing some material data separated by ":"
     * Ex: "STONE:2"
     *
     * @param source String to parse
     * @return {@link Optional} containing the {@link MaterialData} if valid.
     */
    private Optional<MaterialData> getMaterialDataFor(final String source) {
        val splited = source.split(":");

        val mat = Material.matchMaterial(splited[0]);
        if (mat == null) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "O material '{}' nao existe!", splited[0]);
            return Optional.empty();
        }

        val materialData = new MaterialData(mat);

        if (splited.length > 1) {
            try {
                materialData.setData(Byte.parseByte(splited[1]));
            } catch (NumberFormatException ignore) {
            }
        }

        return Optional.of(materialData);
    }

    public enum StackMethod {
        WHITELIST,
        BLACKLIST,
        DESATIVADO
    }
}
