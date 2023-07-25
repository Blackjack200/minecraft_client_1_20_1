package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldStem;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class OptimizeWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), (object2intopencustomhashmap) -> {
      object2intopencustomhashmap.put(Level.OVERWORLD, -13408734);
      object2intopencustomhashmap.put(Level.NETHER, -10075085);
      object2intopencustomhashmap.put(Level.END, -8943531);
      object2intopencustomhashmap.defaultReturnValue(-2236963);
   });
   private final BooleanConsumer callback;
   private final WorldUpgrader upgrader;

   @Nullable
   public static OptimizeWorldScreen create(Minecraft minecraft, BooleanConsumer booleanconsumer, DataFixer datafixer, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, boolean flag) {
      try {
         WorldStem worldstem = minecraft.createWorldOpenFlows().loadWorldStem(levelstoragesource_levelstorageaccess, false);

         OptimizeWorldScreen var8;
         try {
            WorldData worlddata = worldstem.worldData();
            RegistryAccess.Frozen registryaccess_frozen = worldstem.registries().compositeAccess();
            levelstoragesource_levelstorageaccess.saveDataTag(registryaccess_frozen, worlddata);
            var8 = new OptimizeWorldScreen(booleanconsumer, datafixer, levelstoragesource_levelstorageaccess, worlddata.getLevelSettings(), flag, registryaccess_frozen.registryOrThrow(Registries.LEVEL_STEM));
         } catch (Throwable var10) {
            if (worldstem != null) {
               try {
                  worldstem.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (worldstem != null) {
            worldstem.close();
         }

         return var8;
      } catch (Exception var11) {
         LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)var11);
         return null;
      }
   }

   private OptimizeWorldScreen(BooleanConsumer booleanconsumer, DataFixer datafixer, LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, LevelSettings levelsettings, boolean flag, Registry<LevelStem> registry) {
      super(Component.translatable("optimizeWorld.title", levelsettings.levelName()));
      this.callback = booleanconsumer;
      this.upgrader = new WorldUpgrader(levelstoragesource_levelstorageaccess, datafixer, registry, flag);
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
         this.upgrader.cancel();
         this.callback.accept(false);
      }).bounds(this.width / 2 - 100, this.height / 4 + 150, 200, 20).build());
   }

   public void tick() {
      if (this.upgrader.isFinished()) {
         this.callback.accept(true);
      }

   }

   public void onClose() {
      this.callback.accept(false);
   }

   public void removed() {
      this.upgrader.cancel();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      int k = this.width / 2 - 150;
      int l = this.width / 2 + 150;
      int i1 = this.height / 4 + 100;
      int j1 = i1 + 10;
      guigraphics.drawCenteredString(this.font, this.upgrader.getStatus(), this.width / 2, i1 - 9 - 2, 10526880);
      if (this.upgrader.getTotalChunks() > 0) {
         guigraphics.fill(k - 1, i1 - 1, l + 1, j1 + 1, -16777216);
         guigraphics.drawString(this.font, Component.translatable("optimizeWorld.info.converted", this.upgrader.getConverted()), k, 40, 10526880);
         guigraphics.drawString(this.font, Component.translatable("optimizeWorld.info.skipped", this.upgrader.getSkipped()), k, 40 + 9 + 3, 10526880);
         guigraphics.drawString(this.font, Component.translatable("optimizeWorld.info.total", this.upgrader.getTotalChunks()), k, 40 + (9 + 3) * 2, 10526880);
         int k1 = 0;

         for(ResourceKey<Level> resourcekey : this.upgrader.levels()) {
            int l1 = Mth.floor(this.upgrader.dimensionProgress(resourcekey) * (float)(l - k));
            guigraphics.fill(k + k1, i1, k + k1 + l1, j1, DIMENSION_COLORS.getInt(resourcekey));
            k1 += l1;
         }

         int i2 = this.upgrader.getConverted() + this.upgrader.getSkipped();
         guigraphics.drawCenteredString(this.font, i2 + " / " + this.upgrader.getTotalChunks(), this.width / 2, i1 + 2 * 9 + 2, 10526880);
         guigraphics.drawCenteredString(this.font, Mth.floor(this.upgrader.getProgress() * 100.0F) + "%", this.width / 2, i1 + (j1 - i1) / 2 - 9 / 2, 10526880);
      }

      super.render(guigraphics, i, j, f);
   }
}
