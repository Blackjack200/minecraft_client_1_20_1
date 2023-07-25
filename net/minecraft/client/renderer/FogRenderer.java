package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class FogRenderer {
   private static final int WATER_FOG_DISTANCE = 96;
   private static final List<FogRenderer.MobEffectFogFunction> MOB_EFFECT_FOG = Lists.newArrayList(new FogRenderer.BlindnessFogFunction(), new FogRenderer.DarknessFogFunction());
   public static final float BIOME_FOG_TRANSITION_TIME = 5000.0F;
   private static float fogRed;
   private static float fogGreen;
   private static float fogBlue;
   private static int targetBiomeFog = -1;
   private static int previousBiomeFog = -1;
   private static long biomeChangedTime = -1L;

   public static void setupColor(Camera camera, float f, ClientLevel clientlevel, int i, float f1) {
      FogType fogtype = camera.getFluidInCamera();
      Entity entity = camera.getEntity();
      if (fogtype == FogType.WATER) {
         long j = Util.getMillis();
         int k = clientlevel.getBiome(BlockPos.containing(camera.getPosition())).value().getWaterFogColor();
         if (biomeChangedTime < 0L) {
            targetBiomeFog = k;
            previousBiomeFog = k;
            biomeChangedTime = j;
         }

         int l = targetBiomeFog >> 16 & 255;
         int i1 = targetBiomeFog >> 8 & 255;
         int j1 = targetBiomeFog & 255;
         int k1 = previousBiomeFog >> 16 & 255;
         int l1 = previousBiomeFog >> 8 & 255;
         int i2 = previousBiomeFog & 255;
         float f2 = Mth.clamp((float)(j - biomeChangedTime) / 5000.0F, 0.0F, 1.0F);
         float f3 = Mth.lerp(f2, (float)k1, (float)l);
         float f4 = Mth.lerp(f2, (float)l1, (float)i1);
         float f5 = Mth.lerp(f2, (float)i2, (float)j1);
         fogRed = f3 / 255.0F;
         fogGreen = f4 / 255.0F;
         fogBlue = f5 / 255.0F;
         if (targetBiomeFog != k) {
            targetBiomeFog = k;
            previousBiomeFog = Mth.floor(f3) << 16 | Mth.floor(f4) << 8 | Mth.floor(f5);
            biomeChangedTime = j;
         }
      } else if (fogtype == FogType.LAVA) {
         fogRed = 0.6F;
         fogGreen = 0.1F;
         fogBlue = 0.0F;
         biomeChangedTime = -1L;
      } else if (fogtype == FogType.POWDER_SNOW) {
         fogRed = 0.623F;
         fogGreen = 0.734F;
         fogBlue = 0.785F;
         biomeChangedTime = -1L;
         RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
      } else {
         float f6 = 0.25F + 0.75F * (float)i / 32.0F;
         f6 = 1.0F - (float)Math.pow((double)f6, 0.25D);
         Vec3 vec3 = clientlevel.getSkyColor(camera.getPosition(), f);
         float f7 = (float)vec3.x;
         float f8 = (float)vec3.y;
         float f9 = (float)vec3.z;
         float f10 = Mth.clamp(Mth.cos(clientlevel.getTimeOfDay(f) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
         BiomeManager biomemanager = clientlevel.getBiomeManager();
         Vec3 vec31 = camera.getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
         Vec3 vec32 = CubicSampler.gaussianSampleVec3(vec31, (j2, k2, l2) -> clientlevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(j2, k2, l2).value().getFogColor()), f10));
         fogRed = (float)vec32.x();
         fogGreen = (float)vec32.y();
         fogBlue = (float)vec32.z();
         if (i >= 4) {
            float f11 = Mth.sin(clientlevel.getSunAngle(f)) > 0.0F ? -1.0F : 1.0F;
            Vector3f vector3f = new Vector3f(f11, 0.0F, 0.0F);
            float f12 = camera.getLookVector().dot(vector3f);
            if (f12 < 0.0F) {
               f12 = 0.0F;
            }

            if (f12 > 0.0F) {
               float[] afloat = clientlevel.effects().getSunriseColor(clientlevel.getTimeOfDay(f), f);
               if (afloat != null) {
                  f12 *= afloat[3];
                  fogRed = fogRed * (1.0F - f12) + afloat[0] * f12;
                  fogGreen = fogGreen * (1.0F - f12) + afloat[1] * f12;
                  fogBlue = fogBlue * (1.0F - f12) + afloat[2] * f12;
               }
            }
         }

         fogRed += (f7 - fogRed) * f6;
         fogGreen += (f8 - fogGreen) * f6;
         fogBlue += (f9 - fogBlue) * f6;
         float f13 = clientlevel.getRainLevel(f);
         if (f13 > 0.0F) {
            float f14 = 1.0F - f13 * 0.5F;
            float f15 = 1.0F - f13 * 0.4F;
            fogRed *= f14;
            fogGreen *= f14;
            fogBlue *= f15;
         }

         float f16 = clientlevel.getThunderLevel(f);
         if (f16 > 0.0F) {
            float f17 = 1.0F - f16 * 0.5F;
            fogRed *= f17;
            fogGreen *= f17;
            fogBlue *= f17;
         }

         biomeChangedTime = -1L;
      }

      float f18 = ((float)camera.getPosition().y - (float)clientlevel.getMinBuildHeight()) * clientlevel.getLevelData().getClearColorScale();
      FogRenderer.MobEffectFogFunction fogrenderer_mobeffectfogfunction = getPriorityFogFunction(entity, f);
      if (fogrenderer_mobeffectfogfunction != null) {
         LivingEntity livingentity = (LivingEntity)entity;
         f18 = fogrenderer_mobeffectfogfunction.getModifiedVoidDarkness(livingentity, livingentity.getEffect(fogrenderer_mobeffectfogfunction.getMobEffect()), f18, f);
      }

      if (f18 < 1.0F && fogtype != FogType.LAVA && fogtype != FogType.POWDER_SNOW) {
         if (f18 < 0.0F) {
            f18 = 0.0F;
         }

         f18 *= f18;
         fogRed *= f18;
         fogGreen *= f18;
         fogBlue *= f18;
      }

      if (f1 > 0.0F) {
         fogRed = fogRed * (1.0F - f1) + fogRed * 0.7F * f1;
         fogGreen = fogGreen * (1.0F - f1) + fogGreen * 0.6F * f1;
         fogBlue = fogBlue * (1.0F - f1) + fogBlue * 0.6F * f1;
      }

      float f19;
      if (fogtype == FogType.WATER) {
         if (entity instanceof LocalPlayer) {
            f19 = ((LocalPlayer)entity).getWaterVision();
         } else {
            f19 = 1.0F;
         }
      } else {
         label86: {
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity1 = (LivingEntity)entity;
               if (livingentity1.hasEffect(MobEffects.NIGHT_VISION) && !livingentity1.hasEffect(MobEffects.DARKNESS)) {
                  f19 = GameRenderer.getNightVisionScale(livingentity1, f);
                  break label86;
               }
            }

            f19 = 0.0F;
         }
      }

      if (fogRed != 0.0F && fogGreen != 0.0F && fogBlue != 0.0F) {
         float f23 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
         fogRed = fogRed * (1.0F - f19) + fogRed * f23 * f19;
         fogGreen = fogGreen * (1.0F - f19) + fogGreen * f23 * f19;
         fogBlue = fogBlue * (1.0F - f19) + fogBlue * f23 * f19;
      }

      RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
   }

   public static void setupNoFog() {
      RenderSystem.setShaderFogStart(Float.MAX_VALUE);
   }

   @Nullable
   private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity entity, float f) {
      if (entity instanceof LivingEntity livingentity) {
         return MOB_EFFECT_FOG.stream().filter((fogrenderer_mobeffectfogfunction) -> fogrenderer_mobeffectfogfunction.isEnabled(livingentity, f)).findFirst().orElse((FogRenderer.MobEffectFogFunction)null);
      } else {
         return null;
      }
   }

   public static void setupFog(Camera camera, FogRenderer.FogMode fogrenderer_fogmode, float f, boolean flag, float f1) {
      FogType fogtype = camera.getFluidInCamera();
      Entity entity = camera.getEntity();
      FogRenderer.FogData fogrenderer_fogdata = new FogRenderer.FogData(fogrenderer_fogmode);
      FogRenderer.MobEffectFogFunction fogrenderer_mobeffectfogfunction = getPriorityFogFunction(entity, f1);
      if (fogtype == FogType.LAVA) {
         if (entity.isSpectator()) {
            fogrenderer_fogdata.start = -8.0F;
            fogrenderer_fogdata.end = f * 0.5F;
         } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
            fogrenderer_fogdata.start = 0.0F;
            fogrenderer_fogdata.end = 3.0F;
         } else {
            fogrenderer_fogdata.start = 0.25F;
            fogrenderer_fogdata.end = 1.0F;
         }
      } else if (fogtype == FogType.POWDER_SNOW) {
         if (entity.isSpectator()) {
            fogrenderer_fogdata.start = -8.0F;
            fogrenderer_fogdata.end = f * 0.5F;
         } else {
            fogrenderer_fogdata.start = 0.0F;
            fogrenderer_fogdata.end = 2.0F;
         }
      } else if (fogrenderer_mobeffectfogfunction != null) {
         LivingEntity livingentity = (LivingEntity)entity;
         MobEffectInstance mobeffectinstance = livingentity.getEffect(fogrenderer_mobeffectfogfunction.getMobEffect());
         if (mobeffectinstance != null) {
            fogrenderer_mobeffectfogfunction.setupFog(fogrenderer_fogdata, livingentity, mobeffectinstance, f, f1);
         }
      } else if (fogtype == FogType.WATER) {
         fogrenderer_fogdata.start = -8.0F;
         fogrenderer_fogdata.end = 96.0F;
         if (entity instanceof LocalPlayer) {
            LocalPlayer localplayer = (LocalPlayer)entity;
            fogrenderer_fogdata.end *= Math.max(0.25F, localplayer.getWaterVision());
            Holder<Biome> holder = localplayer.level().getBiome(localplayer.blockPosition());
            if (holder.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
               fogrenderer_fogdata.end *= 0.85F;
            }
         }

         if (fogrenderer_fogdata.end > f) {
            fogrenderer_fogdata.end = f;
            fogrenderer_fogdata.shape = FogShape.CYLINDER;
         }
      } else if (flag) {
         fogrenderer_fogdata.start = f * 0.05F;
         fogrenderer_fogdata.end = Math.min(f, 192.0F) * 0.5F;
      } else if (fogrenderer_fogmode == FogRenderer.FogMode.FOG_SKY) {
         fogrenderer_fogdata.start = 0.0F;
         fogrenderer_fogdata.end = f;
         fogrenderer_fogdata.shape = FogShape.CYLINDER;
      } else {
         float f2 = Mth.clamp(f / 10.0F, 4.0F, 64.0F);
         fogrenderer_fogdata.start = f - f2;
         fogrenderer_fogdata.end = f;
         fogrenderer_fogdata.shape = FogShape.CYLINDER;
      }

      RenderSystem.setShaderFogStart(fogrenderer_fogdata.start);
      RenderSystem.setShaderFogEnd(fogrenderer_fogdata.end);
      RenderSystem.setShaderFogShape(fogrenderer_fogdata.shape);
   }

   public static void levelFogColor() {
      RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
   }

   static class BlindnessFogFunction implements FogRenderer.MobEffectFogFunction {
      public MobEffect getMobEffect() {
         return MobEffects.BLINDNESS;
      }

      public void setupFog(FogRenderer.FogData fogrenderer_fogdata, LivingEntity livingentity, MobEffectInstance mobeffectinstance, float f, float f1) {
         float f2 = mobeffectinstance.isInfiniteDuration() ? 5.0F : Mth.lerp(Math.min(1.0F, (float)mobeffectinstance.getDuration() / 20.0F), f, 5.0F);
         if (fogrenderer_fogdata.mode == FogRenderer.FogMode.FOG_SKY) {
            fogrenderer_fogdata.start = 0.0F;
            fogrenderer_fogdata.end = f2 * 0.8F;
         } else {
            fogrenderer_fogdata.start = f2 * 0.25F;
            fogrenderer_fogdata.end = f2;
         }

      }
   }

   static class DarknessFogFunction implements FogRenderer.MobEffectFogFunction {
      public MobEffect getMobEffect() {
         return MobEffects.DARKNESS;
      }

      public void setupFog(FogRenderer.FogData fogrenderer_fogdata, LivingEntity livingentity, MobEffectInstance mobeffectinstance, float f, float f1) {
         if (!mobeffectinstance.getFactorData().isEmpty()) {
            float f2 = Mth.lerp(mobeffectinstance.getFactorData().get().getFactor(livingentity, f1), f, 15.0F);
            fogrenderer_fogdata.start = fogrenderer_fogdata.mode == FogRenderer.FogMode.FOG_SKY ? 0.0F : f2 * 0.75F;
            fogrenderer_fogdata.end = f2;
         }
      }

      public float getModifiedVoidDarkness(LivingEntity livingentity, MobEffectInstance mobeffectinstance, float f, float f1) {
         return mobeffectinstance.getFactorData().isEmpty() ? 0.0F : 1.0F - mobeffectinstance.getFactorData().get().getFactor(livingentity, f1);
      }
   }

   static class FogData {
      public final FogRenderer.FogMode mode;
      public float start;
      public float end;
      public FogShape shape = FogShape.SPHERE;

      public FogData(FogRenderer.FogMode fogrenderer_fogmode) {
         this.mode = fogrenderer_fogmode;
      }
   }

   public static enum FogMode {
      FOG_SKY,
      FOG_TERRAIN;
   }

   interface MobEffectFogFunction {
      MobEffect getMobEffect();

      void setupFog(FogRenderer.FogData fogrenderer_fogdata, LivingEntity livingentity, MobEffectInstance mobeffectinstance, float f, float f1);

      default boolean isEnabled(LivingEntity livingentity, float f) {
         return livingentity.hasEffect(this.getMobEffect());
      }

      default float getModifiedVoidDarkness(LivingEntity livingentity, MobEffectInstance mobeffectinstance, float f, float f1) {
         MobEffectInstance mobeffectinstance1 = livingentity.getEffect(this.getMobEffect());
         if (mobeffectinstance1 != null) {
            if (mobeffectinstance1.endsWithin(19)) {
               f = 1.0F - (float)mobeffectinstance1.getDuration() / 20.0F;
            } else {
               f = 0.0F;
            }
         }

         return f;
      }
   }
}
