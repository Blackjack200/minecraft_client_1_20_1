package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public abstract class StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected BoundingBox boundingBox;
   @Nullable
   private Direction orientation;
   private Mirror mirror;
   private Rotation rotation;
   protected int genDepth;
   private final StructurePieceType type;
   private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();

   protected StructurePiece(StructurePieceType structurepiecetype, int i, BoundingBox boundingbox) {
      this.type = structurepiecetype;
      this.genDepth = i;
      this.boundingBox = boundingbox;
   }

   public StructurePiece(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
      this(structurepiecetype, compoundtag.getInt("GD"), BoundingBox.CODEC.parse(NbtOps.INSTANCE, compoundtag.get("BB")).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid boundingbox")));
      int i = compoundtag.getInt("O");
      this.setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
   }

   protected static BoundingBox makeBoundingBox(int i, int j, int k, Direction direction, int l, int i1, int j1) {
      return direction.getAxis() == Direction.Axis.Z ? new BoundingBox(i, j, k, i + l - 1, j + i1 - 1, k + j1 - 1) : new BoundingBox(i, j, k, i + j1 - 1, j + i1 - 1, k + l - 1);
   }

   protected static Direction getRandomHorizontalDirection(RandomSource randomsource) {
      return Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
   }

   public final CompoundTag createTag(StructurePieceSerializationContext structurepieceserializationcontext) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("id", BuiltInRegistries.STRUCTURE_PIECE.getKey(this.getType()).toString());
      BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("BB", tag));
      Direction direction = this.getOrientation();
      compoundtag.putInt("O", direction == null ? -1 : direction.get2DDataValue());
      compoundtag.putInt("GD", this.genDepth);
      this.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
      return compoundtag;
   }

   protected abstract void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag);

   public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
   }

   public abstract void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos);

   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getGenDepth() {
      return this.genDepth;
   }

   public void setGenDepth(int i) {
      this.genDepth = i;
   }

   public boolean isCloseToChunk(ChunkPos chunkpos, int i) {
      int j = chunkpos.getMinBlockX();
      int k = chunkpos.getMinBlockZ();
      return this.boundingBox.intersects(j - i, k - i, j + 15 + i, k + 15 + i);
   }

   public BlockPos getLocatorPosition() {
      return new BlockPos(this.boundingBox.getCenter());
   }

   protected BlockPos.MutableBlockPos getWorldPos(int i, int j, int k) {
      return new BlockPos.MutableBlockPos(this.getWorldX(i, k), this.getWorldY(j), this.getWorldZ(i, k));
   }

   protected int getWorldX(int i, int j) {
      Direction direction = this.getOrientation();
      if (direction == null) {
         return i;
      } else {
         switch (direction) {
            case NORTH:
            case SOUTH:
               return this.boundingBox.minX() + i;
            case WEST:
               return this.boundingBox.maxX() - j;
            case EAST:
               return this.boundingBox.minX() + j;
            default:
               return i;
         }
      }
   }

   protected int getWorldY(int i) {
      return this.getOrientation() == null ? i : i + this.boundingBox.minY();
   }

   protected int getWorldZ(int i, int j) {
      Direction direction = this.getOrientation();
      if (direction == null) {
         return j;
      } else {
         switch (direction) {
            case NORTH:
               return this.boundingBox.maxZ() - j;
            case SOUTH:
               return this.boundingBox.minZ() + j;
            case WEST:
            case EAST:
               return this.boundingBox.minZ() + i;
            default:
               return j;
         }
      }
   }

   protected void placeBlock(WorldGenLevel worldgenlevel, BlockState blockstate, int i, int j, int k, BoundingBox boundingbox) {
      BlockPos blockpos = this.getWorldPos(i, j, k);
      if (boundingbox.isInside(blockpos)) {
         if (this.canBeReplaced(worldgenlevel, i, j, k, boundingbox)) {
            if (this.mirror != Mirror.NONE) {
               blockstate = blockstate.mirror(this.mirror);
            }

            if (this.rotation != Rotation.NONE) {
               blockstate = blockstate.rotate(this.rotation);
            }

            worldgenlevel.setBlock(blockpos, blockstate, 2);
            FluidState fluidstate = worldgenlevel.getFluidState(blockpos);
            if (!fluidstate.isEmpty()) {
               worldgenlevel.scheduleTick(blockpos, fluidstate.getType(), 0);
            }

            if (SHAPE_CHECK_BLOCKS.contains(blockstate.getBlock())) {
               worldgenlevel.getChunk(blockpos).markPosForPostprocessing(blockpos);
            }

         }
      }
   }

   protected boolean canBeReplaced(LevelReader levelreader, int i, int j, int k, BoundingBox boundingbox) {
      return true;
   }

   protected BlockState getBlock(BlockGetter blockgetter, int i, int j, int k, BoundingBox boundingbox) {
      BlockPos blockpos = this.getWorldPos(i, j, k);
      return !boundingbox.isInside(blockpos) ? Blocks.AIR.defaultBlockState() : blockgetter.getBlockState(blockpos);
   }

   protected boolean isInterior(LevelReader levelreader, int i, int j, int k, BoundingBox boundingbox) {
      BlockPos blockpos = this.getWorldPos(i, j + 1, k);
      if (!boundingbox.isInside(blockpos)) {
         return false;
      } else {
         return blockpos.getY() < levelreader.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockpos.getX(), blockpos.getZ());
      }
   }

   protected void generateAirBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, int j1) {
      for(int k1 = j; k1 <= i1; ++k1) {
         for(int l1 = i; l1 <= l; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), l1, k1, i2, boundingbox);
            }
         }
      }

   }

   protected void generateBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, int j1, BlockState blockstate, BlockState blockstate1, boolean flag) {
      for(int k1 = j; k1 <= i1; ++k1) {
         for(int l1 = i; l1 <= l; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               if (!flag || !this.getBlock(worldgenlevel, l1, k1, i2, boundingbox).isAir()) {
                  if (k1 != j && k1 != i1 && l1 != i && l1 != l && i2 != k && i2 != j1) {
                     this.placeBlock(worldgenlevel, blockstate1, l1, k1, i2, boundingbox);
                  } else {
                     this.placeBlock(worldgenlevel, blockstate, l1, k1, i2, boundingbox);
                  }
               }
            }
         }
      }

   }

   protected void generateBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, BoundingBox boundingbox1, BlockState blockstate, BlockState blockstate1, boolean flag) {
      this.generateBox(worldgenlevel, boundingbox, boundingbox1.minX(), boundingbox1.minY(), boundingbox1.minZ(), boundingbox1.maxX(), boundingbox1.maxY(), boundingbox1.maxZ(), blockstate, blockstate1, flag);
   }

   protected void generateBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, int j1, boolean flag, RandomSource randomsource, StructurePiece.BlockSelector structurepiece_blockselector) {
      for(int k1 = j; k1 <= i1; ++k1) {
         for(int l1 = i; l1 <= l; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               if (!flag || !this.getBlock(worldgenlevel, l1, k1, i2, boundingbox).isAir()) {
                  structurepiece_blockselector.next(randomsource, l1, k1, i2, k1 == j || k1 == i1 || l1 == i || l1 == l || i2 == k || i2 == j1);
                  this.placeBlock(worldgenlevel, structurepiece_blockselector.getNext(), l1, k1, i2, boundingbox);
               }
            }
         }
      }

   }

   protected void generateBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, BoundingBox boundingbox1, boolean flag, RandomSource randomsource, StructurePiece.BlockSelector structurepiece_blockselector) {
      this.generateBox(worldgenlevel, boundingbox, boundingbox1.minX(), boundingbox1.minY(), boundingbox1.minZ(), boundingbox1.maxX(), boundingbox1.maxY(), boundingbox1.maxZ(), flag, randomsource, structurepiece_blockselector);
   }

   protected void generateMaybeBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, RandomSource randomsource, float f, int i, int j, int k, int l, int i1, int j1, BlockState blockstate, BlockState blockstate1, boolean flag, boolean flag1) {
      for(int k1 = j; k1 <= i1; ++k1) {
         for(int l1 = i; l1 <= l; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               if (!(randomsource.nextFloat() > f) && (!flag || !this.getBlock(worldgenlevel, l1, k1, i2, boundingbox).isAir()) && (!flag1 || this.isInterior(worldgenlevel, l1, k1, i2, boundingbox))) {
                  if (k1 != j && k1 != i1 && l1 != i && l1 != l && i2 != k && i2 != j1) {
                     this.placeBlock(worldgenlevel, blockstate1, l1, k1, i2, boundingbox);
                  } else {
                     this.placeBlock(worldgenlevel, blockstate, l1, k1, i2, boundingbox);
                  }
               }
            }
         }
      }

   }

   protected void maybeGenerateBlock(WorldGenLevel worldgenlevel, BoundingBox boundingbox, RandomSource randomsource, float f, int i, int j, int k, BlockState blockstate) {
      if (randomsource.nextFloat() < f) {
         this.placeBlock(worldgenlevel, blockstate, i, j, k, boundingbox);
      }

   }

   protected void generateUpperHalfSphere(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, int j1, BlockState blockstate, boolean flag) {
      float f = (float)(l - i + 1);
      float f1 = (float)(i1 - j + 1);
      float f2 = (float)(j1 - k + 1);
      float f3 = (float)i + f / 2.0F;
      float f4 = (float)k + f2 / 2.0F;

      for(int k1 = j; k1 <= i1; ++k1) {
         float f5 = (float)(k1 - j) / f1;

         for(int l1 = i; l1 <= l; ++l1) {
            float f6 = ((float)l1 - f3) / (f * 0.5F);

            for(int i2 = k; i2 <= j1; ++i2) {
               float f7 = ((float)i2 - f4) / (f2 * 0.5F);
               if (!flag || !this.getBlock(worldgenlevel, l1, k1, i2, boundingbox).isAir()) {
                  float f8 = f6 * f6 + f5 * f5 + f7 * f7;
                  if (f8 <= 1.05F) {
                     this.placeBlock(worldgenlevel, blockstate, l1, k1, i2, boundingbox);
                  }
               }
            }
         }
      }

   }

   protected void fillColumnDown(WorldGenLevel worldgenlevel, BlockState blockstate, int i, int j, int k, BoundingBox boundingbox) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = this.getWorldPos(i, j, k);
      if (boundingbox.isInside(blockpos_mutableblockpos)) {
         while(this.isReplaceableByStructures(worldgenlevel.getBlockState(blockpos_mutableblockpos)) && blockpos_mutableblockpos.getY() > worldgenlevel.getMinBuildHeight() + 1) {
            worldgenlevel.setBlock(blockpos_mutableblockpos, blockstate, 2);
            blockpos_mutableblockpos.move(Direction.DOWN);
         }

      }
   }

   protected boolean isReplaceableByStructures(BlockState blockstate) {
      return blockstate.isAir() || blockstate.liquid() || blockstate.is(Blocks.GLOW_LICHEN) || blockstate.is(Blocks.SEAGRASS) || blockstate.is(Blocks.TALL_SEAGRASS);
   }

   protected boolean createChest(WorldGenLevel worldgenlevel, BoundingBox boundingbox, RandomSource randomsource, int i, int j, int k, ResourceLocation resourcelocation) {
      return this.createChest(worldgenlevel, boundingbox, randomsource, this.getWorldPos(i, j, k), resourcelocation, (BlockState)null);
   }

   public static BlockState reorient(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      Direction direction = null;

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction1);
         BlockState blockstate1 = blockgetter.getBlockState(blockpos1);
         if (blockstate1.is(Blocks.CHEST)) {
            return blockstate;
         }

         if (blockstate1.isSolidRender(blockgetter, blockpos1)) {
            if (direction != null) {
               direction = null;
               break;
            }

            direction = direction1;
         }
      }

      if (direction != null) {
         return blockstate.setValue(HorizontalDirectionalBlock.FACING, direction.getOpposite());
      } else {
         Direction direction2 = blockstate.getValue(HorizontalDirectionalBlock.FACING);
         BlockPos blockpos2 = blockpos.relative(direction2);
         if (blockgetter.getBlockState(blockpos2).isSolidRender(blockgetter, blockpos2)) {
            direction2 = direction2.getOpposite();
            blockpos2 = blockpos.relative(direction2);
         }

         if (blockgetter.getBlockState(blockpos2).isSolidRender(blockgetter, blockpos2)) {
            direction2 = direction2.getClockWise();
            blockpos2 = blockpos.relative(direction2);
         }

         if (blockgetter.getBlockState(blockpos2).isSolidRender(blockgetter, blockpos2)) {
            direction2 = direction2.getOpposite();
            blockpos.relative(direction2);
         }

         return blockstate.setValue(HorizontalDirectionalBlock.FACING, direction2);
      }
   }

   protected boolean createChest(ServerLevelAccessor serverlevelaccessor, BoundingBox boundingbox, RandomSource randomsource, BlockPos blockpos, ResourceLocation resourcelocation, @Nullable BlockState blockstate) {
      if (boundingbox.isInside(blockpos) && !serverlevelaccessor.getBlockState(blockpos).is(Blocks.CHEST)) {
         if (blockstate == null) {
            blockstate = reorient(serverlevelaccessor, blockpos, Blocks.CHEST.defaultBlockState());
         }

         serverlevelaccessor.setBlock(blockpos, blockstate, 2);
         BlockEntity blockentity = serverlevelaccessor.getBlockEntity(blockpos);
         if (blockentity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockentity).setLootTable(resourcelocation, randomsource.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean createDispenser(WorldGenLevel worldgenlevel, BoundingBox boundingbox, RandomSource randomsource, int i, int j, int k, Direction direction, ResourceLocation resourcelocation) {
      BlockPos blockpos = this.getWorldPos(i, j, k);
      if (boundingbox.isInside(blockpos) && !worldgenlevel.getBlockState(blockpos).is(Blocks.DISPENSER)) {
         this.placeBlock(worldgenlevel, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, direction), i, j, k, boundingbox);
         BlockEntity blockentity = worldgenlevel.getBlockEntity(blockpos);
         if (blockentity instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)blockentity).setLootTable(resourcelocation, randomsource.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   public void move(int i, int j, int k) {
      this.boundingBox.move(i, j, k);
   }

   public static BoundingBox createBoundingBox(Stream<StructurePiece> stream) {
      return BoundingBox.encapsulatingBoxes(stream.map(StructurePiece::getBoundingBox)::iterator).orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox without pieces"));
   }

   @Nullable
   public static StructurePiece findCollisionPiece(List<StructurePiece> list, BoundingBox boundingbox) {
      for(StructurePiece structurepiece : list) {
         if (structurepiece.getBoundingBox().intersects(boundingbox)) {
            return structurepiece;
         }
      }

      return null;
   }

   @Nullable
   public Direction getOrientation() {
      return this.orientation;
   }

   public void setOrientation(@Nullable Direction direction) {
      this.orientation = direction;
      if (direction == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch (direction) {
            case SOUTH:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.NONE;
               break;
            case WEST:
               this.mirror = Mirror.LEFT_RIGHT;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            case EAST:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.CLOCKWISE_90;
               break;
            default:
               this.mirror = Mirror.NONE;
               this.rotation = Rotation.NONE;
         }
      }

   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public StructurePieceType getType() {
      return this.type;
   }

   public abstract static class BlockSelector {
      protected BlockState next = Blocks.AIR.defaultBlockState();

      public abstract void next(RandomSource randomsource, int i, int j, int k, boolean flag);

      public BlockState getNext() {
         return this.next;
      }
   }
}
