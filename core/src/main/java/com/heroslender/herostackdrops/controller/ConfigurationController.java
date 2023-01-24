package com.heroslender.herostackdrops.controller;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.services.ConfigurationService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
    private FilterType method;
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

    @Getter
    private FilterType filterMobs;
    @Getter
    private List<EntityType> mobsToFilter;

    public void init() {
        val config = configurationService.getConfig();
        String methodMigration = config.getString("restringir-itens.method");
        if (methodMigration != null) {
            config.set("restringir-itens.metodo", methodMigration);
            config.set("restringir-itens.method", null);
        }

        method = FilterType.from(config.getString("restringir-itens.metodo", "DESATIVADO"));

        items = new ArrayList<>(getItems(config.getStringList("restringir-itens.itens")));
        if (method != FilterType.DISABLED) {
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

        filterMobs = FilterType.from(config.getString("filtrar-mobs.metodo", "DESATIVADO"));
        mobsToFilter = new ArrayList<>();
        for (String mob : config.getStringList("filtrar-mobs.filtro")) {
            try {
                EntityType.valueOf(mob.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                StackDrops.getInstance().getLogger().log(Level.WARNING, "Invalid mob type: {}", mob);
            }
        }
    }

    /**
     * Is the item allowed to be over stacked
     *
     * @param itemStack Item to check
     * @return {@code true} if the item is allowed to stack,
     * {@code false} otherwise.
     */
    public boolean isItemDisabled(@NotNull final ItemStack itemStack) {
        if (method == FilterType.DISABLED) {
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

        return (method != FilterType.BLACKLIST || itemsContains) && (method != FilterType.WHITELIST || !itemsContains);
    }

    /**
     * Are the mob drops allowed to be stacked
     *
     * @param entityType Mob to check
     * @return {@code true} if the mob drops are allowed to stack,
     * {@code false} otherwise.
     */
    public boolean isMobDisabled(@NotNull final EntityType entityType) {
        if (method == FilterType.DISABLED) {
            return false;
        }

        boolean mobsContains = false;
        for (EntityType type : mobsToFilter) {
            if (type == entityType) {
                mobsContains = true;
                break;
            }
        }

        return (method != FilterType.BLACKLIST || mobsContains) && (method != FilterType.WHITELIST || !mobsContains);
    }

    public List<Entity> getNearby(final Entity source) {
        return source.getNearbyEntities(getStackRadius(), getStackRadius(), getStackRadius());
    }

    public boolean isSimilar(@NotNull final ItemStack source, @NotNull final ItemStack other) {
        return source.isSimilar(other);
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

    public enum FilterType {
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
        DISABLED;

        /**
         * Get the {@link FilterType} for the given string
         *
         * @param method The string to read the method
         * @return The method
         */
        public static FilterType from(final String method) {
            final String methodName = method.toLowerCase(Locale.ROOT);
            if (methodName.equals("whitelist")) {
                return FilterType.WHITELIST;
            } else if (methodName.equals("blacklist")) {
                return FilterType.BLACKLIST;
            } else {
                return FilterType.DISABLED;
            }
        }
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
}
