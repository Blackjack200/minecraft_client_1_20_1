package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext {
   private final LootParams params;
   private final RandomSource random;
   private final LootDataResolver lootDataResolver;
   private final Set<LootContext.VisitedEntry<?>> visitedElements = Sets.newLinkedHashSet();

   LootContext(LootParams lootparams, RandomSource randomsource, LootDataResolver lootdataresolver) {
      this.params = lootparams;
      this.random = randomsource;
      this.lootDataResolver = lootdataresolver;
   }

   public boolean hasParam(LootContextParam<?> lootcontextparam) {
      return this.params.hasParam(lootcontextparam);
   }

   public <T> T getParam(LootContextParam<T> lootcontextparam) {
      return this.params.getParameter(lootcontextparam);
   }

   public void addDynamicDrops(ResourceLocation resourcelocation, Consumer<ItemStack> consumer) {
      this.params.addDynamicDrops(resourcelocation, consumer);
   }

   @Nullable
   public <T> T getParamOrNull(LootContextParam<T> lootcontextparam) {
      return this.params.getParamOrNull(lootcontextparam);
   }

   public boolean hasVisitedElement(LootContext.VisitedEntry<?> lootcontext_visitedentry) {
      return this.visitedElements.contains(lootcontext_visitedentry);
   }

   public boolean pushVisitedElement(LootContext.VisitedEntry<?> lootcontext_visitedentry) {
      return this.visitedElements.add(lootcontext_visitedentry);
   }

   public void popVisitedElement(LootContext.VisitedEntry<?> lootcontext_visitedentry) {
      this.visitedElements.remove(lootcontext_visitedentry);
   }

   public LootDataResolver getResolver() {
      return this.lootDataResolver;
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public float getLuck() {
      return this.params.getLuck();
   }

   public ServerLevel getLevel() {
      return this.params.getLevel();
   }

   public static LootContext.VisitedEntry<LootTable> createVisitedEntry(LootTable loottable) {
      return new LootContext.VisitedEntry<>(LootDataType.TABLE, loottable);
   }

   public static LootContext.VisitedEntry<LootItemCondition> createVisitedEntry(LootItemCondition lootitemcondition) {
      return new LootContext.VisitedEntry<>(LootDataType.PREDICATE, lootitemcondition);
   }

   public static LootContext.VisitedEntry<LootItemFunction> createVisitedEntry(LootItemFunction lootitemfunction) {
      return new LootContext.VisitedEntry<>(LootDataType.MODIFIER, lootitemfunction);
   }

   public static class Builder {
      private final LootParams params;
      @Nullable
      private RandomSource random;

      public Builder(LootParams lootparams) {
         this.params = lootparams;
      }

      public LootContext.Builder withOptionalRandomSeed(long i) {
         if (i != 0L) {
            this.random = RandomSource.create(i);
         }

         return this;
      }

      public ServerLevel getLevel() {
         return this.params.getLevel();
      }

      public LootContext create(@Nullable ResourceLocation resourcelocation) {
         ServerLevel serverlevel = this.getLevel();
         MinecraftServer minecraftserver = serverlevel.getServer();
         RandomSource randomsource;
         if (this.random != null) {
            randomsource = this.random;
         } else if (resourcelocation != null) {
            randomsource = serverlevel.getRandomSequence(resourcelocation);
         } else {
            randomsource = serverlevel.getRandom();
         }

         return new LootContext(this.params, randomsource, minecraftserver.getLootData());
      }
   }

   public static enum EntityTarget {
      THIS("this", LootContextParams.THIS_ENTITY),
      KILLER("killer", LootContextParams.KILLER_ENTITY),
      DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

      final String name;
      private final LootContextParam<? extends Entity> param;

      private EntityTarget(String s, LootContextParam<? extends Entity> lootcontextparam) {
         this.name = s;
         this.param = lootcontextparam;
      }

      public LootContextParam<? extends Entity> getParam() {
         return this.param;
      }

      public static LootContext.EntityTarget getByName(String s) {
         for(LootContext.EntityTarget lootcontext_entitytarget : values()) {
            if (lootcontext_entitytarget.name.equals(s)) {
               return lootcontext_entitytarget;
            }
         }

         throw new IllegalArgumentException("Invalid entity target " + s);
      }

      public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
         public void write(JsonWriter jsonwriter, LootContext.EntityTarget lootcontext_entitytarget) throws IOException {
            jsonwriter.value(lootcontext_entitytarget.name);
         }

         public LootContext.EntityTarget read(JsonReader jsonreader) throws IOException {
            return LootContext.EntityTarget.getByName(jsonreader.nextString());
         }
      }
   }

   public static record VisitedEntry<T>(LootDataType<T> type, T value) {
   }
}
