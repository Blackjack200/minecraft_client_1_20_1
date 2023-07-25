package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
   private static final int BANNER_WIDTH = 20;
   private static final int BANNER_HEIGHT = 40;
   private static final int MAX_PATTERNS = 16;
   public static final String FLAG = "flag";
   private static final String POLE = "pole";
   private static final String BAR = "bar";
   private final ModelPart flag;
   private final ModelPart pole;
   private final ModelPart bar;

   public BannerRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      ModelPart modelpart = blockentityrendererprovider_context.bakeLayer(ModelLayers.BANNER);
      this.flag = modelpart.getChild("flag");
      this.pole = modelpart.getChild("pole");
      this.bar = modelpart.getChild("bar");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("bar", CubeListBuilder.create().texOffs(0, 42).addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void render(BannerBlockEntity bannerblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      List<Pair<Holder<BannerPattern>, DyeColor>> list = bannerblockentity.getPatterns();
      float f1 = 0.6666667F;
      boolean flag = bannerblockentity.getLevel() == null;
      posestack.pushPose();
      long k;
      if (flag) {
         k = 0L;
         posestack.translate(0.5F, 0.5F, 0.5F);
         this.pole.visible = true;
      } else {
         k = bannerblockentity.getLevel().getGameTime();
         BlockState blockstate = bannerblockentity.getBlockState();
         if (blockstate.getBlock() instanceof BannerBlock) {
            posestack.translate(0.5F, 0.5F, 0.5F);
            float f2 = -RotationSegment.convertToDegrees(blockstate.getValue(BannerBlock.ROTATION));
            posestack.mulPose(Axis.YP.rotationDegrees(f2));
            this.pole.visible = true;
         } else {
            posestack.translate(0.5F, -0.16666667F, 0.5F);
            float f3 = -blockstate.getValue(WallBannerBlock.FACING).toYRot();
            posestack.mulPose(Axis.YP.rotationDegrees(f3));
            posestack.translate(0.0F, -0.3125F, -0.4375F);
            this.pole.visible = false;
         }
      }

      posestack.pushPose();
      posestack.scale(0.6666667F, -0.6666667F, -0.6666667F);
      VertexConsumer vertexconsumer = ModelBakery.BANNER_BASE.buffer(multibuffersource, RenderType::entitySolid);
      this.pole.render(posestack, vertexconsumer, i, j);
      this.bar.render(posestack, vertexconsumer, i, j);
      BlockPos blockpos = bannerblockentity.getBlockPos();
      float f4 = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + k, 100L) + f) / 100.0F;
      this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(((float)Math.PI * 2F) * f4)) * (float)Math.PI;
      this.flag.y = -32.0F;
      renderPatterns(posestack, multibuffersource, i, j, this.flag, ModelBakery.BANNER_BASE, true, list);
      posestack.popPose();
      posestack.popPose();
   }

   public static void renderPatterns(PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, ModelPart modelpart, Material material, boolean flag, List<Pair<Holder<BannerPattern>, DyeColor>> list) {
      renderPatterns(posestack, multibuffersource, i, j, modelpart, material, flag, list, false);
   }

   public static void renderPatterns(PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, ModelPart modelpart, Material material, boolean flag, List<Pair<Holder<BannerPattern>, DyeColor>> list, boolean flag1) {
      modelpart.render(posestack, material.buffer(multibuffersource, RenderType::entitySolid, flag1), i, j);

      for(int k = 0; k < 17 && k < list.size(); ++k) {
         Pair<Holder<BannerPattern>, DyeColor> pair = list.get(k);
         float[] afloat = pair.getSecond().getTextureDiffuseColors();
         pair.getFirst().unwrapKey().map((resourcekey) -> flag ? Sheets.getBannerMaterial(resourcekey) : Sheets.getShieldMaterial(resourcekey)).ifPresent((material1) -> modelpart.render(posestack, material1.buffer(multibuffersource, RenderType::entityNoOutline), i, j, afloat[0], afloat[1], afloat[2], 1.0F));
      }

   }
}
