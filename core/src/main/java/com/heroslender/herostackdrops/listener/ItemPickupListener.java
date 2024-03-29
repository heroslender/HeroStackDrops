package com.heroslender.herostackdrops.listener;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.controller.ConfigurationController;
import com.heroslender.herostackdrops.event.PlayerPrePickupItemEvent;
import com.heroslender.herostackdrops.nms.NmsFacade;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.heroslender.herostackdrops.config.Constants.META_KEY;
import static java.lang.Math.min;

public class ItemPickupListener implements Listener {
    private final StackDrops plugin;
    private final Logger logger;
    private final ConfigurationController configurationController;

    public ItemPickupListener(StackDrops plugin, ConfigurationController configurationController) {
        this.plugin = plugin;
        this.configurationController = configurationController;

        logger = plugin.getLogger();
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
        val prePickupEvent = new PlayerPrePickupItemEvent(player, item);
        Bukkit.getPluginManager().callEvent(prePickupEvent);
        if (prePickupEvent.isCancelled()) {
            return true;
        }

        val metadata = item.getMetadata(META_KEY);
        if (metadata.isEmpty()) {
            return false;
        }

        val amount = metadata.get(0).asInt();
        val leftOver = addItemToInv(item.getItemStack(), amount, inventory);

        if (player != null) {
            val pickupEvent = new com.heroslender.herostackdrops.event.PlayerPickupItemEvent(player, item, amount - leftOver);
            Bukkit.getPluginManager().callEvent(pickupEvent);
        }

        if (player != null)
            collectItem(player, item);
        if (leftOver == 0) {
            item.remove();
        } else {
            StackDrops.getInstance().updateItem(item, leftOver);
        }

        return true;
    }

    private int addItemToInv(final ItemStack itemStack, int amount, final Inventory inventory) {
        int maxStackSize = itemStack.getMaxStackSize();

        Map<Integer, ItemStack> result;
        do {
            int stackSize = min(maxStackSize, amount);
            itemStack.setAmount(stackSize);
            amount -= stackSize;
            result = inventory.addItem(itemStack);
        } while (result.isEmpty() && amount > 0);

        if (!result.isEmpty()) {
            return amount + result.values().iterator().next().getAmount();
        }

        return amount;
    }

    private void collectItem(final Player player, final Item item) {
        if (!configurationController.isShowAnimation()) {
            return;
        }

        try {
            NmsFacade nmsFacade = plugin.getNmsFacade();
            if (nmsFacade != null) {
                nmsFacade.sendItemPickupAnimation(player, item);
            }

            try {
                player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5F, 10F);
            } catch (NoSuchFieldError e) {
                try {
                    // Versoes mais recentes tem o nome do som diferente, mt viagem...
                    player.playSound(player.getLocation(), Sound.valueOf("ENTITY_ITEM_PICKUP"), 0.5F, 10F);
                } catch (NoSuchFieldError er) {
                    // Parece que ta com uma versao mais recente do que eu planejava haushuahsuahsu
                    logger.log(Level.WARNING, "Essa versão do spigot ainda não é totalmente suportada.", er);
                }
            }
        } catch (Exception e) {
            // Apanhar todas as exceçoes para nao travar a execuçao do parent, ate
            // porque este não é um metodo muito importante, é apenas efeito visual.
            logger.log(Level.WARNING, "Ocurreu um erro ao amostrar a animação/som de coletar.", e);
        }
    }
}
