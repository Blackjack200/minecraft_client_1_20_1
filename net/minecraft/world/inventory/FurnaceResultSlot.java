package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot extends Slot {
   private final Player player;
   private int removeCount;

   public FurnaceResultSlot(Player player, Container container, int i, int j, int k) {
      super(container, i, j, k);
      this.player = player;
   }

   public boolean mayPlace(ItemStack itemstack) {
      return false;
   }

   public ItemStack remove(int i) {
      if (this.hasItem()) {
         this.removeCount += Math.min(i, this.getItem().getCount());
      }

      return super.remove(i);
   }

   public void onTake(Player player, ItemStack itemstack) {
      this.checkTakeAchievements(itemstack);
      super.onTake(player, itemstack);
   }

   protected void onQuickCraft(ItemStack itemstack, int i) {
      this.removeCount += i;
      this.checkTakeAchievements(itemstack);
   }

   protected void checkTakeAchievements(ItemStack itemstack) {
      itemstack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      Player var4 = this.player;
      if (var4 instanceof ServerPlayer serverplayer) {
         Container var5 = this.container;
         if (var5 instanceof AbstractFurnaceBlockEntity abstractfurnaceblockentity) {
            abstractfurnaceblockentity.awardUsedRecipesAndPopExperience(serverplayer);
         }
      }

      this.removeCount = 0;
   }
}
