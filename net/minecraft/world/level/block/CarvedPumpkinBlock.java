package net.minecraft.world.level.block;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class CarvedPumpkinBlock extends HorizontalDirectionalBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   @Nullable
   private BlockPattern snowGolemBase;
   @Nullable
   private BlockPattern snowGolemFull;
   @Nullable
   private BlockPattern ironGolemBase;
   @Nullable
   private BlockPattern ironGolemFull;
   private static final Predicate<BlockState> PUMPKINS_PREDICATE = (blockstate) -> blockstate != null && (blockstate.is(Blocks.CARVED_PUMPKIN) || blockstate.is(Blocks.JACK_O_LANTERN));

   protected CarvedPumpkinBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         this.trySpawnGolem(level, blockpos);
      }
   }

   public boolean canSpawnGolem(LevelReader levelreader, BlockPos blockpos) {
      return this.getOrCreateSnowGolemBase().find(levelreader, blockpos) != null || this.getOrCreateIronGolemBase().find(levelreader, blockpos) != null;
   }

   private void trySpawnGolem(Level level, BlockPos blockpos) {
      BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch = this.getOrCreateSnowGolemFull().find(level, blockpos);
      if (blockpattern_blockpatternmatch != null) {
         SnowGolem snowgolem = EntityType.SNOW_GOLEM.create(level);
         if (snowgolem != null) {
            spawnGolemInWorld(level, blockpattern_blockpatternmatch, snowgolem, blockpattern_blockpatternmatch.getBlock(0, 2, 0).getPos());
         }
      } else {
         BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch1 = this.getOrCreateIronGolemFull().find(level, blockpos);
         if (blockpattern_blockpatternmatch1 != null) {
            IronGolem irongolem = EntityType.IRON_GOLEM.create(level);
            if (irongolem != null) {
               irongolem.setPlayerCreated(true);
               spawnGolemInWorld(level, blockpattern_blockpatternmatch1, irongolem, blockpattern_blockpatternmatch1.getBlock(1, 2, 0).getPos());
            }
         }
      }

   }

   private static void spawnGolemInWorld(Level level, BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch, Entity entity, BlockPos blockpos) {
      clearPatternBlocks(level, blockpattern_blockpatternmatch);
      entity.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.05D, (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
      level.addFreshEntity(entity);

      for(ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, entity.getBoundingBox().inflate(5.0D))) {
         CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, entity);
      }

      updatePatternBlocks(level, blockpattern_blockpatternmatch);
   }

   public static void clearPatternBlocks(Level level, BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch) {
      for(int i = 0; i < blockpattern_blockpatternmatch.getWidth(); ++i) {
         for(int j = 0; j < blockpattern_blockpatternmatch.getHeight(); ++j) {
            BlockInWorld blockinworld = blockpattern_blockpatternmatch.getBlock(i, j, 0);
            level.setBlock(blockinworld.getPos(), Blocks.AIR.defaultBlockState(), 2);
            level.levelEvent(2001, blockinworld.getPos(), Block.getId(blockinworld.getState()));
         }
      }

   }

   public static void updatePatternBlocks(Level level, BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch) {
      for(int i = 0; i < blockpattern_blockpatternmatch.getWidth(); ++i) {
         for(int j = 0; j < blockpattern_blockpatternmatch.getHeight(); ++j) {
            BlockInWorld blockinworld = blockpattern_blockpatternmatch.getBlock(i, j, 0);
            level.blockUpdated(blockinworld.getPos(), Blocks.AIR);
         }
      }

   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING);
   }

   private BlockPattern getOrCreateSnowGolemBase() {
      if (this.snowGolemBase == null) {
         this.snowGolemBase = BlockPatternBuilder.start().aisle(" ", "#", "#").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
      }

      return this.snowGolemBase;
   }

   private BlockPattern getOrCreateSnowGolemFull() {
      if (this.snowGolemFull == null) {
         this.snowGolemFull = BlockPatternBuilder.start().aisle("^", "#", "#").where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.SNOW_BLOCK))).build();
      }

      return this.snowGolemFull;
   }

   private BlockPattern getOrCreateIronGolemBase() {
      if (this.ironGolemBase == null) {
         this.ironGolemBase = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', (blockinworld) -> blockinworld.getState().isAir()).build();
      }

      return this.ironGolemBase;
   }

   private BlockPattern getOrCreateIronGolemFull() {
      if (this.ironGolemFull == null) {
         this.ironGolemFull = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', BlockInWorld.hasState(PUMPKINS_PREDICATE)).where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.IRON_BLOCK))).where('~', (blockinworld) -> blockinworld.getState().isAir()).build();
      }

      return this.ironGolemFull;
   }
}
