package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ComposterBlock extends Block implements WorldlyContainerHolder {
   public static final int READY = 8;
   public static final int MIN_LEVEL = 0;
   public static final int MAX_LEVEL = 7;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
   public static final Object2FloatMap<ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap<>();
   private static final int AABB_SIDE_THICKNESS = 2;
   private static final VoxelShape OUTER_SHAPE = Shapes.block();
   private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], (avoxelshape) -> {
      for(int i = 0; i < 8; ++i) {
         avoxelshape[i] = Shapes.join(OUTER_SHAPE, Block.box(2.0D, (double)Math.max(2, 1 + i * 2), 2.0D, 14.0D, 16.0D, 14.0D), BooleanOp.ONLY_FIRST);
      }

      avoxelshape[8] = avoxelshape[7];
   });

   public static void bootStrap() {
      COMPOSTABLES.defaultReturnValue(-1.0F);
      float f = 0.3F;
      float f1 = 0.5F;
      float f2 = 0.65F;
      float f3 = 0.85F;
      float f4 = 1.0F;
      add(0.3F, Items.JUNGLE_LEAVES);
      add(0.3F, Items.OAK_LEAVES);
      add(0.3F, Items.SPRUCE_LEAVES);
      add(0.3F, Items.DARK_OAK_LEAVES);
      add(0.3F, Items.ACACIA_LEAVES);
      add(0.3F, Items.CHERRY_LEAVES);
      add(0.3F, Items.BIRCH_LEAVES);
      add(0.3F, Items.AZALEA_LEAVES);
      add(0.3F, Items.MANGROVE_LEAVES);
      add(0.3F, Items.OAK_SAPLING);
      add(0.3F, Items.SPRUCE_SAPLING);
      add(0.3F, Items.BIRCH_SAPLING);
      add(0.3F, Items.JUNGLE_SAPLING);
      add(0.3F, Items.ACACIA_SAPLING);
      add(0.3F, Items.CHERRY_SAPLING);
      add(0.3F, Items.DARK_OAK_SAPLING);
      add(0.3F, Items.MANGROVE_PROPAGULE);
      add(0.3F, Items.BEETROOT_SEEDS);
      add(0.3F, Items.DRIED_KELP);
      add(0.3F, Items.GRASS);
      add(0.3F, Items.KELP);
      add(0.3F, Items.MELON_SEEDS);
      add(0.3F, Items.PUMPKIN_SEEDS);
      add(0.3F, Items.SEAGRASS);
      add(0.3F, Items.SWEET_BERRIES);
      add(0.3F, Items.GLOW_BERRIES);
      add(0.3F, Items.WHEAT_SEEDS);
      add(0.3F, Items.MOSS_CARPET);
      add(0.3F, Items.PINK_PETALS);
      add(0.3F, Items.SMALL_DRIPLEAF);
      add(0.3F, Items.HANGING_ROOTS);
      add(0.3F, Items.MANGROVE_ROOTS);
      add(0.3F, Items.TORCHFLOWER_SEEDS);
      add(0.3F, Items.PITCHER_POD);
      add(0.5F, Items.DRIED_KELP_BLOCK);
      add(0.5F, Items.TALL_GRASS);
      add(0.5F, Items.FLOWERING_AZALEA_LEAVES);
      add(0.5F, Items.CACTUS);
      add(0.5F, Items.SUGAR_CANE);
      add(0.5F, Items.VINE);
      add(0.5F, Items.NETHER_SPROUTS);
      add(0.5F, Items.WEEPING_VINES);
      add(0.5F, Items.TWISTING_VINES);
      add(0.5F, Items.MELON_SLICE);
      add(0.5F, Items.GLOW_LICHEN);
      add(0.65F, Items.SEA_PICKLE);
      add(0.65F, Items.LILY_PAD);
      add(0.65F, Items.PUMPKIN);
      add(0.65F, Items.CARVED_PUMPKIN);
      add(0.65F, Items.MELON);
      add(0.65F, Items.APPLE);
      add(0.65F, Items.BEETROOT);
      add(0.65F, Items.CARROT);
      add(0.65F, Items.COCOA_BEANS);
      add(0.65F, Items.POTATO);
      add(0.65F, Items.WHEAT);
      add(0.65F, Items.BROWN_MUSHROOM);
      add(0.65F, Items.RED_MUSHROOM);
      add(0.65F, Items.MUSHROOM_STEM);
      add(0.65F, Items.CRIMSON_FUNGUS);
      add(0.65F, Items.WARPED_FUNGUS);
      add(0.65F, Items.NETHER_WART);
      add(0.65F, Items.CRIMSON_ROOTS);
      add(0.65F, Items.WARPED_ROOTS);
      add(0.65F, Items.SHROOMLIGHT);
      add(0.65F, Items.DANDELION);
      add(0.65F, Items.POPPY);
      add(0.65F, Items.BLUE_ORCHID);
      add(0.65F, Items.ALLIUM);
      add(0.65F, Items.AZURE_BLUET);
      add(0.65F, Items.RED_TULIP);
      add(0.65F, Items.ORANGE_TULIP);
      add(0.65F, Items.WHITE_TULIP);
      add(0.65F, Items.PINK_TULIP);
      add(0.65F, Items.OXEYE_DAISY);
      add(0.65F, Items.CORNFLOWER);
      add(0.65F, Items.LILY_OF_THE_VALLEY);
      add(0.65F, Items.WITHER_ROSE);
      add(0.65F, Items.FERN);
      add(0.65F, Items.SUNFLOWER);
      add(0.65F, Items.LILAC);
      add(0.65F, Items.ROSE_BUSH);
      add(0.65F, Items.PEONY);
      add(0.65F, Items.LARGE_FERN);
      add(0.65F, Items.SPORE_BLOSSOM);
      add(0.65F, Items.AZALEA);
      add(0.65F, Items.MOSS_BLOCK);
      add(0.65F, Items.BIG_DRIPLEAF);
      add(0.85F, Items.HAY_BLOCK);
      add(0.85F, Items.BROWN_MUSHROOM_BLOCK);
      add(0.85F, Items.RED_MUSHROOM_BLOCK);
      add(0.85F, Items.NETHER_WART_BLOCK);
      add(0.85F, Items.WARPED_WART_BLOCK);
      add(0.85F, Items.FLOWERING_AZALEA);
      add(0.85F, Items.BREAD);
      add(0.85F, Items.BAKED_POTATO);
      add(0.85F, Items.COOKIE);
      add(0.85F, Items.TORCHFLOWER);
      add(0.85F, Items.PITCHER_PLANT);
      add(1.0F, Items.CAKE);
      add(1.0F, Items.PUMPKIN_PIE);
   }

   private static void add(float f, ItemLike itemlike) {
      COMPOSTABLES.put(itemlike.asItem(), f);
   }

   public ComposterBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
   }

   public static void handleFill(Level level, BlockPos blockpos, boolean flag) {
      BlockState blockstate = level.getBlockState(blockpos);
      level.playLocalSound(blockpos, flag ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
      double d0 = blockstate.getShape(level, blockpos).max(Direction.Axis.Y, 0.5D, 0.5D) + 0.03125D;
      double d1 = (double)0.13125F;
      double d2 = (double)0.7375F;
      RandomSource randomsource = level.getRandom();

      for(int i = 0; i < 10; ++i) {
         double d3 = randomsource.nextGaussian() * 0.02D;
         double d4 = randomsource.nextGaussian() * 0.02D;
         double d5 = randomsource.nextGaussian() * 0.02D;
         level.addParticle(ParticleTypes.COMPOSTER, (double)blockpos.getX() + (double)0.13125F + (double)0.7375F * (double)randomsource.nextFloat(), (double)blockpos.getY() + d0 + (double)randomsource.nextFloat() * (1.0D - d0), (double)blockpos.getZ() + (double)0.13125F + (double)0.7375F * (double)randomsource.nextFloat(), d3, d4, d5);
      }

   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPES[blockstate.getValue(LEVEL)];
   }

   public VoxelShape getInteractionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return OUTER_SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPES[0];
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (blockstate.getValue(LEVEL) == 7) {
         level.scheduleTick(blockpos, blockstate.getBlock(), 20);
      }

   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      int i = blockstate.getValue(LEVEL);
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (i < 8 && COMPOSTABLES.containsKey(itemstack.getItem())) {
         if (i < 7 && !level.isClientSide) {
            BlockState blockstate1 = addItem(player, blockstate, level, blockpos, itemstack);
            level.levelEvent(1500, blockpos, blockstate != blockstate1 ? 1 : 0);
            player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
            if (!player.getAbilities().instabuild) {
               itemstack.shrink(1);
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else if (i == 8) {
         extractProduce(player, blockstate, level, blockpos);
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static BlockState insertItem(Entity entity, BlockState blockstate, ServerLevel serverlevel, ItemStack itemstack, BlockPos blockpos) {
      int i = blockstate.getValue(LEVEL);
      if (i < 7 && COMPOSTABLES.containsKey(itemstack.getItem())) {
         BlockState blockstate1 = addItem(entity, blockstate, serverlevel, blockpos, itemstack);
         itemstack.shrink(1);
         return blockstate1;
      } else {
         return blockstate;
      }
   }

   public static BlockState extractProduce(Entity entity, BlockState blockstate, Level level, BlockPos blockpos) {
      if (!level.isClientSide) {
         Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5D, 1.01D, 0.5D).offsetRandom(level.random, 0.7F);
         ItemEntity itementity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), new ItemStack(Items.BONE_MEAL));
         itementity.setDefaultPickUpDelay();
         level.addFreshEntity(itementity);
      }

      BlockState blockstate1 = empty(entity, blockstate, level, blockpos);
      level.playSound((Player)null, blockpos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
      return blockstate1;
   }

   static BlockState empty(@Nullable Entity entity, BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockState blockstate1 = blockstate.setValue(LEVEL, Integer.valueOf(0));
      levelaccessor.setBlock(blockpos, blockstate1, 3);
      levelaccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, blockstate1));
      return blockstate1;
   }

   static BlockState addItem(@Nullable Entity entity, BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, ItemStack itemstack) {
      int i = blockstate.getValue(LEVEL);
      float f = COMPOSTABLES.getFloat(itemstack.getItem());
      if ((i != 0 || !(f > 0.0F)) && !(levelaccessor.getRandom().nextDouble() < (double)f)) {
         return blockstate;
      } else {
         int j = i + 1;
         BlockState blockstate1 = blockstate.setValue(LEVEL, Integer.valueOf(j));
         levelaccessor.setBlock(blockpos, blockstate1, 3);
         levelaccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, blockstate1));
         if (j == 7) {
            levelaccessor.scheduleTick(blockpos, blockstate.getBlock(), 20);
         }

         return blockstate1;
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LEVEL) == 7) {
         serverlevel.setBlock(blockpos, blockstate.cycle(LEVEL), 3);
         serverlevel.playSound((Player)null, blockpos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return blockstate.getValue(LEVEL);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LEVEL);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public WorldlyContainer getContainer(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
      int i = blockstate.getValue(LEVEL);
      if (i == 8) {
         return new ComposterBlock.OutputContainer(blockstate, levelaccessor, blockpos, new ItemStack(Items.BONE_MEAL));
      } else {
         return (WorldlyContainer)(i < 7 ? new ComposterBlock.InputContainer(blockstate, levelaccessor, blockpos) : new ComposterBlock.EmptyContainer());
      }
   }

   static class EmptyContainer extends SimpleContainer implements WorldlyContainer {
      public EmptyContainer() {
         super(0);
      }

      public int[] getSlotsForFace(Direction direction) {
         return new int[0];
      }

      public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, @Nullable Direction direction) {
         return false;
      }

      public boolean canTakeItemThroughFace(int i, ItemStack itemstack, Direction direction) {
         return false;
      }
   }

   static class InputContainer extends SimpleContainer implements WorldlyContainer {
      private final BlockState state;
      private final LevelAccessor level;
      private final BlockPos pos;
      private boolean changed;

      public InputContainer(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
         super(1);
         this.state = blockstate;
         this.level = levelaccessor;
         this.pos = blockpos;
      }

      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction direction) {
         return direction == Direction.UP ? new int[]{0} : new int[0];
      }

      public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, @Nullable Direction direction) {
         return !this.changed && direction == Direction.UP && ComposterBlock.COMPOSTABLES.containsKey(itemstack.getItem());
      }

      public boolean canTakeItemThroughFace(int i, ItemStack itemstack, Direction direction) {
         return false;
      }

      public void setChanged() {
         ItemStack itemstack = this.getItem(0);
         if (!itemstack.isEmpty()) {
            this.changed = true;
            BlockState blockstate = ComposterBlock.addItem((Entity)null, this.state, this.level, this.pos, itemstack);
            this.level.levelEvent(1500, this.pos, blockstate != this.state ? 1 : 0);
            this.removeItemNoUpdate(0);
         }

      }
   }

   static class OutputContainer extends SimpleContainer implements WorldlyContainer {
      private final BlockState state;
      private final LevelAccessor level;
      private final BlockPos pos;
      private boolean changed;

      public OutputContainer(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, ItemStack itemstack) {
         super(itemstack);
         this.state = blockstate;
         this.level = levelaccessor;
         this.pos = blockpos;
      }

      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction direction) {
         return direction == Direction.DOWN ? new int[]{0} : new int[0];
      }

      public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, @Nullable Direction direction) {
         return false;
      }

      public boolean canTakeItemThroughFace(int i, ItemStack itemstack, Direction direction) {
         return !this.changed && direction == Direction.DOWN && itemstack.is(Items.BONE_MEAL);
      }

      public void setChanged() {
         ComposterBlock.empty((Entity)null, this.state, this.level, this.pos);
         this.changed = true;
      }
   }
}
