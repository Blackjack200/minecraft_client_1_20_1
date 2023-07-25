package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CaveVines {
   VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
   BooleanProperty BERRIES = BlockStateProperties.BERRIES;

   static InteractionResult use(@Nullable Entity entity, BlockState blockstate, Level level, BlockPos blockpos) {
      if (blockstate.getValue(BERRIES)) {
         Block.popResource(level, blockpos, new ItemStack(Items.GLOW_BERRIES, 1));
         float f = Mth.randomBetween(level.random, 0.8F, 1.2F);
         level.playSound((Player)null, blockpos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
         BlockState blockstate1 = blockstate.setValue(BERRIES, Boolean.valueOf(false));
         level.setBlock(blockpos, blockstate1, 2);
         level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, blockstate1));
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   static boolean hasGlowBerries(BlockState blockstate) {
      return blockstate.hasProperty(BERRIES) && blockstate.getValue(BERRIES);
   }

   static ToIntFunction<BlockState> emission(int i) {
      return (blockstate) -> blockstate.getValue(BlockStateProperties.BERRIES) ? i : 0;
   }
}
