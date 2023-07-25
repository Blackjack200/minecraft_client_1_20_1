package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BreakingItemParticle extends TextureSheetParticle {
   private final float uo;
   private final float vo;

   BreakingItemParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, ItemStack itemstack) {
      this(clientlevel, d0, d1, d2, itemstack);
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += d3;
      this.yd += d4;
      this.zd += d5;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.TERRAIN_SHEET;
   }

   protected BreakingItemParticle(ClientLevel clientlevel, double d0, double d1, double d2, ItemStack itemstack) {
      super(clientlevel, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(itemstack, clientlevel, (LivingEntity)null, 0).getParticleIcon());
      this.gravity = 1.0F;
      this.quadSize /= 2.0F;
      this.uo = this.random.nextFloat() * 3.0F;
      this.vo = this.random.nextFloat() * 3.0F;
   }

   protected float getU0() {
      return this.sprite.getU((double)((this.uo + 1.0F) / 4.0F * 16.0F));
   }

   protected float getU1() {
      return this.sprite.getU((double)(this.uo / 4.0F * 16.0F));
   }

   protected float getV0() {
      return this.sprite.getV((double)(this.vo / 4.0F * 16.0F));
   }

   protected float getV1() {
      return this.sprite.getV((double)((this.vo + 1.0F) / 4.0F * 16.0F));
   }

   public static class Provider implements ParticleProvider<ItemParticleOption> {
      public Particle createParticle(ItemParticleOption itemparticleoption, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new BreakingItemParticle(clientlevel, d0, d1, d2, d3, d4, d5, itemparticleoption.getItem());
      }
   }

   public static class SlimeProvider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new BreakingItemParticle(clientlevel, d0, d1, d2, new ItemStack(Items.SLIME_BALL));
      }
   }

   public static class SnowballProvider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType simpleparticletype, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new BreakingItemParticle(clientlevel, d0, d1, d2, new ItemStack(Items.SNOWBALL));
      }
   }
}
