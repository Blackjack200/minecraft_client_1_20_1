package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.slf4j.Logger;

public class TextureAtlas extends AbstractTexture implements Dumpable, Tickable {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** @deprecated */
   @Deprecated
   public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
   /** @deprecated */
   @Deprecated
   public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
   private List<SpriteContents> sprites = List.of();
   private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
   private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
   private final ResourceLocation location;
   private final int maxSupportedTextureSize;
   private int width;
   private int height;
   private int mipLevel;

   public TextureAtlas(ResourceLocation resourcelocation) {
      this.location = resourcelocation;
      this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
   }

   public void load(ResourceManager resourcemanager) {
   }

   public void upload(SpriteLoader.Preparations spriteloader_preparations) {
      LOGGER.info("Created: {}x{}x{} {}-atlas", spriteloader_preparations.width(), spriteloader_preparations.height(), spriteloader_preparations.mipLevel(), this.location);
      TextureUtil.prepareImage(this.getId(), spriteloader_preparations.mipLevel(), spriteloader_preparations.width(), spriteloader_preparations.height());
      this.width = spriteloader_preparations.width();
      this.height = spriteloader_preparations.height();
      this.mipLevel = spriteloader_preparations.mipLevel();
      this.clearTextureData();
      this.texturesByName = Map.copyOf(spriteloader_preparations.regions());
      List<SpriteContents> list = new ArrayList<>();
      List<TextureAtlasSprite.Ticker> list1 = new ArrayList<>();

      for(TextureAtlasSprite textureatlassprite : spriteloader_preparations.regions().values()) {
         list.add(textureatlassprite.contents());

         try {
            textureatlassprite.uploadFirstFrame();
         } catch (Throwable var9) {
            CrashReport crashreport = CrashReport.forThrowable(var9, "Stitching texture atlas");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Texture being stitched together");
            crashreportcategory.setDetail("Atlas path", this.location);
            crashreportcategory.setDetail("Sprite", textureatlassprite);
            throw new ReportedException(crashreport);
         }

         TextureAtlasSprite.Ticker textureatlassprite_ticker = textureatlassprite.createTicker();
         if (textureatlassprite_ticker != null) {
            list1.add(textureatlassprite_ticker);
         }
      }

      this.sprites = List.copyOf(list);
      this.animatedTextures = List.copyOf(list1);
   }

   public void dumpContents(ResourceLocation resourcelocation, Path path) throws IOException {
      String s = resourcelocation.toDebugFileName();
      TextureUtil.writeAsPNG(path, s, this.getId(), this.mipLevel, this.width, this.height);
      dumpSpriteNames(path, s, this.texturesByName);
   }

   private static void dumpSpriteNames(Path path, String s, Map<ResourceLocation, TextureAtlasSprite> map) {
      Path path1 = path.resolve(s + ".txt");

      try {
         Writer writer = Files.newBufferedWriter(path1);

         try {
            for(Map.Entry<ResourceLocation, TextureAtlasSprite> map_entry : map.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
               TextureAtlasSprite textureatlassprite = map_entry.getValue();
               writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", map_entry.getKey(), textureatlassprite.getX(), textureatlassprite.getY(), textureatlassprite.contents().width(), textureatlassprite.contents().height()));
            }
         } catch (Throwable var9) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var10) {
         LOGGER.warn("Failed to write file {}", path1, var10);
      }

   }

   public void cycleAnimationFrames() {
      this.bind();

      for(TextureAtlasSprite.Ticker textureatlassprite_ticker : this.animatedTextures) {
         textureatlassprite_ticker.tickAndUpload();
      }

   }

   public void tick() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::cycleAnimationFrames);
      } else {
         this.cycleAnimationFrames();
      }

   }

   public TextureAtlasSprite getSprite(ResourceLocation resourcelocation) {
      TextureAtlasSprite textureatlassprite = this.texturesByName.get(resourcelocation);
      return textureatlassprite == null ? this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : textureatlassprite;
   }

   public void clearTextureData() {
      this.sprites.forEach(SpriteContents::close);
      this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
      this.sprites = List.of();
      this.animatedTextures = List.of();
      this.texturesByName = Map.of();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public int maxSupportedTextureSize() {
      return this.maxSupportedTextureSize;
   }

   int getWidth() {
      return this.width;
   }

   int getHeight() {
      return this.height;
   }

   public void updateFilter(SpriteLoader.Preparations spriteloader_preparations) {
      this.setFilter(false, spriteloader_preparations.mipLevel() > 0);
   }
}
