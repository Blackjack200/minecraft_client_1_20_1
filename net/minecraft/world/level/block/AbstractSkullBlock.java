package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public abstract class AbstractSkullBlock extends BaseEntityBlock implements Equipable {
   private final SkullBlock.Type type;

   public AbstractSkullBlock(SkullBlock.Type skullblock_type, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.type = skullblock_type;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SkullBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      if (level.isClientSide) {
         boolean flag = blockstate.is(Blocks.DRAGON_HEAD) || blockstate.is(Blocks.DRAGON_WALL_HEAD) || blockstate.is(Blocks.PIGLIN_HEAD) || blockstate.is(Blocks.PIGLIN_WALL_HEAD);
         if (flag) {
            return createTickerHelper(blockentitytype, BlockEntityType.SKULL, SkullBlockEntity::animation);
         }
      }

      return null;
   }

   public SkullBlock.Type getType() {
      return this.type;
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.HEAD;
   }
}
