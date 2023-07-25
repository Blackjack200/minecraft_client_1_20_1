package net.minecraft.client.renderer.blockentity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BrightnessCombiner<S extends BlockEntity> implements DoubleBlockCombiner.Combiner<S, Int2IntFunction> {
   public Int2IntFunction acceptDouble(S blockentity, S blockentity1) {
      return (i) -> {
         int j = LevelRenderer.getLightColor(blockentity.getLevel(), blockentity.getBlockPos());
         int k = LevelRenderer.getLightColor(blockentity1.getLevel(), blockentity1.getBlockPos());
         int l = LightTexture.block(j);
         int i1 = LightTexture.block(k);
         int j1 = LightTexture.sky(j);
         int k1 = LightTexture.sky(k);
         return LightTexture.pack(Math.max(l, i1), Math.max(j1, k1));
      };
   }

   public Int2IntFunction acceptSingle(S blockentity) {
      return (i) -> i;
   }

   public Int2IntFunction acceptNone() {
      return (i) -> i;
   }
}
