package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SymlinkWarningScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.Entry> {
   static final Logger LOGGER = LogUtils.getLogger();
   static final DateFormat DATE_FORMAT = new SimpleDateFormat();
   private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
   static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
   static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
   static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
   static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
   static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
   static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
   static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
   private final SelectWorldScreen screen;
   private CompletableFuture<List<LevelSummary>> pendingLevels;
   @Nullable
   private List<LevelSummary> currentlyDisplayedLevels;
   private String filter;
   private final WorldSelectionList.LoadingHeader loadingHeader;

   public WorldSelectionList(SelectWorldScreen selectworldscreen, Minecraft minecraft, int i, int j, int k, int l, int i1, String s, @Nullable WorldSelectionList worldselectionlist) {
      super(minecraft, i, j, k, l, i1);
      this.screen = selectworldscreen;
      this.loadingHeader = new WorldSelectionList.LoadingHeader(minecraft);
      this.filter = s;
      if (worldselectionlist != null) {
         this.pendingLevels = worldselectionlist.pendingLevels;
      } else {
         this.pendingLevels = this.loadLevels();
      }

      this.handleNewLevels(this.pollLevelsIgnoreErrors());
   }

   protected void clearEntries() {
      this.children().forEach(WorldSelectionList.Entry::close);
      super.clearEntries();
   }

   @Nullable
   private List<LevelSummary> pollLevelsIgnoreErrors() {
      try {
         return this.pendingLevels.getNow((List<LevelSummary>)null);
      } catch (CancellationException | CompletionException var2) {
         return null;
      }
   }

   void reloadWorldList() {
      this.pendingLevels = this.loadLevels();
   }

   public boolean keyPressed(int i, int j, int k) {
      if (CommonInputs.selected(i)) {
         Optional<WorldSelectionList.WorldListEntry> optional = this.getSelectedOpt();
         if (optional.isPresent()) {
            optional.get().joinWorld();
            return true;
         }
      }

      return super.keyPressed(i, j, k);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      List<LevelSummary> list = this.pollLevelsIgnoreErrors();
      if (list != this.currentlyDisplayedLevels) {
         this.handleNewLevels(list);
      }

      super.render(guigraphics, i, j, f);
   }

   private void handleNewLevels(@Nullable List<LevelSummary> list) {
      if (list == null) {
         this.fillLoadingLevels();
      } else {
         this.fillLevels(this.filter, list);
      }

      this.currentlyDisplayedLevels = list;
   }

   public void updateFilter(String s) {
      if (this.currentlyDisplayedLevels != null && !s.equals(this.filter)) {
         this.fillLevels(s, this.currentlyDisplayedLevels);
      }

      this.filter = s;
   }

   private CompletableFuture<List<LevelSummary>> loadLevels() {
      LevelStorageSource.LevelCandidates levelstoragesource_levelcandidates;
      try {
         levelstoragesource_levelcandidates = this.minecraft.getLevelSource().findLevelCandidates();
      } catch (LevelStorageException var3) {
         LOGGER.error("Couldn't load level list", (Throwable)var3);
         this.handleLevelLoadFailure(var3.getMessageComponent());
         return CompletableFuture.completedFuture(List.of());
      }

      if (levelstoragesource_levelcandidates.isEmpty()) {
         CreateWorldScreen.openFresh(this.minecraft, (Screen)null);
         return CompletableFuture.completedFuture(List.of());
      } else {
         return this.minecraft.getLevelSource().loadLevelSummaries(levelstoragesource_levelcandidates).exceptionally((throwable) -> {
            this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
            return List.of();
         });
      }
   }

   private void fillLevels(String s, List<LevelSummary> list) {
      this.clearEntries();
      s = s.toLowerCase(Locale.ROOT);

      for(LevelSummary levelsummary : list) {
         if (this.filterAccepts(s, levelsummary)) {
            this.addEntry(new WorldSelectionList.WorldListEntry(this, levelsummary));
         }
      }

      this.notifyListUpdated();
   }

   private boolean filterAccepts(String s, LevelSummary levelsummary) {
      return levelsummary.getLevelName().toLowerCase(Locale.ROOT).contains(s) || levelsummary.getLevelId().toLowerCase(Locale.ROOT).contains(s);
   }

   private void fillLoadingLevels() {
      this.clearEntries();
      this.addEntry(this.loadingHeader);
      this.notifyListUpdated();
   }

   private void notifyListUpdated() {
      this.screen.triggerImmediateNarration(true);
   }

   private void handleLevelLoadFailure(Component component) {
      this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), component));
   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 20;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 50;
   }

   public void setSelected(@Nullable WorldSelectionList.Entry worldselectionlist_entry) {
      super.setSelected(worldselectionlist_entry);
      this.screen.updateButtonStatus(worldselectionlist_entry != null && worldselectionlist_entry.isSelectable(), worldselectionlist_entry != null);
   }

   public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
      WorldSelectionList.Entry worldselectionlist_entry = this.getSelected();
      if (worldselectionlist_entry instanceof WorldSelectionList.WorldListEntry worldselectionlist_worldlistentry) {
         return Optional.of(worldselectionlist_worldlistentry);
      } else {
         return Optional.empty();
      }
   }

   public SelectWorldScreen getScreen() {
      return this.screen;
   }

   public void updateNarration(NarrationElementOutput narrationelementoutput) {
      if (this.children().contains(this.loadingHeader)) {
         this.loadingHeader.updateNarration(narrationelementoutput);
      } else {
         super.updateNarration(narrationelementoutput);
      }
   }

   public abstract static class Entry extends ObjectSelectionList.Entry<WorldSelectionList.Entry> implements AutoCloseable {
      public abstract boolean isSelectable();

      public void close() {
      }
   }

   public static class LoadingHeader extends WorldSelectionList.Entry {
      private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
      private final Minecraft minecraft;

      public LoadingHeader(Minecraft minecraft) {
         this.minecraft = minecraft;
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         int l1 = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
         int i2 = j + (i1 - 9) / 2;
         guigraphics.drawString(this.minecraft.font, LOADING_LABEL, l1, i2, 16777215, false);
         String s = LoadingDotsText.get(Util.getMillis());
         int j2 = (this.minecraft.screen.width - this.minecraft.font.width(s)) / 2;
         int k2 = i2 + 9;
         guigraphics.drawString(this.minecraft.font, s, j2, k2, 8421504, false);
      }

      public Component getNarration() {
         return LOADING_LABEL;
      }

      public boolean isSelectable() {
         return false;
      }
   }

   public final class WorldListEntry extends WorldSelectionList.Entry implements AutoCloseable {
      private static final int ICON_WIDTH = 32;
      private static final int ICON_HEIGHT = 32;
      private static final int ICON_OVERLAY_X_JOIN = 0;
      private static final int ICON_OVERLAY_X_JOIN_WITH_NOTIFY = 32;
      private static final int ICON_OVERLAY_X_WARNING = 64;
      private static final int ICON_OVERLAY_X_ERROR = 96;
      private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
      private static final int ICON_OVERLAY_Y_SELECTED = 32;
      private final Minecraft minecraft;
      private final SelectWorldScreen screen;
      private final LevelSummary summary;
      private final FaviconTexture icon;
      @Nullable
      private Path iconFile;
      private long lastClickTime;

      public WorldListEntry(WorldSelectionList worldselectionlist1, LevelSummary levelsummary) {
         this.minecraft = worldselectionlist1.minecraft;
         this.screen = worldselectionlist1.getScreen();
         this.summary = levelsummary;
         this.icon = FaviconTexture.forWorld(this.minecraft.getTextureManager(), levelsummary.getLevelId());
         this.iconFile = levelsummary.getIcon();
         this.validateIconFile();
         this.loadIcon();
      }

      private void validateIconFile() {
         if (this.iconFile != null) {
            try {
               BasicFileAttributes basicfileattributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
               if (basicfileattributes.isSymbolicLink()) {
                  List<ForbiddenSymlinkInfo> list = new ArrayList<>();
                  this.minecraft.getLevelSource().getWorldDirValidator().validateSymlink(this.iconFile, list);
                  if (!list.isEmpty()) {
                     WorldSelectionList.LOGGER.warn(ContentValidationException.getMessage(this.iconFile, list));
                     this.iconFile = null;
                  } else {
                     basicfileattributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class);
                  }
               }

               if (!basicfileattributes.isRegularFile()) {
                  this.iconFile = null;
               }
            } catch (NoSuchFileException var3) {
               this.iconFile = null;
            } catch (IOException var4) {
               WorldSelectionList.LOGGER.error("could not validate symlink", (Throwable)var4);
               this.iconFile = null;
            }

         }
      }

      public Component getNarration() {
         Component component = Component.translatable("narrator.select.world_info", this.summary.getLevelName(), new Date(this.summary.getLastPlayed()), this.summary.getInfo());
         Component component1;
         if (this.summary.isLocked()) {
            component1 = CommonComponents.joinForNarration(component, WorldSelectionList.WORLD_LOCKED_TOOLTIP);
         } else {
            component1 = component;
         }

         return Component.translatable("narrator.select", component1);
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         String s = this.summary.getLevelName();
         String s1 = this.summary.getLevelId();
         long l1 = this.summary.getLastPlayed();
         if (l1 != -1L) {
            s1 = s1 + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(l1)) + ")";
         }

         if (StringUtils.isEmpty(s)) {
            s = I18n.get("selectWorld.world") + " " + (i + 1);
         }

         Component component = this.summary.getInfo();
         guigraphics.drawString(this.minecraft.font, s, k + 32 + 3, j + 1, 16777215, false);
         guigraphics.drawString(this.minecraft.font, s1, k + 32 + 3, j + 9 + 3, 8421504, false);
         guigraphics.drawString(this.minecraft.font, component, k + 32 + 3, j + 9 + 9 + 3, 8421504, false);
         RenderSystem.enableBlend();
         guigraphics.blit(this.icon.textureLocation(), k, j, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
         if (this.minecraft.options.touchscreen().get() || flag) {
            guigraphics.fill(k, j, k + 32, j + 32, -1601138544);
            int i2 = j1 - k;
            boolean flag1 = i2 < 32;
            int j2 = flag1 ? 32 : 0;
            if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
               guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)j2, 32, 32, 256, 256);
               guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 32.0F, (float)j2, 32, 32, 256, 256);
               return;
            }

            if (this.summary.isLocked()) {
               guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)j2, 32, 32, 256, 256);
               if (flag1) {
                  this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
               }
            } else if (this.summary.requiresManualConversion()) {
               guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)j2, 32, 32, 256, 256);
               if (flag1) {
                  this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
               }
            } else if (this.summary.markVersionInList()) {
               guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 32.0F, (float)j2, 32, 32, 256, 256);
               if (this.summary.askToOpenWorld()) {
                  guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)j2, 32, 32, 256, 256);
                  if (flag1) {
                     this.screen.setTooltipForNextRenderPass(ImmutableList.of(WorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText()));
                  }
               } else if (!SharedConstants.getCurrentVersion().isStable()) {
                  guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, (float)j2, 32, 32, 256, 256);
                  if (flag1) {
                     this.screen.setTooltipForNextRenderPass(ImmutableList.of(WorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText()));
                  }
               }
            } else {
               guigraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, (float)j2, 32, 32, 256, 256);
            }
         }

      }

      public boolean mouseClicked(double d0, double d1, int i) {
         if (this.summary.isDisabled()) {
            return true;
         } else {
            WorldSelectionList.this.setSelected((WorldSelectionList.Entry)this);
            if (d0 - (double)WorldSelectionList.this.getRowLeft() <= 32.0D) {
               this.joinWorld();
               return true;
            } else if (Util.getMillis() - this.lastClickTime < 250L) {
               this.joinWorld();
               return true;
            } else {
               this.lastClickTime = Util.getMillis();
               return true;
            }
         }
      }

      public void joinWorld() {
         if (!this.summary.isDisabled()) {
            if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
               this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
            } else {
               LevelSummary.BackupStatus levelsummary_backupstatus = this.summary.backupStatus();
               if (levelsummary_backupstatus.shouldBackup()) {
                  String s = "selectWorld.backupQuestion." + levelsummary_backupstatus.getTranslationKey();
                  String s1 = "selectWorld.backupWarning." + levelsummary_backupstatus.getTranslationKey();
                  MutableComponent mutablecomponent = Component.translatable(s);
                  if (levelsummary_backupstatus.isSevere()) {
                     mutablecomponent.withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
                  }

                  Component component = Component.translatable(s1, this.summary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
                  this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (flag1, flag2) -> {
                     if (flag1) {
                        String s2 = this.summary.getLevelId();

                        try {
                           LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.minecraft.getLevelSource().validateAndCreateAccess(s2);

                           try {
                              EditWorldScreen.makeBackupAndShowToast(levelstoragesource_levelstorageaccess);
                           } catch (Throwable var8) {
                              if (levelstoragesource_levelstorageaccess != null) {
                                 try {
                                    levelstoragesource_levelstorageaccess.close();
                                 } catch (Throwable var7) {
                                    var8.addSuppressed(var7);
                                 }
                              }

                              throw var8;
                           }

                           if (levelstoragesource_levelstorageaccess != null) {
                              levelstoragesource_levelstorageaccess.close();
                           }
                        } catch (IOException var9) {
                           SystemToast.onWorldAccessFailure(this.minecraft, s2);
                           WorldSelectionList.LOGGER.error("Failed to backup level {}", s2, var9);
                        } catch (ContentValidationException var10) {
                           WorldSelectionList.LOGGER.warn("{}", (Object)var10.getMessage());
                           this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
                        }
                     }

                     this.loadWorld();
                  }, mutablecomponent, component, false));
               } else if (this.summary.askToOpenWorld()) {
                  this.minecraft.setScreen(new ConfirmScreen((flag) -> {
                     if (flag) {
                        try {
                           this.loadWorld();
                        } catch (Exception var3) {
                           WorldSelectionList.LOGGER.error("Failure to open 'future world'", (Throwable)var3);
                           this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), Component.translatable("selectWorld.futureworld.error.title"), Component.translatable("selectWorld.futureworld.error.text")));
                        }
                     } else {
                        this.minecraft.setScreen(this.screen);
                     }

                  }, Component.translatable("selectWorld.versionQuestion"), Component.translatable("selectWorld.versionWarning", this.summary.getWorldVersionName()), Component.translatable("selectWorld.versionJoinButton"), CommonComponents.GUI_CANCEL));
               } else {
                  this.loadWorld();
               }

            }
         }
      }

      public void deleteWorld() {
         this.minecraft.setScreen(new ConfirmScreen((flag) -> {
            if (flag) {
               this.minecraft.setScreen(new ProgressScreen(true));
               this.doDeleteWorld();
            }

            this.minecraft.setScreen(this.screen);
         }, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
      }

      public void doDeleteWorld() {
         LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
         String s = this.summary.getLevelId();

         try {
            LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = levelstoragesource.createAccess(s);

            try {
               levelstoragesource_levelstorageaccess.deleteLevel();
            } catch (Throwable var7) {
               if (levelstoragesource_levelstorageaccess != null) {
                  try {
                     levelstoragesource_levelstorageaccess.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (levelstoragesource_levelstorageaccess != null) {
               levelstoragesource_levelstorageaccess.close();
            }
         } catch (IOException var8) {
            SystemToast.onWorldDeleteFailure(this.minecraft, s);
            WorldSelectionList.LOGGER.error("Failed to delete world {}", s, var8);
         }

         WorldSelectionList.this.reloadWorldList();
      }

      public void editWorld() {
         if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
            this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
         } else {
            this.queueLoadScreen();
            String s = this.summary.getLevelId();

            try {
               LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.minecraft.getLevelSource().validateAndCreateAccess(s);
               this.minecraft.setScreen(new EditWorldScreen((flag) -> {
                  try {
                     levelstoragesource_levelstorageaccess.close();
                  } catch (IOException var5) {
                     WorldSelectionList.LOGGER.error("Failed to unlock level {}", s, var5);
                  }

                  if (flag) {
                     WorldSelectionList.this.reloadWorldList();
                  }

                  this.minecraft.setScreen(this.screen);
               }, levelstoragesource_levelstorageaccess));
            } catch (IOException var3) {
               SystemToast.onWorldAccessFailure(this.minecraft, s);
               WorldSelectionList.LOGGER.error("Failed to access level {}", s, var3);
               WorldSelectionList.this.reloadWorldList();
            } catch (ContentValidationException var4) {
               WorldSelectionList.LOGGER.warn("{}", (Object)var4.getMessage());
               this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
            }

         }
      }

      public void recreateWorld() {
         if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
            this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
         } else {
            this.queueLoadScreen();

            try {
               LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = this.minecraft.getLevelSource().validateAndCreateAccess(this.summary.getLevelId());

               try {
                  Pair<LevelSettings, WorldCreationContext> pair = this.minecraft.createWorldOpenFlows().recreateWorldData(levelstoragesource_levelstorageaccess);
                  LevelSettings levelsettings = pair.getFirst();
                  WorldCreationContext worldcreationcontext = pair.getSecond();
                  Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelstoragesource_levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                  if (worldcreationcontext.options().isOldCustomizedWorld()) {
                     this.minecraft.setScreen(new ConfirmScreen((flag) -> this.minecraft.setScreen((Screen)(flag ? CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelsettings, worldcreationcontext, path) : this.screen)), Component.translatable("selectWorld.recreate.customized.title"), Component.translatable("selectWorld.recreate.customized.text"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
                  } else {
                     this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelsettings, worldcreationcontext, path));
                  }
               } catch (Throwable var7) {
                  if (levelstoragesource_levelstorageaccess != null) {
                     try {
                        levelstoragesource_levelstorageaccess.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (levelstoragesource_levelstorageaccess != null) {
                  levelstoragesource_levelstorageaccess.close();
               }
            } catch (ContentValidationException var8) {
               WorldSelectionList.LOGGER.warn("{}", (Object)var8.getMessage());
               this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
            } catch (Exception var9) {
               WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var9);
               this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), Component.translatable("selectWorld.recreate.error.title"), Component.translatable("selectWorld.recreate.error.text")));
            }

         }
      }

      private void loadWorld() {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
            this.queueLoadScreen();
            this.minecraft.createWorldOpenFlows().loadLevel(this.screen, this.summary.getLevelId());
         }

      }

      private void queueLoadScreen() {
         this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
      }

      private void loadIcon() {
         boolean flag = this.iconFile != null && Files.isRegularFile(this.iconFile);
         if (flag) {
            try {
               InputStream inputstream = Files.newInputStream(this.iconFile);

               try {
                  this.icon.upload(NativeImage.read(inputstream));
               } catch (Throwable var6) {
                  if (inputstream != null) {
                     try {
                        inputstream.close();
                     } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                     }
                  }

                  throw var6;
               }

               if (inputstream != null) {
                  inputstream.close();
               }
            } catch (Throwable var7) {
               WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var7);
               this.iconFile = null;
            }
         } else {
            this.icon.clear();
         }

      }

      public void close() {
         this.icon.close();
      }

      public String getLevelName() {
         return this.summary.getLevelName();
      }

      public boolean isSelectable() {
         return !this.summary.isDisabled();
      }
   }
}
