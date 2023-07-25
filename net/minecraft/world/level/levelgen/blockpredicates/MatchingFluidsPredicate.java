package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

class MatchingFluidsPredicate extends StateTestingPredicate {
   private final HolderSet<Fluid> fluids;
   public static final Codec<MatchingFluidsPredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> stateTestingCodec(recordcodecbuilder_instance).and(RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluids").forGetter((matchingfluidspredicate) -> matchingfluidspredicate.fluids)).apply(recordcodecbuilder_instance, MatchingFluidsPredicate::new));

   public MatchingFluidsPredicate(Vec3i vec3i, HolderSet<Fluid> holderset) {
      super(vec3i);
      this.fluids = holderset;
   }

   protected boolean test(BlockState blockstate) {
      return blockstate.getFluidState().is(this.fluids);
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_FLUIDS;
   }
}
