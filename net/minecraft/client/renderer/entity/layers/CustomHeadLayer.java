package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

public class CustomHeadLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
   private final float scaleX;
   private final float scaleY;
   private final float scaleZ;
   private final Map<SkullBlock.Type, SkullModelBase> skullModels;
   private final ItemInHandRenderer itemInHandRenderer;

   public CustomHeadLayer(RenderLayerParent<T, M> renderlayerparent, EntityModelSet entitymodelset, ItemInHandRenderer iteminhandrenderer) {
      this(renderlayerparent, entitymodelset, 1.0F, 1.0F, 1.0F, iteminhandrenderer);
   }

   public CustomHeadLayer(RenderLayerParent<T, M> renderlayerparent, EntityModelSet entitymodelset, float f, float f1, float f2, ItemInHandRenderer iteminhandrenderer) {
      super(renderlayerparent);
      this.scaleX = f;
      this.scaleY = f1;
      this.scaleZ = f2;
      this.skullModels = SkullBlockRenderer.createSkullRenderers(entitymodelset);
      this.itemInHandRenderer = iteminhandrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      ItemStack itemstack = livingentity.getItemBySlot(EquipmentSlot.HEAD);
      if (!itemstack.isEmpty()) {
         Item item = itemstack.getItem();
         posestack.pushPose();
         posestack.scale(this.scaleX, this.scaleY, this.scaleZ);
         boolean flag = livingentity instanceof Villager || livingentity instanceof ZombieVillager;
         if (livingentity.isBaby() && !(livingentity instanceof Villager)) {
            float f6 = 2.0F;
            float f7 = 1.4F;
            posestack.translate(0.0F, 0.03125F, 0.0F);
            posestack.scale(0.7F, 0.7F, 0.7F);
            posestack.translate(0.0F, 1.0F, 0.0F);
         }

         this.getParentModel().getHead().translateAndRotate(posestack);
         if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) {
            float f8 = 1.1875F;
            posestack.scale(1.1875F, -1.1875F, -1.1875F);
            if (flag) {
               posestack.translate(0.0F, 0.0625F, 0.0F);
            }

            GameProfile gameprofile = null;
            if (itemstack.hasTag()) {
               CompoundTag compoundtag = itemstack.getTag();
               if (compoundtag.contains("SkullOwner", 10)) {
                  gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
               }
            }

            posestack.translate(-0.5D, 0.0D, -0.5D);
            SkullBlock.Type skullblock_type = ((AbstractSkullBlock)((BlockItem)item).getBlock()).getType();
            SkullModelBase skullmodelbase = this.skullModels.get(skullblock_type);
            RenderType rendertype = SkullBlockRenderer.getRenderType(skullblock_type, gameprofile);
            Entity var22 = livingentity.getVehicle();
            WalkAnimationState walkanimationstate;
            if (var22 instanceof LivingEntity) {
               LivingEntity livingentity1 = (LivingEntity)var22;
               walkanimationstate = livingentity1.walkAnimation;
            } else {
               walkanimationstate = livingentity.walkAnimation;
            }

            float f9 = walkanimationstate.position(f2);
            SkullBlockRenderer.renderSkull((Direction)null, 180.0F, f9, posestack, multibuffersource, i, skullmodelbase, rendertype);
         } else {
            label60: {
               if (item instanceof ArmorItem) {
                  ArmorItem armoritem = (ArmorItem)item;
                  if (armoritem.getEquipmentSlot() == EquipmentSlot.HEAD) {
                     break label60;
                  }
               }

               translateToHead(posestack, flag);
               this.itemInHandRenderer.renderItem(livingentity, itemstack, ItemDisplayContext.HEAD, false, posestack, multibuffersource, i);
            }
         }

         posestack.popPose();
      }
   }

   public static void translateToHead(PoseStack posestack, boolean flag) {
      float f = 0.625F;
      posestack.translate(0.0F, -0.25F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
      posestack.scale(0.625F, -0.625F, -0.625F);
      if (flag) {
         posestack.translate(0.0F, 0.1875F, 0.0F);
      }

   }
}
