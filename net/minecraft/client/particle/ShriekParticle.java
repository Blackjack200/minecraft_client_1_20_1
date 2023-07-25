package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ShriekParticle extends TextureSheetParticle {
   private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
   private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
   private static final float MAGICAL_X_ROT = 1.0472F;
   private int delay;

   ShriekParticle(ClientLevel clientlevel, double d0, double d1, double d2, int i) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.quadSize = 0.85F;
      this.delay = i;
      this.lifetime = 30;
      this.gravity = 0.0F;
      this.xd = 0.0D;
      this.yd = 0.1D;
      this.zd = 0.0D;
   }

   public float getQuadSize(float f) {
      return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 0.75F, 0.0F, 1.0F);
   }

   public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
      if (this.delay <= 0) {
         this.alpha = 1.0F - Mth.clamp(((float)this.age + f) / (float)this.lifetime, 0.0F, 1.0F);
         this.renderRotatedParticle(vertexconsumer, camera, f, (quaternionf1) -> quaternionf1.mul((new Quaternionf()).rotationX(-1.0472F)));
         this.renderRotatedParticle(vertexconsumer, camera, f, (quaternionf) -> quaternionf.mul((new Quaternionf()).rotationYXZ(-(float)Math.PI, 1.0472F, 0.0F)));
      }
   }

   private void renderRotatedParticle(VertexConsumer vertexconsumer, Camera camera, float f, Consumer<Quaternionf> consumer) {
      Vec3 vec3 = camera.getPosition();
      float f1 = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
      float f2 = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
      float f3 = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
      Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
      consumer.accept(quaternionf);
      quaternionf.transform(TRANSFORM_VECTOR);
      Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float f4 = this.getQuadSize(f);

      for(int i = 0; i < 4; ++i) {
         Vector3f vector3f = avector3f[i];
         vector3f.rotate(quaternionf);
         vector3f.mul(f4);
         vector3f.add(f1, f2, f3);
      }

      int j = this.getLightColor(f);
      this.makeCornerVertex(vertexconsumer, avector3f[0], this.getU1(), this.getV1(), j);
      this.makeCornerVertex(vertexconsumer, avector3f[1], this.getU1(), this.getV0(), j);
      this.makeCornerVertex(vertexconsumer, avector3f[2], this.getU0(), this.getV0(), j);
      this.makeCornerVertex(vertexconsumer, avector3f[3], this.getU0(), this.getV1(), j);
   }

   private void makeCornerVertex(VertexConsumer vertexconsumer, Vector3f vector3f, float f, float f1, int i) {
      vertexconsumer.vertex((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z()).uv(f, f1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(i).endVertex();
   }

   public int getLightColor(float f) {
      return 240;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      if (this.delay > 0) {
         --this.delay;
      } else {
         super.tick();
      }
   }

   public static class Provider implements ParticleProvider<ShriekParticleOption> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(ShriekParticleOption shriekparticleoption, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         ShriekParticle shriekparticle = new ShriekParticle(clientlevel, d0, d1, d2, shriekparticleoption.getDelay());
         shriekparticle.pickSprite(this.sprite);
         shriekparticle.setAlpha(1.0F);
         return shriekparticle;
      }
   }
}
