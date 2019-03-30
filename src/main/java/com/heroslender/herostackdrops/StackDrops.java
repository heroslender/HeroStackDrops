package com.heroslender.herostackdrops;

import com.heroslender.herostackdrops.command.CommandStackdrops;
import com.heroslender.herostackdrops.config.Constants;
import com.heroslender.herostackdrops.controller.ConfigurationController;
import com.heroslender.herostackdrops.event.ItemUpdateEvent;
import com.heroslender.herostackdrops.nms.NMS;
import com.heroslender.herostackdrops.services.ConfigurationService;
import com.heroslender.herostackdrops.services.ConfigurationServiceImpl;
import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Heroslender.
 */
public class StackDrops extends JavaPlugin {
    private static StackDrops instance;
    @Getter private final ConfigurationController configurationController;

    public StackDrops() {
        instance = this;
        saveDefaultConfig();

        ConfigurationService configurationService = new ConfigurationServiceImpl();
        this.configurationController = new ConfigurationController(configurationService);
    }

    public static StackDrops getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        NMS.registerCommand(new CommandStackdrops());
    }

    public void updateItem(final Item item, final int quantidade) {
        ItemUpdateEvent itemUpdateEvent = new ItemUpdateEvent(item, configurationController.getItemName(), quantidade);
        getServer().getPluginManager().callEvent(itemUpdateEvent);

        updateItemSilent(itemUpdateEvent.getEntity(), itemUpdateEvent.getHologramTextFormat(), itemUpdateEvent.getQuantity());
    }

    public void updateItemSilent(final Item item, final String itemName, final int quantidade) {
        // Atualizar a MetaData do Item
        item.setMetadata(Constants.META_KEY, new FixedMetadataValue(this, quantidade));
        // Alterar a quantiade para 1, permitindo assim juntar com outros packs
        item.getItemStack().setAmount(1);
        // Defenir o holograma no Item
        if (itemName != null) {
            item.setCustomName(itemName
                    .replace("{quantidade}", Integer.toString(quantidade))
                    .replace("{nome}", NMS.getNome(item.getItemStack())));
            if (!item.isCustomNameVisible())
                item.setCustomNameVisible(true);
        }
    }

    public void reloadConfiguration() {
        reloadConfig();
        configurationController.init();
    }
}
