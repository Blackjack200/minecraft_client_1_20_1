package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class LightTexture implements AutoCloseable {
   public static final int FULL_BRIGHT = 15728880;
   public static final int FULL_SKY = 15728640;
   public static final int FULL_BLOCK = 240;
   private final DynamicTexture lightTexture;
   private final NativeImage lightPixels;
   private final ResourceLocation lightTextureLocation;
   private boolean updateLightTexture;
   private float blockLightRedFlicker;
   private final GameRenderer renderer;
   private final Minecraft minecraft;

   public LightTexture(GameRenderer gamerenderer, Minecraft minecraft) {
      this.renderer = gamerenderer;
      this.minecraft = minecraft;
      this.lightTexture = new DynamicTexture(16, 16, false);
      this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
      this.lightPixels = this.lightTexture.getPixels();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            this.lightPixels.setPixelRGBA(j, i, -1);
         }
      }

      this.lightTexture.upload();
   }

   public void close() {
      this.lightTexture.close();
   }

   public void tick() {
      this.blockLightRedFlicker += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1D);
      this.blockLightRedFlicker *= 0.9F;
      this.updateLightTexture = true;
   }

   public void turnOffLightLayer() {
      RenderSystem.setShaderTexture(2, 0);
   }

   public void turnOnLightLayer() {
      RenderSystem.setShaderTexture(2, this.lightTextureLocation);
      this.minecraft.getTextureManager().bindForSetup(this.lightTextureLocation);
      RenderSystem.texParameter(3553, 10241, 9729);
      RenderSystem.texParameter(3553, 10240, 9729);
   }

   private float getDarknessGamma(float f) {
      if (this.minecraft.player.hasEffect(MobEffects.DARKNESS)) {
         MobEffectInstance mobeffectinstance = this.minecraft.player.getEffect(MobEffects.DARKNESS);
         if (mobeffectinstance != null && mobeffectinstance.getFactorData().isPresent()) {
            return mobeffectinstance.getFactorData().get().getFactor(this.minecraft.player, f);
         }
      }

      return 0.0F;
   }

   private float calculateDarknessScale(LivingEntity livingentity, float f, float f1) {
      float f2 = 0.45F * f;
      return Math.max(0.0F, Mth.cos(((float)livingentity.tickCount - f1) * (float)Math.PI * 0.025F) * f2);
   }

   public void updateLightTexture(float f) {
      if (this.updateLightTexture) {
         this.updateLightTexture = false;
         this.minecraft.getProfiler().push("lightTex");
         ClientLevel clientlevel = this.minecraft.level;
         if (clientlevel != null) {
            float f1 = clientlevel.getSkyDarken(1.0F);
            float f2;
            if (clientlevel.getSkyFlashTime() > 0) {
               f2 = 1.0F;
            } else {
               f2 = f1 * 0.95F + 0.05F;
            }

            float f4 = this.minecraft.options.darknessEffectScale().get().floatValue();
            float f5 = this.getDarknessGamma(f) * f4;
            float f6 = this.calculateDarknessScale(this.minecraft.player, f5, f) * f4;
            float f7 = this.minecraft.player.getWaterVision();
            float f8;
            if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
               f8 = GameRenderer.getNightVisionScale(this.minecraft.player, f);
            } else if (f7 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
               f8 = f7;
            } else {
               f8 = 0.0F;
            }

            Vector3f vector3f = (new Vector3f(f1, f1, 1.0F)).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
            float f11 = this.blockLightRedFlicker + 1.5F;
            Vector3f vector3f1 = new Vector3f();

            for(int i = 0; i < 16; ++i) {
               for(int j = 0; j < 16; ++j) {
                  float f12 = getBrightness(clientlevel.dimensionType(), i) * f2;
                  float f13 = getBrightness(clientlevel.dimensionType(), j) * f11;
                  float f15 = f13 * ((f13 * 0.6F + 0.4F) * 0.6F + 0.4F);
                  float f16 = f13 * (f13 * f13 * 0.6F + 0.4F);
                  vector3f1.set(f13, f15, f16);
                  boolean flag = clientlevel.effects().forceBrightLightmap();
                  if (flag) {
                     vector3f1.lerp(new Vector3f(0.99F, 1.12F, 1.0F), 0.25F);
                     clampColor(vector3f1);
                  } else {
                     Vector3f vector3f2 = (new Vector3f((Vector3fc)vector3f)).mul(f12);
                     vector3f1.add(vector3f2);
                     vector3f1.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                     if (this.renderer.getDarkenWorldAmount(f) > 0.0F) {
                        float f17 = this.renderer.getDarkenWorldAmount(f);
                        Vector3f vector3f3 = (new Vector3f((Vector3fc)vector3f1)).mul(0.7F, 0.6F, 0.6F);
                        vector3f1.lerp(vector3f3, f17);
                     }
                  }

                  if (f8 > 0.0F) {
                     float f18 = Math.max(vector3f1.x(), Math.max(vector3f1.y(), vector3f1.z()));
                     if (f18 < 1.0F) {
                        float f19 = 1.0F / f18;
                        Vector3f vector3f4 = (new Vector3f((Vector3fc)vector3f1)).mul(f19);
                        vector3f1.lerp(vector3f4, f8);
                     }
                  }

                  if (!flag) {
                     if (f6 > 0.0F) {
                        vector3f1.add(-f6, -f6, -f6);
                     }

                     clampColor(vector3f1);
                  }

                  float f20 = this.minecraft.options.gamma().get().floatValue();
                  Vector3f vector3f5 = new Vector3f(this.notGamma(vector3f1.x), this.notGamma(vector3f1.y), this.notGamma(vector3f1.z));
                  vector3f1.lerp(vector3f5, Math.max(0.0F, f20 - f5));
                  vector3f1.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                  clampColor(vector3f1);
                  vector3f1.mul(255.0F);
                  int k = 255;
                  int l = (int)vector3f1.x();
                  int i1 = (int)vector3f1.y();
                  int j1 = (int)vector3f1.z();
                  this.lightPixels.setPixelRGBA(j, i, -16777216 | j1 << 16 | i1 << 8 | l);
               }
            }

            this.lightTexture.upload();
            this.minecraft.getProfiler().pop();
         }
      }
   }

   private static void clampColor(Vector3f vector3f) {
      vector3f.set(Mth.clamp(vector3f.x, 0.0F, 1.0F), Mth.clamp(vector3f.y, 0.0F, 1.0F), Mth.clamp(vector3f.z, 0.0F, 1.0F));
   }

   private float notGamma(float f) {
      float f1 = 1.0F - f;
      return 1.0F - f1 * f1 * f1 * f1;
   }

   public static float getBrightness(DimensionType dimensiontype, int i) {
      float f = (float)i / 15.0F;
      float f1 = f / (4.0F - 3.0F * f);
      return Mth.lerp(dimensiontype.ambientLight(), f1, 1.0F);
   }

   public static int pack(int i, int j) {
      return i << 4 | j << 20;
   }

   public static int block(int i) {
      return i >> 4 & '\uffff';
   }

   public static int sky(int i) {
      return i >> 20 & '\uffff';
   }
}
