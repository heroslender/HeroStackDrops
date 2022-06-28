package com.heroslender.herostackdrops.nms.v1_18_R2;

import com.heroslender.herostackdrops.nms.NmsFacade;
import net.minecraft.locale.LocaleLanguage;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.item.EntityItem;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
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
            System.out.println("Failed to setup CraftItemStack handle field");
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
                net.minecraft.world.item.ItemStack nmsStack
                    = (net.minecraft.world.item.ItemStack) itemHandleField.get(craftItemStack);
                return LocaleLanguage.a().a(nmsStack.o());
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

            PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(entity, 1);
            PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(entity.ae(), entity.ai(), true);
            PacketPlayOutCollect collectPacket = new PacketPlayOutCollect(entity.ae(), player.getEntityId(), item.getItemStack().getAmount());

            PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
            connection.a(spawnPacket);
            connection.a(metadataPacket);
            connection.a(collectPacket);
        } catch (Exception e) {
            System.out.println("Failed to send item pickup animation");
            e.printStackTrace();
        }
    }
}
