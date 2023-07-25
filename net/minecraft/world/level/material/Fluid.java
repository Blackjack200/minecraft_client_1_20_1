package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Fluid {
   public static final IdMapper<FluidState> FLUID_STATE_REGISTRY = new IdMapper<>();
   protected final StateDefinition<Fluid, FluidState> stateDefinition;
   private FluidState defaultFluidState;
   private final Holder.Reference<Fluid> builtInRegistryHolder = BuiltInRegistries.FLUID.createIntrusiveHolder(this);

   protected Fluid() {
      StateDefinition.Builder<Fluid, FluidState> statedefinition_builder = new StateDefinition.Builder<>(this);
      this.createFluidStateDefinition(statedefinition_builder);
      this.stateDefinition = statedefinition_builder.create(Fluid::defaultFluidState, FluidState::new);
      this.registerDefaultState(this.stateDefinition.any());
   }

   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> statedefinition_builder) {
   }

   public StateDefinition<Fluid, FluidState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(FluidState fluidstate) {
      this.defaultFluidState = fluidstate;
   }

   public final FluidState defaultFluidState() {
      return this.defaultFluidState;
   }

   public abstract Item getBucket();

   protected void animateTick(Level level, BlockPos blockpos, FluidState fluidstate, RandomSource randomsource) {
   }

   protected void tick(Level level, BlockPos blockpos, FluidState fluidstate) {
   }

   protected void randomTick(Level level, BlockPos blockpos, FluidState fluidstate, RandomSource randomsource) {
   }

   @Nullable
   protected ParticleOptions getDripParticle() {
      return null;
   }

   protected abstract boolean canBeReplacedWith(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos, Fluid fluid, Direction direction);

   protected abstract Vec3 getFlow(BlockGetter blockgetter, BlockPos blockpos, FluidState fluidstate);

   public abstract int getTickDelay(LevelReader levelreader);

   protected boolean isRandomlyTicking() {
      return false;
   }

   protected boolean isEmpty() {
      return false;
   }

   protected abstract float getExplosionResistance();

   public abstract float getHeight(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos);

   public abstract float getOwnHeight(FluidState fluidstate);

   protected abstract BlockState createLegacyBlock(FluidState fluidstate);

   public abstract boolean isSource(FluidState fluidstate);

   public abstract int getAmount(FluidState fluidstate);

   public boolean isSame(Fluid fluid) {
      return fluid == this;
   }

   /** @deprecated */
   @Deprecated
   public boolean is(TagKey<Fluid> tagkey) {
      return this.builtInRegistryHolder.is(tagkey);
   }

   public abstract VoxelShape getShape(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos);

   public Optional<SoundEvent> getPickupSound() {
      return Optional.empty();
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Fluid> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }
}
