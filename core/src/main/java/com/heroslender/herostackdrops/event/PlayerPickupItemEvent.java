package com.heroslender.herostackdrops.event;

import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerPickupItemEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    @Getter private int quantity;
    @Getter private Item item;

    public PlayerPickupItemEvent(Player player, Item item, int quantity) {
        super(player);
        this.item = item;
        this.quantity = quantity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
