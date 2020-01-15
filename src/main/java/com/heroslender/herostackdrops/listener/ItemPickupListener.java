package com.heroslender.herostackdrops.listener;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.config.Constants;
import com.heroslender.herostackdrops.controller.ConfigurationController;
import com.heroslender.herostackdrops.nms.NMS;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

@RequiredArgsConstructor
public class ItemPickupListener implements Listener {
    private final Logger logger = StackDrops.getInstance().getLogger();
    private final ConfigurationController configurationController;

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
            StackDrops.getInstance().getServer().getPluginManager().callEvent(pickupEvent);
        }

        if (player != null)
            collectItem(player, item);
        if (quant == 0) {
            item.remove();
        } else {
            StackDrops.getInstance().updateItem(item, quant);
        }
        return true;
    }

    private void collectItem(final Player player, final Item item) {
        if (!configurationController.getAnimation()) {
            return;
        }

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
