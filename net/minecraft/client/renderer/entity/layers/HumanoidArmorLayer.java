package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
   private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
   private final A innerModel;
   private final A outerModel;
   private final TextureAtlas armorTrimAtlas;

   public HumanoidArmorLayer(RenderLayerParent<T, M> renderlayerparent, A humanoidmodel, A humanoidmodel1, ModelManager modelmanager) {
      super(renderlayerparent);
      this.innerModel = humanoidmodel;
      this.outerModel = humanoidmodel1;
      this.armorTrimAtlas = modelmanager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      this.renderArmorPiece(posestack, multibuffersource, livingentity, EquipmentSlot.CHEST, i, this.getArmorModel(EquipmentSlot.CHEST));
      this.renderArmorPiece(posestack, multibuffersource, livingentity, EquipmentSlot.LEGS, i, this.getArmorModel(EquipmentSlot.LEGS));
      this.renderArmorPiece(posestack, multibuffersource, livingentity, EquipmentSlot.FEET, i, this.getArmorModel(EquipmentSlot.FEET));
      this.renderArmorPiece(posestack, multibuffersource, livingentity, EquipmentSlot.HEAD, i, this.getArmorModel(EquipmentSlot.HEAD));
   }

   private void renderArmorPiece(PoseStack posestack, MultiBufferSource multibuffersource, T livingentity, EquipmentSlot equipmentslot, int i, A humanoidmodel) {
      ItemStack itemstack = livingentity.getItemBySlot(equipmentslot);
      Item flag = itemstack.getItem();
      if (flag instanceof ArmorItem armoritem) {
         if (armoritem.getEquipmentSlot() == equipmentslot) {
            this.getParentModel().copyPropertiesTo(humanoidmodel);
            this.setPartVisibility(humanoidmodel, equipmentslot);
            boolean flag = this.usesInnerModel(equipmentslot);
            if (armoritem instanceof DyeableArmorItem) {
               DyeableArmorItem dyeablearmoritem = (DyeableArmorItem)armoritem;
               int j = dyeablearmoritem.getColor(itemstack);
               float f = (float)(j >> 16 & 255) / 255.0F;
               float f1 = (float)(j >> 8 & 255) / 255.0F;
               float f2 = (float)(j & 255) / 255.0F;
               this.renderModel(posestack, multibuffersource, i, armoritem, humanoidmodel, flag, f, f1, f2, (String)null);
               this.renderModel(posestack, multibuffersource, i, armoritem, humanoidmodel, flag, 1.0F, 1.0F, 1.0F, "overlay");
            } else {
               this.renderModel(posestack, multibuffersource, i, armoritem, humanoidmodel, flag, 1.0F, 1.0F, 1.0F, (String)null);
            }

            ArmorTrim.getTrim(livingentity.level().registryAccess(), itemstack).ifPresent((armortrim) -> this.renderTrim(armoritem.getMaterial(), posestack, multibuffersource, i, armortrim, humanoidmodel, flag));
            if (itemstack.hasFoil()) {
               this.renderGlint(posestack, multibuffersource, i, humanoidmodel);
            }

         }
      }
   }

   protected void setPartVisibility(A humanoidmodel, EquipmentSlot equipmentslot) {
      humanoidmodel.setAllVisible(false);
      switch (equipmentslot) {
         case HEAD:
            humanoidmodel.head.visible = true;
            humanoidmodel.hat.visible = true;
            break;
         case CHEST:
            humanoidmodel.body.visible = true;
            humanoidmodel.rightArm.visible = true;
            humanoidmodel.leftArm.visible = true;
            break;
         case LEGS:
            humanoidmodel.body.visible = true;
            humanoidmodel.rightLeg.visible = true;
            humanoidmodel.leftLeg.visible = true;
            break;
         case FEET:
            humanoidmodel.rightLeg.visible = true;
            humanoidmodel.leftLeg.visible = true;
      }

   }

   private void renderModel(PoseStack posestack, MultiBufferSource multibuffersource, int i, ArmorItem armoritem, A humanoidmodel, boolean flag, float f, float f1, float f2, @Nullable String s) {
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.armorCutoutNoCull(this.getArmorLocation(armoritem, flag, s)));
      humanoidmodel.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F);
   }

   private void renderTrim(ArmorMaterial armormaterial, PoseStack posestack, MultiBufferSource multibuffersource, int i, ArmorTrim armortrim, A humanoidmodel, boolean flag) {
      TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(flag ? armortrim.innerTexture(armormaterial) : armortrim.outerTexture(armormaterial));
      VertexConsumer vertexconsumer = textureatlassprite.wrap(multibuffersource.getBuffer(Sheets.armorTrimsSheet()));
      humanoidmodel.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderGlint(PoseStack posestack, MultiBufferSource multibuffersource, int i, A humanoidmodel) {
      humanoidmodel.renderToBuffer(posestack, multibuffersource.getBuffer(RenderType.armorEntityGlint()), i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private A getArmorModel(EquipmentSlot equipmentslot) {
      return (A)(this.usesInnerModel(equipmentslot) ? this.innerModel : this.outerModel);
   }

   private boolean usesInnerModel(EquipmentSlot equipmentslot) {
      return equipmentslot == EquipmentSlot.LEGS;
   }

   private ResourceLocation getArmorLocation(ArmorItem armoritem, boolean flag, @Nullable String s) {
      String s1 = "textures/models/armor/" + armoritem.getMaterial().getName() + "_layer_" + (flag ? 2 : 1) + (s == null ? "" : "_" + s) + ".png";
      return ARMOR_LOCATION_CACHE.computeIfAbsent(s1, ResourceLocation::new);
   }
}
