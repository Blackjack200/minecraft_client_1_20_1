package net.minecraft.world.entity.vehicle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
   private boolean enabled = true;

   public MinecartHopper(EntityType<? extends MinecartHopper> entitytype, Level level) {
      super(entitytype, level);
   }

   public MinecartHopper(Level level, double d0, double d1, double d2) {
      super(EntityType.HOPPER_MINECART, d0, d1, d2, level);
   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.HOPPER;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.HOPPER.defaultBlockState();
   }

   public int getDefaultDisplayOffset() {
      return 1;
   }

   public int getContainerSize() {
      return 5;
   }

   public void activateMinecart(int i, int j, int k, boolean flag) {
      boolean flag1 = !flag;
      if (flag1 != this.isEnabled()) {
         this.setEnabled(flag1);
      }

   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean flag) {
      this.enabled = flag;
   }

   public double getLevelX() {
      return this.getX();
   }

   public double getLevelY() {
      return this.getY() + 0.5D;
   }

   public double getLevelZ() {
      return this.getZ();
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide && this.isAlive() && this.isEnabled() && this.suckInItems()) {
         this.setChanged();
      }

   }

   public boolean suckInItems() {
      if (HopperBlockEntity.suckInItems(this.level(), this)) {
         return true;
      } else {
         for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25D, 0.0D, 0.25D), EntitySelector.ENTITY_STILL_ALIVE)) {
            if (HopperBlockEntity.addItem(this, itementity)) {
               return true;
            }
         }

         return false;
      }
   }

   protected Item getDropItem() {
      return Items.HOPPER_MINECART;
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putBoolean("Enabled", this.enabled);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.enabled = compoundtag.contains("Enabled") ? compoundtag.getBoolean("Enabled") : true;
   }

   public AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return new HopperMenu(i, inventory, this);
   }
}
