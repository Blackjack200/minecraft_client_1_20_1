package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class MobAppearanceParticle extends Particle {
   private final Model model;
   private final RenderType renderType = RenderType.entityTranslucent(ElderGuardianRenderer.GUARDIAN_ELDER_LOCATION);

   MobAppearanceParticle(ClientLevel clientlevel, double d0, double d1, double d2) {
      super(clientlevel, d0, d1, d2);
      this.model = new GuardianModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELDER_GUARDIAN));
      this.gravity = 0.0F;
      this.lifetime = 30;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
      float f1 = ((float)this.age + f) / (float)this.lifetime;
      float f2 = 0.05F + 0.5F * Mth.sin(f1 * (float)Math.PI);
      PoseStack posestack = new PoseStack();
      posestack.mulPose(camera.rotation());
      posestack.mulPose(Axis.XP.rotationDegrees(150.0F * f1 - 60.0F));
      posestack.scale(-1.0F, -1.0F, 1.0F);
      posestack.translate(0.0F, -1.101F, 1.5F);
      MultiBufferSource.BufferSource multibuffersource_buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
      VertexConsumer vertexconsumer1 = multibuffersource_buffersource.getBuffer(this.renderType);
      this.model.renderToBuffer(posestack, vertexconsumer1, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, f2);
      multibuffersource_buffersource.endBatch();
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new MobAppearanceParticle(clientlevel, d0, d1, d2);
      }
   }
}
