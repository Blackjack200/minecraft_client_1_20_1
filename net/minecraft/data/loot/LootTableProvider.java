package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackOutput.PathProvider pathProvider;
   private final Set<ResourceLocation> requiredTables;
   private final List<LootTableProvider.SubProviderEntry> subProviders;

   public LootTableProvider(PackOutput packoutput, Set<ResourceLocation> set, List<LootTableProvider.SubProviderEntry> list) {
      this.pathProvider = packoutput.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
      this.subProviders = list;
      this.requiredTables = set;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      final Map<ResourceLocation, LootTable> map = Maps.newHashMap();
      Map<RandomSupport.Seed128bit, ResourceLocation> map1 = new Object2ObjectOpenHashMap<>();
      this.subProviders.forEach((loottableprovider_subproviderentry) -> loottableprovider_subproviderentry.provider().get().generate((resourcelocation3, loottable_builder) -> {
            ResourceLocation resourcelocation4 = map1.put(RandomSequence.seedForKey(resourcelocation3), resourcelocation3);
            if (resourcelocation4 != null) {
               Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + resourcelocation4 + " and " + resourcelocation3);
            }

            loottable_builder.setRandomSequence(resourcelocation3);
            if (map.put(resourcelocation3, loottable_builder.setParamSet(loottableprovider_subproviderentry.paramSet).build()) != null) {
               throw new IllegalStateException("Duplicate loot table " + resourcelocation3);
            }
         }));
      ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
         @Nullable
         public <T> T getElement(LootDataId<T> lootdataid) {
            return (T)(lootdataid.type() == LootDataType.TABLE ? map.get(lootdataid.location()) : null);
         }
      });

      for(ResourceLocation resourcelocation : Sets.difference(this.requiredTables, map.keySet())) {
         validationcontext.reportProblem("Missing built-in table: " + resourcelocation);
      }

      map.forEach((resourcelocation2, loottable1) -> loottable1.validate(validationcontext.setParams(loottable1.getParamSet()).enterElement("{" + resourcelocation2 + "}", new LootDataId<>(LootDataType.TABLE, resourcelocation2))));
      Multimap<String, String> multimap = validationcontext.getProblems();
      if (!multimap.isEmpty()) {
         multimap.forEach((s, s1) -> LOGGER.warn("Found validation problem in {}: {}", s, s1));
         throw new IllegalStateException("Failed to validate loot tables, see logs");
      } else {
         return CompletableFuture.allOf(map.entrySet().stream().map((map_entry) -> {
            ResourceLocation resourcelocation1 = map_entry.getKey();
            LootTable loottable = map_entry.getValue();
            Path path = this.pathProvider.json(resourcelocation1);
            return DataProvider.saveStable(cachedoutput, LootDataType.TABLE.parser().toJsonTree(loottable), path);
         }).toArray((i) -> new CompletableFuture[i]));
      }
   }

   public final String getName() {
      return "Loot Tables";
   }

   public static record SubProviderEntry(Supplier<LootTableSubProvider> provider, LootContextParamSet paramSet) {
      final LootContextParamSet paramSet;
   }
}
