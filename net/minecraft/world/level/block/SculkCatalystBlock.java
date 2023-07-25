package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SculkCatalystBlock extends BaseEntityBlock {
   public static final BooleanProperty PULSE = BlockStateProperties.BLOOM;
   private final IntProvider xpRange = ConstantInt.of(5);

   public SculkCatalystBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(PULSE, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(PULSE);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(PULSE)) {
         serverlevel.setBlock(blockpos, blockstate.setValue(PULSE, Boolean.valueOf(false)), 3);
      }

   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SculkCatalystBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return level.isClientSide ? null : createTickerHelper(blockentitytype, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::serverTick);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (flag) {
         this.tryDropExperience(serverlevel, blockpos, itemstack, this.xpRange);
      }

   }
}
