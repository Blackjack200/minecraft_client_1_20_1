package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ItemFrame extends HangingEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
   public static final int NUM_ROTATIONS = 8;
   private float dropChance = 1.0F;
   private boolean fixed;

   public ItemFrame(EntityType<? extends ItemFrame> entitytype, Level level) {
      super(entitytype, level);
   }

   public ItemFrame(Level level, BlockPos blockpos, Direction direction) {
      this(EntityType.ITEM_FRAME, level, blockpos, direction);
   }

   public ItemFrame(EntityType<? extends ItemFrame> entitytype, Level level, BlockPos blockpos, Direction direction) {
      super(entitytype, level, blockpos);
      this.setDirection(direction);
   }

   protected float getEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return 0.0F;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
      this.getEntityData().define(DATA_ROTATION, 0);
   }

   protected void setDirection(Direction direction) {
      Validate.notNull(direction);
      this.direction = direction;
      if (direction.getAxis().isHorizontal()) {
         this.setXRot(0.0F);
         this.setYRot((float)(this.direction.get2DDataValue() * 90));
      } else {
         this.setXRot((float)(-90 * direction.getAxisDirection().getStep()));
         this.setYRot(0.0F);
      }

      this.xRotO = this.getXRot();
      this.yRotO = this.getYRot();
      this.recalculateBoundingBox();
   }

   protected void recalculateBoundingBox() {
      if (this.direction != null) {
         double d0 = 0.46875D;
         double d1 = (double)this.pos.getX() + 0.5D - (double)this.direction.getStepX() * 0.46875D;
         double d2 = (double)this.pos.getY() + 0.5D - (double)this.direction.getStepY() * 0.46875D;
         double d3 = (double)this.pos.getZ() + 0.5D - (double)this.direction.getStepZ() * 0.46875D;
         this.setPosRaw(d1, d2, d3);
         double d4 = (double)this.getWidth();
         double d5 = (double)this.getHeight();
         double d6 = (double)this.getWidth();
         Direction.Axis direction_axis = this.direction.getAxis();
         switch (direction_axis) {
            case X:
               d4 = 1.0D;
               break;
            case Y:
               d5 = 1.0D;
               break;
            case Z:
               d6 = 1.0D;
         }

         d4 /= 32.0D;
         d5 /= 32.0D;
         d6 /= 32.0D;
         this.setBoundingBox(new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
      }
   }

   public boolean survives() {
      if (this.fixed) {
         return true;
      } else if (!this.level().noCollision(this)) {
         return false;
      } else {
         BlockState blockstate = this.level().getBlockState(this.pos.relative(this.direction.getOpposite()));
         return blockstate.isSolid() || this.direction.getAxis().isHorizontal() && DiodeBlock.isDiode(blockstate) ? this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty() : false;
      }
   }

   public void move(MoverType movertype, Vec3 vec3) {
      if (!this.fixed) {
         super.move(movertype, vec3);
      }

   }

   public void push(double d0, double d1, double d2) {
      if (!this.fixed) {
         super.push(d0, d1, d2);
      }

   }

   public float getPickRadius() {
      return 0.0F;
   }

   public void kill() {
      this.removeFramedMap(this.getItem());
      super.kill();
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.fixed) {
         return !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damagesource.isCreativePlayer() ? false : super.hurt(damagesource, f);
      } else if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else if (!damagesource.is(DamageTypeTags.IS_EXPLOSION) && !this.getItem().isEmpty()) {
         if (!this.level().isClientSide) {
            this.dropItem(damagesource.getEntity(), false);
            this.gameEvent(GameEvent.BLOCK_CHANGE, damagesource.getEntity());
            this.playSound(this.getRemoveItemSound(), 1.0F, 1.0F);
         }

         return true;
      } else {
         return super.hurt(damagesource, f);
      }
   }

   public SoundEvent getRemoveItemSound() {
      return SoundEvents.ITEM_FRAME_REMOVE_ITEM;
   }

   public int getWidth() {
      return 12;
   }

   public int getHeight() {
      return 12;
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      double d1 = 16.0D;
      d1 *= 64.0D * getViewScale();
      return d0 < d1 * d1;
   }

   public void dropItem(@Nullable Entity entity) {
      this.playSound(this.getBreakSound(), 1.0F, 1.0F);
      this.dropItem(entity, true);
      this.gameEvent(GameEvent.BLOCK_CHANGE, entity);
   }

   public SoundEvent getBreakSound() {
      return SoundEvents.ITEM_FRAME_BREAK;
   }

   public void playPlacementSound() {
      this.playSound(this.getPlaceSound(), 1.0F, 1.0F);
   }

   public SoundEvent getPlaceSound() {
      return SoundEvents.ITEM_FRAME_PLACE;
   }

   private void dropItem(@Nullable Entity entity, boolean flag) {
      if (!this.fixed) {
         ItemStack itemstack = this.getItem();
         this.setItem(ItemStack.EMPTY);
         if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (entity == null) {
               this.removeFramedMap(itemstack);
            }

         } else {
            if (entity instanceof Player) {
               Player player = (Player)entity;
               if (player.getAbilities().instabuild) {
                  this.removeFramedMap(itemstack);
                  return;
               }
            }

            if (flag) {
               this.spawnAtLocation(this.getFrameItemStack());
            }

            if (!itemstack.isEmpty()) {
               itemstack = itemstack.copy();
               this.removeFramedMap(itemstack);
               if (this.random.nextFloat() < this.dropChance) {
                  this.spawnAtLocation(itemstack);
               }
            }

         }
      }
   }

   private void removeFramedMap(ItemStack itemstack) {
      this.getFramedMapId().ifPresent((i) -> {
         MapItemSavedData mapitemsaveddata = MapItem.getSavedData(i, this.level());
         if (mapitemsaveddata != null) {
            mapitemsaveddata.removedFromFrame(this.pos, this.getId());
            mapitemsaveddata.setDirty(true);
         }

      });
      itemstack.setEntityRepresentation((Entity)null);
   }

   public ItemStack getItem() {
      return this.getEntityData().get(DATA_ITEM);
   }

   public OptionalInt getFramedMapId() {
      ItemStack itemstack = this.getItem();
      if (itemstack.is(Items.FILLED_MAP)) {
         Integer integer = MapItem.getMapId(itemstack);
         if (integer != null) {
            return OptionalInt.of(integer);
         }
      }

      return OptionalInt.empty();
   }

   public boolean hasFramedMap() {
      return this.getFramedMapId().isPresent();
   }

   public void setItem(ItemStack itemstack) {
      this.setItem(itemstack, true);
   }

   public void setItem(ItemStack itemstack, boolean flag) {
      if (!itemstack.isEmpty()) {
         itemstack = itemstack.copyWithCount(1);
      }

      this.onItemChanged(itemstack);
      this.getEntityData().set(DATA_ITEM, itemstack);
      if (!itemstack.isEmpty()) {
         this.playSound(this.getAddItemSound(), 1.0F, 1.0F);
      }

      if (flag && this.pos != null) {
         this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
      }

   }

   public SoundEvent getAddItemSound() {
      return SoundEvents.ITEM_FRAME_ADD_ITEM;
   }

   public SlotAccess getSlot(int i) {
      return i == 0 ? new SlotAccess() {
         public ItemStack get() {
            return ItemFrame.this.getItem();
         }

         public boolean set(ItemStack itemstack) {
            ItemFrame.this.setItem(itemstack);
            return true;
         }
      } : super.getSlot(i);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (entitydataaccessor.equals(DATA_ITEM)) {
         this.onItemChanged(this.getItem());
      }

   }

   private void onItemChanged(ItemStack itemstack) {
      if (!itemstack.isEmpty() && itemstack.getFrame() != this) {
         itemstack.setEntityRepresentation(this);
      }

      this.recalculateBoundingBox();
   }

   public int getRotation() {
      return this.getEntityData().get(DATA_ROTATION);
   }

   public void setRotation(int i) {
      this.setRotation(i, true);
   }

   private void setRotation(int i, boolean flag) {
      this.getEntityData().set(DATA_ROTATION, i % 8);
      if (flag && this.pos != null) {
         this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      if (!this.getItem().isEmpty()) {
         compoundtag.put("Item", this.getItem().save(new CompoundTag()));
         compoundtag.putByte("ItemRotation", (byte)this.getRotation());
         compoundtag.putFloat("ItemDropChance", this.dropChance);
      }

      compoundtag.putByte("Facing", (byte)this.direction.get3DDataValue());
      compoundtag.putBoolean("Invisible", this.isInvisible());
      compoundtag.putBoolean("Fixed", this.fixed);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      CompoundTag compoundtag1 = compoundtag.getCompound("Item");
      if (compoundtag1 != null && !compoundtag1.isEmpty()) {
         ItemStack itemstack = ItemStack.of(compoundtag1);
         if (itemstack.isEmpty()) {
            LOGGER.warn("Unable to load item from: {}", (Object)compoundtag1);
         }

         ItemStack itemstack1 = this.getItem();
         if (!itemstack1.isEmpty() && !ItemStack.matches(itemstack, itemstack1)) {
            this.removeFramedMap(itemstack1);
         }

         this.setItem(itemstack, false);
         this.setRotation(compoundtag.getByte("ItemRotation"), false);
         if (compoundtag.contains("ItemDropChance", 99)) {
            this.dropChance = compoundtag.getFloat("ItemDropChance");
         }
      }

      this.setDirection(Direction.from3DDataValue(compoundtag.getByte("Facing")));
      this.setInvisible(compoundtag.getBoolean("Invisible"));
      this.fixed = compoundtag.getBoolean("Fixed");
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      boolean flag = !this.getItem().isEmpty();
      boolean flag1 = !itemstack.isEmpty();
      if (this.fixed) {
         return InteractionResult.PASS;
      } else if (!this.level().isClientSide) {
         if (!flag) {
            if (flag1 && !this.isRemoved()) {
               if (itemstack.is(Items.FILLED_MAP)) {
                  MapItemSavedData mapitemsaveddata = MapItem.getSavedData(itemstack, this.level());
                  if (mapitemsaveddata != null && mapitemsaveddata.isTrackedCountOverLimit(256)) {
                     return InteractionResult.FAIL;
                  }
               }

               this.setItem(itemstack);
               this.gameEvent(GameEvent.BLOCK_CHANGE, player);
               if (!player.getAbilities().instabuild) {
                  itemstack.shrink(1);
               }
            }
         } else {
            this.playSound(this.getRotateItemSound(), 1.0F, 1.0F);
            this.setRotation(this.getRotation() + 1);
            this.gameEvent(GameEvent.BLOCK_CHANGE, player);
         }

         return InteractionResult.CONSUME;
      } else {
         return !flag && !flag1 ? InteractionResult.PASS : InteractionResult.SUCCESS;
      }
   }

   public SoundEvent getRotateItemSound() {
      return SoundEvents.ITEM_FRAME_ROTATE_ITEM;
   }

   public int getAnalogOutput() {
      return this.getItem().isEmpty() ? 0 : this.getRotation() % 8 + 1;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      this.setDirection(Direction.from3DDataValue(clientboundaddentitypacket.getData()));
   }

   public ItemStack getPickResult() {
      ItemStack itemstack = this.getItem();
      return itemstack.isEmpty() ? this.getFrameItemStack() : itemstack.copy();
   }

   protected ItemStack getFrameItemStack() {
      return new ItemStack(Items.ITEM_FRAME);
   }

   public float getVisualRotationYInDegrees() {
      Direction direction = this.getDirection();
      int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getStep() : 0;
      return (float)Mth.wrapDegrees(180 + direction.get2DDataValue() * 90 + this.getRotation() * 45 + i);
   }
}
