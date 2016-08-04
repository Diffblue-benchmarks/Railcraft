/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.network;

import mods.railcraft.common.core.Railcraft;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.IOException;

//TODO: dual welding breaks this
public class PacketCurrentItemNBT extends RailcraftPacket {

    private final EntityPlayer player;
    @Nullable
    private final ItemStack currentItem;

    public PacketCurrentItemNBT(EntityPlayer player, @Nullable ItemStack stack) {
        this.player = player;
        this.currentItem = stack;
    }

    @Override
    public void writeData(RailcraftOutputStream data) throws IOException {
        data.writeItemStack(currentItem);
    }

    @Override
    public void readData(RailcraftInputStream data) throws IOException {
        try {
            ItemStack stack = data.readItemStack();

            if (stack == null || currentItem == null)
                return;

            if (stack.getItem() != currentItem.getItem())
                return;

            if (!(currentItem.getItem() instanceof IEditableItem))
                return;

            IEditableItem eItem = (IEditableItem) stack.getItem();

            if (!eItem.canPlayerEdit(player, currentItem)) {
                Game.log(Level.WARN, "{0} attempted to edit an item he is not allowed to edit {0}.", Railcraft.proxy.getPlayerUsername(player), currentItem.getItem().getUnlocalizedName());
                return;
            }

            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null || !eItem.validateNBT(nbt)) {
                Game.log(Level.WARN, "Item NBT not valid!");
                return;
            }

            currentItem.setTagCompound(stack.getTagCompound());
        } catch (Exception exception) {
            Game.logThrowable("Error reading Item NBT packet", exception);
        }
    }

    public void sendPacket() {
        PacketDispatcher.sendToServer(this);
    }

    @Override
    public int getID() {
        return PacketType.ITEM_NBT.ordinal();
    }

}
