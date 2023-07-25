package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConduitBlockEntity extends BlockEntity {
   private static final int BLOCK_REFRESH_RATE = 2;
   private static final int EFFECT_DURATION = 13;
   private static final float ROTATION_SPEED = -0.0375F;
   private static final int MIN_ACTIVE_SIZE = 16;
   private static final int MIN_KILL_SIZE = 42;
   private static final int KILL_RANGE = 8;
   private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
   public int tickCount;
   private float activeRotation;
   private boolean isActive;
   private boolean isHunting;
   private final List<BlockPos> effectBlocks = Lists.newArrayList();
   @Nullable
   private LivingEntity destroyTarget;
   @Nullable
   private UUID destroyTargetUUID;
   private long nextAmbientSoundActivation;

   public ConduitBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.CONDUIT, blockpos, blockstate);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.hasUUID("Target")) {
         this.destroyTargetUUID = compoundtag.getUUID("Target");
      } else {
         this.destroyTargetUUID = null;
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (this.destroyTarget != null) {
         compoundtag.putUUID("Target", this.destroyTarget.getUUID());
      }

   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public static void clientTick(Level level, BlockPos blockpos, BlockState blockstate, ConduitBlockEntity conduitblockentity) {
      ++conduitblockentity.tickCount;
      long i = level.getGameTime();
      List<BlockPos> list = conduitblockentity.effectBlocks;
      if (i % 40L == 0L) {
         conduitblockentity.isActive = updateShape(level, blockpos, list);
         updateHunting(conduitblockentity, list);
      }

      updateClientTarget(level, blockpos, conduitblockentity);
      animationTick(level, blockpos, list, conduitblockentity.destroyTarget, conduitblockentity.tickCount);
      if (conduitblockentity.isActive()) {
         ++conduitblockentity.activeRotation;
      }

   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, ConduitBlockEntity conduitblockentity) {
      ++conduitblockentity.tickCount;
      long i = level.getGameTime();
      List<BlockPos> list = conduitblockentity.effectBlocks;
      if (i % 40L == 0L) {
         boolean flag = updateShape(level, blockpos, list);
         if (flag != conduitblockentity.isActive) {
            SoundEvent soundevent = flag ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE;
            level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         conduitblockentity.isActive = flag;
         updateHunting(conduitblockentity, list);
         if (flag) {
            applyEffects(level, blockpos, list);
            updateDestroyTarget(level, blockpos, blockstate, list, conduitblockentity);
         }
      }

      if (conduitblockentity.isActive()) {
         if (i % 80L == 0L) {
            level.playSound((Player)null, blockpos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         if (i > conduitblockentity.nextAmbientSoundActivation) {
            conduitblockentity.nextAmbientSoundActivation = i + 60L + (long)level.getRandom().nextInt(40);
            level.playSound((Player)null, blockpos, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   private static void updateHunting(ConduitBlockEntity conduitblockentity, List<BlockPos> list) {
      conduitblockentity.setHunting(list.size() >= 42);
   }

   private static boolean updateShape(Level level, BlockPos blockpos, List<BlockPos> list) {
      list.clear();

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            for(int k = -1; k <= 1; ++k) {
               BlockPos blockpos1 = blockpos.offset(i, j, k);
               if (!level.isWaterAt(blockpos1)) {
                  return false;
               }
            }
         }
      }

      for(int l = -2; l <= 2; ++l) {
         for(int i1 = -2; i1 <= 2; ++i1) {
            for(int j1 = -2; j1 <= 2; ++j1) {
               int k1 = Math.abs(l);
               int l1 = Math.abs(i1);
               int i2 = Math.abs(j1);
               if ((k1 > 1 || l1 > 1 || i2 > 1) && (l == 0 && (l1 == 2 || i2 == 2) || i1 == 0 && (k1 == 2 || i2 == 2) || j1 == 0 && (k1 == 2 || l1 == 2))) {
                  BlockPos blockpos2 = blockpos.offset(l, i1, j1);
                  BlockState blockstate = level.getBlockState(blockpos2);

                  for(Block block : VALID_BLOCKS) {
                     if (blockstate.is(block)) {
                        list.add(blockpos2);
                     }
                  }
               }
            }
         }
      }

      return list.size() >= 16;
   }

   private static void applyEffects(Level level, BlockPos blockpos, List<BlockPos> list) {
      int i = list.size();
      int j = i / 7 * 16;
      int k = blockpos.getX();
      int l = blockpos.getY();
      int i1 = blockpos.getZ();
      AABB aabb = (new AABB((double)k, (double)l, (double)i1, (double)(k + 1), (double)(l + 1), (double)(i1 + 1))).inflate((double)j).expandTowards(0.0D, (double)level.getHeight(), 0.0D);
      List<Player> list1 = level.getEntitiesOfClass(Player.class, aabb);
      if (!list1.isEmpty()) {
         for(Player player : list1) {
            if (blockpos.closerThan(player.blockPosition(), (double)j) && player.isInWaterOrRain()) {
               player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
            }
         }

      }
   }

   private static void updateDestroyTarget(Level level, BlockPos blockpos, BlockState blockstate, List<BlockPos> list, ConduitBlockEntity conduitblockentity) {
      LivingEntity livingentity = conduitblockentity.destroyTarget;
      int i = list.size();
      if (i < 42) {
         conduitblockentity.destroyTarget = null;
      } else if (conduitblockentity.destroyTarget == null && conduitblockentity.destroyTargetUUID != null) {
         conduitblockentity.destroyTarget = findDestroyTarget(level, blockpos, conduitblockentity.destroyTargetUUID);
         conduitblockentity.destroyTargetUUID = null;
      } else if (conduitblockentity.destroyTarget == null) {
         List<LivingEntity> list1 = level.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB(blockpos), (livingentity1) -> livingentity1 instanceof Enemy && livingentity1.isInWaterOrRain());
         if (!list1.isEmpty()) {
            conduitblockentity.destroyTarget = list1.get(level.random.nextInt(list1.size()));
         }
      } else if (!conduitblockentity.destroyTarget.isAlive() || !blockpos.closerThan(conduitblockentity.destroyTarget.blockPosition(), 8.0D)) {
         conduitblockentity.destroyTarget = null;
      }

      if (conduitblockentity.destroyTarget != null) {
         level.playSound((Player)null, conduitblockentity.destroyTarget.getX(), conduitblockentity.destroyTarget.getY(), conduitblockentity.destroyTarget.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0F, 1.0F);
         conduitblockentity.destroyTarget.hurt(level.damageSources().magic(), 4.0F);
      }

      if (livingentity != conduitblockentity.destroyTarget) {
         level.sendBlockUpdated(blockpos, blockstate, blockstate, 2);
      }

   }

   private static void updateClientTarget(Level level, BlockPos blockpos, ConduitBlockEntity conduitblockentity) {
      if (conduitblockentity.destroyTargetUUID == null) {
         conduitblockentity.destroyTarget = null;
      } else if (conduitblockentity.destroyTarget == null || !conduitblockentity.destroyTarget.getUUID().equals(conduitblockentity.destroyTargetUUID)) {
         conduitblockentity.destroyTarget = findDestroyTarget(level, blockpos, conduitblockentity.destroyTargetUUID);
         if (conduitblockentity.destroyTarget == null) {
            conduitblockentity.destroyTargetUUID = null;
         }
      }

   }

   private static AABB getDestroyRangeAABB(BlockPos blockpos) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      return (new AABB((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1))).inflate(8.0D);
   }

   @Nullable
   private static LivingEntity findDestroyTarget(Level level, BlockPos blockpos, UUID uuid) {
      List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB(blockpos), (livingentity) -> livingentity.getUUID().equals(uuid));
      return list.size() == 1 ? list.get(0) : null;
   }

   private static void animationTick(Level level, BlockPos blockpos, List<BlockPos> list, @Nullable Entity entity, int i) {
      RandomSource randomsource = level.random;
      double d0 = (double)(Mth.sin((float)(i + 35) * 0.1F) / 2.0F + 0.5F);
      d0 = (d0 * d0 + d0) * (double)0.3F;
      Vec3 vec3 = new Vec3((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 1.5D + d0, (double)blockpos.getZ() + 0.5D);

      for(BlockPos blockpos1 : list) {
         if (randomsource.nextInt(50) == 0) {
            BlockPos blockpos2 = blockpos1.subtract(blockpos);
            float f = -0.5F + randomsource.nextFloat() + (float)blockpos2.getX();
            float f1 = -2.0F + randomsource.nextFloat() + (float)blockpos2.getY();
            float f2 = -0.5F + randomsource.nextFloat() + (float)blockpos2.getZ();
            level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, (double)f, (double)f1, (double)f2);
         }
      }

      if (entity != null) {
         Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
         float f3 = (-0.5F + randomsource.nextFloat()) * (3.0F + entity.getBbWidth());
         float f4 = -1.0F + randomsource.nextFloat() * entity.getBbHeight();
         float f5 = (-0.5F + randomsource.nextFloat()) * (3.0F + entity.getBbWidth());
         Vec3 vec32 = new Vec3((double)f3, (double)f4, (double)f5);
         level.addParticle(ParticleTypes.NAUTILUS, vec31.x, vec31.y, vec31.z, vec32.x, vec32.y, vec32.z);
      }

   }

   public boolean isActive() {
      return this.isActive;
   }

   public boolean isHunting() {
      return this.isHunting;
   }

   private void setHunting(boolean flag) {
      this.isHunting = flag;
   }

   public float getActiveRotation(float f) {
      return (this.activeRotation + f) * -0.0375F;
   }
}
