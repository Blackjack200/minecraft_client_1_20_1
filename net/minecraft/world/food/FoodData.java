package net.minecraft.world.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public class FoodData {
   private int foodLevel = 20;
   private float saturationLevel;
   private float exhaustionLevel;
   private int tickTimer;
   private int lastFoodLevel = 20;

   public FoodData() {
      this.saturationLevel = 5.0F;
   }

   public void eat(int i, float f) {
      this.foodLevel = Math.min(i + this.foodLevel, 20);
      this.saturationLevel = Math.min(this.saturationLevel + (float)i * f * 2.0F, (float)this.foodLevel);
   }

   public void eat(Item item, ItemStack itemstack) {
      if (item.isEdible()) {
         FoodProperties foodproperties = item.getFoodProperties();
         this.eat(foodproperties.getNutrition(), foodproperties.getSaturationModifier());
      }

   }

   public void tick(Player player) {
      Difficulty difficulty = player.level().getDifficulty();
      this.lastFoodLevel = this.foodLevel;
      if (this.exhaustionLevel > 4.0F) {
         this.exhaustionLevel -= 4.0F;
         if (this.saturationLevel > 0.0F) {
            this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
         } else if (difficulty != Difficulty.PEACEFUL) {
            this.foodLevel = Math.max(this.foodLevel - 1, 0);
         }
      }

      boolean flag = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
      if (flag && this.saturationLevel > 0.0F && player.isHurt() && this.foodLevel >= 20) {
         ++this.tickTimer;
         if (this.tickTimer >= 10) {
            float f = Math.min(this.saturationLevel, 6.0F);
            player.heal(f / 6.0F);
            this.addExhaustion(f);
            this.tickTimer = 0;
         }
      } else if (flag && this.foodLevel >= 18 && player.isHurt()) {
         ++this.tickTimer;
         if (this.tickTimer >= 80) {
            player.heal(1.0F);
            this.addExhaustion(6.0F);
            this.tickTimer = 0;
         }
      } else if (this.foodLevel <= 0) {
         ++this.tickTimer;
         if (this.tickTimer >= 80) {
            if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
               player.hurt(player.damageSources().starve(), 1.0F);
            }

            this.tickTimer = 0;
         }
      } else {
         this.tickTimer = 0;
      }

   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      if (compoundtag.contains("foodLevel", 99)) {
         this.foodLevel = compoundtag.getInt("foodLevel");
         this.tickTimer = compoundtag.getInt("foodTickTimer");
         this.saturationLevel = compoundtag.getFloat("foodSaturationLevel");
         this.exhaustionLevel = compoundtag.getFloat("foodExhaustionLevel");
      }

   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putInt("foodLevel", this.foodLevel);
      compoundtag.putInt("foodTickTimer", this.tickTimer);
      compoundtag.putFloat("foodSaturationLevel", this.saturationLevel);
      compoundtag.putFloat("foodExhaustionLevel", this.exhaustionLevel);
   }

   public int getFoodLevel() {
      return this.foodLevel;
   }

   public int getLastFoodLevel() {
      return this.lastFoodLevel;
   }

   public boolean needsFood() {
      return this.foodLevel < 20;
   }

   public void addExhaustion(float f) {
      this.exhaustionLevel = Math.min(this.exhaustionLevel + f, 40.0F);
   }

   public float getExhaustionLevel() {
      return this.exhaustionLevel;
   }

   public float getSaturationLevel() {
      return this.saturationLevel;
   }

   public void setFoodLevel(int i) {
      this.foodLevel = i;
   }

   public void setSaturation(float f) {
      this.saturationLevel = f;
   }

   public void setExhaustion(float f) {
      this.exhaustionLevel = f;
   }
}
