package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class EntityModel<T extends Entity> extends Model {
   public float attackTime;
   public boolean riding;
   public boolean young = true;

   protected EntityModel() {
      this(RenderType::entityCutoutNoCull);
   }

   protected EntityModel(Function<ResourceLocation, RenderType> function) {
      super(function);
   }

   public abstract void setupAnim(T entity, float f, float f1, float f2, float f3, float f4);

   public void prepareMobModel(T entity, float f, float f1, float f2) {
   }

   public void copyPropertiesTo(EntityModel<T> entitymodel) {
      entitymodel.attackTime = this.attackTime;
      entitymodel.riding = this.riding;
      entitymodel.young = this.young;
   }
}
