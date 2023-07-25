package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class SwampHutPiece extends ScatteredFeaturePiece {
   private boolean spawnedWitch;
   private boolean spawnedCat;

   public SwampHutPiece(RandomSource randomsource, int i, int j) {
      super(StructurePieceType.SWAMPLAND_HUT, i, 64, j, 7, 7, 9, getRandomHorizontalDirection(randomsource));
   }

   public SwampHutPiece(CompoundTag compoundtag) {
      super(StructurePieceType.SWAMPLAND_HUT, compoundtag);
      this.spawnedWitch = compoundtag.getBoolean("Witch");
      this.spawnedCat = compoundtag.getBoolean("Cat");
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
      compoundtag.putBoolean("Witch", this.spawnedWitch);
      compoundtag.putBoolean("Cat", this.spawnedCat);
   }

   public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
      if (this.updateAverageGroundHeight(worldgenlevel, boundingbox, 0)) {
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 1, 3, 4, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 5, 3, 4, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 5, 3, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, boundingbox);
         BlockState blockstate = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         BlockState blockstate1 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate2 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         BlockState blockstate3 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         this.generateBox(worldgenlevel, boundingbox, 0, 4, 1, 6, 4, 1, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 4, 2, 0, 4, 7, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 4, 2, 6, 4, 7, blockstate2, blockstate2, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 4, 8, 6, 4, 8, blockstate3, blockstate3, false);
         this.placeBlock(worldgenlevel, blockstate.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, boundingbox);
         this.placeBlock(worldgenlevel, blockstate.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, boundingbox);

         for(int i = 2; i <= 7; i += 5) {
            for(int j = 1; j <= 5; j += 4) {
               this.fillColumnDown(worldgenlevel, Blocks.OAK_LOG.defaultBlockState(), j, -1, i, boundingbox);
            }
         }

         if (!this.spawnedWitch) {
            BlockPos blockpos1 = this.getWorldPos(2, 2, 5);
            if (boundingbox.isInside(blockpos1)) {
               this.spawnedWitch = true;
               Witch witch = EntityType.WITCH.create(worldgenlevel.getLevel());
               if (witch != null) {
                  witch.setPersistenceRequired();
                  witch.moveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
                  witch.finalizeSpawn(worldgenlevel, worldgenlevel.getCurrentDifficultyAt(blockpos1), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
                  worldgenlevel.addFreshEntityWithPassengers(witch);
               }
            }
         }

         this.spawnCat(worldgenlevel, boundingbox);
      }
   }

   private void spawnCat(ServerLevelAccessor serverlevelaccessor, BoundingBox boundingbox) {
      if (!this.spawnedCat) {
         BlockPos blockpos = this.getWorldPos(2, 2, 5);
         if (boundingbox.isInside(blockpos)) {
            this.spawnedCat = true;
            Cat cat = EntityType.CAT.create(serverlevelaccessor.getLevel());
            if (cat != null) {
               cat.setPersistenceRequired();
               cat.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
               cat.finalizeSpawn(serverlevelaccessor, serverlevelaccessor.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
               serverlevelaccessor.addFreshEntityWithPassengers(cat);
            }
         }
      }

   }
}
