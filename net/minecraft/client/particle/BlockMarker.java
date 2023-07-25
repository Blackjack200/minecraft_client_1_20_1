package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMarker extends TextureSheetParticle {
   BlockMarker(ClientLevel clientlevel, double d0, double d1, double d2, BlockState blockstate) {
      super(clientlevel, d0, d1, d2);
      this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockstate));
      this.gravity = 0.0F;
      this.lifetime = 80;
      this.hasPhysics = false;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.TERRAIN_SHEET;
   }

   public float getQuadSize(float f) {
      return 0.5F;
   }

   public static class Provider implements ParticleProvider<BlockParticleOption> {
      public Particle createParticle(BlockParticleOption blockparticleoption, ClientLevel clientlevel, double d0, double d1, double d2, double d3, double d4, double d5) {
         return new BlockMarker(clientlevel, d0, d1, d2, blockparticleoption.getState());
      }
   }
}
