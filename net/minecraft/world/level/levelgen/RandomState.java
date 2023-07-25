package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RandomState {
   final PositionalRandomFactory random;
   private final HolderGetter<NormalNoise.NoiseParameters> noises;
   private final NoiseRouter router;
   private final Climate.Sampler sampler;
   private final SurfaceSystem surfaceSystem;
   private final PositionalRandomFactory aquiferRandom;
   private final PositionalRandomFactory oreRandom;
   private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances;
   private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms;

   public static RandomState create(HolderGetter.Provider holdergetter_provider, ResourceKey<NoiseGeneratorSettings> resourcekey, long i) {
      return create(holdergetter_provider.<NoiseGeneratorSettings>lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(resourcekey).value(), holdergetter_provider.lookupOrThrow(Registries.NOISE), i);
   }

   public static RandomState create(NoiseGeneratorSettings noisegeneratorsettings, HolderGetter<NormalNoise.NoiseParameters> holdergetter, long i) {
      return new RandomState(noisegeneratorsettings, holdergetter, i);
   }

   private RandomState(NoiseGeneratorSettings noisegeneratorsettings, HolderGetter<NormalNoise.NoiseParameters> holdergetter, final long i) {
      this.random = noisegeneratorsettings.getRandomSource().newInstance(i).forkPositional();
      this.noises = holdergetter;
      this.aquiferRandom = this.random.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
      this.oreRandom = this.random.fromHashOf(new ResourceLocation("ore")).forkPositional();
      this.noiseIntances = new ConcurrentHashMap<>();
      this.positionalRandoms = new ConcurrentHashMap<>();
      this.surfaceSystem = new SurfaceSystem(this, noisegeneratorsettings.defaultBlock(), noisegeneratorsettings.seaLevel(), this.random);
      final boolean flag = noisegeneratorsettings.useLegacyRandomSource();

      class NoiseWiringHelper implements DensityFunction.Visitor {
         private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

         private RandomSource newLegacyInstance(long ix) {
            return new LegacyRandomSource(i + i);
         }

         public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder densityfunction_noiseholder) {
            Holder<NormalNoise.NoiseParameters> holder = densityfunction_noiseholder.noiseData();
            if (flag) {
               if (holder.is(Noises.TEMPERATURE)) {
                  NormalNoise normalnoise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0D, 1.0D));
                  return new DensityFunction.NoiseHolder(holder, normalnoise);
               }

               if (holder.is(Noises.VEGETATION)) {
                  NormalNoise normalnoise1 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0D, 1.0D));
                  return new DensityFunction.NoiseHolder(holder, normalnoise1);
               }

               if (holder.is(Noises.SHIFT)) {
                  NormalNoise normalnoise2 = NormalNoise.create(RandomState.this.random.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0D));
                  return new DensityFunction.NoiseHolder(holder, normalnoise2);
               }
            }

            NormalNoise normalnoise3 = RandomState.this.getOrCreateNoise(holder.unwrapKey().orElseThrow());
            return new DensityFunction.NoiseHolder(holder, normalnoise3);
         }

         private DensityFunction wrapNew(DensityFunction densityfunction) {
            if (densityfunction instanceof BlendedNoise blendednoise) {
               RandomSource randomsource = flag ? this.newLegacyInstance(0L) : RandomState.this.random.fromHashOf(new ResourceLocation("terrain"));
               return blendednoise.withNewRandom(randomsource);
            } else {
               return (DensityFunction)(densityfunction instanceof DensityFunctions.EndIslandDensityFunction ? new DensityFunctions.EndIslandDensityFunction(i) : densityfunction);
            }
         }

         public DensityFunction apply(DensityFunction densityfunction) {
            return this.wrapped.computeIfAbsent(densityfunction, this::wrapNew);
         }
      }

      this.router = noisegeneratorsettings.noiseRouter().mapAll(new NoiseWiringHelper());
      DensityFunction.Visitor densityfunction_visitor = new DensityFunction.Visitor() {
         private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

         private DensityFunction wrapNew(DensityFunction densityfunction) {
            if (densityfunction instanceof DensityFunctions.HolderHolder densityfunctions_holderholder) {
               return densityfunctions_holderholder.function().value();
            } else if (densityfunction instanceof DensityFunctions.Marker densityfunctions_marker) {
               return densityfunctions_marker.wrapped();
            } else {
               return densityfunction;
            }
         }

         public DensityFunction apply(DensityFunction densityfunction) {
            return this.wrapped.computeIfAbsent(densityfunction, this::wrapNew);
         }
      };
      this.sampler = new Climate.Sampler(this.router.temperature().mapAll(densityfunction_visitor), this.router.vegetation().mapAll(densityfunction_visitor), this.router.continents().mapAll(densityfunction_visitor), this.router.erosion().mapAll(densityfunction_visitor), this.router.depth().mapAll(densityfunction_visitor), this.router.ridges().mapAll(densityfunction_visitor), noisegeneratorsettings.spawnTarget());
   }

   public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> resourcekey) {
      return this.noiseIntances.computeIfAbsent(resourcekey, (resourcekey2) -> Noises.instantiate(this.noises, this.random, resourcekey));
   }

   public PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation resourcelocation) {
      return this.positionalRandoms.computeIfAbsent(resourcelocation, (resourcelocation2) -> this.random.fromHashOf(resourcelocation).forkPositional());
   }

   public NoiseRouter router() {
      return this.router;
   }

   public Climate.Sampler sampler() {
      return this.sampler;
   }

   public SurfaceSystem surfaceSystem() {
      return this.surfaceSystem;
   }

   public PositionalRandomFactory aquiferRandom() {
      return this.aquiferRandom;
   }

   public PositionalRandomFactory oreRandom() {
      return this.oreRandom;
   }
}
