package net.minecraft.world.level.levelgen;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
   public static final float GLOBAL_OFFSET = -0.50375F;
   private static final float ORE_THICKNESS = 0.08F;
   private static final double VEININESS_FREQUENCY = 1.5D;
   private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5D;
   private static final double SURFACE_DENSITY_THRESHOLD = 1.5625D;
   private static final double CHEESE_NOISE_TARGET = -0.703125D;
   public static final int ISLAND_CHUNK_DISTANCE = 64;
   public static final long ISLAND_CHUNK_DISTANCE_SQR = 4096L;
   private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0D);
   private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
   private static final ResourceKey<DensityFunction> ZERO = createKey("zero");
   private static final ResourceKey<DensityFunction> Y = createKey("y");
   private static final ResourceKey<DensityFunction> SHIFT_X = createKey("shift_x");
   private static final ResourceKey<DensityFunction> SHIFT_Z = createKey("shift_z");
   private static final ResourceKey<DensityFunction> BASE_3D_NOISE_OVERWORLD = createKey("overworld/base_3d_noise");
   private static final ResourceKey<DensityFunction> BASE_3D_NOISE_NETHER = createKey("nether/base_3d_noise");
   private static final ResourceKey<DensityFunction> BASE_3D_NOISE_END = createKey("end/base_3d_noise");
   public static final ResourceKey<DensityFunction> CONTINENTS = createKey("overworld/continents");
   public static final ResourceKey<DensityFunction> EROSION = createKey("overworld/erosion");
   public static final ResourceKey<DensityFunction> RIDGES = createKey("overworld/ridges");
   public static final ResourceKey<DensityFunction> RIDGES_FOLDED = createKey("overworld/ridges_folded");
   public static final ResourceKey<DensityFunction> OFFSET = createKey("overworld/offset");
   public static final ResourceKey<DensityFunction> FACTOR = createKey("overworld/factor");
   public static final ResourceKey<DensityFunction> JAGGEDNESS = createKey("overworld/jaggedness");
   public static final ResourceKey<DensityFunction> DEPTH = createKey("overworld/depth");
   private static final ResourceKey<DensityFunction> SLOPED_CHEESE = createKey("overworld/sloped_cheese");
   public static final ResourceKey<DensityFunction> CONTINENTS_LARGE = createKey("overworld_large_biomes/continents");
   public static final ResourceKey<DensityFunction> EROSION_LARGE = createKey("overworld_large_biomes/erosion");
   private static final ResourceKey<DensityFunction> OFFSET_LARGE = createKey("overworld_large_biomes/offset");
   private static final ResourceKey<DensityFunction> FACTOR_LARGE = createKey("overworld_large_biomes/factor");
   private static final ResourceKey<DensityFunction> JAGGEDNESS_LARGE = createKey("overworld_large_biomes/jaggedness");
   private static final ResourceKey<DensityFunction> DEPTH_LARGE = createKey("overworld_large_biomes/depth");
   private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = createKey("overworld_large_biomes/sloped_cheese");
   private static final ResourceKey<DensityFunction> OFFSET_AMPLIFIED = createKey("overworld_amplified/offset");
   private static final ResourceKey<DensityFunction> FACTOR_AMPLIFIED = createKey("overworld_amplified/factor");
   private static final ResourceKey<DensityFunction> JAGGEDNESS_AMPLIFIED = createKey("overworld_amplified/jaggedness");
   private static final ResourceKey<DensityFunction> DEPTH_AMPLIFIED = createKey("overworld_amplified/depth");
   private static final ResourceKey<DensityFunction> SLOPED_CHEESE_AMPLIFIED = createKey("overworld_amplified/sloped_cheese");
   private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = createKey("end/sloped_cheese");
   private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = createKey("overworld/caves/spaghetti_roughness_function");
   private static final ResourceKey<DensityFunction> ENTRANCES = createKey("overworld/caves/entrances");
   private static final ResourceKey<DensityFunction> NOODLE = createKey("overworld/caves/noodle");
   private static final ResourceKey<DensityFunction> PILLARS = createKey("overworld/caves/pillars");
   private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = createKey("overworld/caves/spaghetti_2d_thickness_modulator");
   private static final ResourceKey<DensityFunction> SPAGHETTI_2D = createKey("overworld/caves/spaghetti_2d");

   private static ResourceKey<DensityFunction> createKey(String s) {
      return ResourceKey.create(Registries.DENSITY_FUNCTION, new ResourceLocation(s));
   }

   public static Holder<? extends DensityFunction> bootstrap(BootstapContext<DensityFunction> bootstapcontext) {
      HolderGetter<NormalNoise.NoiseParameters> holdergetter = bootstapcontext.lookup(Registries.NOISE);
      HolderGetter<DensityFunction> holdergetter1 = bootstapcontext.lookup(Registries.DENSITY_FUNCTION);
      bootstapcontext.register(ZERO, DensityFunctions.zero());
      int i = DimensionType.MIN_Y * 2;
      int j = DimensionType.MAX_Y * 2;
      bootstapcontext.register(Y, DensityFunctions.yClampedGradient(i, j, (double)i, (double)j));
      DensityFunction densityfunction = registerAndWrap(bootstapcontext, SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(holdergetter.getOrThrow(Noises.SHIFT)))));
      DensityFunction densityfunction1 = registerAndWrap(bootstapcontext, SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(holdergetter.getOrThrow(Noises.SHIFT)))));
      bootstapcontext.register(BASE_3D_NOISE_OVERWORLD, BlendedNoise.createUnseeded(0.25D, 0.125D, 80.0D, 160.0D, 8.0D));
      bootstapcontext.register(BASE_3D_NOISE_NETHER, BlendedNoise.createUnseeded(0.25D, 0.375D, 80.0D, 60.0D, 8.0D));
      bootstapcontext.register(BASE_3D_NOISE_END, BlendedNoise.createUnseeded(0.25D, 0.25D, 80.0D, 160.0D, 4.0D));
      Holder<DensityFunction> holder = bootstapcontext.register(CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.CONTINENTALNESS))));
      Holder<DensityFunction> holder1 = bootstapcontext.register(EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.EROSION))));
      DensityFunction densityfunction2 = registerAndWrap(bootstapcontext, RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.RIDGE))));
      bootstapcontext.register(RIDGES_FOLDED, peaksAndValleys(densityfunction2));
      DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter.getOrThrow(Noises.JAGGED), 1500.0D, 0.0D);
      registerTerrainNoises(bootstapcontext, holdergetter1, densityfunction3, holder, holder1, OFFSET, FACTOR, JAGGEDNESS, DEPTH, SLOPED_CHEESE, false);
      Holder<DensityFunction> holder2 = bootstapcontext.register(CONTINENTS_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.CONTINENTALNESS_LARGE))));
      Holder<DensityFunction> holder3 = bootstapcontext.register(EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityfunction, densityfunction1, 0.25D, holdergetter.getOrThrow(Noises.EROSION_LARGE))));
      registerTerrainNoises(bootstapcontext, holdergetter1, densityfunction3, holder2, holder3, OFFSET_LARGE, FACTOR_LARGE, JAGGEDNESS_LARGE, DEPTH_LARGE, SLOPED_CHEESE_LARGE, false);
      registerTerrainNoises(bootstapcontext, holdergetter1, densityfunction3, holder, holder1, OFFSET_AMPLIFIED, FACTOR_AMPLIFIED, JAGGEDNESS_AMPLIFIED, DEPTH_AMPLIFIED, SLOPED_CHEESE_AMPLIFIED, true);
      bootstapcontext.register(SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), getFunction(holdergetter1, BASE_3D_NOISE_END)));
      bootstapcontext.register(SPAGHETTI_ROUGHNESS_FUNCTION, spaghettiRoughnessFunction(holdergetter));
      bootstapcontext.register(SPAGHETTI_2D_THICKNESS_MODULATOR, DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.SPAGHETTI_2D_THICKNESS), 2.0D, 1.0D, -0.6D, -1.3D)));
      bootstapcontext.register(SPAGHETTI_2D, spaghetti2D(holdergetter1, holdergetter));
      bootstapcontext.register(ENTRANCES, entrances(holdergetter1, holdergetter));
      bootstapcontext.register(NOODLE, noodle(holdergetter1, holdergetter));
      return bootstapcontext.register(PILLARS, pillars(holdergetter));
   }

   private static void registerTerrainNoises(BootstapContext<DensityFunction> bootstapcontext, HolderGetter<DensityFunction> holdergetter, DensityFunction densityfunction, Holder<DensityFunction> holder, Holder<DensityFunction> holder1, ResourceKey<DensityFunction> resourcekey, ResourceKey<DensityFunction> resourcekey1, ResourceKey<DensityFunction> resourcekey2, ResourceKey<DensityFunction> resourcekey3, ResourceKey<DensityFunction> resourcekey4, boolean flag) {
      DensityFunctions.Spline.Coordinate densityfunctions_spline_coordinate = new DensityFunctions.Spline.Coordinate(holder);
      DensityFunctions.Spline.Coordinate densityfunctions_spline_coordinate1 = new DensityFunctions.Spline.Coordinate(holder1);
      DensityFunctions.Spline.Coordinate densityfunctions_spline_coordinate2 = new DensityFunctions.Spline.Coordinate(holdergetter.getOrThrow(RIDGES));
      DensityFunctions.Spline.Coordinate densityfunctions_spline_coordinate3 = new DensityFunctions.Spline.Coordinate(holdergetter.getOrThrow(RIDGES_FOLDED));
      DensityFunction densityfunction1 = registerAndWrap(bootstapcontext, resourcekey, splineWithBlending(DensityFunctions.add(DensityFunctions.constant((double)-0.50375F), DensityFunctions.spline(TerrainProvider.overworldOffset(densityfunctions_spline_coordinate, densityfunctions_spline_coordinate1, densityfunctions_spline_coordinate3, flag))), DensityFunctions.blendOffset()));
      DensityFunction densityfunction2 = registerAndWrap(bootstapcontext, resourcekey1, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldFactor(densityfunctions_spline_coordinate, densityfunctions_spline_coordinate1, densityfunctions_spline_coordinate2, densityfunctions_spline_coordinate3, flag)), BLENDING_FACTOR));
      DensityFunction densityfunction3 = registerAndWrap(bootstapcontext, resourcekey3, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5D, -1.5D), densityfunction1));
      DensityFunction densityfunction4 = registerAndWrap(bootstapcontext, resourcekey2, splineWithBlending(DensityFunctions.spline(TerrainProvider.overworldJaggedness(densityfunctions_spline_coordinate, densityfunctions_spline_coordinate1, densityfunctions_spline_coordinate2, densityfunctions_spline_coordinate3, flag)), BLENDING_JAGGEDNESS));
      DensityFunction densityfunction5 = DensityFunctions.mul(densityfunction4, densityfunction.halfNegative());
      DensityFunction densityfunction6 = noiseGradientDensity(densityfunction2, DensityFunctions.add(densityfunction3, densityfunction5));
      bootstapcontext.register(resourcekey4, DensityFunctions.add(densityfunction6, getFunction(holdergetter, BASE_3D_NOISE_OVERWORLD)));
   }

   private static DensityFunction registerAndWrap(BootstapContext<DensityFunction> bootstapcontext, ResourceKey<DensityFunction> resourcekey, DensityFunction densityfunction) {
      return new DensityFunctions.HolderHolder(bootstapcontext.register(resourcekey, densityfunction));
   }

   private static DensityFunction getFunction(HolderGetter<DensityFunction> holdergetter, ResourceKey<DensityFunction> resourcekey) {
      return new DensityFunctions.HolderHolder(holdergetter.getOrThrow(resourcekey));
   }

   private static DensityFunction peaksAndValleys(DensityFunction densityfunction) {
      return DensityFunctions.mul(DensityFunctions.add(DensityFunctions.add(densityfunction.abs(), DensityFunctions.constant(-0.6666666666666666D)).abs(), DensityFunctions.constant(-0.3333333333333333D)), DensityFunctions.constant(-3.0D));
   }

   public static float peaksAndValleys(float f) {
      return -(Math.abs(Math.abs(f) - 0.6666667F) - 0.33333334F) * 3.0F;
   }

   private static DensityFunction spaghettiRoughnessFunction(HolderGetter<NormalNoise.NoiseParameters> holdergetter) {
      DensityFunction densityfunction = DensityFunctions.noise(holdergetter.getOrThrow(Noises.SPAGHETTI_ROUGHNESS));
      DensityFunction densityfunction1 = DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0D, -0.1D);
      return DensityFunctions.cacheOnce(DensityFunctions.mul(densityfunction1, DensityFunctions.add(densityfunction.abs(), DensityFunctions.constant(-0.4D))));
   }

   private static DensityFunction entrances(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1) {
      DensityFunction densityfunction = DensityFunctions.cacheOnce(DensityFunctions.noise(holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_RARITY), 2.0D, 1.0D));
      DensityFunction densityfunction1 = DensityFunctions.mappedNoise(holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_THICKNESS), -0.065D, -0.088D);
      DensityFunction densityfunction2 = DensityFunctions.weirdScaledSampler(densityfunction, holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_1), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1);
      DensityFunction densityfunction3 = DensityFunctions.weirdScaledSampler(densityfunction, holdergetter1.getOrThrow(Noises.SPAGHETTI_3D_2), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1);
      DensityFunction densityfunction4 = DensityFunctions.add(DensityFunctions.max(densityfunction2, densityfunction3), densityfunction1).clamp(-1.0D, 1.0D);
      DensityFunction densityfunction5 = getFunction(holdergetter, SPAGHETTI_ROUGHNESS_FUNCTION);
      DensityFunction densityfunction6 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.CAVE_ENTRANCE), 0.75D, 0.5D);
      DensityFunction densityfunction7 = DensityFunctions.add(DensityFunctions.add(densityfunction6, DensityFunctions.constant(0.37D)), DensityFunctions.yClampedGradient(-10, 30, 0.3D, 0.0D));
      return DensityFunctions.cacheOnce(DensityFunctions.min(densityfunction7, DensityFunctions.add(densityfunction5, densityfunction4)));
   }

   private static DensityFunction noodle(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1) {
      DensityFunction densityfunction = getFunction(holdergetter, Y);
      int i = -64;
      int j = -60;
      int k = 320;
      DensityFunction densityfunction1 = yLimitedInterpolatable(densityfunction, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.NOODLE), 1.0D, 1.0D), -60, 320, -1);
      DensityFunction densityfunction2 = yLimitedInterpolatable(densityfunction, DensityFunctions.mappedNoise(holdergetter1.getOrThrow(Noises.NOODLE_THICKNESS), 1.0D, 1.0D, -0.05D, -0.1D), -60, 320, 0);
      double d0 = 2.6666666666666665D;
      DensityFunction densityfunction3 = yLimitedInterpolatable(densityfunction, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.NOODLE_RIDGE_A), 2.6666666666666665D, 2.6666666666666665D), -60, 320, 0);
      DensityFunction densityfunction4 = yLimitedInterpolatable(densityfunction, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.NOODLE_RIDGE_B), 2.6666666666666665D, 2.6666666666666665D), -60, 320, 0);
      DensityFunction densityfunction5 = DensityFunctions.mul(DensityFunctions.constant(1.5D), DensityFunctions.max(densityfunction3.abs(), densityfunction4.abs()));
      return DensityFunctions.rangeChoice(densityfunction1, -1000000.0D, 0.0D, DensityFunctions.constant(64.0D), DensityFunctions.add(densityfunction2, densityfunction5));
   }

   private static DensityFunction pillars(HolderGetter<NormalNoise.NoiseParameters> holdergetter) {
      double d0 = 25.0D;
      double d1 = 0.3D;
      DensityFunction densityfunction = DensityFunctions.noise(holdergetter.getOrThrow(Noises.PILLAR), 25.0D, 0.3D);
      DensityFunction densityfunction1 = DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.PILLAR_RARENESS), 0.0D, -2.0D);
      DensityFunction densityfunction2 = DensityFunctions.mappedNoise(holdergetter.getOrThrow(Noises.PILLAR_THICKNESS), 0.0D, 1.1D);
      DensityFunction densityfunction3 = DensityFunctions.add(DensityFunctions.mul(densityfunction, DensityFunctions.constant(2.0D)), densityfunction1);
      return DensityFunctions.cacheOnce(DensityFunctions.mul(densityfunction3, densityfunction2.cube()));
   }

   private static DensityFunction spaghetti2D(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1) {
      DensityFunction densityfunction = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.SPAGHETTI_2D_MODULATOR), 2.0D, 1.0D);
      DensityFunction densityfunction1 = DensityFunctions.weirdScaledSampler(densityfunction, holdergetter1.getOrThrow(Noises.SPAGHETTI_2D), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2);
      DensityFunction densityfunction2 = DensityFunctions.mappedNoise(holdergetter1.getOrThrow(Noises.SPAGHETTI_2D_ELEVATION), 0.0D, (double)Math.floorDiv(-64, 8), 8.0D);
      DensityFunction densityfunction3 = getFunction(holdergetter, SPAGHETTI_2D_THICKNESS_MODULATOR);
      DensityFunction densityfunction4 = DensityFunctions.add(densityfunction2, DensityFunctions.yClampedGradient(-64, 320, 8.0D, -40.0D)).abs();
      DensityFunction densityfunction5 = DensityFunctions.add(densityfunction4, densityfunction3).cube();
      double d0 = 0.083D;
      DensityFunction densityfunction6 = DensityFunctions.add(densityfunction1, DensityFunctions.mul(DensityFunctions.constant(0.083D), densityfunction3));
      return DensityFunctions.max(densityfunction6, densityfunction5).clamp(-1.0D, 1.0D);
   }

   private static DensityFunction underground(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1, DensityFunction densityfunction) {
      DensityFunction densityfunction1 = getFunction(holdergetter, SPAGHETTI_2D);
      DensityFunction densityfunction2 = getFunction(holdergetter, SPAGHETTI_ROUGHNESS_FUNCTION);
      DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.CAVE_LAYER), 8.0D);
      DensityFunction densityfunction4 = DensityFunctions.mul(DensityFunctions.constant(4.0D), densityfunction3.square());
      DensityFunction densityfunction5 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.CAVE_CHEESE), 0.6666666666666666D);
      DensityFunction densityfunction6 = DensityFunctions.add(DensityFunctions.add(DensityFunctions.constant(0.27D), densityfunction5).clamp(-1.0D, 1.0D), DensityFunctions.add(DensityFunctions.constant(1.5D), DensityFunctions.mul(DensityFunctions.constant(-0.64D), densityfunction)).clamp(0.0D, 0.5D));
      DensityFunction densityfunction7 = DensityFunctions.add(densityfunction4, densityfunction6);
      DensityFunction densityfunction8 = DensityFunctions.min(DensityFunctions.min(densityfunction7, getFunction(holdergetter, ENTRANCES)), DensityFunctions.add(densityfunction1, densityfunction2));
      DensityFunction densityfunction9 = getFunction(holdergetter, PILLARS);
      DensityFunction densityfunction10 = DensityFunctions.rangeChoice(densityfunction9, -1000000.0D, 0.03D, DensityFunctions.constant(-1000000.0D), densityfunction9);
      return DensityFunctions.max(densityfunction8, densityfunction10);
   }

   private static DensityFunction postProcess(DensityFunction densityfunction) {
      DensityFunction densityfunction1 = DensityFunctions.blendDensity(densityfunction);
      return DensityFunctions.mul(DensityFunctions.interpolated(densityfunction1), DensityFunctions.constant(0.64D)).squeeze();
   }

   protected static NoiseRouter overworld(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1, boolean flag, boolean flag1) {
      DensityFunction densityfunction = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_BARRIER), 0.5D);
      DensityFunction densityfunction1 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67D);
      DensityFunction densityfunction2 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143D);
      DensityFunction densityfunction3 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.AQUIFER_LAVA));
      DensityFunction densityfunction4 = getFunction(holdergetter, SHIFT_X);
      DensityFunction densityfunction5 = getFunction(holdergetter, SHIFT_Z);
      DensityFunction densityfunction6 = DensityFunctions.shiftedNoise2d(densityfunction4, densityfunction5, 0.25D, holdergetter1.getOrThrow(flag ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE));
      DensityFunction densityfunction7 = DensityFunctions.shiftedNoise2d(densityfunction4, densityfunction5, 0.25D, holdergetter1.getOrThrow(flag ? Noises.VEGETATION_LARGE : Noises.VEGETATION));
      DensityFunction densityfunction8 = getFunction(holdergetter, flag ? FACTOR_LARGE : (flag1 ? FACTOR_AMPLIFIED : FACTOR));
      DensityFunction densityfunction9 = getFunction(holdergetter, flag ? DEPTH_LARGE : (flag1 ? DEPTH_AMPLIFIED : DEPTH));
      DensityFunction densityfunction10 = noiseGradientDensity(DensityFunctions.cache2d(densityfunction8), densityfunction9);
      DensityFunction densityfunction11 = getFunction(holdergetter, flag ? SLOPED_CHEESE_LARGE : (flag1 ? SLOPED_CHEESE_AMPLIFIED : SLOPED_CHEESE));
      DensityFunction densityfunction12 = DensityFunctions.min(densityfunction11, DensityFunctions.mul(DensityFunctions.constant(5.0D), getFunction(holdergetter, ENTRANCES)));
      DensityFunction densityfunction13 = DensityFunctions.rangeChoice(densityfunction11, -1000000.0D, 1.5625D, densityfunction12, underground(holdergetter, holdergetter1, densityfunction11));
      DensityFunction densityfunction14 = DensityFunctions.min(postProcess(slideOverworld(flag1, densityfunction13)), getFunction(holdergetter, NOODLE));
      DensityFunction densityfunction15 = getFunction(holdergetter, Y);
      int i = Stream.of(OreVeinifier.VeinType.values()).mapToInt((oreveinifier_veintype1) -> oreveinifier_veintype1.minY).min().orElse(-DimensionType.MIN_Y * 2);
      int j = Stream.of(OreVeinifier.VeinType.values()).mapToInt((oreveinifier_veintype) -> oreveinifier_veintype.maxY).max().orElse(-DimensionType.MIN_Y * 2);
      DensityFunction densityfunction16 = yLimitedInterpolatable(densityfunction15, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_VEININESS), 1.5D, 1.5D), i, j, 0);
      float f = 4.0F;
      DensityFunction densityfunction17 = yLimitedInterpolatable(densityfunction15, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_VEIN_A), 4.0D, 4.0D), i, j, 0).abs();
      DensityFunction densityfunction18 = yLimitedInterpolatable(densityfunction15, DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_VEIN_B), 4.0D, 4.0D), i, j, 0).abs();
      DensityFunction densityfunction19 = DensityFunctions.add(DensityFunctions.constant((double)-0.08F), DensityFunctions.max(densityfunction17, densityfunction18));
      DensityFunction densityfunction20 = DensityFunctions.noise(holdergetter1.getOrThrow(Noises.ORE_GAP));
      return new NoiseRouter(densityfunction, densityfunction1, densityfunction2, densityfunction3, densityfunction6, densityfunction7, getFunction(holdergetter, flag ? CONTINENTS_LARGE : CONTINENTS), getFunction(holdergetter, flag ? EROSION_LARGE : EROSION), densityfunction9, getFunction(holdergetter, RIDGES), slideOverworld(flag1, DensityFunctions.add(densityfunction10, DensityFunctions.constant(-0.703125D)).clamp(-64.0D, 64.0D)), densityfunction14, densityfunction16, densityfunction19, densityfunction20);
   }

   private static NoiseRouter noNewCaves(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1, DensityFunction densityfunction) {
      DensityFunction densityfunction1 = getFunction(holdergetter, SHIFT_X);
      DensityFunction densityfunction2 = getFunction(holdergetter, SHIFT_Z);
      DensityFunction densityfunction3 = DensityFunctions.shiftedNoise2d(densityfunction1, densityfunction2, 0.25D, holdergetter1.getOrThrow(Noises.TEMPERATURE));
      DensityFunction densityfunction4 = DensityFunctions.shiftedNoise2d(densityfunction1, densityfunction2, 0.25D, holdergetter1.getOrThrow(Noises.VEGETATION));
      DensityFunction densityfunction5 = postProcess(densityfunction);
      return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction3, densityfunction4, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction5, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
   }

   private static DensityFunction slideOverworld(boolean flag, DensityFunction densityfunction) {
      return slide(densityfunction, -64, 384, flag ? 16 : 80, flag ? 0 : 64, -0.078125D, 0, 24, flag ? 0.4D : 0.1171875D);
   }

   private static DensityFunction slideNetherLike(HolderGetter<DensityFunction> holdergetter, int i, int j) {
      return slide(getFunction(holdergetter, BASE_3D_NOISE_NETHER), i, j, 24, 0, 0.9375D, -8, 24, 2.5D);
   }

   private static DensityFunction slideEndLike(DensityFunction densityfunction, int i, int j) {
      return slide(densityfunction, i, j, 72, -184, -23.4375D, 4, 32, -0.234375D);
   }

   protected static NoiseRouter nether(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1) {
      return noNewCaves(holdergetter, holdergetter1, slideNetherLike(holdergetter, 0, 128));
   }

   protected static NoiseRouter caves(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1) {
      return noNewCaves(holdergetter, holdergetter1, slideNetherLike(holdergetter, -64, 192));
   }

   protected static NoiseRouter floatingIslands(HolderGetter<DensityFunction> holdergetter, HolderGetter<NormalNoise.NoiseParameters> holdergetter1) {
      return noNewCaves(holdergetter, holdergetter1, slideEndLike(getFunction(holdergetter, BASE_3D_NOISE_END), 0, 256));
   }

   private static DensityFunction slideEnd(DensityFunction densityfunction) {
      return slideEndLike(densityfunction, 0, 128);
   }

   protected static NoiseRouter end(HolderGetter<DensityFunction> holdergetter) {
      DensityFunction densityfunction = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
      DensityFunction densityfunction1 = postProcess(slideEnd(getFunction(holdergetter, SLOPED_CHEESE_END)));
      return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityfunction, DensityFunctions.zero(), DensityFunctions.zero(), slideEnd(DensityFunctions.add(densityfunction, DensityFunctions.constant(-0.703125D))), densityfunction1, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
   }

   protected static NoiseRouter none() {
      return new NoiseRouter(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
   }

   private static DensityFunction splineWithBlending(DensityFunction densityfunction, DensityFunction densityfunction1) {
      DensityFunction densityfunction2 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityfunction1, densityfunction);
      return DensityFunctions.flatCache(DensityFunctions.cache2d(densityfunction2));
   }

   private static DensityFunction noiseGradientDensity(DensityFunction densityfunction, DensityFunction densityfunction1) {
      DensityFunction densityfunction2 = DensityFunctions.mul(densityfunction1, densityfunction);
      return DensityFunctions.mul(DensityFunctions.constant(4.0D), densityfunction2.quarterNegative());
   }

   private static DensityFunction yLimitedInterpolatable(DensityFunction densityfunction, DensityFunction densityfunction1, int i, int j, int k) {
      return DensityFunctions.interpolated(DensityFunctions.rangeChoice(densityfunction, (double)i, (double)(j + 1), densityfunction1, DensityFunctions.constant((double)k)));
   }

   private static DensityFunction slide(DensityFunction densityfunction, int i, int j, int k, int l, double d0, int i1, int j1, double d1) {
      DensityFunction densityfunction2 = DensityFunctions.yClampedGradient(i + j - k, i + j - l, 1.0D, 0.0D);
      DensityFunction densityfunction1 = DensityFunctions.lerp(densityfunction2, d0, densityfunction);
      DensityFunction densityfunction3 = DensityFunctions.yClampedGradient(i + i1, i + j1, 0.0D, 1.0D);
      return DensityFunctions.lerp(densityfunction3, d1, densityfunction1);
   }

   protected static final class QuantizedSpaghettiRarity {
      protected static double getSphaghettiRarity2D(double d0) {
         if (d0 < -0.75D) {
            return 0.5D;
         } else if (d0 < -0.5D) {
            return 0.75D;
         } else if (d0 < 0.5D) {
            return 1.0D;
         } else {
            return d0 < 0.75D ? 2.0D : 3.0D;
         }
      }

      protected static double getSpaghettiRarity3D(double d0) {
         if (d0 < -0.5D) {
            return 0.75D;
         } else if (d0 < 0.0D) {
            return 1.0D;
         } else {
            return d0 < 0.5D ? 1.5D : 2.0D;
         }
      }
   }
}
