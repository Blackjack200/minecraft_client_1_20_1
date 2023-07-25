package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DamageSource {
   private final Holder<DamageType> type;
   @Nullable
   private final Entity causingEntity;
   @Nullable
   private final Entity directEntity;
   @Nullable
   private final Vec3 damageSourcePosition;

   public String toString() {
      return "DamageSource (" + this.type().msgId() + ")";
   }

   public float getFoodExhaustion() {
      return this.type().exhaustion();
   }

   public boolean isIndirect() {
      return this.causingEntity != this.directEntity;
   }

   private DamageSource(Holder<DamageType> holder, @Nullable Entity entity, @Nullable Entity entity1, @Nullable Vec3 vec3) {
      this.type = holder;
      this.causingEntity = entity1;
      this.directEntity = entity;
      this.damageSourcePosition = vec3;
   }

   public DamageSource(Holder<DamageType> holder, @Nullable Entity entity, @Nullable Entity entity1) {
      this(holder, entity, entity1, (Vec3)null);
   }

   public DamageSource(Holder<DamageType> holder, Vec3 vec3) {
      this(holder, (Entity)null, (Entity)null, vec3);
   }

   public DamageSource(Holder<DamageType> holder, @Nullable Entity entity) {
      this(holder, entity, entity);
   }

   public DamageSource(Holder<DamageType> holder) {
      this(holder, (Entity)null, (Entity)null, (Vec3)null);
   }

   @Nullable
   public Entity getDirectEntity() {
      return this.directEntity;
   }

   @Nullable
   public Entity getEntity() {
      return this.causingEntity;
   }

   public Component getLocalizedDeathMessage(LivingEntity livingentity) {
      String s = "death.attack." + this.type().msgId();
      if (this.causingEntity == null && this.directEntity == null) {
         LivingEntity livingentity2 = livingentity.getKillCredit();
         String s1 = s + ".player";
         return livingentity2 != null ? Component.translatable(s1, livingentity.getDisplayName(), livingentity2.getDisplayName()) : Component.translatable(s, livingentity.getDisplayName());
      } else {
         Component component = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
         Entity var6 = this.causingEntity;
         ItemStack var10000;
         if (var6 instanceof LivingEntity) {
            LivingEntity livingentity1 = (LivingEntity)var6;
            var10000 = livingentity1.getMainHandItem();
         } else {
            var10000 = ItemStack.EMPTY;
         }

         ItemStack itemstack = var10000;
         return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ? Component.translatable(s + ".item", livingentity.getDisplayName(), component, itemstack.getDisplayName()) : Component.translatable(s, livingentity.getDisplayName(), component);
      }
   }

   public String getMsgId() {
      return this.type().msgId();
   }

   public boolean scalesWithDifficulty() {
      boolean var10000;
      switch (this.type().scaling()) {
         case NEVER:
            var10000 = false;
            break;
         case WHEN_CAUSED_BY_LIVING_NON_PLAYER:
            var10000 = this.causingEntity instanceof LivingEntity && !(this.causingEntity instanceof Player);
            break;
         case ALWAYS:
            var10000 = true;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isCreativePlayer() {
      Entity var2 = this.getEntity();
      if (var2 instanceof Player player) {
         if (player.getAbilities().instabuild) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public Vec3 getSourcePosition() {
      if (this.damageSourcePosition != null) {
         return this.damageSourcePosition;
      } else {
         return this.directEntity != null ? this.directEntity.position() : null;
      }
   }

   @Nullable
   public Vec3 sourcePositionRaw() {
      return this.damageSourcePosition;
   }

   public boolean is(TagKey<DamageType> tagkey) {
      return this.type.is(tagkey);
   }

   public boolean is(ResourceKey<DamageType> resourcekey) {
      return this.type.is(resourcekey);
   }

   public DamageType type() {
      return this.type.value();
   }

   public Holder<DamageType> typeHolder() {
      return this.type;
   }
}
