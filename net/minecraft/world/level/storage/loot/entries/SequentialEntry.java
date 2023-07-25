package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
   SequentialEntry(LootPoolEntryContainer[] alootpoolentrycontainer, LootItemCondition[] alootitemcondition) {
      super(alootpoolentrycontainer, alootitemcondition);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.SEQUENCE;
   }

   protected ComposableEntryContainer compose(ComposableEntryContainer[] acomposableentrycontainer) {
      switch (acomposableentrycontainer.length) {
         case 0:
            return ALWAYS_TRUE;
         case 1:
            return acomposableentrycontainer[0];
         case 2:
            return acomposableentrycontainer[0].and(acomposableentrycontainer[1]);
         default:
            return (lootcontext, consumer) -> {
               for(ComposableEntryContainer composableentrycontainer : acomposableentrycontainer) {
                  if (!composableentrycontainer.expand(lootcontext, consumer)) {
                     return false;
                  }
               }

               return true;
            };
      }
   }

   public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... alootpoolentrycontainer_builder) {
      return new SequentialEntry.Builder(alootpoolentrycontainer_builder);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

      public Builder(LootPoolEntryContainer.Builder<?>... alootpoolentrycontainer_builder) {
         for(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder : alootpoolentrycontainer_builder) {
            this.entries.add(lootpoolentrycontainer_builder.build());
         }

      }

      protected SequentialEntry.Builder getThis() {
         return this;
      }

      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         this.entries.add(lootpoolentrycontainer_builder.build());
         return this;
      }

      public LootPoolEntryContainer build() {
         return new SequentialEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
      }
   }
}
