package com.heroslender.herostackdrops.listener;

import com.heroslender.herostackdrops.StackDrops;
import com.heroslender.herostackdrops.controller.ConfigurationController;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.Objects;

import static com.heroslender.herostackdrops.config.Constants.META_KEY;

@RequiredArgsConstructor
public class ItemListener implements Listener {
    private final ConfigurationController configurationController;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onItemSpawn(final ItemSpawnEvent e) {
        if (isWorldBlocked(e.getEntity().getWorld()))
            return;

        val item = e.getEntity();
        val itemStack = item.getItemStack();

        if (!configurationController.isItemAllowed(itemStack)) return;

        if (configurationController.getStackOnSpawn()) {
            for (Entity entity : configurationController.getNearby(item)) {
                if (!(entity instanceof Item)) {
                    continue;
                }

                Item targetItem = (Item) entity;
                if (targetItem.getItemStack().isSimilar(itemStack)) {
                    val metadata = targetItem.getMetadata(META_KEY);
                    if (!metadata.isEmpty()) {
                        e.setCancelled(true);
                        int amount = metadata.get(0).asInt() + itemStack.getAmount();
                        StackDrops.getInstance().updateItem(targetItem, amount);

                        // Reset the item age, preventing it from despawning
                        targetItem.setTicksLived(2);
                        return;
                    }
                }
            }
        }
        StackDrops.getInstance().updateItem(item, itemStack.getAmount());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onItemMerge(final ItemMergeEvent e) {
        if (isWorldBlocked(e.getEntity().getWorld())) {
            return;
        }

        val source = e.getEntity();
        val target = e.getTarget();
        if (!configurationController.isItemAllowed(source.getItemStack())) {
            return;
        }

        val sourceMetadata = source.getMetadata(META_KEY);
        val targetMetadata = target.getMetadata(META_KEY);
        if (sourceMetadata.isEmpty() && targetMetadata.isEmpty()) {
            return;
        }

        int targetAmount = targetMetadata.isEmpty() ? target.getItemStack().getAmount() : targetMetadata.get(0).asInt();
        int sourceAmount = sourceMetadata.isEmpty() ? source.getItemStack().getAmount() : sourceMetadata.get(0).asInt();
        StackDrops.getInstance().updateItem(target, targetAmount + sourceAmount);
        target.setTicksLived(2);
        source.remove();
        e.setCancelled(true);
    }

    private boolean isWorldBlocked(final World world) {
        Objects.requireNonNull(world, "world is required");

        return configurationController.getBlockedWorlds().contains(world.getName());
    }
}
