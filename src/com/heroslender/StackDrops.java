package com.heroslender;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Heroslender.
 */
// TODO - adicionar comando para reiniciar a config :)
public class StackDrops extends JavaPlugin implements Listener {
    private static final String META_KEY = "heroQuant";
    private Metodo metodo;
    private List<Material> itens;
    private String nomeItem;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        itens = new ArrayList<>();

        switch (getConfig().getString("restringir-itens.metodo", "BLACKLIST").toLowerCase()) {
            case "whitelist":
                metodo = Metodo.WHITELIST;
                break;
            case "blacklist":
                metodo = Metodo.BLACKLIST;
                break;
            default:
                metodo = Metodo.DESATIVADO;
                break;
        }

        if (getConfig().contains("restringir-itens.itens"))
            for (String s : getConfig().getStringList("restringir-itens.itens")) {
                try {
                    itens.add(Material.valueOf(s));
                } catch (NoSuchFieldError e) {
                    getLogger().warning("O material '" + s + "' nao existe!");
                }
            }

        nomeItem = getConfig().getBoolean("holograma.ativado", true)
                ? getConfig().getString("holograma.texto", "&7{quantidade}x &e{nome}").replace('&', '§')
                : null;

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (e.isCancelled())
            return;

        Item item = e.getEntity();
        ItemStack itemStack = item.getItemStack();

        if ((metodo == Metodo.BLACKLIST && itens.contains(itemStack.getType()))
                || (metodo == Metodo.WHITELIST && !itens.contains(itemStack.getType())))
            return;

        for (Entity entity : item.getNearbyEntities(5D, 5D, 5D)) {
            if (entity instanceof Item) {
                Item targetItem = (Item) entity;
                if (targetItem.hasMetadata(META_KEY) && targetItem.getItemStack().isSimilar(itemStack)) {
                    e.setCancelled(true);
                    int quant = targetItem.getMetadata(META_KEY).get(0).asInt() + itemStack.getAmount();
                    updateItem(targetItem, quant);
                    // Colocar para o iten despawnar apenas passados 60 segundos
                    NMS.resetDespawnDelay(targetItem);
//                    ((EntityItem) ((CraftItem) targetItem).getHandle()).j();
                    return;
                }
            }
        }
        updateItem(item, itemStack.getAmount());
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        if (e.isCancelled() || !e.getItem().hasMetadata(META_KEY)) return;
        e.setCancelled(true);

        Item item = e.getItem();
        int quant = item.getMetadata(META_KEY).get(0).asInt();

        while (quant > 0) {
            int stackSize = (quant > item.getItemStack().getType().getMaxStackSize()) ? item.getItemStack().getType().getMaxStackSize() : quant;

            ItemStack itemStack = item.getItemStack().clone();
            itemStack.setAmount(stackSize);
            Map<Integer, ItemStack> result = e.getPlayer().getInventory().addItem(itemStack);
            if (!result.isEmpty()) {
                quant -= stackSize - result.values().iterator().next().getAmount();
                break;
            }
            quant -= stackSize;
        }

        collectItem(e.getPlayer(), e.getItem());
        if (quant == 0) {
            item.remove();
        } else {
            updateItem(item, quant);
        }
    }

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent e) {
        if (e.isCancelled()
                || !e.getInventory().getType().equals(InventoryType.HOPPER)
                || !e.getItem().hasMetadata(META_KEY))
            return;
        e.setCancelled(true);

        Item item = e.getItem();
        int quant = item.getMetadata(META_KEY).get(0).asInt();

        while (quant > 0) {
            int stackSize = (quant > item.getItemStack().getType().getMaxStackSize()) ? item.getItemStack().getType().getMaxStackSize() : quant;

            ItemStack itemStack = item.getItemStack().clone();
            itemStack.setAmount(stackSize);
            Map<Integer, ItemStack> result = e.getInventory().addItem(itemStack);
            if (!result.isEmpty()) {
                quant -= stackSize - result.values().iterator().next().getAmount();
                break;
            }
            quant -= stackSize;
        }
        if (quant == 0)
            item.remove();
        else {
            updateItem(item, quant);
        }
    }

    private void updateItem(Item item, int quantidade) {
        item.setMetadata(META_KEY, new FixedMetadataValue(this, quantidade));
        if (nomeItem != null) {
            item.setCustomName(nomeItem
                    .replace("{quantidade}", quantidade + "")
                    .replace("{nome}", NMS.getNome(item.getItemStack())));
            if (!item.isCustomNameVisible())
                item.setCustomNameVisible(true);
        }
    }

    private void collectItem(Player player, Item item) {
        try {
            NMS.displayCollectItem(player, item);
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.5F, 10F);
        } catch (NoSuchFieldError e) {
            try {
                // Versoes mais recentes tem o nome do som diferente, mt viagem...
                player.playSound(player.getLocation(), Sound.valueOf("ENTITY_ITEM_PICKUP"), 0.5F, 10F);
            } catch (NoSuchFieldError er) {
                // Parece que ta com uma versao mais recente do que eu planejava haushuahsuahsu
                er.printStackTrace();
            }
        } catch (Exception e) {
            // Apanhar todas as exceçoes para nao travar a execuçao do parent,
            // ate porque este não é um metodo muito importante, é
            // apenas efeito visual
            e.printStackTrace();
        }
    }

    enum Metodo {
        WHITELIST,
        BLACKLIST,
        DESATIVADO
    }
}
