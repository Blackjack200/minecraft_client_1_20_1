package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LeashFenceKnotEntity extends HangingEntity {
   public static final double OFFSET_Y = 0.375D;

   public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> entitytype, Level level) {
      super(entitytype, level);
   }

   public LeashFenceKnotEntity(Level level, BlockPos blockpos) {
      super(EntityType.LEASH_KNOT, level, blockpos);
      this.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
   }

   protected void recalculateBoundingBox() {
      this.setPosRaw((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.375D, (double)this.pos.getZ() + 0.5D);
      double d0 = (double)this.getType().getWidth() / 2.0D;
      double d1 = (double)this.getType().getHeight();
      this.setBoundingBox(new AABB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0, this.getY() + d1, this.getZ() + d0));
   }

   public void setDirection(Direction direction) {
   }

   public int getWidth() {
      return 9;
   }

   public int getHeight() {
      return 9;
   }

   protected float getEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return 0.0625F;
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      return d0 < 1024.0D;
   }

   public void dropItem(@Nullable Entity entity) {
      this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      if (this.level().isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         boolean flag = false;
         double d0 = 7.0D;
         List<Mob> list = this.level().getEntitiesOfClass(Mob.class, new AABB(this.getX() - 7.0D, this.getY() - 7.0D, this.getZ() - 7.0D, this.getX() + 7.0D, this.getY() + 7.0D, this.getZ() + 7.0D));

         for(Mob mob : list) {
            if (mob.getLeashHolder() == player) {
               mob.setLeashedTo(this, true);
               flag = true;
            }
         }

         boolean flag1 = false;
         if (!flag) {
            this.discard();
            if (player.getAbilities().instabuild) {
               for(Mob mob1 : list) {
                  if (mob1.isLeashed() && mob1.getLeashHolder() == this) {
                     mob1.dropLeash(true, false);
                     flag1 = true;
                  }
               }
            }
         }

         if (flag || flag1) {
            this.gameEvent(GameEvent.BLOCK_ATTACH, player);
         }

         return InteractionResult.CONSUME;
      }
   }

   public boolean survives() {
      return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
   }

   public static LeashFenceKnotEntity getOrCreateKnot(Level level, BlockPos blockpos) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();

      for(LeashFenceKnotEntity leashfenceknotentity : level.getEntitiesOfClass(LeashFenceKnotEntity.class, new AABB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D))) {
         if (leashfenceknotentity.getPos().equals(blockpos)) {
            return leashfenceknotentity;
         }
      }

      LeashFenceKnotEntity leashfenceknotentity1 = new LeashFenceKnotEntity(level, blockpos);
      level.addFreshEntity(leashfenceknotentity1);
      return leashfenceknotentity1;
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this, 0, this.getPos());
   }

   public Vec3 getRopeHoldPosition(float f) {
      return this.getPosition(f).add(0.0D, 0.2D, 0.0D);
   }

   public ItemStack getPickResult() {
      return new ItemStack(Items.LEAD);
   }
}
