package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PressurePlateBlock extends BasePressurePlateBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private final PressurePlateBlock.Sensitivity sensitivity;

   protected PressurePlateBlock(PressurePlateBlock.Sensitivity pressureplateblock_sensitivity, BlockBehaviour.Properties blockbehaviour_properties, BlockSetType blocksettype) {
      super(blockbehaviour_properties, blocksettype);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
      this.sensitivity = pressureplateblock_sensitivity;
   }

   protected int getSignalForState(BlockState blockstate) {
      return blockstate.getValue(POWERED) ? 15 : 0;
   }

   protected BlockState setSignalForState(BlockState blockstate, int i) {
      return blockstate.setValue(POWERED, Boolean.valueOf(i > 0));
   }

   protected int getSignalStrength(Level level, BlockPos blockpos) {
      Class<Entity> var10000;
      switch (this.sensitivity) {
         case EVERYTHING:
            var10000 = Entity.class;
            break;
         case MOBS:
            var10000 = LivingEntity.class;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      Class oclass = var10000;
      return getEntityCount(level, TOUCH_AABB.move(blockpos), oclass) > 0 ? 15 : 0;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(POWERED);
   }

   public static enum Sensitivity {
      EVERYTHING,
      MOBS;
   }
}
