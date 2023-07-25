package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class EntityBoundSoundInstance extends AbstractTickableSoundInstance {
   private final Entity entity;

   public EntityBoundSoundInstance(SoundEvent soundevent, SoundSource soundsource, float f, float f1, Entity entity, long i) {
      super(soundevent, soundsource, RandomSource.create(i));
      this.volume = f;
      this.pitch = f1;
      this.entity = entity;
      this.x = (double)((float)this.entity.getX());
      this.y = (double)((float)this.entity.getY());
      this.z = (double)((float)this.entity.getZ());
   }

   public boolean canPlaySound() {
      return !this.entity.isSilent();
   }

   public void tick() {
      if (this.entity.isRemoved()) {
         this.stop();
      } else {
         this.x = (double)((float)this.entity.getX());
         this.y = (double)((float)this.entity.getY());
         this.z = (double)((float)this.entity.getZ());
      }
   }
}
