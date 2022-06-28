package com.heroslender.herostackdrops.nms.v1_8_R3;

import com.heroslender.herostackdrops.nms.NmsFacade;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class NmsFacadeImpl implements NmsFacade {
    private final Field itemHandleField;

    public NmsFacadeImpl() {
        Field handle = null;
        try {
            handle = CraftItemStack.class.getDeclaredField("handle");
            handle.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }

        this.itemHandleField = handle;
    }

    @Override
    @Nullable
    public String getI18nDisplayName(@NotNull ItemStack itemStack) {
        if (itemHandleField == null) {
            return null;
        }

        try {
            CraftItemStack craftItemStack;
            if (itemStack instanceof CraftItemStack) {
                craftItemStack = (CraftItemStack) itemStack;
            } else {
                craftItemStack = CraftItemStack.asCraftCopy(itemStack);
            }

            try {
                net.minecraft.server.v1_8_R3.ItemStack nmsStack
                    = (net.minecraft.server.v1_8_R3.ItemStack) itemHandleField.get(craftItemStack);
                return nmsStack.getItem().a(nmsStack);
            } catch (IllegalAccessException ignored) {
            }
        } catch (Exception e) {
            System.out.println("Failed to get translated item name");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void sendItemPickupAnimation(@NotNull Player player, @NotNull Item item) {
        try {
            Location loc = item.getLocation();
            EntityItem entity = new EntityItem(
                ((CraftWorld) item.getWorld()).getHandle(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                CraftItemStack.asNMSCopy(item.getItemStack())
            );

            PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(entity, 2, 100);
            PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
            PacketPlayOutCollect collectPacket = new PacketPlayOutCollect(entity.getId(), player.getEntityId());

            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(spawnPacket);
            connection.sendPacket(metadataPacket);
            connection.sendPacket(collectPacket);
        } catch (Exception e) {
            System.out.println("Failed to send item pickup animation");
            e.printStackTrace();
        }
    }
}
