package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

public interface PackResources extends AutoCloseable {
   String METADATA_EXTENSION = ".mcmeta";
   String PACK_META = "pack.mcmeta";

   @Nullable
   IoSupplier<InputStream> getRootResource(String... astring);

   @Nullable
   IoSupplier<InputStream> getResource(PackType packtype, ResourceLocation resourcelocation);

   void listResources(PackType packtype, String s, String s1, PackResources.ResourceOutput packresources_resourceoutput);

   Set<String> getNamespaces(PackType packtype);

   @Nullable
   <T> T getMetadataSection(MetadataSectionSerializer<T> metadatasectionserializer) throws IOException;

   String packId();

   default boolean isBuiltin() {
      return false;
   }

   void close();

   @FunctionalInterface
   public interface ResourceOutput extends BiConsumer<ResourceLocation, IoSupplier<InputStream>> {
   }
}
