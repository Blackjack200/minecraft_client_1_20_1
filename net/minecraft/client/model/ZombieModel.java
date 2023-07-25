package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieModel<T extends Zombie> extends AbstractZombieModel<T> {
   public ZombieModel(ModelPart modelpart) {
      super(modelpart);
   }

   public boolean isAggressive(T zombie) {
      return zombie.isAggressive();
   }
}
