package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;

public class PackOutput {
   private final Path outputFolder;

   public PackOutput(Path path) {
      this.outputFolder = path;
   }

   public Path getOutputFolder() {
      return this.outputFolder;
   }

   public Path getOutputFolder(PackOutput.Target packoutput_target) {
      return this.getOutputFolder().resolve(packoutput_target.directory);
   }

   public PackOutput.PathProvider createPathProvider(PackOutput.Target packoutput_target, String s) {
      return new PackOutput.PathProvider(this, packoutput_target, s);
   }

   public static class PathProvider {
      private final Path root;
      private final String kind;

      PathProvider(PackOutput packoutput, PackOutput.Target packoutput_target, String s) {
         this.root = packoutput.getOutputFolder(packoutput_target);
         this.kind = s;
      }

      public Path file(ResourceLocation resourcelocation, String s) {
         return this.root.resolve(resourcelocation.getNamespace()).resolve(this.kind).resolve(resourcelocation.getPath() + "." + s);
      }

      public Path json(ResourceLocation resourcelocation) {
         return this.root.resolve(resourcelocation.getNamespace()).resolve(this.kind).resolve(resourcelocation.getPath() + ".json");
      }
   }

   public static enum Target {
      DATA_PACK("data"),
      RESOURCE_PACK("assets"),
      REPORTS("reports");

      final String directory;

      private Target(String s) {
         this.directory = s;
      }
   }
}
