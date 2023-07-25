package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class RuinedPortalPiece extends TemplateStructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float PROBABILITY_OF_GOLD_GONE = 0.3F;
   private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07F;
   private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2F;
   private final RuinedPortalPiece.VerticalPlacement verticalPlacement;
   private final RuinedPortalPiece.Properties properties;

   public RuinedPortalPiece(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, RuinedPortalPiece.VerticalPlacement ruinedportalpiece_verticalplacement, RuinedPortalPiece.Properties ruinedportalpiece_properties, ResourceLocation resourcelocation, StructureTemplate structuretemplate, Rotation rotation, Mirror mirror, BlockPos blockpos1) {
      super(StructurePieceType.RUINED_PORTAL, 0, structuretemplatemanager, resourcelocation, resourcelocation.toString(), makeSettings(mirror, rotation, ruinedportalpiece_verticalplacement, blockpos1, ruinedportalpiece_properties), blockpos);
      this.verticalPlacement = ruinedportalpiece_verticalplacement;
      this.properties = ruinedportalpiece_properties;
   }

   public RuinedPortalPiece(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag) {
      super(StructurePieceType.RUINED_PORTAL, compoundtag, structuretemplatemanager, (resourcelocation) -> makeSettings(structuretemplatemanager, compoundtag, resourcelocation));
      this.verticalPlacement = RuinedPortalPiece.VerticalPlacement.byName(compoundtag.getString("VerticalPlacement"));
      this.properties = RuinedPortalPiece.Properties.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Properties"))).getOrThrow(true, LOGGER::error);
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
      compoundtag.putString("Rotation", this.placeSettings.getRotation().name());
      compoundtag.putString("Mirror", this.placeSettings.getMirror().name());
      compoundtag.putString("VerticalPlacement", this.verticalPlacement.getName());
      RuinedPortalPiece.Properties.CODEC.encodeStart(NbtOps.INSTANCE, this.properties).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("Properties", tag));
   }

   private static StructurePlaceSettings makeSettings(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag, ResourceLocation resourcelocation) {
      StructureTemplate structuretemplate = structuretemplatemanager.getOrCreate(resourcelocation);
      BlockPos blockpos = new BlockPos(structuretemplate.getSize().getX() / 2, 0, structuretemplate.getSize().getZ() / 2);
      return makeSettings(Mirror.valueOf(compoundtag.getString("Mirror")), Rotation.valueOf(compoundtag.getString("Rotation")), RuinedPortalPiece.VerticalPlacement.byName(compoundtag.getString("VerticalPlacement")), blockpos, RuinedPortalPiece.Properties.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Properties"))).getOrThrow(true, LOGGER::error));
   }

   private static StructurePlaceSettings makeSettings(Mirror mirror, Rotation rotation, RuinedPortalPiece.VerticalPlacement ruinedportalpiece_verticalplacement, BlockPos blockpos, RuinedPortalPiece.Properties ruinedportalpiece_properties) {
      BlockIgnoreProcessor blockignoreprocessor = ruinedportalpiece_properties.airPocket ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
      List<ProcessorRule> list = Lists.newArrayList();
      list.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
      list.add(getLavaProcessorRule(ruinedportalpiece_verticalplacement, ruinedportalpiece_properties));
      if (!ruinedportalpiece_properties.cold) {
         list.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
      }

      StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setRotation(rotation).setMirror(mirror).setRotationPivot(blockpos).addProcessor(blockignoreprocessor).addProcessor(new RuleProcessor(list)).addProcessor(new BlockAgeProcessor(ruinedportalpiece_properties.mossiness)).addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE)).addProcessor(new LavaSubmergedBlockProcessor());
      if (ruinedportalpiece_properties.replaceWithBlackstone) {
         structureplacesettings.addProcessor(BlackstoneReplaceProcessor.INSTANCE);
      }

      return structureplacesettings;
   }

   private static ProcessorRule getLavaProcessorRule(RuinedPortalPiece.VerticalPlacement ruinedportalpiece_verticalplacement, RuinedPortalPiece.Properties ruinedportalpiece_properties) {
      if (ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR) {
         return getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK);
      } else {
         return ruinedportalpiece_properties.cold ? getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK) : getBlockReplaceRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK);
      }
   }

   public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
      BoundingBox boundingbox1 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
      if (boundingbox.isInside(boundingbox1.getCenter())) {
         boundingbox.encapsulate(boundingbox1);
         super.postProcess(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, blockpos);
         this.spreadNetherrack(randomsource, worldgenlevel);
         this.addNetherrackDripColumnsBelowPortal(randomsource, worldgenlevel);
         if (this.properties.vines || this.properties.overgrown) {
            BlockPos.betweenClosedStream(this.getBoundingBox()).forEach((blockpos1) -> {
               if (this.properties.vines) {
                  this.maybeAddVines(randomsource, worldgenlevel, blockpos1);
               }

               if (this.properties.overgrown) {
                  this.maybeAddLeavesAbove(randomsource, worldgenlevel, blockpos1);
               }

            });
         }

      }
   }

   protected void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox) {
   }

   private void maybeAddVines(RandomSource randomsource, LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      if (!blockstate.isAir() && !blockstate.is(Blocks.VINE)) {
         Direction direction = getRandomHorizontalDirection(randomsource);
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate1 = levelaccessor.getBlockState(blockpos1);
         if (blockstate1.isAir()) {
            if (Block.isFaceFull(blockstate.getCollisionShape(levelaccessor, blockpos), direction)) {
               BooleanProperty booleanproperty = VineBlock.getPropertyForFace(direction.getOpposite());
               levelaccessor.setBlock(blockpos1, Blocks.VINE.defaultBlockState().setValue(booleanproperty, Boolean.valueOf(true)), 3);
            }
         }
      }
   }

   private void maybeAddLeavesAbove(RandomSource randomsource, LevelAccessor levelaccessor, BlockPos blockpos) {
      if (randomsource.nextFloat() < 0.5F && levelaccessor.getBlockState(blockpos).is(Blocks.NETHERRACK) && levelaccessor.getBlockState(blockpos.above()).isAir()) {
         levelaccessor.setBlock(blockpos.above(), Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, Boolean.valueOf(true)), 3);
      }

   }

   private void addNetherrackDripColumnsBelowPortal(RandomSource randomsource, LevelAccessor levelaccessor) {
      for(int i = this.boundingBox.minX() + 1; i < this.boundingBox.maxX(); ++i) {
         for(int j = this.boundingBox.minZ() + 1; j < this.boundingBox.maxZ(); ++j) {
            BlockPos blockpos = new BlockPos(i, this.boundingBox.minY(), j);
            if (levelaccessor.getBlockState(blockpos).is(Blocks.NETHERRACK)) {
               this.addNetherrackDripColumn(randomsource, levelaccessor, blockpos.below());
            }
         }
      }

   }

   private void addNetherrackDripColumn(RandomSource randomsource, LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      this.placeNetherrackOrMagma(randomsource, levelaccessor, blockpos_mutableblockpos);
      int i = 8;

      while(i > 0 && randomsource.nextFloat() < 0.5F) {
         blockpos_mutableblockpos.move(Direction.DOWN);
         --i;
         this.placeNetherrackOrMagma(randomsource, levelaccessor, blockpos_mutableblockpos);
      }

   }

   private void spreadNetherrack(RandomSource randomsource, LevelAccessor levelaccessor) {
      boolean flag = this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE || this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
      BlockPos blockpos = this.boundingBox.getCenter();
      int i = blockpos.getX();
      int j = blockpos.getZ();
      float[] afloat = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
      int k = afloat.length;
      int l = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
      int i1 = randomsource.nextInt(Math.max(1, 8 - l / 2));
      int j1 = 3;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = BlockPos.ZERO.mutable();

      for(int k1 = i - k; k1 <= i + k; ++k1) {
         for(int l1 = j - k; l1 <= j + k; ++l1) {
            int i2 = Math.abs(k1 - i) + Math.abs(l1 - j);
            int j2 = Math.max(0, i2 + i1);
            if (j2 < k) {
               float f = afloat[j2];
               if (randomsource.nextDouble() < (double)f) {
                  int k2 = getSurfaceY(levelaccessor, k1, l1, this.verticalPlacement);
                  int l2 = flag ? k2 : Math.min(this.boundingBox.minY(), k2);
                  blockpos_mutableblockpos.set(k1, l2, l1);
                  if (Math.abs(l2 - this.boundingBox.minY()) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma(levelaccessor, blockpos_mutableblockpos)) {
                     this.placeNetherrackOrMagma(randomsource, levelaccessor, blockpos_mutableblockpos);
                     if (this.properties.overgrown) {
                        this.maybeAddLeavesAbove(randomsource, levelaccessor, blockpos_mutableblockpos);
                     }

                     this.addNetherrackDripColumn(randomsource, levelaccessor, blockpos_mutableblockpos.below());
                  }
               }
            }
         }
      }

   }

   private boolean canBlockBeReplacedByNetherrackOrMagma(LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      return !blockstate.is(Blocks.AIR) && !blockstate.is(Blocks.OBSIDIAN) && !blockstate.is(BlockTags.FEATURES_CANNOT_REPLACE) && (this.verticalPlacement == RuinedPortalPiece.VerticalPlacement.IN_NETHER || !blockstate.is(Blocks.LAVA));
   }

   private void placeNetherrackOrMagma(RandomSource randomsource, LevelAccessor levelaccessor, BlockPos blockpos) {
      if (!this.properties.cold && randomsource.nextFloat() < 0.07F) {
         levelaccessor.setBlock(blockpos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
      } else {
         levelaccessor.setBlock(blockpos, Blocks.NETHERRACK.defaultBlockState(), 3);
      }

   }

   private static int getSurfaceY(LevelAccessor levelaccessor, int i, int j, RuinedPortalPiece.VerticalPlacement ruinedportalpiece_verticalplacement) {
      return levelaccessor.getHeight(getHeightMapType(ruinedportalpiece_verticalplacement), i, j) - 1;
   }

   public static Heightmap.Types getHeightMapType(RuinedPortalPiece.VerticalPlacement ruinedportalpiece_verticalplacement) {
      return ruinedportalpiece_verticalplacement == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
   }

   private static ProcessorRule getBlockReplaceRule(Block block, float f, Block block1) {
      return new ProcessorRule(new RandomBlockMatchTest(block, f), AlwaysTrueTest.INSTANCE, block1.defaultBlockState());
   }

   private static ProcessorRule getBlockReplaceRule(Block block, Block block1) {
      return new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, block1.defaultBlockState());
   }

   public static class Properties {
      public static final Codec<RuinedPortalPiece.Properties> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.BOOL.fieldOf("cold").forGetter((ruinedportalpiece_properties5) -> ruinedportalpiece_properties5.cold), Codec.FLOAT.fieldOf("mossiness").forGetter((ruinedportalpiece_properties4) -> ruinedportalpiece_properties4.mossiness), Codec.BOOL.fieldOf("air_pocket").forGetter((ruinedportalpiece_properties3) -> ruinedportalpiece_properties3.airPocket), Codec.BOOL.fieldOf("overgrown").forGetter((ruinedportalpiece_properties2) -> ruinedportalpiece_properties2.overgrown), Codec.BOOL.fieldOf("vines").forGetter((ruinedportalpiece_properties1) -> ruinedportalpiece_properties1.vines), Codec.BOOL.fieldOf("replace_with_blackstone").forGetter((ruinedportalpiece_properties) -> ruinedportalpiece_properties.replaceWithBlackstone)).apply(recordcodecbuilder_instance, RuinedPortalPiece.Properties::new));
      public boolean cold;
      public float mossiness;
      public boolean airPocket;
      public boolean overgrown;
      public boolean vines;
      public boolean replaceWithBlackstone;

      public Properties() {
      }

      public Properties(boolean flag, float f, boolean flag1, boolean flag2, boolean flag3, boolean flag4) {
         this.cold = flag;
         this.mossiness = f;
         this.airPocket = flag1;
         this.overgrown = flag2;
         this.vines = flag3;
         this.replaceWithBlackstone = flag4;
      }
   }

   public static enum VerticalPlacement implements StringRepresentable {
      ON_LAND_SURFACE("on_land_surface"),
      PARTLY_BURIED("partly_buried"),
      ON_OCEAN_FLOOR("on_ocean_floor"),
      IN_MOUNTAIN("in_mountain"),
      UNDERGROUND("underground"),
      IN_NETHER("in_nether");

      public static final StringRepresentable.EnumCodec<RuinedPortalPiece.VerticalPlacement> CODEC = StringRepresentable.fromEnum(RuinedPortalPiece.VerticalPlacement::values);
      private final String name;

      private VerticalPlacement(String s) {
         this.name = s;
      }

      public String getName() {
         return this.name;
      }

      public static RuinedPortalPiece.VerticalPlacement byName(String s) {
         return CODEC.byName(s);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
