package net.minecraft.client.model.geom.builders;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class CubeDefinition {
   @Nullable
   private final String comment;
   private final Vector3f origin;
   private final Vector3f dimensions;
   private final CubeDeformation grow;
   private final boolean mirror;
   private final UVPair texCoord;
   private final UVPair texScale;
   private final Set<Direction> visibleFaces;

   protected CubeDefinition(@Nullable String s, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, CubeDeformation cubedeformation, boolean flag, float f8, float f9, Set<Direction> set) {
      this.comment = s;
      this.texCoord = new UVPair(f, f1);
      this.origin = new Vector3f(f2, f3, f4);
      this.dimensions = new Vector3f(f5, f6, f7);
      this.grow = cubedeformation;
      this.mirror = flag;
      this.texScale = new UVPair(f8, f9);
      this.visibleFaces = set;
   }

   public ModelPart.Cube bake(int i, int j) {
      return new ModelPart.Cube((int)this.texCoord.u(), (int)this.texCoord.v(), this.origin.x(), this.origin.y(), this.origin.z(), this.dimensions.x(), this.dimensions.y(), this.dimensions.z(), this.grow.growX, this.grow.growY, this.grow.growZ, this.mirror, (float)i * this.texScale.u(), (float)j * this.texScale.v(), this.visibleFaces);
   }
}
