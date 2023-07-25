package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ChestRaftModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

public class BoatRenderer extends EntityRenderer<Boat> {
   private final Map<Boat.Type, Pair<ResourceLocation, ListModel<Boat>>> boatResources;

   public BoatRenderer(EntityRendererProvider.Context entityrendererprovider_context, boolean flag) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.8F;
      this.boatResources = Stream.of(Boat.Type.values()).collect(ImmutableMap.toImmutableMap((boat_type1) -> boat_type1, (boat_type) -> Pair.of(new ResourceLocation(getTextureLocation(boat_type, flag)), this.createBoatModel(entityrendererprovider_context, boat_type, flag))));
   }

   private ListModel<Boat> createBoatModel(EntityRendererProvider.Context entityrendererprovider_context, Boat.Type boat_type, boolean flag) {
      ModelLayerLocation modellayerlocation = flag ? ModelLayers.createChestBoatModelName(boat_type) : ModelLayers.createBoatModelName(boat_type);
      ModelPart modelpart = entityrendererprovider_context.bakeLayer(modellayerlocation);
      if (boat_type == Boat.Type.BAMBOO) {
         return (ListModel<Boat>)(flag ? new ChestRaftModel(modelpart) : new RaftModel(modelpart));
      } else {
         return (ListModel<Boat>)(flag ? new ChestBoatModel(modelpart) : new BoatModel(modelpart));
      }
   }

   private static String getTextureLocation(Boat.Type boat_type, boolean flag) {
      return flag ? "textures/entity/chest_boat/" + boat_type.getName() + ".png" : "textures/entity/boat/" + boat_type.getName() + ".png";
   }

   public void render(Boat boat, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.translate(0.0F, 0.375F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
      float f2 = (float)boat.getHurtTime() - f1;
      float f3 = boat.getDamage() - f1;
      if (f3 < 0.0F) {
         f3 = 0.0F;
      }

      if (f2 > 0.0F) {
         posestack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f2) * f2 * f3 / 10.0F * (float)boat.getHurtDir()));
      }

      float f4 = boat.getBubbleAngle(f1);
      if (!Mth.equal(f4, 0.0F)) {
         posestack.mulPose((new Quaternionf()).setAngleAxis(boat.getBubbleAngle(f1) * ((float)Math.PI / 180F), 1.0F, 0.0F, 1.0F));
      }

      Pair<ResourceLocation, ListModel<Boat>> pair = this.boatResources.get(boat.getVariant());
      ResourceLocation resourcelocation = pair.getFirst();
      ListModel<Boat> listmodel = pair.getSecond();
      posestack.scale(-1.0F, -1.0F, 1.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(90.0F));
      listmodel.setupAnim(boat, f1, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(listmodel.renderType(resourcelocation));
      listmodel.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      if (!boat.isUnderWater()) {
         VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(RenderType.waterMask());
         if (listmodel instanceof WaterPatchModel) {
            WaterPatchModel waterpatchmodel = (WaterPatchModel)listmodel;
            waterpatchmodel.waterPatch().render(posestack, vertexconsumer1, i, OverlayTexture.NO_OVERLAY);
         }
      }

      posestack.popPose();
      super.render(boat, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(Boat boat) {
      return this.boatResources.get(boat.getVariant()).getFirst();
   }
}
