package com.heroslender.herostackdrops;

import com.heroslender.herostackdrops.command.CommandStackdrops;
import com.heroslender.herostackdrops.config.Constants;
import com.heroslender.herostackdrops.controller.ConfigurationController;
import com.heroslender.herostackdrops.event.ItemUpdateEvent;
import com.heroslender.herostackdrops.listener.ItemListener;
import com.heroslender.herostackdrops.listener.ItemPickupListener;
import com.heroslender.herostackdrops.nms.NmsFacade;
import com.heroslender.herostackdrops.services.ConfigurationService;
import com.heroslender.herostackdrops.services.ConfigurationServiceImpl;
import com.heroslender.herostackdrops.version.NMSVersion;
import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Heroslender.
 */
public class StackDrops extends JavaPlugin {
    @Getter
    private static StackDrops instance;
    @Getter
    private final ConfigurationController configurationController;

    @Getter
    private final NmsFacade nmsFacade;

    public StackDrops() {
        instance = this;
        saveDefaultConfig();

        NMSVersion currentVersion = NMSVersion.CURRENT;
        if (NMSVersion.V1_8_R3.equals(currentVersion)) {
            nmsFacade = new com.heroslender.herostackdrops.nms.v1_8_R3.NmsFacadeImpl();
        } else if (NMSVersion.V1_18_R2.equals(currentVersion)) {
            nmsFacade = new com.heroslender.herostackdrops.nms.v1_18_R2.NmsFacadeImpl();
        } else {
            nmsFacade = null;
        }

        ConfigurationService configurationService = new ConfigurationServiceImpl(this);
        this.configurationController = new ConfigurationController(configurationService);
    }

    @Override
    public void onEnable() {
        configurationController.init();

        getServer().getPluginCommand("herostackdrops").setExecutor(new CommandStackdrops());

        getServer().getPluginManager().registerEvents(new ItemListener(configurationController), this);
        getServer().getPluginManager().registerEvents(new ItemPickupListener(this, configurationController), this);

        // https://bstats.org/plugin/bukkit/HeroStackDrops
        new Metrics(this, 5041);
    }

    public void updateItem(final Item item, final int amount) {
        ItemUpdateEvent itemUpdateEvent = new ItemUpdateEvent(item, configurationController.getItemName(), amount);
        getServer().getPluginManager().callEvent(itemUpdateEvent);

        updateItemSilent(itemUpdateEvent.getEntity(), itemUpdateEvent.getHologramTextFormat(), itemUpdateEvent.getAmount());
    }

    public void updateItemSilent(final Item item, final String itemName, final int amount) {
        item.setMetadata(Constants.META_KEY, new FixedMetadataValue(this, amount));
        // Alterar a quantiade para 1, permitindo assim juntar com outros packs
        item.getItemStack().setAmount(1);

        if (itemName != null) {
            item.setCustomName(itemName
                .replace("{quantidade}", Integer.toString(amount))
                .replace("{nome}", getName(item.getItemStack())));
            if (!item.isCustomNameVisible())
                item.setCustomNameVisible(true);
        }
    }

    private String getName(final ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();

            if (meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }

        if (nmsFacade != null) {
            final String name = nmsFacade.getI18nDisplayName(itemStack);
            if (name != null) {
                return name;
            }
        }

        final StringBuilder nameBuilder = new StringBuilder();
        final String name = itemStack.getType().name();
        int prevIndex = 0;
        int index = 0;
        while ((index = name.indexOf(' ', index)) != -1) {
            final String text = name.substring(prevIndex + 1, index);
            nameBuilder.append(Character.toUpperCase(text.charAt(0)));
            nameBuilder.append(text.substring(1));
            nameBuilder.append(' ');

            prevIndex = index;
        }
        final String text = name.substring(prevIndex + 1);
        nameBuilder.append(Character.toUpperCase(text.charAt(0)));
        nameBuilder.append(text.substring(1));

        return nameBuilder.toString();
    }

    public void reloadConfiguration() {
        reloadConfig();
        configurationController.init();
    }
}
