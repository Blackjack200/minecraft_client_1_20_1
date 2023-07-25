package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FluidState extends StateHolder<Fluid, FluidState> {
   public static final Codec<FluidState> CODEC = codec(BuiltInRegistries.FLUID.byNameCodec(), Fluid::defaultFluidState).stable();
   public static final int AMOUNT_MAX = 9;
   public static final int AMOUNT_FULL = 8;

   public FluidState(Fluid fluid, ImmutableMap<Property<?>, Comparable<?>> immutablemap, MapCodec<FluidState> mapcodec) {
      super(fluid, immutablemap, mapcodec);
   }

   public Fluid getType() {
      return this.owner;
   }

   public boolean isSource() {
      return this.getType().isSource(this);
   }

   public boolean isSourceOfType(Fluid fluid) {
      return this.owner == fluid && this.owner.isSource(this);
   }

   public boolean isEmpty() {
      return this.getType().isEmpty();
   }

   public float getHeight(BlockGetter blockgetter, BlockPos blockpos) {
      return this.getType().getHeight(this, blockgetter, blockpos);
   }

   public float getOwnHeight() {
      return this.getType().getOwnHeight(this);
   }

   public int getAmount() {
      return this.getType().getAmount(this);
   }

   public boolean shouldRenderBackwardUpFace(BlockGetter blockgetter, BlockPos blockpos) {
      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockpos1 = blockpos.offset(i, 0, j);
            FluidState fluidstate = blockgetter.getFluidState(blockpos1);
            if (!fluidstate.getType().isSame(this.getType()) && !blockgetter.getBlockState(blockpos1).isSolidRender(blockgetter, blockpos1)) {
               return true;
            }
         }
      }

      return false;
   }

   public void tick(Level level, BlockPos blockpos) {
      this.getType().tick(level, blockpos, this);
   }

   public void animateTick(Level level, BlockPos blockpos, RandomSource randomsource) {
      this.getType().animateTick(level, blockpos, this, randomsource);
   }

   public boolean isRandomlyTicking() {
      return this.getType().isRandomlyTicking();
   }

   public void randomTick(Level level, BlockPos blockpos, RandomSource randomsource) {
      this.getType().randomTick(level, blockpos, this, randomsource);
   }

   public Vec3 getFlow(BlockGetter blockgetter, BlockPos blockpos) {
      return this.getType().getFlow(blockgetter, blockpos, this);
   }

   public BlockState createLegacyBlock() {
      return this.getType().createLegacyBlock(this);
   }

   @Nullable
   public ParticleOptions getDripParticle() {
      return this.getType().getDripParticle();
   }

   public boolean is(TagKey<Fluid> tagkey) {
      return this.getType().builtInRegistryHolder().is(tagkey);
   }

   public boolean is(HolderSet<Fluid> holderset) {
      return holderset.contains(this.getType().builtInRegistryHolder());
   }

   public boolean is(Fluid fluid) {
      return this.getType() == fluid;
   }

   public float getExplosionResistance() {
      return this.getType().getExplosionResistance();
   }

   public boolean canBeReplacedWith(BlockGetter blockgetter, BlockPos blockpos, Fluid fluid, Direction direction) {
      return this.getType().canBeReplacedWith(this, blockgetter, blockpos, fluid, direction);
   }

   public VoxelShape getShape(BlockGetter blockgetter, BlockPos blockpos) {
      return this.getType().getShape(this, blockgetter, blockpos);
   }

   public Holder<Fluid> holder() {
      return this.owner.builtInRegistryHolder();
   }

   public Stream<TagKey<Fluid>> getTags() {
      return this.owner.builtInRegistryHolder().tags();
   }
}
