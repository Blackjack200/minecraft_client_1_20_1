package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
   private final PackOutput output;

   public RegistryDumpReport(PackOutput packoutput) {
      this.output = packoutput;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      JsonObject jsonobject = new JsonObject();
      BuiltInRegistries.REGISTRY.holders().forEach((holder_reference) -> jsonobject.add(holder_reference.key().location().toString(), dumpRegistry(holder_reference.value())));
      Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registries.json");
      return DataProvider.saveStable(cachedoutput, jsonobject, path);
   }

   private static <T> JsonElement dumpRegistry(Registry<T> registry) {
      JsonObject jsonobject = new JsonObject();
      if (registry instanceof DefaultedRegistry) {
         ResourceLocation resourcelocation = ((DefaultedRegistry)registry).getDefaultKey();
         jsonobject.addProperty("default", resourcelocation.toString());
      }

      int i = BuiltInRegistries.REGISTRY.getId(registry);
      jsonobject.addProperty("protocol_id", i);
      JsonObject jsonobject1 = new JsonObject();
      registry.holders().forEach((holder_reference) -> {
         T object = holder_reference.value();
         int j = registry.getId(object);
         JsonObject jsonobject3 = new JsonObject();
         jsonobject3.addProperty("protocol_id", j);
         jsonobject1.add(holder_reference.key().location().toString(), jsonobject3);
      });
      jsonobject.add("entries", jsonobject1);
      return jsonobject;
   }

   public final String getName() {
      return "Registry Dump";
   }
}
