package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class FontTexture extends AbstractTexture implements Dumpable {
   private static final int SIZE = 256;
   private final GlyphRenderTypes renderTypes;
   private final boolean colored;
   private final FontTexture.Node root;

   public FontTexture(GlyphRenderTypes glyphrendertypes, boolean flag) {
      this.colored = flag;
      this.root = new FontTexture.Node(0, 0, 256, 256);
      TextureUtil.prepareImage(flag ? NativeImage.InternalGlFormat.RGBA : NativeImage.InternalGlFormat.RED, this.getId(), 256, 256);
      this.renderTypes = glyphrendertypes;
   }

   public void load(ResourceManager resourcemanager) {
   }

   public void close() {
      this.releaseId();
   }

   @Nullable
   public BakedGlyph add(SheetGlyphInfo sheetglyphinfo) {
      if (sheetglyphinfo.isColored() != this.colored) {
         return null;
      } else {
         FontTexture.Node fonttexture_node = this.root.insert(sheetglyphinfo);
         if (fonttexture_node != null) {
            this.bind();
            sheetglyphinfo.upload(fonttexture_node.x, fonttexture_node.y);
            float f = 256.0F;
            float f1 = 256.0F;
            float f2 = 0.01F;
            return new BakedGlyph(this.renderTypes, ((float)fonttexture_node.x + 0.01F) / 256.0F, ((float)fonttexture_node.x - 0.01F + (float)sheetglyphinfo.getPixelWidth()) / 256.0F, ((float)fonttexture_node.y + 0.01F) / 256.0F, ((float)fonttexture_node.y - 0.01F + (float)sheetglyphinfo.getPixelHeight()) / 256.0F, sheetglyphinfo.getLeft(), sheetglyphinfo.getRight(), sheetglyphinfo.getUp(), sheetglyphinfo.getDown());
         } else {
            return null;
         }
      }
   }

   public void dumpContents(ResourceLocation resourcelocation, Path path) {
      String s = resourcelocation.toDebugFileName();
      TextureUtil.writeAsPNG(path, s, this.getId(), 0, 256, 256, (i) -> (i & -16777216) == 0 ? -16777216 : i);
   }

   static class Node {
      final int x;
      final int y;
      private final int width;
      private final int height;
      @Nullable
      private FontTexture.Node left;
      @Nullable
      private FontTexture.Node right;
      private boolean occupied;

      Node(int i, int j, int k, int l) {
         this.x = i;
         this.y = j;
         this.width = k;
         this.height = l;
      }

      @Nullable
      FontTexture.Node insert(SheetGlyphInfo sheetglyphinfo) {
         if (this.left != null && this.right != null) {
            FontTexture.Node fonttexture_node = this.left.insert(sheetglyphinfo);
            if (fonttexture_node == null) {
               fonttexture_node = this.right.insert(sheetglyphinfo);
            }

            return fonttexture_node;
         } else if (this.occupied) {
            return null;
         } else {
            int i = sheetglyphinfo.getPixelWidth();
            int j = sheetglyphinfo.getPixelHeight();
            if (i <= this.width && j <= this.height) {
               if (i == this.width && j == this.height) {
                  this.occupied = true;
                  return this;
               } else {
                  int k = this.width - i;
                  int l = this.height - j;
                  if (k > l) {
                     this.left = new FontTexture.Node(this.x, this.y, i, this.height);
                     this.right = new FontTexture.Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
                  } else {
                     this.left = new FontTexture.Node(this.x, this.y, this.width, j);
                     this.right = new FontTexture.Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
                  }

                  return this.left.insert(sheetglyphinfo);
               }
            } else {
               return null;
            }
         }
      }
   }
}
