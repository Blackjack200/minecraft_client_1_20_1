package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;

public class MushroomCowRenderer extends MobRenderer<MushroomCow, CowModel<MushroomCow>> {
   private static final Map<MushroomCow.MushroomType, ResourceLocation> TEXTURES = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put(MushroomCow.MushroomType.BROWN, new ResourceLocation("textures/entity/cow/brown_mooshroom.png"));
      hashmap.put(MushroomCow.MushroomType.RED, new ResourceLocation("textures/entity/cow/red_mooshroom.png"));
   });

   public MushroomCowRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new CowModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.MOOSHROOM)), 0.7F);
      this.addLayer(new MushroomCowMushroomLayer<>(this, entityrendererprovider_context.getBlockRenderDispatcher()));
   }

   public ResourceLocation getTextureLocation(MushroomCow mushroomcow) {
      return TEXTURES.get(mushroomcow.getVariant());
   }
}
