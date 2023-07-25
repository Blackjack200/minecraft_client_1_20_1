package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class EnderDragon extends Mob implements Enemy {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
   private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = TargetingConditions.forCombat().range(64.0D);
   private static final int GROWL_INTERVAL_MIN = 200;
   private static final int GROWL_INTERVAL_MAX = 400;
   private static final float SITTING_ALLOWED_DAMAGE_PERCENTAGE = 0.25F;
   private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
   private static final String DRAGON_PHASE_KEY = "DragonPhase";
   public final double[][] positions = new double[64][3];
   public int posPointer = -1;
   private final EnderDragonPart[] subEntities;
   public final EnderDragonPart head;
   private final EnderDragonPart neck;
   private final EnderDragonPart body;
   private final EnderDragonPart tail1;
   private final EnderDragonPart tail2;
   private final EnderDragonPart tail3;
   private final EnderDragonPart wing1;
   private final EnderDragonPart wing2;
   public float oFlapTime;
   public float flapTime;
   public boolean inWall;
   public int dragonDeathTime;
   public float yRotA;
   @Nullable
   public EndCrystal nearestCrystal;
   @Nullable
   private EndDragonFight dragonFight;
   private BlockPos fightOrigin = BlockPos.ZERO;
   private final EnderDragonPhaseManager phaseManager;
   private int growlTime = 100;
   private float sittingDamageReceived;
   private final Node[] nodes = new Node[24];
   private final int[] nodeAdjacency = new int[24];
   private final BinaryHeap openSet = new BinaryHeap();

   public EnderDragon(EntityType<? extends EnderDragon> entitytype, Level level) {
      super(EntityType.ENDER_DRAGON, level);
      this.head = new EnderDragonPart(this, "head", 1.0F, 1.0F);
      this.neck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
      this.body = new EnderDragonPart(this, "body", 5.0F, 3.0F);
      this.tail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.tail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.tail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.wing1 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
      this.wing2 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
      this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
      this.setHealth(this.getMaxHealth());
      this.noPhysics = true;
      this.noCulling = true;
      this.phaseManager = new EnderDragonPhaseManager(this);
   }

   public void setDragonFight(EndDragonFight enddragonfight) {
      this.dragonFight = enddragonfight;
   }

   public void setFightOrigin(BlockPos blockpos) {
      this.fightOrigin = blockpos;
   }

   public BlockPos getFightOrigin() {
      return this.fightOrigin;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0D);
   }

   public boolean isFlapping() {
      float f = Mth.cos(this.flapTime * ((float)Math.PI * 2F));
      float f1 = Mth.cos(this.oFlapTime * ((float)Math.PI * 2F));
      return f1 <= -0.3F && f >= -0.3F;
   }

   public void onFlap() {
      if (this.level().isClientSide && !this.isSilent()) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
   }

   public double[] getLatencyPos(int i, float f) {
      if (this.isDeadOrDying()) {
         f = 0.0F;
      }

      f = 1.0F - f;
      int j = this.posPointer - i & 63;
      int k = this.posPointer - i - 1 & 63;
      double[] adouble = new double[3];
      double d0 = this.positions[j][0];
      double d1 = Mth.wrapDegrees(this.positions[k][0] - d0);
      adouble[0] = d0 + d1 * (double)f;
      d0 = this.positions[j][1];
      d1 = this.positions[k][1] - d0;
      adouble[1] = d0 + d1 * (double)f;
      adouble[2] = Mth.lerp((double)f, this.positions[j][2], this.positions[k][2]);
      return adouble;
   }

   public void aiStep() {
      this.processFlappingMovement();
      if (this.level().isClientSide) {
         this.setHealth(this.getHealth());
         if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
            this.growlTime = 200 + this.random.nextInt(200);
         }
      }

      if (this.dragonFight == null) {
         Level f3 = this.level();
         if (f3 instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)f3;
            EndDragonFight enddragonfight = serverlevel.getDragonFight();
            if (enddragonfight != null && this.getUUID().equals(enddragonfight.getDragonUUID())) {
               this.dragonFight = enddragonfight;
            }
         }
      }

      this.oFlapTime = this.flapTime;
      if (this.isDeadOrDying()) {
         float f = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f, this.getY() + 2.0D + (double)f1, this.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
      } else {
         this.checkCrystals();
         Vec3 vec3 = this.getDeltaMovement();
         float f3 = 0.2F / ((float)vec3.horizontalDistance() * 10.0F + 1.0F);
         f3 *= (float)Math.pow(2.0D, vec3.y);
         if (this.phaseManager.getCurrentPhase().isSitting()) {
            this.flapTime += 0.1F;
         } else if (this.inWall) {
            this.flapTime += f3 * 0.5F;
         } else {
            this.flapTime += f3;
         }

         this.setYRot(Mth.wrapDegrees(this.getYRot()));
         if (this.isNoAi()) {
            this.flapTime = 0.5F;
         } else {
            if (this.posPointer < 0) {
               for(int i = 0; i < this.positions.length; ++i) {
                  this.positions[i][0] = (double)this.getYRot();
                  this.positions[i][1] = this.getY();
               }
            }

            if (++this.posPointer == this.positions.length) {
               this.posPointer = 0;
            }

            this.positions[this.posPointer][0] = (double)this.getYRot();
            this.positions[this.posPointer][1] = this.getY();
            if (this.level().isClientSide) {
               if (this.lerpSteps > 0) {
                  double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
                  double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
                  double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
                  double d3 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
                  this.setYRot(this.getYRot() + (float)d3 / (float)this.lerpSteps);
                  this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
                  --this.lerpSteps;
                  this.setPos(d0, d1, d2);
                  this.setRot(this.getYRot(), this.getXRot());
               }

               this.phaseManager.getCurrentPhase().doClientTick();
            } else {
               DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
               dragonphaseinstance.doServerTick();
               if (this.phaseManager.getCurrentPhase() != dragonphaseinstance) {
                  dragonphaseinstance = this.phaseManager.getCurrentPhase();
                  dragonphaseinstance.doServerTick();
               }

               Vec3 vec31 = dragonphaseinstance.getFlyTargetLocation();
               if (vec31 != null) {
                  double d4 = vec31.x - this.getX();
                  double d5 = vec31.y - this.getY();
                  double d6 = vec31.z - this.getZ();
                  double d7 = d4 * d4 + d5 * d5 + d6 * d6;
                  float f4 = dragonphaseinstance.getFlySpeed();
                  double d8 = Math.sqrt(d4 * d4 + d6 * d6);
                  if (d8 > 0.0D) {
                     d5 = Mth.clamp(d5 / d8, (double)(-f4), (double)f4);
                  }

                  this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d5 * 0.01D, 0.0D));
                  this.setYRot(Mth.wrapDegrees(this.getYRot()));
                  Vec3 vec32 = vec31.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                  Vec3 vec33 = (new Vec3((double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), this.getDeltaMovement().y, (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))))).normalize();
                  float f5 = Math.max(((float)vec33.dot(vec32) + 0.5F) / 1.5F, 0.0F);
                  if (Math.abs(d4) > (double)1.0E-5F || Math.abs(d6) > (double)1.0E-5F) {
                     float f6 = Mth.clamp(Mth.wrapDegrees(180.0F - (float)Mth.atan2(d4, d6) * (180F / (float)Math.PI) - this.getYRot()), -50.0F, 50.0F);
                     this.yRotA *= 0.8F;
                     this.yRotA += f6 * dragonphaseinstance.getTurnSpeed();
                     this.setYRot(this.getYRot() + this.yRotA * 0.1F);
                  }

                  float f7 = (float)(2.0D / (d7 + 1.0D));
                  float f8 = 0.06F;
                  this.moveRelative(0.06F * (f5 * f7 + (1.0F - f7)), new Vec3(0.0D, 0.0D, -1.0D));
                  if (this.inWall) {
                     this.move(MoverType.SELF, this.getDeltaMovement().scale((double)0.8F));
                  } else {
                     this.move(MoverType.SELF, this.getDeltaMovement());
                  }

                  Vec3 vec34 = this.getDeltaMovement().normalize();
                  double d9 = 0.8D + 0.15D * (vec34.dot(vec33) + 1.0D) / 2.0D;
                  this.setDeltaMovement(this.getDeltaMovement().multiply(d9, (double)0.91F, d9));
               }
            }

            this.yBodyRot = this.getYRot();
            Vec3[] avec3 = new Vec3[this.subEntities.length];

            for(int j = 0; j < this.subEntities.length; ++j) {
               avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
            }

            float f9 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
            float f10 = Mth.cos(f9);
            float f11 = Mth.sin(f9);
            float f12 = this.getYRot() * ((float)Math.PI / 180F);
            float f13 = Mth.sin(f12);
            float f14 = Mth.cos(f12);
            this.tickPart(this.body, (double)(f13 * 0.5F), 0.0D, (double)(-f14 * 0.5F));
            this.tickPart(this.wing1, (double)(f14 * 4.5F), 2.0D, (double)(f13 * 4.5F));
            this.tickPart(this.wing2, (double)(f14 * -4.5F), 2.0D, (double)(f13 * -4.5F));
            if (!this.level().isClientSide && this.hurtTime == 0) {
               this.knockBack(this.level().getEntities(this, this.wing1.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.knockBack(this.level().getEntities(this, this.wing2.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level().getEntities(this, this.head.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level().getEntities(this, this.neck.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            }

            float f15 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f16 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f17 = this.getHeadYOffset();
            this.tickPart(this.head, (double)(f15 * 6.5F * f10), (double)(f17 + f11 * 6.5F), (double)(-f16 * 6.5F * f10));
            this.tickPart(this.neck, (double)(f15 * 5.5F * f10), (double)(f17 + f11 * 5.5F), (double)(-f16 * 5.5F * f10));
            double[] adouble = this.getLatencyPos(5, 1.0F);

            for(int k = 0; k < 3; ++k) {
               EnderDragonPart enderdragonpart = null;
               if (k == 0) {
                  enderdragonpart = this.tail1;
               }

               if (k == 1) {
                  enderdragonpart = this.tail2;
               }

               if (k == 2) {
                  enderdragonpart = this.tail3;
               }

               double[] adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
               float f18 = this.getYRot() * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
               float f19 = Mth.sin(f18);
               float f20 = Mth.cos(f18);
               float f21 = 1.5F;
               float f22 = (float)(k + 1) * 2.0F;
               this.tickPart(enderdragonpart, (double)(-(f13 * 1.5F + f19 * f22) * f10), adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f11) + 1.5D, (double)((f14 * 1.5F + f20 * f22) * f10));
            }

            if (!this.level().isClientSide) {
               this.inWall = this.checkWalls(this.head.getBoundingBox()) | this.checkWalls(this.neck.getBoundingBox()) | this.checkWalls(this.body.getBoundingBox());
               if (this.dragonFight != null) {
                  this.dragonFight.updateDragon(this);
               }
            }

            for(int l = 0; l < this.subEntities.length; ++l) {
               this.subEntities[l].xo = avec3[l].x;
               this.subEntities[l].yo = avec3[l].y;
               this.subEntities[l].zo = avec3[l].z;
               this.subEntities[l].xOld = avec3[l].x;
               this.subEntities[l].yOld = avec3[l].y;
               this.subEntities[l].zOld = avec3[l].z;
            }

         }
      }
   }

   private void tickPart(EnderDragonPart enderdragonpart, double d0, double d1, double d2) {
      enderdragonpart.setPos(this.getX() + d0, this.getY() + d1, this.getZ() + d2);
   }

   private float getHeadYOffset() {
      if (this.phaseManager.getCurrentPhase().isSitting()) {
         return -1.0F;
      } else {
         double[] adouble = this.getLatencyPos(5, 1.0F);
         double[] adouble1 = this.getLatencyPos(0, 1.0F);
         return (float)(adouble[1] - adouble1[1]);
      }
   }

   private void checkCrystals() {
      if (this.nearestCrystal != null) {
         if (this.nearestCrystal.isRemoved()) {
            this.nearestCrystal = null;
         } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
         }
      }

      if (this.random.nextInt(10) == 0) {
         List<EndCrystal> list = this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0D));
         EndCrystal endcrystal = null;
         double d0 = Double.MAX_VALUE;

         for(EndCrystal endcrystal1 : list) {
            double d1 = endcrystal1.distanceToSqr(this);
            if (d1 < d0) {
               d0 = d1;
               endcrystal = endcrystal1;
            }
         }

         this.nearestCrystal = endcrystal;
      }

   }

   private void knockBack(List<Entity> list) {
      double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
      double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;

      for(Entity entity : list) {
         if (entity instanceof LivingEntity) {
            double d2 = entity.getX() - d0;
            double d3 = entity.getZ() - d1;
            double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
            entity.push(d2 / d4 * 4.0D, (double)0.2F, d3 / d4 * 4.0D);
            if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 2) {
               entity.hurt(this.damageSources().mobAttack(this), 5.0F);
               this.doEnchantDamageEffects(this, entity);
            }
         }
      }

   }

   private void hurt(List<Entity> list) {
      for(Entity entity : list) {
         if (entity instanceof LivingEntity) {
            entity.hurt(this.damageSources().mobAttack(this), 10.0F);
            this.doEnchantDamageEffects(this, entity);
         }
      }

   }

   private float rotWrap(double d0) {
      return (float)Mth.wrapDegrees(d0);
   }

   private boolean checkWalls(AABB aabb) {
      int i = Mth.floor(aabb.minX);
      int j = Mth.floor(aabb.minY);
      int k = Mth.floor(aabb.minZ);
      int l = Mth.floor(aabb.maxX);
      int i1 = Mth.floor(aabb.maxY);
      int j1 = Mth.floor(aabb.maxZ);
      boolean flag = false;
      boolean flag1 = false;

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = j; l1 <= i1; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               BlockPos blockpos = new BlockPos(k1, l1, i2);
               BlockState blockstate = this.level().getBlockState(blockpos);
               if (!blockstate.isAir() && !blockstate.is(BlockTags.DRAGON_TRANSPARENT)) {
                  if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && !blockstate.is(BlockTags.DRAGON_IMMUNE)) {
                     flag1 = this.level().removeBlock(blockpos, false) || flag1;
                  } else {
                     flag = true;
                  }
               }
            }
         }
      }

      if (flag1) {
         BlockPos blockpos1 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(i1 - j + 1), k + this.random.nextInt(j1 - k + 1));
         this.level().levelEvent(2008, blockpos1, 0);
      }

      return flag;
   }

   public boolean hurt(EnderDragonPart enderdragonpart, DamageSource damagesource, float f) {
      if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
         return false;
      } else {
         f = this.phaseManager.getCurrentPhase().onHurt(damagesource, f);
         if (enderdragonpart != this.head) {
            f = f / 4.0F + Math.min(f, 1.0F);
         }

         if (f < 0.01F) {
            return false;
         } else {
            if (damagesource.getEntity() instanceof Player || damagesource.is(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
               float f1 = this.getHealth();
               this.reallyHurt(damagesource, f);
               if (this.isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                  this.setHealth(1.0F);
                  this.phaseManager.setPhase(EnderDragonPhase.DYING);
               }

               if (this.phaseManager.getCurrentPhase().isSitting()) {
                  this.sittingDamageReceived = this.sittingDamageReceived + f1 - this.getHealth();
                  if (this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                     this.sittingDamageReceived = 0.0F;
                     this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                  }
               }
            }

            return true;
         }
      }
   }

   public boolean hurt(DamageSource damagesource, float f) {
      return !this.level().isClientSide ? this.hurt(this.body, damagesource, f) : false;
   }

   protected boolean reallyHurt(DamageSource damagesource, float f) {
      return super.hurt(damagesource, f);
   }

   public void kill() {
      this.remove(Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
         this.dragonFight.setDragonKilled(this);
      }

   }

   protected void tickDeath() {
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
      }

      ++this.dragonDeathTime;
      if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
         float f = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0D + (double)f1, this.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
      }

      boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
      int i = 500;
      if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
         i = 12000;
      }

      if (this.level() instanceof ServerLevel) {
         if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && flag) {
            ExperienceOrb.award((ServerLevel)this.level(), this.position(), Mth.floor((float)i * 0.08F));
         }

         if (this.dragonDeathTime == 1 && !this.isSilent()) {
            this.level().globalLevelEvent(1028, this.blockPosition(), 0);
         }
      }

      this.move(MoverType.SELF, new Vec3(0.0D, (double)0.1F, 0.0D));
      if (this.dragonDeathTime == 200 && this.level() instanceof ServerLevel) {
         if (flag) {
            ExperienceOrb.award((ServerLevel)this.level(), this.position(), Mth.floor((float)i * 0.2F));
         }

         if (this.dragonFight != null) {
            this.dragonFight.setDragonKilled(this);
         }

         this.remove(Entity.RemovalReason.KILLED);
         this.gameEvent(GameEvent.ENTITY_DIE);
      }

   }

   public int findClosestNode() {
      if (this.nodes[0] == null) {
         for(int i = 0; i < 24; ++i) {
            int j = 5;
            int l;
            int i1;
            if (i < 12) {
               l = Mth.floor(60.0F * Mth.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
               i1 = Mth.floor(60.0F * Mth.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
            } else if (i < 20) {
               int k = i - 12;
               l = Mth.floor(40.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)k)));
               i1 = Mth.floor(40.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)k)));
               j += 10;
            } else {
               int var7 = i - 20;
               l = Mth.floor(20.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)var7)));
               i1 = Mth.floor(20.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)var7)));
            }

            int j2 = Math.max(this.level().getSeaLevel() + 10, this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, i1)).getY() + j);
            this.nodes[i] = new Node(l, j2, i1);
         }

         this.nodeAdjacency[0] = 6146;
         this.nodeAdjacency[1] = 8197;
         this.nodeAdjacency[2] = 8202;
         this.nodeAdjacency[3] = 16404;
         this.nodeAdjacency[4] = 32808;
         this.nodeAdjacency[5] = 32848;
         this.nodeAdjacency[6] = 65696;
         this.nodeAdjacency[7] = 131392;
         this.nodeAdjacency[8] = 131712;
         this.nodeAdjacency[9] = 263424;
         this.nodeAdjacency[10] = 526848;
         this.nodeAdjacency[11] = 525313;
         this.nodeAdjacency[12] = 1581057;
         this.nodeAdjacency[13] = 3166214;
         this.nodeAdjacency[14] = 2138120;
         this.nodeAdjacency[15] = 6373424;
         this.nodeAdjacency[16] = 4358208;
         this.nodeAdjacency[17] = 12910976;
         this.nodeAdjacency[18] = 9044480;
         this.nodeAdjacency[19] = 9706496;
         this.nodeAdjacency[20] = 15216640;
         this.nodeAdjacency[21] = 13688832;
         this.nodeAdjacency[22] = 11763712;
         this.nodeAdjacency[23] = 8257536;
      }

      return this.findClosestNode(this.getX(), this.getY(), this.getZ());
   }

   public int findClosestNode(double d0, double d1, double d2) {
      float f = 10000.0F;
      int i = 0;
      Node node = new Node(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2));
      int j = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         j = 12;
      }

      for(int k = j; k < 24; ++k) {
         if (this.nodes[k] != null) {
            float f1 = this.nodes[k].distanceToSqr(node);
            if (f1 < f) {
               f = f1;
               i = k;
            }
         }
      }

      return i;
   }

   @Nullable
   public Path findPath(int i, int j, @Nullable Node node) {
      for(int k = 0; k < 24; ++k) {
         Node node1 = this.nodes[k];
         node1.closed = false;
         node1.f = 0.0F;
         node1.g = 0.0F;
         node1.h = 0.0F;
         node1.cameFrom = null;
         node1.heapIdx = -1;
      }

      Node node2 = this.nodes[i];
      Node node3 = this.nodes[j];
      node2.g = 0.0F;
      node2.h = node2.distanceTo(node3);
      node2.f = node2.h;
      this.openSet.clear();
      this.openSet.insert(node2);
      Node node4 = node2;
      int l = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         l = 12;
      }

      while(!this.openSet.isEmpty()) {
         Node node5 = this.openSet.pop();
         if (node5.equals(node3)) {
            if (node != null) {
               node.cameFrom = node3;
               node3 = node;
            }

            return this.reconstructPath(node2, node3);
         }

         if (node5.distanceTo(node3) < node4.distanceTo(node3)) {
            node4 = node5;
         }

         node5.closed = true;
         int i1 = 0;

         for(int j1 = 0; j1 < 24; ++j1) {
            if (this.nodes[j1] == node5) {
               i1 = j1;
               break;
            }
         }

         for(int k1 = l; k1 < 24; ++k1) {
            if ((this.nodeAdjacency[i1] & 1 << k1) > 0) {
               Node node6 = this.nodes[k1];
               if (!node6.closed) {
                  float f = node5.g + node5.distanceTo(node6);
                  if (!node6.inOpenSet() || f < node6.g) {
                     node6.cameFrom = node5;
                     node6.g = f;
                     node6.h = node6.distanceTo(node3);
                     if (node6.inOpenSet()) {
                        this.openSet.changeCost(node6, node6.g + node6.h);
                     } else {
                        node6.f = node6.g + node6.h;
                        this.openSet.insert(node6);
                     }
                  }
               }
            }
         }
      }

      if (node4 == node2) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", i, j);
         if (node != null) {
            node.cameFrom = node4;
            node4 = node;
         }

         return this.reconstructPath(node2, node4);
      }
   }

   private Path reconstructPath(Node node, Node node1) {
      List<Node> list = Lists.newArrayList();
      Node node2 = node1;
      list.add(0, node1);

      while(node2.cameFrom != null) {
         node2 = node2.cameFrom;
         list.add(0, node2);
      }

      return new Path(list, new BlockPos(node1.x, node1.y, node1.z), true);
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
      compoundtag.putInt("DragonDeathTime", this.dragonDeathTime);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("DragonPhase")) {
         this.phaseManager.setPhase(EnderDragonPhase.getById(compoundtag.getInt("DragonPhase")));
      }

      if (compoundtag.contains("DragonDeathTime")) {
         this.dragonDeathTime = compoundtag.getInt("DragonDeathTime");
      }

   }

   public void checkDespawn() {
   }

   public EnderDragonPart[] getSubEntities() {
      return this.subEntities;
   }

   public boolean isPickable() {
      return false;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENDER_DRAGON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.ENDER_DRAGON_HURT;
   }

   protected float getSoundVolume() {
      return 5.0F;
   }

   public float getHeadPartYOffset(int i, double[] adouble, double[] adouble1) {
      DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
      double d2;
      if (enderdragonphase != EnderDragonPhase.LANDING && enderdragonphase != EnderDragonPhase.TAKEOFF) {
         if (dragonphaseinstance.isSitting()) {
            d2 = (double)i;
         } else if (i == 6) {
            d2 = 0.0D;
         } else {
            d2 = adouble1[1] - adouble[1];
         }
      } else {
         BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
         double d0 = Math.max(Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0D, 1.0D);
         d2 = (double)i / d0;
      }

      return (float)d2;
   }

   public Vec3 getHeadLookVector(float f) {
      DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
      Vec3 vec31;
      if (enderdragonphase != EnderDragonPhase.LANDING && enderdragonphase != EnderDragonPhase.TAKEOFF) {
         if (dragonphaseinstance.isSitting()) {
            float f5 = this.getXRot();
            float f6 = 1.5F;
            this.setXRot(-45.0F);
            vec31 = this.getViewVector(f);
            this.setXRot(f5);
         } else {
            vec31 = this.getViewVector(f);
         }
      } else {
         BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
         float f1 = Math.max((float)Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0F, 1.0F);
         float f2 = 6.0F / f1;
         float f3 = this.getXRot();
         float f4 = 1.5F;
         this.setXRot(-f2 * 1.5F * 5.0F);
         vec31 = this.getViewVector(f);
         this.setXRot(f3);
      }

      return vec31;
   }

   public void onCrystalDestroyed(EndCrystal endcrystal, BlockPos blockpos, DamageSource damagesource) {
      Player player;
      if (damagesource.getEntity() instanceof Player) {
         player = (Player)damagesource.getEntity();
      } else {
         player = this.level().getNearestPlayer(CRYSTAL_DESTROY_TARGETING, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
      }

      if (endcrystal == this.nearestCrystal) {
         this.hurt(this.head, this.damageSources().explosion(endcrystal, player), 10.0F);
      }

      this.phaseManager.getCurrentPhase().onCrystalDestroyed(endcrystal, blockpos, damagesource, player);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_PHASE.equals(entitydataaccessor) && this.level().isClientSide) {
         this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   public EnderDragonPhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   @Nullable
   public EndDragonFight getDragonFight() {
      return this.dragonFight;
   }

   public boolean addEffect(MobEffectInstance mobeffectinstance, @Nullable Entity entity) {
      return false;
   }

   protected boolean canRide(Entity entity) {
      return false;
   }

   public boolean canChangeDimensions() {
      return false;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      EnderDragonPart[] aenderdragonpart = this.getSubEntities();

      for(int i = 0; i < aenderdragonpart.length; ++i) {
         aenderdragonpart[i].setId(i + clientboundaddentitypacket.getId());
      }

   }

   public boolean canAttack(LivingEntity livingentity) {
      return livingentity.canBeSeenAsEnemy();
   }

   public double getPassengersRidingOffset() {
      return (double)this.body.getBbHeight();
   }
}
