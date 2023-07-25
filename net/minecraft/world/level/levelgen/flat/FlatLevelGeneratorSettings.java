package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class FlatLevelGeneratorSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryCodecs.homogeneousList(Registries.STRUCTURE_SET).optionalFieldOf("structure_overrides").forGetter((flatlevelgeneratorsettings3) -> flatlevelgeneratorsettings3.structureOverrides), FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo), Codec.BOOL.fieldOf("lakes").orElse(false).forGetter((flatlevelgeneratorsettings2) -> flatlevelgeneratorsettings2.addLakes), Codec.BOOL.fieldOf("features").orElse(false).forGetter((flatlevelgeneratorsettings1) -> flatlevelgeneratorsettings1.decoration), Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter((flatlevelgeneratorsettings) -> Optional.of(flatlevelgeneratorsettings.biome)), RegistryOps.retrieveElement(Biomes.PLAINS), RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND), RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_SURFACE)).apply(recordcodecbuilder_instance, FlatLevelGeneratorSettings::new)).comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity()).stable();
   private final Optional<HolderSet<StructureSet>> structureOverrides;
   private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
   private final Holder<Biome> biome;
   private final List<BlockState> layers;
   private boolean voidGen;
   private boolean decoration;
   private boolean addLakes;
   private final List<Holder<PlacedFeature>> lakes;

   private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      int i = flatlevelgeneratorsettings.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
      return i > DimensionType.Y_SIZE ? DataResult.error(() -> "Sum of layer heights is > " + DimensionType.Y_SIZE, flatlevelgeneratorsettings) : DataResult.success(flatlevelgeneratorsettings);
   }

   private FlatLevelGeneratorSettings(Optional<HolderSet<StructureSet>> optional, List<FlatLayerInfo> list, boolean flag, boolean flag1, Optional<Holder<Biome>> optional1, Holder.Reference<Biome> holder_reference, Holder<PlacedFeature> holder, Holder<PlacedFeature> holder1) {
      this(optional, getBiome(optional1, holder_reference), List.of(holder, holder1));
      if (flag) {
         this.setAddLakes();
      }

      if (flag1) {
         this.setDecoration();
      }

      this.layersInfo.addAll(list);
      this.updateLayers();
   }

   private static Holder<Biome> getBiome(Optional<? extends Holder<Biome>> optional, Holder<Biome> holder) {
      if (optional.isEmpty()) {
         LOGGER.error("Unknown biome, defaulting to plains");
         return holder;
      } else {
         return optional.get();
      }
   }

   public FlatLevelGeneratorSettings(Optional<HolderSet<StructureSet>> optional, Holder<Biome> holder, List<Holder<PlacedFeature>> list) {
      this.structureOverrides = optional;
      this.biome = holder;
      this.layers = Lists.newArrayList();
      this.lakes = list;
   }

   public FlatLevelGeneratorSettings withBiomeAndLayers(List<FlatLayerInfo> list, Optional<HolderSet<StructureSet>> optional, Holder<Biome> holder) {
      FlatLevelGeneratorSettings flatlevelgeneratorsettings = new FlatLevelGeneratorSettings(optional, holder, this.lakes);

      for(FlatLayerInfo flatlayerinfo : list) {
         flatlevelgeneratorsettings.layersInfo.add(new FlatLayerInfo(flatlayerinfo.getHeight(), flatlayerinfo.getBlockState().getBlock()));
         flatlevelgeneratorsettings.updateLayers();
      }

      if (this.decoration) {
         flatlevelgeneratorsettings.setDecoration();
      }

      if (this.addLakes) {
         flatlevelgeneratorsettings.setAddLakes();
      }

      return flatlevelgeneratorsettings;
   }

   public void setDecoration() {
      this.decoration = true;
   }

   public void setAddLakes() {
      this.addLakes = true;
   }

   public BiomeGenerationSettings adjustGenerationSettings(Holder<Biome> holder) {
      if (!holder.equals(this.biome)) {
         return holder.value().getGenerationSettings();
      } else {
         BiomeGenerationSettings biomegenerationsettings = this.getBiome().value().getGenerationSettings();
         BiomeGenerationSettings.PlainBuilder biomegenerationsettings_plainbuilder = new BiomeGenerationSettings.PlainBuilder();
         if (this.addLakes) {
            for(Holder<PlacedFeature> holder1 : this.lakes) {
               biomegenerationsettings_plainbuilder.addFeature(GenerationStep.Decoration.LAKES, holder1);
            }
         }

         boolean flag = (!this.voidGen || holder.is(Biomes.THE_VOID)) && this.decoration;
         if (flag) {
            List<HolderSet<PlacedFeature>> list = biomegenerationsettings.features();

            for(int i = 0; i < list.size(); ++i) {
               if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal() && (!this.addLakes || i != GenerationStep.Decoration.LAKES.ordinal())) {
                  for(Holder<PlacedFeature> holder2 : list.get(i)) {
                     biomegenerationsettings_plainbuilder.addFeature(i, holder2);
                  }
               }
            }
         }

         List<BlockState> list1 = this.getLayers();

         for(int j = 0; j < list1.size(); ++j) {
            BlockState blockstate = list1.get(j);
            if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockstate)) {
               list1.set(j, (BlockState)null);
               biomegenerationsettings_plainbuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(j, blockstate)));
            }
         }

         return biomegenerationsettings_plainbuilder.build();
      }
   }

   public Optional<HolderSet<StructureSet>> structureOverrides() {
      return this.structureOverrides;
   }

   public Holder<Biome> getBiome() {
      return this.biome;
   }

   public List<FlatLayerInfo> getLayersInfo() {
      return this.layersInfo;
   }

   public List<BlockState> getLayers() {
      return this.layers;
   }

   public void updateLayers() {
      this.layers.clear();

      for(FlatLayerInfo flatlayerinfo : this.layersInfo) {
         for(int i = 0; i < flatlayerinfo.getHeight(); ++i) {
            this.layers.add(flatlayerinfo.getBlockState());
         }
      }

      this.voidGen = this.layers.stream().allMatch((blockstate) -> blockstate.is(Blocks.AIR));
   }

   public static FlatLevelGeneratorSettings getDefault(HolderGetter<Biome> holdergetter, HolderGetter<StructureSet> holdergetter1, HolderGetter<PlacedFeature> holdergetter2) {
      HolderSet<StructureSet> holderset = HolderSet.direct(holdergetter1.getOrThrow(BuiltinStructureSets.STRONGHOLDS), holdergetter1.getOrThrow(BuiltinStructureSets.VILLAGES));
      FlatLevelGeneratorSettings flatlevelgeneratorsettings = new FlatLevelGeneratorSettings(Optional.of(holderset), getDefaultBiome(holdergetter), createLakesList(holdergetter2));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
      flatlevelgeneratorsettings.updateLayers();
      return flatlevelgeneratorsettings;
   }

   public static Holder<Biome> getDefaultBiome(HolderGetter<Biome> holdergetter) {
      return holdergetter.getOrThrow(Biomes.PLAINS);
   }

   public static List<Holder<PlacedFeature>> createLakesList(HolderGetter<PlacedFeature> holdergetter) {
      return List.of(holdergetter.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND), holdergetter.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_SURFACE));
   }
}
