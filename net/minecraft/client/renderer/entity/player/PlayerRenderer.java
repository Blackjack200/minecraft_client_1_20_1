package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public PlayerRenderer(EntityRendererProvider.Context entityrendererprovider_context, boolean flag) {
      super(entityrendererprovider_context, new PlayerModel<>(entityrendererprovider_context.bakeLayer(flag ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), flag), 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel(entityrendererprovider_context.bakeLayer(flag ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel(entityrendererprovider_context.bakeLayer(flag ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)), entityrendererprovider_context.getModelManager()));
      this.addLayer(new PlayerItemInHandLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
      this.addLayer(new ArrowLayer<>(entityrendererprovider_context, this));
      this.addLayer(new Deadmau5EarsLayer(this));
      this.addLayer(new CapeLayer(this));
      this.addLayer(new CustomHeadLayer<>(this, entityrendererprovider_context.getModelSet(), entityrendererprovider_context.getItemInHandRenderer()));
      this.addLayer(new ElytraLayer<>(this, entityrendererprovider_context.getModelSet()));
      this.addLayer(new ParrotOnShoulderLayer<>(this, entityrendererprovider_context.getModelSet()));
      this.addLayer(new SpinAttackEffectLayer<>(this, entityrendererprovider_context.getModelSet()));
      this.addLayer(new BeeStingerLayer<>(this));
   }

   public void render(AbstractClientPlayer abstractclientplayer, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      this.setModelProperties(abstractclientplayer);
      super.render(abstractclientplayer, f, f1, posestack, multibuffersource, i);
   }

   public Vec3 getRenderOffset(AbstractClientPlayer abstractclientplayer, float f) {
      return abstractclientplayer.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(abstractclientplayer, f);
   }

   private void setModelProperties(AbstractClientPlayer abstractclientplayer) {
      PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
      if (abstractclientplayer.isSpectator()) {
         playermodel.setAllVisible(false);
         playermodel.head.visible = true;
         playermodel.hat.visible = true;
      } else {
         playermodel.setAllVisible(true);
         playermodel.hat.visible = abstractclientplayer.isModelPartShown(PlayerModelPart.HAT);
         playermodel.jacket.visible = abstractclientplayer.isModelPartShown(PlayerModelPart.JACKET);
         playermodel.leftPants.visible = abstractclientplayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
         playermodel.rightPants.visible = abstractclientplayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
         playermodel.leftSleeve.visible = abstractclientplayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
         playermodel.rightSleeve.visible = abstractclientplayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
         playermodel.crouching = abstractclientplayer.isCrouching();
         HumanoidModel.ArmPose humanoidmodel_armpose = getArmPose(abstractclientplayer, InteractionHand.MAIN_HAND);
         HumanoidModel.ArmPose humanoidmodel_armpose1 = getArmPose(abstractclientplayer, InteractionHand.OFF_HAND);
         if (humanoidmodel_armpose.isTwoHanded()) {
            humanoidmodel_armpose1 = abstractclientplayer.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
         }

         if (abstractclientplayer.getMainArm() == HumanoidArm.RIGHT) {
            playermodel.rightArmPose = humanoidmodel_armpose;
            playermodel.leftArmPose = humanoidmodel_armpose1;
         } else {
            playermodel.rightArmPose = humanoidmodel_armpose1;
            playermodel.leftArmPose = humanoidmodel_armpose;
         }
      }

   }

   private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer abstractclientplayer, InteractionHand interactionhand) {
      ItemStack itemstack = abstractclientplayer.getItemInHand(interactionhand);
      if (itemstack.isEmpty()) {
         return HumanoidModel.ArmPose.EMPTY;
      } else {
         if (abstractclientplayer.getUsedItemHand() == interactionhand && abstractclientplayer.getUseItemRemainingTicks() > 0) {
            UseAnim useanim = itemstack.getUseAnimation();
            if (useanim == UseAnim.BLOCK) {
               return HumanoidModel.ArmPose.BLOCK;
            }

            if (useanim == UseAnim.BOW) {
               return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            if (useanim == UseAnim.SPEAR) {
               return HumanoidModel.ArmPose.THROW_SPEAR;
            }

            if (useanim == UseAnim.CROSSBOW && interactionhand == abstractclientplayer.getUsedItemHand()) {
               return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }

            if (useanim == UseAnim.SPYGLASS) {
               return HumanoidModel.ArmPose.SPYGLASS;
            }

            if (useanim == UseAnim.TOOT_HORN) {
               return HumanoidModel.ArmPose.TOOT_HORN;
            }

            if (useanim == UseAnim.BRUSH) {
               return HumanoidModel.ArmPose.BRUSH;
            }
         } else if (!abstractclientplayer.swinging && itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
         }

         return HumanoidModel.ArmPose.ITEM;
      }
   }

   public ResourceLocation getTextureLocation(AbstractClientPlayer abstractclientplayer) {
      return abstractclientplayer.getSkinTextureLocation();
   }

   protected void scale(AbstractClientPlayer abstractclientplayer, PoseStack posestack, float f) {
      float f1 = 0.9375F;
      posestack.scale(0.9375F, 0.9375F, 0.9375F);
   }

   protected void renderNameTag(AbstractClientPlayer abstractclientplayer, Component component, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(abstractclientplayer);
      posestack.pushPose();
      if (d0 < 100.0D) {
         Scoreboard scoreboard = abstractclientplayer.getScoreboard();
         Objective objective = scoreboard.getDisplayObjective(2);
         if (objective != null) {
            Score score = scoreboard.getOrCreatePlayerScore(abstractclientplayer.getScoreboardName(), objective);
            super.renderNameTag(abstractclientplayer, Component.literal(Integer.toString(score.getScore())).append(CommonComponents.SPACE).append(objective.getDisplayName()), posestack, multibuffersource, i);
            posestack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
         }
      }

      super.renderNameTag(abstractclientplayer, component, posestack, multibuffersource, i);
      posestack.popPose();
   }

   public void renderRightHand(PoseStack posestack, MultiBufferSource multibuffersource, int i, AbstractClientPlayer abstractclientplayer) {
      this.renderHand(posestack, multibuffersource, i, abstractclientplayer, (this.model).rightArm, (this.model).rightSleeve);
   }

   public void renderLeftHand(PoseStack posestack, MultiBufferSource multibuffersource, int i, AbstractClientPlayer abstractclientplayer) {
      this.renderHand(posestack, multibuffersource, i, abstractclientplayer, (this.model).leftArm, (this.model).leftSleeve);
   }

   private void renderHand(PoseStack posestack, MultiBufferSource multibuffersource, int i, AbstractClientPlayer abstractclientplayer, ModelPart modelpart, ModelPart modelpart1) {
      PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
      this.setModelProperties(abstractclientplayer);
      playermodel.attackTime = 0.0F;
      playermodel.crouching = false;
      playermodel.swimAmount = 0.0F;
      playermodel.setupAnim(abstractclientplayer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      modelpart.xRot = 0.0F;
      modelpart.render(posestack, multibuffersource.getBuffer(RenderType.entitySolid(abstractclientplayer.getSkinTextureLocation())), i, OverlayTexture.NO_OVERLAY);
      modelpart1.xRot = 0.0F;
      modelpart1.render(posestack, multibuffersource.getBuffer(RenderType.entityTranslucent(abstractclientplayer.getSkinTextureLocation())), i, OverlayTexture.NO_OVERLAY);
   }

   protected void setupRotations(AbstractClientPlayer abstractclientplayer, PoseStack posestack, float f, float f1, float f2) {
      float f3 = abstractclientplayer.getSwimAmount(f2);
      if (abstractclientplayer.isFallFlying()) {
         super.setupRotations(abstractclientplayer, posestack, f, f1, f2);
         float f4 = (float)abstractclientplayer.getFallFlyingTicks() + f2;
         float f5 = Mth.clamp(f4 * f4 / 100.0F, 0.0F, 1.0F);
         if (!abstractclientplayer.isAutoSpinAttack()) {
            posestack.mulPose(Axis.XP.rotationDegrees(f5 * (-90.0F - abstractclientplayer.getXRot())));
         }

         Vec3 vec3 = abstractclientplayer.getViewVector(f2);
         Vec3 vec31 = abstractclientplayer.getDeltaMovementLerped(f2);
         double d0 = vec31.horizontalDistanceSqr();
         double d1 = vec3.horizontalDistanceSqr();
         if (d0 > 0.0D && d1 > 0.0D) {
            double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
            double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
            posestack.mulPose(Axis.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
         }
      } else if (f3 > 0.0F) {
         super.setupRotations(abstractclientplayer, posestack, f, f1, f2);
         float f6 = abstractclientplayer.isInWater() ? -90.0F - abstractclientplayer.getXRot() : -90.0F;
         float f7 = Mth.lerp(f3, 0.0F, f6);
         posestack.mulPose(Axis.XP.rotationDegrees(f7));
         if (abstractclientplayer.isVisuallySwimming()) {
            posestack.translate(0.0F, -1.0F, 0.3F);
         }
      } else {
         super.setupRotations(abstractclientplayer, posestack, f, f1, f2);
      }

   }
}
