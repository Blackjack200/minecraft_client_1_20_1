package net.minecraft.client.resources.sounds;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public class BiomeAmbientSoundsHandler implements AmbientSoundHandler {
   private static final int LOOP_SOUND_CROSS_FADE_TIME = 40;
   private static final float SKY_MOOD_RECOVERY_RATE = 0.001F;
   private final LocalPlayer player;
   private final SoundManager soundManager;
   private final BiomeManager biomeManager;
   private final RandomSource random;
   private final Object2ObjectArrayMap<Biome, BiomeAmbientSoundsHandler.LoopSoundInstance> loopSounds = new Object2ObjectArrayMap<>();
   private Optional<AmbientMoodSettings> moodSettings = Optional.empty();
   private Optional<AmbientAdditionsSettings> additionsSettings = Optional.empty();
   private float moodiness;
   @Nullable
   private Biome previousBiome;

   public BiomeAmbientSoundsHandler(LocalPlayer localplayer, SoundManager soundmanager, BiomeManager biomemanager) {
      this.random = localplayer.level().getRandom();
      this.player = localplayer;
      this.soundManager = soundmanager;
      this.biomeManager = biomemanager;
   }

   public float getMoodiness() {
      return this.moodiness;
   }

   public void tick() {
      this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
      Biome biome = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ()).value();
      if (biome != this.previousBiome) {
         this.previousBiome = biome;
         this.moodSettings = biome.getAmbientMood();
         this.additionsSettings = biome.getAmbientAdditions();
         this.loopSounds.values().forEach(BiomeAmbientSoundsHandler.LoopSoundInstance::fadeOut);
         biome.getAmbientLoop().ifPresent((holder) -> this.loopSounds.compute(biome, (biome2, biomeambientsoundshandler_loopsoundinstance) -> {
               if (biomeambientsoundshandler_loopsoundinstance == null) {
                  biomeambientsoundshandler_loopsoundinstance = new BiomeAmbientSoundsHandler.LoopSoundInstance((SoundEvent)holder.value());
                  this.soundManager.play(biomeambientsoundshandler_loopsoundinstance);
               }

               biomeambientsoundshandler_loopsoundinstance.fadeIn();
               return biomeambientsoundshandler_loopsoundinstance;
            }));
      }

      this.additionsSettings.ifPresent((ambientadditionssettings) -> {
         if (this.random.nextDouble() < ambientadditionssettings.getTickChance()) {
            this.soundManager.play(SimpleSoundInstance.forAmbientAddition(ambientadditionssettings.getSoundEvent().value()));
         }

      });
      this.moodSettings.ifPresent((ambientmoodsettings) -> {
         Level level = this.player.level();
         int i = ambientmoodsettings.getBlockSearchExtent() * 2 + 1;
         BlockPos blockpos = BlockPos.containing(this.player.getX() + (double)this.random.nextInt(i) - (double)ambientmoodsettings.getBlockSearchExtent(), this.player.getEyeY() + (double)this.random.nextInt(i) - (double)ambientmoodsettings.getBlockSearchExtent(), this.player.getZ() + (double)this.random.nextInt(i) - (double)ambientmoodsettings.getBlockSearchExtent());
         int j = level.getBrightness(LightLayer.SKY, blockpos);
         if (j > 0) {
            this.moodiness -= (float)j / (float)level.getMaxLightLevel() * 0.001F;
         } else {
            this.moodiness -= (float)(level.getBrightness(LightLayer.BLOCK, blockpos) - 1) / (float)ambientmoodsettings.getTickDelay();
         }

         if (this.moodiness >= 1.0F) {
            double d0 = (double)blockpos.getX() + 0.5D;
            double d1 = (double)blockpos.getY() + 0.5D;
            double d2 = (double)blockpos.getZ() + 0.5D;
            double d3 = d0 - this.player.getX();
            double d4 = d1 - this.player.getEyeY();
            double d5 = d2 - this.player.getZ();
            double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
            double d7 = d6 + ambientmoodsettings.getSoundPositionOffset();
            SimpleSoundInstance simplesoundinstance = SimpleSoundInstance.forAmbientMood(ambientmoodsettings.getSoundEvent().value(), this.random, this.player.getX() + d3 / d6 * d7, this.player.getEyeY() + d4 / d6 * d7, this.player.getZ() + d5 / d6 * d7);
            this.soundManager.play(simplesoundinstance);
            this.moodiness = 0.0F;
         } else {
            this.moodiness = Math.max(this.moodiness, 0.0F);
         }

      });
   }

   public static class LoopSoundInstance extends AbstractTickableSoundInstance {
      private int fadeDirection;
      private int fade;

      public LoopSoundInstance(SoundEvent soundevent) {
         super(soundevent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
         this.looping = true;
         this.delay = 0;
         this.volume = 1.0F;
         this.relative = true;
      }

      public void tick() {
         if (this.fade < 0) {
            this.stop();
         }

         this.fade += this.fadeDirection;
         this.volume = Mth.clamp((float)this.fade / 40.0F, 0.0F, 1.0F);
      }

      public void fadeOut() {
         this.fade = Math.min(this.fade, 40);
         this.fadeDirection = -1;
      }

      public void fadeIn() {
         this.fade = Math.max(0, this.fade);
         this.fadeDirection = 1;
      }
   }
}
