package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class EndGatewayBlock extends BaseEntityBlock {
   protected EndGatewayBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new TheEndGatewayBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.END_GATEWAY, level.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::teleportTick);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof TheEndGatewayBlockEntity) {
         int i = ((TheEndGatewayBlockEntity)blockentity).getParticleAmount();

         for(int j = 0; j < i; ++j) {
            double d0 = (double)blockpos.getX() + randomsource.nextDouble();
            double d1 = (double)blockpos.getY() + randomsource.nextDouble();
            double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
            double d3 = (randomsource.nextDouble() - 0.5D) * 0.5D;
            double d4 = (randomsource.nextDouble() - 0.5D) * 0.5D;
            double d5 = (randomsource.nextDouble() - 0.5D) * 0.5D;
            int k = randomsource.nextInt(2) * 2 - 1;
            if (randomsource.nextBoolean()) {
               d2 = (double)blockpos.getZ() + 0.5D + 0.25D * (double)k;
               d5 = (double)(randomsource.nextFloat() * 2.0F * (float)k);
            } else {
               d0 = (double)blockpos.getX() + 0.5D + 0.25D * (double)k;
               d3 = (double)(randomsource.nextFloat() * 2.0F * (float)k);
            }

            level.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
         }

      }
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return ItemStack.EMPTY;
   }

   public boolean canBeReplaced(BlockState blockstate, Fluid fluid) {
      return false;
   }
}
