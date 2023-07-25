package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float EYE_BED_OFFSET = 0.1F;
   protected M model;
   protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();

   public LivingEntityRenderer(EntityRendererProvider.Context entityrendererprovider_context, M entitymodel, float f) {
      super(entityrendererprovider_context);
      this.model = entitymodel;
      this.shadowRadius = f;
   }

   protected final boolean addLayer(RenderLayer<T, M> renderlayer) {
      return this.layers.add(renderlayer);
   }

   public M getModel() {
      return this.model;
   }

   public void render(T livingentity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      this.model.attackTime = this.getAttackAnim(livingentity, f1);
      this.model.riding = livingentity.isPassenger();
      this.model.young = livingentity.isBaby();
      float f2 = Mth.rotLerp(f1, livingentity.yBodyRotO, livingentity.yBodyRot);
      float f3 = Mth.rotLerp(f1, livingentity.yHeadRotO, livingentity.yHeadRot);
      float f4 = f3 - f2;
      if (livingentity.isPassenger() && livingentity.getVehicle() instanceof LivingEntity) {
         LivingEntity livingentity1 = (LivingEntity)livingentity.getVehicle();
         f2 = Mth.rotLerp(f1, livingentity1.yBodyRotO, livingentity1.yBodyRot);
         f4 = f3 - f2;
         float f5 = Mth.wrapDegrees(f4);
         if (f5 < -85.0F) {
            f5 = -85.0F;
         }

         if (f5 >= 85.0F) {
            f5 = 85.0F;
         }

         f2 = f3 - f5;
         if (f5 * f5 > 2500.0F) {
            f2 += f5 * 0.2F;
         }

         f4 = f3 - f2;
      }

      float f6 = Mth.lerp(f1, livingentity.xRotO, livingentity.getXRot());
      if (isEntityUpsideDown(livingentity)) {
         f6 *= -1.0F;
         f4 *= -1.0F;
      }

      if (livingentity.hasPose(Pose.SLEEPING)) {
         Direction direction = livingentity.getBedOrientation();
         if (direction != null) {
            float f7 = livingentity.getEyeHeight(Pose.STANDING) - 0.1F;
            posestack.translate((float)(-direction.getStepX()) * f7, 0.0F, (float)(-direction.getStepZ()) * f7);
         }
      }

      float f8 = this.getBob(livingentity, f1);
      this.setupRotations(livingentity, posestack, f8, f2, f1);
      posestack.scale(-1.0F, -1.0F, 1.0F);
      this.scale(livingentity, posestack, f1);
      posestack.translate(0.0F, -1.501F, 0.0F);
      float f9 = 0.0F;
      float f10 = 0.0F;
      if (!livingentity.isPassenger() && livingentity.isAlive()) {
         f9 = livingentity.walkAnimation.speed(f1);
         f10 = livingentity.walkAnimation.position(f1);
         if (livingentity.isBaby()) {
            f10 *= 3.0F;
         }

         if (f9 > 1.0F) {
            f9 = 1.0F;
         }
      }

      this.model.prepareMobModel(livingentity, f10, f9, f1);
      this.model.setupAnim(livingentity, f10, f9, f8, f4, f6);
      Minecraft minecraft = Minecraft.getInstance();
      boolean flag = this.isBodyVisible(livingentity);
      boolean flag1 = !flag && !livingentity.isInvisibleTo(minecraft.player);
      boolean flag2 = minecraft.shouldEntityAppearGlowing(livingentity);
      RenderType rendertype = this.getRenderType(livingentity, flag, flag1, flag2);
      if (rendertype != null) {
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(rendertype);
         int j = getOverlayCoords(livingentity, this.getWhiteOverlayProgress(livingentity, f1));
         this.model.renderToBuffer(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
      }

      if (!livingentity.isSpectator()) {
         for(RenderLayer<T, M> renderlayer : this.layers) {
            renderlayer.render(posestack, multibuffersource, i, livingentity, f10, f9, f1, f8, f4, f6);
         }
      }

      posestack.popPose();
      super.render(livingentity, f, f1, posestack, multibuffersource, i);
   }

   @Nullable
   protected RenderType getRenderType(T livingentity, boolean flag, boolean flag1, boolean flag2) {
      ResourceLocation resourcelocation = this.getTextureLocation(livingentity);
      if (flag1) {
         return RenderType.itemEntityTranslucentCull(resourcelocation);
      } else if (flag) {
         return this.model.renderType(resourcelocation);
      } else {
         return flag2 ? RenderType.outline(resourcelocation) : null;
      }
   }

   public static int getOverlayCoords(LivingEntity livingentity, float f) {
      return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingentity.hurtTime > 0 || livingentity.deathTime > 0));
   }

   protected boolean isBodyVisible(T livingentity) {
      return !livingentity.isInvisible();
   }

   private static float sleepDirectionToRotation(Direction direction) {
      switch (direction) {
         case SOUTH:
            return 90.0F;
         case WEST:
            return 0.0F;
         case NORTH:
            return 270.0F;
         case EAST:
            return 180.0F;
         default:
            return 0.0F;
      }
   }

   protected boolean isShaking(T livingentity) {
      return livingentity.isFullyFrozen();
   }

   protected void setupRotations(T livingentity, PoseStack posestack, float f, float f1, float f2) {
      if (this.isShaking(livingentity)) {
         f1 += (float)(Math.cos((double)livingentity.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      if (!livingentity.hasPose(Pose.SLEEPING)) {
         posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f1));
      }

      if (livingentity.deathTime > 0) {
         float f3 = ((float)livingentity.deathTime + f2 - 1.0F) / 20.0F * 1.6F;
         f3 = Mth.sqrt(f3);
         if (f3 > 1.0F) {
            f3 = 1.0F;
         }

         posestack.mulPose(Axis.ZP.rotationDegrees(f3 * this.getFlipDegrees(livingentity)));
      } else if (livingentity.isAutoSpinAttack()) {
         posestack.mulPose(Axis.XP.rotationDegrees(-90.0F - livingentity.getXRot()));
         posestack.mulPose(Axis.YP.rotationDegrees(((float)livingentity.tickCount + f2) * -75.0F));
      } else if (livingentity.hasPose(Pose.SLEEPING)) {
         Direction direction = livingentity.getBedOrientation();
         float f4 = direction != null ? sleepDirectionToRotation(direction) : f1;
         posestack.mulPose(Axis.YP.rotationDegrees(f4));
         posestack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(livingentity)));
         posestack.mulPose(Axis.YP.rotationDegrees(270.0F));
      } else if (isEntityUpsideDown(livingentity)) {
         posestack.translate(0.0F, livingentity.getBbHeight() + 0.1F, 0.0F);
         posestack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      }

   }

   protected float getAttackAnim(T livingentity, float f) {
      return livingentity.getAttackAnim(f);
   }

   protected float getBob(T livingentity, float f) {
      return (float)livingentity.tickCount + f;
   }

   protected float getFlipDegrees(T livingentity) {
      return 90.0F;
   }

   protected float getWhiteOverlayProgress(T livingentity, float f) {
      return 0.0F;
   }

   protected void scale(T livingentity, PoseStack posestack, float f) {
   }

   protected boolean shouldShowName(T livingentity) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(livingentity);
      float f = livingentity.isDiscrete() ? 32.0F : 64.0F;
      if (d0 >= (double)(f * f)) {
         return false;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer localplayer = minecraft.player;
         boolean flag = !livingentity.isInvisibleTo(localplayer);
         if (livingentity != localplayer) {
            Team team = livingentity.getTeam();
            Team team1 = localplayer.getTeam();
            if (team != null) {
               Team.Visibility team_visibility = team.getNameTagVisibility();
               switch (team_visibility) {
                  case ALWAYS:
                     return flag;
                  case NEVER:
                     return false;
                  case HIDE_FOR_OTHER_TEAMS:
                     return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                  case HIDE_FOR_OWN_TEAM:
                     return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                  default:
                     return true;
               }
            }
         }

         return Minecraft.renderNames() && livingentity != minecraft.getCameraEntity() && flag && !livingentity.isVehicle();
      }
   }

   public static boolean isEntityUpsideDown(LivingEntity livingentity) {
      if (livingentity instanceof Player || livingentity.hasCustomName()) {
         String s = ChatFormatting.stripFormatting(livingentity.getName().getString());
         if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
            return !(livingentity instanceof Player) || ((Player)livingentity).isModelPartShown(PlayerModelPart.CAPE);
         }
      }

      return false;
   }
}
