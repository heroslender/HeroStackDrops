package com.heroslender.herostackdrops.controller;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.services.ConfigurationService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @Getter
    private StackMethod method;
    @Getter
    private List<ConfigItem> items;
    @Getter
    private List<String> blockedWorlds;
    @Getter
    private String itemName;
    @Getter
    private double stackRadius;
    @Getter
    private boolean stackOnSpawn;
    @Getter
    private boolean showAnimation;

    public void init() {
        val config = configurationService.getConfig();
        method = getStackMethodFrom(config.getString("restringir-itens.method", "DESATIVADO"));

        items = new ArrayList<>(getItems(config.getStringList("restringir-itens.itens")));
        if (method != StackMethod.ALL) {
            for (int i = 0; i < min(items.size(), 10); i++) {
                StackDrops.getInstance().getLogger().info("Loaded item " + items.get(i).getMaterial().name() + ";");
            }

            if (items.size() >= 10) {
                StackDrops.getInstance().getLogger().info("(...)");
            }
        }

        blockedWorlds = config.getStringList("mundos-bloqueados");

        itemName = config.getBoolean("holograma.ativado", true)
            ? config.getString("holograma.texto", "&7{quantidade}x &e{nome}").replace('&', '§')
            : null;

        stackOnSpawn = config.getBoolean("stack-on-spawn", false);
        stackRadius = config.getDouble("raio-de-stack", 5D);

        showAnimation = config.getBoolean("animacao", true);
    }

    /**
     * Is the item allowed to be over stacked
     *
     * @param itemStack Item to check
     * @return {@code true} if the item is allowed to stack,
     * {@code false} otherwise.
     */
    public boolean isItemDisabled(@NotNull final ItemStack itemStack) {
        if (method == StackMethod.ALL) {
            return false;
        }

        val type = itemStack.getType();

        boolean itemsContains = false;
        for (ConfigItem item : items) {
            if (item.isSame(type, itemStack)) {
                itemsContains = true;
                break;
            }
        }

        return (method != StackMethod.BLACKLIST || itemsContains) && (method != StackMethod.WHITELIST || !itemsContains);
    }

    public List<Entity> getNearby(final Entity source) {
        return source.getNearbyEntities(getStackRadius(), getStackRadius(), getStackRadius());
    }

    public boolean isSimilar(@NotNull final ItemStack source, @NotNull final ItemStack other) {
        return source.isSimilar(other);
    }

    /**
     * Get the {@link StackMethod} for the given string
     *
     * @param method The string to read the method
     * @return The method
     */
    private StackMethod getStackMethodFrom(final String method) {
        final String methodName = method.toLowerCase(Locale.ROOT);
        if (methodName.equals("whitelist")) {
            return StackMethod.WHITELIST;
        } else if (methodName.equals("blacklist")) {
            return StackMethod.BLACKLIST;
        } else {
            return StackMethod.ALL;
        }
    }

    /**
     * Parse a list of strings containing material names and data
     * to a list of {@link Material}.
     *
     * @param items list of material names and data, separated by ":"
     * @return {@link List} of {@link Material} containing all
     * items parsed.
     */
    private List<ConfigItem> getItems(final List<String> items) {
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
     * @return {@link Optional} containing the {@link Material} if valid.
     */
    private Optional<ConfigItem> getMaterialDataFor(final String source) {
        String[] split = source.split(":");
        val matName = split[0];
        val mat = Material.matchMaterial(matName);
        if (mat == null) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "O material '" + matName + "' nao existe!");
            return Optional.empty();
        }

        short data = -1;
        if (split.length > 1) {
            try {
                data = Short.parseShort(split[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        return Optional.of(new ConfigItem(mat, data));
    }

    @Data
    private static final class ConfigItem {
        @NotNull
        private final Material material;

        private final short data;

        private boolean isSame(@NotNull final Material itemMaterial, @NotNull final ItemStack itemStack) {
            if (material != itemMaterial) {
                return false;
            }

            if (data == -1) {
                // Ignore data, so it is the same
                return true;
            }

            return data == itemStack.getDurability();
        }
    }


    public enum StackMethod {
        /**
         * Only the items listed will stack.
         */
        WHITELIST,
        /**
         * All items will stack, but the items listed won't.
         */
        BLACKLIST,
        /**
         * All items will stack, regardless of the items list
         */
        ALL
    }
}
