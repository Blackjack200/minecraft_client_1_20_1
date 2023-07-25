package net.minecraft.world.level.storage.loot;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface LootDataResolver {
   @Nullable
   <T> T getElement(LootDataId<T> lootdataid);

   @Nullable
   default <T> T getElement(LootDataType<T> lootdatatype, ResourceLocation resourcelocation) {
      return this.getElement(new LootDataId<>(lootdatatype, resourcelocation));
   }

   default <T> Optional<T> getElementOptional(LootDataId<T> lootdataid) {
      return Optional.ofNullable(this.getElement(lootdataid));
   }

   default <T> Optional<T> getElementOptional(LootDataType<T> lootdatatype, ResourceLocation resourcelocation) {
      return this.getElementOptional(new LootDataId<>(lootdatatype, resourcelocation));
   }

   default LootTable getLootTable(ResourceLocation resourcelocation) {
      return this.getElementOptional(LootDataType.TABLE, resourcelocation).orElse(LootTable.EMPTY);
   }
}
