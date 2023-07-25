package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;

@FunctionalInterface
interface ComposableEntryContainer {
   ComposableEntryContainer ALWAYS_FALSE = (lootcontext, consumer) -> false;
   ComposableEntryContainer ALWAYS_TRUE = (lootcontext, consumer) -> true;

   boolean expand(LootContext lootcontext, Consumer<LootPoolEntry> consumer);

   default ComposableEntryContainer and(ComposableEntryContainer composableentrycontainer) {
      Objects.requireNonNull(composableentrycontainer);
      return (lootcontext, consumer) -> this.expand(lootcontext, consumer) && composableentrycontainer.expand(lootcontext, consumer);
   }

   default ComposableEntryContainer or(ComposableEntryContainer composableentrycontainer) {
      Objects.requireNonNull(composableentrycontainer);
      return (lootcontext, consumer) -> this.expand(lootcontext, consumer) || composableentrycontainer.expand(lootcontext, consumer);
   }
}
