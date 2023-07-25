package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class LootParams {
   private final ServerLevel level;
   private final Map<LootContextParam<?>, Object> params;
   private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops;
   private final float luck;

   public LootParams(ServerLevel serverlevel, Map<LootContextParam<?>, Object> map, Map<ResourceLocation, LootParams.DynamicDrop> map1, float f) {
      this.level = serverlevel;
      this.params = map;
      this.dynamicDrops = map1;
      this.luck = f;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public boolean hasParam(LootContextParam<?> lootcontextparam) {
      return this.params.containsKey(lootcontextparam);
   }

   public <T> T getParameter(LootContextParam<T> lootcontextparam) {
      T object = (T)this.params.get(lootcontextparam);
      if (object == null) {
         throw new NoSuchElementException(lootcontextparam.getName().toString());
      } else {
         return object;
      }
   }

   @Nullable
   public <T> T getOptionalParameter(LootContextParam<T> lootcontextparam) {
      return (T)this.params.get(lootcontextparam);
   }

   @Nullable
   public <T> T getParamOrNull(LootContextParam<T> lootcontextparam) {
      return (T)this.params.get(lootcontextparam);
   }

   public void addDynamicDrops(ResourceLocation resourcelocation, Consumer<ItemStack> consumer) {
      LootParams.DynamicDrop lootparams_dynamicdrop = this.dynamicDrops.get(resourcelocation);
      if (lootparams_dynamicdrop != null) {
         lootparams_dynamicdrop.add(consumer);
      }

   }

   public float getLuck() {
      return this.luck;
   }

   public static class Builder {
      private final ServerLevel level;
      private final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
      private final Map<ResourceLocation, LootParams.DynamicDrop> dynamicDrops = Maps.newHashMap();
      private float luck;

      public Builder(ServerLevel serverlevel) {
         this.level = serverlevel;
      }

      public ServerLevel getLevel() {
         return this.level;
      }

      public <T> LootParams.Builder withParameter(LootContextParam<T> lootcontextparam, T object) {
         this.params.put(lootcontextparam, object);
         return this;
      }

      public <T> LootParams.Builder withOptionalParameter(LootContextParam<T> lootcontextparam, @Nullable T object) {
         if (object == null) {
            this.params.remove(lootcontextparam);
         } else {
            this.params.put(lootcontextparam, object);
         }

         return this;
      }

      public <T> T getParameter(LootContextParam<T> lootcontextparam) {
         T object = (T)this.params.get(lootcontextparam);
         if (object == null) {
            throw new NoSuchElementException(lootcontextparam.getName().toString());
         } else {
            return object;
         }
      }

      @Nullable
      public <T> T getOptionalParameter(LootContextParam<T> lootcontextparam) {
         return (T)this.params.get(lootcontextparam);
      }

      public LootParams.Builder withDynamicDrop(ResourceLocation resourcelocation, LootParams.DynamicDrop lootparams_dynamicdrop) {
         LootParams.DynamicDrop lootparams_dynamicdrop1 = this.dynamicDrops.put(resourcelocation, lootparams_dynamicdrop);
         if (lootparams_dynamicdrop1 != null) {
            throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
         } else {
            return this;
         }
      }

      public LootParams.Builder withLuck(float f) {
         this.luck = f;
         return this;
      }

      public LootParams create(LootContextParamSet lootcontextparamset) {
         Set<LootContextParam<?>> set = Sets.difference(this.params.keySet(), lootcontextparamset.getAllowed());
         if (!set.isEmpty()) {
            throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
         } else {
            Set<LootContextParam<?>> set1 = Sets.difference(lootcontextparamset.getRequired(), this.params.keySet());
            if (!set1.isEmpty()) {
               throw new IllegalArgumentException("Missing required parameters: " + set1);
            } else {
               return new LootParams(this.level, this.params, this.dynamicDrops, this.luck);
            }
         }
      }
   }

   @FunctionalInterface
   public interface DynamicDrop {
      void add(Consumer<ItemStack> consumer);
   }
}
