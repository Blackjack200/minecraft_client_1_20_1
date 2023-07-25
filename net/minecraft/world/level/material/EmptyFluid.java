package net.minecraft.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EmptyFluid extends Fluid {
   public Item getBucket() {
      return Items.AIR;
   }

   public boolean canBeReplacedWith(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos, Fluid fluid, Direction direction) {
      return true;
   }

   public Vec3 getFlow(BlockGetter blockgetter, BlockPos blockpos, FluidState fluidstate) {
      return Vec3.ZERO;
   }

   public int getTickDelay(LevelReader levelreader) {
      return 0;
   }

   protected boolean isEmpty() {
      return true;
   }

   protected float getExplosionResistance() {
      return 0.0F;
   }

   public float getHeight(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos) {
      return 0.0F;
   }

   public float getOwnHeight(FluidState fluidstate) {
      return 0.0F;
   }

   protected BlockState createLegacyBlock(FluidState fluidstate) {
      return Blocks.AIR.defaultBlockState();
   }

   public boolean isSource(FluidState fluidstate) {
      return false;
   }

   public int getAmount(FluidState fluidstate) {
      return 0;
   }

   public VoxelShape getShape(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Shapes.empty();
   }
}
