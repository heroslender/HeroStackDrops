package com.heroslender.herostackdrops.nms;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NmsFacade {

    @Nullable
    String getI18nDisplayName(@NotNull ItemStack itemStack);

    void sendItemPickupAnimation(@NotNull Player player, @NotNull Item item);
}
