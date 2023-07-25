package net.minecraft.world.entity.monster.warden;

import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AngerLevel {
   CALM(0, SoundEvents.WARDEN_AMBIENT, SoundEvents.WARDEN_LISTENING),
   AGITATED(40, SoundEvents.WARDEN_AGITATED, SoundEvents.WARDEN_LISTENING_ANGRY),
   ANGRY(80, SoundEvents.WARDEN_ANGRY, SoundEvents.WARDEN_LISTENING_ANGRY);

   private static final AngerLevel[] SORTED_LEVELS = Util.make(values(), (aangerlevel) -> Arrays.sort(aangerlevel, (angerlevel, angerlevel1) -> Integer.compare(angerlevel1.minimumAnger, angerlevel.minimumAnger)));
   private final int minimumAnger;
   private final SoundEvent ambientSound;
   private final SoundEvent listeningSound;

   private AngerLevel(int i, SoundEvent soundevent, SoundEvent soundevent1) {
      this.minimumAnger = i;
      this.ambientSound = soundevent;
      this.listeningSound = soundevent1;
   }

   public int getMinimumAnger() {
      return this.minimumAnger;
   }

   public SoundEvent getAmbientSound() {
      return this.ambientSound;
   }

   public SoundEvent getListeningSound() {
      return this.listeningSound;
   }

   public static AngerLevel byAnger(int i) {
      for(AngerLevel angerlevel : SORTED_LEVELS) {
         if (i >= angerlevel.minimumAnger) {
            return angerlevel;
         }
      }

      return CALM;
   }

   public boolean isAngry() {
      return this == ANGRY;
   }
}
