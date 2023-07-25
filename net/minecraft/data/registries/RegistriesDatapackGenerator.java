package net.minecraft.data.registries;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class RegistriesDatapackGenerator implements DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackOutput output;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public RegistriesDatapackGenerator(PackOutput packoutput, CompletableFuture<HolderLookup.Provider> completablefuture) {
      this.registries = completablefuture;
      this.output = packoutput;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      return this.registries.thenCompose((holderlookup_provider) -> {
         DynamicOps<JsonElement> dynamicops = RegistryOps.create(JsonOps.INSTANCE, holderlookup_provider);
         return CompletableFuture.allOf(RegistryDataLoader.WORLDGEN_REGISTRIES.stream().flatMap((registrydataloader_registrydata) -> this.dumpRegistryCap(cachedoutput, holderlookup_provider, dynamicops, registrydataloader_registrydata).stream()).toArray((i) -> new CompletableFuture[i]));
      });
   }

   private <T> Optional<CompletableFuture<?>> dumpRegistryCap(CachedOutput cachedoutput, HolderLookup.Provider holderlookup_provider, DynamicOps<JsonElement> dynamicops, RegistryDataLoader.RegistryData<T> registrydataloader_registrydata) {
      ResourceKey<? extends Registry<T>> resourcekey = registrydataloader_registrydata.key();
      return holderlookup_provider.lookup(resourcekey).map((holderlookup_registrylookup) -> {
         PackOutput.PathProvider packoutput_pathprovider = this.output.createPathProvider(PackOutput.Target.DATA_PACK, resourcekey.location().getPath());
         return CompletableFuture.allOf(holderlookup_registrylookup.listElements().map((holder_reference) -> dumpValue(packoutput_pathprovider.json(holder_reference.key().location()), cachedoutput, dynamicops, registrydataloader_registrydata.elementCodec(), (T)holder_reference.value())).toArray((i) -> new CompletableFuture[i]));
      });
   }

   private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedoutput, DynamicOps<JsonElement> dynamicops, Encoder<E> encoder, E object) {
      Optional<JsonElement> optional = encoder.encodeStart(dynamicops, object).resultOrPartial((s) -> LOGGER.error("Couldn't serialize element {}: {}", path, s));
      return optional.isPresent() ? DataProvider.saveStable(cachedoutput, optional.get(), path) : CompletableFuture.completedFuture((Object)null);
   }

   public final String getName() {
      return "Registries";
   }
}
