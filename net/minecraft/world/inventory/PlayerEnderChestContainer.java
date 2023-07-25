package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

public class PlayerEnderChestContainer extends SimpleContainer {
   @Nullable
   private EnderChestBlockEntity activeChest;

   public PlayerEnderChestContainer() {
      super(27);
   }

   public void setActiveChest(EnderChestBlockEntity enderchestblockentity) {
      this.activeChest = enderchestblockentity;
   }

   public boolean isActiveChest(EnderChestBlockEntity enderchestblockentity) {
      return this.activeChest == enderchestblockentity;
   }

   public void fromTag(ListTag listtag) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         this.setItem(i, ItemStack.EMPTY);
      }

      for(int j = 0; j < listtag.size(); ++j) {
         CompoundTag compoundtag = listtag.getCompound(j);
         int k = compoundtag.getByte("Slot") & 255;
         if (k >= 0 && k < this.getContainerSize()) {
            this.setItem(k, ItemStack.of(compoundtag));
         }
      }

   }

   public ListTag createTag() {
      ListTag listtag = new ListTag();

      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (!itemstack.isEmpty()) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putByte("Slot", (byte)i);
            itemstack.save(compoundtag);
            listtag.add(compoundtag);
         }
      }

      return listtag;
   }

   public boolean stillValid(Player player) {
      return this.activeChest != null && !this.activeChest.stillValid(player) ? false : super.stillValid(player);
   }

   public void startOpen(Player player) {
      if (this.activeChest != null) {
         this.activeChest.startOpen(player);
      }

      super.startOpen(player);
   }

   public void stopOpen(Player player) {
      if (this.activeChest != null) {
         this.activeChest.stopOpen(player);
      }

      super.stopOpen(player);
      this.activeChest = null;
   }
}
