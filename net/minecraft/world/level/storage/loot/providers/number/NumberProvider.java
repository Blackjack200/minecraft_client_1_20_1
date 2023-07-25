package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface NumberProvider extends LootContextUser {
   float getFloat(LootContext lootcontext);

   default int getInt(LootContext lootcontext) {
      return Math.round(this.getFloat(lootcontext));
   }

   LootNumberProviderType getType();
}
