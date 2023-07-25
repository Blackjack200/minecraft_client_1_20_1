package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class SingleQuadParticle extends Particle {
   protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

   protected SingleQuadParticle(ClientLevel clientlevel, double d0, double d1, double d2) {
      super(clientlevel, d0, d1, d2);
   }

   protected SingleQuadParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
   }

   public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
      Vec3 vec3 = camera.getPosition();
      float f1 = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
      float f2 = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
      float f3 = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
      Quaternionf quaternionf;
      if (this.roll == 0.0F) {
         quaternionf = camera.rotation();
      } else {
         quaternionf = new Quaternionf(camera.rotation());
         quaternionf.rotateZ(Mth.lerp(f, this.oRoll, this.roll));
      }

      Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float f4 = this.getQuadSize(f);

      for(int i = 0; i < 4; ++i) {
         Vector3f vector3f = avector3f[i];
         vector3f.rotate(quaternionf);
         vector3f.mul(f4);
         vector3f.add(f1, f2, f3);
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

   public float getQuadSize(float f) {
      return this.quadSize;
   }

   public Particle scale(float f) {
      this.quadSize *= f;
      return super.scale(f);
   }

   protected abstract float getU0();

   protected abstract float getU1();

   protected abstract float getV0();

   protected abstract float getV1();
}
