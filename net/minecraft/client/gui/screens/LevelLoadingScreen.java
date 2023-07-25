package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkStatus;

public class LevelLoadingScreen extends Screen {
   private static final long NARRATION_DELAY_MS = 2000L;
   private final StoringChunkProgressListener progressListener;
   private long lastNarration = -1L;
   private boolean done;
   private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), (object2intopenhashmap) -> {
      object2intopenhashmap.defaultReturnValue(0);
      object2intopenhashmap.put(ChunkStatus.EMPTY, 5526612);
      object2intopenhashmap.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
      object2intopenhashmap.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
      object2intopenhashmap.put(ChunkStatus.BIOMES, 8434258);
      object2intopenhashmap.put(ChunkStatus.NOISE, 13750737);
      object2intopenhashmap.put(ChunkStatus.SURFACE, 7497737);
      object2intopenhashmap.put(ChunkStatus.CARVERS, 3159410);
      object2intopenhashmap.put(ChunkStatus.FEATURES, 2213376);
      object2intopenhashmap.put(ChunkStatus.INITIALIZE_LIGHT, 13421772);
      object2intopenhashmap.put(ChunkStatus.LIGHT, 16769184);
      object2intopenhashmap.put(ChunkStatus.SPAWN, 15884384);
      object2intopenhashmap.put(ChunkStatus.FULL, 16777215);
   });

   public LevelLoadingScreen(StoringChunkProgressListener storingchunkprogresslistener) {
      super(GameNarrator.NO_TITLE);
      this.progressListener = storingchunkprogresslistener;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected boolean shouldNarrateNavigation() {
      return false;
   }

   public void removed() {
      this.done = true;
      this.triggerImmediateNarration(true);
   }

   protected void updateNarratedWidget(NarrationElementOutput narrationelementoutput) {
      if (this.done) {
         narrationelementoutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narrator.loading.done"));
      } else {
         String s = this.getFormattedProgress();
         narrationelementoutput.add(NarratedElementType.TITLE, s);
      }

   }

   private String getFormattedProgress() {
      return Mth.clamp(this.progressListener.getProgress(), 0, 100) + "%";
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      long k = Util.getMillis();
      if (k - this.lastNarration > 2000L) {
         this.lastNarration = k;
         this.triggerImmediateNarration(true);
      }

      int l = this.width / 2;
      int i1 = this.height / 2;
      int j1 = 30;
      renderChunks(guigraphics, this.progressListener, l, i1 + 30, 2, 0);
      guigraphics.drawCenteredString(this.font, this.getFormattedProgress(), l, i1 - 9 / 2 - 30, 16777215);
   }

   public static void renderChunks(GuiGraphics guigraphics, StoringChunkProgressListener storingchunkprogresslistener, int i, int j, int k, int l) {
      int i1 = k + l;
      int j1 = storingchunkprogresslistener.getFullDiameter();
      int k1 = j1 * i1 - l;
      int l1 = storingchunkprogresslistener.getDiameter();
      int i2 = l1 * i1 - l;
      int j2 = i - i2 / 2;
      int k2 = j - i2 / 2;
      int l2 = k1 / 2 + 1;
      int i3 = -16772609;
      guigraphics.drawManaged(() -> {
         if (l != 0) {
            guigraphics.fill(i - l2, j - l2, i - l2 + 1, j + l2, -16772609);
            guigraphics.fill(i + l2 - 1, j - l2, i + l2, j + l2, -16772609);
            guigraphics.fill(i - l2, j - l2, i + l2, j - l2 + 1, -16772609);
            guigraphics.fill(i - l2, j + l2 - 1, i + l2, j + l2, -16772609);
         }

         for(int k5 = 0; k5 < l1; ++k5) {
            for(int l5 = 0; l5 < l1; ++l5) {
               ChunkStatus chunkstatus = storingchunkprogresslistener.getStatus(k5, l5);
               int i6 = j2 + k5 * i1;
               int j6 = k2 + l5 * i1;
               guigraphics.fill(i6, j6, i6 + k, j6 + k, COLORS.getInt(chunkstatus) | -16777216);
            }
         }

      });
   }
}
