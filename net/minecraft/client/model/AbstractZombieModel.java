package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;

public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
   protected AbstractZombieModel(ModelPart modelpart) {
      super(modelpart);
   }

   public void setupAnim(T monster, float f, float f1, float f2, float f3, float f4) {
      super.setupAnim(monster, f, f1, f2, f3, f4);
      AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(monster), this.attackTime, f2);
   }

   public abstract boolean isAggressive(T monster);
}
