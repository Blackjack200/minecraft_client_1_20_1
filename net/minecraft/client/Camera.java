package net.minecraft.client;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Camera {
   private boolean initialized;
   private BlockGetter level;
   private Entity entity;
   private Vec3 position = Vec3.ZERO;
   private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
   private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
   private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
   private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
   private float xRot;
   private float yRot;
   private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
   private boolean detached;
   private float eyeHeight;
   private float eyeHeightOld;
   public static final float FOG_DISTANCE_SCALE = 0.083333336F;

   public void setup(BlockGetter blockgetter, Entity entity, boolean flag, boolean flag1, float f) {
      this.initialized = true;
      this.level = blockgetter;
      this.entity = entity;
      this.detached = flag;
      this.setRotation(entity.getViewYRot(f), entity.getViewXRot(f));
      this.setPosition(Mth.lerp((double)f, entity.xo, entity.getX()), Mth.lerp((double)f, entity.yo, entity.getY()) + (double)Mth.lerp(f, this.eyeHeightOld, this.eyeHeight), Mth.lerp((double)f, entity.zo, entity.getZ()));
      if (flag) {
         if (flag1) {
            this.setRotation(this.yRot + 180.0F, -this.xRot);
         }

         this.move(-this.getMaxZoom(4.0D), 0.0D, 0.0D);
      } else if (entity instanceof LivingEntity && ((LivingEntity)entity).isSleeping()) {
         Direction direction = ((LivingEntity)entity).getBedOrientation();
         this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
         this.move(0.0D, 0.3D, 0.0D);
      }

   }

   public void tick() {
      if (this.entity != null) {
         this.eyeHeightOld = this.eyeHeight;
         this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5F;
      }

   }

   private double getMaxZoom(double d0) {
      for(int i = 0; i < 8; ++i) {
         float f = (float)((i & 1) * 2 - 1);
         float f1 = (float)((i >> 1 & 1) * 2 - 1);
         float f2 = (float)((i >> 2 & 1) * 2 - 1);
         f *= 0.1F;
         f1 *= 0.1F;
         f2 *= 0.1F;
         Vec3 vec3 = this.position.add((double)f, (double)f1, (double)f2);
         Vec3 vec31 = new Vec3(this.position.x - (double)this.forwards.x() * d0 + (double)f, this.position.y - (double)this.forwards.y() * d0 + (double)f1, this.position.z - (double)this.forwards.z() * d0 + (double)f2);
         HitResult hitresult = this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
         if (hitresult.getType() != HitResult.Type.MISS) {
            double d1 = hitresult.getLocation().distanceTo(this.position);
            if (d1 < d0) {
               d0 = d1;
            }
         }
      }

      return d0;
   }

   protected void move(double d0, double d1, double d2) {
      double d3 = (double)this.forwards.x() * d0 + (double)this.up.x() * d1 + (double)this.left.x() * d2;
      double d4 = (double)this.forwards.y() * d0 + (double)this.up.y() * d1 + (double)this.left.y() * d2;
      double d5 = (double)this.forwards.z() * d0 + (double)this.up.z() * d1 + (double)this.left.z() * d2;
      this.setPosition(new Vec3(this.position.x + d3, this.position.y + d4, this.position.z + d5));
   }

   protected void setRotation(float f, float f1) {
      this.xRot = f1;
      this.yRot = f;
      this.rotation.rotationYXZ(-f * ((float)Math.PI / 180F), f1 * ((float)Math.PI / 180F), 0.0F);
      this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
      this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
      this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
   }

   protected void setPosition(double d0, double d1, double d2) {
      this.setPosition(new Vec3(d0, d1, d2));
   }

   protected void setPosition(Vec3 vec3) {
      this.position = vec3;
      this.blockPosition.set(vec3.x, vec3.y, vec3.z);
   }

   public Vec3 getPosition() {
      return this.position;
   }

   public BlockPos getBlockPosition() {
      return this.blockPosition;
   }

   public float getXRot() {
      return this.xRot;
   }

   public float getYRot() {
      return this.yRot;
   }

   public Quaternionf rotation() {
      return this.rotation;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public boolean isDetached() {
      return this.detached;
   }

   public Camera.NearPlane getNearPlane() {
      Minecraft minecraft = Minecraft.getInstance();
      double d0 = (double)minecraft.getWindow().getWidth() / (double)minecraft.getWindow().getHeight();
      double d1 = Math.tan((double)((float)minecraft.options.fov().get().intValue() * ((float)Math.PI / 180F)) / 2.0D) * (double)0.05F;
      double d2 = d1 * d0;
      Vec3 vec3 = (new Vec3(this.forwards)).scale((double)0.05F);
      Vec3 vec31 = (new Vec3(this.left)).scale(d2);
      Vec3 vec32 = (new Vec3(this.up)).scale(d1);
      return new Camera.NearPlane(vec3, vec31, vec32);
   }

   public FogType getFluidInCamera() {
      if (!this.initialized) {
         return FogType.NONE;
      } else {
         FluidState fluidstate = this.level.getFluidState(this.blockPosition);
         if (fluidstate.is(FluidTags.WATER) && this.position.y < (double)((float)this.blockPosition.getY() + fluidstate.getHeight(this.level, this.blockPosition))) {
            return FogType.WATER;
         } else {
            Camera.NearPlane camera_nearplane = this.getNearPlane();

            for(Vec3 vec3 : Arrays.asList(camera_nearplane.forward, camera_nearplane.getTopLeft(), camera_nearplane.getTopRight(), camera_nearplane.getBottomLeft(), camera_nearplane.getBottomRight())) {
               Vec3 vec31 = this.position.add(vec3);
               BlockPos blockpos = BlockPos.containing(vec31);
               FluidState fluidstate1 = this.level.getFluidState(blockpos);
               if (fluidstate1.is(FluidTags.LAVA)) {
                  if (vec31.y <= (double)(fluidstate1.getHeight(this.level, blockpos) + (float)blockpos.getY())) {
                     return FogType.LAVA;
                  }
               } else {
                  BlockState blockstate = this.level.getBlockState(blockpos);
                  if (blockstate.is(Blocks.POWDER_SNOW)) {
                     return FogType.POWDER_SNOW;
                  }
               }
            }

            return FogType.NONE;
         }
      }
   }

   public final Vector3f getLookVector() {
      return this.forwards;
   }

   public final Vector3f getUpVector() {
      return this.up;
   }

   public final Vector3f getLeftVector() {
      return this.left;
   }

   public void reset() {
      this.level = null;
      this.entity = null;
      this.initialized = false;
   }

   public static class NearPlane {
      final Vec3 forward;
      private final Vec3 left;
      private final Vec3 up;

      NearPlane(Vec3 vec3, Vec3 vec31, Vec3 vec32) {
         this.forward = vec3;
         this.left = vec31;
         this.up = vec32;
      }

      public Vec3 getTopLeft() {
         return this.forward.add(this.up).add(this.left);
      }

      public Vec3 getTopRight() {
         return this.forward.add(this.up).subtract(this.left);
      }

      public Vec3 getBottomLeft() {
         return this.forward.subtract(this.up).add(this.left);
      }

      public Vec3 getBottomRight() {
         return this.forward.subtract(this.up).subtract(this.left);
      }

      public Vec3 getPointOnPlane(float f, float f1) {
         return this.forward.add(this.up.scale((double)f1)).subtract(this.left.scale((double)f));
      }
   }
}
