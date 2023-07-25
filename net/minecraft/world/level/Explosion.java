package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Explosion {
   private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
   private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
   private final boolean fire;
   private final Explosion.BlockInteraction blockInteraction;
   private final RandomSource random = RandomSource.create();
   private final Level level;
   private final double x;
   private final double y;
   private final double z;
   @Nullable
   private final Entity source;
   private final float radius;
   private final DamageSource damageSource;
   private final ExplosionDamageCalculator damageCalculator;
   private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
   private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();

   public Explosion(Level level, @Nullable Entity entity, double d0, double d1, double d2, float f, List<BlockPos> list) {
      this(level, entity, d0, d1, d2, f, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY, list);
   }

   public Explosion(Level level, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.BlockInteraction explosion_blockinteraction, List<BlockPos> list) {
      this(level, entity, d0, d1, d2, f, flag, explosion_blockinteraction);
      this.toBlow.addAll(list);
   }

   public Explosion(Level level, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.BlockInteraction explosion_blockinteraction) {
      this(level, entity, (DamageSource)null, (ExplosionDamageCalculator)null, d0, d1, d2, f, flag, explosion_blockinteraction);
   }

   public Explosion(Level level, @Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Explosion.BlockInteraction explosion_blockinteraction) {
      this.level = level;
      this.source = entity;
      this.radius = f;
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.fire = flag;
      this.blockInteraction = explosion_blockinteraction;
      this.damageSource = damagesource == null ? level.damageSources().explosion(this) : damagesource;
      this.damageCalculator = explosiondamagecalculator == null ? this.makeDamageCalculator(entity) : explosiondamagecalculator;
   }

   private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
      return (ExplosionDamageCalculator)(entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity));
   }

   public static float getSeenPercent(Vec3 vec3, Entity entity) {
      AABB aabb = entity.getBoundingBox();
      double d0 = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
      double d1 = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
      double d2 = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
      double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
      double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
      if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
         int i = 0;
         int j = 0;

         for(double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
            for(double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
               for(double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                  double d8 = Mth.lerp(d5, aabb.minX, aabb.maxX);
                  double d9 = Mth.lerp(d6, aabb.minY, aabb.maxY);
                  double d10 = Mth.lerp(d7, aabb.minZ, aabb.maxZ);
                  Vec3 vec31 = new Vec3(d8 + d3, d9, d10 + d4);
                  if (entity.level().clip(new ClipContext(vec31, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (float)i / (float)j;
      } else {
         return 0.0F;
      }
   }

   public void explode() {
      this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
      Set<BlockPos> set = Sets.newHashSet();
      int i = 16;

      for(int j = 0; j < 16; ++j) {
         for(int k = 0; k < 16; ++k) {
            for(int l = 0; l < 16; ++l) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                  double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                  double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 /= d3;
                  d1 /= d3;
                  d2 /= d3;
                  float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                  double d4 = this.x;
                  double d5 = this.y;
                  double d6 = this.z;

                  for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                     BlockPos blockpos = BlockPos.containing(d4, d5, d6);
                     BlockState blockstate = this.level.getBlockState(blockpos);
                     FluidState fluidstate = this.level.getFluidState(blockpos);
                     if (!this.level.isInWorldBounds(blockpos)) {
                        break;
                     }

                     Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                     if (optional.isPresent()) {
                        f -= (optional.get() + 0.3F) * 0.3F;
                     }

                     if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                        set.add(blockpos);
                     }

                     d4 += d0 * (double)0.3F;
                     d5 += d1 * (double)0.3F;
                     d6 += d2 * (double)0.3F;
                  }
               }
            }
         }
      }

      this.toBlow.addAll(set);
      float f2 = this.radius * 2.0F;
      int i1 = Mth.floor(this.x - (double)f2 - 1.0D);
      int j1 = Mth.floor(this.x + (double)f2 + 1.0D);
      int k1 = Mth.floor(this.y - (double)f2 - 1.0D);
      int l1 = Mth.floor(this.y + (double)f2 + 1.0D);
      int i2 = Mth.floor(this.z - (double)f2 - 1.0D);
      int j2 = Mth.floor(this.z + (double)f2 + 1.0D);
      List<Entity> list = this.level.getEntities(this.source, new AABB((double)i1, (double)k1, (double)i2, (double)j1, (double)l1, (double)j2));
      Vec3 vec3 = new Vec3(this.x, this.y, this.z);

      for(int k2 = 0; k2 < list.size(); ++k2) {
         Entity entity = list.get(k2);
         if (!entity.ignoreExplosion()) {
            double d7 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
            if (d7 <= 1.0D) {
               double d8 = entity.getX() - this.x;
               double d9 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
               double d10 = entity.getZ() - this.z;
               double d11 = Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
               if (d11 != 0.0D) {
                  d8 /= d11;
                  d9 /= d11;
                  d10 /= d11;
                  double d12 = (double)getSeenPercent(vec3, entity);
                  double d13 = (1.0D - d7) * d12;
                  entity.hurt(this.getDamageSource(), (float)((int)((d13 * d13 + d13) / 2.0D * 7.0D * (double)f2 + 1.0D)));
                  double d14;
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingentity = (LivingEntity)entity;
                     d14 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d13);
                  } else {
                     d14 = d13;
                  }

                  d8 *= d14;
                  d9 *= d14;
                  d10 *= d14;
                  Vec3 vec31 = new Vec3(d8, d9, d10);
                  entity.setDeltaMovement(entity.getDeltaMovement().add(vec31));
                  if (entity instanceof Player) {
                     Player player = (Player)entity;
                     if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                        this.hitPlayers.put(player, vec31);
                     }
                  }
               }
            }
         }
      }

   }

   public void finalizeExplosion(boolean flag) {
      if (this.level.isClientSide) {
         this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
      }

      boolean flag1 = this.interactsWithBlocks();
      if (flag) {
         if (!(this.radius < 2.0F) && flag1) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         } else {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         }
      }

      if (flag1) {
         ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
         boolean flag2 = this.getIndirectSourceEntity() instanceof Player;
         Util.shuffle(this.toBlow, this.level.random);

         for(BlockPos blockpos : this.toBlow) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (!blockstate.isAir()) {
               BlockPos blockpos1 = blockpos.immutable();
               this.level.getProfiler().push("explosion_blocks");
               if (block.dropFromExplosion(this)) {
                  Level blockentity = this.level;
                  if (blockentity instanceof ServerLevel) {
                     ServerLevel serverlevel = (ServerLevel)blockentity;
                     BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                     LootParams.Builder lootparams_builder = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                     if (this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                        lootparams_builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                     }

                     blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag2);
                     blockstate.getDrops(lootparams_builder).forEach((itemstack) -> addBlockDrops(objectarraylist, itemstack, blockpos1));
                  }
               }

               this.level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
               block.wasExploded(this.level, blockpos, this);
               this.level.getProfiler().pop();
            }
         }

         for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
         }
      }

      if (this.fire) {
         for(BlockPos blockpos2 : this.toBlow) {
            if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
               this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
            }
         }
      }

   }

   public boolean interactsWithBlocks() {
      return this.blockInteraction != Explosion.BlockInteraction.KEEP;
   }

   private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist, ItemStack itemstack, BlockPos blockpos) {
      int i = objectarraylist.size();

      for(int j = 0; j < i; ++j) {
         Pair<ItemStack, BlockPos> pair = objectarraylist.get(j);
         ItemStack itemstack1 = pair.getFirst();
         if (ItemEntity.areMergable(itemstack1, itemstack)) {
            ItemStack itemstack2 = ItemEntity.merge(itemstack1, itemstack, 16);
            objectarraylist.set(j, Pair.of(itemstack2, pair.getSecond()));
            if (itemstack.isEmpty()) {
               return;
            }
         }
      }

      objectarraylist.add(Pair.of(itemstack, blockpos));
   }

   public DamageSource getDamageSource() {
      return this.damageSource;
   }

   public Map<Player, Vec3> getHitPlayers() {
      return this.hitPlayers;
   }

   @Nullable
   public LivingEntity getIndirectSourceEntity() {
      if (this.source == null) {
         return null;
      } else {
         Entity entity = this.source;
         if (entity instanceof PrimedTnt) {
            PrimedTnt primedtnt = (PrimedTnt)entity;
            return primedtnt.getOwner();
         } else {
            entity = this.source;
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity;
               return livingentity;
            } else {
               entity = this.source;
               if (entity instanceof Projectile) {
                  Projectile projectile = (Projectile)entity;
                  entity = projectile.getOwner();
                  if (entity instanceof LivingEntity) {
                     return (LivingEntity)entity;
                  }
               }

               return null;
            }
         }
      }
   }

   @Nullable
   public Entity getDirectSourceEntity() {
      return this.source;
   }

   public void clearToBlow() {
      this.toBlow.clear();
   }

   public List<BlockPos> getToBlow() {
      return this.toBlow;
   }

   public static enum BlockInteraction {
      KEEP,
      DESTROY,
      DESTROY_WITH_DECAY;
   }
}
