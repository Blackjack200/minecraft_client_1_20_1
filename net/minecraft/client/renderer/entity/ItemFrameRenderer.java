package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

public class ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T> {
   private static final ModelResourceLocation FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=false");
   private static final ModelResourceLocation MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=true");
   private static final ModelResourceLocation GLOW_FRAME_LOCATION = ModelResourceLocation.vanilla("glow_item_frame", "map=false");
   private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("glow_item_frame", "map=true");
   public static final int GLOW_FRAME_BRIGHTNESS = 5;
   public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
   private final ItemRenderer itemRenderer;
   private final BlockRenderDispatcher blockRenderer;

   public ItemFrameRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.itemRenderer = entityrendererprovider_context.getItemRenderer();
      this.blockRenderer = entityrendererprovider_context.getBlockRenderDispatcher();
   }

   protected int getBlockLightLevel(T itemframe, BlockPos blockpos) {
      return itemframe.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, super.getBlockLightLevel(itemframe, blockpos)) : super.getBlockLightLevel(itemframe, blockpos);
   }

   public void render(T itemframe, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      super.render(itemframe, f, f1, posestack, multibuffersource, i);
      posestack.pushPose();
      Direction direction = itemframe.getDirection();
      Vec3 vec3 = this.getRenderOffset(itemframe, f1);
      posestack.translate(-vec3.x(), -vec3.y(), -vec3.z());
      double d0 = 0.46875D;
      posestack.translate((double)direction.getStepX() * 0.46875D, (double)direction.getStepY() * 0.46875D, (double)direction.getStepZ() * 0.46875D);
      posestack.mulPose(Axis.XP.rotationDegrees(itemframe.getXRot()));
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - itemframe.getYRot()));
      boolean flag = itemframe.isInvisible();
      ItemStack itemstack = itemframe.getItem();
      if (!flag) {
         ModelManager modelmanager = this.blockRenderer.getBlockModelShaper().getModelManager();
         ModelResourceLocation modelresourcelocation = this.getFrameModelResourceLoc(itemframe, itemstack);
         posestack.pushPose();
         posestack.translate(-0.5F, -0.5F, -0.5F);
         this.blockRenderer.getModelRenderer().renderModel(posestack.last(), multibuffersource.getBuffer(Sheets.solidBlockSheet()), (BlockState)null, modelmanager.getModel(modelresourcelocation), 1.0F, 1.0F, 1.0F, i, OverlayTexture.NO_OVERLAY);
         posestack.popPose();
      }

      if (!itemstack.isEmpty()) {
         OptionalInt optionalint = itemframe.getFramedMapId();
         if (flag) {
            posestack.translate(0.0F, 0.0F, 0.5F);
         } else {
            posestack.translate(0.0F, 0.0F, 0.4375F);
         }

         int j = optionalint.isPresent() ? itemframe.getRotation() % 4 * 2 : itemframe.getRotation();
         posestack.mulPose(Axis.ZP.rotationDegrees((float)j * 360.0F / 8.0F));
         if (optionalint.isPresent()) {
            posestack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            float f2 = 0.0078125F;
            posestack.scale(0.0078125F, 0.0078125F, 0.0078125F);
            posestack.translate(-64.0F, -64.0F, 0.0F);
            MapItemSavedData mapitemsaveddata = MapItem.getSavedData(optionalint.getAsInt(), itemframe.level());
            posestack.translate(0.0F, 0.0F, -1.0F);
            if (mapitemsaveddata != null) {
               int k = this.getLightVal(itemframe, 15728850, i);
               Minecraft.getInstance().gameRenderer.getMapRenderer().render(posestack, multibuffersource, optionalint.getAsInt(), mapitemsaveddata, true, k);
            }
         } else {
            int l = this.getLightVal(itemframe, 15728880, i);
            posestack.scale(0.5F, 0.5F, 0.5F);
            this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, l, OverlayTexture.NO_OVERLAY, posestack, multibuffersource, itemframe.level(), itemframe.getId());
         }
      }

      posestack.popPose();
   }

   private int getLightVal(T itemframe, int i, int j) {
      return itemframe.getType() == EntityType.GLOW_ITEM_FRAME ? i : j;
   }

   private ModelResourceLocation getFrameModelResourceLoc(T itemframe, ItemStack itemstack) {
      boolean flag = itemframe.getType() == EntityType.GLOW_ITEM_FRAME;
      if (itemstack.is(Items.FILLED_MAP)) {
         return flag ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;
      } else {
         return flag ? GLOW_FRAME_LOCATION : FRAME_LOCATION;
      }
   }

   public Vec3 getRenderOffset(T itemframe, float f) {
      return new Vec3((double)((float)itemframe.getDirection().getStepX() * 0.3F), -0.25D, (double)((float)itemframe.getDirection().getStepZ() * 0.3F));
   }

   public ResourceLocation getTextureLocation(T itemframe) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   protected boolean shouldShowName(T itemframe) {
      if (Minecraft.renderNames() && !itemframe.getItem().isEmpty() && itemframe.getItem().hasCustomHoverName() && this.entityRenderDispatcher.crosshairPickEntity == itemframe) {
         double d0 = this.entityRenderDispatcher.distanceToSqr(itemframe);
         float f = itemframe.isDiscrete() ? 32.0F : 64.0F;
         return d0 < (double)(f * f);
      } else {
         return false;
      }
   }

   protected void renderNameTag(T itemframe, Component component, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      super.renderNameTag(itemframe, itemframe.getItem().getHoverName(), posestack, multibuffersource, i);
   }
}
