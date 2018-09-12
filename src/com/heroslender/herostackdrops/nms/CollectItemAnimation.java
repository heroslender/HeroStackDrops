package com.heroslender.herostackdrops.nms;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class CollectItemAnimation extends NMS {
    public CollectItemAnimation(final Player player, final Item item) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // Criar uma copia da entidade para o player nao coletar a entidade original
        Object entityItem = getNMSClass("EntityItem").getConstructor(getNMSClass("World"), double.class, double.class, double.class, getNMSClass("ItemStack"))
                .newInstance(getOBCClass("CraftWorld").getMethod("getHandle").invoke(getOBCClass("CraftWorld").cast(item.getWorld())),
                        item.getLocation().getX(),
                        item.getLocation().getY(),
                        item.getLocation().getZ(),
                        getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item.getItemStack()));

        Object p = getNMSClass("PacketPlayOutSpawnEntity").getConstructor(getNMSClass("Entity"), int.class, int.class).newInstance(entityItem, 2, 100);
        Object data = getNMSClass("PacketPlayOutEntityMetadata").getConstructor(int.class, getNMSClass("DataWatcher"), boolean.class)
                .newInstance(entityItem.getClass().getMethod("getId").invoke(entityItem), entityItem.getClass().getMethod("getDataWatcher").invoke(entityItem), true);
        Object playAnim = getCollectAnimationPacket(player, entityItem);

        Location loc = player.getLocation();
        for (Player target : player.getWorld().getPlayers()) {
            if (loc.distanceSquared(target.getLocation()) < 100) {
                sendPacket(target, p);
                sendPacket(target, data);
                sendPacket(target, playAnim);
            }
        }
    }

    private Object getCollectAnimationPacket(Player player, Object entityItem) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Object playAnim;
        try {
            playAnim = getNMSClass("PacketPlayOutCollect").getConstructor(int.class, int.class).newInstance(entityItem.getClass().getMethod("getId").invoke(entityItem), player.getEntityId());
            sendPacket(player, playAnim);
        } catch (NoSuchMethodException exception) {
            playAnim = getNMSClass("PacketPlayOutCollect").getConstructor(int.class, int.class, int.class).newInstance(entityItem.getClass().getMethod("getId").invoke(entityItem), player.getEntityId(), 0);
            sendPacket(player, playAnim);
        }
        return playAnim;
    }
}