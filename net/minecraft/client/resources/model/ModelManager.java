package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public class ModelManager implements PreparableReloadListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<ResourceLocation, ResourceLocation> VANILLA_ATLASES = Map.of(Sheets.BANNER_SHEET, new ResourceLocation("banner_patterns"), Sheets.BED_SHEET, new ResourceLocation("beds"), Sheets.CHEST_SHEET, new ResourceLocation("chests"), Sheets.SHIELD_SHEET, new ResourceLocation("shield_patterns"), Sheets.SIGN_SHEET, new ResourceLocation("signs"), Sheets.SHULKER_SHEET, new ResourceLocation("shulker_boxes"), Sheets.ARMOR_TRIMS_SHEET, new ResourceLocation("armor_trims"), Sheets.DECORATED_POT_SHEET, new ResourceLocation("decorated_pot"), TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("blocks"));
   private Map<ResourceLocation, BakedModel> bakedRegistry;
   private final AtlasSet atlases;
   private final BlockModelShaper blockModelShaper;
   private final BlockColors blockColors;
   private int maxMipmapLevels;
   private BakedModel missingModel;
   private Object2IntMap<BlockState> modelGroups;

   public ModelManager(TextureManager texturemanager, BlockColors blockcolors, int i) {
      this.blockColors = blockcolors;
      this.maxMipmapLevels = i;
      this.blockModelShaper = new BlockModelShaper(this);
      this.atlases = new AtlasSet(VANILLA_ATLASES, texturemanager);
   }

   public BakedModel getModel(ModelResourceLocation modelresourcelocation) {
      return this.bakedRegistry.getOrDefault(modelresourcelocation, this.missingModel);
   }

   public BakedModel getMissingModel() {
      return this.missingModel;
   }

   public BlockModelShaper getBlockModelShaper() {
      return this.blockModelShaper;
   }

   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      profilerfiller.startTick();
      CompletableFuture<Map<ResourceLocation, BlockModel>> completablefuture = loadBlockModels(resourcemanager, executor);
      CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> completablefuture1 = loadBlockStates(resourcemanager, executor);
      CompletableFuture<ModelBakery> completablefuture2 = completablefuture.thenCombineAsync(completablefuture1, (map2, map3) -> new ModelBakery(this.blockColors, profilerfiller, map2, map3), executor);
      Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map = this.atlases.scheduleLoad(resourcemanager, this.maxMipmapLevels, executor);
      return CompletableFuture.allOf(Stream.concat(map.values().stream(), Stream.of(completablefuture2)).toArray((i) -> new CompletableFuture[i])).thenApplyAsync((ovoid1) -> this.loadModels(profilerfiller, map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (map_entry) -> map_entry.getValue().join())), completablefuture2.join()), executor).thenCompose((modelmanager_reloadstate1) -> modelmanager_reloadstate1.readyForUpload.thenApply((ovoid) -> modelmanager_reloadstate1)).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((modelmanager_reloadstate) -> this.apply(modelmanager_reloadstate, profilerfiller1), executor1);
   }

   private static CompletableFuture<Map<ResourceLocation, BlockModel>> loadBlockModels(ResourceManager resourcemanager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> ModelBakery.MODEL_LISTER.listMatchingResources(resourcemanager), executor).thenCompose((map) -> {
         List<CompletableFuture<Pair<ResourceLocation, BlockModel>>> list = new ArrayList<>(map.size());

         for(Map.Entry<ResourceLocation, Resource> map_entry : map.entrySet()) {
            list.add(CompletableFuture.supplyAsync(() -> {
               try {
                  Reader reader = map_entry.getValue().openAsReader();

                  Pair var2;
                  try {
                     var2 = Pair.of(map_entry.getKey(), BlockModel.fromStream(reader));
                  } catch (Throwable var5) {
                     if (reader != null) {
                        try {
                           reader.close();
                        } catch (Throwable var4) {
                           var5.addSuppressed(var4);
                        }
                     }

                     throw var5;
                  }

                  if (reader != null) {
                     reader.close();
                  }

                  return var2;
               } catch (Exception var6) {
                  LOGGER.error("Failed to load model {}", map_entry.getKey(), var6);
                  return null;
               }
            }, executor));
         }

         return Util.sequence(list).thenApply((list1) -> list1.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
      });
   }

   private static CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> loadBlockStates(ResourceManager resourcemanager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> ModelBakery.BLOCKSTATE_LISTER.listMatchingResourceStacks(resourcemanager), executor).thenCompose((map) -> {
         List<CompletableFuture<Pair<ResourceLocation, List<ModelBakery.LoadedJson>>>> list = new ArrayList<>(map.size());

         for(Map.Entry<ResourceLocation, List<Resource>> map_entry : map.entrySet()) {
            list.add(CompletableFuture.supplyAsync(() -> {
               List<Resource> list2 = map_entry.getValue();
               List<ModelBakery.LoadedJson> list3 = new ArrayList<>(list2.size());

               for(Resource resource : list2) {
                  try {
                     Reader reader = resource.openAsReader();

                     try {
                        JsonObject jsonobject = GsonHelper.parse(reader);
                        list3.add(new ModelBakery.LoadedJson(resource.sourcePackId(), jsonobject));
                     } catch (Throwable var9) {
                        if (reader != null) {
                           try {
                              reader.close();
                           } catch (Throwable var8) {
                              var9.addSuppressed(var8);
                           }
                        }

                        throw var9;
                     }

                     if (reader != null) {
                        reader.close();
                     }
                  } catch (Exception var10) {
                     LOGGER.error("Failed to load blockstate {} from pack {}", map_entry.getKey(), resource.sourcePackId(), var10);
                  }
               }

               return Pair.of(map_entry.getKey(), list3);
            }, executor));
         }

         return Util.sequence(list).thenApply((list1) -> list1.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
      });
   }

   private ModelManager.ReloadState loadModels(ProfilerFiller profilerfiller, Map<ResourceLocation, AtlasSet.StitchResult> map, ModelBakery modelbakery) {
      profilerfiller.push("load");
      profilerfiller.popPush("baking");
      Multimap<ResourceLocation, Material> multimap = HashMultimap.create();
      modelbakery.bakeModels((resourcelocation2, material1) -> {
         AtlasSet.StitchResult atlasset_stitchresult = map.get(material1.atlasLocation());
         TextureAtlasSprite textureatlassprite = atlasset_stitchresult.getSprite(material1.texture());
         if (textureatlassprite != null) {
            return textureatlassprite;
         } else {
            multimap.put(resourcelocation2, material1);
            return atlasset_stitchresult.missing();
         }
      });
      multimap.asMap().forEach((resourcelocation1, collection) -> LOGGER.warn("Missing textures in model {}:\n{}", resourcelocation1, collection.stream().sorted(Material.COMPARATOR).map((material) -> "    " + material.atlasLocation() + ":" + material.texture()).collect(Collectors.joining("\n"))));
      profilerfiller.popPush("dispatch");
      Map<ResourceLocation, BakedModel> map1 = modelbakery.getBakedTopLevelModels();
      BakedModel bakedmodel = map1.get(ModelBakery.MISSING_MODEL_LOCATION);
      Map<BlockState, BakedModel> map2 = new IdentityHashMap<>();

      for(Block block : BuiltInRegistries.BLOCK) {
         block.getStateDefinition().getPossibleStates().forEach((blockstate) -> {
            ResourceLocation resourcelocation = blockstate.getBlock().builtInRegistryHolder().key().location();
            BakedModel bakedmodel2 = map1.getOrDefault(BlockModelShaper.stateToModelLocation(resourcelocation, blockstate), bakedmodel);
            map2.put(blockstate, bakedmodel2);
         });
      }

      CompletableFuture<Void> completablefuture = CompletableFuture.allOf(map.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray((i) -> new CompletableFuture[i]));
      profilerfiller.pop();
      profilerfiller.endTick();
      return new ModelManager.ReloadState(modelbakery, bakedmodel, map2, map, completablefuture);
   }

   private void apply(ModelManager.ReloadState modelmanager_reloadstate, ProfilerFiller profilerfiller) {
      profilerfiller.startTick();
      profilerfiller.push("upload");
      modelmanager_reloadstate.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
      ModelBakery modelbakery = modelmanager_reloadstate.modelBakery;
      this.bakedRegistry = modelbakery.getBakedTopLevelModels();
      this.modelGroups = modelbakery.getModelGroups();
      this.missingModel = modelmanager_reloadstate.missingModel;
      profilerfiller.popPush("cache");
      this.blockModelShaper.replaceCache(modelmanager_reloadstate.modelCache);
      profilerfiller.pop();
      profilerfiller.endTick();
   }

   public boolean requiresRender(BlockState blockstate, BlockState blockstate1) {
      if (blockstate == blockstate1) {
         return false;
      } else {
         int i = this.modelGroups.getInt(blockstate);
         if (i != -1) {
            int j = this.modelGroups.getInt(blockstate1);
            if (i == j) {
               FluidState fluidstate = blockstate.getFluidState();
               FluidState fluidstate1 = blockstate1.getFluidState();
               return fluidstate != fluidstate1;
            }
         }

         return true;
      }
   }

   public TextureAtlas getAtlas(ResourceLocation resourcelocation) {
      return this.atlases.getAtlas(resourcelocation);
   }

   public void close() {
      this.atlases.close();
   }

   public void updateMaxMipLevel(int i) {
      this.maxMipmapLevels = i;
   }

   static record ReloadState(ModelBakery modelBakery, BakedModel missingModel, Map<BlockState, BakedModel> modelCache, Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations, CompletableFuture<Void> readyForUpload) {
      final ModelBakery modelBakery;
      final BakedModel missingModel;
      final Map<BlockState, BakedModel> modelCache;
      final Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations;
      final CompletableFuture<Void> readyForUpload;
   }
}
