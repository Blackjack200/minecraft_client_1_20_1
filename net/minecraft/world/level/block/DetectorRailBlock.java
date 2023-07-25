package net.minecraft.world.level.block;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class DetectorRailBlock extends BaseRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int PRESSED_CHECK_PERIOD = 20;

   public DetectorRailBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(true, blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide) {
         if (!blockstate.getValue(POWERED)) {
            this.checkPressed(level, blockpos, blockstate);
         }
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(POWERED)) {
         this.checkPressed(serverlevel, blockpos, blockstate);
      }
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) ? 15 : 0;
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      if (!blockstate.getValue(POWERED)) {
         return 0;
      } else {
         return direction == Direction.UP ? 15 : 0;
      }
   }

   private void checkPressed(Level level, BlockPos blockpos, BlockState blockstate) {
      if (this.canSurvive(blockstate, level, blockpos)) {
         boolean flag = blockstate.getValue(POWERED);
         boolean flag1 = false;
         List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, blockpos, AbstractMinecart.class, (entity) -> true);
         if (!list.isEmpty()) {
            flag1 = true;
         }

         if (flag1 && !flag) {
            BlockState blockstate1 = blockstate.setValue(POWERED, Boolean.valueOf(true));
            level.setBlock(blockpos, blockstate1, 3);
            this.updatePowerToConnected(level, blockpos, blockstate1, true);
            level.updateNeighborsAt(blockpos, this);
            level.updateNeighborsAt(blockpos.below(), this);
            level.setBlocksDirty(blockpos, blockstate, blockstate1);
         }

         if (!flag1 && flag) {
            BlockState blockstate2 = blockstate.setValue(POWERED, Boolean.valueOf(false));
            level.setBlock(blockpos, blockstate2, 3);
            this.updatePowerToConnected(level, blockpos, blockstate2, false);
            level.updateNeighborsAt(blockpos, this);
            level.updateNeighborsAt(blockpos.below(), this);
            level.setBlocksDirty(blockpos, blockstate, blockstate2);
         }

         if (flag1) {
            level.scheduleTick(blockpos, this, 20);
         }

         level.updateNeighbourForOutputSignal(blockpos, this);
      }
   }

   protected void updatePowerToConnected(Level level, BlockPos blockpos, BlockState blockstate, boolean flag) {
      RailState railstate = new RailState(level, blockpos, blockstate);

      for(BlockPos blockpos1 : railstate.getConnections()) {
         BlockState blockstate1 = level.getBlockState(blockpos1);
         level.neighborChanged(blockstate1, blockpos1, blockstate1.getBlock(), blockpos, false);
      }

   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         BlockState blockstate2 = this.updateState(blockstate, level, blockpos, flag);
         this.checkPressed(level, blockpos, blockstate2);
      }
   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      if (blockstate.getValue(POWERED)) {
         List<MinecartCommandBlock> list = this.getInteractingMinecartOfType(level, blockpos, MinecartCommandBlock.class, (entity) -> true);
         if (!list.isEmpty()) {
            return list.get(0).getCommandBlock().getSuccessCount();
         }

         List<AbstractMinecart> list1 = this.getInteractingMinecartOfType(level, blockpos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
         if (!list1.isEmpty()) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)list1.get(0));
         }
      }

      return 0;
   }

   private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockpos, Class<T> oclass, Predicate<Entity> predicate) {
      return level.getEntitiesOfClass(oclass, this.getSearchBB(blockpos), predicate);
   }

   private AABB getSearchBB(BlockPos blockpos) {
      double d0 = 0.2D;
      return new AABB((double)blockpos.getX() + 0.2D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.2D, (double)(blockpos.getX() + 1) - 0.2D, (double)(blockpos.getY() + 1) - 0.2D, (double)(blockpos.getZ() + 1) - 0.2D);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
            }
         case COUNTERCLOCKWISE_90:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_SOUTH);
            }
         case CLOCKWISE_90:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_SOUTH);
            }
         default:
            return blockstate;
      }
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      RailShape railshape = blockstate.getValue(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            switch (railshape) {
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               default:
                  return super.mirror(blockstate, mirror);
            }
         case FRONT_BACK:
            switch (railshape) {
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
               case ASCENDING_SOUTH:
               default:
                  break;
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
            }
      }

      return super.mirror(blockstate, mirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(SHAPE, POWERED, WATERLOGGED);
   }
}
