package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class WinScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
   private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
   private static final String NAME_PREFIX = "           ";
   private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
   private static final float SPEEDUP_FACTOR = 5.0F;
   private static final float SPEEDUP_FACTOR_FAST = 15.0F;
   private final boolean poem;
   private final Runnable onFinished;
   private float scroll;
   private List<FormattedCharSequence> lines;
   private IntSet centeredLines;
   private int totalScrollLength;
   private boolean speedupActive;
   private final IntSet speedupModifiers = new IntOpenHashSet();
   private float scrollSpeed;
   private final float unmodifiedScrollSpeed;
   private int direction;
   private final LogoRenderer logoRenderer = new LogoRenderer(false);

   public WinScreen(boolean flag, Runnable runnable) {
      super(GameNarrator.NO_TITLE);
      this.poem = flag;
      this.onFinished = runnable;
      if (!flag) {
         this.unmodifiedScrollSpeed = 0.75F;
      } else {
         this.unmodifiedScrollSpeed = 0.5F;
      }

      this.direction = 1;
      this.scrollSpeed = this.unmodifiedScrollSpeed;
   }

   private float calculateScrollSpeed() {
      return this.speedupActive ? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) * (float)this.direction : this.unmodifiedScrollSpeed * (float)this.direction;
   }

   public void tick() {
      this.minecraft.getMusicManager().tick();
      this.minecraft.getSoundManager().tick(false);
      float f = (float)(this.totalScrollLength + this.height + this.height + 24);
      if (this.scroll > f) {
         this.respawn();
      }

   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 265) {
         this.direction = -1;
      } else if (i != 341 && i != 345) {
         if (i == 32) {
            this.speedupActive = true;
         }
      } else {
         this.speedupModifiers.add(i);
      }

      this.scrollSpeed = this.calculateScrollSpeed();
      return super.keyPressed(i, j, k);
   }

   public boolean keyReleased(int i, int j, int k) {
      if (i == 265) {
         this.direction = 1;
      }

      if (i == 32) {
         this.speedupActive = false;
      } else if (i == 341 || i == 345) {
         this.speedupModifiers.remove(i);
      }

      this.scrollSpeed = this.calculateScrollSpeed();
      return super.keyReleased(i, j, k);
   }

   public void onClose() {
      this.respawn();
   }

   private void respawn() {
      this.onFinished.run();
   }

   protected void init() {
      if (this.lines == null) {
         this.lines = Lists.newArrayList();
         this.centeredLines = new IntOpenHashSet();
         if (this.poem) {
            this.wrapCreditsIO("texts/end.txt", this::addPoemFile);
         }

         this.wrapCreditsIO("texts/credits.json", this::addCreditsFile);
         if (this.poem) {
            this.wrapCreditsIO("texts/postcredits.txt", this::addPoemFile);
         }

         this.totalScrollLength = this.lines.size() * 12;
      }
   }

   private void wrapCreditsIO(String s, WinScreen.CreditsReader winscreen_creditsreader) {
      try {
         Reader reader = this.minecraft.getResourceManager().openAsReader(new ResourceLocation(s));

         try {
            winscreen_creditsreader.read(reader);
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (Exception var8) {
         LOGGER.error("Couldn't load credits", (Throwable)var8);
      }

   }

   private void addPoemFile(Reader reader) throws IOException {
      BufferedReader bufferedreader = new BufferedReader(reader);
      RandomSource randomsource = RandomSource.create(8124371L);

      String s;
      while((s = bufferedreader.readLine()) != null) {
         int i;
         String s1;
         String s2;
         for(s = s.replaceAll("PLAYERNAME", this.minecraft.getUser().getName()); (i = s.indexOf(OBFUSCATE_TOKEN)) != -1; s = s1 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, randomsource.nextInt(4) + 3) + s2) {
            s1 = s.substring(0, i);
            s2 = s.substring(i + OBFUSCATE_TOKEN.length());
         }

         this.addPoemLines(s);
         this.addEmptyLine();
      }

      for(int j = 0; j < 8; ++j) {
         this.addEmptyLine();
      }

   }

   private void addCreditsFile(Reader reader1) {
      for(JsonElement jsonelement : GsonHelper.parseArray(reader1)) {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         String s3 = jsonobject.get("section").getAsString();
         this.addCreditsLine(SECTION_HEADING, true);
         this.addCreditsLine(Component.literal(s3).withStyle(ChatFormatting.YELLOW), true);
         this.addCreditsLine(SECTION_HEADING, true);
         this.addEmptyLine();
         this.addEmptyLine();

         for(JsonElement jsonelement1 : jsonobject.getAsJsonArray("disciplines")) {
            JsonObject jsonobject1 = jsonelement1.getAsJsonObject();
            String s4 = jsonobject1.get("discipline").getAsString();
            if (StringUtils.isNotEmpty(s4)) {
               this.addCreditsLine(Component.literal(s4).withStyle(ChatFormatting.YELLOW), true);
               this.addEmptyLine();
               this.addEmptyLine();
            }

            for(JsonElement jsonelement2 : jsonobject1.getAsJsonArray("titles")) {
               JsonObject jsonobject2 = jsonelement2.getAsJsonObject();
               String s5 = jsonobject2.get("title").getAsString();
               JsonArray jsonarray3 = jsonobject2.getAsJsonArray("names");
               this.addCreditsLine(Component.literal(s5).withStyle(ChatFormatting.GRAY), false);

               for(JsonElement jsonelement3 : jsonarray3) {
                  String s6 = jsonelement3.getAsString();
                  this.addCreditsLine(Component.literal("           ").append(s6).withStyle(ChatFormatting.WHITE), false);
               }

               this.addEmptyLine();
               this.addEmptyLine();
            }
         }
      }

   }

   private void addEmptyLine() {
      this.lines.add(FormattedCharSequence.EMPTY);
   }

   private void addPoemLines(String s) {
      this.lines.addAll(this.minecraft.font.split(Component.literal(s), 256));
   }

   private void addCreditsLine(Component component, boolean flag) {
      if (flag) {
         this.centeredLines.add(this.lines.size());
      }

      this.lines.add(component.getVisualOrderText());
   }

   private void renderBg(GuiGraphics guigraphics) {
      int i = this.width;
      float f = this.scroll * 0.5F;
      int j = 64;
      float f1 = this.scroll / this.unmodifiedScrollSpeed;
      float f2 = f1 * 0.02F;
      float f3 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
      float f4 = (f3 - 20.0F - f1) * 0.005F;
      if (f4 < f2) {
         f2 = f4;
      }

      if (f2 > 1.0F) {
         f2 = 1.0F;
      }

      f2 *= f2;
      f2 = f2 * 96.0F / 255.0F;
      guigraphics.setColor(f2, f2, f2, 1.0F);
      guigraphics.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, f, i, this.height, 64, 64);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.scroll = Math.max(0.0F, this.scroll + f * this.scrollSpeed);
      this.renderBg(guigraphics);
      int k = this.width / 2 - 128;
      int l = this.height + 50;
      float f1 = -this.scroll;
      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, f1, 0.0F);
      this.logoRenderer.renderLogo(guigraphics, this.width, 1.0F, l);
      int i1 = l + 100;

      for(int j1 = 0; j1 < this.lines.size(); ++j1) {
         if (j1 == this.lines.size() - 1) {
            float f2 = (float)i1 + f1 - (float)(this.height / 2 - 6);
            if (f2 < 0.0F) {
               guigraphics.pose().translate(0.0F, -f2, 0.0F);
            }
         }

         if ((float)i1 + f1 + 12.0F + 8.0F > 0.0F && (float)i1 + f1 < (float)this.height) {
            FormattedCharSequence formattedcharsequence = this.lines.get(j1);
            if (this.centeredLines.contains(j1)) {
               guigraphics.drawCenteredString(this.font, formattedcharsequence, k + 128, i1, 16777215);
            } else {
               guigraphics.drawString(this.font, formattedcharsequence, k, i1, 16777215);
            }
         }

         i1 += 12;
      }

      guigraphics.pose().popPose();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
      guigraphics.blit(VIGNETTE_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      super.render(guigraphics, i, j, f);
   }

   public void removed() {
      this.minecraft.getMusicManager().stopPlaying(Musics.CREDITS);
   }

   public Music getBackgroundMusic() {
      return Musics.CREDITS;
   }

   @FunctionalInterface
   interface CreditsReader {
      void read(Reader reader) throws IOException;
   }
}
