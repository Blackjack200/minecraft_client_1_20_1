package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

public class NoRenderParticle extends Particle {
   protected NoRenderParticle(ClientLevel clientlevel, double d0, double d1, double d2) {
      super(clientlevel, d0, d1, d2);
   }

   protected NoRenderParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
   }

   public final void render(VertexConsumer vertexconsumer, Camera camera, float f) {
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.NO_RENDER;
   }
}
