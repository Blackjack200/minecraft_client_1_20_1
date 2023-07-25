package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurtleEggBlock extends Block {
   public static final int MAX_HATCH_LEVEL = 2;
   public static final int MIN_EGGS = 1;
   public static final int MAX_EGGS = 4;
   private static final VoxelShape ONE_EGG_AABB = Block.box(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
   private static final VoxelShape MULTIPLE_EGGS_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);
   public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
   public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

   public TurtleEggBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)).setValue(EGGS, Integer.valueOf(1)));
   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
      if (!entity.isSteppingCarefully()) {
         this.destroyEgg(level, blockstate, blockpos, entity, 100);
      }

      super.stepOn(level, blockpos, blockstate, entity);
   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      if (!(entity instanceof Zombie)) {
         this.destroyEgg(level, blockstate, blockpos, entity, 3);
      }

      super.fallOn(level, blockstate, blockpos, entity, f);
   }

   private void destroyEgg(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, int i) {
      if (this.canDestroyEgg(level, entity)) {
         if (!level.isClientSide && level.random.nextInt(i) == 0 && blockstate.is(Blocks.TURTLE_EGG)) {
            this.decreaseEggs(level, blockpos, blockstate);
         }

      }
   }

   private void decreaseEggs(Level level, BlockPos blockpos, BlockState blockstate) {
      level.playSound((Player)null, blockpos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
      int i = blockstate.getValue(EGGS);
      if (i <= 1) {
         level.destroyBlock(blockpos, false);
      } else {
         level.setBlock(blockpos, blockstate.setValue(EGGS, Integer.valueOf(i - 1)), 2);
         level.gameEvent(GameEvent.BLOCK_DESTROY, blockpos, GameEvent.Context.of(blockstate));
         level.levelEvent(2001, blockpos, Block.getId(blockstate));
      }

   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (this.shouldUpdateHatchLevel(serverlevel) && onSand(serverlevel, blockpos)) {
         int i = blockstate.getValue(HATCH);
         if (i < 2) {
            serverlevel.playSound((Player)null, blockpos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + randomsource.nextFloat() * 0.2F);
            serverlevel.setBlock(blockpos, blockstate.setValue(HATCH, Integer.valueOf(i + 1)), 2);
         } else {
            serverlevel.playSound((Player)null, blockpos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + randomsource.nextFloat() * 0.2F);
            serverlevel.removeBlock(blockpos, false);

            for(int j = 0; j < blockstate.getValue(EGGS); ++j) {
               serverlevel.levelEvent(2001, blockpos, Block.getId(blockstate));
               Turtle turtle = EntityType.TURTLE.create(serverlevel);
               if (turtle != null) {
                  turtle.setAge(-24000);
                  turtle.setHomePos(blockpos);
                  turtle.moveTo((double)blockpos.getX() + 0.3D + (double)j * 0.2D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.3D, 0.0F, 0.0F);
                  serverlevel.addFreshEntity(turtle);
               }
            }
         }
      }

   }

   public static boolean onSand(BlockGetter blockgetter, BlockPos blockpos) {
      return isSand(blockgetter, blockpos.below());
   }

   public static boolean isSand(BlockGetter blockgetter, BlockPos blockpos) {
      return blockgetter.getBlockState(blockpos).is(BlockTags.SAND);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (onSand(level, blockpos) && !level.isClientSide) {
         level.levelEvent(2005, blockpos, 0);
      }

   }

   private boolean shouldUpdateHatchLevel(Level level) {
      float f = level.getTimeOfDay(1.0F);
      if ((double)f < 0.69D && (double)f > 0.65D) {
         return true;
      } else {
         return level.random.nextInt(500) == 0;
      }
   }

   public void playerDestroy(Level level, Player player, BlockPos blockpos, BlockState blockstate, @Nullable BlockEntity blockentity, ItemStack itemstack) {
      super.playerDestroy(level, player, blockpos, blockstate, blockentity, itemstack);
      this.decreaseEggs(level, blockpos, blockstate);
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return !blockplacecontext.isSecondaryUseActive() && blockplacecontext.getItemInHand().is(this.asItem()) && blockstate.getValue(EGGS) < 4 ? true : super.canBeReplaced(blockstate, blockplacecontext);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos());
      return blockstate.is(this) ? blockstate.setValue(EGGS, Integer.valueOf(Math.min(4, blockstate.getValue(EGGS) + 1))) : super.getStateForPlacement(blockplacecontext);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return blockstate.getValue(EGGS) > 1 ? MULTIPLE_EGGS_AABB : ONE_EGG_AABB;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HATCH, EGGS);
   }

   private boolean canDestroyEgg(Level level, Entity entity) {
      if (!(entity instanceof Turtle) && !(entity instanceof Bat)) {
         if (!(entity instanceof LivingEntity)) {
            return false;
         } else {
            return entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
         }
      } else {
         return false;
      }
   }
}
