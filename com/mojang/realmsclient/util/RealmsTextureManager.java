package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class RealmsTextureManager {
   private static final Map<String, RealmsTextureManager.RealmsTexture> TEXTURES = Maps.newHashMap();
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

   public static ResourceLocation worldTemplate(String s, @Nullable String s1) {
      return s1 == null ? TEMPLATE_ICON_LOCATION : getTexture(s, s1);
   }

   private static ResourceLocation getTexture(String s, String s1) {
      RealmsTextureManager.RealmsTexture realmstexturemanager_realmstexture = TEXTURES.get(s);
      if (realmstexturemanager_realmstexture != null && realmstexturemanager_realmstexture.image().equals(s1)) {
         return realmstexturemanager_realmstexture.textureId;
      } else {
         NativeImage nativeimage = loadImage(s1);
         if (nativeimage == null) {
            ResourceLocation resourcelocation = MissingTextureAtlasSprite.getLocation();
            TEXTURES.put(s, new RealmsTextureManager.RealmsTexture(s1, resourcelocation));
            return resourcelocation;
         } else {
            ResourceLocation resourcelocation1 = new ResourceLocation("realms", "dynamic/" + s);
            Minecraft.getInstance().getTextureManager().register(resourcelocation1, new DynamicTexture(nativeimage));
            TEXTURES.put(s, new RealmsTextureManager.RealmsTexture(s1, resourcelocation1));
            return resourcelocation1;
         }
      }
   }

   @Nullable
   private static NativeImage loadImage(String s) {
      byte[] abyte = Base64.getDecoder().decode(s);
      ByteBuffer bytebuffer = MemoryUtil.memAlloc(abyte.length);

      try {
         return NativeImage.read(bytebuffer.put(abyte).flip());
      } catch (IOException var7) {
         LOGGER.warn("Failed to load world image: {}", s, var7);
      } finally {
         MemoryUtil.memFree(bytebuffer);
      }

      return null;
   }

   public static record RealmsTexture(String image, ResourceLocation textureId) {
      final ResourceLocation textureId;
   }
}
