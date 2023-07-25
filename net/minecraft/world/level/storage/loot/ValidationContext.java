package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
   private final Multimap<String, String> problems;
   private final Supplier<String> context;
   private final LootContextParamSet params;
   private final LootDataResolver resolver;
   private final Set<LootDataId<?>> visitedElements;
   @Nullable
   private String contextCache;

   public ValidationContext(LootContextParamSet lootcontextparamset, LootDataResolver lootdataresolver) {
      this(HashMultimap.create(), () -> "", lootcontextparamset, lootdataresolver, ImmutableSet.of());
   }

   public ValidationContext(Multimap<String, String> multimap, Supplier<String> supplier, LootContextParamSet lootcontextparamset, LootDataResolver lootdataresolver, Set<LootDataId<?>> set) {
      this.problems = multimap;
      this.context = supplier;
      this.params = lootcontextparamset;
      this.resolver = lootdataresolver;
      this.visitedElements = set;
   }

   private String getContext() {
      if (this.contextCache == null) {
         this.contextCache = this.context.get();
      }

      return this.contextCache;
   }

   public void reportProblem(String s) {
      this.problems.put(this.getContext(), s);
   }

   public ValidationContext forChild(String s) {
      return new ValidationContext(this.problems, () -> this.getContext() + s, this.params, this.resolver, this.visitedElements);
   }

   public ValidationContext enterElement(String s, LootDataId<?> lootdataid) {
      ImmutableSet<LootDataId<?>> immutableset = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(lootdataid).build();
      return new ValidationContext(this.problems, () -> this.getContext() + s, this.params, this.resolver, immutableset);
   }

   public boolean hasVisitedElement(LootDataId<?> lootdataid) {
      return this.visitedElements.contains(lootdataid);
   }

   public Multimap<String, String> getProblems() {
      return ImmutableMultimap.copyOf(this.problems);
   }

   public void validateUser(LootContextUser lootcontextuser) {
      this.params.validateUser(this, lootcontextuser);
   }

   public LootDataResolver resolver() {
      return this.resolver;
   }

   public ValidationContext setParams(LootContextParamSet lootcontextparamset) {
      return new ValidationContext(this.problems, this.context, lootcontextparamset, this.resolver, this.visitedElements);
   }
}
