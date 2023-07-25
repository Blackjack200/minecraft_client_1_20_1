package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Direction;

public class CubeListBuilder {
   private static final Set<Direction> ALL_VISIBLE = EnumSet.allOf(Direction.class);
   private final List<CubeDefinition> cubes = Lists.newArrayList();
   private int xTexOffs;
   private int yTexOffs;
   private boolean mirror;

   public CubeListBuilder texOffs(int i, int j) {
      this.xTexOffs = i;
      this.yTexOffs = j;
      return this;
   }

   public CubeListBuilder mirror() {
      return this.mirror(true);
   }

   public CubeListBuilder mirror(boolean flag) {
      this.mirror = flag;
      return this;
   }

   public CubeListBuilder addBox(String s, float f, float f1, float f2, int i, int j, int k, CubeDeformation cubedeformation, int l, int i1) {
      this.texOffs(l, i1);
      this.cubes.add(new CubeDefinition(s, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, (float)i, (float)j, (float)k, cubedeformation, this.mirror, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(String s, float f, float f1, float f2, int i, int j, int k, int l, int i1) {
      this.texOffs(l, i1);
      this.cubes.add(new CubeDefinition(s, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, (float)i, (float)j, (float)k, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(float f, float f1, float f2, float f3, float f4, float f5) {
      this.cubes.add(new CubeDefinition((String)null, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(float f, float f1, float f2, float f3, float f4, float f5, Set<Direction> set) {
      this.cubes.add(new CubeDefinition((String)null, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F, set));
      return this;
   }

   public CubeListBuilder addBox(String s, float f, float f1, float f2, float f3, float f4, float f5) {
      this.cubes.add(new CubeDefinition(s, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, CubeDeformation.NONE, this.mirror, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(String s, float f, float f1, float f2, float f3, float f4, float f5, CubeDeformation cubedeformation) {
      this.cubes.add(new CubeDefinition(s, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, cubedeformation, this.mirror, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(float f, float f1, float f2, float f3, float f4, float f5, boolean flag) {
      this.cubes.add(new CubeDefinition((String)null, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, CubeDeformation.NONE, flag, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(float f, float f1, float f2, float f3, float f4, float f5, CubeDeformation cubedeformation, float f6, float f7) {
      this.cubes.add(new CubeDefinition((String)null, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, cubedeformation, this.mirror, f6, f7, ALL_VISIBLE));
      return this;
   }

   public CubeListBuilder addBox(float f, float f1, float f2, float f3, float f4, float f5, CubeDeformation cubedeformation) {
      this.cubes.add(new CubeDefinition((String)null, (float)this.xTexOffs, (float)this.yTexOffs, f, f1, f2, f3, f4, f5, cubedeformation, this.mirror, 1.0F, 1.0F, ALL_VISIBLE));
      return this;
   }

   public List<CubeDefinition> getCubes() {
      return ImmutableList.copyOf(this.cubes);
   }

   public static CubeListBuilder create() {
      return new CubeListBuilder();
   }
}
