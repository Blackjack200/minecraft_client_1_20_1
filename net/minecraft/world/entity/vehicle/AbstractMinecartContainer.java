package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements ContainerEntity {
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;

   protected AbstractMinecartContainer(EntityType<?> entitytype, Level level) {
      super(entitytype, level);
   }

   protected AbstractMinecartContainer(EntityType<?> entitytype, double d0, double d1, double d2, Level level) {
      super(entitytype, level, d0, d1, d2);
   }

   public void destroy(DamageSource damagesource) {
      super.destroy(damagesource);
      this.chestVehicleDestroyed(damagesource, this.level(), this);
   }

   public ItemStack getItem(int i) {
      return this.getChestVehicleItem(i);
   }

   public ItemStack removeItem(int i, int j) {
      return this.removeChestVehicleItem(i, j);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return this.removeChestVehicleItemNoUpdate(i);
   }

   public void setItem(int i, ItemStack itemstack) {
      this.setChestVehicleItem(i, itemstack);
   }

   public SlotAccess getSlot(int i) {
      return this.getChestVehicleSlot(i);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      return this.isChestVehicleStillValid(player);
   }

   public void remove(Entity.RemovalReason entity_removalreason) {
      if (!this.level().isClientSide && entity_removalreason.shouldDestroy()) {
         Containers.dropContents(this.level(), this, this);
      }

      super.remove(entity_removalreason);
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      this.addChestVehicleSaveData(compoundtag);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.readChestVehicleSaveData(compoundtag);
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      return this.interactWithContainerVehicle(player);
   }

   protected void applyNaturalSlowdown() {
      float f = 0.98F;
      if (this.lootTable == null) {
         int i = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
         f += (float)i * 0.001F;
      }

      if (this.isInWater()) {
         f *= 0.95F;
      }

      this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0D, (double)f));
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   public void setLootTable(ResourceLocation resourcelocation, long i) {
      this.lootTable = resourcelocation;
      this.lootTableSeed = i;
   }

   @Nullable
   public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      if (this.lootTable != null && player.isSpectator()) {
         return null;
      } else {
         this.unpackChestVehicleLootTable(inventory.player);
         return this.createMenu(i, inventory);
      }
   }

   protected abstract AbstractContainerMenu createMenu(int i, Inventory inventory);

   @Nullable
   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(@Nullable ResourceLocation resourcelocation) {
      this.lootTable = resourcelocation;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long i) {
      this.lootTableSeed = i;
   }

   public NonNullList<ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public void clearItemStacks() {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   }
}
