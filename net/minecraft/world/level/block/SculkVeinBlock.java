package net.minecraft.world.level.block;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SculkVeinBlock extends MultifaceBlock implements SculkBehaviour, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final MultifaceSpreader veinSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.DEFAULT_SPREAD_ORDER));
   private final MultifaceSpreader sameSpaceSpreader = new MultifaceSpreader(new SculkVeinBlock.SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType.SAME_POSITION));

   public SculkVeinBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public MultifaceSpreader getSpreader() {
      return this.veinSpreader;
   }

   public MultifaceSpreader getSameSpaceSpreader() {
      return this.sameSpaceSpreader;
   }

   public static boolean regrow(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, Collection<Direction> collection) {
      boolean flag = false;
      BlockState blockstate1 = Blocks.SCULK_VEIN.defaultBlockState();

      for(Direction direction : collection) {
         BlockPos blockpos1 = blockpos.relative(direction);
         if (canAttachTo(levelaccessor, direction, blockpos1, levelaccessor.getBlockState(blockpos1))) {
            blockstate1 = blockstate1.setValue(getFaceProperty(direction), Boolean.valueOf(true));
            flag = true;
         }
      }

      if (!flag) {
         return false;
      } else {
         if (!blockstate.getFluidState().isEmpty()) {
            blockstate1 = blockstate1.setValue(WATERLOGGED, Boolean.valueOf(true));
         }

         levelaccessor.setBlock(blockpos, blockstate1, 3);
         return true;
      }
   }

   public void onDischarged(LevelAccessor levelaccessor, BlockState blockstate, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.is(this)) {
         for(Direction direction : DIRECTIONS) {
            BooleanProperty booleanproperty = getFaceProperty(direction);
            if (blockstate.getValue(booleanproperty) && levelaccessor.getBlockState(blockpos.relative(direction)).is(Blocks.SCULK)) {
               blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
            }
         }

         if (!hasAnyFace(blockstate)) {
            FluidState fluidstate = levelaccessor.getFluidState(blockpos);
            blockstate = (fluidstate.isEmpty() ? Blocks.AIR : Blocks.WATER).defaultBlockState();
         }

         levelaccessor.setBlock(blockpos, blockstate, 3);
         SculkBehaviour.super.onDischarged(levelaccessor, blockstate, blockpos, randomsource);
      }
   }

   public int attemptUseCharge(SculkSpreader.ChargeCursor sculkspreader_chargecursor, LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, SculkSpreader sculkspreader, boolean flag) {
      if (flag && this.attemptPlaceSculk(sculkspreader, levelaccessor, sculkspreader_chargecursor.getPos(), randomsource)) {
         return sculkspreader_chargecursor.getCharge() - 1;
      } else {
         return randomsource.nextInt(sculkspreader.chargeDecayRate()) == 0 ? Mth.floor((float)sculkspreader_chargecursor.getCharge() * 0.5F) : sculkspreader_chargecursor.getCharge();
      }
   }

   private boolean attemptPlaceSculk(SculkSpreader sculkspreader, LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource) {
      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      TagKey<Block> tagkey = sculkspreader.replaceableBlocks();

      for(Direction direction : Direction.allShuffled(randomsource)) {
         if (hasFace(blockstate, direction)) {
            BlockPos blockpos1 = blockpos.relative(direction);
            BlockState blockstate1 = levelaccessor.getBlockState(blockpos1);
            if (blockstate1.is(tagkey)) {
               BlockState blockstate2 = Blocks.SCULK.defaultBlockState();
               levelaccessor.setBlock(blockpos1, blockstate2, 3);
               Block.pushEntitiesUp(blockstate1, blockstate2, levelaccessor, blockpos1);
               levelaccessor.playSound((Player)null, blockpos1, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
               this.veinSpreader.spreadAll(blockstate2, levelaccessor, blockpos1, sculkspreader.isWorldGeneration());
               Direction direction1 = direction.getOpposite();

               for(Direction direction2 : DIRECTIONS) {
                  if (direction2 != direction1) {
                     BlockPos blockpos2 = blockpos1.relative(direction2);
                     BlockState blockstate3 = levelaccessor.getBlockState(blockpos2);
                     if (blockstate3.is(this)) {
                        this.onDischarged(levelaccessor, blockstate3, blockpos2, randomsource);
                     }
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   public static boolean hasSubstrateAccess(LevelAccessor levelaccessor, BlockState blockstate, BlockPos blockpos) {
      if (!blockstate.is(Blocks.SCULK_VEIN)) {
         return false;
      } else {
         for(Direction direction : DIRECTIONS) {
            if (hasFace(blockstate, direction) && levelaccessor.getBlockState(blockpos.relative(direction)).is(BlockTags.SCULK_REPLACEABLE)) {
               return true;
            }
         }

         return false;
      }
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      super.createBlockStateDefinition(statedefinition_builder);
      statedefinition_builder.add(WATERLOGGED);
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return !blockplacecontext.getItemInHand().is(Items.SCULK_VEIN) || super.canBeReplaced(blockstate, blockplacecontext);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   class SculkVeinSpreaderConfig extends MultifaceSpreader.DefaultSpreaderConfig {
      private final MultifaceSpreader.SpreadType[] spreadTypes;

      public SculkVeinSpreaderConfig(MultifaceSpreader.SpreadType... amultifacespreader_spreadtype) {
         super(SculkVeinBlock.this);
         this.spreadTypes = amultifacespreader_spreadtype;
      }

      public boolean stateCanBeReplaced(BlockGetter blockgetter, BlockPos blockpos, BlockPos blockpos1, Direction direction, BlockState blockstate) {
         BlockState blockstate1 = blockgetter.getBlockState(blockpos1.relative(direction));
         if (!blockstate1.is(Blocks.SCULK) && !blockstate1.is(Blocks.SCULK_CATALYST) && !blockstate1.is(Blocks.MOVING_PISTON)) {
            if (blockpos.distManhattan(blockpos1) == 2) {
               BlockPos blockpos2 = blockpos.relative(direction.getOpposite());
               if (blockgetter.getBlockState(blockpos2).isFaceSturdy(blockgetter, blockpos2, direction)) {
                  return false;
               }
            }

            FluidState fluidstate = blockstate.getFluidState();
            if (!fluidstate.isEmpty() && !fluidstate.is(Fluids.WATER)) {
               return false;
            } else if (blockstate.is(BlockTags.FIRE)) {
               return false;
            } else {
               return blockstate.canBeReplaced() || super.stateCanBeReplaced(blockgetter, blockpos, blockpos1, direction, blockstate);
            }
         } else {
            return false;
         }
      }

      public MultifaceSpreader.SpreadType[] getSpreadTypes() {
         return this.spreadTypes;
      }

      public boolean isOtherBlockValidAsSource(BlockState blockstate) {
         return !blockstate.is(Blocks.SCULK_VEIN);
      }
   }
}
