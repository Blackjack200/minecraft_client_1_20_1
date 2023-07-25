package net.minecraft.client.renderer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

public class ItemInHandRenderer {
   private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
   private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
   private static final float ITEM_SWING_X_POS_SCALE = -0.4F;
   private static final float ITEM_SWING_Y_POS_SCALE = 0.2F;
   private static final float ITEM_SWING_Z_POS_SCALE = -0.2F;
   private static final float ITEM_HEIGHT_SCALE = -0.6F;
   private static final float ITEM_POS_X = 0.56F;
   private static final float ITEM_POS_Y = -0.52F;
   private static final float ITEM_POS_Z = -0.72F;
   private static final float ITEM_PRESWING_ROT_Y = 45.0F;
   private static final float ITEM_SWING_X_ROT_AMOUNT = -80.0F;
   private static final float ITEM_SWING_Y_ROT_AMOUNT = -20.0F;
   private static final float ITEM_SWING_Z_ROT_AMOUNT = -20.0F;
   private static final float EAT_JIGGLE_X_ROT_AMOUNT = 10.0F;
   private static final float EAT_JIGGLE_Y_ROT_AMOUNT = 90.0F;
   private static final float EAT_JIGGLE_Z_ROT_AMOUNT = 30.0F;
   private static final float EAT_JIGGLE_X_POS_SCALE = 0.6F;
   private static final float EAT_JIGGLE_Y_POS_SCALE = -0.5F;
   private static final float EAT_JIGGLE_Z_POS_SCALE = 0.0F;
   private static final double EAT_JIGGLE_EXPONENT = 27.0D;
   private static final float EAT_EXTRA_JIGGLE_CUTOFF = 0.8F;
   private static final float EAT_EXTRA_JIGGLE_SCALE = 0.1F;
   private static final float ARM_SWING_X_POS_SCALE = -0.3F;
   private static final float ARM_SWING_Y_POS_SCALE = 0.4F;
   private static final float ARM_SWING_Z_POS_SCALE = -0.4F;
   private static final float ARM_SWING_Y_ROT_AMOUNT = 70.0F;
   private static final float ARM_SWING_Z_ROT_AMOUNT = -20.0F;
   private static final float ARM_HEIGHT_SCALE = -0.6F;
   private static final float ARM_POS_SCALE = 0.8F;
   private static final float ARM_POS_X = 0.8F;
   private static final float ARM_POS_Y = -0.75F;
   private static final float ARM_POS_Z = -0.9F;
   private static final float ARM_PRESWING_ROT_Y = 45.0F;
   private static final float ARM_PREROTATION_X_OFFSET = -1.0F;
   private static final float ARM_PREROTATION_Y_OFFSET = 3.6F;
   private static final float ARM_PREROTATION_Z_OFFSET = 3.5F;
   private static final float ARM_POSTROTATION_X_OFFSET = 5.6F;
   private static final int ARM_ROT_X = 200;
   private static final int ARM_ROT_Y = -135;
   private static final int ARM_ROT_Z = 120;
   private static final float MAP_SWING_X_POS_SCALE = -0.4F;
   private static final float MAP_SWING_Z_POS_SCALE = -0.2F;
   private static final float MAP_HANDS_POS_X = 0.0F;
   private static final float MAP_HANDS_POS_Y = 0.04F;
   private static final float MAP_HANDS_POS_Z = -0.72F;
   private static final float MAP_HANDS_HEIGHT_SCALE = -1.2F;
   private static final float MAP_HANDS_TILT_SCALE = -0.5F;
   private static final float MAP_PLAYER_PITCH_SCALE = 45.0F;
   private static final float MAP_HANDS_Z_ROT_AMOUNT = -85.0F;
   private static final float MAPHAND_X_ROT_AMOUNT = 45.0F;
   private static final float MAPHAND_Y_ROT_AMOUNT = 92.0F;
   private static final float MAPHAND_Z_ROT_AMOUNT = -41.0F;
   private static final float MAP_HAND_X_POS = 0.3F;
   private static final float MAP_HAND_Y_POS = -1.1F;
   private static final float MAP_HAND_Z_POS = 0.45F;
   private static final float MAP_SWING_X_ROT_AMOUNT = 20.0F;
   private static final float MAP_PRE_ROT_SCALE = 0.38F;
   private static final float MAP_GLOBAL_X_POS = -0.5F;
   private static final float MAP_GLOBAL_Y_POS = -0.5F;
   private static final float MAP_GLOBAL_Z_POS = 0.0F;
   private static final float MAP_FINAL_SCALE = 0.0078125F;
   private static final int MAP_BORDER = 7;
   private static final int MAP_HEIGHT = 128;
   private static final int MAP_WIDTH = 128;
   private static final float BOW_CHARGE_X_POS_SCALE = 0.0F;
   private static final float BOW_CHARGE_Y_POS_SCALE = 0.0F;
   private static final float BOW_CHARGE_Z_POS_SCALE = 0.04F;
   private static final float BOW_CHARGE_SHAKE_X_SCALE = 0.0F;
   private static final float BOW_CHARGE_SHAKE_Y_SCALE = 0.004F;
   private static final float BOW_CHARGE_SHAKE_Z_SCALE = 0.0F;
   private static final float BOW_CHARGE_Z_SCALE = 0.2F;
   private static final float BOW_MIN_SHAKE_CHARGE = 0.1F;
   private final Minecraft minecraft;
   private ItemStack mainHandItem = ItemStack.EMPTY;
   private ItemStack offHandItem = ItemStack.EMPTY;
   private float mainHandHeight;
   private float oMainHandHeight;
   private float offHandHeight;
   private float oOffHandHeight;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;

   public ItemInHandRenderer(Minecraft minecraft, EntityRenderDispatcher entityrenderdispatcher, ItemRenderer itemrenderer) {
      this.minecraft = minecraft;
      this.entityRenderDispatcher = entityrenderdispatcher;
      this.itemRenderer = itemrenderer;
   }

   public void renderItem(LivingEntity livingentity, ItemStack itemstack, ItemDisplayContext itemdisplaycontext, boolean flag, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (!itemstack.isEmpty()) {
         this.itemRenderer.renderStatic(livingentity, itemstack, itemdisplaycontext, flag, posestack, multibuffersource, livingentity.level(), i, OverlayTexture.NO_OVERLAY, livingentity.getId() + itemdisplaycontext.ordinal());
      }
   }

   private float calculateMapTilt(float f) {
      float f1 = 1.0F - f / 45.0F + 0.1F;
      f1 = Mth.clamp(f1, 0.0F, 1.0F);
      return -Mth.cos(f1 * (float)Math.PI) * 0.5F + 0.5F;
   }

   private void renderMapHand(PoseStack posestack, MultiBufferSource multibuffersource, int i, HumanoidArm humanoidarm) {
      RenderSystem.setShaderTexture(0, this.minecraft.player.getSkinTextureLocation());
      PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
      posestack.pushPose();
      float f = humanoidarm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      posestack.mulPose(Axis.YP.rotationDegrees(92.0F));
      posestack.mulPose(Axis.XP.rotationDegrees(45.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
      posestack.translate(f * 0.3F, -1.1F, 0.45F);
      if (humanoidarm == HumanoidArm.RIGHT) {
         playerrenderer.renderRightHand(posestack, multibuffersource, i, this.minecraft.player);
      } else {
         playerrenderer.renderLeftHand(posestack, multibuffersource, i, this.minecraft.player);
      }

      posestack.popPose();
   }

   private void renderOneHandedMap(PoseStack posestack, MultiBufferSource multibuffersource, int i, float f, HumanoidArm humanoidarm, float f1, ItemStack itemstack) {
      float f2 = humanoidarm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      posestack.translate(f2 * 0.125F, -0.125F, 0.0F);
      if (!this.minecraft.player.isInvisible()) {
         posestack.pushPose();
         posestack.mulPose(Axis.ZP.rotationDegrees(f2 * 10.0F));
         this.renderPlayerArm(posestack, multibuffersource, i, f, f1, humanoidarm);
         posestack.popPose();
      }

      posestack.pushPose();
      posestack.translate(f2 * 0.51F, -0.08F + f * -1.2F, -0.75F);
      float f3 = Mth.sqrt(f1);
      float f4 = Mth.sin(f3 * (float)Math.PI);
      float f5 = -0.5F * f4;
      float f6 = 0.4F * Mth.sin(f3 * ((float)Math.PI * 2F));
      float f7 = -0.3F * Mth.sin(f1 * (float)Math.PI);
      posestack.translate(f2 * f5, f6 - 0.3F * f4, f7);
      posestack.mulPose(Axis.XP.rotationDegrees(f4 * -45.0F));
      posestack.mulPose(Axis.YP.rotationDegrees(f2 * f4 * -30.0F));
      this.renderMap(posestack, multibuffersource, i, itemstack);
      posestack.popPose();
   }

   private void renderTwoHandedMap(PoseStack posestack, MultiBufferSource multibuffersource, int i, float f, float f1, float f2) {
      float f3 = Mth.sqrt(f2);
      float f4 = -0.2F * Mth.sin(f2 * (float)Math.PI);
      float f5 = -0.4F * Mth.sin(f3 * (float)Math.PI);
      posestack.translate(0.0F, -f4 / 2.0F, f5);
      float f6 = this.calculateMapTilt(f);
      posestack.translate(0.0F, 0.04F + f1 * -1.2F + f6 * -0.5F, -0.72F);
      posestack.mulPose(Axis.XP.rotationDegrees(f6 * -85.0F));
      if (!this.minecraft.player.isInvisible()) {
         posestack.pushPose();
         posestack.mulPose(Axis.YP.rotationDegrees(90.0F));
         this.renderMapHand(posestack, multibuffersource, i, HumanoidArm.RIGHT);
         this.renderMapHand(posestack, multibuffersource, i, HumanoidArm.LEFT);
         posestack.popPose();
      }

      float f7 = Mth.sin(f3 * (float)Math.PI);
      posestack.mulPose(Axis.XP.rotationDegrees(f7 * 20.0F));
      posestack.scale(2.0F, 2.0F, 2.0F);
      this.renderMap(posestack, multibuffersource, i, this.mainHandItem);
   }

   private void renderMap(PoseStack posestack, MultiBufferSource multibuffersource, int i, ItemStack itemstack) {
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      posestack.scale(0.38F, 0.38F, 0.38F);
      posestack.translate(-0.5F, -0.5F, 0.0F);
      posestack.scale(0.0078125F, 0.0078125F, 0.0078125F);
      Integer integer = MapItem.getMapId(itemstack);
      MapItemSavedData mapitemsaveddata = MapItem.getSavedData(integer, this.minecraft.level);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
      Matrix4f matrix4f = posestack.last().pose();
      vertexconsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(i).endVertex();
      vertexconsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(i).endVertex();
      if (mapitemsaveddata != null) {
         this.minecraft.gameRenderer.getMapRenderer().render(posestack, multibuffersource, integer, mapitemsaveddata, false, i);
      }

   }

   private void renderPlayerArm(PoseStack posestack, MultiBufferSource multibuffersource, int i, float f, float f1, HumanoidArm humanoidarm) {
      boolean flag = humanoidarm != HumanoidArm.LEFT;
      float f2 = flag ? 1.0F : -1.0F;
      float f3 = Mth.sqrt(f1);
      float f4 = -0.3F * Mth.sin(f3 * (float)Math.PI);
      float f5 = 0.4F * Mth.sin(f3 * ((float)Math.PI * 2F));
      float f6 = -0.4F * Mth.sin(f1 * (float)Math.PI);
      posestack.translate(f2 * (f4 + 0.64000005F), f5 + -0.6F + f * -0.6F, f6 + -0.71999997F);
      posestack.mulPose(Axis.YP.rotationDegrees(f2 * 45.0F));
      float f7 = Mth.sin(f1 * f1 * (float)Math.PI);
      float f8 = Mth.sin(f3 * (float)Math.PI);
      posestack.mulPose(Axis.YP.rotationDegrees(f2 * f8 * 70.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees(f2 * f7 * -20.0F));
      AbstractClientPlayer abstractclientplayer = this.minecraft.player;
      RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
      posestack.translate(f2 * -1.0F, 3.6F, 3.5F);
      posestack.mulPose(Axis.ZP.rotationDegrees(f2 * 120.0F));
      posestack.mulPose(Axis.XP.rotationDegrees(200.0F));
      posestack.mulPose(Axis.YP.rotationDegrees(f2 * -135.0F));
      posestack.translate(f2 * 5.6F, 0.0F, 0.0F);
      PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(abstractclientplayer);
      if (flag) {
         playerrenderer.renderRightHand(posestack, multibuffersource, i, abstractclientplayer);
      } else {
         playerrenderer.renderLeftHand(posestack, multibuffersource, i, abstractclientplayer);
      }

   }

   private void applyEatTransform(PoseStack posestack, float f, HumanoidArm humanoidarm, ItemStack itemstack) {
      float f1 = (float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F;
      float f2 = f1 / (float)itemstack.getUseDuration();
      if (f2 < 0.8F) {
         float f3 = Mth.abs(Mth.cos(f1 / 4.0F * (float)Math.PI) * 0.1F);
         posestack.translate(0.0F, f3, 0.0F);
      }

      float f4 = 1.0F - (float)Math.pow((double)f2, 27.0D);
      int i = humanoidarm == HumanoidArm.RIGHT ? 1 : -1;
      posestack.translate(f4 * 0.6F * (float)i, f4 * -0.5F, f4 * 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees((float)i * f4 * 90.0F));
      posestack.mulPose(Axis.XP.rotationDegrees(f4 * 10.0F));
      posestack.mulPose(Axis.ZP.rotationDegrees((float)i * f4 * 30.0F));
   }

   private void applyBrushTransform(PoseStack posestack, float f, HumanoidArm humanoidarm, ItemStack itemstack, float f1) {
      this.applyItemArmTransform(posestack, humanoidarm, f1);
      float f2 = (float)(this.minecraft.player.getUseItemRemainingTicks() % 10);
      float f3 = f2 - f + 1.0F;
      float f4 = 1.0F - f3 / 10.0F;
      float f5 = -90.0F;
      float f6 = 60.0F;
      float f7 = 150.0F;
      float f8 = -15.0F;
      int i = 2;
      float f9 = -15.0F + 75.0F * Mth.cos(f4 * 2.0F * (float)Math.PI);
      if (humanoidarm != HumanoidArm.RIGHT) {
         posestack.translate(0.1D, 0.83D, 0.35D);
         posestack.mulPose(Axis.XP.rotationDegrees(-80.0F));
         posestack.mulPose(Axis.YP.rotationDegrees(-90.0F));
         posestack.mulPose(Axis.XP.rotationDegrees(f9));
         posestack.translate(-0.3D, 0.22D, 0.35D);
      } else {
         posestack.translate(-0.25D, 0.22D, 0.35D);
         posestack.mulPose(Axis.XP.rotationDegrees(-80.0F));
         posestack.mulPose(Axis.YP.rotationDegrees(90.0F));
         posestack.mulPose(Axis.ZP.rotationDegrees(0.0F));
         posestack.mulPose(Axis.XP.rotationDegrees(f9));
      }

   }

   private void applyItemArmAttackTransform(PoseStack posestack, HumanoidArm humanoidarm, float f) {
      int i = humanoidarm == HumanoidArm.RIGHT ? 1 : -1;
      float f1 = Mth.sin(f * f * (float)Math.PI);
      posestack.mulPose(Axis.YP.rotationDegrees((float)i * (45.0F + f1 * -20.0F)));
      float f2 = Mth.sin(Mth.sqrt(f) * (float)Math.PI);
      posestack.mulPose(Axis.ZP.rotationDegrees((float)i * f2 * -20.0F));
      posestack.mulPose(Axis.XP.rotationDegrees(f2 * -80.0F));
      posestack.mulPose(Axis.YP.rotationDegrees((float)i * -45.0F));
   }

   private void applyItemArmTransform(PoseStack posestack, HumanoidArm humanoidarm, float f) {
      int i = humanoidarm == HumanoidArm.RIGHT ? 1 : -1;
      posestack.translate((float)i * 0.56F, -0.52F + f * -0.6F, -0.72F);
   }

   public void renderHandsWithItems(float f, PoseStack posestack, MultiBufferSource.BufferSource multibuffersource_buffersource, LocalPlayer localplayer, int i) {
      float f1 = localplayer.getAttackAnim(f);
      InteractionHand interactionhand = MoreObjects.firstNonNull(localplayer.swingingArm, InteractionHand.MAIN_HAND);
      float f2 = Mth.lerp(f, localplayer.xRotO, localplayer.getXRot());
      ItemInHandRenderer.HandRenderSelection iteminhandrenderer_handrenderselection = evaluateWhichHandsToRender(localplayer);
      float f3 = Mth.lerp(f, localplayer.xBobO, localplayer.xBob);
      float f4 = Mth.lerp(f, localplayer.yBobO, localplayer.yBob);
      posestack.mulPose(Axis.XP.rotationDegrees((localplayer.getViewXRot(f) - f3) * 0.1F));
      posestack.mulPose(Axis.YP.rotationDegrees((localplayer.getViewYRot(f) - f4) * 0.1F));
      if (iteminhandrenderer_handrenderselection.renderMainHand) {
         float f5 = interactionhand == InteractionHand.MAIN_HAND ? f1 : 0.0F;
         float f6 = 1.0F - Mth.lerp(f, this.oMainHandHeight, this.mainHandHeight);
         this.renderArmWithItem(localplayer, f, f2, InteractionHand.MAIN_HAND, f5, this.mainHandItem, f6, posestack, multibuffersource_buffersource, i);
      }

      if (iteminhandrenderer_handrenderselection.renderOffHand) {
         float f7 = interactionhand == InteractionHand.OFF_HAND ? f1 : 0.0F;
         float f8 = 1.0F - Mth.lerp(f, this.oOffHandHeight, this.offHandHeight);
         this.renderArmWithItem(localplayer, f, f2, InteractionHand.OFF_HAND, f7, this.offHandItem, f8, posestack, multibuffersource_buffersource, i);
      }

      multibuffersource_buffersource.endBatch();
   }

   @VisibleForTesting
   static ItemInHandRenderer.HandRenderSelection evaluateWhichHandsToRender(LocalPlayer localplayer) {
      ItemStack itemstack = localplayer.getMainHandItem();
      ItemStack itemstack1 = localplayer.getOffhandItem();
      boolean flag = itemstack.is(Items.BOW) || itemstack1.is(Items.BOW);
      boolean flag1 = itemstack.is(Items.CROSSBOW) || itemstack1.is(Items.CROSSBOW);
      if (!flag && !flag1) {
         return ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
      } else if (localplayer.isUsingItem()) {
         return selectionUsingItemWhileHoldingBowLike(localplayer);
      } else {
         return isChargedCrossbow(itemstack) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
      }
   }

   private static ItemInHandRenderer.HandRenderSelection selectionUsingItemWhileHoldingBowLike(LocalPlayer localplayer) {
      ItemStack itemstack = localplayer.getUseItem();
      InteractionHand interactionhand = localplayer.getUsedItemHand();
      if (!itemstack.is(Items.BOW) && !itemstack.is(Items.CROSSBOW)) {
         return interactionhand == InteractionHand.MAIN_HAND && isChargedCrossbow(localplayer.getOffhandItem()) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
      } else {
         return ItemInHandRenderer.HandRenderSelection.onlyForHand(interactionhand);
      }
   }

   private static boolean isChargedCrossbow(ItemStack itemstack) {
      return itemstack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemstack);
   }

   private void renderArmWithItem(AbstractClientPlayer abstractclientplayer, float f, float f1, InteractionHand interactionhand, float f2, ItemStack itemstack, float f3, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (!abstractclientplayer.isScoping()) {
         boolean flag = interactionhand == InteractionHand.MAIN_HAND;
         HumanoidArm humanoidarm = flag ? abstractclientplayer.getMainArm() : abstractclientplayer.getMainArm().getOpposite();
         posestack.pushPose();
         if (itemstack.isEmpty()) {
            if (flag && !abstractclientplayer.isInvisible()) {
               this.renderPlayerArm(posestack, multibuffersource, i, f3, f2, humanoidarm);
            }
         } else if (itemstack.is(Items.FILLED_MAP)) {
            if (flag && this.offHandItem.isEmpty()) {
               this.renderTwoHandedMap(posestack, multibuffersource, i, f1, f3, f2);
            } else {
               this.renderOneHandedMap(posestack, multibuffersource, i, f3, humanoidarm, f2, itemstack);
            }
         } else if (itemstack.is(Items.CROSSBOW)) {
            boolean flag1 = CrossbowItem.isCharged(itemstack);
            boolean flag2 = humanoidarm == HumanoidArm.RIGHT;
            int j = flag2 ? 1 : -1;
            if (abstractclientplayer.isUsingItem() && abstractclientplayer.getUseItemRemainingTicks() > 0 && abstractclientplayer.getUsedItemHand() == interactionhand) {
               this.applyItemArmTransform(posestack, humanoidarm, f3);
               posestack.translate((float)j * -0.4785682F, -0.094387F, 0.05731531F);
               posestack.mulPose(Axis.XP.rotationDegrees(-11.935F));
               posestack.mulPose(Axis.YP.rotationDegrees((float)j * 65.3F));
               posestack.mulPose(Axis.ZP.rotationDegrees((float)j * -9.785F));
               float f4 = (float)itemstack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
               float f5 = f4 / (float)CrossbowItem.getChargeDuration(itemstack);
               if (f5 > 1.0F) {
                  f5 = 1.0F;
               }

               if (f5 > 0.1F) {
                  float f6 = Mth.sin((f4 - 0.1F) * 1.3F);
                  float f7 = f5 - 0.1F;
                  float f8 = f6 * f7;
                  posestack.translate(f8 * 0.0F, f8 * 0.004F, f8 * 0.0F);
               }

               posestack.translate(f5 * 0.0F, f5 * 0.0F, f5 * 0.04F);
               posestack.scale(1.0F, 1.0F, 1.0F + f5 * 0.2F);
               posestack.mulPose(Axis.YN.rotationDegrees((float)j * 45.0F));
            } else {
               float f9 = -0.4F * Mth.sin(Mth.sqrt(f2) * (float)Math.PI);
               float f10 = 0.2F * Mth.sin(Mth.sqrt(f2) * ((float)Math.PI * 2F));
               float f11 = -0.2F * Mth.sin(f2 * (float)Math.PI);
               posestack.translate((float)j * f9, f10, f11);
               this.applyItemArmTransform(posestack, humanoidarm, f3);
               this.applyItemArmAttackTransform(posestack, humanoidarm, f2);
               if (flag1 && f2 < 0.001F && flag) {
                  posestack.translate((float)j * -0.641864F, 0.0F, 0.0F);
                  posestack.mulPose(Axis.YP.rotationDegrees((float)j * 10.0F));
               }
            }

            this.renderItem(abstractclientplayer, itemstack, flag2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag2, posestack, multibuffersource, i);
         } else {
            boolean flag3 = humanoidarm == HumanoidArm.RIGHT;
            if (abstractclientplayer.isUsingItem() && abstractclientplayer.getUseItemRemainingTicks() > 0 && abstractclientplayer.getUsedItemHand() == interactionhand) {
               int k = flag3 ? 1 : -1;
               switch (itemstack.getUseAnimation()) {
                  case NONE:
                     this.applyItemArmTransform(posestack, humanoidarm, f3);
                     break;
                  case EAT:
                  case DRINK:
                     this.applyEatTransform(posestack, f, humanoidarm, itemstack);
                     this.applyItemArmTransform(posestack, humanoidarm, f3);
                     break;
                  case BLOCK:
                     this.applyItemArmTransform(posestack, humanoidarm, f3);
                     break;
                  case BOW:
                     this.applyItemArmTransform(posestack, humanoidarm, f3);
                     posestack.translate((float)k * -0.2785682F, 0.18344387F, 0.15731531F);
                     posestack.mulPose(Axis.XP.rotationDegrees(-13.935F));
                     posestack.mulPose(Axis.YP.rotationDegrees((float)k * 35.3F));
                     posestack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
                     float f12 = (float)itemstack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
                     float f13 = f12 / 20.0F;
                     f13 = (f13 * f13 + f13 * 2.0F) / 3.0F;
                     if (f13 > 1.0F) {
                        f13 = 1.0F;
                     }

                     if (f13 > 0.1F) {
                        float f14 = Mth.sin((f12 - 0.1F) * 1.3F);
                        float f15 = f13 - 0.1F;
                        float f16 = f14 * f15;
                        posestack.translate(f16 * 0.0F, f16 * 0.004F, f16 * 0.0F);
                     }

                     posestack.translate(f13 * 0.0F, f13 * 0.0F, f13 * 0.04F);
                     posestack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
                     posestack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
                     break;
                  case SPEAR:
                     this.applyItemArmTransform(posestack, humanoidarm, f3);
                     posestack.translate((float)k * -0.5F, 0.7F, 0.1F);
                     posestack.mulPose(Axis.XP.rotationDegrees(-55.0F));
                     posestack.mulPose(Axis.YP.rotationDegrees((float)k * 35.3F));
                     posestack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
                     float f17 = (float)itemstack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
                     float f18 = f17 / 10.0F;
                     if (f18 > 1.0F) {
                        f18 = 1.0F;
                     }

                     if (f18 > 0.1F) {
                        float f19 = Mth.sin((f17 - 0.1F) * 1.3F);
                        float f20 = f18 - 0.1F;
                        float f21 = f19 * f20;
                        posestack.translate(f21 * 0.0F, f21 * 0.004F, f21 * 0.0F);
                     }

                     posestack.translate(0.0F, 0.0F, f18 * 0.2F);
                     posestack.scale(1.0F, 1.0F, 1.0F + f18 * 0.2F);
                     posestack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
                     break;
                  case BRUSH:
                     this.applyBrushTransform(posestack, f, humanoidarm, itemstack, f3);
               }
            } else if (abstractclientplayer.isAutoSpinAttack()) {
               this.applyItemArmTransform(posestack, humanoidarm, f3);
               int l = flag3 ? 1 : -1;
               posestack.translate((float)l * -0.4F, 0.8F, 0.3F);
               posestack.mulPose(Axis.YP.rotationDegrees((float)l * 65.0F));
               posestack.mulPose(Axis.ZP.rotationDegrees((float)l * -85.0F));
            } else {
               float f22 = -0.4F * Mth.sin(Mth.sqrt(f2) * (float)Math.PI);
               float f23 = 0.2F * Mth.sin(Mth.sqrt(f2) * ((float)Math.PI * 2F));
               float f24 = -0.2F * Mth.sin(f2 * (float)Math.PI);
               int i1 = flag3 ? 1 : -1;
               posestack.translate((float)i1 * f22, f23, f24);
               this.applyItemArmTransform(posestack, humanoidarm, f3);
               this.applyItemArmAttackTransform(posestack, humanoidarm, f2);
            }

            this.renderItem(abstractclientplayer, itemstack, flag3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag3, posestack, multibuffersource, i);
         }

         posestack.popPose();
      }
   }

   public void tick() {
      this.oMainHandHeight = this.mainHandHeight;
      this.oOffHandHeight = this.offHandHeight;
      LocalPlayer localplayer = this.minecraft.player;
      ItemStack itemstack = localplayer.getMainHandItem();
      ItemStack itemstack1 = localplayer.getOffhandItem();
      if (ItemStack.matches(this.mainHandItem, itemstack)) {
         this.mainHandItem = itemstack;
      }

      if (ItemStack.matches(this.offHandItem, itemstack1)) {
         this.offHandItem = itemstack1;
      }

      if (localplayer.isHandsBusy()) {
         this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
         this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
      } else {
         float f = localplayer.getAttackStrengthScale(1.0F);
         this.mainHandHeight += Mth.clamp((this.mainHandItem == itemstack ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
         this.offHandHeight += Mth.clamp((float)(this.offHandItem == itemstack1 ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
      }

      if (this.mainHandHeight < 0.1F) {
         this.mainHandItem = itemstack;
      }

      if (this.offHandHeight < 0.1F) {
         this.offHandItem = itemstack1;
      }

   }

   public void itemUsed(InteractionHand interactionhand) {
      if (interactionhand == InteractionHand.MAIN_HAND) {
         this.mainHandHeight = 0.0F;
      } else {
         this.offHandHeight = 0.0F;
      }

   }

   @VisibleForTesting
   static enum HandRenderSelection {
      RENDER_BOTH_HANDS(true, true),
      RENDER_MAIN_HAND_ONLY(true, false),
      RENDER_OFF_HAND_ONLY(false, true);

      final boolean renderMainHand;
      final boolean renderOffHand;

      private HandRenderSelection(boolean flag, boolean flag1) {
         this.renderMainHand = flag;
         this.renderOffHand = flag1;
      }

      public static ItemInHandRenderer.HandRenderSelection onlyForHand(InteractionHand interactionhand) {
         return interactionhand == InteractionHand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
      }
   }
}
