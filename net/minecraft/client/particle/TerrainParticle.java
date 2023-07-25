package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TerrainParticle extends TextureSheetParticle {
   private final BlockPos pos;
   private final float uo;
   private final float vo;

   public TerrainParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, BlockState blockstate) {
      this(clientlevel, d0, d1, d2, d3, d4, d5, blockstate, BlockPos.containing(d0, d1, d2));
   }

   public TerrainParticle(ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5, BlockState blockstate, BlockPos blockpos) {
      super(clientlevel, d0, d1, d2, d3, d4, d5);
      this.pos = blockpos;
      this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockstate));
      this.gravity = 1.0F;
      this.rCol = 0.6F;
      this.gCol = 0.6F;
      this.bCol = 0.6F;
      if (!blockstate.is(Blocks.GRASS_BLOCK)) {
         int i = Minecraft.getInstance().getBlockColors().getColor(blockstate, clientlevel, blockpos, 0);
         this.rCol *= (float)(i >> 16 & 255) / 255.0F;
         this.gCol *= (float)(i >> 8 & 255) / 255.0F;
         this.bCol *= (float)(i & 255) / 255.0F;
      }

      this.quadSize /= 2.0F;
      this.uo = this.random.nextFloat() * 3.0F;
      this.vo = this.random.nextFloat() * 3.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.TERRAIN_SHEET;
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

   public int getLightColor(float f) {
      int i = super.getLightColor(f);
      return i == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : i;
   }

   public static class Provider implements ParticleProvider<BlockParticleOption> {
      public Particle createParticle(BlockParticleOption blockparticleoption, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         BlockState blockstate = blockparticleoption.getState();
         return !blockstate.isAir() && !blockstate.is(Blocks.MOVING_PISTON) ? new TerrainParticle(clientlevel, d0, d1, d2, d3, d4, d5, blockstate) : null;
      }
   }
}
