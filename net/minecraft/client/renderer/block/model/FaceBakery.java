package net.minecraft.client.renderer.block.model;

import com.mojang.math.Transformation;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public class FaceBakery {
   public static final int VERTEX_INT_SIZE = 8;
   private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((double)((float)Math.PI / 8F)) - 1.0F;
   private static final float RESCALE_45 = 1.0F / (float)Math.cos((double)((float)Math.PI / 4F)) - 1.0F;
   public static final int VERTEX_COUNT = 4;
   private static final int COLOR_INDEX = 3;
   public static final int UV_INDEX = 4;

   public BakedQuad bakeQuad(Vector3f vector3f, Vector3f vector3f1, BlockElementFace blockelementface, TextureAtlasSprite textureatlassprite, Direction direction, ModelState modelstate, @Nullable BlockElementRotation blockelementrotation, boolean flag, ResourceLocation resourcelocation) {
      BlockFaceUV blockfaceuv = blockelementface.uv;
      if (modelstate.isUvLocked()) {
         blockfaceuv = recomputeUVs(blockelementface.uv, direction, modelstate.getRotation(), resourcelocation);
      }

      float[] afloat = new float[blockfaceuv.uvs.length];
      System.arraycopy(blockfaceuv.uvs, 0, afloat, 0, afloat.length);
      float f = textureatlassprite.uvShrinkRatio();
      float f1 = (blockfaceuv.uvs[0] + blockfaceuv.uvs[0] + blockfaceuv.uvs[2] + blockfaceuv.uvs[2]) / 4.0F;
      float f2 = (blockfaceuv.uvs[1] + blockfaceuv.uvs[1] + blockfaceuv.uvs[3] + blockfaceuv.uvs[3]) / 4.0F;
      blockfaceuv.uvs[0] = Mth.lerp(f, blockfaceuv.uvs[0], f1);
      blockfaceuv.uvs[2] = Mth.lerp(f, blockfaceuv.uvs[2], f1);
      blockfaceuv.uvs[1] = Mth.lerp(f, blockfaceuv.uvs[1], f2);
      blockfaceuv.uvs[3] = Mth.lerp(f, blockfaceuv.uvs[3], f2);
      int[] aint = this.makeVertices(blockfaceuv, textureatlassprite, direction, this.setupShape(vector3f, vector3f1), modelstate.getRotation(), blockelementrotation, flag);
      Direction direction1 = calculateFacing(aint);
      System.arraycopy(afloat, 0, blockfaceuv.uvs, 0, afloat.length);
      if (blockelementrotation == null) {
         this.recalculateWinding(aint, direction1);
      }

      return new BakedQuad(aint, blockelementface.tintIndex, direction1, textureatlassprite, flag);
   }

   public static BlockFaceUV recomputeUVs(BlockFaceUV blockfaceuv, Direction direction, Transformation transformation, ResourceLocation resourcelocation) {
      Matrix4f matrix4f = BlockMath.getUVLockTransform(transformation, direction, () -> "Unable to resolve UVLock for model: " + resourcelocation).getMatrix();
      float f = blockfaceuv.getU(blockfaceuv.getReverseIndex(0));
      float f1 = blockfaceuv.getV(blockfaceuv.getReverseIndex(0));
      Vector4f vector4f = matrix4f.transform(new Vector4f(f / 16.0F, f1 / 16.0F, 0.0F, 1.0F));
      float f2 = 16.0F * vector4f.x();
      float f3 = 16.0F * vector4f.y();
      float f4 = blockfaceuv.getU(blockfaceuv.getReverseIndex(2));
      float f5 = blockfaceuv.getV(blockfaceuv.getReverseIndex(2));
      Vector4f vector4f1 = matrix4f.transform(new Vector4f(f4 / 16.0F, f5 / 16.0F, 0.0F, 1.0F));
      float f6 = 16.0F * vector4f1.x();
      float f7 = 16.0F * vector4f1.y();
      float f8;
      float f9;
      if (Math.signum(f4 - f) == Math.signum(f6 - f2)) {
         f8 = f2;
         f9 = f6;
      } else {
         f8 = f6;
         f9 = f2;
      }

      float f12;
      float f13;
      if (Math.signum(f5 - f1) == Math.signum(f7 - f3)) {
         f12 = f3;
         f13 = f7;
      } else {
         f12 = f7;
         f13 = f3;
      }

      float f16 = (float)Math.toRadians((double)blockfaceuv.rotation);
      Matrix3f matrix3f = new Matrix3f(matrix4f);
      Vector3f vector3f = matrix3f.transform(new Vector3f(Mth.cos(f16), Mth.sin(f16), 0.0F));
      int i = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2((double)vector3f.y(), (double)vector3f.x())) / 90.0D)) * 90, 360);
      return new BlockFaceUV(new float[]{f8, f12, f9, f13}, i);
   }

   private int[] makeVertices(BlockFaceUV blockfaceuv, TextureAtlasSprite textureatlassprite, Direction direction, float[] afloat, Transformation transformation, @Nullable BlockElementRotation blockelementrotation, boolean flag) {
      int[] aint = new int[32];

      for(int i = 0; i < 4; ++i) {
         this.bakeVertex(aint, i, direction, blockfaceuv, afloat, textureatlassprite, transformation, blockelementrotation, flag);
      }

      return aint;
   }

   private float[] setupShape(Vector3f vector3f, Vector3f vector3f1) {
      float[] afloat = new float[Direction.values().length];
      afloat[FaceInfo.Constants.MIN_X] = vector3f.x() / 16.0F;
      afloat[FaceInfo.Constants.MIN_Y] = vector3f.y() / 16.0F;
      afloat[FaceInfo.Constants.MIN_Z] = vector3f.z() / 16.0F;
      afloat[FaceInfo.Constants.MAX_X] = vector3f1.x() / 16.0F;
      afloat[FaceInfo.Constants.MAX_Y] = vector3f1.y() / 16.0F;
      afloat[FaceInfo.Constants.MAX_Z] = vector3f1.z() / 16.0F;
      return afloat;
   }

   private void bakeVertex(int[] aint, int i, Direction direction, BlockFaceUV blockfaceuv, float[] afloat, TextureAtlasSprite textureatlassprite, Transformation transformation, @Nullable BlockElementRotation blockelementrotation, boolean flag) {
      FaceInfo.VertexInfo faceinfo_vertexinfo = FaceInfo.fromFacing(direction).getVertexInfo(i);
      Vector3f vector3f = new Vector3f(afloat[faceinfo_vertexinfo.xFace], afloat[faceinfo_vertexinfo.yFace], afloat[faceinfo_vertexinfo.zFace]);
      this.applyElementRotation(vector3f, blockelementrotation);
      this.applyModelRotation(vector3f, transformation);
      this.fillVertex(aint, i, vector3f, textureatlassprite, blockfaceuv);
   }

   private void fillVertex(int[] aint, int i, Vector3f vector3f, TextureAtlasSprite textureatlassprite, BlockFaceUV blockfaceuv) {
      int j = i * 8;
      aint[j] = Float.floatToRawIntBits(vector3f.x());
      aint[j + 1] = Float.floatToRawIntBits(vector3f.y());
      aint[j + 2] = Float.floatToRawIntBits(vector3f.z());
      aint[j + 3] = -1;
      aint[j + 4] = Float.floatToRawIntBits(textureatlassprite.getU((double)blockfaceuv.getU(i)));
      aint[j + 4 + 1] = Float.floatToRawIntBits(textureatlassprite.getV((double)blockfaceuv.getV(i)));
   }

   private void applyElementRotation(Vector3f vector3f, @Nullable BlockElementRotation blockelementrotation) {
      if (blockelementrotation != null) {
         Vector3f vector3f1;
         Vector3f vector3f2;
         switch (blockelementrotation.axis()) {
            case X:
               vector3f1 = new Vector3f(1.0F, 0.0F, 0.0F);
               vector3f2 = new Vector3f(0.0F, 1.0F, 1.0F);
               break;
            case Y:
               vector3f1 = new Vector3f(0.0F, 1.0F, 0.0F);
               vector3f2 = new Vector3f(1.0F, 0.0F, 1.0F);
               break;
            case Z:
               vector3f1 = new Vector3f(0.0F, 0.0F, 1.0F);
               vector3f2 = new Vector3f(1.0F, 1.0F, 0.0F);
               break;
            default:
               throw new IllegalArgumentException("There are only 3 axes");
         }

         Quaternionf quaternionf = (new Quaternionf()).rotationAxis(blockelementrotation.angle() * ((float)Math.PI / 180F), vector3f1);
         if (blockelementrotation.rescale()) {
            if (Math.abs(blockelementrotation.angle()) == 22.5F) {
               vector3f2.mul(RESCALE_22_5);
            } else {
               vector3f2.mul(RESCALE_45);
            }

            vector3f2.add(1.0F, 1.0F, 1.0F);
         } else {
            vector3f2.set(1.0F, 1.0F, 1.0F);
         }

         this.rotateVertexBy(vector3f, new Vector3f((Vector3fc)blockelementrotation.origin()), (new Matrix4f()).rotation(quaternionf), vector3f2);
      }
   }

   public void applyModelRotation(Vector3f vector3f, Transformation transformation) {
      if (transformation != Transformation.identity()) {
         this.rotateVertexBy(vector3f, new Vector3f(0.5F, 0.5F, 0.5F), transformation.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
      }
   }

   private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f1, Matrix4f matrix4f, Vector3f vector3f2) {
      Vector4f vector4f = matrix4f.transform(new Vector4f(vector3f.x() - vector3f1.x(), vector3f.y() - vector3f1.y(), vector3f.z() - vector3f1.z(), 1.0F));
      vector4f.mul(new Vector4f(vector3f2, 1.0F));
      vector3f.set(vector4f.x() + vector3f1.x(), vector4f.y() + vector3f1.y(), vector4f.z() + vector3f1.z());
   }

   public static Direction calculateFacing(int[] aint) {
      Vector3f vector3f = new Vector3f(Float.intBitsToFloat(aint[0]), Float.intBitsToFloat(aint[1]), Float.intBitsToFloat(aint[2]));
      Vector3f vector3f1 = new Vector3f(Float.intBitsToFloat(aint[8]), Float.intBitsToFloat(aint[9]), Float.intBitsToFloat(aint[10]));
      Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(aint[16]), Float.intBitsToFloat(aint[17]), Float.intBitsToFloat(aint[18]));
      Vector3f vector3f3 = (new Vector3f((Vector3fc)vector3f)).sub(vector3f1);
      Vector3f vector3f4 = (new Vector3f((Vector3fc)vector3f2)).sub(vector3f1);
      Vector3f vector3f5 = (new Vector3f((Vector3fc)vector3f4)).cross(vector3f3).normalize();
      if (!vector3f5.isFinite()) {
         return Direction.UP;
      } else {
         Direction direction = null;
         float f = 0.0F;

         for(Direction direction1 : Direction.values()) {
            Vec3i vec3i = direction1.getNormal();
            Vector3f vector3f6 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
            float f1 = vector3f5.dot(vector3f6);
            if (f1 >= 0.0F && f1 > f) {
               f = f1;
               direction = direction1;
            }
         }

         return direction == null ? Direction.UP : direction;
      }
   }

   private void recalculateWinding(int[] aint, Direction direction) {
      int[] aint1 = new int[aint.length];
      System.arraycopy(aint, 0, aint1, 0, aint.length);
      float[] afloat = new float[Direction.values().length];
      afloat[FaceInfo.Constants.MIN_X] = 999.0F;
      afloat[FaceInfo.Constants.MIN_Y] = 999.0F;
      afloat[FaceInfo.Constants.MIN_Z] = 999.0F;
      afloat[FaceInfo.Constants.MAX_X] = -999.0F;
      afloat[FaceInfo.Constants.MAX_Y] = -999.0F;
      afloat[FaceInfo.Constants.MAX_Z] = -999.0F;

      for(int i = 0; i < 4; ++i) {
         int j = 8 * i;
         float f = Float.intBitsToFloat(aint1[j]);
         float f1 = Float.intBitsToFloat(aint1[j + 1]);
         float f2 = Float.intBitsToFloat(aint1[j + 2]);
         if (f < afloat[FaceInfo.Constants.MIN_X]) {
            afloat[FaceInfo.Constants.MIN_X] = f;
         }

         if (f1 < afloat[FaceInfo.Constants.MIN_Y]) {
            afloat[FaceInfo.Constants.MIN_Y] = f1;
         }

         if (f2 < afloat[FaceInfo.Constants.MIN_Z]) {
            afloat[FaceInfo.Constants.MIN_Z] = f2;
         }

         if (f > afloat[FaceInfo.Constants.MAX_X]) {
            afloat[FaceInfo.Constants.MAX_X] = f;
         }

         if (f1 > afloat[FaceInfo.Constants.MAX_Y]) {
            afloat[FaceInfo.Constants.MAX_Y] = f1;
         }

         if (f2 > afloat[FaceInfo.Constants.MAX_Z]) {
            afloat[FaceInfo.Constants.MAX_Z] = f2;
         }
      }

      FaceInfo faceinfo = FaceInfo.fromFacing(direction);

      for(int k = 0; k < 4; ++k) {
         int l = 8 * k;
         FaceInfo.VertexInfo faceinfo_vertexinfo = faceinfo.getVertexInfo(k);
         float f3 = afloat[faceinfo_vertexinfo.xFace];
         float f4 = afloat[faceinfo_vertexinfo.yFace];
         float f5 = afloat[faceinfo_vertexinfo.zFace];
         aint[l] = Float.floatToRawIntBits(f3);
         aint[l + 1] = Float.floatToRawIntBits(f4);
         aint[l + 2] = Float.floatToRawIntBits(f5);

         for(int i1 = 0; i1 < 4; ++i1) {
            int j1 = 8 * i1;
            float f6 = Float.intBitsToFloat(aint1[j1]);
            float f7 = Float.intBitsToFloat(aint1[j1 + 1]);
            float f8 = Float.intBitsToFloat(aint1[j1 + 2]);
            if (Mth.equal(f3, f6) && Mth.equal(f4, f7) && Mth.equal(f5, f8)) {
               aint[l + 4] = aint1[j1 + 4];
               aint[l + 4 + 1] = aint1[j1 + 4 + 1];
            }
         }
      }

   }
}
