package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;

public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
   private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(Variant.class), (enummap) -> {
      enummap.put(Variant.WHITE, new ResourceLocation("textures/entity/horse/horse_white.png"));
      enummap.put(Variant.CREAMY, new ResourceLocation("textures/entity/horse/horse_creamy.png"));
      enummap.put(Variant.CHESTNUT, new ResourceLocation("textures/entity/horse/horse_chestnut.png"));
      enummap.put(Variant.BROWN, new ResourceLocation("textures/entity/horse/horse_brown.png"));
      enummap.put(Variant.BLACK, new ResourceLocation("textures/entity/horse/horse_black.png"));
      enummap.put(Variant.GRAY, new ResourceLocation("textures/entity/horse/horse_gray.png"));
      enummap.put(Variant.DARK_BROWN, new ResourceLocation("textures/entity/horse/horse_darkbrown.png"));
   });

   public HorseRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new HorseModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.HORSE)), 1.1F);
      this.addLayer(new HorseMarkingLayer(this));
      this.addLayer(new HorseArmorLayer(this, entityrendererprovider_context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Horse horse) {
      return LOCATION_BY_VARIANT.get(horse.getVariant());
   }
}
