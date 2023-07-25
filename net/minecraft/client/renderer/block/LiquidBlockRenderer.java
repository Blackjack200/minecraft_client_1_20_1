package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlockRenderer {
   private static final float MAX_FLUID_HEIGHT = 0.8888889F;
   private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
   private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
   private TextureAtlasSprite waterOverlay;

   protected void setupSprites() {
      this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
      this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
      this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
      this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
      this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
   }

   private static boolean isNeighborSameFluid(FluidState fluidstate, FluidState fluidstate1) {
      return fluidstate1.getType().isSame(fluidstate.getType());
   }

   private static boolean isFaceOccludedByState(BlockGetter blockgetter, Direction direction, float f, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.canOcclude()) {
         VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)f, 1.0D);
         VoxelShape voxelshape1 = blockstate.getOcclusionShape(blockgetter, blockpos);
         return Shapes.blockOccudes(voxelshape, voxelshape1, direction);
      } else {
         return false;
      }
   }

   private static boolean isFaceOccludedByNeighbor(BlockGetter blockgetter, BlockPos blockpos, Direction direction, float f, BlockState blockstate) {
      return isFaceOccludedByState(blockgetter, direction, f, blockpos.relative(direction), blockstate);
   }

   private static boolean isFaceOccludedBySelf(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Direction direction) {
      return isFaceOccludedByState(blockgetter, direction.getOpposite(), 1.0F, blockpos, blockstate);
   }

   public static boolean shouldRenderFace(BlockAndTintGetter blockandtintgetter, BlockPos blockpos, FluidState fluidstate, BlockState blockstate, Direction direction, FluidState fluidstate1) {
      return !isFaceOccludedBySelf(blockandtintgetter, blockpos, blockstate, direction) && !isNeighborSameFluid(fluidstate, fluidstate1);
   }

   public void tesselate(BlockAndTintGetter blockandtintgetter, BlockPos blockpos, VertexConsumer vertexconsumer, BlockState blockstate, FluidState fluidstate) {
      boolean flag = fluidstate.is(FluidTags.LAVA);
      TextureAtlasSprite[] atextureatlassprite = flag ? this.lavaIcons : this.waterIcons;
      int i = flag ? 16777215 : BiomeColors.getAverageWaterColor(blockandtintgetter, blockpos);
      float f = (float)(i >> 16 & 255) / 255.0F;
      float f1 = (float)(i >> 8 & 255) / 255.0F;
      float f2 = (float)(i & 255) / 255.0F;
      BlockState blockstate1 = blockandtintgetter.getBlockState(blockpos.relative(Direction.DOWN));
      FluidState fluidstate1 = blockstate1.getFluidState();
      BlockState blockstate2 = blockandtintgetter.getBlockState(blockpos.relative(Direction.UP));
      FluidState fluidstate2 = blockstate2.getFluidState();
      BlockState blockstate3 = blockandtintgetter.getBlockState(blockpos.relative(Direction.NORTH));
      FluidState fluidstate3 = blockstate3.getFluidState();
      BlockState blockstate4 = blockandtintgetter.getBlockState(blockpos.relative(Direction.SOUTH));
      FluidState fluidstate4 = blockstate4.getFluidState();
      BlockState blockstate5 = blockandtintgetter.getBlockState(blockpos.relative(Direction.WEST));
      FluidState fluidstate5 = blockstate5.getFluidState();
      BlockState blockstate6 = blockandtintgetter.getBlockState(blockpos.relative(Direction.EAST));
      FluidState fluidstate6 = blockstate6.getFluidState();
      boolean flag1 = !isNeighborSameFluid(fluidstate, fluidstate2);
      boolean flag2 = shouldRenderFace(blockandtintgetter, blockpos, fluidstate, blockstate, Direction.DOWN, fluidstate1) && !isFaceOccludedByNeighbor(blockandtintgetter, blockpos, Direction.DOWN, 0.8888889F, blockstate1);
      boolean flag3 = shouldRenderFace(blockandtintgetter, blockpos, fluidstate, blockstate, Direction.NORTH, fluidstate3);
      boolean flag4 = shouldRenderFace(blockandtintgetter, blockpos, fluidstate, blockstate, Direction.SOUTH, fluidstate4);
      boolean flag5 = shouldRenderFace(blockandtintgetter, blockpos, fluidstate, blockstate, Direction.WEST, fluidstate5);
      boolean flag6 = shouldRenderFace(blockandtintgetter, blockpos, fluidstate, blockstate, Direction.EAST, fluidstate6);
      if (flag1 || flag2 || flag6 || flag5 || flag3 || flag4) {
         float f3 = blockandtintgetter.getShade(Direction.DOWN, true);
         float f4 = blockandtintgetter.getShade(Direction.UP, true);
         float f5 = blockandtintgetter.getShade(Direction.NORTH, true);
         float f6 = blockandtintgetter.getShade(Direction.WEST, true);
         Fluid fluid = fluidstate.getType();
         float f7 = this.getHeight(blockandtintgetter, fluid, blockpos, blockstate, fluidstate);
         float f8;
         float f9;
         float f10;
         float f11;
         if (f7 >= 1.0F) {
            f8 = 1.0F;
            f9 = 1.0F;
            f10 = 1.0F;
            f11 = 1.0F;
         } else {
            float f12 = this.getHeight(blockandtintgetter, fluid, blockpos.north(), blockstate3, fluidstate3);
            float f13 = this.getHeight(blockandtintgetter, fluid, blockpos.south(), blockstate4, fluidstate4);
            float f14 = this.getHeight(blockandtintgetter, fluid, blockpos.east(), blockstate6, fluidstate6);
            float f15 = this.getHeight(blockandtintgetter, fluid, blockpos.west(), blockstate5, fluidstate5);
            f8 = this.calculateAverageHeight(blockandtintgetter, fluid, f7, f12, f14, blockpos.relative(Direction.NORTH).relative(Direction.EAST));
            f9 = this.calculateAverageHeight(blockandtintgetter, fluid, f7, f12, f15, blockpos.relative(Direction.NORTH).relative(Direction.WEST));
            f10 = this.calculateAverageHeight(blockandtintgetter, fluid, f7, f13, f14, blockpos.relative(Direction.SOUTH).relative(Direction.EAST));
            f11 = this.calculateAverageHeight(blockandtintgetter, fluid, f7, f13, f15, blockpos.relative(Direction.SOUTH).relative(Direction.WEST));
         }

         double d0 = (double)(blockpos.getX() & 15);
         double d1 = (double)(blockpos.getY() & 15);
         double d2 = (double)(blockpos.getZ() & 15);
         float f20 = 0.001F;
         float f21 = flag2 ? 0.001F : 0.0F;
         if (flag1 && !isFaceOccludedByNeighbor(blockandtintgetter, blockpos, Direction.UP, Math.min(Math.min(f9, f11), Math.min(f10, f8)), blockstate2)) {
            f9 -= 0.001F;
            f11 -= 0.001F;
            f10 -= 0.001F;
            f8 -= 0.001F;
            Vec3 vec3 = fluidstate.getFlow(blockandtintgetter, blockpos);
            float f22;
            float f24;
            float f26;
            float f28;
            float f23;
            float f25;
            float f27;
            float f29;
            if (vec3.x == 0.0D && vec3.z == 0.0D) {
               TextureAtlasSprite textureatlassprite = atextureatlassprite[0];
               f22 = textureatlassprite.getU(0.0D);
               f23 = textureatlassprite.getV(0.0D);
               f24 = f22;
               f25 = textureatlassprite.getV(16.0D);
               f26 = textureatlassprite.getU(16.0D);
               f27 = f25;
               f28 = f26;
               f29 = f23;
            } else {
               TextureAtlasSprite textureatlassprite1 = atextureatlassprite[1];
               float f30 = (float)Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
               float f31 = Mth.sin(f30) * 0.25F;
               float f32 = Mth.cos(f30) * 0.25F;
               float f33 = 8.0F;
               f22 = textureatlassprite1.getU((double)(8.0F + (-f32 - f31) * 16.0F));
               f23 = textureatlassprite1.getV((double)(8.0F + (-f32 + f31) * 16.0F));
               f24 = textureatlassprite1.getU((double)(8.0F + (-f32 + f31) * 16.0F));
               f25 = textureatlassprite1.getV((double)(8.0F + (f32 + f31) * 16.0F));
               f26 = textureatlassprite1.getU((double)(8.0F + (f32 + f31) * 16.0F));
               f27 = textureatlassprite1.getV((double)(8.0F + (f32 - f31) * 16.0F));
               f28 = textureatlassprite1.getU((double)(8.0F + (f32 - f31) * 16.0F));
               f29 = textureatlassprite1.getV((double)(8.0F + (-f32 - f31) * 16.0F));
            }

            float f42 = (f22 + f24 + f26 + f28) / 4.0F;
            float f43 = (f23 + f25 + f27 + f29) / 4.0F;
            float f44 = atextureatlassprite[0].uvShrinkRatio();
            f22 = Mth.lerp(f44, f22, f42);
            f24 = Mth.lerp(f44, f24, f42);
            f26 = Mth.lerp(f44, f26, f42);
            f28 = Mth.lerp(f44, f28, f42);
            f23 = Mth.lerp(f44, f23, f43);
            f25 = Mth.lerp(f44, f25, f43);
            f27 = Mth.lerp(f44, f27, f43);
            f29 = Mth.lerp(f44, f29, f43);
            int j = this.getLightColor(blockandtintgetter, blockpos);
            float f45 = f4 * f;
            float f46 = f4 * f1;
            float f47 = f4 * f2;
            this.vertex(vertexconsumer, d0 + 0.0D, d1 + (double)f9, d2 + 0.0D, f45, f46, f47, f22, f23, j);
            this.vertex(vertexconsumer, d0 + 0.0D, d1 + (double)f11, d2 + 1.0D, f45, f46, f47, f24, f25, j);
            this.vertex(vertexconsumer, d0 + 1.0D, d1 + (double)f10, d2 + 1.0D, f45, f46, f47, f26, f27, j);
            this.vertex(vertexconsumer, d0 + 1.0D, d1 + (double)f8, d2 + 0.0D, f45, f46, f47, f28, f29, j);
            if (fluidstate.shouldRenderBackwardUpFace(blockandtintgetter, blockpos.above())) {
               this.vertex(vertexconsumer, d0 + 0.0D, d1 + (double)f9, d2 + 0.0D, f45, f46, f47, f22, f23, j);
               this.vertex(vertexconsumer, d0 + 1.0D, d1 + (double)f8, d2 + 0.0D, f45, f46, f47, f28, f29, j);
               this.vertex(vertexconsumer, d0 + 1.0D, d1 + (double)f10, d2 + 1.0D, f45, f46, f47, f26, f27, j);
               this.vertex(vertexconsumer, d0 + 0.0D, d1 + (double)f11, d2 + 1.0D, f45, f46, f47, f24, f25, j);
            }
         }

         if (flag2) {
            float f48 = atextureatlassprite[0].getU0();
            float f49 = atextureatlassprite[0].getU1();
            float f50 = atextureatlassprite[0].getV0();
            float f51 = atextureatlassprite[0].getV1();
            int k = this.getLightColor(blockandtintgetter, blockpos.below());
            float f52 = f3 * f;
            float f53 = f3 * f1;
            float f54 = f3 * f2;
            this.vertex(vertexconsumer, d0, d1 + (double)f21, d2 + 1.0D, f52, f53, f54, f48, f51, k);
            this.vertex(vertexconsumer, d0, d1 + (double)f21, d2, f52, f53, f54, f48, f50, k);
            this.vertex(vertexconsumer, d0 + 1.0D, d1 + (double)f21, d2, f52, f53, f54, f49, f50, k);
            this.vertex(vertexconsumer, d0 + 1.0D, d1 + (double)f21, d2 + 1.0D, f52, f53, f54, f49, f51, k);
         }

         int l = this.getLightColor(blockandtintgetter, blockpos);

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            float f55;
            float f56;
            double d3;
            double d5;
            double d4;
            double d6;
            boolean flag7;
            switch (direction) {
               case NORTH:
                  f55 = f9;
                  f56 = f8;
                  d3 = d0;
                  d4 = d0 + 1.0D;
                  d5 = d2 + (double)0.001F;
                  d6 = d2 + (double)0.001F;
                  flag7 = flag3;
                  break;
               case SOUTH:
                  f55 = f10;
                  f56 = f11;
                  d3 = d0 + 1.0D;
                  d4 = d0;
                  d5 = d2 + 1.0D - (double)0.001F;
                  d6 = d2 + 1.0D - (double)0.001F;
                  flag7 = flag4;
                  break;
               case WEST:
                  f55 = f11;
                  f56 = f9;
                  d3 = d0 + (double)0.001F;
                  d4 = d0 + (double)0.001F;
                  d5 = d2 + 1.0D;
                  d6 = d2;
                  flag7 = flag5;
                  break;
               default:
                  f55 = f8;
                  f56 = f10;
                  d3 = d0 + 1.0D - (double)0.001F;
                  d4 = d0 + 1.0D - (double)0.001F;
                  d5 = d2;
                  d6 = d2 + 1.0D;
                  flag7 = flag6;
            }

            if (flag7 && !isFaceOccludedByNeighbor(blockandtintgetter, blockpos, direction, Math.max(f55, f56), blockandtintgetter.getBlockState(blockpos.relative(direction)))) {
               BlockPos blockpos1 = blockpos.relative(direction);
               TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
               if (!flag) {
                  Block block = blockandtintgetter.getBlockState(blockpos1).getBlock();
                  if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
                     textureatlassprite2 = this.waterOverlay;
                  }
               }

               float f63 = textureatlassprite2.getU(0.0D);
               float f64 = textureatlassprite2.getU(8.0D);
               float f65 = textureatlassprite2.getV((double)((1.0F - f55) * 16.0F * 0.5F));
               float f66 = textureatlassprite2.getV((double)((1.0F - f56) * 16.0F * 0.5F));
               float f67 = textureatlassprite2.getV(8.0D);
               float f68 = direction.getAxis() == Direction.Axis.Z ? f5 : f6;
               float f69 = f4 * f68 * f;
               float f70 = f4 * f68 * f1;
               float f71 = f4 * f68 * f2;
               this.vertex(vertexconsumer, d3, d1 + (double)f55, d5, f69, f70, f71, f63, f65, l);
               this.vertex(vertexconsumer, d4, d1 + (double)f56, d6, f69, f70, f71, f64, f66, l);
               this.vertex(vertexconsumer, d4, d1 + (double)f21, d6, f69, f70, f71, f64, f67, l);
               this.vertex(vertexconsumer, d3, d1 + (double)f21, d5, f69, f70, f71, f63, f67, l);
               if (textureatlassprite2 != this.waterOverlay) {
                  this.vertex(vertexconsumer, d3, d1 + (double)f21, d5, f69, f70, f71, f63, f67, l);
                  this.vertex(vertexconsumer, d4, d1 + (double)f21, d6, f69, f70, f71, f64, f67, l);
                  this.vertex(vertexconsumer, d4, d1 + (double)f56, d6, f69, f70, f71, f64, f66, l);
                  this.vertex(vertexconsumer, d3, d1 + (double)f55, d5, f69, f70, f71, f63, f65, l);
               }
            }
         }

      }
   }

   private float calculateAverageHeight(BlockAndTintGetter blockandtintgetter, Fluid fluid, float f, float f1, float f2, BlockPos blockpos) {
      if (!(f2 >= 1.0F) && !(f1 >= 1.0F)) {
         float[] afloat = new float[2];
         if (f2 > 0.0F || f1 > 0.0F) {
            float f3 = this.getHeight(blockandtintgetter, fluid, blockpos);
            if (f3 >= 1.0F) {
               return 1.0F;
            }

            this.addWeightedHeight(afloat, f3);
         }

         this.addWeightedHeight(afloat, f);
         this.addWeightedHeight(afloat, f2);
         this.addWeightedHeight(afloat, f1);
         return afloat[0] / afloat[1];
      } else {
         return 1.0F;
      }
   }

   private void addWeightedHeight(float[] afloat, float f) {
      if (f >= 0.8F) {
         afloat[0] += f * 10.0F;
         afloat[1] += 10.0F;
      } else if (f >= 0.0F) {
         afloat[0] += f;
         int var10002 = afloat[1]++;
      }

   }

   private float getHeight(BlockAndTintGetter blockandtintgetter, Fluid fluid, BlockPos blockpos) {
      BlockState blockstate = blockandtintgetter.getBlockState(blockpos);
      return this.getHeight(blockandtintgetter, fluid, blockpos, blockstate, blockstate.getFluidState());
   }

   private float getHeight(BlockAndTintGetter blockandtintgetter, Fluid fluid, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      if (fluid.isSame(fluidstate.getType())) {
         BlockState blockstate1 = blockandtintgetter.getBlockState(blockpos.above());
         return fluid.isSame(blockstate1.getFluidState().getType()) ? 1.0F : fluidstate.getOwnHeight();
      } else {
         return !blockstate.isSolid() ? 0.0F : -1.0F;
      }
   }

   private void vertex(VertexConsumer vertexconsumer, double d0, double d1, double d2, float f, float f1, float f2, float f3, float f4, int i) {
      vertexconsumer.vertex(d0, d1, d2).color(f, f1, f2, 1.0F).uv(f3, f4).uv2(i).normal(0.0F, 1.0F, 0.0F).endVertex();
   }

   private int getLightColor(BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
      int i = LevelRenderer.getLightColor(blockandtintgetter, blockpos);
      int j = LevelRenderer.getLightColor(blockandtintgetter, blockpos.above());
      int k = i & 255;
      int l = j & 255;
      int i1 = i >> 16 & 255;
      int j1 = j >> 16 & 255;
      return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
   }
}
