package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelProvider implements DataProvider {
   private final PackOutput.PathProvider blockStatePathProvider;
   private final PackOutput.PathProvider modelPathProvider;

   public ModelProvider(PackOutput packoutput) {
      this.blockStatePathProvider = packoutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
      this.modelPathProvider = packoutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      Map<Block, BlockStateGenerator> map = Maps.newHashMap();
      Consumer<BlockStateGenerator> consumer = (blockstategenerator) -> {
         Block block3 = blockstategenerator.getBlock();
         BlockStateGenerator blockstategenerator1 = map.put(block3, blockstategenerator);
         if (blockstategenerator1 != null) {
            throw new IllegalStateException("Duplicate blockstate definition for " + block3);
         }
      };
      Map<ResourceLocation, Supplier<JsonElement>> map1 = Maps.newHashMap();
      Set<Item> set = Sets.newHashSet();
      BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer = (resourcelocation1, supplier) -> {
         Supplier<JsonElement> supplier1 = map1.put(resourcelocation1, supplier);
         if (supplier1 != null) {
            throw new IllegalStateException("Duplicate model definition for " + resourcelocation1);
         }
      };
      Consumer<Item> consumer1 = set::add;
      (new BlockModelGenerators(consumer, biconsumer, consumer1)).run();
      (new ItemModelGenerators(biconsumer)).run();
      List<Block> list = BuiltInRegistries.BLOCK.stream().filter((block2) -> !map.containsKey(block2)).toList();
      if (!list.isEmpty()) {
         throw new IllegalStateException("Missing blockstate definitions for: " + list);
      } else {
         BuiltInRegistries.BLOCK.forEach((block1) -> {
            Item item = Item.BY_BLOCK.get(block1);
            if (item != null) {
               if (set.contains(item)) {
                  return;
               }

               ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(item);
               if (!map1.containsKey(resourcelocation)) {
                  map1.put(resourcelocation, new DelegatedModel(ModelLocationUtils.getModelLocation(block1)));
               }
            }

         });
         return CompletableFuture.allOf(this.saveCollection(cachedoutput, map, (block) -> this.blockStatePathProvider.json(block.builtInRegistryHolder().key().location())), this.saveCollection(cachedoutput, map1, this.modelPathProvider::json));
      }
   }

   private <T> CompletableFuture<?> saveCollection(CachedOutput cachedoutput, Map<T, ? extends Supplier<JsonElement>> map, Function<T, Path> function) {
      return CompletableFuture.allOf(map.entrySet().stream().map((map_entry) -> {
         Path path = function.apply(map_entry.getKey());
         JsonElement jsonelement = map_entry.getValue().get();
         return DataProvider.saveStable(cachedoutput, jsonelement, path);
      }).toArray((i) -> new CompletableFuture[i]));
   }

   public final String getName() {
      return "Model Definitions";
   }
}
