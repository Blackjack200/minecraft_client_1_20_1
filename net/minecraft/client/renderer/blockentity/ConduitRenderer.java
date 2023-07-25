package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ConduitRenderer implements BlockEntityRenderer<ConduitBlockEntity> {
   public static final Material SHELL_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/base"));
   public static final Material ACTIVE_SHELL_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/cage"));
   public static final Material WIND_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/wind"));
   public static final Material VERTICAL_WIND_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/wind_vertical"));
   public static final Material OPEN_EYE_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/open_eye"));
   public static final Material CLOSED_EYE_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/closed_eye"));
   private final ModelPart eye;
   private final ModelPart wind;
   private final ModelPart shell;
   private final ModelPart cage;
   private final BlockEntityRenderDispatcher renderer;

   public ConduitRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.renderer = blockentityrendererprovider_context.getBlockEntityRenderDispatcher();
      this.eye = blockentityrendererprovider_context.bakeLayer(ModelLayers.CONDUIT_EYE);
      this.wind = blockentityrendererprovider_context.bakeLayer(ModelLayers.CONDUIT_WIND);
      this.shell = blockentityrendererprovider_context.bakeLayer(ModelLayers.CONDUIT_SHELL);
      this.cage = blockentityrendererprovider_context.bakeLayer(ModelLayers.CONDUIT_CAGE);
   }

   public static LayerDefinition createEyeLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 16, 16);
   }

   public static LayerDefinition createWindLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("wind", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public static LayerDefinition createShellLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 32, 16);
   }

   public static LayerDefinition createCageLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 32, 16);
   }

   public void render(ConduitBlockEntity conduitblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      float f1 = (float)conduitblockentity.tickCount + f;
      if (!conduitblockentity.isActive()) {
         float f2 = conduitblockentity.getActiveRotation(0.0F);
         VertexConsumer vertexconsumer = SHELL_TEXTURE.buffer(multibuffersource, RenderType::entitySolid);
         posestack.pushPose();
         posestack.translate(0.5F, 0.5F, 0.5F);
         posestack.mulPose((new Quaternionf()).rotationY(f2 * ((float)Math.PI / 180F)));
         this.shell.render(posestack, vertexconsumer, i, j);
         posestack.popPose();
      } else {
         float f3 = conduitblockentity.getActiveRotation(f) * (180F / (float)Math.PI);
         float f4 = Mth.sin(f1 * 0.1F) / 2.0F + 0.5F;
         f4 = f4 * f4 + f4;
         posestack.pushPose();
         posestack.translate(0.5F, 0.3F + f4 * 0.2F, 0.5F);
         Vector3f vector3f = (new Vector3f(0.5F, 1.0F, 0.5F)).normalize();
         posestack.mulPose((new Quaternionf()).rotationAxis(f3 * ((float)Math.PI / 180F), vector3f));
         this.cage.render(posestack, ACTIVE_SHELL_TEXTURE.buffer(multibuffersource, RenderType::entityCutoutNoCull), i, j);
         posestack.popPose();
         int k = conduitblockentity.tickCount / 66 % 3;
         posestack.pushPose();
         posestack.translate(0.5F, 0.5F, 0.5F);
         if (k == 1) {
            posestack.mulPose((new Quaternionf()).rotationX(((float)Math.PI / 2F)));
         } else if (k == 2) {
            posestack.mulPose((new Quaternionf()).rotationZ(((float)Math.PI / 2F)));
         }

         VertexConsumer vertexconsumer1 = (k == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE).buffer(multibuffersource, RenderType::entityCutoutNoCull);
         this.wind.render(posestack, vertexconsumer1, i, j);
         posestack.popPose();
         posestack.pushPose();
         posestack.translate(0.5F, 0.5F, 0.5F);
         posestack.scale(0.875F, 0.875F, 0.875F);
         posestack.mulPose((new Quaternionf()).rotationXYZ((float)Math.PI, 0.0F, (float)Math.PI));
         this.wind.render(posestack, vertexconsumer1, i, j);
         posestack.popPose();
         Camera camera = this.renderer.camera;
         posestack.pushPose();
         posestack.translate(0.5F, 0.3F + f4 * 0.2F, 0.5F);
         posestack.scale(0.5F, 0.5F, 0.5F);
         float f5 = -camera.getYRot();
         posestack.mulPose((new Quaternionf()).rotationYXZ(f5 * ((float)Math.PI / 180F), camera.getXRot() * ((float)Math.PI / 180F), (float)Math.PI));
         float f6 = 1.3333334F;
         posestack.scale(1.3333334F, 1.3333334F, 1.3333334F);
         this.eye.render(posestack, (conduitblockentity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).buffer(multibuffersource, RenderType::entityCutoutNoCull), i, j);
         posestack.popPose();
      }
   }
}
