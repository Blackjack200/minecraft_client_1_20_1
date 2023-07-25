package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public class AlternativesEntry extends CompositeEntryBase {
   AlternativesEntry(LootPoolEntryContainer[] alootpoolentrycontainer, LootItemCondition[] alootitemcondition) {
      super(alootpoolentrycontainer, alootitemcondition);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.ALTERNATIVES;
   }

   protected ComposableEntryContainer compose(ComposableEntryContainer[] acomposableentrycontainer) {
      switch (acomposableentrycontainer.length) {
         case 0:
            return ALWAYS_FALSE;
         case 1:
            return acomposableentrycontainer[0];
         case 2:
            return acomposableentrycontainer[0].or(acomposableentrycontainer[1]);
         default:
            return (lootcontext, consumer) -> {
               for(ComposableEntryContainer composableentrycontainer : acomposableentrycontainer) {
                  if (composableentrycontainer.expand(lootcontext, consumer)) {
                     return true;
                  }
               }

               return false;
            };
      }
   }

   public void validate(ValidationContext validationcontext) {
      super.validate(validationcontext);

      for(int i = 0; i < this.children.length - 1; ++i) {
         if (ArrayUtils.isEmpty((Object[])this.children[i].conditions)) {
            validationcontext.reportProblem("Unreachable entry!");
         }
      }

   }

   public static AlternativesEntry.Builder alternatives(LootPoolEntryContainer.Builder<?>... alootpoolentrycontainer_builder) {
      return new AlternativesEntry.Builder(alootpoolentrycontainer_builder);
   }

   public static <E> AlternativesEntry.Builder alternatives(Collection<E> collection, Function<E, LootPoolEntryContainer.Builder<?>> function) {
      return new AlternativesEntry.Builder(collection.stream().map(function::apply).toArray((i) -> new LootPoolEntryContainer.Builder[i]));
   }

   public static class Builder extends LootPoolEntryContainer.Builder<AlternativesEntry.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

      public Builder(LootPoolEntryContainer.Builder<?>... alootpoolentrycontainer_builder) {
         for(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder : alootpoolentrycontainer_builder) {
            this.entries.add(lootpoolentrycontainer_builder.build());
         }

      }

      protected AlternativesEntry.Builder getThis() {
         return this;
      }

      public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> lootpoolentrycontainer_builder) {
         this.entries.add(lootpoolentrycontainer_builder.build());
         return this;
      }

      public LootPoolEntryContainer build() {
         return new AlternativesEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
      }
   }
}
