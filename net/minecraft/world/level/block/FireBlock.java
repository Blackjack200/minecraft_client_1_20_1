package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FireBlock extends BaseFireBlock {
   public static final int MAX_AGE = 15;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty UP = PipeBlock.UP;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((map_entry) -> map_entry.getKey() != Direction.DOWN).collect(Util.toMap());
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private final Map<BlockState, VoxelShape> shapesCache;
   private static final int IGNITE_INSTANT = 60;
   private static final int IGNITE_EASY = 30;
   private static final int IGNITE_MEDIUM = 15;
   private static final int IGNITE_HARD = 5;
   private static final int BURN_INSTANT = 100;
   private static final int BURN_EASY = 60;
   private static final int BURN_MEDIUM = 20;
   private static final int BURN_HARD = 5;
   private final Object2IntMap<Block> igniteOdds = new Object2IntOpenHashMap<>();
   private final Object2IntMap<Block> burnOdds = new Object2IntOpenHashMap<>();

   public FireBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, 1.0F);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false)));
      this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().filter((blockstate1) -> blockstate1.getValue(AGE) == 0).collect(Collectors.toMap(Function.identity(), FireBlock::calculateShape)));
   }

   private static VoxelShape calculateShape(BlockState blockstate) {
      VoxelShape voxelshape = Shapes.empty();
      if (blockstate.getValue(UP)) {
         voxelshape = UP_AABB;
      }

      if (blockstate.getValue(NORTH)) {
         voxelshape = Shapes.or(voxelshape, NORTH_AABB);
      }

      if (blockstate.getValue(SOUTH)) {
         voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
      }

      if (blockstate.getValue(EAST)) {
         voxelshape = Shapes.or(voxelshape, EAST_AABB);
      }

      if (blockstate.getValue(WEST)) {
         voxelshape = Shapes.or(voxelshape, WEST_AABB);
      }

      return voxelshape.isEmpty() ? DOWN_AABB : voxelshape;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return this.canSurvive(blockstate, levelaccessor, blockpos) ? this.getStateWithAge(levelaccessor, blockpos, blockstate.getValue(AGE)) : Blocks.AIR.defaultBlockState();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapesCache.get(blockstate.setValue(AGE, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.getStateForPlacement(blockplacecontext.getLevel(), blockplacecontext.getClickedPos());
   }

   protected BlockState getStateForPlacement(BlockGetter blockgetter, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate = blockgetter.getBlockState(blockpos1);
      if (!this.canBurn(blockstate) && !blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.UP)) {
         BlockState blockstate1 = this.defaultBlockState();

         for(Direction direction : Direction.values()) {
            BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(direction);
            if (booleanproperty != null) {
               blockstate1 = blockstate1.setValue(booleanproperty, Boolean.valueOf(this.canBurn(blockgetter.getBlockState(blockpos.relative(direction)))));
            }
         }

         return blockstate1;
      } else {
         return this.defaultBlockState();
      }
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      return levelreader.getBlockState(blockpos1).isFaceSturdy(levelreader, blockpos1, Direction.UP) || this.isValidFireLocation(levelreader, blockpos);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      serverlevel.scheduleTick(blockpos, this, getFireTickDelay(serverlevel.random));
      if (serverlevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
         if (!blockstate.canSurvive(serverlevel, blockpos)) {
            serverlevel.removeBlock(blockpos, false);
         }

         BlockState blockstate1 = serverlevel.getBlockState(blockpos.below());
         boolean flag = blockstate1.is(serverlevel.dimensionType().infiniburn());
         int i = blockstate.getValue(AGE);
         if (!flag && serverlevel.isRaining() && this.isNearRain(serverlevel, blockpos) && randomsource.nextFloat() < 0.2F + (float)i * 0.03F) {
            serverlevel.removeBlock(blockpos, false);
         } else {
            int j = Math.min(15, i + randomsource.nextInt(3) / 2);
            if (i != j) {
               blockstate = blockstate.setValue(AGE, Integer.valueOf(j));
               serverlevel.setBlock(blockpos, blockstate, 4);
            }

            if (!flag) {
               if (!this.isValidFireLocation(serverlevel, blockpos)) {
                  BlockPos blockpos1 = blockpos.below();
                  if (!serverlevel.getBlockState(blockpos1).isFaceSturdy(serverlevel, blockpos1, Direction.UP) || i > 3) {
                     serverlevel.removeBlock(blockpos, false);
                  }

                  return;
               }

               if (i == 15 && randomsource.nextInt(4) == 0 && !this.canBurn(serverlevel.getBlockState(blockpos.below()))) {
                  serverlevel.removeBlock(blockpos, false);
                  return;
               }
            }

            boolean flag1 = serverlevel.getBiome(blockpos).is(BiomeTags.INCREASED_FIRE_BURNOUT);
            int k = flag1 ? -50 : 0;
            this.checkBurnOut(serverlevel, blockpos.east(), 300 + k, randomsource, i);
            this.checkBurnOut(serverlevel, blockpos.west(), 300 + k, randomsource, i);
            this.checkBurnOut(serverlevel, blockpos.below(), 250 + k, randomsource, i);
            this.checkBurnOut(serverlevel, blockpos.above(), 250 + k, randomsource, i);
            this.checkBurnOut(serverlevel, blockpos.north(), 300 + k, randomsource, i);
            this.checkBurnOut(serverlevel, blockpos.south(), 300 + k, randomsource, i);
            BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

            for(int l = -1; l <= 1; ++l) {
               for(int i1 = -1; i1 <= 1; ++i1) {
                  for(int j1 = -1; j1 <= 4; ++j1) {
                     if (l != 0 || j1 != 0 || i1 != 0) {
                        int k1 = 100;
                        if (j1 > 1) {
                           k1 += (j1 - 1) * 100;
                        }

                        blockpos_mutableblockpos.setWithOffset(blockpos, l, j1, i1);
                        int l1 = this.getIgniteOdds(serverlevel, blockpos_mutableblockpos);
                        if (l1 > 0) {
                           int i2 = (l1 + 40 + serverlevel.getDifficulty().getId() * 7) / (i + 30);
                           if (flag1) {
                              i2 /= 2;
                           }

                           if (i2 > 0 && randomsource.nextInt(k1) <= i2 && (!serverlevel.isRaining() || !this.isNearRain(serverlevel, blockpos_mutableblockpos))) {
                              int j2 = Math.min(15, i + randomsource.nextInt(5) / 4);
                              serverlevel.setBlock(blockpos_mutableblockpos, this.getStateWithAge(serverlevel, blockpos_mutableblockpos, j2), 3);
                           }
                        }
                     }
                  }
               }
            }

         }
      }
   }

   protected boolean isNearRain(Level level, BlockPos blockpos) {
      return level.isRainingAt(blockpos) || level.isRainingAt(blockpos.west()) || level.isRainingAt(blockpos.east()) || level.isRainingAt(blockpos.north()) || level.isRainingAt(blockpos.south());
   }

   private int getBurnOdds(BlockState blockstate) {
      return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate.getValue(BlockStateProperties.WATERLOGGED) ? 0 : this.burnOdds.getInt(blockstate.getBlock());
   }

   private int getIgniteOdds(BlockState blockstate) {
      return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate.getValue(BlockStateProperties.WATERLOGGED) ? 0 : this.igniteOdds.getInt(blockstate.getBlock());
   }

   private void checkBurnOut(Level level, BlockPos blockpos, int i, RandomSource randomsource, int j) {
      int k = this.getBurnOdds(level.getBlockState(blockpos));
      if (randomsource.nextInt(i) < k) {
         BlockState blockstate = level.getBlockState(blockpos);
         if (randomsource.nextInt(j + 10) < 5 && !level.isRainingAt(blockpos)) {
            int l = Math.min(j + randomsource.nextInt(5) / 4, 15);
            level.setBlock(blockpos, this.getStateWithAge(level, blockpos, l), 3);
         } else {
            level.removeBlock(blockpos, false);
         }

         Block block = blockstate.getBlock();
         if (block instanceof TntBlock) {
            TntBlock.explode(level, blockpos);
         }
      }

   }

   private BlockState getStateWithAge(LevelAccessor levelaccessor, BlockPos blockpos, int i) {
      BlockState blockstate = getState(levelaccessor, blockpos);
      return blockstate.is(Blocks.FIRE) ? blockstate.setValue(AGE, Integer.valueOf(i)) : blockstate;
   }

   private boolean isValidFireLocation(BlockGetter blockgetter, BlockPos blockpos) {
      for(Direction direction : Direction.values()) {
         if (this.canBurn(blockgetter.getBlockState(blockpos.relative(direction)))) {
            return true;
         }
      }

      return false;
   }

   private int getIgniteOdds(LevelReader levelreader, BlockPos blockpos) {
      if (!levelreader.isEmptyBlock(blockpos)) {
         return 0;
      } else {
         int i = 0;

         for(Direction direction : Direction.values()) {
            BlockState blockstate = levelreader.getBlockState(blockpos.relative(direction));
            i = Math.max(this.getIgniteOdds(blockstate), i);
         }

         return i;
      }
   }

   protected boolean canBurn(BlockState blockstate) {
      return this.getIgniteOdds(blockstate) > 0;
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      super.onPlace(blockstate, level, blockpos, blockstate1, flag);
      level.scheduleTick(blockpos, this, getFireTickDelay(level.random));
   }

   private static int getFireTickDelay(RandomSource randomsource) {
      return 30 + randomsource.nextInt(10);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
   }

   private void setFlammable(Block block, int i, int j) {
      this.igniteOdds.put(block, i);
      this.burnOdds.put(block, j);
   }

   public static void bootStrap() {
      FireBlock fireblock = (FireBlock)Blocks.FIRE;
      fireblock.setFlammable(Blocks.OAK_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.SPRUCE_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.BIRCH_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.JUNGLE_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.ACACIA_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.CHERRY_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.DARK_OAK_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.MANGROVE_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_PLANKS, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_MOSAIC, 5, 20);
      fireblock.setFlammable(Blocks.OAK_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.SPRUCE_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.BIRCH_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.JUNGLE_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.ACACIA_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.CHERRY_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.DARK_OAK_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.MANGROVE_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_MOSAIC_SLAB, 5, 20);
      fireblock.setFlammable(Blocks.OAK_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.SPRUCE_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.BIRCH_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.JUNGLE_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.ACACIA_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.CHERRY_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.MANGROVE_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_FENCE_GATE, 5, 20);
      fireblock.setFlammable(Blocks.OAK_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.SPRUCE_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.BIRCH_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.JUNGLE_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.ACACIA_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.CHERRY_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.DARK_OAK_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.MANGROVE_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_FENCE, 5, 20);
      fireblock.setFlammable(Blocks.OAK_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.BIRCH_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.SPRUCE_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.JUNGLE_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.ACACIA_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.CHERRY_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.DARK_OAK_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.MANGROVE_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.BAMBOO_MOSAIC_STAIRS, 5, 20);
      fireblock.setFlammable(Blocks.OAK_LOG, 5, 5);
      fireblock.setFlammable(Blocks.SPRUCE_LOG, 5, 5);
      fireblock.setFlammable(Blocks.BIRCH_LOG, 5, 5);
      fireblock.setFlammable(Blocks.JUNGLE_LOG, 5, 5);
      fireblock.setFlammable(Blocks.ACACIA_LOG, 5, 5);
      fireblock.setFlammable(Blocks.CHERRY_LOG, 5, 5);
      fireblock.setFlammable(Blocks.DARK_OAK_LOG, 5, 5);
      fireblock.setFlammable(Blocks.MANGROVE_LOG, 5, 5);
      fireblock.setFlammable(Blocks.BAMBOO_BLOCK, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_OAK_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_CHERRY_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_MANGROVE_LOG, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_BAMBOO_BLOCK, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_OAK_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_CHERRY_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.STRIPPED_MANGROVE_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.OAK_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.SPRUCE_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.BIRCH_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.JUNGLE_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.ACACIA_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.CHERRY_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.DARK_OAK_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.MANGROVE_WOOD, 5, 5);
      fireblock.setFlammable(Blocks.MANGROVE_ROOTS, 5, 20);
      fireblock.setFlammable(Blocks.OAK_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.SPRUCE_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.BIRCH_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.JUNGLE_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.ACACIA_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.CHERRY_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.DARK_OAK_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.MANGROVE_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.BOOKSHELF, 30, 20);
      fireblock.setFlammable(Blocks.TNT, 15, 100);
      fireblock.setFlammable(Blocks.GRASS, 60, 100);
      fireblock.setFlammable(Blocks.FERN, 60, 100);
      fireblock.setFlammable(Blocks.DEAD_BUSH, 60, 100);
      fireblock.setFlammable(Blocks.SUNFLOWER, 60, 100);
      fireblock.setFlammable(Blocks.LILAC, 60, 100);
      fireblock.setFlammable(Blocks.ROSE_BUSH, 60, 100);
      fireblock.setFlammable(Blocks.PEONY, 60, 100);
      fireblock.setFlammable(Blocks.TALL_GRASS, 60, 100);
      fireblock.setFlammable(Blocks.LARGE_FERN, 60, 100);
      fireblock.setFlammable(Blocks.DANDELION, 60, 100);
      fireblock.setFlammable(Blocks.POPPY, 60, 100);
      fireblock.setFlammable(Blocks.BLUE_ORCHID, 60, 100);
      fireblock.setFlammable(Blocks.ALLIUM, 60, 100);
      fireblock.setFlammable(Blocks.AZURE_BLUET, 60, 100);
      fireblock.setFlammable(Blocks.RED_TULIP, 60, 100);
      fireblock.setFlammable(Blocks.ORANGE_TULIP, 60, 100);
      fireblock.setFlammable(Blocks.WHITE_TULIP, 60, 100);
      fireblock.setFlammable(Blocks.PINK_TULIP, 60, 100);
      fireblock.setFlammable(Blocks.OXEYE_DAISY, 60, 100);
      fireblock.setFlammable(Blocks.CORNFLOWER, 60, 100);
      fireblock.setFlammable(Blocks.LILY_OF_THE_VALLEY, 60, 100);
      fireblock.setFlammable(Blocks.TORCHFLOWER, 60, 100);
      fireblock.setFlammable(Blocks.PITCHER_PLANT, 60, 100);
      fireblock.setFlammable(Blocks.WITHER_ROSE, 60, 100);
      fireblock.setFlammable(Blocks.PINK_PETALS, 60, 100);
      fireblock.setFlammable(Blocks.WHITE_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.ORANGE_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.MAGENTA_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.LIGHT_BLUE_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.YELLOW_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.LIME_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.PINK_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.GRAY_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.LIGHT_GRAY_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.CYAN_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.PURPLE_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.BLUE_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.BROWN_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.GREEN_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.RED_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.BLACK_WOOL, 30, 60);
      fireblock.setFlammable(Blocks.VINE, 15, 100);
      fireblock.setFlammable(Blocks.COAL_BLOCK, 5, 5);
      fireblock.setFlammable(Blocks.HAY_BLOCK, 60, 20);
      fireblock.setFlammable(Blocks.TARGET, 15, 20);
      fireblock.setFlammable(Blocks.WHITE_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.ORANGE_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.MAGENTA_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.LIGHT_BLUE_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.YELLOW_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.LIME_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.PINK_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.GRAY_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.LIGHT_GRAY_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.CYAN_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.PURPLE_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.BLUE_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.BROWN_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.GREEN_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.RED_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.BLACK_CARPET, 60, 20);
      fireblock.setFlammable(Blocks.DRIED_KELP_BLOCK, 30, 60);
      fireblock.setFlammable(Blocks.BAMBOO, 60, 60);
      fireblock.setFlammable(Blocks.SCAFFOLDING, 60, 60);
      fireblock.setFlammable(Blocks.LECTERN, 30, 20);
      fireblock.setFlammable(Blocks.COMPOSTER, 5, 20);
      fireblock.setFlammable(Blocks.SWEET_BERRY_BUSH, 60, 100);
      fireblock.setFlammable(Blocks.BEEHIVE, 5, 20);
      fireblock.setFlammable(Blocks.BEE_NEST, 30, 20);
      fireblock.setFlammable(Blocks.AZALEA_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.FLOWERING_AZALEA_LEAVES, 30, 60);
      fireblock.setFlammable(Blocks.CAVE_VINES, 15, 60);
      fireblock.setFlammable(Blocks.CAVE_VINES_PLANT, 15, 60);
      fireblock.setFlammable(Blocks.SPORE_BLOSSOM, 60, 100);
      fireblock.setFlammable(Blocks.AZALEA, 30, 60);
      fireblock.setFlammable(Blocks.FLOWERING_AZALEA, 30, 60);
      fireblock.setFlammable(Blocks.BIG_DRIPLEAF, 60, 100);
      fireblock.setFlammable(Blocks.BIG_DRIPLEAF_STEM, 60, 100);
      fireblock.setFlammable(Blocks.SMALL_DRIPLEAF, 60, 100);
      fireblock.setFlammable(Blocks.HANGING_ROOTS, 30, 60);
      fireblock.setFlammable(Blocks.GLOW_LICHEN, 15, 100);
   }
}
