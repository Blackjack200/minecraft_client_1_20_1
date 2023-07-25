package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class BedRenderer implements BlockEntityRenderer<BedBlockEntity> {
   private final ModelPart headRoot;
   private final ModelPart footRoot;

   public BedRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.headRoot = blockentityrendererprovider_context.bakeLayer(ModelLayers.BED_HEAD);
      this.footRoot = blockentityrendererprovider_context.bakeLayer(ModelLayers.BED_FOOT);
   }

   public static LayerDefinition createHeadLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 6).addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float)Math.PI / 2F), 0.0F, ((float)Math.PI / 2F)));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 18).addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float)Math.PI / 2F), 0.0F, (float)Math.PI));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public static LayerDefinition createFootLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 0).addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 12).addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), PartPose.rotation(((float)Math.PI / 2F), 0.0F, ((float)Math.PI * 1.5F)));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void render(BedBlockEntity bedblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      Material material = Sheets.BED_TEXTURES[bedblockentity.getColor().getId()];
      Level level = bedblockentity.getLevel();
      if (level != null) {
         BlockState blockstate = bedblockentity.getBlockState();
         DoubleBlockCombiner.NeighborCombineResult<? extends BedBlockEntity> doubleblockcombiner_neighborcombineresult = DoubleBlockCombiner.combineWithNeigbour(BlockEntityType.BED, BedBlock::getBlockType, BedBlock::getConnectedDirection, ChestBlock.FACING, blockstate, level, bedblockentity.getBlockPos(), (levelaccessor, blockpos) -> false);
         int k = doubleblockcombiner_neighborcombineresult.<Int2IntFunction>apply(new BrightnessCombiner<>()).get(i);
         this.renderPiece(posestack, multibuffersource, blockstate.getValue(BedBlock.PART) == BedPart.HEAD ? this.headRoot : this.footRoot, blockstate.getValue(BedBlock.FACING), material, k, j, false);
      } else {
         this.renderPiece(posestack, multibuffersource, this.headRoot, Direction.SOUTH, material, i, j, false);
         this.renderPiece(posestack, multibuffersource, this.footRoot, Direction.SOUTH, material, i, j, true);
      }

   }

   private void renderPiece(PoseStack posestack, MultiBufferSource multibuffersource, ModelPart modelpart, Direction direction, Material material, int i, int j, boolean flag) {
      posestack.pushPose();
      posestack.translate(0.0F, 0.5625F, flag ? -1.0F : 0.0F);
      posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
      posestack.translate(0.5F, 0.5F, 0.5F);
      posestack.mulPose(Axis.ZP.rotationDegrees(180.0F + direction.toYRot()));
      posestack.translate(-0.5F, -0.5F, -0.5F);
      VertexConsumer vertexconsumer = material.buffer(multibuffersource, RenderType::entitySolid);
      modelpart.render(posestack, vertexconsumer, i, j);
      posestack.popPose();
   }
}
