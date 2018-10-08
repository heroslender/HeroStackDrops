package com.heroslender.herostackdrops.event;

import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class ItemUpdateEvent extends EntityEvent {
    private static final HandlerList handlers = new HandlerList();

    private String hologramTextFormat;
    private int quantity;

    public ItemUpdateEvent(Item item, String hologramTextFormat, int quantity) {
        super(item);
        this.hologramTextFormat = hologramTextFormat;
        this.quantity = quantity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getHologramTextFormat() {
        return hologramTextFormat;
    }

    public void setHologramTextFormat(String hologramTextFormat) {
        this.hologramTextFormat = hologramTextFormat;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Item getEntity() {
        return (Item) this.entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
