package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class DustColorTransitionParticle extends DustParticleBase<DustColorTransitionOptions> {
   private final Vector3f fromColor;
   private final Vector3f toColor;

   protected DustColorTransitionParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, DustColorTransitionOptions dustcolortransitionoptions, SpriteSet spriteset) {
      super(clientlevel, d0, d1, d2, d3, d4, d5, dustcolortransitionoptions, spriteset);
      float f = this.random.nextFloat() * 0.4F + 0.6F;
      this.fromColor = this.randomizeColor(dustcolortransitionoptions.getFromColor(), f);
      this.toColor = this.randomizeColor(dustcolortransitionoptions.getToColor(), f);
   }

   private Vector3f randomizeColor(Vector3f vector3f, float f) {
      return new Vector3f(this.randomizeColor(vector3f.x(), f), this.randomizeColor(vector3f.y(), f), this.randomizeColor(vector3f.z(), f));
   }

   private void lerpColors(float f) {
      float f1 = ((float)this.age + f) / ((float)this.lifetime + 1.0F);
      Vector3f vector3f = (new Vector3f((Vector3fc)this.fromColor)).lerp(this.toColor, f1);
      this.rCol = vector3f.x();
      this.gCol = vector3f.y();
      this.bCol = vector3f.z();
   }

   public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
      this.lerpColors(f);
      super.render(vertexconsumer, camera, f);
   }

   public static class Provider implements ParticleProvider<DustColorTransitionOptions> {
      private final SpriteSet sprites;

      public Provider(SpriteSet spriteset) {
         this.sprites = spriteset;
      }

      public Particle createParticle(DustColorTransitionOptions dustcolortransitionoptions, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new DustColorTransitionParticle(clientlevel, d0, d1, d2, d3, d4, d5, dustcolortransitionoptions, this.sprites);
      }
   }
}
