package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public class ItemPickupParticle extends Particle {
   private static final int LIFE_TIME = 3;
   private final RenderBuffers renderBuffers;
   private final Entity itemEntity;
   private final Entity target;
   private int life;
   private final EntityRenderDispatcher entityRenderDispatcher;

   public ItemPickupParticle(EntityRenderDispatcher entityrenderdispatcher, RenderBuffers renderbuffers, ClientLevel clientlevel, Entity entity, Entity entity1) {
      this(entityrenderdispatcher, renderbuffers, clientlevel, entity, entity1, entity.getDeltaMovement());
   }

   private ItemPickupParticle(EntityRenderDispatcher entityrenderdispatcher, RenderBuffers renderbuffers, ClientLevel clientlevel, Entity entity, Entity entity1, Vec3 vec3) {
      super(clientlevel, entity.getX(), entity.getY(), entity.getZ(), vec3.x, vec3.y, vec3.z);
      this.renderBuffers = renderbuffers;
      this.itemEntity = this.getSafeCopy(entity);
      this.target = entity1;
      this.entityRenderDispatcher = entityrenderdispatcher;
   }

   private Entity getSafeCopy(Entity entity) {
      return (Entity)(!(entity instanceof ItemEntity) ? entity : ((ItemEntity)entity).copy());
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void render(VertexConsumer vertexconsumer, Camera camera, float f) {
      float f1 = ((float)this.life + f) / 3.0F;
      f1 *= f1;
      double d0 = Mth.lerp((double)f, this.target.xOld, this.target.getX());
      double d1 = Mth.lerp((double)f, this.target.yOld, (this.target.getY() + this.target.getEyeY()) / 2.0D);
      double d2 = Mth.lerp((double)f, this.target.zOld, this.target.getZ());
      double d3 = Mth.lerp((double)f1, this.itemEntity.getX(), d0);
      double d4 = Mth.lerp((double)f1, this.itemEntity.getY(), d1);
      double d5 = Mth.lerp((double)f1, this.itemEntity.getZ(), d2);
      MultiBufferSource.BufferSource multibuffersource_buffersource = this.renderBuffers.bufferSource();
      Vec3 vec3 = camera.getPosition();
      this.entityRenderDispatcher.render(this.itemEntity, d3 - vec3.x(), d4 - vec3.y(), d5 - vec3.z(), this.itemEntity.getYRot(), f, new PoseStack(), multibuffersource_buffersource, this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, f));
      multibuffersource_buffersource.endBatch();
   }

   public void tick() {
      ++this.life;
      if (this.life == 3) {
         this.remove();
      }

   }
}
