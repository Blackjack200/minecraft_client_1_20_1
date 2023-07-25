package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class LootContextParamSet {
   private final Set<LootContextParam<?>> required;
   private final Set<LootContextParam<?>> all;

   LootContextParamSet(Set<LootContextParam<?>> set, Set<LootContextParam<?>> set1) {
      this.required = ImmutableSet.copyOf(set);
      this.all = ImmutableSet.copyOf(Sets.union(set, set1));
   }

   public boolean isAllowed(LootContextParam<?> lootcontextparam) {
      return this.all.contains(lootcontextparam);
   }

   public Set<LootContextParam<?>> getRequired() {
      return this.required;
   }

   public Set<LootContextParam<?>> getAllowed() {
      return this.all;
   }

   public String toString() {
      return "[" + Joiner.on(", ").join(this.all.stream().map((lootcontextparam) -> (this.required.contains(lootcontextparam) ? "!" : "") + lootcontextparam.getName()).iterator()) + "]";
   }

   public void validateUser(ValidationContext validationcontext, LootContextUser lootcontextuser) {
      Set<LootContextParam<?>> set = lootcontextuser.getReferencedContextParams();
      Set<LootContextParam<?>> set1 = Sets.difference(set, this.all);
      if (!set1.isEmpty()) {
         validationcontext.reportProblem("Parameters " + set1 + " are not provided in this context");
      }

   }

   public static LootContextParamSet.Builder builder() {
      return new LootContextParamSet.Builder();
   }

   public static class Builder {
      private final Set<LootContextParam<?>> required = Sets.newIdentityHashSet();
      private final Set<LootContextParam<?>> optional = Sets.newIdentityHashSet();

      public LootContextParamSet.Builder required(LootContextParam<?> lootcontextparam) {
         if (this.optional.contains(lootcontextparam)) {
            throw new IllegalArgumentException("Parameter " + lootcontextparam.getName() + " is already optional");
         } else {
            this.required.add(lootcontextparam);
            return this;
         }
      }

      public LootContextParamSet.Builder optional(LootContextParam<?> lootcontextparam) {
         if (this.required.contains(lootcontextparam)) {
            throw new IllegalArgumentException("Parameter " + lootcontextparam.getName() + " is already required");
         } else {
            this.optional.add(lootcontextparam);
            return this;
         }
      }

      public LootContextParamSet build() {
         return new LootContextParamSet(this.required, this.optional);
      }
   }
}
