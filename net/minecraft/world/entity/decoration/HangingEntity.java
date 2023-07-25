package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public abstract class HangingEntity extends Entity {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final Predicate<Entity> HANGING_ENTITY = (entity) -> entity instanceof HangingEntity;
   private int checkInterval;
   protected BlockPos pos;
   protected Direction direction = Direction.SOUTH;

   protected HangingEntity(EntityType<? extends HangingEntity> entitytype, Level level) {
      super(entitytype, level);
   }

   protected HangingEntity(EntityType<? extends HangingEntity> entitytype, Level level, BlockPos blockpos) {
      this(entitytype, level);
      this.pos = blockpos;
   }

   protected void defineSynchedData() {
   }

   protected void setDirection(Direction direction) {
      Validate.notNull(direction);
      Validate.isTrue(direction.getAxis().isHorizontal());
      this.direction = direction;
      this.setYRot((float)(this.direction.get2DDataValue() * 90));
      this.yRotO = this.getYRot();
      this.recalculateBoundingBox();
   }

   protected void recalculateBoundingBox() {
      if (this.direction != null) {
         double d0 = (double)this.pos.getX() + 0.5D;
         double d1 = (double)this.pos.getY() + 0.5D;
         double d2 = (double)this.pos.getZ() + 0.5D;
         double d3 = 0.46875D;
         double d4 = this.offs(this.getWidth());
         double d5 = this.offs(this.getHeight());
         d0 -= (double)this.direction.getStepX() * 0.46875D;
         d2 -= (double)this.direction.getStepZ() * 0.46875D;
         d1 += d5;
         Direction direction = this.direction.getCounterClockWise();
         d0 += d4 * (double)direction.getStepX();
         d2 += d4 * (double)direction.getStepZ();
         this.setPosRaw(d0, d1, d2);
         double d6 = (double)this.getWidth();
         double d7 = (double)this.getHeight();
         double d8 = (double)this.getWidth();
         if (this.direction.getAxis() == Direction.Axis.Z) {
            d8 = 1.0D;
         } else {
            d6 = 1.0D;
         }

         d6 /= 32.0D;
         d7 /= 32.0D;
         d8 /= 32.0D;
         this.setBoundingBox(new AABB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8));
      }
   }

   private double offs(int i) {
      return i % 32 == 0 ? 0.5D : 0.0D;
   }

   public void tick() {
      if (!this.level().isClientSide) {
         this.checkBelowWorld();
         if (this.checkInterval++ == 100) {
            this.checkInterval = 0;
            if (!this.isRemoved() && !this.survives()) {
               this.discard();
               this.dropItem((Entity)null);
            }
         }
      }

   }

   public boolean survives() {
      if (!this.level().noCollision(this)) {
         return false;
      } else {
         int i = Math.max(1, this.getWidth() / 16);
         int j = Math.max(1, this.getHeight() / 16);
         BlockPos blockpos = this.pos.relative(this.direction.getOpposite());
         Direction direction = this.direction.getCounterClockWise();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int k = 0; k < i; ++k) {
            for(int l = 0; l < j; ++l) {
               int i1 = (i - 1) / -2;
               int j1 = (j - 1) / -2;
               blockpos_mutableblockpos.set(blockpos).move(direction, k + i1).move(Direction.UP, l + j1);
               BlockState blockstate = this.level().getBlockState(blockpos_mutableblockpos);
               if (!blockstate.isSolid() && !DiodeBlock.isDiode(blockstate)) {
                  return false;
               }
            }
         }

         return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
      }
   }

   public boolean isPickable() {
      return true;
   }

   public boolean skipAttackInteraction(Entity entity) {
      if (entity instanceof Player player) {
         return !this.level().mayInteract(player, this.pos) ? true : this.hurt(this.damageSources().playerAttack(player), 0.0F);
      } else {
         return false;
      }
   }

   public Direction getDirection() {
      return this.direction;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else {
         if (!this.isRemoved() && !this.level().isClientSide) {
            this.kill();
            this.markHurt();
            this.dropItem(damagesource.getEntity());
         }

         return true;
      }
   }

   public void move(MoverType movertype, Vec3 vec3) {
      if (!this.level().isClientSide && !this.isRemoved() && vec3.lengthSqr() > 0.0D) {
         this.kill();
         this.dropItem((Entity)null);
      }

   }

   public void push(double d0, double d1, double d2) {
      if (!this.level().isClientSide && !this.isRemoved() && d0 * d0 + d1 * d1 + d2 * d2 > 0.0D) {
         this.kill();
         this.dropItem((Entity)null);
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      BlockPos blockpos = this.getPos();
      compoundtag.putInt("TileX", blockpos.getX());
      compoundtag.putInt("TileY", blockpos.getY());
      compoundtag.putInt("TileZ", blockpos.getZ());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      BlockPos blockpos = new BlockPos(compoundtag.getInt("TileX"), compoundtag.getInt("TileY"), compoundtag.getInt("TileZ"));
      if (!blockpos.closerThan(this.blockPosition(), 16.0D)) {
         LOGGER.error("Hanging entity at invalid position: {}", (Object)blockpos);
      } else {
         this.pos = blockpos;
      }
   }

   public abstract int getWidth();

   public abstract int getHeight();

   public abstract void dropItem(@Nullable Entity entity);

   public abstract void playPlacementSound();

   public ItemEntity spawnAtLocation(ItemStack itemstack, float f) {
      ItemEntity itementity = new ItemEntity(this.level(), this.getX() + (double)((float)this.direction.getStepX() * 0.15F), this.getY() + (double)f, this.getZ() + (double)((float)this.direction.getStepZ() * 0.15F), itemstack);
      itementity.setDefaultPickUpDelay();
      this.level().addFreshEntity(itementity);
      return itementity;
   }

   protected boolean repositionEntityAfterLoad() {
      return false;
   }

   public void setPos(double d0, double d1, double d2) {
      this.pos = BlockPos.containing(d0, d1, d2);
      this.recalculateBoundingBox();
      this.hasImpulse = true;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public float rotate(Rotation rotation) {
      if (this.direction.getAxis() != Direction.Axis.Y) {
         switch (rotation) {
            case CLOCKWISE_180:
               this.direction = this.direction.getOpposite();
               break;
            case COUNTERCLOCKWISE_90:
               this.direction = this.direction.getCounterClockWise();
               break;
            case CLOCKWISE_90:
               this.direction = this.direction.getClockWise();
         }
      }

      float f = Mth.wrapDegrees(this.getYRot());
      switch (rotation) {
         case CLOCKWISE_180:
            return f + 180.0F;
         case COUNTERCLOCKWISE_90:
            return f + 90.0F;
         case CLOCKWISE_90:
            return f + 270.0F;
         default:
            return f;
      }
   }

   public float mirror(Mirror mirror) {
      return this.rotate(mirror.getRotation(this.direction));
   }

   public void thunderHit(ServerLevel serverlevel, LightningBolt lightningbolt) {
   }

   public void refreshDimensions() {
   }
}
