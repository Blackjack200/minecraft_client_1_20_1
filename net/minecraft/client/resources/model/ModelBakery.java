package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

public class ModelBakery {
   public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_0"));
   public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_1"));
   public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/lava_flow"));
   public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_flow"));
   public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_overlay"));
   public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, new ResourceLocation("entity/banner_base"));
   public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base"));
   public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base_nopattern"));
   public static final int DESTROY_STAGE_COUNT = 10;
   public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10).mapToObj((i) -> new ResourceLocation("block/destroy_stage_" + i)).collect(Collectors.toList());
   public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream().map((resourcelocation) -> new ResourceLocation("textures/" + resourcelocation.getPath() + ".png")).collect(Collectors.toList());
   public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
   static final int SINGLETON_MODEL_GROUP = -1;
   private static final int INVISIBLE_MODEL_GROUP = 0;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String BUILTIN_SLASH = "builtin/";
   private static final String BUILTIN_SLASH_GENERATED = "builtin/generated";
   private static final String BUILTIN_BLOCK_ENTITY = "builtin/entity";
   private static final String MISSING_MODEL_NAME = "missing";
   public static final ModelResourceLocation MISSING_MODEL_LOCATION = ModelResourceLocation.vanilla("builtin/missing", "missing");
   public static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
   public static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
   @VisibleForTesting
   public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '" + MissingTextureAtlasSprite.getLocation().getPath() + "',       'missingno': '" + MissingTextureAtlasSprite.getLocation().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '"');
   private static final Map<String, String> BUILTIN_MODELS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_MODEL_MESH));
   private static final Splitter COMMA_SPLITTER = Splitter.on(',');
   private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
   public static final BlockModel GENERATION_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"front\"}"), (blockmodel) -> blockmodel.name = "generation marker");
   public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"side\"}"), (blockmodel) -> blockmodel.name = "block entity marker");
   private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = (new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)).add(BooleanProperty.create("map")).create(Block::defaultBlockState, BlockState::new);
   static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
   private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION, new ResourceLocation("glow_item_frame"), ITEM_FRAME_FAKE_DEFINITION);
   private final BlockColors blockColors;
   private final Map<ResourceLocation, BlockModel> modelResources;
   private final Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStateResources;
   private final Set<ResourceLocation> loadingStack = Sets.newHashSet();
   private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
   private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.newHashMap();
   final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = Maps.newHashMap();
   private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.newHashMap();
   private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.newHashMap();
   private int nextModelGroup = 1;
   private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap<>(), (object2intopenhashmap) -> object2intopenhashmap.defaultReturnValue(-1));

   public ModelBakery(BlockColors blockcolors, ProfilerFiller profilerfiller, Map<ResourceLocation, BlockModel> map, Map<ResourceLocation, List<ModelBakery.LoadedJson>> map1) {
      this.blockColors = blockcolors;
      this.modelResources = map;
      this.blockStateResources = map1;
      profilerfiller.push("missing_model");

      try {
         this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
         this.loadTopLevel(MISSING_MODEL_LOCATION);
      } catch (IOException var7) {
         LOGGER.error("Error loading missing model, should never happen :(", (Throwable)var7);
         throw new RuntimeException(var7);
      }

      profilerfiller.popPush("static_definitions");
      STATIC_DEFINITIONS.forEach((resourcelocation1, statedefinition) -> statedefinition.getPossibleStates().forEach((blockstate1) -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(resourcelocation1, blockstate1))));
      profilerfiller.popPush("blocks");

      for(Block block : BuiltInRegistries.BLOCK) {
         block.getStateDefinition().getPossibleStates().forEach((blockstate) -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(blockstate)));
      }

      profilerfiller.popPush("items");

      for(ResourceLocation resourcelocation : BuiltInRegistries.ITEM.keySet()) {
         this.loadTopLevel(new ModelResourceLocation(resourcelocation, "inventory"));
      }

      profilerfiller.popPush("special");
      this.loadTopLevel(ItemRenderer.TRIDENT_IN_HAND_MODEL);
      this.loadTopLevel(ItemRenderer.SPYGLASS_IN_HAND_MODEL);
      this.topLevelModels.values().forEach((unbakedmodel) -> unbakedmodel.resolveParents(this::getModel));
      profilerfiller.pop();
   }

   public void bakeModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> bifunction) {
      this.topLevelModels.keySet().forEach((resourcelocation) -> {
         BakedModel bakedmodel = null;

         try {
            bakedmodel = (new ModelBakery.ModelBakerImpl(bifunction, resourcelocation)).bake(resourcelocation, BlockModelRotation.X0_Y0);
         } catch (Exception var5) {
            LOGGER.warn("Unable to bake model: '{}': {}", resourcelocation, var5);
         }

         if (bakedmodel != null) {
            this.bakedTopLevelModels.put(resourcelocation, bakedmodel);
         }

      });
   }

   private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> statedefinition, String s) {
      Map<Property<?>, Comparable<?>> map = Maps.newHashMap();

      for(String s1 : COMMA_SPLITTER.split(s)) {
         Iterator<String> iterator = EQUAL_SPLITTER.split(s1).iterator();
         if (iterator.hasNext()) {
            String s2 = iterator.next();
            Property<?> property = statedefinition.getProperty(s2);
            if (property != null && iterator.hasNext()) {
               String s3 = iterator.next();
               Comparable<?> comparable = getValueHelper(property, s3);
               if (comparable == null) {
                  throw new RuntimeException("Unknown value: '" + s3 + "' for blockstate property: '" + s2 + "' " + property.getPossibleValues());
               }

               map.put(property, comparable);
            } else if (!s2.isEmpty()) {
               throw new RuntimeException("Unknown blockstate property: '" + s2 + "'");
            }
         }
      }

      Block block = statedefinition.getOwner();
      return (blockstate) -> {
         if (blockstate != null && blockstate.is(block)) {
            for(Map.Entry<Property<?>, Comparable<?>> map_entry : map.entrySet()) {
               if (!Objects.equals(blockstate.getValue(map_entry.getKey()), map_entry.getValue())) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      };
   }

   @Nullable
   static <T extends Comparable<T>> T getValueHelper(Property<T> property, String s) {
      return property.getValue(s).orElse((T)null);
   }

   public UnbakedModel getModel(ResourceLocation resourcelocation) {
      if (this.unbakedCache.containsKey(resourcelocation)) {
         return this.unbakedCache.get(resourcelocation);
      } else if (this.loadingStack.contains(resourcelocation)) {
         throw new IllegalStateException("Circular reference while loading " + resourcelocation);
      } else {
         this.loadingStack.add(resourcelocation);
         UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);

         while(!this.loadingStack.isEmpty()) {
            ResourceLocation resourcelocation1 = this.loadingStack.iterator().next();

            try {
               if (!this.unbakedCache.containsKey(resourcelocation1)) {
                  this.loadModel(resourcelocation1);
               }
            } catch (ModelBakery.BlockStateDefinitionException var9) {
               LOGGER.warn(var9.getMessage());
               this.unbakedCache.put(resourcelocation1, unbakedmodel);
            } catch (Exception var10) {
               LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", resourcelocation1, resourcelocation, var10);
               this.unbakedCache.put(resourcelocation1, unbakedmodel);
            } finally {
               this.loadingStack.remove(resourcelocation1);
            }
         }

         return this.unbakedCache.getOrDefault(resourcelocation, unbakedmodel);
      }
   }

   private void loadModel(ResourceLocation resourcelocation) throws Exception {
      if (!(resourcelocation instanceof ModelResourceLocation modelresourcelocation)) {
         this.cacheAndQueueDependencies(resourcelocation, this.loadBlockModel(resourcelocation));
      } else {
         if (Objects.equals(modelresourcelocation.getVariant(), "inventory")) {
            ResourceLocation resourcelocation1 = resourcelocation.withPrefix("item/");
            BlockModel blockmodel = this.loadBlockModel(resourcelocation1);
            this.cacheAndQueueDependencies(modelresourcelocation, blockmodel);
            this.unbakedCache.put(resourcelocation1, blockmodel);
         } else {
            ResourceLocation resourcelocation2 = new ResourceLocation(resourcelocation.getNamespace(), resourcelocation.getPath());
            StateDefinition<Block, BlockState> statedefinition = Optional.ofNullable(STATIC_DEFINITIONS.get(resourcelocation2)).orElseGet(() -> BuiltInRegistries.BLOCK.get(resourcelocation2).getStateDefinition());
            this.context.setDefinition(statedefinition);
            List<Property<?>> list = ImmutableList.copyOf(this.blockColors.getColoringProperties(statedefinition.getOwner()));
            ImmutableList<BlockState> immutablelist = statedefinition.getPossibleStates();
            Map<ModelResourceLocation, BlockState> map = Maps.newHashMap();
            immutablelist.forEach((blockstate6) -> map.put(BlockModelShaper.stateToModelLocation(resourcelocation2, blockstate6), blockstate6));
            Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map1 = Maps.newHashMap();
            ResourceLocation resourcelocation3 = BLOCKSTATE_LISTER.idToFile(resourcelocation);
            UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);
            ModelBakery.ModelGroupKey modelbakery_modelgroupkey = new ModelBakery.ModelGroupKey(ImmutableList.of(unbakedmodel), ImmutableList.of());
            Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair = Pair.of(unbakedmodel, () -> modelbakery_modelgroupkey);

            try {
               for(Pair<String, BlockModelDefinition> pair1 : this.blockStateResources.getOrDefault(resourcelocation3, List.of()).stream().map((modelbakery_loadedjson) -> {
                  try {
                     return Pair.of(modelbakery_loadedjson.source, BlockModelDefinition.fromJsonElement(this.context, modelbakery_loadedjson.data));
                  } catch (Exception var4) {
                     throw new ModelBakery.BlockStateDefinitionException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resourcelocation3, modelbakery_loadedjson.source, var4.getMessage()));
                  }
               }).toList()) {
                  BlockModelDefinition blockmodeldefinition = pair1.getSecond();
                  Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map2 = Maps.newIdentityHashMap();
                  MultiPart multipart;
                  if (blockmodeldefinition.isMultiPart()) {
                     multipart = blockmodeldefinition.getMultiPart();
                     immutablelist.forEach((blockstate4) -> map2.put(blockstate4, Pair.of(multipart, () -> ModelBakery.ModelGroupKey.create(blockstate4, multipart, list))));
                  } else {
                     multipart = null;
                  }

                  blockmodeldefinition.getVariants().forEach((s, multivariant) -> {
                     try {
                        immutablelist.stream().filter(predicate(statedefinition, s)).forEach((blockstate2) -> {
                           Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair7 = map2.put(blockstate2, Pair.of(multivariant, () -> ModelBakery.ModelGroupKey.create(blockstate2, multivariant, list)));
                           if (pair7 != null && pair7.getFirst() != multipart) {
                              map2.put(blockstate2, pair);
                              throw new RuntimeException("Overlapping definition with: " + (String)blockmodeldefinition.getVariants().entrySet().stream().filter((map_entry) -> map_entry.getValue() == pair7.getFirst()).findFirst().get().getKey());
                           }
                        });
                     } catch (Exception var12) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", resourcelocation3, pair1.getFirst(), s, var12.getMessage());
                     }

                  });
                  map1.putAll(map2);
               }
            } catch (ModelBakery.BlockStateDefinitionException var24) {
               throw var24;
            } catch (Exception var25) {
               throw new ModelBakery.BlockStateDefinitionException(String.format(Locale.ROOT, "Exception loading blockstate definition: '%s': %s", resourcelocation3, var25));
            } finally {
               Map<ModelBakery.ModelGroupKey, Set<BlockState>> map4 = Maps.newHashMap();
               map.forEach((modelresourcelocation1, blockstate1) -> {
                  Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair3 = map1.get(blockstate1);
                  if (pair3 == null) {
                     LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", resourcelocation3, modelresourcelocation1);
                     pair3 = pair;
                  }

                  this.cacheAndQueueDependencies(modelresourcelocation1, pair3.getFirst());

                  try {
                     ModelBakery.ModelGroupKey modelbakery_modelgroupkey2 = pair3.getSecond().get();
                     map4.computeIfAbsent(modelbakery_modelgroupkey2, (modelbakery_modelgroupkey3) -> Sets.newIdentityHashSet()).add(blockstate1);
                  } catch (Exception var9) {
                     LOGGER.warn("Exception evaluating model definition: '{}'", modelresourcelocation1, var9);
                  }

               });
               map4.forEach((modelbakery_modelgroupkey1, set) -> {
                  Iterator<BlockState> iterator = set.iterator();

                  while(iterator.hasNext()) {
                     BlockState blockstate = iterator.next();
                     if (blockstate.getRenderShape() != RenderShape.MODEL) {
                        iterator.remove();
                        this.modelGroups.put(blockstate, 0);
                     }
                  }

                  if (set.size() > 1) {
                     this.registerModelGroup(set);
                  }

               });
            }
         }

      }
   }

   private void cacheAndQueueDependencies(ResourceLocation resourcelocation, UnbakedModel unbakedmodel) {
      this.unbakedCache.put(resourcelocation, unbakedmodel);
      this.loadingStack.addAll(unbakedmodel.getDependencies());
   }

   private void loadTopLevel(ModelResourceLocation modelresourcelocation) {
      UnbakedModel unbakedmodel = this.getModel(modelresourcelocation);
      this.unbakedCache.put(modelresourcelocation, unbakedmodel);
      this.topLevelModels.put(modelresourcelocation, unbakedmodel);
   }

   private void registerModelGroup(Iterable<BlockState> iterable) {
      int i = this.nextModelGroup++;
      iterable.forEach((blockstate) -> this.modelGroups.put(blockstate, i));
   }

   private BlockModel loadBlockModel(ResourceLocation resourcelocation) throws IOException {
      String s = resourcelocation.getPath();
      if ("builtin/generated".equals(s)) {
         return GENERATION_MARKER;
      } else if ("builtin/entity".equals(s)) {
         return BLOCK_ENTITY_MARKER;
      } else if (s.startsWith("builtin/")) {
         String s1 = s.substring("builtin/".length());
         String s2 = BUILTIN_MODELS.get(s1);
         if (s2 == null) {
            throw new FileNotFoundException(resourcelocation.toString());
         } else {
            Reader reader = new StringReader(s2);
            BlockModel blockmodel = BlockModel.fromStream(reader);
            blockmodel.name = resourcelocation.toString();
            return blockmodel;
         }
      } else {
         ResourceLocation resourcelocation1 = MODEL_LISTER.idToFile(resourcelocation);
         BlockModel blockmodel1 = this.modelResources.get(resourcelocation1);
         if (blockmodel1 == null) {
            throw new FileNotFoundException(resourcelocation1.toString());
         } else {
            blockmodel1.name = resourcelocation.toString();
            return blockmodel1;
         }
      }
   }

   public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
      return this.bakedTopLevelModels;
   }

   public Object2IntMap<BlockState> getModelGroups() {
      return this.modelGroups;
   }

   static record BakedCacheKey(ResourceLocation id, Transformation transformation, boolean isUvLocked) {
   }

   static class BlockStateDefinitionException extends RuntimeException {
      public BlockStateDefinitionException(String s) {
         super(s);
      }
   }

   public static record LoadedJson(String source, JsonElement data) {
      final String source;
      final JsonElement data;
   }

   class ModelBakerImpl implements ModelBaker {
      private final Function<Material, TextureAtlasSprite> modelTextureGetter;

      ModelBakerImpl(BiFunction<ResourceLocation, Material, TextureAtlasSprite> bifunction, ResourceLocation resourcelocation) {
         this.modelTextureGetter = (material) -> bifunction.apply(resourcelocation, material);
      }

      public UnbakedModel getModel(ResourceLocation resourcelocation) {
         return ModelBakery.this.getModel(resourcelocation);
      }

      public BakedModel bake(ResourceLocation resourcelocation, ModelState modelstate) {
         ModelBakery.BakedCacheKey modelbakery_bakedcachekey = new ModelBakery.BakedCacheKey(resourcelocation, modelstate.getRotation(), modelstate.isUvLocked());
         BakedModel bakedmodel = ModelBakery.this.bakedCache.get(modelbakery_bakedcachekey);
         if (bakedmodel != null) {
            return bakedmodel;
         } else {
            UnbakedModel unbakedmodel = this.getModel(resourcelocation);
            if (unbakedmodel instanceof BlockModel) {
               BlockModel blockmodel = (BlockModel)unbakedmodel;
               if (blockmodel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                  return ModelBakery.ITEM_MODEL_GENERATOR.generateBlockModel(this.modelTextureGetter, blockmodel).bake(this, blockmodel, this.modelTextureGetter, modelstate, resourcelocation, false);
               }
            }

            BakedModel bakedmodel1 = unbakedmodel.bake(this, this.modelTextureGetter, modelstate, resourcelocation);
            ModelBakery.this.bakedCache.put(modelbakery_bakedcachekey, bakedmodel1);
            return bakedmodel1;
         }
      }
   }

   static class ModelGroupKey {
      private final List<UnbakedModel> models;
      private final List<Object> coloringValues;

      public ModelGroupKey(List<UnbakedModel> list, List<Object> list1) {
         this.models = list;
         this.coloringValues = list1;
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (!(object instanceof ModelBakery.ModelGroupKey)) {
            return false;
         } else {
            ModelBakery.ModelGroupKey modelbakery_modelgroupkey = (ModelBakery.ModelGroupKey)object;
            return Objects.equals(this.models, modelbakery_modelgroupkey.models) && Objects.equals(this.coloringValues, modelbakery_modelgroupkey.coloringValues);
         }
      }

      public int hashCode() {
         return 31 * this.models.hashCode() + this.coloringValues.hashCode();
      }

      public static ModelBakery.ModelGroupKey create(BlockState blockstate, MultiPart multipart, Collection<Property<?>> collection) {
         StateDefinition<Block, BlockState> statedefinition = blockstate.getBlock().getStateDefinition();
         List<UnbakedModel> list = multipart.getSelectors().stream().filter((selector) -> selector.getPredicate(statedefinition).test(blockstate)).map(Selector::getVariant).collect(ImmutableList.toImmutableList());
         List<Object> list1 = getColoringValues(blockstate, collection);
         return new ModelBakery.ModelGroupKey(list, list1);
      }

      public static ModelBakery.ModelGroupKey create(BlockState blockstate, UnbakedModel unbakedmodel, Collection<Property<?>> collection) {
         List<Object> list = getColoringValues(blockstate, collection);
         return new ModelBakery.ModelGroupKey(ImmutableList.of(unbakedmodel), list);
      }

      private static List<Object> getColoringValues(BlockState blockstate, Collection<Property<?>> collection) {
         return collection.stream().map(blockstate::getValue).collect(ImmutableList.toImmutableList());
      }
   }
}
