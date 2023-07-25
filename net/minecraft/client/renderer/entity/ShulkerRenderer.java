package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerRenderer extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
   private static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/" + Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().getPath() + ".png");
   private static final ResourceLocation[] TEXTURE_LOCATION = Sheets.SHULKER_TEXTURE_LOCATION.stream().map((material) -> new ResourceLocation("textures/" + material.texture().getPath() + ".png")).toArray((i) -> new ResourceLocation[i]);

   public ShulkerRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new ShulkerModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.SHULKER)), 0.0F);
      this.addLayer(new ShulkerHeadLayer(this));
   }

   public Vec3 getRenderOffset(Shulker shulker, float f) {
      return shulker.getRenderPosition(f).orElse(super.getRenderOffset(shulker, f));
   }

   public boolean shouldRender(Shulker shulker, Frustum frustum, double d0, double d1, double d2) {
      return super.shouldRender(shulker, frustum, d0, d1, d2) ? true : shulker.getRenderPosition(0.0F).filter((vec3) -> {
         EntityType<?> entitytype = shulker.getType();
         float f = entitytype.getHeight() / 2.0F;
         float f1 = entitytype.getWidth() / 2.0F;
         Vec3 vec31 = Vec3.atBottomCenterOf(shulker.blockPosition());
         return frustum.isVisible((new AABB(vec3.x, vec3.y + (double)f, vec3.z, vec31.x, vec31.y + (double)f, vec31.z)).inflate((double)f1, (double)f, (double)f1));
      }).isPresent();
   }

   public ResourceLocation getTextureLocation(Shulker shulker) {
      return getTextureLocation(shulker.getColor());
   }

   public static ResourceLocation getTextureLocation(@Nullable DyeColor dyecolor) {
      return dyecolor == null ? DEFAULT_TEXTURE_LOCATION : TEXTURE_LOCATION[dyecolor.getId()];
   }

   protected void setupRotations(Shulker shulker, PoseStack posestack, float f, float f1, float f2) {
      super.setupRotations(shulker, posestack, f, f1 + 180.0F, f2);
      posestack.translate(0.0D, 0.5D, 0.0D);
      posestack.mulPose(shulker.getAttachFace().getOpposite().getRotation());
      posestack.translate(0.0D, -0.5D, 0.0D);
   }
}
