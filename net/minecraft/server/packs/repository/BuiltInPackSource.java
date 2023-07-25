package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public abstract class BuiltInPackSource implements RepositorySource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String VANILLA_ID = "vanilla";
   private final PackType packType;
   private final VanillaPackResources vanillaPack;
   private final ResourceLocation packDir;

   public BuiltInPackSource(PackType packtype, VanillaPackResources vanillapackresources, ResourceLocation resourcelocation) {
      this.packType = packtype;
      this.vanillaPack = vanillapackresources;
      this.packDir = resourcelocation;
   }

   public void loadPacks(Consumer<Pack> consumer) {
      Pack pack = this.createVanillaPack(this.vanillaPack);
      if (pack != null) {
         consumer.accept(pack);
      }

      this.listBundledPacks(consumer);
   }

   @Nullable
   protected abstract Pack createVanillaPack(PackResources packresources);

   protected abstract Component getPackTitle(String s);

   public VanillaPackResources getVanillaPack() {
      return this.vanillaPack;
   }

   private void listBundledPacks(Consumer<Pack> consumer) {
      Map<String, Function<String, Pack>> map = new HashMap<>();
      this.populatePackList(map::put);
      map.forEach((s, function) -> {
         Pack pack = function.apply(s);
         if (pack != null) {
            consumer.accept(pack);
         }

      });
   }

   protected void populatePackList(BiConsumer<String, Function<String, Pack>> biconsumer) {
      this.vanillaPack.listRawPaths(this.packType, this.packDir, (path) -> this.discoverPacksInPath(path, biconsumer));
   }

   protected void discoverPacksInPath(@Nullable Path path, BiConsumer<String, Function<String, Pack>> biconsumer) {
      if (path != null && Files.isDirectory(path)) {
         try {
            FolderRepositorySource.discoverPacks(path, true, (path1, pack_resourcessupplier) -> biconsumer.accept(pathToId(path1), (s) -> this.createBuiltinPack(s, pack_resourcessupplier, this.getPackTitle(s))));
         } catch (IOException var4) {
            LOGGER.warn("Failed to discover packs in {}", path, var4);
         }
      }

   }

   private static String pathToId(Path path) {
      return StringUtils.removeEnd(path.getFileName().toString(), ".zip");
   }

   @Nullable
   protected abstract Pack createBuiltinPack(String s, Pack.ResourcesSupplier pack_resourcessupplier, Component component);
}
