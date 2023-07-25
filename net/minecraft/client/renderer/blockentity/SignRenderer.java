package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
   private static final String STICK = "stick";
   private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
   private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
   private static final float RENDER_SCALE = 0.6666667F;
   private static final Vec3 TEXT_OFFSET = new Vec3(0.0D, (double)0.33333334F, (double)0.046666667F);
   private final Map<WoodType, SignRenderer.SignModel> signModels;
   private final Font font;

   public SignRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.signModels = WoodType.values().collect(ImmutableMap.toImmutableMap((woodtype1) -> woodtype1, (woodtype) -> new SignRenderer.SignModel(blockentityrendererprovider_context.bakeLayer(ModelLayers.createSignModelName(woodtype)))));
      this.font = blockentityrendererprovider_context.getFont();
   }

   public void render(SignBlockEntity signblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      BlockState blockstate = signblockentity.getBlockState();
      SignBlock signblock = (SignBlock)blockstate.getBlock();
      WoodType woodtype = SignBlock.getWoodType(signblock);
      SignRenderer.SignModel signrenderer_signmodel = this.signModels.get(woodtype);
      signrenderer_signmodel.stick.visible = blockstate.getBlock() instanceof StandingSignBlock;
      this.renderSignWithText(signblockentity, posestack, multibuffersource, i, j, blockstate, signblock, woodtype, signrenderer_signmodel);
   }

   public float getSignModelRenderScale() {
      return 0.6666667F;
   }

   public float getSignTextRenderScale() {
      return 0.6666667F;
   }

   void renderSignWithText(SignBlockEntity signblockentity, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, BlockState blockstate, SignBlock signblock, WoodType woodtype, Model model) {
      posestack.pushPose();
      this.translateSign(posestack, -signblock.getYRotationDegrees(blockstate), blockstate);
      this.renderSign(posestack, multibuffersource, i, j, woodtype, model);
      this.renderSignText(signblockentity.getBlockPos(), signblockentity.getFrontText(), posestack, multibuffersource, i, signblockentity.getTextLineHeight(), signblockentity.getMaxTextLineWidth(), true);
      this.renderSignText(signblockentity.getBlockPos(), signblockentity.getBackText(), posestack, multibuffersource, i, signblockentity.getTextLineHeight(), signblockentity.getMaxTextLineWidth(), false);
      posestack.popPose();
   }

   void translateSign(PoseStack posestack, float f, BlockState blockstate) {
      posestack.translate(0.5F, 0.75F * this.getSignModelRenderScale(), 0.5F);
      posestack.mulPose(Axis.YP.rotationDegrees(f));
      if (!(blockstate.getBlock() instanceof StandingSignBlock)) {
         posestack.translate(0.0F, -0.3125F, -0.4375F);
      }

   }

   void renderSign(PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, WoodType woodtype, Model model) {
      posestack.pushPose();
      float f = this.getSignModelRenderScale();
      posestack.scale(f, -f, -f);
      Material material = this.getSignMaterial(woodtype);
      VertexConsumer vertexconsumer = material.buffer(multibuffersource, model::renderType);
      this.renderSignModel(posestack, i, j, model, vertexconsumer);
      posestack.popPose();
   }

   void renderSignModel(PoseStack posestack, int i, int j, Model model, VertexConsumer vertexconsumer) {
      SignRenderer.SignModel signrenderer_signmodel = (SignRenderer.SignModel)model;
      signrenderer_signmodel.root.render(posestack, vertexconsumer, i, j);
   }

   Material getSignMaterial(WoodType woodtype) {
      return Sheets.getSignMaterial(woodtype);
   }

   void renderSignText(BlockPos blockpos, SignText signtext, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, int k, boolean flag) {
      posestack.pushPose();
      this.translateSignText(posestack, flag, this.getTextOffset());
      int l = getDarkColor(signtext);
      int i1 = 4 * j / 2;
      FormattedCharSequence[] aformattedcharsequence = signtext.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (component) -> {
         List<FormattedCharSequence> list = this.font.split(component, k);
         return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
      });
      int j1;
      boolean flag1;
      int k1;
      if (signtext.hasGlowingText()) {
         j1 = signtext.getColor().getTextColor();
         flag1 = isOutlineVisible(blockpos, j1);
         k1 = 15728880;
      } else {
         j1 = l;
         flag1 = false;
         k1 = i;
      }

      for(int j2 = 0; j2 < 4; ++j2) {
         FormattedCharSequence formattedcharsequence = aformattedcharsequence[j2];
         float f = (float)(-this.font.width(formattedcharsequence) / 2);
         if (flag1) {
            this.font.drawInBatch8xOutline(formattedcharsequence, f, (float)(j2 * j - i1), j1, l, posestack.last().pose(), multibuffersource, k1);
         } else {
            this.font.drawInBatch(formattedcharsequence, f, (float)(j2 * j - i1), j1, false, posestack.last().pose(), multibuffersource, Font.DisplayMode.POLYGON_OFFSET, 0, k1);
         }
      }

      posestack.popPose();
   }

   private void translateSignText(PoseStack posestack, boolean flag, Vec3 vec3) {
      if (!flag) {
         posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
      }

      float f = 0.015625F * this.getSignTextRenderScale();
      posestack.translate(vec3.x, vec3.y, vec3.z);
      posestack.scale(f, -f, f);
   }

   Vec3 getTextOffset() {
      return TEXT_OFFSET;
   }

   static boolean isOutlineVisible(BlockPos blockpos, int i) {
      if (i == DyeColor.BLACK.getTextColor()) {
         return true;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer localplayer = minecraft.player;
         if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
            return true;
         } else {
            Entity entity = minecraft.getCameraEntity();
            return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockpos)) < (double)OUTLINE_RENDER_DISTANCE;
         }
      }
   }

   static int getDarkColor(SignText signtext) {
      int i = signtext.getColor().getTextColor();
      if (i == DyeColor.BLACK.getTextColor() && signtext.hasGlowingText()) {
         return -988212;
      } else {
         double d0 = 0.4D;
         int j = (int)((double)FastColor.ARGB32.red(i) * 0.4D);
         int k = (int)((double)FastColor.ARGB32.green(i) * 0.4D);
         int l = (int)((double)FastColor.ARGB32.blue(i) * 0.4D);
         return FastColor.ARGB32.color(0, j, k, l);
      }
   }

   public static SignRenderer.SignModel createSignModel(EntityModelSet entitymodelset, WoodType woodtype) {
      return new SignRenderer.SignModel(entitymodelset.bakeLayer(ModelLayers.createSignModelName(woodtype)));
   }

   public static LayerDefinition createSignLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public static final class SignModel extends Model {
      public final ModelPart root;
      public final ModelPart stick;

      public SignModel(ModelPart modelpart) {
         super(RenderType::entityCutoutNoCull);
         this.root = modelpart;
         this.stick = modelpart.getChild("stick");
      }

      public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
         this.root.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
      }
   }
}
