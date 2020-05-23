package com.heroslender.herostackdrops.listener;

import com.google.common.collect.Maps;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static com.heroslender.herostackdrops.config.Constants.META_KEY;

@RequiredArgsConstructor
public class ItemListener implements Listener {
    private final ConfigurationController configurationController;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMobKill(final EntityDeathEvent e) {
        if (isWorldBlocked(e.getEntity().getWorld()))
            return;

        Map<ItemStack, Integer> toDrop = Maps.newHashMap();

        for (ItemStack drop : e.getDrops()) {
            boolean added = false;
            for (Map.Entry<ItemStack, Integer> entry : toDrop.entrySet()) {
                if (entry.getKey().isSimilar(drop)) {
                    toDrop.put(entry.getKey(), entry.getValue() + drop.getAmount());
                    added = true;
                    break;
                }
            }

            if (!added) {
                toDrop.put(drop, drop.getAmount());
            }
        }

        for (Map.Entry<ItemStack, Integer> entry : toDrop.entrySet()) {
            spawnStack(entry.getKey(), entry.getValue(), e.getEntity(), null);
        }

        e.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onItemSpawn(final ItemSpawnEvent e) {
        if (isWorldBlocked(e.getEntity().getWorld()))
            return;

        val item = e.getEntity();
        val itemStack = item.getItemStack();

        val cancelEvent = spawnStack(itemStack, itemStack.getAmount(), item, item);

        e.setCancelled(cancelEvent);
    }


    private boolean spawnStack(ItemStack itemStack, int itemAmount, Entity source, @Nullable Item item) {
        if (configurationController.isItemDisabled(itemStack)) return false;

        if (configurationController.isStackOnSpawn()) {
            for (Entity entity : configurationController.getNearby(source)) {
                if (!(entity instanceof Item)) {
                    continue;
                }

                Item targetItem = (Item) entity;
                if (targetItem.getItemStack().isSimilar(itemStack)) {
                    val metadata = targetItem.getMetadata(META_KEY);
                    if (!metadata.isEmpty()) {
                        int amount = metadata.get(0).asInt() + itemAmount;
                        StackDrops.getInstance().updateItem(targetItem, amount);

                        // Reset the item age, preventing it from despawning
                        targetItem.setTicksLived(2);
                        return true;
                    }
                }
            }
        }
        if (item == null) {
            item = source.getWorld().dropItemNaturally(source.getLocation(), itemStack);
        }
        StackDrops.getInstance().updateItem(item, itemAmount);
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onItemMerge(final ItemMergeEvent e) {
        if (isWorldBlocked(e.getEntity().getWorld())) {
            return;
        }

        val source = e.getEntity();
        val target = e.getTarget();
        if (configurationController.isItemDisabled(source.getItemStack())) {
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

    private boolean isWorldBlocked(@NotNull final World world) {
        Objects.requireNonNull(world, "world is required");

        return configurationController.getBlockedWorlds().contains(world.getName());
    }
}
