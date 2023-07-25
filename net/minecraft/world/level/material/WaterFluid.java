package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class WaterFluid extends FlowingFluid {
   public Fluid getFlowing() {
      return Fluids.FLOWING_WATER;
   }

   public Fluid getSource() {
      return Fluids.WATER;
   }

   public Item getBucket() {
      return Items.WATER_BUCKET;
   }

   public void animateTick(Level level, BlockPos blockpos, FluidState fluidstate, RandomSource randomsource) {
      if (!fluidstate.isSource() && !fluidstate.getValue(FALLING)) {
         if (randomsource.nextInt(64) == 0) {
            level.playLocalSound((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
         }
      } else if (randomsource.nextInt(10) == 0) {
         level.addParticle(ParticleTypes.UNDERWATER, (double)blockpos.getX() + randomsource.nextDouble(), (double)blockpos.getY() + randomsource.nextDouble(), (double)blockpos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
      }

   }

   @Nullable
   public ParticleOptions getDripParticle() {
      return ParticleTypes.DRIPPING_WATER;
   }

   protected boolean canConvertToSource(Level level) {
      return level.getGameRules().getBoolean(GameRules.RULE_WATER_SOURCE_CONVERSION);
   }

   protected void beforeDestroyingBlock(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      BlockEntity blockentity = blockstate.hasBlockEntity() ? levelaccessor.getBlockEntity(blockpos) : null;
      Block.dropResources(blockstate, levelaccessor, blockpos, blockentity);
   }

   public int getSlopeFindDistance(LevelReader levelreader) {
      return 4;
   }

   public BlockState createLegacyBlock(FluidState fluidstate) {
      return Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(fluidstate)));
   }

   public boolean isSame(Fluid fluid) {
      return fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER;
   }

   public int getDropOff(LevelReader levelreader) {
      return 1;
   }

   public int getTickDelay(LevelReader levelreader) {
      return 5;
   }

   public boolean canBeReplacedWith(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos, Fluid fluid, Direction direction) {
      return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL);
   }

   public static class Flowing extends WaterFluid {
      protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> statedefinition_builder) {
         super.createFluidStateDefinition(statedefinition_builder);
         statedefinition_builder.add(LEVEL);
      }

      public int getAmount(FluidState fluidstate) {
         return fluidstate.getValue(LEVEL);
      }

      public boolean isSource(FluidState fluidstate) {
         return false;
      }
   }

   public static class Source extends WaterFluid {
      public int getAmount(FluidState fluidstate) {
         return 8;
      }

      public boolean isSource(FluidState fluidstate) {
         return true;
      }
   }
}
