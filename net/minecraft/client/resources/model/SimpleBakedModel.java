package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBakedModel implements BakedModel {
   protected final List<BakedQuad> unculledFaces;
   protected final Map<Direction, List<BakedQuad>> culledFaces;
   protected final boolean hasAmbientOcclusion;
   protected final boolean isGui3d;
   protected final boolean usesBlockLight;
   protected final TextureAtlasSprite particleIcon;
   protected final ItemTransforms transforms;
   protected final ItemOverrides overrides;

   public SimpleBakedModel(List<BakedQuad> list, Map<Direction, List<BakedQuad>> map, boolean flag, boolean flag1, boolean flag2, TextureAtlasSprite textureatlassprite, ItemTransforms itemtransforms, ItemOverrides itemoverrides) {
      this.unculledFaces = list;
      this.culledFaces = map;
      this.hasAmbientOcclusion = flag;
      this.isGui3d = flag2;
      this.usesBlockLight = flag1;
      this.particleIcon = textureatlassprite;
      this.transforms = itemtransforms;
      this.overrides = itemoverrides;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState blockstate, @Nullable Direction direction, RandomSource randomsource) {
      return direction == null ? this.unculledFaces : this.culledFaces.get(direction);
   }

   public boolean useAmbientOcclusion() {
      return this.hasAmbientOcclusion;
   }

   public boolean isGui3d() {
      return this.isGui3d;
   }

   public boolean usesBlockLight() {
      return this.usesBlockLight;
   }

   public boolean isCustomRenderer() {
      return false;
   }

   public TextureAtlasSprite getParticleIcon() {
      return this.particleIcon;
   }

   public ItemTransforms getTransforms() {
      return this.transforms;
   }

   public ItemOverrides getOverrides() {
      return this.overrides;
   }

   public static class Builder {
      private final List<BakedQuad> unculledFaces = Lists.newArrayList();
      private final Map<Direction, List<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
      private final ItemOverrides overrides;
      private final boolean hasAmbientOcclusion;
      private TextureAtlasSprite particleIcon;
      private final boolean usesBlockLight;
      private final boolean isGui3d;
      private final ItemTransforms transforms;

      public Builder(BlockModel blockmodel, ItemOverrides itemoverrides, boolean flag) {
         this(blockmodel.hasAmbientOcclusion(), blockmodel.getGuiLight().lightLikeBlock(), flag, blockmodel.getTransforms(), itemoverrides);
      }

      private Builder(boolean flag, boolean flag1, boolean flag2, ItemTransforms itemtransforms, ItemOverrides itemoverrides) {
         for(Direction direction : Direction.values()) {
            this.culledFaces.put(direction, Lists.newArrayList());
         }

         this.overrides = itemoverrides;
         this.hasAmbientOcclusion = flag;
         this.usesBlockLight = flag1;
         this.isGui3d = flag2;
         this.transforms = itemtransforms;
      }

      public SimpleBakedModel.Builder addCulledFace(Direction direction, BakedQuad bakedquad) {
         this.culledFaces.get(direction).add(bakedquad);
         return this;
      }

      public SimpleBakedModel.Builder addUnculledFace(BakedQuad bakedquad) {
         this.unculledFaces.add(bakedquad);
         return this;
      }

      public SimpleBakedModel.Builder particle(TextureAtlasSprite textureatlassprite) {
         this.particleIcon = textureatlassprite;
         return this;
      }

      public SimpleBakedModel.Builder item() {
         return this;
      }

      public BakedModel build() {
         if (this.particleIcon == null) {
            throw new RuntimeException("Missing particle!");
         } else {
            return new SimpleBakedModel(this.unculledFaces, this.culledFaces, this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.overrides);
         }
      }
   }
}
