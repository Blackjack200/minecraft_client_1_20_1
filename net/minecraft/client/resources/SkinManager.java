package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

public class SkinManager {
   public static final String PROPERTY_TEXTURES = "textures";
   private final TextureManager textureManager;
   private final File skinsDirectory;
   private final MinecraftSessionService sessionService;
   private final LoadingCache<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> insecureSkinCache;

   public SkinManager(TextureManager texturemanager, File file, final MinecraftSessionService minecraftsessionservice) {
      this.textureManager = texturemanager;
      this.skinsDirectory = file;
      this.sessionService = minecraftsessionservice;
      this.insecureSkinCache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<String, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {
         public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(String s) {
            GameProfile gameprofile = new GameProfile((UUID)null, "dummy_mcdummyface");
            gameprofile.getProperties().put("textures", new Property("textures", s, ""));

            try {
               return minecraftsessionservice.getTextures(gameprofile, false);
            } catch (Throwable var4) {
               return ImmutableMap.of();
            }
         }
      });
   }

   public ResourceLocation registerTexture(MinecraftProfileTexture minecraftprofiletexture, MinecraftProfileTexture.Type minecraftprofiletexture_type) {
      return this.registerTexture(minecraftprofiletexture, minecraftprofiletexture_type, (SkinManager.SkinTextureCallback)null);
   }

   private ResourceLocation registerTexture(MinecraftProfileTexture minecraftprofiletexture, MinecraftProfileTexture.Type minecraftprofiletexture_type, @Nullable SkinManager.SkinTextureCallback skinmanager_skintexturecallback) {
      String s = Hashing.sha1().hashUnencodedChars(minecraftprofiletexture.getHash()).toString();
      ResourceLocation resourcelocation = getTextureLocation(minecraftprofiletexture_type, s);
      AbstractTexture abstracttexture = this.textureManager.getTexture(resourcelocation, MissingTextureAtlasSprite.getTexture());
      if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
         File file = new File(this.skinsDirectory, s.length() > 2 ? s.substring(0, 2) : "xx");
         File file1 = new File(file, s);
         HttpTexture httptexture = new HttpTexture(file1, minecraftprofiletexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), minecraftprofiletexture_type == Type.SKIN, () -> {
            if (skinmanager_skintexturecallback != null) {
               skinmanager_skintexturecallback.onSkinTextureAvailable(minecraftprofiletexture_type, resourcelocation, minecraftprofiletexture);
            }

         });
         this.textureManager.register(resourcelocation, httptexture);
      } else if (skinmanager_skintexturecallback != null) {
         skinmanager_skintexturecallback.onSkinTextureAvailable(minecraftprofiletexture_type, resourcelocation, minecraftprofiletexture);
      }

      return resourcelocation;
   }

   private static ResourceLocation getTextureLocation(MinecraftProfileTexture.Type minecraftprofiletexture_type, String s) {
      String var10000;
      switch (minecraftprofiletexture_type) {
         case SKIN:
            var10000 = "skins";
            break;
         case CAPE:
            var10000 = "capes";
            break;
         case ELYTRA:
            var10000 = "elytra";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      String s1 = var10000;
      return new ResourceLocation(s1 + "/" + s);
   }

   public void registerSkins(GameProfile gameprofile, SkinManager.SkinTextureCallback skinmanager_skintexturecallback, boolean flag) {
      Runnable runnable = () -> {
         Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();

         try {
            map.putAll(this.sessionService.getTextures(gameprofile, flag));
         } catch (InsecurePublicKeyException var7) {
         }

         if (map.isEmpty()) {
            gameprofile.getProperties().clear();
            if (gameprofile.getId().equals(Minecraft.getInstance().getUser().getGameProfile().getId())) {
               gameprofile.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
               map.putAll(this.sessionService.getTextures(gameprofile, false));
            } else {
               this.sessionService.fillProfileProperties(gameprofile, flag);

               try {
                  map.putAll(this.sessionService.getTextures(gameprofile, flag));
               } catch (InsecurePublicKeyException var6) {
               }
            }
         }

         Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(() -> ImmutableList.of(Type.SKIN, Type.CAPE).forEach((minecraftprofiletexture_type) -> {
                  if (map.containsKey(minecraftprofiletexture_type)) {
                     this.registerTexture(map.get(minecraftprofiletexture_type), minecraftprofiletexture_type, skinmanager_skintexturecallback);
                  }

               })));
      };
      Util.backgroundExecutor().execute(runnable);
   }

   public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile gameprofile) {
      Property property = Iterables.getFirst(gameprofile.getProperties().get("textures"), (Property)null);
      return (Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>)(property == null ? ImmutableMap.of() : this.insecureSkinCache.getUnchecked(property.getValue()));
   }

   public ResourceLocation getInsecureSkinLocation(GameProfile gameprofile) {
      MinecraftProfileTexture minecraftprofiletexture = this.getInsecureSkinInformation(gameprofile).get(Type.SKIN);
      return minecraftprofiletexture != null ? this.registerTexture(minecraftprofiletexture, Type.SKIN) : DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(gameprofile));
   }

   public interface SkinTextureCallback {
      void onSkinTextureAvailable(MinecraftProfileTexture.Type minecraftprofiletexture_type, ResourceLocation resourcelocation, MinecraftProfileTexture minecraftprofiletexture);
   }
}
