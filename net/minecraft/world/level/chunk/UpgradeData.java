package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public class UpgradeData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final UpgradeData EMPTY = new UpgradeData(EmptyBlockGetter.INSTANCE);
   private static final String TAG_INDICES = "Indices";
   private static final Direction8[] DIRECTIONS = Direction8.values();
   private final EnumSet<Direction8> sides = EnumSet.noneOf(Direction8.class);
   private final List<SavedTick<Block>> neighborBlockTicks = Lists.newArrayList();
   private final List<SavedTick<Fluid>> neighborFluidTicks = Lists.newArrayList();
   private final int[][] index;
   static final Map<Block, UpgradeData.BlockFixer> MAP = new IdentityHashMap<>();
   static final Set<UpgradeData.BlockFixer> CHUNKY_FIXERS = Sets.newHashSet();

   private UpgradeData(LevelHeightAccessor levelheightaccessor) {
      this.index = new int[levelheightaccessor.getSectionsCount()][];
   }

   public UpgradeData(CompoundTag compoundtag, LevelHeightAccessor levelheightaccessor) {
      this(levelheightaccessor);
      if (compoundtag.contains("Indices", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("Indices");

         for(int i = 0; i < this.index.length; ++i) {
            String s = String.valueOf(i);
            if (compoundtag1.contains(s, 11)) {
               this.index[i] = compoundtag1.getIntArray(s);
            }
         }
      }

      int j = compoundtag.getInt("Sides");

      for(Direction8 direction8 : Direction8.values()) {
         if ((j & 1 << direction8.ordinal()) != 0) {
            this.sides.add(direction8);
         }
      }

      loadTicks(compoundtag, "neighbor_block_ticks", (s2) -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(s2)).or(() -> Optional.of(Blocks.AIR)), this.neighborBlockTicks);
      loadTicks(compoundtag, "neighbor_fluid_ticks", (s1) -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(s1)).or(() -> Optional.of(Fluids.EMPTY)), this.neighborFluidTicks);
   }

   private static <T> void loadTicks(CompoundTag compoundtag, String s, Function<String, Optional<T>> function, List<SavedTick<T>> list) {
      if (compoundtag.contains(s, 9)) {
         for(Tag tag : compoundtag.getList(s, 10)) {
            SavedTick.loadTick((CompoundTag)tag, function).ifPresent(list::add);
         }
      }

   }

   public void upgrade(LevelChunk levelchunk) {
      this.upgradeInside(levelchunk);

      for(Direction8 direction8 : DIRECTIONS) {
         upgradeSides(levelchunk, direction8);
      }

      Level level = levelchunk.getLevel();
      this.neighborBlockTicks.forEach((savedtick1) -> {
         Block block = savedtick1.type() == Blocks.AIR ? level.getBlockState(savedtick1.pos()).getBlock() : savedtick1.type();
         level.scheduleTick(savedtick1.pos(), block, savedtick1.delay(), savedtick1.priority());
      });
      this.neighborFluidTicks.forEach((savedtick) -> {
         Fluid fluid = savedtick.type() == Fluids.EMPTY ? level.getFluidState(savedtick.pos()).getType() : savedtick.type();
         level.scheduleTick(savedtick.pos(), fluid, savedtick.delay(), savedtick.priority());
      });
      CHUNKY_FIXERS.forEach((upgradedata_blockfixer) -> upgradedata_blockfixer.processChunk(level));
   }

   private static void upgradeSides(LevelChunk levelchunk, Direction8 direction8) {
      Level level = levelchunk.getLevel();
      if (levelchunk.getUpgradeData().sides.remove(direction8)) {
         Set<Direction> set = direction8.getDirections();
         int i = 0;
         int j = 15;
         boolean flag = set.contains(Direction.EAST);
         boolean flag1 = set.contains(Direction.WEST);
         boolean flag2 = set.contains(Direction.SOUTH);
         boolean flag3 = set.contains(Direction.NORTH);
         boolean flag4 = set.size() == 1;
         ChunkPos chunkpos = levelchunk.getPos();
         int k = chunkpos.getMinBlockX() + (!flag4 || !flag3 && !flag2 ? (flag1 ? 0 : 15) : 1);
         int l = chunkpos.getMinBlockX() + (!flag4 || !flag3 && !flag2 ? (flag1 ? 0 : 15) : 14);
         int i1 = chunkpos.getMinBlockZ() + (!flag4 || !flag && !flag1 ? (flag3 ? 0 : 15) : 1);
         int j1 = chunkpos.getMinBlockZ() + (!flag4 || !flag && !flag1 ? (flag3 ? 0 : 15) : 14);
         Direction[] adirection = Direction.values();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(BlockPos blockpos : BlockPos.betweenClosed(k, level.getMinBuildHeight(), i1, l, level.getMaxBuildHeight() - 1, j1)) {
            BlockState blockstate = level.getBlockState(blockpos);
            BlockState blockstate1 = blockstate;

            for(Direction direction : adirection) {
               blockpos_mutableblockpos.setWithOffset(blockpos, direction);
               blockstate1 = updateState(blockstate1, direction, level, blockpos, blockpos_mutableblockpos);
            }

            Block.updateOrDestroy(blockstate, blockstate1, level, blockpos, 18);
         }

      }
   }

   private static BlockState updateState(BlockState blockstate, Direction direction, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return MAP.getOrDefault(blockstate.getBlock(), UpgradeData.BlockFixers.DEFAULT).updateShape(blockstate, direction, levelaccessor.getBlockState(blockpos1), levelaccessor, blockpos, blockpos1);
   }

   private void upgradeInside(LevelChunk levelchunk) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = new BlockPos.MutableBlockPos();
      ChunkPos chunkpos = levelchunk.getPos();
      LevelAccessor levelaccessor = levelchunk.getLevel();

      for(int i = 0; i < this.index.length; ++i) {
         LevelChunkSection levelchunksection = levelchunk.getSection(i);
         int[] aint = this.index[i];
         this.index[i] = null;
         if (aint != null && aint.length > 0) {
            Direction[] adirection = Direction.values();
            PalettedContainer<BlockState> palettedcontainer = levelchunksection.getStates();
            int j = levelchunk.getSectionYFromSectionIndex(i);
            int k = SectionPos.sectionToBlockCoord(j);

            for(int l : aint) {
               int i1 = l & 15;
               int j1 = l >> 8 & 15;
               int k1 = l >> 4 & 15;
               blockpos_mutableblockpos.set(chunkpos.getMinBlockX() + i1, k + j1, chunkpos.getMinBlockZ() + k1);
               BlockState blockstate = palettedcontainer.get(l);
               BlockState blockstate1 = blockstate;

               for(Direction direction : adirection) {
                  blockpos_mutableblockpos1.setWithOffset(blockpos_mutableblockpos, direction);
                  if (SectionPos.blockToSectionCoord(blockpos_mutableblockpos.getX()) == chunkpos.x && SectionPos.blockToSectionCoord(blockpos_mutableblockpos.getZ()) == chunkpos.z) {
                     blockstate1 = updateState(blockstate1, direction, levelaccessor, blockpos_mutableblockpos, blockpos_mutableblockpos1);
                  }
               }

               Block.updateOrDestroy(blockstate, blockstate1, levelaccessor, blockpos_mutableblockpos, 18);
            }
         }
      }

      for(int l1 = 0; l1 < this.index.length; ++l1) {
         if (this.index[l1] != null) {
            LOGGER.warn("Discarding update data for section {} for chunk ({} {})", levelaccessor.getSectionYFromSectionIndex(l1), chunkpos.x, chunkpos.z);
         }

         this.index[l1] = null;
      }

   }

   public boolean isEmpty() {
      for(int[] aint : this.index) {
         if (aint != null) {
            return false;
         }
      }

      return this.sides.isEmpty();
   }

   public CompoundTag write() {
      CompoundTag compoundtag = new CompoundTag();
      CompoundTag compoundtag1 = new CompoundTag();

      for(int i = 0; i < this.index.length; ++i) {
         String s = String.valueOf(i);
         if (this.index[i] != null && this.index[i].length != 0) {
            compoundtag1.putIntArray(s, this.index[i]);
         }
      }

      if (!compoundtag1.isEmpty()) {
         compoundtag.put("Indices", compoundtag1);
      }

      int j = 0;

      for(Direction8 direction8 : this.sides) {
         j |= 1 << direction8.ordinal();
      }

      compoundtag.putByte("Sides", (byte)j);
      if (!this.neighborBlockTicks.isEmpty()) {
         ListTag listtag = new ListTag();
         this.neighborBlockTicks.forEach((savedtick1) -> listtag.add(savedtick1.save((block) -> BuiltInRegistries.BLOCK.getKey(block).toString())));
         compoundtag.put("neighbor_block_ticks", listtag);
      }

      if (!this.neighborFluidTicks.isEmpty()) {
         ListTag listtag1 = new ListTag();
         this.neighborFluidTicks.forEach((savedtick) -> listtag1.add(savedtick.save((fluid) -> BuiltInRegistries.FLUID.getKey(fluid).toString())));
         compoundtag.put("neighbor_fluid_ticks", listtag1);
      }

      return compoundtag;
   }

   public interface BlockFixer {
      BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1);

      default void processChunk(LevelAccessor levelaccessor) {
      }
   }

   static enum BlockFixers implements UpgradeData.BlockFixer {
      BLACKLIST(Blocks.OBSERVER, Blocks.NETHER_PORTAL, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.DRAGON_EGG, Blocks.GRAVEL, Blocks.SAND, Blocks.RED_SAND, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN) {
         public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
            return blockstate;
         }
      },
      DEFAULT {
         public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
            return blockstate.updateShape(direction, levelaccessor.getBlockState(blockpos1), levelaccessor, blockpos, blockpos1);
         }
      },
      CHEST(Blocks.CHEST, Blocks.TRAPPED_CHEST) {
         public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
            if (blockstate1.is(blockstate.getBlock()) && direction.getAxis().isHorizontal() && blockstate.getValue(ChestBlock.TYPE) == ChestType.SINGLE && blockstate1.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
               Direction direction1 = blockstate.getValue(ChestBlock.FACING);
               if (direction.getAxis() != direction1.getAxis() && direction1 == blockstate1.getValue(ChestBlock.FACING)) {
                  ChestType chesttype = direction == direction1.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                  levelaccessor.setBlock(blockpos1, blockstate1.setValue(ChestBlock.TYPE, chesttype.getOpposite()), 18);
                  if (direction1 == Direction.NORTH || direction1 == Direction.EAST) {
                     BlockEntity blockentity = levelaccessor.getBlockEntity(blockpos);
                     BlockEntity blockentity1 = levelaccessor.getBlockEntity(blockpos1);
                     if (blockentity instanceof ChestBlockEntity && blockentity1 instanceof ChestBlockEntity) {
                        ChestBlockEntity.swapContents((ChestBlockEntity)blockentity, (ChestBlockEntity)blockentity1);
                     }
                  }

                  return blockstate.setValue(ChestBlock.TYPE, chesttype);
               }
            }

            return blockstate;
         }
      },
      LEAVES(true, Blocks.ACACIA_LEAVES, Blocks.CHERRY_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES) {
         private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity(7));

         public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
            BlockState blockstate2 = blockstate.updateShape(direction, levelaccessor.getBlockState(blockpos1), levelaccessor, blockpos, blockpos1);
            if (blockstate != blockstate2) {
               int i = blockstate2.getValue(BlockStateProperties.DISTANCE);
               List<ObjectSet<BlockPos>> list = this.queue.get();
               if (list.isEmpty()) {
                  for(int j = 0; j < 7; ++j) {
                     list.add(new ObjectOpenHashSet<>());
                  }
               }

               list.get(i).add(blockpos.immutable());
            }

            return blockstate;
         }

         public void processChunk(LevelAccessor levelaccessor) {
            BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
            List<ObjectSet<BlockPos>> list = this.queue.get();

            for(int i = 2; i < list.size(); ++i) {
               int j = i - 1;
               ObjectSet<BlockPos> objectset = list.get(j);
               ObjectSet<BlockPos> objectset1 = list.get(i);

               for(BlockPos blockpos : objectset) {
                  BlockState blockstate = levelaccessor.getBlockState(blockpos);
                  if (blockstate.getValue(BlockStateProperties.DISTANCE) >= j) {
                     levelaccessor.setBlock(blockpos, blockstate.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(j)), 18);
                     if (i != 7) {
                        for(Direction direction : f) {
                           blockpos_mutableblockpos.setWithOffset(blockpos, direction);
                           BlockState blockstate1 = levelaccessor.getBlockState(blockpos_mutableblockpos);
                           if (blockstate1.hasProperty(BlockStateProperties.DISTANCE) && blockstate.getValue(BlockStateProperties.DISTANCE) > i) {
                              objectset1.add(blockpos_mutableblockpos.immutable());
                           }
                        }
                     }
                  }
               }
            }

            list.clear();
         }
      },
      STEM_BLOCK(Blocks.MELON_STEM, Blocks.PUMPKIN_STEM) {
         public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
            if (blockstate.getValue(StemBlock.AGE) == 7) {
               StemGrownBlock stemgrownblock = ((StemBlock)blockstate.getBlock()).getFruit();
               if (blockstate1.is(stemgrownblock)) {
                  return stemgrownblock.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction);
               }
            }

            return blockstate;
         }
      };

      public static final Direction[] DIRECTIONS = Direction.values();

      BlockFixers(Block... ablock) {
         this(false, ablock);
      }

      BlockFixers(boolean flag, Block... ablock) {
         for(Block block : ablock) {
            UpgradeData.MAP.put(block, this);
         }

         if (flag) {
            UpgradeData.CHUNKY_FIXERS.add(this);
         }

      }
   }
}
