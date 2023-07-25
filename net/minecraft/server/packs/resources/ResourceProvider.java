package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ResourceProvider {
   Optional<Resource> getResource(ResourceLocation resourcelocation);

   default Resource getResourceOrThrow(ResourceLocation resourcelocation) throws FileNotFoundException {
      return this.getResource(resourcelocation).orElseThrow(() -> new FileNotFoundException(resourcelocation.toString()));
   }

   default InputStream open(ResourceLocation resourcelocation) throws IOException {
      return this.getResourceOrThrow(resourcelocation).open();
   }

   default BufferedReader openAsReader(ResourceLocation resourcelocation) throws IOException {
      return this.getResourceOrThrow(resourcelocation).openAsReader();
   }

   static ResourceProvider fromMap(Map<ResourceLocation, Resource> map) {
      return (resourcelocation) -> Optional.ofNullable(map.get(resourcelocation));
   }
}
