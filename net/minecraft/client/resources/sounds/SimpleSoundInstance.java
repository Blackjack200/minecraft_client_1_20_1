package net.minecraft.client.resources.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class SimpleSoundInstance extends AbstractSoundInstance {
   public SimpleSoundInstance(SoundEvent soundevent, SoundSource soundsource, float f, float f1, RandomSource randomsource, BlockPos blockpos) {
      this(soundevent, soundsource, f, f1, randomsource, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
   }

   public static SimpleSoundInstance forUI(SoundEvent soundevent, float f) {
      return forUI(soundevent, f, 0.25F);
   }

   public static SimpleSoundInstance forUI(Holder<SoundEvent> holder, float f) {
      return forUI(holder.value(), f);
   }

   public static SimpleSoundInstance forUI(SoundEvent soundevent, float f, float f1) {
      return new SimpleSoundInstance(soundevent.getLocation(), SoundSource.MASTER, f1, f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
   }

   public static SimpleSoundInstance forMusic(SoundEvent soundevent) {
      return new SimpleSoundInstance(soundevent.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
   }

   public static SimpleSoundInstance forRecord(SoundEvent soundevent, Vec3 vec3) {
      return new SimpleSoundInstance(soundevent, SoundSource.RECORDS, 4.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, vec3.x, vec3.y, vec3.z);
   }

   public static SimpleSoundInstance forLocalAmbience(SoundEvent soundevent, float f, float f1) {
      return new SimpleSoundInstance(soundevent.getLocation(), SoundSource.AMBIENT, f1, f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true);
   }

   public static SimpleSoundInstance forAmbientAddition(SoundEvent soundevent) {
      return forLocalAmbience(soundevent, 1.0F, 1.0F);
   }

   public static SimpleSoundInstance forAmbientMood(SoundEvent soundevent, RandomSource randomsource, double d0, double d1, double d2) {
      return new SimpleSoundInstance(soundevent, SoundSource.AMBIENT, 1.0F, 1.0F, randomsource, false, 0, SoundInstance.Attenuation.LINEAR, d0, d1, d2);
   }

   public SimpleSoundInstance(SoundEvent soundevent, SoundSource soundsource, float f, float f1, RandomSource randomsource, double d0, double d1, double d2) {
      this(soundevent, soundsource, f, f1, randomsource, false, 0, SoundInstance.Attenuation.LINEAR, d0, d1, d2);
   }

   private SimpleSoundInstance(SoundEvent soundevent, SoundSource soundsource, float f, float f1, RandomSource randomsource, boolean flag, int i, SoundInstance.Attenuation soundinstance_attenuation, double d0, double d1, double d2) {
      this(soundevent.getLocation(), soundsource, f, f1, randomsource, flag, i, soundinstance_attenuation, d0, d1, d2, false);
   }

   public SimpleSoundInstance(ResourceLocation resourcelocation, SoundSource soundsource, float f, float f1, RandomSource randomsource, boolean flag, int i, SoundInstance.Attenuation soundinstance_attenuation, double d0, double d1, double d2, boolean flag1) {
      super(resourcelocation, soundsource, randomsource);
      this.volume = f;
      this.pitch = f1;
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.looping = flag;
      this.delay = i;
      this.attenuation = soundinstance_attenuation;
      this.relative = flag1;
   }
}
