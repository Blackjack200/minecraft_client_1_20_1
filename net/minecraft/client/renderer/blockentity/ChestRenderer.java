package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Calendar;
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
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
   private static final String BOTTOM = "bottom";
   private static final String LID = "lid";
   private static final String LOCK = "lock";
   private final ModelPart lid;
   private final ModelPart bottom;
   private final ModelPart lock;
   private final ModelPart doubleLeftLid;
   private final ModelPart doubleLeftBottom;
   private final ModelPart doubleLeftLock;
   private final ModelPart doubleRightLid;
   private final ModelPart doubleRightBottom;
   private final ModelPart doubleRightLock;
   private boolean xmasTextures;

   public ChestRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      Calendar calendar = Calendar.getInstance();
      if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
         this.xmasTextures = true;
      }

      ModelPart modelpart = blockentityrendererprovider_context.bakeLayer(ModelLayers.CHEST);
      this.bottom = modelpart.getChild("bottom");
      this.lid = modelpart.getChild("lid");
      this.lock = modelpart.getChild("lock");
      ModelPart modelpart1 = blockentityrendererprovider_context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
      this.doubleLeftBottom = modelpart1.getChild("bottom");
      this.doubleLeftLid = modelpart1.getChild("lid");
      this.doubleLeftLock = modelpart1.getChild("lock");
      ModelPart modelpart2 = blockentityrendererprovider_context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
      this.doubleRightBottom = modelpart2.getChild("bottom");
      this.doubleRightLid = modelpart2.getChild("lid");
      this.doubleRightLock = modelpart2.getChild("lock");
   }

   public static LayerDefinition createSingleBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public static LayerDefinition createDoubleBodyRightLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(15.0F, -2.0F, 14.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public static LayerDefinition createDoubleBodyLeftLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 1.0F, 15.0F, 10.0F, 14.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 15.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      partdefinition.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, 14.0F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 9.0F, 1.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void render(T blockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      Level level = blockentity.getLevel();
      boolean flag = level != null;
      BlockState blockstate = flag ? blockentity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
      ChestType chesttype = blockstate.hasProperty(ChestBlock.TYPE) ? blockstate.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
      Block block = blockstate.getBlock();
      if (block instanceof AbstractChestBlock<?> abstractchestblock) {
         boolean flag1 = chesttype != ChestType.SINGLE;
         posestack.pushPose();
         float f1 = blockstate.getValue(ChestBlock.FACING).toYRot();
         posestack.translate(0.5F, 0.5F, 0.5F);
         posestack.mulPose(Axis.YP.rotationDegrees(-f1));
         posestack.translate(-0.5F, -0.5F, -0.5F);
         DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> doubleblockcombiner_neighborcombineresult;
         if (flag) {
            doubleblockcombiner_neighborcombineresult = abstractchestblock.combine(blockstate, level, blockentity.getBlockPos(), true);
         } else {
            doubleblockcombiner_neighborcombineresult = DoubleBlockCombiner.Combiner::acceptNone;
         }

         float f2 = doubleblockcombiner_neighborcombineresult.<Float2FloatFunction>apply(ChestBlock.opennessCombiner(blockentity)).get(f);
         f2 = 1.0F - f2;
         f2 = 1.0F - f2 * f2 * f2;
         int k = doubleblockcombiner_neighborcombineresult.<Int2IntFunction>apply(new BrightnessCombiner<>()).applyAsInt(i);
         Material material = Sheets.chooseMaterial(blockentity, chesttype, this.xmasTextures);
         VertexConsumer vertexconsumer = material.buffer(multibuffersource, RenderType::entityCutout);
         if (flag1) {
            if (chesttype == ChestType.LEFT) {
               this.render(posestack, vertexconsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, f2, k, j);
            } else {
               this.render(posestack, vertexconsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, f2, k, j);
            }
         } else {
            this.render(posestack, vertexconsumer, this.lid, this.lock, this.bottom, f2, k, j);
         }

         posestack.popPose();
      }
   }

   private void render(PoseStack posestack, VertexConsumer vertexconsumer, ModelPart modelpart, ModelPart modelpart1, ModelPart modelpart2, float f, int i, int j) {
      modelpart.xRot = -(f * ((float)Math.PI / 2F));
      modelpart1.xRot = modelpart.xRot;
      modelpart.render(posestack, vertexconsumer, i, j);
      modelpart1.render(posestack, vertexconsumer, i, j);
      modelpart2.render(posestack, vertexconsumer, i, j);
   }
}
