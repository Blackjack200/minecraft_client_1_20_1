package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path topPath;
   private final CompletableFuture<HolderLookup.Provider> registries;
   private static final MapCodec<ResourceKey<Biome>> ENTRY_CODEC = ResourceKey.codec(Registries.BIOME).fieldOf("biome");
   private static final Codec<Climate.ParameterList<ResourceKey<Biome>>> CODEC = Climate.ParameterList.<ResourceKey<Biome>>codec(ENTRY_CODEC).fieldOf("biomes").codec();

   public BiomeParametersDumpReport(PackOutput packoutput, CompletableFuture<HolderLookup.Provider> completablefuture) {
      this.topPath = packoutput.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
      this.registries = completablefuture;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      return this.registries.thenCompose((holderlookup_provider) -> {
         DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, holderlookup_provider);
         List<CompletableFuture<?>> list = new ArrayList<>();
         MultiNoiseBiomeSourceParameterList.knownPresets().forEach((multinoisebiomesourceparameterlist_preset, climate_parameterlist) -> list.add(dumpValue(this.createPath(multinoisebiomesourceparameterlist_preset.id()), cachedoutput, dynamicops, CODEC, climate_parameterlist)));
         return CompletableFuture.allOf(list.toArray((i) -> new CompletableFuture[i]));
      });
   }

   private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedoutput, DynamicOps<JsonElement> dynamicops, Encoder<E> encoder, E object) {
      Optional<JsonElement> optional = encoder.encodeStart(dynamicops, object).resultOrPartial((s) -> LOGGER.error("Couldn't serialize element {}: {}", path, s));
      return optional.isPresent() ? DataProvider.saveStable(cachedoutput, optional.get(), path) : CompletableFuture.completedFuture((Object)null);
   }

   private Path createPath(ResourceLocation resourcelocation) {
      return this.topPath.resolve(resourcelocation.getNamespace()).resolve(resourcelocation.getPath() + ".json");
   }

   public final String getName() {
      return "Biome Parameters";
   }
}
