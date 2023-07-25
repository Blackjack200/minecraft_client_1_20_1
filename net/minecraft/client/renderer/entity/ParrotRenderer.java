package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

public class ParrotRenderer extends MobRenderer<Parrot, ParrotModel> {
   private static final ResourceLocation RED_BLUE = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
   private static final ResourceLocation BLUE = new ResourceLocation("textures/entity/parrot/parrot_blue.png");
   private static final ResourceLocation GREEN = new ResourceLocation("textures/entity/parrot/parrot_green.png");
   private static final ResourceLocation YELLOW_BLUE = new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png");
   private static final ResourceLocation GREY = new ResourceLocation("textures/entity/parrot/parrot_grey.png");

   public ParrotRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new ParrotModel(entityrendererprovider_context.bakeLayer(ModelLayers.PARROT)), 0.3F);
   }

   public ResourceLocation getTextureLocation(Parrot parrot) {
      return getVariantTexture(parrot.getVariant());
   }

   public static ResourceLocation getVariantTexture(Parrot.Variant parrot_variant) {
      ResourceLocation var10000;
      switch (parrot_variant) {
         case RED_BLUE:
            var10000 = RED_BLUE;
            break;
         case BLUE:
            var10000 = BLUE;
            break;
         case GREEN:
            var10000 = GREEN;
            break;
         case YELLOW_BLUE:
            var10000 = YELLOW_BLUE;
            break;
         case GRAY:
            var10000 = GREY;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public float getBob(Parrot parrot, float f) {
      float f1 = Mth.lerp(f, parrot.oFlap, parrot.flap);
      float f2 = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
      return (Mth.sin(f1) + 1.0F) * f2;
   }
}
