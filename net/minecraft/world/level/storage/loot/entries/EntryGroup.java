package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup extends CompositeEntryBase {
   EntryGroup(LootPoolEntryContainer[] alootpoolentrycontainer, LootItemCondition[] alootitemcondition) {
      super(alootpoolentrycontainer, alootitemcondition);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.GROUP;
   }

   protected ComposableEntryContainer compose(ComposableEntryContainer[] acomposableentrycontainer) {
      switch (acomposableentrycontainer.length) {
         case 0:
            return ALWAYS_TRUE;
         case 1:
            return acomposableentrycontainer[0];
         case 2:
            ComposableEntryContainer composableentrycontainer = acomposableentrycontainer[0];
            ComposableEntryContainer composableentrycontainer1 = acomposableentrycontainer[1];
            return (lootcontext1, consumer1) -> {
               composableentrycontainer.expand(lootcontext1, consumer1);
               composableentrycontainer1.expand(lootcontext1, consumer1);
               return true;
            };
         default:
            return (lootcontext, consumer) -> {
               for(ComposableEntryContainer composableentrycontainer2 : acomposableentrycontainer) {
                  composableentrycontainer2.expand(lootcontext, consumer);
               }

               return true;
            };
      }
   }

   public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... alootpoolentrycontainer_builder) {
      return new EntryGroup.Builder(alootpoolentrycontainer_builder);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

      public Builder(LootPoolEntryContainer.Builder<?>... alootpoolentrycontainer_builder) {
         for(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder : alootpoolentrycontainer_builder) {
            this.entries.add(lootpoolentrycontainer_builder.build());
         }

      }

      protected EntryGroup.Builder getThis() {
         return this;
      }

      public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         this.entries.add(lootpoolentrycontainer_builder.build());
         return this;
      }

      public LootPoolEntryContainer build() {
         return new EntryGroup(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
      }
   }
}
