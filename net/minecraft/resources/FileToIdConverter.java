package net.minecraft.resources;

import java.util.List;
import java.util.Map;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class FileToIdConverter {
   private final String prefix;
   private final String extension;

   public FileToIdConverter(String s, String s1) {
      this.prefix = s;
      this.extension = s1;
   }

   public static FileToIdConverter json(String s) {
      return new FileToIdConverter(s, ".json");
   }

   public ResourceLocation idToFile(ResourceLocation resourcelocation) {
      return resourcelocation.withPath(this.prefix + "/" + resourcelocation.getPath() + this.extension);
   }

   public ResourceLocation fileToId(ResourceLocation resourcelocation) {
      String s = resourcelocation.getPath();
      return resourcelocation.withPath(s.substring(this.prefix.length() + 1, s.length() - this.extension.length()));
   }

   public Map<ResourceLocation, Resource> listMatchingResources(ResourceManager resourcemanager) {
      return resourcemanager.listResources(this.prefix, (resourcelocation) -> resourcelocation.getPath().endsWith(this.extension));
   }

   public Map<ResourceLocation, List<Resource>> listMatchingResourceStacks(ResourceManager resourcemanager) {
      return resourcemanager.listResourceStacks(this.prefix, (resourcelocation) -> resourcelocation.getPath().endsWith(this.extension));
   }
}
