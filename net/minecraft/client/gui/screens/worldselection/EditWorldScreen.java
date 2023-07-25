package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class EditWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
   private Button renameButton;
   private final BooleanConsumer callback;
   private EditBox nameEdit;
   private final LevelStorageSource.LevelStorageAccess levelAccess;

   public EditWorldScreen(BooleanConsumer booleanconsumer, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess) {
      super(Component.translatable("selectWorld.edit.title"));
      this.callback = booleanconsumer;
      this.levelAccess = levelstoragesource_levelstorageaccess;
   }

   public void tick() {
      this.nameEdit.tick();
   }

   protected void init() {
      this.renameButton = Button.builder(Component.translatable("selectWorld.edit.save"), (button7) -> this.onRename()).bounds(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20).build();
      this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, Component.translatable("selectWorld.enterName"));
      LevelSummary levelsummary = this.levelAccess.getSummary();
      String s = levelsummary == null ? "" : levelsummary.getLevelName();
      this.nameEdit.setValue(s);
      this.nameEdit.setResponder((s1) -> this.renameButton.active = !s1.trim().isEmpty());
      this.addWidget(this.nameEdit);
      Button button = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.resetIcon"), (button6) -> {
         this.levelAccess.getIconFile().ifPresent((path2) -> FileUtils.deleteQuietly(path2.toFile()));
         button6.active = false;
      }).bounds(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.openFolder"), (button5) -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())).bounds(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backup"), (button4) -> {
         boolean flag2 = makeBackupAndShowToast(this.levelAccess);
         this.callback.accept(!flag2);
      }).bounds(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backupFolder"), (button3) -> {
         LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
         Path path1 = levelstoragesource.getBackupPath();

         try {
            FileUtil.createDirectoriesSafe(path1);
         } catch (IOException var5) {
            throw new RuntimeException(var5);
         }

         Util.getPlatform().openFile(path1.toFile());
      }).bounds(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.optimize"), (button2) -> this.minecraft.setScreen(new BackupConfirmScreen(this, (flag, flag1) -> {
            if (flag) {
               makeBackupAndShowToast(this.levelAccess);
            }

            this.minecraft.setScreen(OptimizeWorldScreen.create(this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, flag1));
         }, Component.translatable("optimizeWorld.confirm.title"), Component.translatable("optimizeWorld.confirm.description"), true))).bounds(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20).build());
      this.addRenderableWidget(this.renameButton);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button1) -> this.callback.accept(false)).bounds(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20).build());
      button.active = this.levelAccess.getIconFile().filter((path) -> Files.isRegularFile(path)).isPresent();
      this.setInitialFocus(this.nameEdit);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.nameEdit.getValue();
      this.init(minecraft, i, j);
      this.nameEdit.setValue(s);
   }

   public void onClose() {
      this.callback.accept(false);
   }

   private void onRename() {
      try {
         this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
         this.callback.accept(true);
      } catch (IOException var2) {
         LOGGER.error("Failed to access world '{}'", this.levelAccess.getLevelId(), var2);
         SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
         this.callback.accept(true);
      }

   }

   public static void makeBackupAndShowToast(LevelStorageSource levelstoragesource, String s) {
      boolean flag = false;

      try {
         LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess = levelstoragesource.validateAndCreateAccess(s);

         try {
            flag = true;
            makeBackupAndShowToast(levelstoragesource_levelstorageaccess);
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
         if (!flag) {
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), s);
         }

         LOGGER.warn("Failed to create backup of level {}", s, var8);
      } catch (ContentValidationException var9) {
         LOGGER.warn("{}", (Object)var9.getMessage());
         SystemToast.onWorldAccessFailure(Minecraft.getInstance(), s);
      }

   }

   public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess) {
      long i = 0L;
      IOException ioexception = null;

      try {
         i = levelstoragesource_levelstorageaccess.makeWorldBackup();
      } catch (IOException var6) {
         ioexception = var6;
      }

      if (ioexception != null) {
         Component component = Component.translatable("selectWorld.edit.backupFailed");
         Component component1 = Component.literal(ioexception.getMessage());
         Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component, component1));
         return false;
      } else {
         Component component2 = Component.translatable("selectWorld.edit.backupCreated", levelstoragesource_levelstorageaccess.getLevelId());
         Component component3 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)i / 1048576.0D));
         Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, component2, component3));
         return true;
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
      guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 100, 24, 10526880);
      this.nameEdit.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
   }
}
