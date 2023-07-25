package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpectralArrow extends AbstractArrow {
   private int duration = 200;

   public SpectralArrow(EntityType<? extends SpectralArrow> entitytype, Level level) {
      super(entitytype, level);
   }

   public SpectralArrow(Level level, LivingEntity livingentity) {
      super(EntityType.SPECTRAL_ARROW, livingentity, level);
   }

   public SpectralArrow(Level level, double d0, double d1, double d2) {
      super(EntityType.SPECTRAL_ARROW, d0, d1, d2, level);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide && !this.inGround) {
         this.level().addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
      }

   }

   protected ItemStack getPickupItem() {
      return new ItemStack(Items.SPECTRAL_ARROW);
   }

   protected void doPostHurtEffects(LivingEntity livingentity) {
      super.doPostHurtEffects(livingentity);
      MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
      livingentity.addEffect(mobeffectinstance, this.getEffectSource());
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("Duration")) {
         this.duration = compoundtag.getInt("Duration");
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putInt("Duration", this.duration);
   }
}
