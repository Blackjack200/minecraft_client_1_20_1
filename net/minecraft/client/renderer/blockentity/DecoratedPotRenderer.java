package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
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
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;

public class DecoratedPotRenderer implements BlockEntityRenderer<DecoratedPotBlockEntity> {
   private static final String NECK = "neck";
   private static final String FRONT = "front";
   private static final String BACK = "back";
   private static final String LEFT = "left";
   private static final String RIGHT = "right";
   private static final String TOP = "top";
   private static final String BOTTOM = "bottom";
   private final ModelPart neck;
   private final ModelPart frontSide;
   private final ModelPart backSide;
   private final ModelPart leftSide;
   private final ModelPart rightSide;
   private final ModelPart top;
   private final ModelPart bottom;
   private final Material baseMaterial = Objects.requireNonNull(Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.BASE));

   public DecoratedPotRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      ModelPart modelpart = blockentityrendererprovider_context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
      this.neck = modelpart.getChild("neck");
      this.top = modelpart.getChild("top");
      this.bottom = modelpart.getChild("bottom");
      ModelPart modelpart1 = blockentityrendererprovider_context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
      this.frontSide = modelpart1.getChild("front");
      this.backSide = modelpart1.getChild("back");
      this.leftSide = modelpart1.getChild("left");
      this.rightSide = modelpart1.getChild("right");
   }

   public static LayerDefinition createBaseLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      CubeDeformation cubedeformation = new CubeDeformation(0.2F);
      CubeDeformation cubedeformation1 = new CubeDeformation(-0.1F);
      partdefinition.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, cubedeformation1).texOffs(0, 5).addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, cubedeformation), PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float)Math.PI, 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
      partdefinition.addOrReplaceChild("top", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("bottom", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public static LayerDefinition createSidesLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
      partdefinition.addOrReplaceChild("back", cubelistbuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float)Math.PI));
      partdefinition.addOrReplaceChild("left", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (-(float)Math.PI / 2F), (float)Math.PI));
      partdefinition.addOrReplaceChild("right", cubelistbuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, ((float)Math.PI / 2F), (float)Math.PI));
      partdefinition.addOrReplaceChild("front", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float)Math.PI, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 16, 16);
   }

   @Nullable
   private static Material getMaterial(Item item) {
      Material material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getResourceKey(item));
      if (material == null) {
         material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getResourceKey(Items.BRICK));
      }

      return material;
   }

   public void render(DecoratedPotBlockEntity decoratedpotblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      posestack.pushPose();
      Direction direction = decoratedpotblockentity.getDirection();
      posestack.translate(0.5D, 0.0D, 0.5D);
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
      posestack.translate(-0.5D, 0.0D, -0.5D);
      VertexConsumer vertexconsumer = this.baseMaterial.buffer(multibuffersource, RenderType::entitySolid);
      this.neck.render(posestack, vertexconsumer, i, j);
      this.top.render(posestack, vertexconsumer, i, j);
      this.bottom.render(posestack, vertexconsumer, i, j);
      DecoratedPotBlockEntity.Decorations decoratedpotblockentity_decorations = decoratedpotblockentity.getDecorations();
      this.renderSide(this.frontSide, posestack, multibuffersource, i, j, getMaterial(decoratedpotblockentity_decorations.front()));
      this.renderSide(this.backSide, posestack, multibuffersource, i, j, getMaterial(decoratedpotblockentity_decorations.back()));
      this.renderSide(this.leftSide, posestack, multibuffersource, i, j, getMaterial(decoratedpotblockentity_decorations.left()));
      this.renderSide(this.rightSide, posestack, multibuffersource, i, j, getMaterial(decoratedpotblockentity_decorations.right()));
      posestack.popPose();
   }

   private void renderSide(ModelPart modelpart, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j, @Nullable Material material) {
      if (material == null) {
         material = getMaterial(Items.BRICK);
      }

      if (material != null) {
         modelpart.render(posestack, material.buffer(multibuffersource, RenderType::entitySolid), i, j);
      }

   }
}
