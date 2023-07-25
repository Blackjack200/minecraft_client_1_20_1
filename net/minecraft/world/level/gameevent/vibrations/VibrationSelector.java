package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
   public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(VibrationInfo.CODEC.optionalFieldOf("event").forGetter((vibrationselector1) -> vibrationselector1.currentVibrationData.map(Pair::getLeft)), Codec.LONG.fieldOf("tick").forGetter((vibrationselector) -> vibrationselector.currentVibrationData.map(Pair::getRight).orElse(-1L))).apply(recordcodecbuilder_instance, VibrationSelector::new));
   private Optional<Pair<VibrationInfo, Long>> currentVibrationData;

   public VibrationSelector(Optional<VibrationInfo> optional, long i) {
      this.currentVibrationData = optional.map((vibrationinfo) -> Pair.of(vibrationinfo, i));
   }

   public VibrationSelector() {
      this.currentVibrationData = Optional.empty();
   }

   public void addCandidate(VibrationInfo vibrationinfo, long i) {
      if (this.shouldReplaceVibration(vibrationinfo, i)) {
         this.currentVibrationData = Optional.of(Pair.of(vibrationinfo, i));
      }

   }

   private boolean shouldReplaceVibration(VibrationInfo vibrationinfo, long i) {
      if (this.currentVibrationData.isEmpty()) {
         return true;
      } else {
         Pair<VibrationInfo, Long> pair = this.currentVibrationData.get();
         long j = pair.getRight();
         if (i != j) {
            return false;
         } else {
            VibrationInfo vibrationinfo1 = pair.getLeft();
            if (vibrationinfo.distance() < vibrationinfo1.distance()) {
               return true;
            } else if (vibrationinfo.distance() > vibrationinfo1.distance()) {
               return false;
            } else {
               return VibrationSystem.getGameEventFrequency(vibrationinfo.gameEvent()) > VibrationSystem.getGameEventFrequency(vibrationinfo1.gameEvent());
            }
         }
      }
   }

   public Optional<VibrationInfo> chosenCandidate(long i) {
      if (this.currentVibrationData.isEmpty()) {
         return Optional.empty();
      } else {
         return this.currentVibrationData.get().getRight() < i ? Optional.of(this.currentVibrationData.get().getLeft()) : Optional.empty();
      }
   }

   public void startOver() {
      this.currentVibrationData = Optional.empty();
   }
}
