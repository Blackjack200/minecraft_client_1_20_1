package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final LoadingCache<ServerLevel, FeatureCountTracker.LevelData> data = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5L, TimeUnit.MINUTES).build(new CacheLoader<ServerLevel, FeatureCountTracker.LevelData>() {
      public FeatureCountTracker.LevelData load(ServerLevel serverlevel) {
         return new FeatureCountTracker.LevelData(Object2IntMaps.synchronize(new Object2IntOpenHashMap<>()), new MutableInt(0));
      }
   });

   public static void chunkDecorated(ServerLevel serverlevel) {
      try {
         data.get(serverlevel).chunksWithFeatures().increment();
      } catch (Exception var2) {
         LOGGER.error("Failed to increment chunk count", (Throwable)var2);
      }

   }

   public static void featurePlaced(ServerLevel serverlevel, ConfiguredFeature<?, ?> configuredfeature, Optional<PlacedFeature> optional) {
      try {
         data.get(serverlevel).featureData().computeInt(new FeatureCountTracker.FeatureData(configuredfeature, optional), (featurecounttracker_featuredata, integer) -> integer == null ? 1 : integer + 1);
      } catch (Exception var4) {
         LOGGER.error("Failed to increment feature count", (Throwable)var4);
      }

   }

   public static void clearCounts() {
      data.invalidateAll();
      LOGGER.debug("Cleared feature counts");
   }

   public static void logCounts() {
      LOGGER.debug("Logging feature counts:");
      data.asMap().forEach((serverlevel, featurecounttracker_leveldata) -> {
         String s = serverlevel.dimension().location().toString();
         boolean flag = serverlevel.getServer().isRunning();
         Registry<PlacedFeature> registry = serverlevel.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
         String s1 = (flag ? "running" : "dead") + " " + s;
         Integer integer = featurecounttracker_leveldata.chunksWithFeatures().getValue();
         LOGGER.debug(s1 + " total_chunks: " + integer);
         featurecounttracker_leveldata.featureData().forEach((featurecounttracker_featuredata, integer2) -> LOGGER.debug(s1 + " " + String.format(Locale.ROOT, "%10d ", integer2) + String.format(Locale.ROOT, "%10f ", (double)integer2.intValue() / (double)integer.intValue()) + featurecounttracker_featuredata.topFeature().flatMap(registry::getResourceKey).map(ResourceKey::location) + " " + featurecounttracker_featuredata.feature().feature() + " " + featurecounttracker_featuredata.feature()));
      });
   }

   static record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
   }

   static record LevelData(Object2IntMap<FeatureCountTracker.FeatureData> featureData, MutableInt chunksWithFeatures) {
   }
}
