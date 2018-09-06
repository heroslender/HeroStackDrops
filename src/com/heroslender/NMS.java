package com.heroslender;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

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
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.SEVERE, "Ocurreu um erro ao inicializar as variaveis de pegar o nome do ItemStack em NMS", error);
        }
    }

    static {
        try {
            itemAge = getNMSClass("EntityItem").getDeclaredField("age");
            itemAge.setAccessible(true);
        } catch (NoSuchFieldException error) {
            StackDrops.getInstance().getLogger().log(Level.SEVERE, "Ocurreu um erro ao inicializar a variavel de resetar a idade do ItemStack em NMS", error);
        }
    }

    static void registerCommand(Command command){
        try {
            Object craftServer = getOBCClass("CraftServer").cast(Bukkit.getServer());
            Object commandMap = craftServer.getClass().getMethod("getCommandMap").invoke(craftServer);

            commandMap.getClass().getMethod("register", String.class, Command.class).invoke(commandMap, StackDrops.getInstance().getDescription().getName(), command);
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao registar o comando", error);
        }
    }

    static void displayCollectItem(final Player player, final Item item) {
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
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao aprensentar a animação de coletar o ItemStack em NMS", error);
        }
    }

    static void resetDespawnDelay(final Item item) {
        try {
            Object entityItem = item.getClass().getMethod("getHandle").invoke(item);
            itemAge.set(entityItem, 10);
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao resetar a idade do ItemStack em NMS", error);
        }
    }

    private static void sendPacket(final Player player, final Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao tacar o packet no player", error);
        }
    }

    static String getNome(final ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return itemStack.getItemMeta().getDisplayName();
        try {
            Object handle = handleField.get(itemStack);
            return (String) itemGetNameMethod.invoke(getItemMethod.invoke(handle), handle);
        } catch (IllegalAccessException | InvocationTargetException error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao pegar o nome do ItemStack em NMS", error);
        }
        return itemStack.getType().name().replace('_', ' ').toLowerCase();
    }

    private static Class<?> getOBCClass(final String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao pegar a classe '" + name + "' do CraftBukkit", error);
            return null;
        }
    }

    private static Class<?> getNMSClass(final String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao pegar a classe '" + name + "' do NMS", error);
            return null;
        }
    }
}
