package com.heroslender.herostackdrops.listener;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.controller.ConfigurationController;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import static com.heroslender.herostackdrops.config.Constants.META_KEY;

@RequiredArgsConstructor
public class ItemListener implements Listener {
    private final ConfigurationController configurationController;

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onItemSpawn(final ItemSpawnEvent e) {
        if (e.isCancelled() || isWorldBlocked(e.getEntity().getWorld()))
            return;

        Item item = e.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (!configurationController.isItemAllowed(itemStack)) return;

        if (configurationController.getStackOnSpawn()) {
            for (Item targetItem : configurationController.getNearby(item)) {
                if (targetItem.hasMetadata(META_KEY) && targetItem.getItemStack().isSimilar(itemStack)) {
                    e.setCancelled(true);
                    int quant = targetItem.getMetadata(META_KEY).get(0).asInt() + itemStack.getAmount();
                    StackDrops.getInstance().updateItem(targetItem, quant);
                    // Resetar a idade do item, para ele nao dar despawn rapidamente
                    targetItem.setTicksLived(2);
                    return;
                }

            }
        }
        StackDrops.getInstance().updateItem(item, itemStack.getAmount());
    }

    /**
     * Quando 2 itens ja dropados se juntam, atualizar a quantidade de um e remover o outro
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onItemMerge(final ItemMergeEvent e) {
        if (e.isCancelled() || isWorldBlocked(e.getEntity().getWorld())) {
            return;
        }

        Item originalItem = e.getEntity();
        Item targetItem = e.getTarget();
        if (!targetItem.hasMetadata(META_KEY) && !originalItem.hasMetadata(META_KEY) && !configurationController.isItemAllowed(originalItem.getItemStack()))
            return;

        int targetQuantidade = targetItem.hasMetadata(META_KEY) ? targetItem.getMetadata(META_KEY).get(0).asInt() : targetItem.getItemStack().getAmount();
        int originalQuantidade = originalItem.hasMetadata(META_KEY) ? originalItem.getMetadata(META_KEY).get(0).asInt() : originalItem.getItemStack().getAmount();
        StackDrops.getInstance().updateItem(targetItem, targetQuantidade + originalQuantidade);
        targetItem.setTicksLived(2);
        originalItem.remove();
        e.setCancelled(true);
    }

    private boolean isWorldBlocked(final World world) {
        if (world == null) {
            return true;
        }

        return configurationController.getBlockedWorlds().contains(world.getName());
    }
}
