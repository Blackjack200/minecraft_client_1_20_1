package net.minecraft.client.resources;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

public class ClientPackSource extends BuiltInPackSource {
   private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(Component.translatable("resourcePack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
   private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(PackMetadataSection.TYPE, VERSION_METADATA_SECTION);
   private static final Component VANILLA_NAME = Component.translatable("resourcePack.vanilla.name");
   public static final String HIGH_CONTRAST_PACK = "high_contrast";
   private static final Map<String, Component> SPECIAL_PACK_NAMES = Map.of("programmer_art", Component.translatable("resourcePack.programmer_art.name"), "high_contrast", Component.translatable("resourcePack.high_contrast.name"));
   private static final ResourceLocation PACKS_DIR = new ResourceLocation("minecraft", "resourcepacks");
   @Nullable
   private final Path externalAssetDir;

   public ClientPackSource(Path path) {
      super(PackType.CLIENT_RESOURCES, createVanillaPackSource(path), PACKS_DIR);
      this.externalAssetDir = this.findExplodedAssetPacks(path);
   }

   @Nullable
   private Path findExplodedAssetPacks(Path path) {
      if (SharedConstants.IS_RUNNING_IN_IDE && path.getFileSystem() == FileSystems.getDefault()) {
         Path path1 = path.getParent().resolve("resourcepacks");
         if (Files.isDirectory(path1)) {
            return path1;
         }
      }

      return null;
   }

   private static VanillaPackResources createVanillaPackSource(Path path) {
      VanillaPackResourcesBuilder vanillapackresourcesbuilder = (new VanillaPackResourcesBuilder()).setMetadata(BUILT_IN_METADATA).exposeNamespace("minecraft", "realms");
      return vanillapackresourcesbuilder.applyDevelopmentConfig().pushJarResources().pushAssetPath(PackType.CLIENT_RESOURCES, path).build();
   }

   protected Component getPackTitle(String s) {
      Component component = SPECIAL_PACK_NAMES.get(s);
      return (Component)(component != null ? component : Component.literal(s));
   }

   @Nullable
   protected Pack createVanillaPack(PackResources packresources) {
      return Pack.readMetaAndCreate("vanilla", VANILLA_NAME, true, (s) -> packresources, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
   }

   @Nullable
   protected Pack createBuiltinPack(String s, Pack.ResourcesSupplier pack_resourcessupplier, Component component) {
      return Pack.readMetaAndCreate(s, component, false, pack_resourcessupplier, PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
   }

   protected void populatePackList(BiConsumer<String, Function<String, Pack>> biconsumer) {
      super.populatePackList(biconsumer);
      if (this.externalAssetDir != null) {
         this.discoverPacksInPath(this.externalAssetDir, biconsumer);
      }

   }
}
