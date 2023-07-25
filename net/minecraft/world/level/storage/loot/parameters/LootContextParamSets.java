package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class LootContextParamSets {
   private static final BiMap<ResourceLocation, LootContextParamSet> REGISTRY = HashBiMap.create();
   public static final LootContextParamSet EMPTY = register("empty", (lootcontextparamset_builder) -> {
   });
   public static final LootContextParamSet CHEST = register("chest", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet COMMAND = register("command", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet SELECTOR = register("selector", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet FISHING = register("fishing", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet ENTITY = register("entity", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.DAMAGE_SOURCE).optional(LootContextParams.KILLER_ENTITY).optional(LootContextParams.DIRECT_KILLER_ENTITY).optional(LootContextParams.LAST_DAMAGE_PLAYER));
   public static final LootContextParamSet ARCHAEOLOGY = register("archaeology", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet GIFT = register("gift", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet PIGLIN_BARTER = register("barter", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.THIS_ENTITY));
   public static final LootContextParamSet ADVANCEMENT_REWARD = register("advancement_reward", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
   public static final LootContextParamSet ADVANCEMENT_ENTITY = register("advancement_entity", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
   public static final LootContextParamSet ADVANCEMENT_LOCATION = register("advancement_location", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).required(LootContextParams.BLOCK_STATE));
   public static final LootContextParamSet ALL_PARAMS = register("generic", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.LAST_DAMAGE_PLAYER).required(LootContextParams.DAMAGE_SOURCE).required(LootContextParams.KILLER_ENTITY).required(LootContextParams.DIRECT_KILLER_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE).required(LootContextParams.BLOCK_ENTITY).required(LootContextParams.TOOL).required(LootContextParams.EXPLOSION_RADIUS));
   public static final LootContextParamSet BLOCK = register("block", (lootcontextparamset_builder) -> lootcontextparamset_builder.required(LootContextParams.BLOCK_STATE).required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.BLOCK_ENTITY).optional(LootContextParams.EXPLOSION_RADIUS));

   private static LootContextParamSet register(String s, Consumer<LootContextParamSet.Builder> consumer) {
      LootContextParamSet.Builder lootcontextparamset_builder = new LootContextParamSet.Builder();
      consumer.accept(lootcontextparamset_builder);
      LootContextParamSet lootcontextparamset = lootcontextparamset_builder.build();
      ResourceLocation resourcelocation = new ResourceLocation(s);
      LootContextParamSet lootcontextparamset1 = REGISTRY.put(resourcelocation, lootcontextparamset);
      if (lootcontextparamset1 != null) {
         throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
      } else {
         return lootcontextparamset;
      }
   }

   @Nullable
   public static LootContextParamSet get(ResourceLocation resourcelocation) {
      return REGISTRY.get(resourcelocation);
   }

   @Nullable
   public static ResourceLocation getKey(LootContextParamSet lootcontextparamset) {
      return REGISTRY.inverse().get(lootcontextparamset);
   }
}
