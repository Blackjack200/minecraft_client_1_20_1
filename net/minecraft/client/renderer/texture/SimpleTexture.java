package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class SimpleTexture extends AbstractTexture {
   static final Logger LOGGER = LogUtils.getLogger();
   protected final ResourceLocation location;

   public SimpleTexture(ResourceLocation resourcelocation) {
      this.location = resourcelocation;
   }

   public void load(ResourceManager resourcemanager) throws IOException {
      SimpleTexture.TextureImage simpletexture_textureimage = this.getTextureImage(resourcemanager);
      simpletexture_textureimage.throwIfError();
      TextureMetadataSection texturemetadatasection = simpletexture_textureimage.getTextureMetadata();
      boolean flag;
      boolean flag1;
      if (texturemetadatasection != null) {
         flag = texturemetadatasection.isBlur();
         flag1 = texturemetadatasection.isClamp();
      } else {
         flag = false;
         flag1 = false;
      }

      NativeImage nativeimage = simpletexture_textureimage.getImage();
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> this.doLoad(nativeimage, flag, flag1));
      } else {
         this.doLoad(nativeimage, flag, flag1);
      }

   }

   private void doLoad(NativeImage nativeimage, boolean flag, boolean flag1) {
      TextureUtil.prepareImage(this.getId(), 0, nativeimage.getWidth(), nativeimage.getHeight());
      nativeimage.upload(0, 0, 0, 0, 0, nativeimage.getWidth(), nativeimage.getHeight(), flag, flag1, false, true);
   }

   protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourcemanager) {
      return SimpleTexture.TextureImage.load(resourcemanager, this.location);
   }

   protected static class TextureImage implements Closeable {
      @Nullable
      private final TextureMetadataSection metadata;
      @Nullable
      private final NativeImage image;
      @Nullable
      private final IOException exception;

      public TextureImage(IOException ioexception) {
         this.exception = ioexception;
         this.metadata = null;
         this.image = null;
      }

      public TextureImage(@Nullable TextureMetadataSection texturemetadatasection, NativeImage nativeimage) {
         this.exception = null;
         this.metadata = texturemetadatasection;
         this.image = nativeimage;
      }

      public static SimpleTexture.TextureImage load(ResourceManager resourcemanager, ResourceLocation resourcelocation) {
         try {
            Resource resource = resourcemanager.getResourceOrThrow(resourcelocation);
            InputStream inputstream = resource.open();

            NativeImage nativeimage;
            try {
               nativeimage = NativeImage.read(inputstream);
            } catch (Throwable var9) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable var7) {
                     var9.addSuppressed(var7);
                  }
               }

               throw var9;
            }

            if (inputstream != null) {
               inputstream.close();
            }

            TextureMetadataSection texturemetadatasection = null;

            try {
               texturemetadatasection = resource.metadata().getSection(TextureMetadataSection.SERIALIZER).orElse((TextureMetadataSection)null);
            } catch (RuntimeException var8) {
               SimpleTexture.LOGGER.warn("Failed reading metadata of: {}", resourcelocation, var8);
            }

            return new SimpleTexture.TextureImage(texturemetadatasection, nativeimage);
         } catch (IOException var10) {
            return new SimpleTexture.TextureImage(var10);
         }
      }

      @Nullable
      public TextureMetadataSection getTextureMetadata() {
         return this.metadata;
      }

      public NativeImage getImage() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         } else {
            return this.image;
         }
      }

      public void close() {
         if (this.image != null) {
            this.image.close();
         }

      }

      public void throwIfError() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         }
      }
   }
}
