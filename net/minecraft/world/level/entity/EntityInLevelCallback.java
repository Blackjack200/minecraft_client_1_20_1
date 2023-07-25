package net.minecraft.world.level.entity;

import net.minecraft.world.entity.Entity;

public interface EntityInLevelCallback {
   EntityInLevelCallback NULL = new EntityInLevelCallback() {
      public void onMove() {
      }

      public void onRemove(Entity.RemovalReason entity_removalreason) {
      }
   };

   void onMove();

   void onRemove(Entity.RemovalReason entity_removalreason);
}
