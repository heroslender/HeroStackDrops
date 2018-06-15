package com.heroslender;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Heroslender.
 */
class NMS {
    private static Field handleField;
    private static Method getItemMethod;
    private static Method itemGetNameMethod;
    private static Field itemAge;

    static {
        try {
            handleField = getOBCClass("inventory.CraftItemStack").getDeclaredField("handle");
            handleField.setAccessible(true);

            Class<?> itemStack = getNMSClass("ItemStack");
            getItemMethod = itemStack.getDeclaredMethod("getItem");
            itemGetNameMethod = getNMSClass("Item").getDeclaredMethod("a", itemStack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            itemAge = getNMSClass("EntityItem").getDeclaredField("age");
            itemAge.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    static void displayCollectItem(Player player, Item item) {
        try {
            Object entityItem = getNMSClass("EntityItem").getConstructor(getNMSClass("World"), double.class, double.class, double.class, getNMSClass("ItemStack"))
                    .newInstance(getOBCClass("CraftWorld").getMethod("getHandle").invoke(getOBCClass("CraftWorld").cast(item.getWorld())),
                            item.getLocation().getX(),
                            item.getLocation().getY(),
                            item.getLocation().getZ(),
                            getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item.getItemStack()));

            Object p = getNMSClass("PacketPlayOutSpawnEntity").getConstructor(getNMSClass("Entity"), int.class, int.class).newInstance(entityItem, 2, 100);
            Object data = getNMSClass("PacketPlayOutEntityMetadata").getConstructor(int.class, getNMSClass("DataWatcher"), boolean.class)
                    .newInstance(entityItem.getClass().getMethod("getId").invoke(entityItem), entityItem.getClass().getMethod("getDataWatcher").invoke(entityItem), true);
            sendPacket(player, p);
            sendPacket(player, data);
            Object playAnim = null;
            try {
                playAnim = getNMSClass("PacketPlayOutCollect").getConstructor(int.class, int.class).newInstance(entityItem.getClass().getMethod("getId").invoke(entityItem), player.getEntityId());
                sendPacket(player, playAnim);
            } catch (NoSuchMethodException e) {
                playAnim = getNMSClass("PacketPlayOutCollect").getConstructor(int.class, int.class, int.class).newInstance(entityItem.getClass().getMethod("getId").invoke(entityItem), player.getEntityId(), 0);
                sendPacket(player, playAnim);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Location loc = player.getLocation();
            for (Player target : player.getWorld().getPlayers()) {
                if (loc.distanceSquared(target.getLocation()) < 100) {
                    sendPacket(target, p);
                    sendPacket(target, data);
                    sendPacket(target, playAnim);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void resetDespawnDelay(Item item) {
        try {
            Object entityItem = item.getClass().getMethod("getHandle").invoke(item);
            itemAge.set(entityItem, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String getNome(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return itemStack.getItemMeta().getDisplayName();
        try {
            Object handle = handleField.get(itemStack);
            return (String) itemGetNameMethod.invoke(getItemMethod.invoke(handle), handle);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return itemStack.getType().name().replace('_', ' ').toLowerCase();
    }

    private static Class<?> getOBCClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
