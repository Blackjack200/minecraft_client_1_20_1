package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public abstract class DimensionSpecialEffects {
   private static final Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = Util.make(new Object2ObjectArrayMap<>(), (object2objectarraymap) -> {
      DimensionSpecialEffects.OverworldEffects dimensionspecialeffects_overworldeffects = new DimensionSpecialEffects.OverworldEffects();
      object2objectarraymap.defaultReturnValue(dimensionspecialeffects_overworldeffects);
      object2objectarraymap.put(BuiltinDimensionTypes.OVERWORLD_EFFECTS, dimensionspecialeffects_overworldeffects);
      object2objectarraymap.put(BuiltinDimensionTypes.NETHER_EFFECTS, new DimensionSpecialEffects.NetherEffects());
      object2objectarraymap.put(BuiltinDimensionTypes.END_EFFECTS, new DimensionSpecialEffects.EndEffects());
   });
   private final float[] sunriseCol = new float[4];
   private final float cloudLevel;
   private final boolean hasGround;
   private final DimensionSpecialEffects.SkyType skyType;
   private final boolean forceBrightLightmap;
   private final boolean constantAmbientLight;

   public DimensionSpecialEffects(float f, boolean flag, DimensionSpecialEffects.SkyType dimensionspecialeffects_skytype, boolean flag1, boolean flag2) {
      this.cloudLevel = f;
      this.hasGround = flag;
      this.skyType = dimensionspecialeffects_skytype;
      this.forceBrightLightmap = flag1;
      this.constantAmbientLight = flag2;
   }

   public static DimensionSpecialEffects forType(DimensionType dimensiontype) {
      return EFFECTS.get(dimensiontype.effectsLocation());
   }

   @Nullable
   public float[] getSunriseColor(float f, float f1) {
      float f2 = 0.4F;
      float f3 = Mth.cos(f * ((float)Math.PI * 2F)) - 0.0F;
      float f4 = -0.0F;
      if (f3 >= -0.4F && f3 <= 0.4F) {
         float f5 = (f3 - -0.0F) / 0.4F * 0.5F + 0.5F;
         float f6 = 1.0F - (1.0F - Mth.sin(f5 * (float)Math.PI)) * 0.99F;
         f6 *= f6;
         this.sunriseCol[0] = f5 * 0.3F + 0.7F;
         this.sunriseCol[1] = f5 * f5 * 0.7F + 0.2F;
         this.sunriseCol[2] = f5 * f5 * 0.0F + 0.2F;
         this.sunriseCol[3] = f6;
         return this.sunriseCol;
      } else {
         return null;
      }
   }

   public float getCloudHeight() {
      return this.cloudLevel;
   }

   public boolean hasGround() {
      return this.hasGround;
   }

   public abstract Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f);

   public abstract boolean isFoggyAt(int i, int j);

   public DimensionSpecialEffects.SkyType skyType() {
      return this.skyType;
   }

   public boolean forceBrightLightmap() {
      return this.forceBrightLightmap;
   }

   public boolean constantAmbientLight() {
      return this.constantAmbientLight;
   }

   public static class EndEffects extends DimensionSpecialEffects {
      public EndEffects() {
         super(Float.NaN, false, DimensionSpecialEffects.SkyType.END, true, false);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
         return vec3.scale((double)0.15F);
      }

      public boolean isFoggyAt(int i, int j) {
         return false;
      }

      @Nullable
      public float[] getSunriseColor(float f, float f1) {
         return null;
      }
   }

   public static class NetherEffects extends DimensionSpecialEffects {
      public NetherEffects() {
         super(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, true);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
         return vec3;
      }

      public boolean isFoggyAt(int i, int j) {
         return true;
      }
   }

   public static class OverworldEffects extends DimensionSpecialEffects {
      public static final int CLOUD_LEVEL = 192;

      public OverworldEffects() {
         super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
         return vec3.multiply((double)(f * 0.94F + 0.06F), (double)(f * 0.94F + 0.06F), (double)(f * 0.91F + 0.09F));
      }

      public boolean isFoggyAt(int i, int j) {
         return false;
      }
   }

   public static enum SkyType {
      NONE,
      NORMAL,
      END;
   }
}
