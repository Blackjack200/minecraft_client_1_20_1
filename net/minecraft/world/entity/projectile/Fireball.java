package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public abstract class Fireball extends AbstractHurtingProjectile implements ItemSupplier {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Fireball.class, EntityDataSerializers.ITEM_STACK);

   public Fireball(EntityType<? extends Fireball> entitytype, Level level) {
      super(entitytype, level);
   }

   public Fireball(EntityType<? extends Fireball> entitytype, double d0, double d1, double d2, double d3, double d4, double d5, Level level) {
      super(entitytype, d0, d1, d2, d3, d4, d5, level);
   }

   public Fireball(EntityType<? extends Fireball> entitytype, LivingEntity livingentity, double d0, double d1, double d2, Level level) {
      super(entitytype, livingentity, d0, d1, d2, level);
   }

   public void setItem(ItemStack itemstack) {
      if (!itemstack.is(Items.FIRE_CHARGE) || itemstack.hasTag()) {
         this.getEntityData().set(DATA_ITEM_STACK, itemstack.copyWithCount(1));
      }

   }

   protected ItemStack getItemRaw() {
      return this.getEntityData().get(DATA_ITEM_STACK);
   }

   public ItemStack getItem() {
      ItemStack itemstack = this.getItemRaw();
      return itemstack.isEmpty() ? new ItemStack(Items.FIRE_CHARGE) : itemstack;
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
