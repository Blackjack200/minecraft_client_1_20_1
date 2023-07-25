package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VibrationSignalParticle extends TextureSheetParticle {
   private final PositionSource target;
   private float rot;
   private float rotO;
   private float pitch;
   private float pitchO;

   VibrationSignalParticle(ClientLevel clientlevel, double d0, double d1, double d2, PositionSource positionsource, int i) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.quadSize = 0.3F;
      this.target = positionsource;
      this.lifetime = i;
      Optional<Vec3> optional = positionsource.getPosition(clientlevel);
      if (optional.isPresent()) {
         Vec3 vec3 = optional.get();
         double d3 = d0 - vec3.x();
         double d4 = d1 - vec3.y();
         double d5 = d2 - vec3.z();
         this.rotO = this.rot = (float)Mth.atan2(d3, d5);
         this.pitchO = this.pitch = (float)Mth.atan2(d4, Math.sqrt(d3 * d3 + d5 * d5));
      }

   }

   public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
      float f1 = Mth.sin(((float)this.age + f - ((float)Math.PI * 2F)) * 0.05F) * 2.0F;
      float f2 = Mth.lerp(f, this.rotO, this.rot);
      float f3 = Mth.lerp(f, this.pitchO, this.pitch) + ((float)Math.PI / 2F);
      this.renderSignal(vertexconsumer, camera, f, (quaternionf1) -> quaternionf1.rotateY(f2).rotateX(-f3).rotateY(f1));
      this.renderSignal(vertexconsumer, camera, f, (quaternionf) -> quaternionf.rotateY(-(float)Math.PI + f2).rotateX(f3).rotateY(f1));
   }

   private void renderSignal(VertexConsumer vertexconsumer, Camera camera, float f, Consumer<Quaternionf> consumer) {
      Vec3 vec3 = camera.getPosition();
      float f1 = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
      float f2 = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
      float f3 = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
      Vector3f vector3f = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
      Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, vector3f.x(), vector3f.y(), vector3f.z());
      consumer.accept(quaternionf);
      Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float f4 = this.getQuadSize(f);

      for(int i = 0; i < 4; ++i) {
         Vector3f vector3f1 = avector3f[i];
         vector3f1.rotate(quaternionf);
         vector3f1.mul(f4);
         vector3f1.add(f1, f2, f3);
      }

      float f5 = this.getU0();
      float f6 = this.getU1();
      float f7 = this.getV0();
      float f8 = this.getV1();
      int j = this.getLightColor(f);
      vertexconsumer.vertex((double)avector3f[0].x(), (double)avector3f[0].y(), (double)avector3f[0].z()).uv(f6, f8).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
      vertexconsumer.vertex((double)avector3f[1].x(), (double)avector3f[1].y(), (double)avector3f[1].z()).uv(f6, f7).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
      vertexconsumer.vertex((double)avector3f[2].x(), (double)avector3f[2].y(), (double)avector3f[2].z()).uv(f5, f7).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
      vertexconsumer.vertex((double)avector3f[3].x(), (double)avector3f[3].y(), (double)avector3f[3].z()).uv(f5, f8).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
   }

   public int getLightColor(float f) {
      return 240;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         Optional<Vec3> optional = this.target.getPosition(this.level);
         if (optional.isEmpty()) {
            this.remove();
         } else {
            int i = this.lifetime - this.age;
            double d0 = 1.0D / (double)i;
            Vec3 vec3 = optional.get();
            this.x = Mth.lerp(d0, this.x, vec3.x());
            this.y = Mth.lerp(d0, this.y, vec3.y());
            this.z = Mth.lerp(d0, this.z, vec3.z());
            double d1 = this.x - vec3.x();
            double d2 = this.y - vec3.y();
            double d3 = this.z - vec3.z();
            this.rotO = this.rot;
            this.rot = (float)Mth.atan2(d1, d3);
            this.pitchO = this.pitch;
            this.pitch = (float)Mth.atan2(d2, Math.sqrt(d1 * d1 + d3 * d3));
         }
      }
   }

   public static class Provider implements ParticleProvider<VibrationParticleOption> {
      private final SpriteSet sprite;

      public Provider(SpriteSet spriteset) {
         this.sprite = spriteset;
      }

      public Particle createParticle(VibrationParticleOption vibrationparticleoption, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         VibrationSignalParticle vibrationsignalparticle = new VibrationSignalParticle(clientlevel, d0, d1, d2, vibrationparticleoption.getDestination(), vibrationparticleoption.getArrivalInTicks());
         vibrationsignalparticle.pickSprite(this.sprite);
         vibrationsignalparticle.setAlpha(1.0F);
         return vibrationsignalparticle;
      }
   }
}
