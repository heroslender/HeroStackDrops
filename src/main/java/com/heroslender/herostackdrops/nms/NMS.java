package com.heroslender.herostackdrops.nms;

import com.heroslender.herostackdrops.StackDrops;
import lombok.val;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.SplittableRandom;
import java.util.UUID;

/**
 * Created by Heroslender.
 */
public class NMS {
    private static final SplittableRandom RANDOM = new SplittableRandom();
    private static final EntityDataAccessor<ItemStack> DATA_ITEM;

    static {
        EntityDataAccessor<net.minecraft.world.item.ItemStack> dataItem = null;
        try {
            for (Field field : ItemEntity.class.getDeclaredFields()) {
                if (field.getType().equals(EntityDataAccessor.class)) {
                    field.setAccessible(true);
                    dataItem = (EntityDataAccessor<net.minecraft.world.item.ItemStack>) field.get(null);
                    break;
                }
            }
//            val field = ItemEntity.class.getDeclaredField("DATA_ITEM");
//            dataItem = (EntityDataAccessor<net.minecraft.world.item.ItemStack>) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DATA_ITEM = dataItem;
    }

    public static void registerCommand(Command command) {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register(StackDrops.getInstance().getName(), command);
    }

    public static void displayCollectItem(final Player player, final Item item) {
        try {
            val id = RANDOM.nextInt(99000, 100000);
            ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
                id,
                UUID.randomUUID(),
                item.getLocation().getX(),
                item.getLocation().getY(),
                item.getLocation().getZ(),
                0F,
                0F,
                EntityType.ITEM,
                0,
                Vec3.ZERO
            );

            val watcher = new SynchedEntityData(null);
            val i = item.getItemStack().clone();
            i.setAmount(1);
            watcher.define(DATA_ITEM, CraftItemStack.asNMSCopy(i));

            ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(
                id,
                watcher,
                true
            );

            val playAnim = new ClientboundTakeItemEntityPacket(id, player.getEntityId(), 1);

            Location loc = player.getLocation();
            for (Player target : player.getWorld().getPlayers()) {
                if (loc.distanceSquared(target.getLocation()) < 100) {
                    val connection = ((CraftPlayer) target).getHandle().connection;
                    connection.send(packet);
                    connection.send(dataPacket);
                    connection.send(playAnim);
                }
            }
        } catch (Exception ignore) {
        }
    }
}
