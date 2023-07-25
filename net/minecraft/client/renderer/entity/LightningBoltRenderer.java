package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4f;

public class LightningBoltRenderer extends EntityRenderer<LightningBolt> {
   public LightningBoltRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
   }

   public void render(LightningBolt lightningbolt, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      float[] afloat = new float[8];
      float[] afloat1 = new float[8];
      float f2 = 0.0F;
      float f3 = 0.0F;
      RandomSource randomsource = RandomSource.create(lightningbolt.seed);

      for(int j = 7; j >= 0; --j) {
         afloat[j] = f2;
         afloat1[j] = f3;
         f2 += (float)(randomsource.nextInt(11) - 5);
         f3 += (float)(randomsource.nextInt(11) - 5);
      }

      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lightning());
      Matrix4f matrix4f = posestack.last().pose();

      for(int k = 0; k < 4; ++k) {
         RandomSource randomsource1 = RandomSource.create(lightningbolt.seed);

         for(int l = 0; l < 3; ++l) {
            int i1 = 7;
            int j1 = 0;
            if (l > 0) {
               i1 = 7 - l;
            }

            if (l > 0) {
               j1 = i1 - 2;
            }

            float f4 = afloat[i1] - f2;
            float f5 = afloat1[i1] - f3;

            for(int k1 = i1; k1 >= j1; --k1) {
               float f6 = f4;
               float f7 = f5;
               if (l == 0) {
                  f4 += (float)(randomsource1.nextInt(11) - 5);
                  f5 += (float)(randomsource1.nextInt(11) - 5);
               } else {
                  f4 += (float)(randomsource1.nextInt(31) - 15);
                  f5 += (float)(randomsource1.nextInt(31) - 15);
               }

               float f8 = 0.5F;
               float f9 = 0.45F;
               float f10 = 0.45F;
               float f11 = 0.5F;
               float f12 = 0.1F + (float)k * 0.2F;
               if (l == 0) {
                  f12 *= (float)k1 * 0.1F + 1.0F;
               }

               float f13 = 0.1F + (float)k * 0.2F;
               if (l == 0) {
                  f13 *= ((float)k1 - 1.0F) * 0.1F + 1.0F;
               }

               quad(matrix4f, vertexconsumer, f4, f5, k1, f6, f7, 0.45F, 0.45F, 0.5F, f12, f13, false, false, true, false);
               quad(matrix4f, vertexconsumer, f4, f5, k1, f6, f7, 0.45F, 0.45F, 0.5F, f12, f13, true, false, true, true);
               quad(matrix4f, vertexconsumer, f4, f5, k1, f6, f7, 0.45F, 0.45F, 0.5F, f12, f13, true, true, false, true);
               quad(matrix4f, vertexconsumer, f4, f5, k1, f6, f7, 0.45F, 0.45F, 0.5F, f12, f13, false, true, false, false);
            }
         }
      }

   }

   private static void quad(Matrix4f matrix4f, VertexConsumer vertexconsumer, float f, float f1, int i, float f2, float f3, float f4, float f5, float f6, float f7, float f8, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      vertexconsumer.vertex(matrix4f, f + (flag ? f8 : -f8), (float)(i * 16), f1 + (flag1 ? f8 : -f8)).color(f4, f5, f6, 0.3F).endVertex();
      vertexconsumer.vertex(matrix4f, f2 + (flag ? f7 : -f7), (float)((i + 1) * 16), f3 + (flag1 ? f7 : -f7)).color(f4, f5, f6, 0.3F).endVertex();
      vertexconsumer.vertex(matrix4f, f2 + (flag2 ? f7 : -f7), (float)((i + 1) * 16), f3 + (flag3 ? f7 : -f7)).color(f4, f5, f6, 0.3F).endVertex();
      vertexconsumer.vertex(matrix4f, f + (flag2 ? f8 : -f8), (float)(i * 16), f1 + (flag3 ? f8 : -f8)).color(f4, f5, f6, 0.3F).endVertex();
   }

   public ResourceLocation getTextureLocation(LightningBolt lightningbolt) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
