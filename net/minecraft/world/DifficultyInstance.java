package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;

@Immutable
public class DifficultyInstance {
   private static final float DIFFICULTY_TIME_GLOBAL_OFFSET = -72000.0F;
   private static final float MAX_DIFFICULTY_TIME_GLOBAL = 1440000.0F;
   private static final float MAX_DIFFICULTY_TIME_LOCAL = 3600000.0F;
   private final Difficulty base;
   private final float effectiveDifficulty;

   public DifficultyInstance(Difficulty difficulty, long i, long j, float f) {
      this.base = difficulty;
      this.effectiveDifficulty = this.calculateDifficulty(difficulty, i, j, f);
   }

   public Difficulty getDifficulty() {
      return this.base;
   }

   public float getEffectiveDifficulty() {
      return this.effectiveDifficulty;
   }

   public boolean isHard() {
      return this.effectiveDifficulty >= (float)Difficulty.HARD.ordinal();
   }

   public boolean isHarderThan(float f) {
      return this.effectiveDifficulty > f;
   }

   public float getSpecialMultiplier() {
      if (this.effectiveDifficulty < 2.0F) {
         return 0.0F;
      } else {
         return this.effectiveDifficulty > 4.0F ? 1.0F : (this.effectiveDifficulty - 2.0F) / 2.0F;
      }
   }

   private float calculateDifficulty(Difficulty difficulty, long i, long j, float f) {
      if (difficulty == Difficulty.PEACEFUL) {
         return 0.0F;
      } else {
         boolean flag = difficulty == Difficulty.HARD;
         float f1 = 0.75F;
         float f2 = Mth.clamp(((float)i + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
         f1 += f2;
         float f3 = 0.0F;
         f3 += Mth.clamp((float)j / 3600000.0F, 0.0F, 1.0F) * (flag ? 1.0F : 0.75F);
         f3 += Mth.clamp(f * 0.25F, 0.0F, f2);
         if (difficulty == Difficulty.EASY) {
            f3 *= 0.5F;
         }

         f1 += f3;
         return (float)difficulty.getId() * f1;
      }
   }
}
