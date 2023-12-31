package com.mojang.blaze3d.audio;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class Listener {
   private float gain = 1.0F;
   private Vec3 position = Vec3.ZERO;

   public void setListenerPosition(Vec3 vec3) {
      this.position = vec3;
      AL10.alListener3f(4100, (float)vec3.x, (float)vec3.y, (float)vec3.z);
   }

   public Vec3 getListenerPosition() {
      return this.position;
   }

   public void setListenerOrientation(Vector3f vector3f, Vector3f vector3f1) {
      AL10.alListenerfv(4111, new float[]{vector3f.x(), vector3f.y(), vector3f.z(), vector3f1.x(), vector3f1.y(), vector3f1.z()});
   }

   public void setGain(float f) {
      AL10.alListenerf(4106, f);
      this.gain = f;
   }

   public float getGain() {
      return this.gain;
   }

   public void reset() {
      this.setListenerPosition(Vec3.ZERO);
      this.setListenerOrientation(new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(0.0F, 1.0F, 0.0F));
   }
}
