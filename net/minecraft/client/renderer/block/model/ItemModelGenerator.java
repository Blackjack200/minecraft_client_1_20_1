package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public class ItemModelGenerator {
   public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");
   private static final float MIN_Z = 7.5F;
   private static final float MAX_Z = 8.5F;

   public BlockModel generateBlockModel(Function<Material, TextureAtlasSprite> function, BlockModel blockmodel) {
      Map<String, Either<Material, String>> map = Maps.newHashMap();
      List<BlockElement> list = Lists.newArrayList();

      for(int i = 0; i < LAYERS.size(); ++i) {
         String s = LAYERS.get(i);
         if (!blockmodel.hasTexture(s)) {
            break;
         }

         Material material = blockmodel.getMaterial(s);
         map.put(s, Either.left(material));
         SpriteContents spritecontents = function.apply(material).contents();
         list.addAll(this.processFrames(i, s, spritecontents));
      }

      map.put("particle", blockmodel.hasTexture("particle") ? Either.left(blockmodel.getMaterial("particle")) : map.get("layer0"));
      BlockModel blockmodel1 = new BlockModel((ResourceLocation)null, list, map, false, blockmodel.getGuiLight(), blockmodel.getTransforms(), blockmodel.getOverrides());
      blockmodel1.name = blockmodel.name;
      return blockmodel1;
   }

   private List<BlockElement> processFrames(int i, String s, SpriteContents spritecontents) {
      Map<Direction, BlockElementFace> map = Maps.newHashMap();
      map.put(Direction.SOUTH, new BlockElementFace((Direction)null, i, s, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
      map.put(Direction.NORTH, new BlockElementFace((Direction)null, i, s, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
      List<BlockElement> list = Lists.newArrayList();
      list.add(new BlockElement(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, (BlockElementRotation)null, true));
      list.addAll(this.createSideElements(spritecontents, s, i));
      return list;
   }

   private List<BlockElement> createSideElements(SpriteContents spritecontents, String s, int i) {
      float f = (float)spritecontents.width();
      float f1 = (float)spritecontents.height();
      List<BlockElement> list = Lists.newArrayList();

      for(ItemModelGenerator.Span itemmodelgenerator_span : this.getSpans(spritecontents)) {
         float f2 = 0.0F;
         float f3 = 0.0F;
         float f4 = 0.0F;
         float f5 = 0.0F;
         float f6 = 0.0F;
         float f7 = 0.0F;
         float f8 = 0.0F;
         float f9 = 0.0F;
         float f10 = 16.0F / f;
         float f11 = 16.0F / f1;
         float f12 = (float)itemmodelgenerator_span.getMin();
         float f13 = (float)itemmodelgenerator_span.getMax();
         float f14 = (float)itemmodelgenerator_span.getAnchor();
         ItemModelGenerator.SpanFacing itemmodelgenerator_spanfacing = itemmodelgenerator_span.getFacing();
         switch (itemmodelgenerator_spanfacing) {
            case UP:
               f6 = f12;
               f2 = f12;
               f4 = f7 = f13 + 1.0F;
               f8 = f14;
               f3 = f14;
               f5 = f14;
               f9 = f14 + 1.0F;
               break;
            case DOWN:
               f8 = f14;
               f9 = f14 + 1.0F;
               f6 = f12;
               f2 = f12;
               f4 = f7 = f13 + 1.0F;
               f3 = f14 + 1.0F;
               f5 = f14 + 1.0F;
               break;
            case LEFT:
               f6 = f14;
               f2 = f14;
               f4 = f14;
               f7 = f14 + 1.0F;
               f9 = f12;
               f3 = f12;
               f5 = f8 = f13 + 1.0F;
               break;
            case RIGHT:
               f6 = f14;
               f7 = f14 + 1.0F;
               f2 = f14 + 1.0F;
               f4 = f14 + 1.0F;
               f9 = f12;
               f3 = f12;
               f5 = f8 = f13 + 1.0F;
         }

         f2 *= f10;
         f4 *= f10;
         f3 *= f11;
         f5 *= f11;
         f3 = 16.0F - f3;
         f5 = 16.0F - f5;
         f6 *= f10;
         f7 *= f10;
         f8 *= f11;
         f9 *= f11;
         Map<Direction, BlockElementFace> map = Maps.newHashMap();
         map.put(itemmodelgenerator_spanfacing.getDirection(), new BlockElementFace((Direction)null, i, s, new BlockFaceUV(new float[]{f6, f8, f7, f9}, 0)));
         switch (itemmodelgenerator_spanfacing) {
            case UP:
               list.add(new BlockElement(new Vector3f(f2, f3, 7.5F), new Vector3f(f4, f3, 8.5F), map, (BlockElementRotation)null, true));
               break;
            case DOWN:
               list.add(new BlockElement(new Vector3f(f2, f5, 7.5F), new Vector3f(f4, f5, 8.5F), map, (BlockElementRotation)null, true));
               break;
            case LEFT:
               list.add(new BlockElement(new Vector3f(f2, f3, 7.5F), new Vector3f(f2, f5, 8.5F), map, (BlockElementRotation)null, true));
               break;
            case RIGHT:
               list.add(new BlockElement(new Vector3f(f4, f3, 7.5F), new Vector3f(f4, f5, 8.5F), map, (BlockElementRotation)null, true));
         }
      }

      return list;
   }

   private List<ItemModelGenerator.Span> getSpans(SpriteContents spritecontents) {
      int i = spritecontents.width();
      int j = spritecontents.height();
      List<ItemModelGenerator.Span> list = Lists.newArrayList();
      spritecontents.getUniqueFrames().forEach((i1) -> {
         for(int j1 = 0; j1 < j; ++j1) {
            for(int k1 = 0; k1 < i; ++k1) {
               boolean flag = !this.isTransparent(spritecontents, i1, k1, j1, i, j);
               this.checkTransition(ItemModelGenerator.SpanFacing.UP, list, spritecontents, i1, k1, j1, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, list, spritecontents, i1, k1, j1, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, list, spritecontents, i1, k1, j1, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, list, spritecontents, i1, k1, j1, i, j, flag);
            }
         }

      });
      return list;
   }

   private void checkTransition(ItemModelGenerator.SpanFacing itemmodelgenerator_spanfacing, List<ItemModelGenerator.Span> list, SpriteContents spritecontents, int i, int j, int k, int l, int i1, boolean flag) {
      boolean flag1 = this.isTransparent(spritecontents, i, j + itemmodelgenerator_spanfacing.getXOffset(), k + itemmodelgenerator_spanfacing.getYOffset(), l, i1) && flag;
      if (flag1) {
         this.createOrExpandSpan(list, itemmodelgenerator_spanfacing, j, k);
      }

   }

   private void createOrExpandSpan(List<ItemModelGenerator.Span> list, ItemModelGenerator.SpanFacing itemmodelgenerator_spanfacing, int i, int j) {
      ItemModelGenerator.Span itemmodelgenerator_span = null;

      for(ItemModelGenerator.Span itemmodelgenerator_span1 : list) {
         if (itemmodelgenerator_span1.getFacing() == itemmodelgenerator_spanfacing) {
            int k = itemmodelgenerator_spanfacing.isHorizontal() ? j : i;
            if (itemmodelgenerator_span1.getAnchor() == k) {
               itemmodelgenerator_span = itemmodelgenerator_span1;
               break;
            }
         }
      }

      int l = itemmodelgenerator_spanfacing.isHorizontal() ? j : i;
      int i1 = itemmodelgenerator_spanfacing.isHorizontal() ? i : j;
      if (itemmodelgenerator_span == null) {
         list.add(new ItemModelGenerator.Span(itemmodelgenerator_spanfacing, i1, l));
      } else {
         itemmodelgenerator_span.expand(i1);
      }

   }

   private boolean isTransparent(SpriteContents spritecontents, int i, int j, int k, int l, int i1) {
      return j >= 0 && k >= 0 && j < l && k < i1 ? spritecontents.isTransparent(i, j, k) : true;
   }

   static class Span {
      private final ItemModelGenerator.SpanFacing facing;
      private int min;
      private int max;
      private final int anchor;

      public Span(ItemModelGenerator.SpanFacing itemmodelgenerator_spanfacing, int i, int j) {
         this.facing = itemmodelgenerator_spanfacing;
         this.min = i;
         this.max = i;
         this.anchor = j;
      }

      public void expand(int i) {
         if (i < this.min) {
            this.min = i;
         } else if (i > this.max) {
            this.max = i;
         }

      }

      public ItemModelGenerator.SpanFacing getFacing() {
         return this.facing;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public int getAnchor() {
         return this.anchor;
      }
   }

   static enum SpanFacing {
      UP(Direction.UP, 0, -1),
      DOWN(Direction.DOWN, 0, 1),
      LEFT(Direction.EAST, -1, 0),
      RIGHT(Direction.WEST, 1, 0);

      private final Direction direction;
      private final int xOffset;
      private final int yOffset;

      private SpanFacing(Direction direction, int i, int j) {
         this.direction = direction;
         this.xOffset = i;
         this.yOffset = j;
      }

      public Direction getDirection() {
         return this.direction;
      }

      public int getXOffset() {
         return this.xOffset;
      }

      public int getYOffset() {
         return this.yOffset;
      }

      boolean isHorizontal() {
         return this == DOWN || this == UP;
      }
   }
}
