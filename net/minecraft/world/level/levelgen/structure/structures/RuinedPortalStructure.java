package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalStructure extends Structure {
   private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
   private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
   private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05F;
   private static final int MIN_Y_INDEX = 15;
   private final List<RuinedPortalStructure.Setup> setups;
   public static final Codec<RuinedPortalStructure> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance), ExtraCodecs.nonEmptyList(RuinedPortalStructure.Setup.CODEC.listOf()).fieldOf("setups").forGetter((ruinedportalstructure) -> ruinedportalstructure.setups)).apply(recordcodecbuilder_instance, RuinedPortalStructure::new));

   public RuinedPortalStructure(Structure.StructureSettings structure_structuresettings, List<RuinedPortalStructure.Setup> list) {
      super(structure_structuresettings);
      this.setups = list;
   }

   public RuinedPortalStructure(Structure.StructureSettings structure_structuresettings, RuinedPortalStructure.Setup ruinedportalstructure_setup) {
      this(structure_structuresettings, List.of(ruinedportalstructure_setup));
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      RuinedPortalPiece.Properties ruinedportalpiece_properties = new RuinedPortalPiece.Properties();
      WorldgenRandom worldgenrandom = structure_generationcontext.random();
      RuinedPortalStructure.Setup ruinedportalstructure_setup = null;
      if (this.setups.size() > 1) {
         float f = 0.0F;

         for(RuinedPortalStructure.Setup ruinedportalstructure_setup1 : this.setups) {
            f += ruinedportalstructure_setup1.weight();
         }

         float f1 = worldgenrandom.nextFloat();

         for(RuinedPortalStructure.Setup ruinedportalstructure_setup2 : this.setups) {
            f1 -= ruinedportalstructure_setup2.weight() / f;
            if (f1 < 0.0F) {
               ruinedportalstructure_setup = ruinedportalstructure_setup2;
               break;
            }
         }
      } else {
         ruinedportalstructure_setup = this.setups.get(0);
      }

      if (ruinedportalstructure_setup == null) {
         throw new IllegalStateException();
      } else {
         RuinedPortalStructure.Setup ruinedportalstructure_setup3 = ruinedportalstructure_setup;
         ruinedportalpiece_properties.airPocket = sample(worldgenrandom, ruinedportalstructure_setup3.airPocketProbability());
         ruinedportalpiece_properties.mossiness = ruinedportalstructure_setup3.mossiness();
         ruinedportalpiece_properties.overgrown = ruinedportalstructure_setup3.overgrown();
         ruinedportalpiece_properties.vines = ruinedportalstructure_setup3.vines();
         ruinedportalpiece_properties.replaceWithBlackstone = ruinedportalstructure_setup3.replaceWithBlackstone();
         ResourceLocation resourcelocation;
         if (worldgenrandom.nextFloat() < 0.05F) {
            resourcelocation = new ResourceLocation(STRUCTURE_LOCATION_GIANT_PORTALS[worldgenrandom.nextInt(STRUCTURE_LOCATION_GIANT_PORTALS.length)]);
         } else {
            resourcelocation = new ResourceLocation(STRUCTURE_LOCATION_PORTALS[worldgenrandom.nextInt(STRUCTURE_LOCATION_PORTALS.length)]);
         }

         StructureTemplate structuretemplate = structure_generationcontext.structureTemplateManager().getOrCreate(resourcelocation);
         Rotation rotation = Util.getRandom(Rotation.values(), worldgenrandom);
         Mirror mirror = worldgenrandom.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
         BlockPos blockpos = new BlockPos(structuretemplate.getSize().getX() / 2, 0, structuretemplate.getSize().getZ() / 2);
         ChunkGenerator chunkgenerator = structure_generationcontext.chunkGenerator();
         LevelHeightAccessor levelheightaccessor = structure_generationcontext.heightAccessor();
         RandomState randomstate = structure_generationcontext.randomState();
         BlockPos blockpos1 = structure_generationcontext.chunkPos().getWorldPosition();
         BoundingBox boundingbox = structuretemplate.getBoundingBox(blockpos1, rotation, blockpos, mirror);
         BlockPos blockpos2 = boundingbox.getCenter();
         int i = chunkgenerator.getBaseHeight(blockpos2.getX(), blockpos2.getZ(), RuinedPortalPiece.getHeightMapType(ruinedportalstructure_setup3.placement()), levelheightaccessor, randomstate) - 1;
         int j = findSuitableY(worldgenrandom, chunkgenerator, ruinedportalstructure_setup3.placement(), ruinedportalpiece_properties.airPocket, i, boundingbox.getYSpan(), boundingbox, levelheightaccessor, randomstate);
         BlockPos blockpos3 = new BlockPos(blockpos1.getX(), j, blockpos1.getZ());
         return Optional.of(new Structure.GenerationStub(blockpos3, (structurepiecesbuilder) -> {
            if (ruinedportalstructure_setup3.canBeCold()) {
               ruinedportalpiece_properties.cold = isCold(blockpos3, structure_generationcontext.chunkGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockpos3.getX()), QuartPos.fromBlock(blockpos3.getY()), QuartPos.fromBlock(blockpos3.getZ()), randomstate.sampler()));
            }

            structurepiecesbuilder.addPiece(new RuinedPortalPiece(structure_generationcontext.structureTemplateManager(), blockpos3, ruinedportalstructure_setup3.placement(), ruinedportalpiece_properties, resourcelocation, structuretemplate, rotation, mirror, blockpos));
         }));
      }
   }

   private static boolean sample(WorldgenRandom worldgenrandom, float f) {
      if (f == 0.0F) {
         return false;
      } else if (f == 1.0F) {
         return true;
      } else {
         return worldgenrandom.nextFloat() < f;
      }
   }

   private static boolean isCold(BlockPos blockpos, Holder<Biome> holder) {
      return holder.value().coldEnoughToSnow(blockpos);
   }

   private static int findSuitableY(RandomSource randomsource, ChunkGenerator chunkgenerator, RuinedPortalPiece.VerticalPlacement ruinedportalpiece_verticalplacement, boolean flag, int i, int j, BoundingBox boundingbox, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      int k = levelheightaccessor.getMinBuildHeight() + 15;
      int l;
      if (ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
         if (flag) {
            l = Mth.randomBetweenInclusive(randomsource, 32, 100);
         } else if (randomsource.nextFloat() < 0.5F) {
            l = Mth.randomBetweenInclusive(randomsource, 27, 29);
         } else {
            l = Mth.randomBetweenInclusive(randomsource, 29, 100);
         }
      } else if (ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
         int k1 = i - j;
         l = getRandomWithinInterval(randomsource, 70, k1);
      } else if (ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
         int i2 = i - j;
         l = getRandomWithinInterval(randomsource, k, i2);
      } else if (ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
         l = i - j + Mth.randomBetweenInclusive(randomsource, 2, 8);
      } else {
         l = i;
      }

      List<BlockPos> list = ImmutableList.of(new BlockPos(boundingbox.minX(), 0, boundingbox.minZ()), new BlockPos(boundingbox.maxX(), 0, boundingbox.minZ()), new BlockPos(boundingbox.minX(), 0, boundingbox.maxZ()), new BlockPos(boundingbox.maxX(), 0, boundingbox.maxZ()));
      List<NoiseColumn> list1 = list.stream().map((blockpos) -> chunkgenerator.getBaseColumn(blockpos.getX(), blockpos.getZ(), levelheightaccessor, randomstate)).collect(Collectors.toList());
      Heightmap.Types heightmap_types = ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;

      int i3;
      for(i3 = l; i3 > k; --i3) {
         int j3 = 0;

         for(NoiseColumn noisecolumn : list1) {
            BlockState blockstate = noisecolumn.getBlock(i3);
            if (heightmap_types.isOpaque().test(blockstate)) {
               ++j3;
               if (j3 == 3) {
                  return i3;
               }
            }
         }
      }

      return i3;
   }

   private static int getRandomWithinInterval(RandomSource randomsource, int i, int j) {
      return i < j ? Mth.randomBetweenInclusive(randomsource, i, j) : j;
   }

   public StructureType<?> type() {
      return StructureType.RUINED_PORTAL;
   }

   public static record Setup(RuinedPortalPiece.VerticalPlacement placement, float airPocketProbability, float mossiness, boolean overgrown, boolean vines, boolean canBeCold, boolean replaceWithBlackstone, float weight) {
      public static final Codec<RuinedPortalStructure.Setup> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RuinedPortalPiece.VerticalPlacement.CODEC.fieldOf("placement").forGetter(RuinedPortalStructure.Setup::placement), Codec.floatRange(0.0F, 1.0F).fieldOf("air_pocket_probability").forGetter(RuinedPortalStructure.Setup::airPocketProbability), Codec.floatRange(0.0F, 1.0F).fieldOf("mossiness").forGetter(RuinedPortalStructure.Setup::mossiness), Codec.BOOL.fieldOf("overgrown").forGetter(RuinedPortalStructure.Setup::overgrown), Codec.BOOL.fieldOf("vines").forGetter(RuinedPortalStructure.Setup::vines), Codec.BOOL.fieldOf("can_be_cold").forGetter(RuinedPortalStructure.Setup::canBeCold), Codec.BOOL.fieldOf("replace_with_blackstone").forGetter(RuinedPortalStructure.Setup::replaceWithBlackstone), ExtraCodecs.POSITIVE_FLOAT.fieldOf("weight").forGetter(RuinedPortalStructure.Setup::weight)).apply(recordcodecbuilder_instance, RuinedPortalStructure.Setup::new));
   }
}
