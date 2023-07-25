package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator implements DataProvider {
   private final PackOutput output;
   private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

   public PackMetadataGenerator(PackOutput packoutput) {
      this.output = packoutput;
   }

   public <T> PackMetadataGenerator add(MetadataSectionType<T> metadatasectiontype, T object) {
      this.elements.put(metadatasectiontype.getMetadataSectionName(), () -> metadatasectiontype.toJson(object));
      return this;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      JsonObject jsonobject = new JsonObject();
      this.elements.forEach((s, supplier) -> jsonobject.add(s, supplier.get()));
      return DataProvider.saveStable(cachedoutput, jsonobject, this.output.getOutputFolder().resolve("pack.mcmeta"));
   }

   public final String getName() {
      return "Pack Metadata";
   }

   public static PackMetadataGenerator forFeaturePack(PackOutput packoutput, Component component) {
      return (new PackMetadataGenerator(packoutput)).add(PackMetadataSection.TYPE, new PackMetadataSection(component, DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA)));
   }

   public static PackMetadataGenerator forFeaturePack(PackOutput packoutput, Component component, FeatureFlagSet featureflagset) {
      return forFeaturePack(packoutput, component).add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(featureflagset));
   }
}
