package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud extends Entity implements TraceableEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int TIME_BETWEEN_APPLICATIONS = 5;
   private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
   private static final float MAX_RADIUS = 32.0F;
   private static final float MINIMAL_RADIUS = 0.5F;
   private static final float DEFAULT_RADIUS = 3.0F;
   public static final float DEFAULT_WIDTH = 6.0F;
   public static final float HEIGHT = 0.5F;
   private Potion potion = Potions.EMPTY;
   private final List<MobEffectInstance> effects = Lists.newArrayList();
   private final Map<Entity, Integer> victims = Maps.newHashMap();
   private int duration = 600;
   private int waitTime = 20;
   private int reapplicationDelay = 20;
   private boolean fixedColor;
   private int durationOnUse;
   private float radiusOnUse;
   private float radiusPerTick;
   @Nullable
   private LivingEntity owner;
   @Nullable
   private UUID ownerUUID;

   public AreaEffectCloud(EntityType<? extends AreaEffectCloud> entitytype, Level level) {
      super(entitytype, level);
      this.noPhysics = true;
   }

   public AreaEffectCloud(Level level, double d0, double d1, double d2) {
      this(EntityType.AREA_EFFECT_CLOUD, level);
      this.setPos(d0, d1, d2);
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_COLOR, 0);
      this.getEntityData().define(DATA_RADIUS, 3.0F);
      this.getEntityData().define(DATA_WAITING, false);
      this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
   }

   public void setRadius(float f) {
      if (!this.level().isClientSide) {
         this.getEntityData().set(DATA_RADIUS, Mth.clamp(f, 0.0F, 32.0F));
      }

   }

   public void refreshDimensions() {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      super.refreshDimensions();
      this.setPos(d0, d1, d2);
   }

   public float getRadius() {
      return this.getEntityData().get(DATA_RADIUS);
   }

   public void setPotion(Potion potion) {
      this.potion = potion;
      if (!this.fixedColor) {
         this.updateColor();
      }

   }

   private void updateColor() {
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.getEntityData().set(DATA_COLOR, 0);
      } else {
         this.getEntityData().set(DATA_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(MobEffectInstance mobeffectinstance) {
      this.effects.add(mobeffectinstance);
      if (!this.fixedColor) {
         this.updateColor();
      }

   }

   public int getColor() {
      return this.getEntityData().get(DATA_COLOR);
   }

   public void setFixedColor(int i) {
      this.fixedColor = true;
      this.getEntityData().set(DATA_COLOR, i);
   }

   public ParticleOptions getParticle() {
      return this.getEntityData().get(DATA_PARTICLE);
   }

   public void setParticle(ParticleOptions particleoptions) {
      this.getEntityData().set(DATA_PARTICLE, particleoptions);
   }

   protected void setWaiting(boolean flag) {
      this.getEntityData().set(DATA_WAITING, flag);
   }

   public boolean isWaiting() {
      return this.getEntityData().get(DATA_WAITING);
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int i) {
      this.duration = i;
   }

   public void tick() {
      super.tick();
      boolean flag = this.isWaiting();
      float f = this.getRadius();
      if (this.level().isClientSide) {
         if (flag && this.random.nextBoolean()) {
            return;
         }

         ParticleOptions particleoptions = this.getParticle();
         int i;
         float f1;
         if (flag) {
            i = 2;
            f1 = 0.2F;
         } else {
            i = Mth.ceil((float)Math.PI * f * f);
            f1 = f;
         }

         for(int k = 0; k < i; ++k) {
            float f3 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f4 = Mth.sqrt(this.random.nextFloat()) * f1;
            double d0 = this.getX() + (double)(Mth.cos(f3) * f4);
            double d1 = this.getY();
            double d2 = this.getZ() + (double)(Mth.sin(f3) * f4);
            double d6;
            double d7;
            double d8;
            if (particleoptions.getType() != ParticleTypes.ENTITY_EFFECT) {
               if (flag) {
                  d6 = 0.0D;
                  d7 = 0.0D;
                  d8 = 0.0D;
               } else {
                  d6 = (0.5D - this.random.nextDouble()) * 0.15D;
                  d7 = (double)0.01F;
                  d8 = (0.5D - this.random.nextDouble()) * 0.15D;
               }
            } else {
               int l = flag && this.random.nextBoolean() ? 16777215 : this.getColor();
               d6 = (double)((float)(l >> 16 & 255) / 255.0F);
               d7 = (double)((float)(l >> 8 & 255) / 255.0F);
               d8 = (double)((float)(l & 255) / 255.0F);
            }

            this.level().addAlwaysVisibleParticle(particleoptions, d0, d1, d2, d6, d7, d8);
         }
      } else {
         if (this.tickCount >= this.waitTime + this.duration) {
            this.discard();
            return;
         }

         boolean flag1 = this.tickCount < this.waitTime;
         if (flag != flag1) {
            this.setWaiting(flag1);
         }

         if (flag1) {
            return;
         }

         if (this.radiusPerTick != 0.0F) {
            f += this.radiusPerTick;
            if (f < 0.5F) {
               this.discard();
               return;
            }

            this.setRadius(f);
         }

         if (this.tickCount % 5 == 0) {
            this.victims.entrySet().removeIf((map_entry) -> this.tickCount >= map_entry.getValue());
            List<MobEffectInstance> list = Lists.newArrayList();

            for(MobEffectInstance mobeffectinstance : this.potion.getEffects()) {
               list.add(new MobEffectInstance(mobeffectinstance.getEffect(), mobeffectinstance.mapDuration((i1) -> i1 / 4), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()));
            }

            list.addAll(this.effects);
            if (list.isEmpty()) {
               this.victims.clear();
            } else {
               List<LivingEntity> list1 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
               if (!list1.isEmpty()) {
                  for(LivingEntity livingentity : list1) {
                     if (!this.victims.containsKey(livingentity) && livingentity.isAffectedByPotions()) {
                        double d12 = livingentity.getX() - this.getX();
                        double d13 = livingentity.getZ() - this.getZ();
                        double d14 = d12 * d12 + d13 * d13;
                        if (d14 <= (double)(f * f)) {
                           this.victims.put(livingentity, this.tickCount + this.reapplicationDelay);

                           for(MobEffectInstance mobeffectinstance1 : list) {
                              if (mobeffectinstance1.getEffect().isInstantenous()) {
                                 mobeffectinstance1.getEffect().applyInstantenousEffect(this, this.getOwner(), livingentity, mobeffectinstance1.getAmplifier(), 0.5D);
                              } else {
                                 livingentity.addEffect(new MobEffectInstance(mobeffectinstance1), this);
                              }
                           }

                           if (this.radiusOnUse != 0.0F) {
                              f += this.radiusOnUse;
                              if (f < 0.5F) {
                                 this.discard();
                                 return;
                              }

                              this.setRadius(f);
                           }

                           if (this.durationOnUse != 0) {
                              this.duration += this.durationOnUse;
                              if (this.duration <= 0) {
                                 this.discard();
                                 return;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public float getRadiusOnUse() {
      return this.radiusOnUse;
   }

   public void setRadiusOnUse(float f) {
      this.radiusOnUse = f;
   }

   public float getRadiusPerTick() {
      return this.radiusPerTick;
   }

   public void setRadiusPerTick(float f) {
      this.radiusPerTick = f;
   }

   public int getDurationOnUse() {
      return this.durationOnUse;
   }

   public void setDurationOnUse(int i) {
      this.durationOnUse = i;
   }

   public int getWaitTime() {
      return this.waitTime;
   }

   public void setWaitTime(int i) {
      this.waitTime = i;
   }

   public void setOwner(@Nullable LivingEntity livingentity) {
      this.owner = livingentity;
      this.ownerUUID = livingentity == null ? null : livingentity.getUUID();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
         Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      this.tickCount = compoundtag.getInt("Age");
      this.duration = compoundtag.getInt("Duration");
      this.waitTime = compoundtag.getInt("WaitTime");
      this.reapplicationDelay = compoundtag.getInt("ReapplicationDelay");
      this.durationOnUse = compoundtag.getInt("DurationOnUse");
      this.radiusOnUse = compoundtag.getFloat("RadiusOnUse");
      this.radiusPerTick = compoundtag.getFloat("RadiusPerTick");
      this.setRadius(compoundtag.getFloat("Radius"));
      if (compoundtag.hasUUID("Owner")) {
         this.ownerUUID = compoundtag.getUUID("Owner");
      }

      if (compoundtag.contains("Particle", 8)) {
         try {
            this.setParticle(ParticleArgument.readParticle(new StringReader(compoundtag.getString("Particle")), BuiltInRegistries.PARTICLE_TYPE.asLookup()));
         } catch (CommandSyntaxException var5) {
            LOGGER.warn("Couldn't load custom particle {}", compoundtag.getString("Particle"), var5);
         }
      }

      if (compoundtag.contains("Color", 99)) {
         this.setFixedColor(compoundtag.getInt("Color"));
      }

      if (compoundtag.contains("Potion", 8)) {
         this.setPotion(PotionUtils.getPotion(compoundtag));
      }

      if (compoundtag.contains("Effects", 9)) {
         ListTag listtag = compoundtag.getList("Effects", 10);
         this.effects.clear();

         for(int i = 0; i < listtag.size(); ++i) {
            MobEffectInstance mobeffectinstance = MobEffectInstance.load(listtag.getCompound(i));
            if (mobeffectinstance != null) {
               this.addEffect(mobeffectinstance);
            }
         }
      }

   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putInt("Age", this.tickCount);
      compoundtag.putInt("Duration", this.duration);
      compoundtag.putInt("WaitTime", this.waitTime);
      compoundtag.putInt("ReapplicationDelay", this.reapplicationDelay);
      compoundtag.putInt("DurationOnUse", this.durationOnUse);
      compoundtag.putFloat("RadiusOnUse", this.radiusOnUse);
      compoundtag.putFloat("RadiusPerTick", this.radiusPerTick);
      compoundtag.putFloat("Radius", this.getRadius());
      compoundtag.putString("Particle", this.getParticle().writeToString());
      if (this.ownerUUID != null) {
         compoundtag.putUUID("Owner", this.ownerUUID);
      }

      if (this.fixedColor) {
         compoundtag.putInt("Color", this.getColor());
      }

      if (this.potion != Potions.EMPTY) {
         compoundtag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
      }

      if (!this.effects.isEmpty()) {
         ListTag listtag = new ListTag();

         for(MobEffectInstance mobeffectinstance : this.effects) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }

         compoundtag.put("Effects", listtag);
      }

   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> entitydataaccessor) {
      if (DATA_RADIUS.equals(entitydataaccessor)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(entitydataaccessor);
   }

   public Potion getPotion() {
      return this.potion;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
   }
}
