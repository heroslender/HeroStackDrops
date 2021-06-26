package com.heroslender.herostackdrops.controller;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.services.ConfigurationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.lang.Math.min;

@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @Getter private StackMethod method;
    @Getter private List<MaterialData> items;
    @Getter private List<String> blockedWorlds;
    @Getter private String itemName;
    @Getter private double stackRadius;
    @Getter private boolean stackOnSpawn;
    @Getter private boolean showAnimation;

    public void init() {
        val config = configurationService.getConfig();
        method = getStackMethodFrom(config.getString("restringir-itens.method", "DESATIVADO"));

        items = new ArrayList<>(getItems(config.getStringList("restringir-itens.itens")));
        if (method != StackMethod.ALL) {
            for (int i = 0; i < min(items.size(), 10); i++) {
                StackDrops.getInstance().getLogger().info("Loaded item " + items.get(i).getItemType().name() + ";");
            }

            if (items.size() >= 10) {
                StackDrops.getInstance().getLogger().info("(...)");
            }
        }

        blockedWorlds = config.getStringList("mundos-bloqueados");
        if (blockedWorlds == null) {
            blockedWorlds = Collections.emptyList();
        }

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
        val data = new MaterialData(type);
        if (type.getData() != null) {
            data.setData((byte) itemStack.getDurability());
        }

        boolean itemsContains = false;
        for (MaterialData materialData : items) {
            if (materialData.equals(data)) {
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
        switch (method.toLowerCase()) {
            case "whitelist":
                return StackMethod.WHITELIST;
            case "blacklist":
                return StackMethod.BLACKLIST;
            default:
                return StackMethod.ALL;
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
        val parts = source.split(":");

        val mat = Material.matchMaterial(parts[0]);
        if (mat == null) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "O material '" + parts[0] + "' nao existe!");
            return Optional.empty();
        }

        val materialData = new MaterialData(mat);

        if (parts.length > 1) {
            try {
                materialData.setData(parts[1]);
            } catch (NumberFormatException ignore) {
            }
        }

        return Optional.of(materialData);
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

    private static class MaterialData extends org.bukkit.material.MaterialData {
        private static final boolean HAS_MATERIAL_ID = hasMethod(MaterialData.class, "getItemTypeId");
        private boolean ignoreData;
        private final Material mat;

        public MaterialData(Material type) {
            super(type);

            this.mat = type;
            this.ignoreData = type.getMaxDurability() > 0;
        }

        @Override
        public Material getItemType() {
            return mat;
        }

        public void setData(String data) {
            if (data.equals("*")) {
                ignoreData = true;
                return;
            }

            // Item durability is stored as a short, so we need to parse to short first and then to byte
            setData((byte) Short.parseShort(data));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof org.bukkit.material.MaterialData) {
                org.bukkit.material.MaterialData md = (org.bukkit.material.MaterialData) obj;

                final boolean typeEquals = HAS_MATERIAL_ID ? md.getItemTypeId() == getItemTypeId() : md.getItemType() == getItemType();
                return typeEquals && (ignoreData || md.getData() == getData());
            } else {
                return false;
            }
        }

        private static boolean hasMethod(Class<?> clazz, String methodName) {
            try {
                clazz.getDeclaredMethod(methodName);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    }

}
