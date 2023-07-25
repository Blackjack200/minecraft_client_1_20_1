package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class LootTable {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, (ResourceLocation)null, new LootPool[0], new LootItemFunction[0]);
   public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
   final LootContextParamSet paramSet;
   @Nullable
   final ResourceLocation randomSequence;
   final LootPool[] pools;
   final LootItemFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

   LootTable(LootContextParamSet lootcontextparamset, @Nullable ResourceLocation resourcelocation, LootPool[] alootpool, LootItemFunction[] alootitemfunction) {
      this.paramSet = lootcontextparamset;
      this.randomSequence = resourcelocation;
      this.pools = alootpool;
      this.functions = alootitemfunction;
      this.compositeFunction = LootItemFunctions.compose(alootitemfunction);
   }

   public static Consumer<ItemStack> createStackSplitter(ServerLevel serverlevel, Consumer<ItemStack> consumer) {
      return (itemstack) -> {
         if (itemstack.isItemEnabled(serverlevel.enabledFeatures())) {
            if (itemstack.getCount() < itemstack.getMaxStackSize()) {
               consumer.accept(itemstack);
            } else {
               int i = itemstack.getCount();

               while(i > 0) {
                  ItemStack itemstack1 = itemstack.copyWithCount(Math.min(itemstack.getMaxStackSize(), i));
                  i -= itemstack1.getCount();
                  consumer.accept(itemstack1);
               }
            }

         }
      };
   }

   public void getRandomItemsRaw(LootParams lootparams, Consumer<ItemStack> consumer) {
      this.getRandomItemsRaw((new LootContext.Builder(lootparams)).create(this.randomSequence), consumer);
   }

   public void getRandomItemsRaw(LootContext lootcontext, Consumer<ItemStack> consumer) {
      LootContext.VisitedEntry<?> lootcontext_visitedentry = LootContext.createVisitedEntry(this);
      if (lootcontext.pushVisitedElement(lootcontext_visitedentry)) {
         Consumer<ItemStack> consumer1 = LootItemFunction.decorate(this.compositeFunction, consumer, lootcontext);

         for(LootPool lootpool : this.pools) {
            lootpool.addRandomItems(consumer1, lootcontext);
         }

         lootcontext.popVisitedElement(lootcontext_visitedentry);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }

   }

   public void getRandomItems(LootParams lootparams, long i, Consumer<ItemStack> consumer) {
      this.getRandomItemsRaw((new LootContext.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence), createStackSplitter(lootparams.getLevel(), consumer));
   }

   public void getRandomItems(LootParams lootparams, Consumer<ItemStack> consumer) {
      this.getRandomItemsRaw(lootparams, createStackSplitter(lootparams.getLevel(), consumer));
   }

   public void getRandomItems(LootContext lootcontext, Consumer<ItemStack> consumer) {
      this.getRandomItemsRaw(lootcontext, createStackSplitter(lootcontext.getLevel(), consumer));
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams lootparams, long i) {
      return this.getRandomItems((new LootContext.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence));
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams lootparams) {
      return this.getRandomItems((new LootContext.Builder(lootparams)).create(this.randomSequence));
   }

   private ObjectArrayList<ItemStack> getRandomItems(LootContext lootcontext) {
      ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
      this.getRandomItems(lootcontext, objectarraylist::add);
      return objectarraylist;
   }

   public LootContextParamSet getParamSet() {
      return this.paramSet;
   }

   public void validate(ValidationContext validationcontext) {
      for(int i = 0; i < this.pools.length; ++i) {
         this.pools[i].validate(validationcontext.forChild(".pools[" + i + "]"));
      }

      for(int j = 0; j < this.functions.length; ++j) {
         this.functions[j].validate(validationcontext.forChild(".functions[" + j + "]"));
      }

   }

   public void fill(Container container, LootParams lootparams, long i) {
      LootContext lootcontext = (new LootContext.Builder(lootparams)).withOptionalRandomSeed(i).create(this.randomSequence);
      ObjectArrayList<ItemStack> objectarraylist = this.getRandomItems(lootcontext);
      RandomSource randomsource = lootcontext.getRandom();
      List<Integer> list = this.getAvailableSlots(container, randomsource);
      this.shuffleAndSplitItems(objectarraylist, list.size(), randomsource);

      for(ItemStack itemstack : objectarraylist) {
         if (list.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if (itemstack.isEmpty()) {
            container.setItem(list.remove(list.size() - 1), ItemStack.EMPTY);
         } else {
            container.setItem(list.remove(list.size() - 1), itemstack);
         }
      }

   }

   private void shuffleAndSplitItems(ObjectArrayList<ItemStack> objectarraylist, int i, RandomSource randomsource) {
      List<ItemStack> list = Lists.newArrayList();
      Iterator<ItemStack> iterator = objectarraylist.iterator();

      while(iterator.hasNext()) {
         ItemStack itemstack = iterator.next();
         if (itemstack.isEmpty()) {
            iterator.remove();
         } else if (itemstack.getCount() > 1) {
            list.add(itemstack);
            iterator.remove();
         }
      }

      while(i - objectarraylist.size() - list.size() > 0 && !list.isEmpty()) {
         ItemStack itemstack1 = list.remove(Mth.nextInt(randomsource, 0, list.size() - 1));
         int j = Mth.nextInt(randomsource, 1, itemstack1.getCount() / 2);
         ItemStack itemstack2 = itemstack1.split(j);
         if (itemstack1.getCount() > 1 && randomsource.nextBoolean()) {
            list.add(itemstack1);
         } else {
            objectarraylist.add(itemstack1);
         }

         if (itemstack2.getCount() > 1 && randomsource.nextBoolean()) {
            list.add(itemstack2);
         } else {
            objectarraylist.add(itemstack2);
         }
      }

      objectarraylist.addAll(list);
      Util.shuffle(objectarraylist, randomsource);
   }

   private List<Integer> getAvailableSlots(Container container, RandomSource randomsource) {
      ObjectArrayList<Integer> objectarraylist = new ObjectArrayList<>();

      for(int i = 0; i < container.getContainerSize(); ++i) {
         if (container.getItem(i).isEmpty()) {
            objectarraylist.add(i);
         }
      }

      Util.shuffle(objectarraylist, randomsource);
      return objectarraylist;
   }

   public static LootTable.Builder lootTable() {
      return new LootTable.Builder();
   }

   public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
      private final List<LootPool> pools = Lists.newArrayList();
      private final List<LootItemFunction> functions = Lists.newArrayList();
      private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;
      @Nullable
      private ResourceLocation randomSequence = null;

      public LootTable.Builder withPool(LootPool.Builder lootpool_builder) {
         this.pools.add(lootpool_builder.build());
         return this;
      }

      public LootTable.Builder setParamSet(LootContextParamSet lootcontextparamset) {
         this.paramSet = lootcontextparamset;
         return this;
      }

      public LootTable.Builder setRandomSequence(ResourceLocation resourcelocation) {
         this.randomSequence = resourcelocation;
         return this;
      }

      public LootTable.Builder apply(LootItemFunction.Builder lootitemfunction_builder) {
         this.functions.add(lootitemfunction_builder.build());
         return this;
      }

      public LootTable.Builder unwrap() {
         return this;
      }

      public LootTable build() {
         return new LootTable(this.paramSet, this.randomSequence, this.pools.toArray(new LootPool[0]), this.functions.toArray(new LootItemFunction[0]));
      }
   }

   public static class Serializer implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {
      public LootTable deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "loot table");
         LootPool[] alootpool = GsonHelper.getAsObject(jsonobject, "pools", new LootPool[0], jsondeserializationcontext, LootPool[].class);
         LootContextParamSet lootcontextparamset = null;
         if (jsonobject.has("type")) {
            String s = GsonHelper.getAsString(jsonobject, "type");
            lootcontextparamset = LootContextParamSets.get(new ResourceLocation(s));
         }

         ResourceLocation resourcelocation;
         if (jsonobject.has("random_sequence")) {
            String s1 = GsonHelper.getAsString(jsonobject, "random_sequence");
            resourcelocation = new ResourceLocation(s1);
         } else {
            resourcelocation = null;
         }

         LootItemFunction[] alootitemfunction = GsonHelper.getAsObject(jsonobject, "functions", new LootItemFunction[0], jsondeserializationcontext, LootItemFunction[].class);
         return new LootTable(lootcontextparamset != null ? lootcontextparamset : LootContextParamSets.ALL_PARAMS, resourcelocation, alootpool, alootitemfunction);
      }

      public JsonElement serialize(LootTable loottable, Type type, JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject = new JsonObject();
         if (loottable.paramSet != LootTable.DEFAULT_PARAM_SET) {
            ResourceLocation resourcelocation = LootContextParamSets.getKey(loottable.paramSet);
            if (resourcelocation != null) {
               jsonobject.addProperty("type", resourcelocation.toString());
            } else {
               LootTable.LOGGER.warn("Failed to find id for param set {}", (Object)loottable.paramSet);
            }
         }

         if (loottable.randomSequence != null) {
            jsonobject.addProperty("random_sequence", loottable.randomSequence.toString());
         }

         if (loottable.pools.length > 0) {
            jsonobject.add("pools", jsonserializationcontext.serialize(loottable.pools));
         }

         if (!ArrayUtils.isEmpty((Object[])loottable.functions)) {
            jsonobject.add("functions", jsonserializationcontext.serialize(loottable.functions));
         }

         return jsonobject;
      }
   }
}
