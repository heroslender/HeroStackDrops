package com.heroslender.herostackdrops;

import com.heroslender.herostackdrops.command.CommandStackdrops;
import com.heroslender.herostackdrops.event.ItemUpdateEvent;
import com.heroslender.herostackdrops.nms.NMS;
import lombok.val;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Heroslender.
 */
public class StackDrops extends JavaPlugin implements Listener {
    private static final String META_KEY = "heroQuant";

    private static StackDrops instance;
    private Config config;

    public StackDrops() {
        instance = this;
        saveDefaultConfig();
        config = new Config(this);
    }

    public static StackDrops getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        NMS.registerCommand(new CommandStackdrops());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onItemSpawn(final ItemSpawnEvent e) {
        if (e.isCancelled())
            return;

        Item item = e.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (!config.isItemAllowed(itemStack)) return;

        if (config.getStackOnSpawn()) {
            for (Entity entity : item.getNearbyEntities(config.getStackRadius(), config.getStackRadius(), config.getStackRadius())) {
                if (entity instanceof Item) {
                    Item targetItem = (Item) entity;
                    if (targetItem.hasMetadata(META_KEY) && targetItem.getItemStack().isSimilar(itemStack)) {
                        e.setCancelled(true);
                        int quant = targetItem.getMetadata(META_KEY).get(0).asInt() + itemStack.getAmount();
                        updateItem(targetItem, quant);
                        // Resetar a idade do item, para ele nao dar despawn rapidamente
                        targetItem.setTicksLived(2);
                        return;
                    }
                }
            }
        }
        updateItem(item, itemStack.getAmount());
    }

    /**
     * Quando 2 itens ja dropados se juntam, atualizar a quantidade de um e remover o outro
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onItemMerge(final ItemMergeEvent e) {
        Item originalItem = e.getEntity();
        Item targetItem = e.getTarget();
        if (!targetItem.hasMetadata(META_KEY) && !originalItem.hasMetadata(META_KEY) && !config.isItemAllowed(originalItem.getItemStack()))
            return;

        int targetQuantidade = targetItem.hasMetadata(META_KEY) ? targetItem.getMetadata(META_KEY).get(0).asInt() : targetItem.getItemStack().getAmount();
        int originalQuantidade = originalItem.hasMetadata(META_KEY) ? originalItem.getMetadata(META_KEY).get(0).asInt() : originalItem.getItemStack().getAmount();
        updateItem(targetItem, targetQuantidade + originalQuantidade);
        targetItem.setTicksLived(2);
        originalItem.remove();
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerPickup(final PlayerPickupItemEvent e) {
        e.setCancelled(
                performPickup(
                        e.getItem(),
                        e.getPlayer().getInventory(),
                        e.getPlayer()
                )
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHopperPickup(final InventoryPickupItemEvent e) {
        if (!e.getInventory().getType().equals(InventoryType.HOPPER))
            return;

        e.setCancelled(
                performPickup(
                        e.getItem(),
                        e.getInventory(),
                        null
                )
        );
    }

    private boolean performPickup(final Item item, final Inventory inventory, final Player player) {
        if (!item.hasMetadata(META_KEY)) return false;

        val startQuant = item.getMetadata(META_KEY).get(0).asInt();
        int quant = startQuant;
        while (quant > 0) {
            int stackSize = (quant > item.getItemStack().getType().getMaxStackSize()) ? item.getItemStack().getType().getMaxStackSize() : quant;

            ItemStack itemStack = item.getItemStack().clone();
            itemStack.setAmount(stackSize);
            Map<Integer, ItemStack> result = inventory.addItem(itemStack);
            if (!result.isEmpty()) {
                quant -= stackSize - result.values().iterator().next().getAmount();
                break;
            }
            quant -= stackSize;
        }

        if (player != null) {
            val pickupEvent = new com.heroslender.herostackdrops.event.PlayerPickupItemEvent(player, item, startQuant - quant);
            getServer().getPluginManager().callEvent(pickupEvent);
        }

        if (player != null)
            collectItem(player, item);
        if (quant == 0) {
            item.remove();
        } else {
            updateItem(item, quant);
        }
        return true;
    }

    private void updateItem(final Item item, final int quantidade) {
        ItemUpdateEvent itemUpdateEvent = new ItemUpdateEvent(item, config.getItemName(), quantidade);
        getServer().getPluginManager().callEvent(itemUpdateEvent);

        updateItemSilent(itemUpdateEvent.getEntity(), itemUpdateEvent.getHologramTextFormat(), itemUpdateEvent.getQuantity());
    }

    private void updateItemSilent(final Item item, final String itemName, final int quantidade) {
        // Atualizar a MetaData do Item
        item.setMetadata(META_KEY, new FixedMetadataValue(this, quantidade));
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

    private void collectItem(final Player player, final Item item) {
        try {
            NMS.displayCollectItem(player, item);
            try {
                player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5F, 10F);
            } catch (NoSuchFieldError e) {
                try {
                    // Versoes mais recentes tem o nome do som diferente, mt viagem...
                    player.playSound(player.getLocation(), Sound.valueOf("ENTITY_ITEM_PICKUP"), 0.5F, 10F);
                } catch (NoSuchFieldError er) {
                    // Parece que ta com uma versao mais recente do que eu planejava haushuahsuahsu
                    getLogger().log(Level.WARNING, "Essa versão do spigot ainda não é totalmente suportada.", er);
                }
            }
        } catch (Exception e) {
            // Apanhar todas as exceçoes para nao travar a execuçao do parent, ate
            // porque este não é um metodo muito importante, é apenas efeito visual.
            getLogger().log(Level.WARNING, "Ocurreu um erro ao amostrar a animação/som de coletar.", e);
        }
    }

    public void reloadConfiguration() {
        reloadConfig();
        config = new Config(this);
    }

    enum Metodo {
        WHITELIST,
        BLACKLIST,
        DESATIVADO
    }
}
