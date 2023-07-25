package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class OceanRuinPieces {
   static final StructureProcessor WARM_SUSPICIOUS_BLOCK_PROCESSOR = archyRuleProcessor(Blocks.SAND, Blocks.SUSPICIOUS_SAND, BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY);
   static final StructureProcessor COLD_SUSPICIOUS_BLOCK_PROCESSOR = archyRuleProcessor(Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL, BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY);
   private static final ResourceLocation[] WARM_RUINS = new ResourceLocation[]{new ResourceLocation("underwater_ruin/warm_1"), new ResourceLocation("underwater_ruin/warm_2"), new ResourceLocation("underwater_ruin/warm_3"), new ResourceLocation("underwater_ruin/warm_4"), new ResourceLocation("underwater_ruin/warm_5"), new ResourceLocation("underwater_ruin/warm_6"), new ResourceLocation("underwater_ruin/warm_7"), new ResourceLocation("underwater_ruin/warm_8")};
   private static final ResourceLocation[] RUINS_BRICK = new ResourceLocation[]{new ResourceLocation("underwater_ruin/brick_1"), new ResourceLocation("underwater_ruin/brick_2"), new ResourceLocation("underwater_ruin/brick_3"), new ResourceLocation("underwater_ruin/brick_4"), new ResourceLocation("underwater_ruin/brick_5"), new ResourceLocation("underwater_ruin/brick_6"), new ResourceLocation("underwater_ruin/brick_7"), new ResourceLocation("underwater_ruin/brick_8")};
   private static final ResourceLocation[] RUINS_CRACKED = new ResourceLocation[]{new ResourceLocation("underwater_ruin/cracked_1"), new ResourceLocation("underwater_ruin/cracked_2"), new ResourceLocation("underwater_ruin/cracked_3"), new ResourceLocation("underwater_ruin/cracked_4"), new ResourceLocation("underwater_ruin/cracked_5"), new ResourceLocation("underwater_ruin/cracked_6"), new ResourceLocation("underwater_ruin/cracked_7"), new ResourceLocation("underwater_ruin/cracked_8")};
   private static final ResourceLocation[] RUINS_MOSSY = new ResourceLocation[]{new ResourceLocation("underwater_ruin/mossy_1"), new ResourceLocation("underwater_ruin/mossy_2"), new ResourceLocation("underwater_ruin/mossy_3"), new ResourceLocation("underwater_ruin/mossy_4"), new ResourceLocation("underwater_ruin/mossy_5"), new ResourceLocation("underwater_ruin/mossy_6"), new ResourceLocation("underwater_ruin/mossy_7"), new ResourceLocation("underwater_ruin/mossy_8")};
   private static final ResourceLocation[] BIG_RUINS_BRICK = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_brick_1"), new ResourceLocation("underwater_ruin/big_brick_2"), new ResourceLocation("underwater_ruin/big_brick_3"), new ResourceLocation("underwater_ruin/big_brick_8")};
   private static final ResourceLocation[] BIG_RUINS_MOSSY = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_mossy_1"), new ResourceLocation("underwater_ruin/big_mossy_2"), new ResourceLocation("underwater_ruin/big_mossy_3"), new ResourceLocation("underwater_ruin/big_mossy_8")};
   private static final ResourceLocation[] BIG_RUINS_CRACKED = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_cracked_1"), new ResourceLocation("underwater_ruin/big_cracked_2"), new ResourceLocation("underwater_ruin/big_cracked_3"), new ResourceLocation("underwater_ruin/big_cracked_8")};
   private static final ResourceLocation[] BIG_WARM_RUINS = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_warm_4"), new ResourceLocation("underwater_ruin/big_warm_5"), new ResourceLocation("underwater_ruin/big_warm_6"), new ResourceLocation("underwater_ruin/big_warm_7")};

   private static StructureProcessor archyRuleProcessor(Block block, Block block1, ResourceLocation resourcelocation) {
      return new CappedProcessor(new RuleProcessor(List.of(new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, PosAlwaysTrueTest.INSTANCE, block1.defaultBlockState(), new AppendLoot(resourcelocation)))), ConstantInt.of(5));
   }

   private static ResourceLocation getSmallWarmRuin(RandomSource randomsource) {
      return Util.getRandom(WARM_RUINS, randomsource);
   }

   private static ResourceLocation getBigWarmRuin(RandomSource randomsource) {
      return Util.getRandom(BIG_WARM_RUINS, randomsource);
   }

   public static void addPieces(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, OceanRuinStructure oceanruinstructure) {
      boolean flag = randomsource.nextFloat() <= oceanruinstructure.largeProbability;
      float f = flag ? 0.9F : 0.8F;
      addPiece(structuretemplatemanager, blockpos, rotation, structurepieceaccessor, randomsource, oceanruinstructure, flag, f);
      if (flag && randomsource.nextFloat() <= oceanruinstructure.clusterProbability) {
         addClusterRuins(structuretemplatemanager, randomsource, rotation, blockpos, oceanruinstructure, structurepieceaccessor);
      }

   }

   private static void addClusterRuins(StructureTemplateManager structuretemplatemanager, RandomSource randomsource, Rotation rotation, BlockPos blockpos, OceanRuinStructure oceanruinstructure, StructurePieceAccessor structurepieceaccessor) {
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), 90, blockpos.getZ());
      BlockPos blockpos2 = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, rotation, BlockPos.ZERO).offset(blockpos1);
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos1, blockpos2);
      BlockPos blockpos3 = new BlockPos(Math.min(blockpos1.getX(), blockpos2.getX()), blockpos1.getY(), Math.min(blockpos1.getZ(), blockpos2.getZ()));
      List<BlockPos> list = allPositions(randomsource, blockpos3);
      int i = Mth.nextInt(randomsource, 4, 8);

      for(int j = 0; j < i; ++j) {
         if (!list.isEmpty()) {
            int k = randomsource.nextInt(list.size());
            BlockPos blockpos4 = list.remove(k);
            Rotation rotation1 = Rotation.getRandom(randomsource);
            BlockPos blockpos5 = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, rotation1, BlockPos.ZERO).offset(blockpos4);
            BoundingBox boundingbox1 = BoundingBox.fromCorners(blockpos4, blockpos5);
            if (!boundingbox1.intersects(boundingbox)) {
               addPiece(structuretemplatemanager, blockpos4, rotation1, structurepieceaccessor, randomsource, oceanruinstructure, false, 0.8F);
            }
         }
      }

   }

   private static List<BlockPos> allPositions(RandomSource randomsource, BlockPos blockpos) {
      List<BlockPos> list = Lists.newArrayList();
      list.add(blockpos.offset(-16 + Mth.nextInt(randomsource, 1, 8), 0, 16 + Mth.nextInt(randomsource, 1, 7)));
      list.add(blockpos.offset(-16 + Mth.nextInt(randomsource, 1, 8), 0, Mth.nextInt(randomsource, 1, 7)));
      list.add(blockpos.offset(-16 + Mth.nextInt(randomsource, 1, 8), 0, -16 + Mth.nextInt(randomsource, 4, 8)));
      list.add(blockpos.offset(Mth.nextInt(randomsource, 1, 7), 0, 16 + Mth.nextInt(randomsource, 1, 7)));
      list.add(blockpos.offset(Mth.nextInt(randomsource, 1, 7), 0, -16 + Mth.nextInt(randomsource, 4, 6)));
      list.add(blockpos.offset(16 + Mth.nextInt(randomsource, 1, 7), 0, 16 + Mth.nextInt(randomsource, 3, 8)));
      list.add(blockpos.offset(16 + Mth.nextInt(randomsource, 1, 7), 0, Mth.nextInt(randomsource, 1, 7)));
      list.add(blockpos.offset(16 + Mth.nextInt(randomsource, 1, 7), 0, -16 + Mth.nextInt(randomsource, 4, 8)));
      return list;
   }

   private static void addPiece(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, OceanRuinStructure oceanruinstructure, boolean flag, float f) {
      switch (oceanruinstructure.biomeTemp) {
         case WARM:
         default:
            ResourceLocation resourcelocation = flag ? getBigWarmRuin(randomsource) : getSmallWarmRuin(randomsource);
            structurepieceaccessor.addPiece(new OceanRuinPieces.OceanRuinPiece(structuretemplatemanager, resourcelocation, blockpos, rotation, f, oceanruinstructure.biomeTemp, flag));
            break;
         case COLD:
            ResourceLocation[] aresourcelocation = flag ? BIG_RUINS_BRICK : RUINS_BRICK;
            ResourceLocation[] aresourcelocation1 = flag ? BIG_RUINS_CRACKED : RUINS_CRACKED;
            ResourceLocation[] aresourcelocation2 = flag ? BIG_RUINS_MOSSY : RUINS_MOSSY;
            int i = randomsource.nextInt(aresourcelocation.length);
            structurepieceaccessor.addPiece(new OceanRuinPieces.OceanRuinPiece(structuretemplatemanager, aresourcelocation[i], blockpos, rotation, f, oceanruinstructure.biomeTemp, flag));
            structurepieceaccessor.addPiece(new OceanRuinPieces.OceanRuinPiece(structuretemplatemanager, aresourcelocation1[i], blockpos, rotation, 0.7F, oceanruinstructure.biomeTemp, flag));
            structurepieceaccessor.addPiece(new OceanRuinPieces.OceanRuinPiece(structuretemplatemanager, aresourcelocation2[i], blockpos, rotation, 0.5F, oceanruinstructure.biomeTemp, flag));
      }

   }

   public static class OceanRuinPiece extends TemplateStructurePiece {
      private final OceanRuinStructure.Type biomeType;
      private final float integrity;
      private final boolean isLarge;

      public OceanRuinPiece(StructureTemplateManager structuretemplatemanager, ResourceLocation resourcelocation, BlockPos blockpos, Rotation rotation, float f, OceanRuinStructure.Type oceanruinstructure_type, boolean flag) {
         super(StructurePieceType.OCEAN_RUIN, 0, structuretemplatemanager, resourcelocation, resourcelocation.toString(), makeSettings(rotation, f, oceanruinstructure_type), blockpos);
         this.integrity = f;
         this.biomeType = oceanruinstructure_type;
         this.isLarge = flag;
      }

      private OceanRuinPiece(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag, Rotation rotation, float f, OceanRuinStructure.Type oceanruinstructure_type, boolean flag) {
         super(StructurePieceType.OCEAN_RUIN, compoundtag, structuretemplatemanager, (resourcelocation) -> makeSettings(rotation, f, oceanruinstructure_type));
         this.integrity = f;
         this.biomeType = oceanruinstructure_type;
         this.isLarge = flag;
      }

      private static StructurePlaceSettings makeSettings(Rotation rotation, float f, OceanRuinStructure.Type oceanruinstructure_type) {
         StructureProcessor structureprocessor = oceanruinstructure_type == OceanRuinStructure.Type.COLD ? OceanRuinPieces.COLD_SUSPICIOUS_BLOCK_PROCESSOR : OceanRuinPieces.WARM_SUSPICIOUS_BLOCK_PROCESSOR;
         return (new StructurePlaceSettings()).setRotation(rotation).setMirror(Mirror.NONE).addProcessor(new BlockRotProcessor(f)).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).addProcessor(structureprocessor);
      }

      public static OceanRuinPieces.OceanRuinPiece create(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag) {
         Rotation rotation = Rotation.valueOf(compoundtag.getString("Rot"));
         float f = compoundtag.getFloat("Integrity");
         OceanRuinStructure.Type oceanruinstructure_type = OceanRuinStructure.Type.valueOf(compoundtag.getString("BiomeType"));
         boolean flag = compoundtag.getBoolean("IsLarge");
         return new OceanRuinPieces.OceanRuinPiece(structuretemplatemanager, compoundtag, rotation, f, oceanruinstructure_type, flag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putString("Rot", this.placeSettings.getRotation().name());
         compoundtag.putFloat("Integrity", this.integrity);
         compoundtag.putString("BiomeType", this.biomeType.toString());
         compoundtag.putBoolean("IsLarge", this.isLarge);
      }

      protected void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox) {
         if ("chest".equals(s)) {
            serverlevelaccessor.setBlock(blockpos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, Boolean.valueOf(serverlevelaccessor.getFluidState(blockpos).is(FluidTags.WATER))), 2);
            BlockEntity blockentity = serverlevelaccessor.getBlockEntity(blockpos);
            if (blockentity instanceof ChestBlockEntity) {
               ((ChestBlockEntity)blockentity).setLootTable(this.isLarge ? BuiltInLootTables.UNDERWATER_RUIN_BIG : BuiltInLootTables.UNDERWATER_RUIN_SMALL, randomsource.nextLong());
            }
         } else if ("drowned".equals(s)) {
            Drowned drowned = EntityType.DROWNED.create(serverlevelaccessor.getLevel());
            if (drowned != null) {
               drowned.setPersistenceRequired();
               drowned.moveTo(blockpos, 0.0F, 0.0F);
               drowned.finalizeSpawn(serverlevelaccessor, serverlevelaccessor.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
               serverlevelaccessor.addFreshEntityWithPassengers(drowned);
               if (blockpos.getY() > serverlevelaccessor.getSeaLevel()) {
                  serverlevelaccessor.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 2);
               } else {
                  serverlevelaccessor.setBlock(blockpos, Blocks.WATER.defaultBlockState(), 2);
               }
            }
         }

      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         int i = worldgenlevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
         this.templatePosition = new BlockPos(this.templatePosition.getX(), i, this.templatePosition.getZ());
         BlockPos blockpos1 = StructureTemplate.transform(new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.placeSettings.getRotation(), BlockPos.ZERO).offset(this.templatePosition);
         this.templatePosition = new BlockPos(this.templatePosition.getX(), this.getHeight(this.templatePosition, worldgenlevel, blockpos1), this.templatePosition.getZ());
         super.postProcess(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, blockpos);
      }

      private int getHeight(BlockPos blockpos, BlockGetter blockgetter, BlockPos blockpos1) {
         int i = blockpos.getY();
         int j = 512;
         int k = i - 1;
         int l = 0;

         for(BlockPos blockpos2 : BlockPos.betweenClosed(blockpos, blockpos1)) {
            int i1 = blockpos2.getX();
            int j1 = blockpos2.getZ();
            int k1 = blockpos.getY() - 1;
            BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i1, k1, j1);
            BlockState blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);

            for(FluidState fluidstate = blockgetter.getFluidState(blockpos_mutableblockpos); (blockstate.isAir() || fluidstate.is(FluidTags.WATER) || blockstate.is(BlockTags.ICE)) && k1 > blockgetter.getMinBuildHeight() + 1; fluidstate = blockgetter.getFluidState(blockpos_mutableblockpos)) {
               --k1;
               blockpos_mutableblockpos.set(i1, k1, j1);
               blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);
            }

            j = Math.min(j, k1);
            if (k1 < k - 2) {
               ++l;
            }
         }

         int l1 = Math.abs(blockpos.getX() - blockpos1.getX());
         if (k - j > 2 && l > l1 - 2) {
            i = j + 1;
         }

         return i;
      }
   }
}
