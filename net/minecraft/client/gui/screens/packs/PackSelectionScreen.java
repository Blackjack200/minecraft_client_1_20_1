package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class PackSelectionScreen extends Screen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int LIST_WIDTH = 200;
   private static final Component DRAG_AND_DROP = Component.translatable("pack.dropInfo").withStyle(ChatFormatting.GRAY);
   private static final Component DIRECTORY_BUTTON_TOOLTIP = Component.translatable("pack.folderInfo");
   private static final int RELOAD_COOLDOWN = 20;
   private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
   private final PackSelectionModel model;
   @Nullable
   private PackSelectionScreen.Watcher watcher;
   private long ticksToReload;
   private TransferableSelectionList availablePackList;
   private TransferableSelectionList selectedPackList;
   private final Path packDir;
   private Button doneButton;
   private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();

   public PackSelectionScreen(PackRepository packrepository, Consumer<PackRepository> consumer, Path path, Component component) {
      super(component);
      this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, packrepository, consumer);
      this.packDir = path;
      this.watcher = PackSelectionScreen.Watcher.create(path);
   }

   public void onClose() {
      this.model.commit();
      this.closeWatcher();
   }

   private void closeWatcher() {
      if (this.watcher != null) {
         try {
            this.watcher.close();
            this.watcher = null;
         } catch (Exception var2) {
         }
      }

   }

   protected void init() {
      this.availablePackList = new TransferableSelectionList(this.minecraft, this, 200, this.height, Component.translatable("pack.available.title"));
      this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
      this.addWidget(this.availablePackList);
      this.selectedPackList = new TransferableSelectionList(this.minecraft, this, 200, this.height, Component.translatable("pack.selected.title"));
      this.selectedPackList.setLeftPos(this.width / 2 + 4);
      this.addWidget(this.selectedPackList);
      this.addRenderableWidget(Button.builder(Component.translatable("pack.openFolder"), (button1) -> Util.getPlatform().openUri(this.packDir.toUri())).bounds(this.width / 2 - 154, this.height - 48, 150, 20).tooltip(Tooltip.create(DIRECTORY_BUTTON_TOOLTIP)).build());
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).bounds(this.width / 2 + 4, this.height - 48, 150, 20).build());
      this.reload();
   }

   public void tick() {
      if (this.watcher != null) {
         try {
            if (this.watcher.pollForChanges()) {
               this.ticksToReload = 20L;
            }
         } catch (IOException var2) {
            LOGGER.warn("Failed to poll for directory {} changes, stopping", (Object)this.packDir);
            this.closeWatcher();
         }
      }

      if (this.ticksToReload > 0L && --this.ticksToReload == 0L) {
         this.reload();
      }

   }

   private void populateLists() {
      this.updateList(this.selectedPackList, this.model.getSelected());
      this.updateList(this.availablePackList, this.model.getUnselected());
      this.doneButton.active = !this.selectedPackList.children().isEmpty();
   }

   private void updateList(TransferableSelectionList transferableselectionlist, Stream<PackSelectionModel.Entry> stream) {
      transferableselectionlist.children().clear();
      TransferableSelectionList.PackEntry transferableselectionlist_packentry = transferableselectionlist.getSelected();
      String s = transferableselectionlist_packentry == null ? "" : transferableselectionlist_packentry.getPackId();
      transferableselectionlist.setSelected((TransferableSelectionList.PackEntry)null);
      stream.forEach((packselectionmodel_entry) -> {
         TransferableSelectionList.PackEntry transferableselectionlist_packentry1 = new TransferableSelectionList.PackEntry(this.minecraft, transferableselectionlist, packselectionmodel_entry);
         transferableselectionlist.children().add(transferableselectionlist_packentry1);
         if (packselectionmodel_entry.getId().equals(s)) {
            transferableselectionlist.setSelected(transferableselectionlist_packentry1);
         }

      });
   }

   public void updateFocus(TransferableSelectionList transferableselectionlist) {
      TransferableSelectionList transferableselectionlist1 = this.selectedPackList == transferableselectionlist ? this.availablePackList : this.selectedPackList;
      this.changeFocus(ComponentPath.path(transferableselectionlist1.getFirstElement(), transferableselectionlist1, this));
   }

   public void clearSelected() {
      this.selectedPackList.setSelected((TransferableSelectionList.PackEntry)null);
      this.availablePackList.setSelected((TransferableSelectionList.PackEntry)null);
   }

   private void reload() {
      this.model.findNewPacks();
      this.populateLists();
      this.ticksToReload = 0L;
      this.packIcons.clear();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderDirtBackground(guigraphics);
      this.availablePackList.render(guigraphics, i, j, f);
      this.selectedPackList.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      guigraphics.drawCenteredString(this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
      super.render(guigraphics, i, j, f);
   }

   protected static void copyPacks(Minecraft minecraft, List<Path> list, Path path) {
      MutableBoolean mutableboolean = new MutableBoolean();
      list.forEach((path2) -> {
         try {
            Stream<Path> stream = Files.walk(path2);

            try {
               stream.forEach((path5) -> {
                  try {
                     Util.copyBetweenDirs(path2.getParent(), path, path5);
                  } catch (IOException var5) {
                     LOGGER.warn("Failed to copy datapack file  from {} to {}", path5, path, var5);
                     mutableboolean.setTrue();
                  }

               });
            } catch (Throwable var7) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException var8) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", path2, path);
            mutableboolean.setTrue();
         }

      });
      if (mutableboolean.isTrue()) {
         SystemToast.onPackCopyFailure(minecraft, path.toString());
      }

   }

   public void onFilesDrop(List<Path> list) {
      String s = list.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
      this.minecraft.setScreen(new ConfirmScreen((flag) -> {
         if (flag) {
            copyPacks(this.minecraft, list, this.packDir);
            this.reload();
         }

         this.minecraft.setScreen(this);
      }, Component.translatable("pack.dropConfirm"), Component.literal(s)));
   }

   private ResourceLocation loadPackIcon(TextureManager texturemanager, Pack pack) {
      try {
         PackResources packresources = pack.open();

         ResourceLocation var15;
         label69: {
            ResourceLocation var9;
            try {
               IoSupplier<InputStream> iosupplier = packresources.getRootResource("pack.png");
               if (iosupplier == null) {
                  var15 = DEFAULT_ICON;
                  break label69;
               }

               String s = pack.getId();
               ResourceLocation resourcelocation = new ResourceLocation("minecraft", "pack/" + Util.sanitizeName(s, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(s) + "/icon");
               InputStream inputstream = iosupplier.get();

               try {
                  NativeImage nativeimage = NativeImage.read(inputstream);
                  texturemanager.register(resourcelocation, new DynamicTexture(nativeimage));
                  var9 = resourcelocation;
               } catch (Throwable var12) {
                  if (inputstream != null) {
                     try {
                        inputstream.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (inputstream != null) {
                  inputstream.close();
               }
            } catch (Throwable var13) {
               if (packresources != null) {
                  try {
                     packresources.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (packresources != null) {
               packresources.close();
            }

            return var9;
         }

         if (packresources != null) {
            packresources.close();
         }

         return var15;
      } catch (Exception var14) {
         LOGGER.warn("Failed to load icon from pack {}", pack.getId(), var14);
         return DEFAULT_ICON;
      }
   }

   private ResourceLocation getPackIcon(Pack pack) {
      return this.packIcons.computeIfAbsent(pack.getId(), (s) -> this.loadPackIcon(this.minecraft.getTextureManager(), pack));
   }

   static class Watcher implements AutoCloseable {
      private final WatchService watcher;
      private final Path packPath;

      public Watcher(Path path) throws IOException {
         this.packPath = path;
         this.watcher = path.getFileSystem().newWatchService();

         try {
            this.watchDir(path);
            DirectoryStream<Path> directorystream = Files.newDirectoryStream(path);

            try {
               for(Path path1 : directorystream) {
                  if (Files.isDirectory(path1, LinkOption.NOFOLLOW_LINKS)) {
                     this.watchDir(path1);
                  }
               }
            } catch (Throwable var6) {
               if (directorystream != null) {
                  try {
                     directorystream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (directorystream != null) {
               directorystream.close();
            }

         } catch (Exception var7) {
            this.watcher.close();
            throw var7;
         }
      }

      @Nullable
      public static PackSelectionScreen.Watcher create(Path path) {
         try {
            return new PackSelectionScreen.Watcher(path);
         } catch (IOException var2) {
            PackSelectionScreen.LOGGER.warn("Failed to initialize pack directory {} monitoring", path, var2);
            return null;
         }
      }

      private void watchDir(Path path) throws IOException {
         path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
      }

      public boolean pollForChanges() throws IOException {
         boolean flag = false;

         WatchKey watchkey;
         while((watchkey = this.watcher.poll()) != null) {
            for(WatchEvent<?> watchevent : watchkey.pollEvents()) {
               flag = true;
               if (watchkey.watchable() == this.packPath && watchevent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                  Path path = this.packPath.resolve((Path)watchevent.context());
                  if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                     this.watchDir(path);
                  }
               }
            }

            watchkey.reset();
         }

         return flag;
      }

      public void close() throws IOException {
         this.watcher.close();
      }
   }
}
