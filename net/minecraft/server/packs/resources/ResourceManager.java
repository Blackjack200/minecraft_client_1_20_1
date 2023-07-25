package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager extends ResourceProvider {
   Set<String> getNamespaces();

   List<Resource> getResourceStack(ResourceLocation resourcelocation);

   Map<ResourceLocation, Resource> listResources(String s, Predicate<ResourceLocation> predicate);

   Map<ResourceLocation, List<Resource>> listResourceStacks(String s, Predicate<ResourceLocation> predicate);

   Stream<PackResources> listPacks();

   public static enum Empty implements ResourceManager {
      INSTANCE;

      public Set<String> getNamespaces() {
         return Set.of();
      }

      public Optional<Resource> getResource(ResourceLocation resourcelocation) {
         return Optional.empty();
      }

      public List<Resource> getResourceStack(ResourceLocation resourcelocation) {
         return List.of();
      }

      public Map<ResourceLocation, Resource> listResources(String s, Predicate<ResourceLocation> predicate) {
         return Map.of();
      }

      public Map<ResourceLocation, List<Resource>> listResourceStacks(String s, Predicate<ResourceLocation> predicate) {
         return Map.of();
      }

      public Stream<PackResources> listPacks() {
         return Stream.of();
      }
   }
}
