package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity implements TraceableEntity {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
   private static final int LIFETIME = 6000;
   private static final int INFINITE_PICKUP_DELAY = 32767;
   private static final int INFINITE_LIFETIME = -32768;
   private int age;
   private int pickupDelay;
   private int health = 5;
   @Nullable
   private UUID thrower;
   @Nullable
   private UUID target;
   public final float bobOffs;

   public ItemEntity(EntityType<? extends ItemEntity> entitytype, Level level) {
      super(entitytype, level);
      this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
      this.setYRot(this.random.nextFloat() * 360.0F);
   }

   public ItemEntity(Level level, double d0, double d1, double d2, ItemStack itemstack) {
      this(level, d0, d1, d2, itemstack, level.random.nextDouble() * 0.2D - 0.1D, 0.2D, level.random.nextDouble() * 0.2D - 0.1D);
   }

   public ItemEntity(Level level, double d0, double d1, double d2, ItemStack itemstack, double d3, double d4, double d5) {
      this(EntityType.ITEM, level);
      this.setPos(d0, d1, d2);
      this.setDeltaMovement(d3, d4, d5);
      this.setItem(itemstack);
   }

   private ItemEntity(ItemEntity itementity) {
      super(itementity.getType(), itementity.level());
      this.setItem(itementity.getItem().copy());
      this.copyPosition(itementity);
      this.age = itementity.age;
      this.bobOffs = itementity.bobOffs;
   }

   public boolean dampensVibrations() {
      return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
   }

   @Nullable
   public Entity getOwner() {
      if (this.thrower != null) {
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)var2;
            return serverlevel.getEntity(this.thrower);
         }
      }

      return null;
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
   }

   public void tick() {
      if (this.getItem().isEmpty()) {
         this.discard();
      } else {
         super.tick();
         if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            --this.pickupDelay;
         }

         this.xo = this.getX();
         this.yo = this.getY();
         this.zo = this.getZ();
         Vec3 vec3 = this.getDeltaMovement();
         float f = this.getEyeHeight() - 0.11111111F;
         if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.setUnderwaterMovement();
         } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.setUnderLavaMovement();
         } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
         }

         if (this.level().isClientSide) {
            this.noPhysics = false;
         } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7D));
            if (this.noPhysics) {
               this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
            }
         }

         if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > (double)1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f1 = 0.98F;
            if (this.onGround()) {
               f1 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply((double)f1, 0.98D, (double)f1));
            if (this.onGround()) {
               Vec3 vec31 = this.getDeltaMovement();
               if (vec31.y < 0.0D) {
                  this.setDeltaMovement(vec31.multiply(1.0D, -0.5D, 1.0D));
               }
            }
         }

         boolean flag = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
         int i = flag ? 2 : 40;
         if (this.tickCount % i == 0 && !this.level().isClientSide && this.isMergable()) {
            this.mergeWithNeighbours();
         }

         if (this.age != -32768) {
            ++this.age;
         }

         this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
         if (!this.level().isClientSide) {
            double d0 = this.getDeltaMovement().subtract(vec3).lengthSqr();
            if (d0 > 0.01D) {
               this.hasImpulse = true;
            }
         }

         if (!this.level().isClientSide && this.age >= 6000) {
            this.discard();
         }

      }
   }

   protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return this.getOnPos(0.999999F);
   }

   private void setUnderwaterMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * (double)0.99F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.99F);
   }

   private void setUnderLavaMovement() {
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
   }

   private void mergeWithNeighbours() {
      if (this.isMergable()) {
         for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5D, 0.0D, 0.5D), (itementity1) -> itementity1 != this && itementity1.isMergable())) {
            if (itementity.isMergable()) {
               this.tryToMerge(itementity);
               if (this.isRemoved()) {
                  break;
               }
            }
         }

      }
   }

   private boolean isMergable() {
      ItemStack itemstack = this.getItem();
      return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemstack.getCount() < itemstack.getMaxStackSize();
   }

   private void tryToMerge(ItemEntity itementity) {
      ItemStack itemstack = this.getItem();
      ItemStack itemstack1 = itementity.getItem();
      if (Objects.equals(this.target, itementity.target) && areMergable(itemstack, itemstack1)) {
         if (itemstack1.getCount() < itemstack.getCount()) {
            merge(this, itemstack, itementity, itemstack1);
         } else {
            merge(itementity, itemstack1, this, itemstack);
         }

      }
   }

   public static boolean areMergable(ItemStack itemstack, ItemStack itemstack1) {
      if (!itemstack1.is(itemstack.getItem())) {
         return false;
      } else if (itemstack1.getCount() + itemstack.getCount() > itemstack1.getMaxStackSize()) {
         return false;
      } else if (itemstack1.hasTag() ^ itemstack.hasTag()) {
         return false;
      } else {
         return !itemstack1.hasTag() || itemstack1.getTag().equals(itemstack.getTag());
      }
   }

   public static ItemStack merge(ItemStack itemstack, ItemStack itemstack1, int i) {
      int j = Math.min(Math.min(itemstack.getMaxStackSize(), i) - itemstack.getCount(), itemstack1.getCount());
      ItemStack itemstack2 = itemstack.copyWithCount(itemstack.getCount() + j);
      itemstack1.shrink(j);
      return itemstack2;
   }

   private static void merge(ItemEntity itementity, ItemStack itemstack, ItemStack itemstack1) {
      ItemStack itemstack2 = merge(itemstack, itemstack1, 64);
      itementity.setItem(itemstack2);
   }

   private static void merge(ItemEntity itementity, ItemStack itemstack, ItemEntity itementity1, ItemStack itemstack1) {
      merge(itementity, itemstack, itemstack1);
      itementity.pickupDelay = Math.max(itementity.pickupDelay, itementity1.pickupDelay);
      itementity.age = Math.min(itementity.age, itementity1.age);
      if (itemstack1.isEmpty()) {
         itementity1.discard();
      }

   }

   public boolean fireImmune() {
      return this.getItem().getItem().isFireResistant() || super.fireImmune();
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (!this.getItem().isEmpty() && this.getItem().is(Items.NETHER_STAR) && damagesource.is(DamageTypeTags.IS_EXPLOSION)) {
         return false;
      } else if (!this.getItem().getItem().canBeHurtBy(damagesource)) {
         return false;
      } else if (this.level().isClientSide) {
         return true;
      } else {
         this.markHurt();
         this.health = (int)((float)this.health - f);
         this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
         if (this.health <= 0) {
            this.getItem().onDestroyed(this);
            this.discard();
         }

         return true;
      }
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putShort("Health", (short)this.health);
      compoundtag.putShort("Age", (short)this.age);
      compoundtag.putShort("PickupDelay", (short)this.pickupDelay);
      if (this.thrower != null) {
         compoundtag.putUUID("Thrower", this.thrower);
      }

      if (this.target != null) {
         compoundtag.putUUID("Owner", this.target);
      }

      if (!this.getItem().isEmpty()) {
         compoundtag.put("Item", this.getItem().save(new CompoundTag()));
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      this.health = compoundtag.getShort("Health");
      this.age = compoundtag.getShort("Age");
      if (compoundtag.contains("PickupDelay")) {
         this.pickupDelay = compoundtag.getShort("PickupDelay");
      }

      if (compoundtag.hasUUID("Owner")) {
         this.target = compoundtag.getUUID("Owner");
      }

      if (compoundtag.hasUUID("Thrower")) {
         this.thrower = compoundtag.getUUID("Thrower");
      }

      CompoundTag compoundtag1 = compoundtag.getCompound("Item");
      this.setItem(ItemStack.of(compoundtag1));
      if (this.getItem().isEmpty()) {
         this.discard();
      }

   }

   public void playerTouch(Player player) {
      if (!this.level().isClientSide) {
         ItemStack itemstack = this.getItem();
         Item item = itemstack.getItem();
         int i = itemstack.getCount();
         if (this.pickupDelay == 0 && (this.target == null || this.target.equals(player.getUUID())) && player.getInventory().add(itemstack)) {
            player.take(this, i);
            if (itemstack.isEmpty()) {
               this.discard();
               itemstack.setCount(i);
            }

            player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            player.onItemPickup(this);
         }

      }
   }

   public Component getName() {
      Component component = this.getCustomName();
      return (Component)(component != null ? component : Component.translatable(this.getItem().getDescriptionId()));
   }

   public boolean isAttackable() {
      return false;
   }

   @Nullable
   public Entity changeDimension(ServerLevel serverlevel) {
      Entity entity = super.changeDimension(serverlevel);
      if (!this.level().isClientSide && entity instanceof ItemEntity) {
         ((ItemEntity)entity).mergeWithNeighbours();
      }

      return entity;
   }

   public ItemStack getItem() {
      return this.getEntityData().get(DATA_ITEM);
   }

   public void setItem(ItemStack itemstack) {
      this.getEntityData().set(DATA_ITEM, itemstack);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      super.onSyncedDataUpdated(entitydataaccessor);
      if (DATA_ITEM.equals(entitydataaccessor)) {
         this.getItem().setEntityRepresentation(this);
      }

   }

   public void setTarget(@Nullable UUID uuid) {
      this.target = uuid;
   }

   public void setThrower(@Nullable UUID uuid) {
      this.thrower = uuid;
   }

   public int getAge() {
      return this.age;
   }

   public void setDefaultPickUpDelay() {
      this.pickupDelay = 10;
   }

   public void setNoPickUpDelay() {
      this.pickupDelay = 0;
   }

   public void setNeverPickUp() {
      this.pickupDelay = 32767;
   }

   public void setPickUpDelay(int i) {
      this.pickupDelay = i;
   }

   public boolean hasPickUpDelay() {
      return this.pickupDelay > 0;
   }

   public void setUnlimitedLifetime() {
      this.age = -32768;
   }

   public void setExtendedLifetime() {
      this.age = -6000;
   }

   public void makeFakeItem() {
      this.setNeverPickUp();
      this.age = 5999;
   }

   public float getSpin(float f) {
      return ((float)this.getAge() + f) / 20.0F + this.bobOffs;
   }

   public ItemEntity copy() {
      return new ItemEntity(this);
   }

   public SoundSource getSoundSource() {
      return SoundSource.AMBIENT;
   }

   public float getVisualRotationYInDegrees() {
      return 180.0F - this.getSpin(0.5F) / ((float)Math.PI * 2F) * 360.0F;
   }
}
