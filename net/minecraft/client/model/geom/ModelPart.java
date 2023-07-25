package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public final class ModelPart {
   public static final float DEFAULT_SCALE = 1.0F;
   public float x;
   public float y;
   public float z;
   public float xRot;
   public float yRot;
   public float zRot;
   public float xScale = 1.0F;
   public float yScale = 1.0F;
   public float zScale = 1.0F;
   public boolean visible = true;
   public boolean skipDraw;
   private final List<ModelPart.Cube> cubes;
   private final Map<String, ModelPart> children;
   private PartPose initialPose = PartPose.ZERO;

   public ModelPart(List<ModelPart.Cube> list, Map<String, ModelPart> map) {
      this.cubes = list;
      this.children = map;
   }

   public PartPose storePose() {
      return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
   }

   public PartPose getInitialPose() {
      return this.initialPose;
   }

   public void setInitialPose(PartPose partpose) {
      this.initialPose = partpose;
   }

   public void resetPose() {
      this.loadPose(this.initialPose);
   }

   public void loadPose(PartPose partpose) {
      this.x = partpose.x;
      this.y = partpose.y;
      this.z = partpose.z;
      this.xRot = partpose.xRot;
      this.yRot = partpose.yRot;
      this.zRot = partpose.zRot;
      this.xScale = 1.0F;
      this.yScale = 1.0F;
      this.zScale = 1.0F;
   }

   public void copyFrom(ModelPart modelpart) {
      this.xScale = modelpart.xScale;
      this.yScale = modelpart.yScale;
      this.zScale = modelpart.zScale;
      this.xRot = modelpart.xRot;
      this.yRot = modelpart.yRot;
      this.zRot = modelpart.zRot;
      this.x = modelpart.x;
      this.y = modelpart.y;
      this.z = modelpart.z;
   }

   public boolean hasChild(String s) {
      return this.children.containsKey(s);
   }

   public ModelPart getChild(String s) {
      ModelPart modelpart = this.children.get(s);
      if (modelpart == null) {
         throw new NoSuchElementException("Can't find part " + s);
      } else {
         return modelpart;
      }
   }

   public void setPos(float f, float f1, float f2) {
      this.x = f;
      this.y = f1;
      this.z = f2;
   }

   public void setRotation(float f, float f1, float f2) {
      this.xRot = f;
      this.yRot = f1;
      this.zRot = f2;
   }

   public void render(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j) {
      this.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      if (this.visible) {
         if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
            posestack.pushPose();
            this.translateAndRotate(posestack);
            if (!this.skipDraw) {
               this.compile(posestack.last(), vertexconsumer, i, j, f, f1, f2, f3);
            }

            for(ModelPart modelpart : this.children.values()) {
               modelpart.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
            }

            posestack.popPose();
         }
      }
   }

   public void visit(PoseStack posestack, ModelPart.Visitor modelpart_visitor) {
      this.visit(posestack, modelpart_visitor, "");
   }

   private void visit(PoseStack posestack, ModelPart.Visitor modelpart_visitor, String s) {
      if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
         posestack.pushPose();
         this.translateAndRotate(posestack);
         PoseStack.Pose posestack_pose = posestack.last();

         for(int i = 0; i < this.cubes.size(); ++i) {
            modelpart_visitor.visit(posestack_pose, s, i, this.cubes.get(i));
         }

         String s1 = s + "/";
         this.children.forEach((s3, modelpart) -> modelpart.visit(posestack, modelpart_visitor, s1 + s3));
         posestack.popPose();
      }
   }

   public void translateAndRotate(PoseStack posestack) {
      posestack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
      if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
         posestack.mulPose((new Quaternionf()).rotationZYX(this.zRot, this.yRot, this.xRot));
      }

      if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
         posestack.scale(this.xScale, this.yScale, this.zScale);
      }

   }

   private void compile(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      for(ModelPart.Cube modelpart_cube : this.cubes) {
         modelpart_cube.compile(posestack_pose, vertexconsumer, i, j, f, f1, f2, f3);
      }

   }

   public ModelPart.Cube getRandomCube(RandomSource randomsource) {
      return this.cubes.get(randomsource.nextInt(this.cubes.size()));
   }

   public boolean isEmpty() {
      return this.cubes.isEmpty();
   }

   public void offsetPos(Vector3f vector3f) {
      this.x += vector3f.x();
      this.y += vector3f.y();
      this.z += vector3f.z();
   }

   public void offsetRotation(Vector3f vector3f) {
      this.xRot += vector3f.x();
      this.yRot += vector3f.y();
      this.zRot += vector3f.z();
   }

   public void offsetScale(Vector3f vector3f) {
      this.xScale += vector3f.x();
      this.yScale += vector3f.y();
      this.zScale += vector3f.z();
   }

   public Stream<ModelPart> getAllParts() {
      return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::getAllParts));
   }

   public static class Cube {
      private final ModelPart.Polygon[] polygons;
      public final float minX;
      public final float minY;
      public final float minZ;
      public final float maxX;
      public final float maxY;
      public final float maxZ;

      public Cube(int i, int j, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, boolean flag, float f9, float f10, Set<Direction> set) {
         this.minX = f;
         this.minY = f1;
         this.minZ = f2;
         this.maxX = f + f3;
         this.maxY = f1 + f4;
         this.maxZ = f2 + f5;
         this.polygons = new ModelPart.Polygon[set.size()];
         float f11 = f + f3;
         float f12 = f1 + f4;
         float f13 = f2 + f5;
         f -= f6;
         f1 -= f7;
         f2 -= f8;
         f11 += f6;
         f12 += f7;
         f13 += f8;
         if (flag) {
            float f14 = f11;
            f11 = f;
            f = f14;
         }

         ModelPart.Vertex modelpart_vertex = new ModelPart.Vertex(f, f1, f2, 0.0F, 0.0F);
         ModelPart.Vertex modelpart_vertex1 = new ModelPart.Vertex(f11, f1, f2, 0.0F, 8.0F);
         ModelPart.Vertex modelpart_vertex2 = new ModelPart.Vertex(f11, f12, f2, 8.0F, 8.0F);
         ModelPart.Vertex modelpart_vertex3 = new ModelPart.Vertex(f, f12, f2, 8.0F, 0.0F);
         ModelPart.Vertex modelpart_vertex4 = new ModelPart.Vertex(f, f1, f13, 0.0F, 0.0F);
         ModelPart.Vertex modelpart_vertex5 = new ModelPart.Vertex(f11, f1, f13, 0.0F, 8.0F);
         ModelPart.Vertex modelpart_vertex6 = new ModelPart.Vertex(f11, f12, f13, 8.0F, 8.0F);
         ModelPart.Vertex modelpart_vertex7 = new ModelPart.Vertex(f, f12, f13, 8.0F, 0.0F);
         float f15 = (float)i;
         float f16 = (float)i + f5;
         float f17 = (float)i + f5 + f3;
         float f18 = (float)i + f5 + f3 + f3;
         float f19 = (float)i + f5 + f3 + f5;
         float f20 = (float)i + f5 + f3 + f5 + f3;
         float f21 = (float)j;
         float f22 = (float)j + f5;
         float f23 = (float)j + f5 + f4;
         int k = 0;
         if (set.contains(Direction.DOWN)) {
            this.polygons[k++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart_vertex5, modelpart_vertex4, modelpart_vertex, modelpart_vertex1}, f16, f21, f17, f22, f9, f10, flag, Direction.DOWN);
         }

         if (set.contains(Direction.UP)) {
            this.polygons[k++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart_vertex2, modelpart_vertex3, modelpart_vertex7, modelpart_vertex6}, f17, f22, f18, f21, f9, f10, flag, Direction.UP);
         }

         if (set.contains(Direction.WEST)) {
            this.polygons[k++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart_vertex, modelpart_vertex4, modelpart_vertex7, modelpart_vertex3}, f15, f22, f16, f23, f9, f10, flag, Direction.WEST);
         }

         if (set.contains(Direction.NORTH)) {
            this.polygons[k++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart_vertex1, modelpart_vertex, modelpart_vertex3, modelpart_vertex2}, f16, f22, f17, f23, f9, f10, flag, Direction.NORTH);
         }

         if (set.contains(Direction.EAST)) {
            this.polygons[k++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart_vertex5, modelpart_vertex1, modelpart_vertex2, modelpart_vertex6}, f17, f22, f19, f23, f9, f10, flag, Direction.EAST);
         }

         if (set.contains(Direction.SOUTH)) {
            this.polygons[k] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart_vertex4, modelpart_vertex5, modelpart_vertex6, modelpart_vertex7}, f19, f22, f20, f23, f9, f10, flag, Direction.SOUTH);
         }

      }

      public void compile(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
         Matrix4f matrix4f = posestack_pose.pose();
         Matrix3f matrix3f = posestack_pose.normal();

         for(ModelPart.Polygon modelpart_polygon : this.polygons) {
            Vector3f vector3f = matrix3f.transform(new Vector3f((Vector3fc)modelpart_polygon.normal));
            float f4 = vector3f.x();
            float f5 = vector3f.y();
            float f6 = vector3f.z();

            for(ModelPart.Vertex modelpart_vertex : modelpart_polygon.vertices) {
               float f7 = modelpart_vertex.pos.x() / 16.0F;
               float f8 = modelpart_vertex.pos.y() / 16.0F;
               float f9 = modelpart_vertex.pos.z() / 16.0F;
               Vector4f vector4f = matrix4f.transform(new Vector4f(f7, f8, f9, 1.0F));
               vertexconsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f, f1, f2, f3, modelpart_vertex.u, modelpart_vertex.v, j, i, f4, f5, f6);
            }
         }

      }
   }

   static class Polygon {
      public final ModelPart.Vertex[] vertices;
      public final Vector3f normal;

      public Polygon(ModelPart.Vertex[] amodelpart_vertex, float f, float f1, float f2, float f3, float f4, float f5, boolean flag, Direction direction) {
         this.vertices = amodelpart_vertex;
         float f6 = 0.0F / f4;
         float f7 = 0.0F / f5;
         amodelpart_vertex[0] = amodelpart_vertex[0].remap(f2 / f4 - f6, f1 / f5 + f7);
         amodelpart_vertex[1] = amodelpart_vertex[1].remap(f / f4 + f6, f1 / f5 + f7);
         amodelpart_vertex[2] = amodelpart_vertex[2].remap(f / f4 + f6, f3 / f5 - f7);
         amodelpart_vertex[3] = amodelpart_vertex[3].remap(f2 / f4 - f6, f3 / f5 - f7);
         if (flag) {
            int i = amodelpart_vertex.length;

            for(int j = 0; j < i / 2; ++j) {
               ModelPart.Vertex modelpart_vertex = amodelpart_vertex[j];
               amodelpart_vertex[j] = amodelpart_vertex[i - 1 - j];
               amodelpart_vertex[i - 1 - j] = modelpart_vertex;
            }
         }

         this.normal = direction.step();
         if (flag) {
            this.normal.mul(-1.0F, 1.0F, 1.0F);
         }

      }
   }

   static class Vertex {
      public final Vector3f pos;
      public final float u;
      public final float v;

      public Vertex(float f, float f1, float f2, float f3, float f4) {
         this(new Vector3f(f, f1, f2), f3, f4);
      }

      public ModelPart.Vertex remap(float f, float f1) {
         return new ModelPart.Vertex(this.pos, f, f1);
      }

      public Vertex(Vector3f vector3f, float f, float f1) {
         this.pos = vector3f;
         this.u = f;
         this.v = f1;
      }
   }

   @FunctionalInterface
   public interface Visitor {
      void visit(PoseStack.Pose posestack_pose, String s, int i, ModelPart.Cube modelpart_cube);
   }
}
