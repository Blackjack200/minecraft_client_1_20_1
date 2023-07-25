package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataManager implements PreparableReloadListener, LootDataResolver {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataId<LootTable> EMPTY_LOOT_TABLE_KEY = new LootDataId<>(LootDataType.TABLE, BuiltInLootTables.EMPTY);
   private Map<LootDataId<?>, ?> elements = Map.of();
   private Multimap<LootDataType<?>, ResourceLocation> typeKeys = ImmutableMultimap.of();

   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      Map<LootDataType<?>, Map<ResourceLocation, ?>> map = new HashMap<>();
      CompletableFuture<?>[] acompletablefuture = LootDataType.values().map((lootdatatype) -> scheduleElementParse(lootdatatype, resourcemanager, executor, map)).toArray((i) -> new CompletableFuture[i]);
      return CompletableFuture.allOf(acompletablefuture).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((ovoid) -> this.apply(map), executor1);
   }

   private static <T> CompletableFuture<?> scheduleElementParse(LootDataType<T> lootdatatype, ResourceManager resourcemanager, Executor executor, Map<LootDataType<?>, Map<ResourceLocation, ?>> map) {
      Map<ResourceLocation, T> map1 = new HashMap<>();
      map.put(lootdatatype, map1);
      return CompletableFuture.runAsync(() -> {
         Map<ResourceLocation, JsonElement> map3 = new HashMap<>();
         SimpleJsonResourceReloadListener.scanDirectory(resourcemanager, lootdatatype.directory(), lootdatatype.parser(), map3);
         map3.forEach((resourcelocation, jsonelement) -> lootdatatype.deserialize(resourcelocation, jsonelement).ifPresent((object) -> map1.put(resourcelocation, object)));
      }, executor);
   }

   private void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> map) {
      Object object = map.get(LootDataType.TABLE).remove(BuiltInLootTables.EMPTY);
      if (object != null) {
         LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)BuiltInLootTables.EMPTY);
      }

      ImmutableMap.Builder<LootDataId<?>, Object> immutablemap_builder = ImmutableMap.builder();
      ImmutableMultimap.Builder<LootDataType<?>, ResourceLocation> immutablemultimap_builder = ImmutableMultimap.builder();
      map.forEach((lootdatatype, map2) -> map2.forEach((resourcelocation, object2) -> {
            immutablemap_builder.put(new LootDataId(lootdatatype, resourcelocation), object2);
            immutablemultimap_builder.put(lootdatatype, resourcelocation);
         }));
      immutablemap_builder.put(EMPTY_LOOT_TABLE_KEY, LootTable.EMPTY);
      final Map<LootDataId<?>, ?> map1 = immutablemap_builder.build();
      ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
         @Nullable
         public <T> T getElement(LootDataId<T> lootdataid) {
            return (T)map1.get(lootdataid);
         }
      });
      map1.forEach((lootdataid, object1) -> castAndValidate(validationcontext, lootdataid, object1));
      validationcontext.getProblems().forEach((s, s1) -> LOGGER.warn("Found loot table element validation problem in {}: {}", s, s1));
      this.elements = map1;
      this.typeKeys = immutablemultimap_builder.build();
   }

   private static <T> void castAndValidate(ValidationContext validationcontext, LootDataId<T> lootdataid, Object object) {
      lootdataid.type().runValidation(validationcontext, lootdataid, (T)object);
   }

   @Nullable
   public <T> T getElement(LootDataId<T> lootdataid) {
      return (T)this.elements.get(lootdataid);
   }

   public Collection<ResourceLocation> getKeys(LootDataType<?> lootdatatype) {
      return this.typeKeys.get(lootdatatype);
   }

   public static LootItemCondition createComposite(LootItemCondition[] alootitemcondition) {
      return new LootDataManager.CompositePredicate(alootitemcondition);
   }

   public static LootItemFunction createComposite(LootItemFunction[] alootitemfunction) {
      return new LootDataManager.FunctionSequence(alootitemfunction);
   }

   static class CompositePredicate implements LootItemCondition {
      private final LootItemCondition[] terms;
      private final Predicate<LootContext> composedPredicate;

      CompositePredicate(LootItemCondition[] alootitemcondition) {
         this.terms = alootitemcondition;
         this.composedPredicate = LootItemConditions.andConditions(alootitemcondition);
      }

      public final boolean test(LootContext lootcontext) {
         return this.composedPredicate.test(lootcontext);
      }

      public void validate(ValidationContext validationcontext) {
         LootItemCondition.super.validate(validationcontext);

         for(int i = 0; i < this.terms.length; ++i) {
            this.terms[i].validate(validationcontext.forChild(".term[" + i + "]"));
         }

      }

      public LootItemConditionType getType() {
         throw new UnsupportedOperationException();
      }
   }

   static class FunctionSequence implements LootItemFunction {
      protected final LootItemFunction[] functions;
      private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

      public FunctionSequence(LootItemFunction[] alootitemfunction) {
         this.functions = alootitemfunction;
         this.compositeFunction = LootItemFunctions.compose(alootitemfunction);
      }

      public ItemStack apply(ItemStack itemstack, LootContext lootcontext) {
         return this.compositeFunction.apply(itemstack, lootcontext);
      }

      public void validate(ValidationContext validationcontext) {
         LootItemFunction.super.validate(validationcontext);

         for(int i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(validationcontext.forChild(".function[" + i + "]"));
         }

      }

      public LootItemFunctionType getType() {
         throw new UnsupportedOperationException();
      }
   }
}
