package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ModelBlockRenderer {
   private static final int FACE_CUBIC = 0;
   private static final int FACE_PARTIAL = 1;
   static final Direction[] DIRECTIONS = Direction.values();
   private final BlockColors blockColors;
   private static final int CACHE_SIZE = 100;
   static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(ModelBlockRenderer.Cache::new);

   public ModelBlockRenderer(BlockColors blockcolors) {
      this.blockColors = blockcolors;
   }

   public void tesselateBlock(BlockAndTintGetter blockandtintgetter, BakedModel bakedmodel, BlockState blockstate, BlockPos blockpos, PoseStack posestack, VertexConsumer vertexconsumer, boolean flag, RandomSource randomsource, long i, int j) {
      boolean flag1 = Minecraft.useAmbientOcclusion() && blockstate.getLightEmission() == 0 && bakedmodel.useAmbientOcclusion();
      Vec3 vec3 = blockstate.getOffset(blockandtintgetter, blockpos);
      posestack.translate(vec3.x, vec3.y, vec3.z);

      try {
         if (flag1) {
            this.tesselateWithAO(blockandtintgetter, bakedmodel, blockstate, blockpos, posestack, vertexconsumer, flag, randomsource, i, j);
         } else {
            this.tesselateWithoutAO(blockandtintgetter, bakedmodel, blockstate, blockpos, posestack, vertexconsumer, flag, randomsource, i, j);
         }

      } catch (Throwable var17) {
         CrashReport crashreport = CrashReport.forThrowable(var17, "Tesselating block model");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block model being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, blockandtintgetter, blockpos, blockstate);
         crashreportcategory.setDetail("Using AO", flag1);
         throw new ReportedException(crashreport);
      }
   }

   public void tesselateWithAO(BlockAndTintGetter blockandtintgetter, BakedModel bakedmodel, BlockState blockstate, BlockPos blockpos, PoseStack posestack, VertexConsumer vertexconsumer, boolean flag, RandomSource randomsource, long i, int j) {
      float[] afloat = new float[DIRECTIONS.length * 2];
      BitSet bitset = new BitSet(3);
      ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer_ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(Direction direction : DIRECTIONS) {
         randomsource.setSeed(i);
         List<BakedQuad> list = bakedmodel.getQuads(blockstate, direction, randomsource);
         if (!list.isEmpty()) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction);
            if (!flag || Block.shouldRenderFace(blockstate, blockandtintgetter, blockpos, direction, blockpos_mutableblockpos)) {
               this.renderModelFaceAO(blockandtintgetter, blockstate, blockpos, posestack, vertexconsumer, list, afloat, bitset, modelblockrenderer_ambientocclusionface, j);
            }
         }
      }

      randomsource.setSeed(i);
      List<BakedQuad> list1 = bakedmodel.getQuads(blockstate, (Direction)null, randomsource);
      if (!list1.isEmpty()) {
         this.renderModelFaceAO(blockandtintgetter, blockstate, blockpos, posestack, vertexconsumer, list1, afloat, bitset, modelblockrenderer_ambientocclusionface, j);
      }

   }

   public void tesselateWithoutAO(BlockAndTintGetter blockandtintgetter, BakedModel bakedmodel, BlockState blockstate, BlockPos blockpos, PoseStack posestack, VertexConsumer vertexconsumer, boolean flag, RandomSource randomsource, long i, int j) {
      BitSet bitset = new BitSet(3);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(Direction direction : DIRECTIONS) {
         randomsource.setSeed(i);
         List<BakedQuad> list = bakedmodel.getQuads(blockstate, direction, randomsource);
         if (!list.isEmpty()) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction);
            if (!flag || Block.shouldRenderFace(blockstate, blockandtintgetter, blockpos, direction, blockpos_mutableblockpos)) {
               int k = LevelRenderer.getLightColor(blockandtintgetter, blockstate, blockpos_mutableblockpos);
               this.renderModelFaceFlat(blockandtintgetter, blockstate, blockpos, k, j, false, posestack, vertexconsumer, list, bitset);
            }
         }
      }

      randomsource.setSeed(i);
      List<BakedQuad> list1 = bakedmodel.getQuads(blockstate, (Direction)null, randomsource);
      if (!list1.isEmpty()) {
         this.renderModelFaceFlat(blockandtintgetter, blockstate, blockpos, -1, j, true, posestack, vertexconsumer, list1, bitset);
      }

   }

   private void renderModelFaceAO(BlockAndTintGetter blockandtintgetter, BlockState blockstate, BlockPos blockpos, PoseStack posestack, VertexConsumer vertexconsumer, List<BakedQuad> list, float[] afloat, BitSet bitset, ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer_ambientocclusionface, int i) {
      for(BakedQuad bakedquad : list) {
         this.calculateShape(blockandtintgetter, blockstate, blockpos, bakedquad.getVertices(), bakedquad.getDirection(), afloat, bitset);
         modelblockrenderer_ambientocclusionface.calculate(blockandtintgetter, blockstate, blockpos, bakedquad.getDirection(), afloat, bitset, bakedquad.isShade());
         this.putQuadData(blockandtintgetter, blockstate, blockpos, vertexconsumer, posestack.last(), bakedquad, modelblockrenderer_ambientocclusionface.brightness[0], modelblockrenderer_ambientocclusionface.brightness[1], modelblockrenderer_ambientocclusionface.brightness[2], modelblockrenderer_ambientocclusionface.brightness[3], modelblockrenderer_ambientocclusionface.lightmap[0], modelblockrenderer_ambientocclusionface.lightmap[1], modelblockrenderer_ambientocclusionface.lightmap[2], modelblockrenderer_ambientocclusionface.lightmap[3], i);
      }

   }

   private void putQuadData(BlockAndTintGetter blockandtintgetter, BlockState blockstate, BlockPos blockpos, VertexConsumer vertexconsumer, PoseStack.Pose posestack_pose, BakedQuad bakedquad, float f, float f1, float f2, float f3, int i, int j, int k, int l, int i1) {
      float f4;
      float f5;
      float f6;
      if (bakedquad.isTinted()) {
         int j1 = this.blockColors.getColor(blockstate, blockandtintgetter, blockpos, bakedquad.getTintIndex());
         f4 = (float)(j1 >> 16 & 255) / 255.0F;
         f5 = (float)(j1 >> 8 & 255) / 255.0F;
         f6 = (float)(j1 & 255) / 255.0F;
      } else {
         f4 = 1.0F;
         f5 = 1.0F;
         f6 = 1.0F;
      }

      vertexconsumer.putBulkData(posestack_pose, bakedquad, new float[]{f, f1, f2, f3}, f4, f5, f6, new int[]{i, j, k, l}, i1, true);
   }

   private void calculateShape(BlockAndTintGetter blockandtintgetter, BlockState blockstate, BlockPos blockpos, int[] aint, Direction direction, @Nullable float[] afloat, BitSet bitset) {
      float f = 32.0F;
      float f1 = 32.0F;
      float f2 = 32.0F;
      float f3 = -32.0F;
      float f4 = -32.0F;
      float f5 = -32.0F;

      for(int i = 0; i < 4; ++i) {
         float f6 = Float.intBitsToFloat(aint[i * 8]);
         float f7 = Float.intBitsToFloat(aint[i * 8 + 1]);
         float f8 = Float.intBitsToFloat(aint[i * 8 + 2]);
         f = Math.min(f, f6);
         f1 = Math.min(f1, f7);
         f2 = Math.min(f2, f8);
         f3 = Math.max(f3, f6);
         f4 = Math.max(f4, f7);
         f5 = Math.max(f5, f8);
      }

      if (afloat != null) {
         afloat[Direction.WEST.get3DDataValue()] = f;
         afloat[Direction.EAST.get3DDataValue()] = f3;
         afloat[Direction.DOWN.get3DDataValue()] = f1;
         afloat[Direction.UP.get3DDataValue()] = f4;
         afloat[Direction.NORTH.get3DDataValue()] = f2;
         afloat[Direction.SOUTH.get3DDataValue()] = f5;
         int j = DIRECTIONS.length;
         afloat[Direction.WEST.get3DDataValue() + j] = 1.0F - f;
         afloat[Direction.EAST.get3DDataValue() + j] = 1.0F - f3;
         afloat[Direction.DOWN.get3DDataValue() + j] = 1.0F - f1;
         afloat[Direction.UP.get3DDataValue() + j] = 1.0F - f4;
         afloat[Direction.NORTH.get3DDataValue() + j] = 1.0F - f2;
         afloat[Direction.SOUTH.get3DDataValue() + j] = 1.0F - f5;
      }

      float f9 = 1.0E-4F;
      float f10 = 0.9999F;
      switch (direction) {
         case DOWN:
            bitset.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            bitset.set(0, f1 == f4 && (f1 < 1.0E-4F || blockstate.isCollisionShapeFullBlock(blockandtintgetter, blockpos)));
            break;
         case UP:
            bitset.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
            bitset.set(0, f1 == f4 && (f4 > 0.9999F || blockstate.isCollisionShapeFullBlock(blockandtintgetter, blockpos)));
            break;
         case NORTH:
            bitset.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            bitset.set(0, f2 == f5 && (f2 < 1.0E-4F || blockstate.isCollisionShapeFullBlock(blockandtintgetter, blockpos)));
            break;
         case SOUTH:
            bitset.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
            bitset.set(0, f2 == f5 && (f5 > 0.9999F || blockstate.isCollisionShapeFullBlock(blockandtintgetter, blockpos)));
            break;
         case WEST:
            bitset.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            bitset.set(0, f == f3 && (f < 1.0E-4F || blockstate.isCollisionShapeFullBlock(blockandtintgetter, blockpos)));
            break;
         case EAST:
            bitset.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
            bitset.set(0, f == f3 && (f3 > 0.9999F || blockstate.isCollisionShapeFullBlock(blockandtintgetter, blockpos)));
      }

   }

   private void renderModelFaceFlat(BlockAndTintGetter blockandtintgetter, BlockState blockstate, BlockPos blockpos, int i, int j, boolean flag, PoseStack posestack, VertexConsumer vertexconsumer, List<BakedQuad> list, BitSet bitset) {
      for(BakedQuad bakedquad : list) {
         if (flag) {
            this.calculateShape(blockandtintgetter, blockstate, blockpos, bakedquad.getVertices(), bakedquad.getDirection(), (float[])null, bitset);
            BlockPos blockpos1 = bitset.get(0) ? blockpos.relative(bakedquad.getDirection()) : blockpos;
            i = LevelRenderer.getLightColor(blockandtintgetter, blockstate, blockpos1);
         }

         float f = blockandtintgetter.getShade(bakedquad.getDirection(), bakedquad.isShade());
         this.putQuadData(blockandtintgetter, blockstate, blockpos, vertexconsumer, posestack.last(), bakedquad, f, f, f, f, i, i, i, i, j);
      }

   }

   public void renderModel(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, @Nullable BlockState blockstate, BakedModel bakedmodel, float f, float f1, float f2, int i, int j) {
      RandomSource randomsource = RandomSource.create();
      long k = 42L;

      for(Direction direction : DIRECTIONS) {
         randomsource.setSeed(42L);
         renderQuadList(posestack_pose, vertexconsumer, f, f1, f2, bakedmodel.getQuads(blockstate, direction, randomsource), i, j);
      }

      randomsource.setSeed(42L);
      renderQuadList(posestack_pose, vertexconsumer, f, f1, f2, bakedmodel.getQuads(blockstate, (Direction)null, randomsource), i, j);
   }

   private static void renderQuadList(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, float f, float f1, float f2, List<BakedQuad> list, int i, int j) {
      for(BakedQuad bakedquad : list) {
         float f3;
         float f4;
         float f5;
         if (bakedquad.isTinted()) {
            f3 = Mth.clamp(f, 0.0F, 1.0F);
            f4 = Mth.clamp(f1, 0.0F, 1.0F);
            f5 = Mth.clamp(f2, 0.0F, 1.0F);
         } else {
            f3 = 1.0F;
            f4 = 1.0F;
            f5 = 1.0F;
         }

         vertexconsumer.putBulkData(posestack_pose, bakedquad, f3, f4, f5, i, j);
      }

   }

   public static void enableCaching() {
      CACHE.get().enable();
   }

   public static void clearCache() {
      CACHE.get().disable();
   }

   protected static enum AdjacencyInfo {
      DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH}),
      UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH}),
      NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST}),
      SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST}),
      WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH}),
      EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH});

      final Direction[] corners;
      final boolean doNonCubicWeight;
      final ModelBlockRenderer.SizeInfo[] vert0Weights;
      final ModelBlockRenderer.SizeInfo[] vert1Weights;
      final ModelBlockRenderer.SizeInfo[] vert2Weights;
      final ModelBlockRenderer.SizeInfo[] vert3Weights;
      private static final ModelBlockRenderer.AdjacencyInfo[] BY_FACING = Util.make(new ModelBlockRenderer.AdjacencyInfo[6], (amodelblockrenderer_adjacencyinfo) -> {
         amodelblockrenderer_adjacencyinfo[Direction.DOWN.get3DDataValue()] = DOWN;
         amodelblockrenderer_adjacencyinfo[Direction.UP.get3DDataValue()] = UP;
         amodelblockrenderer_adjacencyinfo[Direction.NORTH.get3DDataValue()] = NORTH;
         amodelblockrenderer_adjacencyinfo[Direction.SOUTH.get3DDataValue()] = SOUTH;
         amodelblockrenderer_adjacencyinfo[Direction.WEST.get3DDataValue()] = WEST;
         amodelblockrenderer_adjacencyinfo[Direction.EAST.get3DDataValue()] = EAST;
      });

      private AdjacencyInfo(Direction[] adirection, float f, boolean flag, ModelBlockRenderer.SizeInfo[] amodelblockrenderer_sizeinfo, ModelBlockRenderer.SizeInfo[] amodelblockrenderer_sizeinfo1, ModelBlockRenderer.SizeInfo[] amodelblockrenderer_sizeinfo2, ModelBlockRenderer.SizeInfo[] amodelblockrenderer_sizeinfo3) {
         this.corners = adirection;
         this.doNonCubicWeight = flag;
         this.vert0Weights = amodelblockrenderer_sizeinfo;
         this.vert1Weights = amodelblockrenderer_sizeinfo1;
         this.vert2Weights = amodelblockrenderer_sizeinfo2;
         this.vert3Weights = amodelblockrenderer_sizeinfo3;
      }

      public static ModelBlockRenderer.AdjacencyInfo fromFacing(Direction direction) {
         return BY_FACING[direction.get3DDataValue()];
      }
   }

   static class AmbientOcclusionFace {
      final float[] brightness = new float[4];
      final int[] lightmap = new int[4];

      public AmbientOcclusionFace() {
      }

      public void calculate(BlockAndTintGetter blockandtintgetter, BlockState blockstate, BlockPos blockpos, Direction direction, float[] afloat, BitSet bitset, boolean flag) {
         BlockPos blockpos1 = bitset.get(0) ? blockpos.relative(direction) : blockpos;
         ModelBlockRenderer.AdjacencyInfo modelblockrenderer_adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
         ModelBlockRenderer.Cache modelblockrenderer_cache = ModelBlockRenderer.CACHE.get();
         blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[0]);
         BlockState blockstate1 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
         int i = modelblockrenderer_cache.getLightColor(blockstate1, blockandtintgetter, blockpos_mutableblockpos);
         float f = modelblockrenderer_cache.getShadeBrightness(blockstate1, blockandtintgetter, blockpos_mutableblockpos);
         blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[1]);
         BlockState blockstate2 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
         int j = modelblockrenderer_cache.getLightColor(blockstate2, blockandtintgetter, blockpos_mutableblockpos);
         float f1 = modelblockrenderer_cache.getShadeBrightness(blockstate2, blockandtintgetter, blockpos_mutableblockpos);
         blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[2]);
         BlockState blockstate3 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
         int k = modelblockrenderer_cache.getLightColor(blockstate3, blockandtintgetter, blockpos_mutableblockpos);
         float f2 = modelblockrenderer_cache.getShadeBrightness(blockstate3, blockandtintgetter, blockpos_mutableblockpos);
         blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[3]);
         BlockState blockstate4 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
         int l = modelblockrenderer_cache.getLightColor(blockstate4, blockandtintgetter, blockpos_mutableblockpos);
         float f3 = modelblockrenderer_cache.getShadeBrightness(blockstate4, blockandtintgetter, blockpos_mutableblockpos);
         BlockState blockstate5 = blockandtintgetter.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[0]).move(direction));
         boolean flag1 = !blockstate5.isViewBlocking(blockandtintgetter, blockpos_mutableblockpos) || blockstate5.getLightBlock(blockandtintgetter, blockpos_mutableblockpos) == 0;
         BlockState blockstate6 = blockandtintgetter.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[1]).move(direction));
         boolean flag2 = !blockstate6.isViewBlocking(blockandtintgetter, blockpos_mutableblockpos) || blockstate6.getLightBlock(blockandtintgetter, blockpos_mutableblockpos) == 0;
         BlockState blockstate7 = blockandtintgetter.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[2]).move(direction));
         boolean flag3 = !blockstate7.isViewBlocking(blockandtintgetter, blockpos_mutableblockpos) || blockstate7.getLightBlock(blockandtintgetter, blockpos_mutableblockpos) == 0;
         BlockState blockstate8 = blockandtintgetter.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[3]).move(direction));
         boolean flag4 = !blockstate8.isViewBlocking(blockandtintgetter, blockpos_mutableblockpos) || blockstate8.getLightBlock(blockandtintgetter, blockpos_mutableblockpos) == 0;
         float f5;
         int j1;
         if (!flag3 && !flag1) {
            f5 = f;
            j1 = i;
         } else {
            blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[0]).move(modelblockrenderer_adjacencyinfo.corners[2]);
            BlockState blockstate9 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
            f5 = modelblockrenderer_cache.getShadeBrightness(blockstate9, blockandtintgetter, blockpos_mutableblockpos);
            j1 = modelblockrenderer_cache.getLightColor(blockstate9, blockandtintgetter, blockpos_mutableblockpos);
         }

         float f7;
         int l1;
         if (!flag4 && !flag1) {
            f7 = f;
            l1 = i;
         } else {
            blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[0]).move(modelblockrenderer_adjacencyinfo.corners[3]);
            BlockState blockstate10 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
            f7 = modelblockrenderer_cache.getShadeBrightness(blockstate10, blockandtintgetter, blockpos_mutableblockpos);
            l1 = modelblockrenderer_cache.getLightColor(blockstate10, blockandtintgetter, blockpos_mutableblockpos);
         }

         float f9;
         int j2;
         if (!flag3 && !flag2) {
            f9 = f;
            j2 = i;
         } else {
            blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[1]).move(modelblockrenderer_adjacencyinfo.corners[2]);
            BlockState blockstate11 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
            f9 = modelblockrenderer_cache.getShadeBrightness(blockstate11, blockandtintgetter, blockpos_mutableblockpos);
            j2 = modelblockrenderer_cache.getLightColor(blockstate11, blockandtintgetter, blockpos_mutableblockpos);
         }

         float f11;
         int l2;
         if (!flag4 && !flag2) {
            f11 = f;
            l2 = i;
         } else {
            blockpos_mutableblockpos.setWithOffset(blockpos1, modelblockrenderer_adjacencyinfo.corners[1]).move(modelblockrenderer_adjacencyinfo.corners[3]);
            BlockState blockstate12 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
            f11 = modelblockrenderer_cache.getShadeBrightness(blockstate12, blockandtintgetter, blockpos_mutableblockpos);
            l2 = modelblockrenderer_cache.getLightColor(blockstate12, blockandtintgetter, blockpos_mutableblockpos);
         }

         int i3 = modelblockrenderer_cache.getLightColor(blockstate, blockandtintgetter, blockpos);
         blockpos_mutableblockpos.setWithOffset(blockpos, direction);
         BlockState blockstate13 = blockandtintgetter.getBlockState(blockpos_mutableblockpos);
         if (bitset.get(0) || !blockstate13.isSolidRender(blockandtintgetter, blockpos_mutableblockpos)) {
            i3 = modelblockrenderer_cache.getLightColor(blockstate13, blockandtintgetter, blockpos_mutableblockpos);
         }

         float f12 = bitset.get(0) ? modelblockrenderer_cache.getShadeBrightness(blockandtintgetter.getBlockState(blockpos1), blockandtintgetter, blockpos1) : modelblockrenderer_cache.getShadeBrightness(blockandtintgetter.getBlockState(blockpos), blockandtintgetter, blockpos);
         ModelBlockRenderer.AmbientVertexRemap modelblockrenderer_ambientvertexremap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(direction);
         if (bitset.get(1) && modelblockrenderer_adjacencyinfo.doNonCubicWeight) {
            float f17 = (f3 + f + f7 + f12) * 0.25F;
            float f18 = (f2 + f + f5 + f12) * 0.25F;
            float f19 = (f2 + f1 + f9 + f12) * 0.25F;
            float f20 = (f3 + f1 + f11 + f12) * 0.25F;
            float f21 = afloat[modelblockrenderer_adjacencyinfo.vert0Weights[0].shape] * afloat[modelblockrenderer_adjacencyinfo.vert0Weights[1].shape];
            float f22 = afloat[modelblockrenderer_adjacencyinfo.vert0Weights[2].shape] * afloat[modelblockrenderer_adjacencyinfo.vert0Weights[3].shape];
            float f23 = afloat[modelblockrenderer_adjacencyinfo.vert0Weights[4].shape] * afloat[modelblockrenderer_adjacencyinfo.vert0Weights[5].shape];
            float f24 = afloat[modelblockrenderer_adjacencyinfo.vert0Weights[6].shape] * afloat[modelblockrenderer_adjacencyinfo.vert0Weights[7].shape];
            float f25 = afloat[modelblockrenderer_adjacencyinfo.vert1Weights[0].shape] * afloat[modelblockrenderer_adjacencyinfo.vert1Weights[1].shape];
            float f26 = afloat[modelblockrenderer_adjacencyinfo.vert1Weights[2].shape] * afloat[modelblockrenderer_adjacencyinfo.vert1Weights[3].shape];
            float f27 = afloat[modelblockrenderer_adjacencyinfo.vert1Weights[4].shape] * afloat[modelblockrenderer_adjacencyinfo.vert1Weights[5].shape];
            float f28 = afloat[modelblockrenderer_adjacencyinfo.vert1Weights[6].shape] * afloat[modelblockrenderer_adjacencyinfo.vert1Weights[7].shape];
            float f29 = afloat[modelblockrenderer_adjacencyinfo.vert2Weights[0].shape] * afloat[modelblockrenderer_adjacencyinfo.vert2Weights[1].shape];
            float f30 = afloat[modelblockrenderer_adjacencyinfo.vert2Weights[2].shape] * afloat[modelblockrenderer_adjacencyinfo.vert2Weights[3].shape];
            float f31 = afloat[modelblockrenderer_adjacencyinfo.vert2Weights[4].shape] * afloat[modelblockrenderer_adjacencyinfo.vert2Weights[5].shape];
            float f32 = afloat[modelblockrenderer_adjacencyinfo.vert2Weights[6].shape] * afloat[modelblockrenderer_adjacencyinfo.vert2Weights[7].shape];
            float f33 = afloat[modelblockrenderer_adjacencyinfo.vert3Weights[0].shape] * afloat[modelblockrenderer_adjacencyinfo.vert3Weights[1].shape];
            float f34 = afloat[modelblockrenderer_adjacencyinfo.vert3Weights[2].shape] * afloat[modelblockrenderer_adjacencyinfo.vert3Weights[3].shape];
            float f35 = afloat[modelblockrenderer_adjacencyinfo.vert3Weights[4].shape] * afloat[modelblockrenderer_adjacencyinfo.vert3Weights[5].shape];
            float f36 = afloat[modelblockrenderer_adjacencyinfo.vert3Weights[6].shape] * afloat[modelblockrenderer_adjacencyinfo.vert3Weights[7].shape];
            this.brightness[modelblockrenderer_ambientvertexremap.vert0] = f17 * f21 + f18 * f22 + f19 * f23 + f20 * f24;
            this.brightness[modelblockrenderer_ambientvertexremap.vert1] = f17 * f25 + f18 * f26 + f19 * f27 + f20 * f28;
            this.brightness[modelblockrenderer_ambientvertexremap.vert2] = f17 * f29 + f18 * f30 + f19 * f31 + f20 * f32;
            this.brightness[modelblockrenderer_ambientvertexremap.vert3] = f17 * f33 + f18 * f34 + f19 * f35 + f20 * f36;
            int j3 = this.blend(l, i, l1, i3);
            int k3 = this.blend(k, i, j1, i3);
            int l3 = this.blend(k, j, j2, i3);
            int i4 = this.blend(l, j, l2, i3);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert0] = this.blend(j3, k3, l3, i4, f21, f22, f23, f24);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert1] = this.blend(j3, k3, l3, i4, f25, f26, f27, f28);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert2] = this.blend(j3, k3, l3, i4, f29, f30, f31, f32);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert3] = this.blend(j3, k3, l3, i4, f33, f34, f35, f36);
         } else {
            float f13 = (f3 + f + f7 + f12) * 0.25F;
            float f14 = (f2 + f + f5 + f12) * 0.25F;
            float f15 = (f2 + f1 + f9 + f12) * 0.25F;
            float f16 = (f3 + f1 + f11 + f12) * 0.25F;
            this.lightmap[modelblockrenderer_ambientvertexremap.vert0] = this.blend(l, i, l1, i3);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert1] = this.blend(k, i, j1, i3);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert2] = this.blend(k, j, j2, i3);
            this.lightmap[modelblockrenderer_ambientvertexremap.vert3] = this.blend(l, j, l2, i3);
            this.brightness[modelblockrenderer_ambientvertexremap.vert0] = f13;
            this.brightness[modelblockrenderer_ambientvertexremap.vert1] = f14;
            this.brightness[modelblockrenderer_ambientvertexremap.vert2] = f15;
            this.brightness[modelblockrenderer_ambientvertexremap.vert3] = f16;
         }

         float f37 = blockandtintgetter.getShade(direction, flag);

         for(int j4 = 0; j4 < this.brightness.length; ++j4) {
            this.brightness[j4] *= f37;
         }

      }

      private int blend(int i, int j, int k, int l) {
         if (i == 0) {
            i = l;
         }

         if (j == 0) {
            j = l;
         }

         if (k == 0) {
            k = l;
         }

         return i + j + k + l >> 2 & 16711935;
      }

      private int blend(int i, int j, int k, int l, float f, float f1, float f2, float f3) {
         int i1 = (int)((float)(i >> 16 & 255) * f + (float)(j >> 16 & 255) * f1 + (float)(k >> 16 & 255) * f2 + (float)(l >> 16 & 255) * f3) & 255;
         int j1 = (int)((float)(i & 255) * f + (float)(j & 255) * f1 + (float)(k & 255) * f2 + (float)(l & 255) * f3) & 255;
         return i1 << 16 | j1;
      }
   }

   static enum AmbientVertexRemap {
      DOWN(0, 1, 2, 3),
      UP(2, 3, 0, 1),
      NORTH(3, 0, 1, 2),
      SOUTH(0, 1, 2, 3),
      WEST(3, 0, 1, 2),
      EAST(1, 2, 3, 0);

      final int vert0;
      final int vert1;
      final int vert2;
      final int vert3;
      private static final ModelBlockRenderer.AmbientVertexRemap[] BY_FACING = Util.make(new ModelBlockRenderer.AmbientVertexRemap[6], (amodelblockrenderer_ambientvertexremap) -> {
         amodelblockrenderer_ambientvertexremap[Direction.DOWN.get3DDataValue()] = DOWN;
         amodelblockrenderer_ambientvertexremap[Direction.UP.get3DDataValue()] = UP;
         amodelblockrenderer_ambientvertexremap[Direction.NORTH.get3DDataValue()] = NORTH;
         amodelblockrenderer_ambientvertexremap[Direction.SOUTH.get3DDataValue()] = SOUTH;
         amodelblockrenderer_ambientvertexremap[Direction.WEST.get3DDataValue()] = WEST;
         amodelblockrenderer_ambientvertexremap[Direction.EAST.get3DDataValue()] = EAST;
      });

      private AmbientVertexRemap(int i, int j, int k, int l) {
         this.vert0 = i;
         this.vert1 = j;
         this.vert2 = k;
         this.vert3 = l;
      }

      public static ModelBlockRenderer.AmbientVertexRemap fromFacing(Direction direction) {
         return BY_FACING[direction.get3DDataValue()];
      }
   }

   static class Cache {
      private boolean enabled;
      private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
         Long2IntLinkedOpenHashMap long2intlinkedopenhashmap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int i) {
            }
         };
         long2intlinkedopenhashmap.defaultReturnValue(Integer.MAX_VALUE);
         return long2intlinkedopenhashmap;
      });
      private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
         Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
            protected void rehash(int i) {
            }
         };
         long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
         return long2floatlinkedopenhashmap;
      });

      private Cache() {
      }

      public void enable() {
         this.enabled = true;
      }

      public void disable() {
         this.enabled = false;
         this.colorCache.clear();
         this.brightnessCache.clear();
      }

      public int getLightColor(BlockState blockstate, BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
         long i = blockpos.asLong();
         if (this.enabled) {
            int j = this.colorCache.get(i);
            if (j != Integer.MAX_VALUE) {
               return j;
            }
         }

         int k = LevelRenderer.getLightColor(blockandtintgetter, blockstate, blockpos);
         if (this.enabled) {
            if (this.colorCache.size() == 100) {
               this.colorCache.removeFirstInt();
            }

            this.colorCache.put(i, k);
         }

         return k;
      }

      public float getShadeBrightness(BlockState blockstate, BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
         long i = blockpos.asLong();
         if (this.enabled) {
            float f = this.brightnessCache.get(i);
            if (!Float.isNaN(f)) {
               return f;
            }
         }

         float f1 = blockstate.getShadeBrightness(blockandtintgetter, blockpos);
         if (this.enabled) {
            if (this.brightnessCache.size() == 100) {
               this.brightnessCache.removeFirstFloat();
            }

            this.brightnessCache.put(i, f1);
         }

         return f1;
      }
   }

   protected static enum SizeInfo {
      DOWN(Direction.DOWN, false),
      UP(Direction.UP, false),
      NORTH(Direction.NORTH, false),
      SOUTH(Direction.SOUTH, false),
      WEST(Direction.WEST, false),
      EAST(Direction.EAST, false),
      FLIP_DOWN(Direction.DOWN, true),
      FLIP_UP(Direction.UP, true),
      FLIP_NORTH(Direction.NORTH, true),
      FLIP_SOUTH(Direction.SOUTH, true),
      FLIP_WEST(Direction.WEST, true),
      FLIP_EAST(Direction.EAST, true);

      final int shape;

      private SizeInfo(Direction direction, boolean flag) {
         this.shape = direction.get3DDataValue() + (flag ? ModelBlockRenderer.DIRECTIONS.length : 0);
      }
   }
}
