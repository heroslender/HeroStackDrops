package com.heroslender.herostackdrops.nms;

import com.heroslender.herostackdrops.StackDrops;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
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
public class NMS {
    private static Field handleField;
    private static Method getItemMethod;
    private static Method itemGetNameMethod;
    private static boolean isNmsNameEnabled = true;

    static {
        try {
            handleField = getOBCClass("inventory.CraftItemStack").getDeclaredField("handle");
            handleField.setAccessible(true);

            Class<?> itemStack = getNMSClass("ItemStack");
            getItemMethod = itemStack.getDeclaredMethod("getItem");
            itemGetNameMethod = getNMSClass("Item").getDeclaredMethod("a", itemStack);
        } catch (Exception error) {
            isNmsNameEnabled = false;
            StackDrops.getInstance().getLogger().log(Level.SEVERE, "Ocurreu um erro ao inicializar as variaveis de pegar o nome do ItemStack em NMS");
        }
    }

    public static void registerCommand(Command command) {
        try {
            Object craftServer = getOBCClass("CraftServer").cast(Bukkit.getServer());
            Object commandMap = craftServer.getClass().getMethod("getCommandMap").invoke(craftServer);

            commandMap.getClass().getMethod("register", String.class, Command.class).invoke(commandMap, StackDrops.getInstance().getDescription().getName(), command);
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao registar o comando", error);
        }
    }

    public static void displayCollectItem(final Player player, final Item item) {
        try {
            new CollectItemAnimation(player, item);
        } catch (Exception ignore) {
        }
    }

    public static String getNome(final ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return itemStack.getItemMeta().getDisplayName();

        if (isNmsNameEnabled) {
            try {
                Object handle = handleField.get(itemStack);
                return (String) itemGetNameMethod.invoke(getItemMethod.invoke(handle), handle);
            } catch (IllegalAccessException | InvocationTargetException error) {
                StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao pegar o nome do ItemStack em NMS", error);
            }
        }

        return itemStack.getType().name().replace('_', ' ').toLowerCase();
    }

    static void sendPacket(final Player player, final Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao tacar o packet no player", error);
        }
    }

    static Class<?> getOBCClass(final String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao pegar a classe '" + name + "' do CraftBukkit", error);
            return null;
        }
    }

    static Class<?> getNMSClass(final String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException error) {
            StackDrops.getInstance().getLogger().log(Level.WARNING, "Ocurreu um erro ao pegar a classe '" + name + "' do NMS", error);
            return null;
        }
    }
}
