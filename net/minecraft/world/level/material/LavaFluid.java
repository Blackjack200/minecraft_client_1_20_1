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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class LavaFluid extends FlowingFluid {
   public static final float MIN_LEVEL_CUTOFF = 0.44444445F;

   public Fluid getFlowing() {
      return Fluids.FLOWING_LAVA;
   }

   public Fluid getSource() {
      return Fluids.LAVA;
   }

   public Item getBucket() {
      return Items.LAVA_BUCKET;
   }

   public void animateTick(Level level, BlockPos blockpos, FluidState fluidstate, RandomSource randomsource) {
      BlockPos blockpos1 = blockpos.above();
      if (level.getBlockState(blockpos1).isAir() && !level.getBlockState(blockpos1).isSolidRender(level, blockpos1)) {
         if (randomsource.nextInt(100) == 0) {
            double d0 = (double)blockpos.getX() + randomsource.nextDouble();
            double d1 = (double)blockpos.getY() + 1.0D;
            double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
            level.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + randomsource.nextFloat() * 0.2F, 0.9F + randomsource.nextFloat() * 0.15F, false);
         }

         if (randomsource.nextInt(200) == 0) {
            level.playLocalSound((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + randomsource.nextFloat() * 0.2F, 0.9F + randomsource.nextFloat() * 0.15F, false);
         }
      }

   }

   public void randomTick(Level level, BlockPos blockpos, FluidState fluidstate, RandomSource randomsource) {
      if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
         int i = randomsource.nextInt(3);
         if (i > 0) {
            BlockPos blockpos1 = blockpos;

            for(int j = 0; j < i; ++j) {
               blockpos1 = blockpos1.offset(randomsource.nextInt(3) - 1, 1, randomsource.nextInt(3) - 1);
               if (!level.isLoaded(blockpos1)) {
                  return;
               }

               BlockState blockstate = level.getBlockState(blockpos1);
               if (blockstate.isAir()) {
                  if (this.hasFlammableNeighbours(level, blockpos1)) {
                     level.setBlockAndUpdate(blockpos1, BaseFireBlock.getState(level, blockpos1));
                     return;
                  }
               } else if (blockstate.blocksMotion()) {
                  return;
               }
            }
         } else {
            for(int k = 0; k < 3; ++k) {
               BlockPos blockpos2 = blockpos.offset(randomsource.nextInt(3) - 1, 0, randomsource.nextInt(3) - 1);
               if (!level.isLoaded(blockpos2)) {
                  return;
               }

               if (level.isEmptyBlock(blockpos2.above()) && this.isFlammable(level, blockpos2)) {
                  level.setBlockAndUpdate(blockpos2.above(), BaseFireBlock.getState(level, blockpos2));
               }
            }
         }

      }
   }

   private boolean hasFlammableNeighbours(LevelReader levelreader, BlockPos blockpos) {
      for(Direction direction : Direction.values()) {
         if (this.isFlammable(levelreader, blockpos.relative(direction))) {
            return true;
         }
      }

      return false;
   }

   private boolean isFlammable(LevelReader levelreader, BlockPos blockpos) {
      return blockpos.getY() >= levelreader.getMinBuildHeight() && blockpos.getY() < levelreader.getMaxBuildHeight() && !levelreader.hasChunkAt(blockpos) ? false : levelreader.getBlockState(blockpos).ignitedByLava();
   }

   @Nullable
   public ParticleOptions getDripParticle() {
      return ParticleTypes.DRIPPING_LAVA;
   }

   protected void beforeDestroyingBlock(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      this.fizz(levelaccessor, blockpos);
   }

   public int getSlopeFindDistance(LevelReader levelreader) {
      return levelreader.dimensionType().ultraWarm() ? 4 : 2;
   }

   public BlockState createLegacyBlock(FluidState fluidstate) {
      return Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(fluidstate)));
   }

   public boolean isSame(Fluid fluid) {
      return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
   }

   public int getDropOff(LevelReader levelreader) {
      return levelreader.dimensionType().ultraWarm() ? 1 : 2;
   }

   public boolean canBeReplacedWith(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos, Fluid fluid, Direction direction) {
      return fluidstate.getHeight(blockgetter, blockpos) >= 0.44444445F && fluid.is(FluidTags.WATER);
   }

   public int getTickDelay(LevelReader levelreader) {
      return levelreader.dimensionType().ultraWarm() ? 10 : 30;
   }

   public int getSpreadDelay(Level level, BlockPos blockpos, FluidState fluidstate, FluidState fluidstate1) {
      int i = this.getTickDelay(level);
      if (!fluidstate.isEmpty() && !fluidstate1.isEmpty() && !fluidstate.getValue(FALLING) && !fluidstate1.getValue(FALLING) && fluidstate1.getHeight(level, blockpos) > fluidstate.getHeight(level, blockpos) && level.getRandom().nextInt(4) != 0) {
         i *= 4;
      }

      return i;
   }

   private void fizz(LevelAccessor levelaccessor, BlockPos blockpos) {
      levelaccessor.levelEvent(1501, blockpos, 0);
   }

   protected boolean canConvertToSource(Level level) {
      return level.getGameRules().getBoolean(GameRules.RULE_LAVA_SOURCE_CONVERSION);
   }

   protected void spreadTo(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, Direction direction, FluidState fluidstate) {
      if (direction == Direction.DOWN) {
         FluidState fluidstate1 = levelaccessor.getFluidState(blockpos);
         if (this.is(FluidTags.LAVA) && fluidstate1.is(FluidTags.WATER)) {
            if (blockstate.getBlock() instanceof LiquidBlock) {
               levelaccessor.setBlock(blockpos, Blocks.STONE.defaultBlockState(), 3);
            }

            this.fizz(levelaccessor, blockpos);
            return;
         }
      }

      super.spreadTo(levelaccessor, blockpos, blockstate, direction, fluidstate);
   }

   protected boolean isRandomlyTicking() {
      return true;
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL_LAVA);
   }

   public static class Flowing extends LavaFluid {
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

   public static class Source extends LavaFluid {
      public int getAmount(FluidState fluidstate) {
         return 8;
      }

      public boolean isSource(FluidState fluidstate) {
         return true;
      }
   }
}
