package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class Donkey extends AbstractChestedHorse {
   public Donkey(EntityType<? extends Donkey> entitytype, Level level) {
      super(entitytype, level);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.DONKEY_AMBIENT;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.DONKEY_ANGRY;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.DONKEY_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.DONKEY_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource damagesource) {
      return SoundEvents.DONKEY_HURT;
   }

   public boolean canMate(Animal animal) {
      if (animal == this) {
         return false;
      } else if (!(animal instanceof Donkey) && !(animal instanceof Horse)) {
         return false;
      } else {
         return this.canParent() && ((AbstractHorse)animal).canParent();
      }
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel serverlevel, AgeableMob ageablemob) {
      EntityType<? extends AbstractHorse> entitytype = ageablemob instanceof Horse ? EntityType.MULE : EntityType.DONKEY;
      AbstractHorse abstracthorse = entitytype.create(serverlevel);
      if (abstracthorse != null) {
         this.setOffspringAttributes(ageablemob, abstracthorse);
      }

      return abstracthorse;
   }
}
