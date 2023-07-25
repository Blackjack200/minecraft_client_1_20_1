package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ThrowableItemProjectile extends ThrowableProjectile implements ItemSupplier {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrowableItemProjectile.class, EntityDataSerializers.ITEM_STACK);

   public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entitytype, Level level) {
      super(entitytype, level);
   }

   public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entitytype, double d0, double d1, double d2, Level level) {
      super(entitytype, d0, d1, d2, level);
   }

   public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> entitytype, LivingEntity livingentity, Level level) {
      super(entitytype, livingentity, level);
   }

   public void setItem(ItemStack itemstack) {
      if (!itemstack.is(this.getDefaultItem()) || itemstack.hasTag()) {
         this.getEntityData().set(DATA_ITEM_STACK, itemstack.copyWithCount(1));
      }

   }

   protected abstract Item getDefaultItem();

   protected ItemStack getItemRaw() {
      return this.getEntityData().get(DATA_ITEM_STACK);
   }

   public ItemStack getItem() {
      ItemStack itemstack = this.getItemRaw();
      return itemstack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemstack;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      ItemStack itemstack = this.getItemRaw();
      if (!itemstack.isEmpty()) {
         compoundtag.put("Item", itemstack.save(new CompoundTag()));
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      ItemStack itemstack = ItemStack.of(compoundtag.getCompound("Item"));
      this.setItem(itemstack);
   }
}
