package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction extends LootItemConditionalFunction {
   final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;
   final boolean append;

   SetBannerPatternFunction(LootItemCondition[] alootitemcondition, List<Pair<Holder<BannerPattern>, DyeColor>> list, boolean flag) {
      super(alootitemcondition);
      this.patterns = list;
      this.append = flag;
   }

   protected ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag == null) {
         compoundtag = new CompoundTag();
      }

      BannerPattern.Builder bannerpattern_builder = new BannerPattern.Builder();
      this.patterns.forEach(bannerpattern_builder::addPattern);
      ListTag listtag = bannerpattern_builder.toListTag();
      ListTag listtag1;
      if (this.append) {
         listtag1 = compoundtag.getList("Patterns", 10).copy();
         listtag1.addAll(listtag);
      } else {
         listtag1 = listtag;
      }

      compoundtag.put("Patterns", listtag1);
      BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, compoundtag);
      return itemstack;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_BANNER_PATTERN;
   }

   public static SetBannerPatternFunction.Builder setBannerPattern(boolean flag) {
      return new SetBannerPatternFunction.Builder(flag);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetBannerPatternFunction.Builder> {
      private final ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> patterns = ImmutableList.builder();
      private final boolean append;

      Builder(boolean flag) {
         this.append = flag;
      }

      protected SetBannerPatternFunction.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
      }

      public SetBannerPatternFunction.Builder addPattern(ResourceKey<BannerPattern> resourcekey, DyeColor dyecolor) {
         return this.addPattern(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(resourcekey), dyecolor);
      }

      public SetBannerPatternFunction.Builder addPattern(Holder<BannerPattern> holder, DyeColor dyecolor) {
         this.patterns.add(Pair.of(holder, dyecolor));
         return this;
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetBannerPatternFunction> {
      public void serialize(JsonObject jsonobject, SetBannerPatternFunction setbannerpatternfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setbannerpatternfunction, jsonserializationcontext);
         JsonArray jsonarray = new JsonArray();
         setbannerpatternfunction.patterns.forEach((pair) -> {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("pattern", pair.getFirst().unwrapKey().orElseThrow(() -> new JsonSyntaxException("Unknown pattern: " + pair.getFirst())).location().toString());
            jsonobject1.addProperty("color", pair.getSecond().getName());
            jsonarray.add(jsonobject1);
         });
         jsonobject.add("patterns", jsonarray);
         jsonobject.addProperty("append", setbannerpatternfunction.append);
      }

      public SetBannerPatternFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         ImmutableList.Builder<Pair<Holder<BannerPattern>, DyeColor>> immutablelist_builder = ImmutableList.builder();
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "patterns");

         for(int i = 0; i < jsonarray.size(); ++i) {
            JsonObject jsonobject1 = GsonHelper.convertToJsonObject(jsonarray.get(i), "pattern[" + i + "]");
            String s = GsonHelper.getAsString(jsonobject1, "pattern");
            Optional<? extends Holder<BannerPattern>> optional = BuiltInRegistries.BANNER_PATTERN.getHolder(ResourceKey.create(Registries.BANNER_PATTERN, new ResourceLocation(s)));
            if (optional.isEmpty()) {
               throw new JsonSyntaxException("Unknown pattern: " + s);
            }

            String s1 = GsonHelper.getAsString(jsonobject1, "color");
            DyeColor dyecolor = DyeColor.byName(s1, (DyeColor)null);
            if (dyecolor == null) {
               throw new JsonSyntaxException("Unknown color: " + s1);
            }

            immutablelist_builder.add(Pair.of(optional.get(), dyecolor));
         }

         boolean flag = GsonHelper.getAsBoolean(jsonobject, "append");
         return new SetBannerPatternFunction(alootitemcondition, immutablelist_builder.build(), flag);
      }
   }
}
