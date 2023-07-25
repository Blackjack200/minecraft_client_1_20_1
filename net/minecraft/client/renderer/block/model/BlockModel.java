package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemDisplayContext;
import org.slf4j.Logger;

public class BlockModel implements UnbakedModel {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FaceBakery FACE_BAKERY = new FaceBakery();
   @VisibleForTesting
   static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(BlockModel.class, new BlockModel.Deserializer()).registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer()).registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer()).registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer()).registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer()).create();
   private static final char REFERENCE_CHAR = '#';
   public static final String PARTICLE_TEXTURE_REFERENCE = "particle";
   private static final boolean DEFAULT_AMBIENT_OCCLUSION = true;
   private final List<BlockElement> elements;
   @Nullable
   private final BlockModel.GuiLight guiLight;
   @Nullable
   private final Boolean hasAmbientOcclusion;
   private final ItemTransforms transforms;
   private final List<ItemOverride> overrides;
   public String name = "";
   @VisibleForTesting
   protected final Map<String, Either<Material, String>> textureMap;
   @Nullable
   protected BlockModel parent;
   @Nullable
   protected ResourceLocation parentLocation;

   public static BlockModel fromStream(Reader reader) {
      return GsonHelper.fromJson(GSON, reader, BlockModel.class);
   }

   public static BlockModel fromString(String s) {
      return fromStream(new StringReader(s));
   }

   public BlockModel(@Nullable ResourceLocation resourcelocation, List<BlockElement> list, Map<String, Either<Material, String>> map, @Nullable Boolean obool, @Nullable BlockModel.GuiLight blockmodel_guilight, ItemTransforms itemtransforms, List<ItemOverride> list1) {
      this.elements = list;
      this.hasAmbientOcclusion = obool;
      this.guiLight = blockmodel_guilight;
      this.textureMap = map;
      this.parentLocation = resourcelocation;
      this.transforms = itemtransforms;
      this.overrides = list1;
   }

   public List<BlockElement> getElements() {
      return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
   }

   public boolean hasAmbientOcclusion() {
      if (this.hasAmbientOcclusion != null) {
         return this.hasAmbientOcclusion;
      } else {
         return this.parent != null ? this.parent.hasAmbientOcclusion() : true;
      }
   }

   public BlockModel.GuiLight getGuiLight() {
      if (this.guiLight != null) {
         return this.guiLight;
      } else {
         return this.parent != null ? this.parent.getGuiLight() : BlockModel.GuiLight.SIDE;
      }
   }

   public boolean isResolved() {
      return this.parentLocation == null || this.parent != null && this.parent.isResolved();
   }

   public List<ItemOverride> getOverrides() {
      return this.overrides;
   }

   private ItemOverrides getItemOverrides(ModelBaker modelbaker, BlockModel blockmodel) {
      return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(modelbaker, blockmodel, this.overrides);
   }

   public Collection<ResourceLocation> getDependencies() {
      Set<ResourceLocation> set = Sets.newHashSet();

      for(ItemOverride itemoverride : this.overrides) {
         set.add(itemoverride.getModel());
      }

      if (this.parentLocation != null) {
         set.add(this.parentLocation);
      }

      return set;
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
      Set<UnbakedModel> set = Sets.newLinkedHashSet();

      for(BlockModel blockmodel = this; blockmodel.parentLocation != null && blockmodel.parent == null; blockmodel = blockmodel.parent) {
         set.add(blockmodel);
         UnbakedModel unbakedmodel = function.apply(blockmodel.parentLocation);
         if (unbakedmodel == null) {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.parentLocation, blockmodel);
         }

         if (set.contains(unbakedmodel)) {
            LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", blockmodel, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentLocation);
            unbakedmodel = null;
         }

         if (unbakedmodel == null) {
            blockmodel.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
            unbakedmodel = function.apply(blockmodel.parentLocation);
         }

         if (!(unbakedmodel instanceof BlockModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
         }

         blockmodel.parent = (BlockModel)unbakedmodel;
      }

      this.overrides.forEach((itemoverride) -> {
         UnbakedModel unbakedmodel1 = function.apply(itemoverride.getModel());
         if (!Objects.equals(unbakedmodel1, this)) {
            unbakedmodel1.resolveParents(function);
         }
      });
   }

   public BakedModel bake(ModelBaker modelbaker, Function<Material, TextureAtlasSprite> function, ModelState modelstate, ResourceLocation resourcelocation) {
      return this.bake(modelbaker, this, function, modelstate, resourcelocation, true);
   }

   public BakedModel bake(ModelBaker modelbaker, BlockModel blockmodel, Function<Material, TextureAtlasSprite> function, ModelState modelstate, ResourceLocation resourcelocation, boolean flag) {
      TextureAtlasSprite textureatlassprite = function.apply(this.getMaterial("particle"));
      if (this.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
         return new BuiltInModel(this.getTransforms(), this.getItemOverrides(modelbaker, blockmodel), textureatlassprite, this.getGuiLight().lightLikeBlock());
      } else {
         SimpleBakedModel.Builder simplebakedmodel_builder = (new SimpleBakedModel.Builder(this, this.getItemOverrides(modelbaker, blockmodel), flag)).particle(textureatlassprite);

         for(BlockElement blockelement : this.getElements()) {
            for(Direction direction : blockelement.faces.keySet()) {
               BlockElementFace blockelementface = blockelement.faces.get(direction);
               TextureAtlasSprite textureatlassprite1 = function.apply(this.getMaterial(blockelementface.texture));
               if (blockelementface.cullForDirection == null) {
                  simplebakedmodel_builder.addUnculledFace(bakeFace(blockelement, blockelementface, textureatlassprite1, direction, modelstate, resourcelocation));
               } else {
                  simplebakedmodel_builder.addCulledFace(Direction.rotate(modelstate.getRotation().getMatrix(), blockelementface.cullForDirection), bakeFace(blockelement, blockelementface, textureatlassprite1, direction, modelstate, resourcelocation));
               }
            }
         }

         return simplebakedmodel_builder.build();
      }
   }

   private static BakedQuad bakeFace(BlockElement blockelement, BlockElementFace blockelementface, TextureAtlasSprite textureatlassprite, Direction direction, ModelState modelstate, ResourceLocation resourcelocation) {
      return FACE_BAKERY.bakeQuad(blockelement.from, blockelement.to, blockelementface, textureatlassprite, direction, modelstate, blockelement.rotation, blockelement.shade, resourcelocation);
   }

   public boolean hasTexture(String s) {
      return !MissingTextureAtlasSprite.getLocation().equals(this.getMaterial(s).texture());
   }

   public Material getMaterial(String s) {
      if (isTextureReference(s)) {
         s = s.substring(1);
      }

      List<String> list = Lists.newArrayList();

      while(true) {
         Either<Material, String> either = this.findTextureEntry(s);
         Optional<Material> optional = either.left();
         if (optional.isPresent()) {
            return optional.get();
         }

         s = either.right().get();
         if (list.contains(s)) {
            LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", Joiner.on("->").join(list), s, this.name);
            return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
         }

         list.add(s);
      }
   }

   private Either<Material, String> findTextureEntry(String s) {
      for(BlockModel blockmodel = this; blockmodel != null; blockmodel = blockmodel.parent) {
         Either<Material, String> either = blockmodel.textureMap.get(s);
         if (either != null) {
            return either;
         }
      }

      return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
   }

   static boolean isTextureReference(String s) {
      return s.charAt(0) == '#';
   }

   public BlockModel getRootModel() {
      return this.parent == null ? this : this.parent.getRootModel();
   }

   public ItemTransforms getTransforms() {
      ItemTransform itemtransform = this.getTransform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
      ItemTransform itemtransform1 = this.getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
      ItemTransform itemtransform2 = this.getTransform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
      ItemTransform itemtransform3 = this.getTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
      ItemTransform itemtransform4 = this.getTransform(ItemDisplayContext.HEAD);
      ItemTransform itemtransform5 = this.getTransform(ItemDisplayContext.GUI);
      ItemTransform itemtransform6 = this.getTransform(ItemDisplayContext.GROUND);
      ItemTransform itemtransform7 = this.getTransform(ItemDisplayContext.FIXED);
      return new ItemTransforms(itemtransform, itemtransform1, itemtransform2, itemtransform3, itemtransform4, itemtransform5, itemtransform6, itemtransform7);
   }

   private ItemTransform getTransform(ItemDisplayContext itemdisplaycontext) {
      return this.parent != null && !this.transforms.hasTransform(itemdisplaycontext) ? this.parent.getTransform(itemdisplaycontext) : this.transforms.getTransform(itemdisplaycontext);
   }

   public String toString() {
      return this.name;
   }

   public static class Deserializer implements JsonDeserializer<BlockModel> {
      public BlockModel deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         List<BlockElement> list = this.getElements(jsondeserializationcontext, jsonobject);
         String s = this.getParentName(jsonobject);
         Map<String, Either<Material, String>> map = this.getTextureMap(jsonobject);
         Boolean obool = this.getAmbientOcclusion(jsonobject);
         ItemTransforms itemtransforms = ItemTransforms.NO_TRANSFORMS;
         if (jsonobject.has("display")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "display");
            itemtransforms = jsondeserializationcontext.deserialize(jsonobject1, ItemTransforms.class);
         }

         List<ItemOverride> list1 = this.getOverrides(jsondeserializationcontext, jsonobject);
         BlockModel.GuiLight blockmodel_guilight = null;
         if (jsonobject.has("gui_light")) {
            blockmodel_guilight = BlockModel.GuiLight.getByName(GsonHelper.getAsString(jsonobject, "gui_light"));
         }

         ResourceLocation resourcelocation = s.isEmpty() ? null : new ResourceLocation(s);
         return new BlockModel(resourcelocation, list, map, obool, blockmodel_guilight, itemtransforms, list1);
      }

      protected List<ItemOverride> getOverrides(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         List<ItemOverride> list = Lists.newArrayList();
         if (jsonobject.has("overrides")) {
            for(JsonElement jsonelement : GsonHelper.getAsJsonArray(jsonobject, "overrides")) {
               list.add(jsondeserializationcontext.deserialize(jsonelement, ItemOverride.class));
            }
         }

         return list;
      }

      private Map<String, Either<Material, String>> getTextureMap(JsonObject jsonobject) {
         ResourceLocation resourcelocation = TextureAtlas.LOCATION_BLOCKS;
         Map<String, Either<Material, String>> map = Maps.newHashMap();
         if (jsonobject.has("textures")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "textures");

            for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
               map.put(map_entry.getKey(), parseTextureLocationOrReference(resourcelocation, map_entry.getValue().getAsString()));
            }
         }

         return map;
      }

      private static Either<Material, String> parseTextureLocationOrReference(ResourceLocation resourcelocation, String s) {
         if (BlockModel.isTextureReference(s)) {
            return Either.right(s.substring(1));
         } else {
            ResourceLocation resourcelocation1 = ResourceLocation.tryParse(s);
            if (resourcelocation1 == null) {
               throw new JsonParseException(s + " is not valid resource location");
            } else {
               return Either.left(new Material(resourcelocation, resourcelocation1));
            }
         }
      }

      private String getParentName(JsonObject jsonobject) {
         return GsonHelper.getAsString(jsonobject, "parent", "");
      }

      @Nullable
      protected Boolean getAmbientOcclusion(JsonObject jsonobject) {
         return jsonobject.has("ambientocclusion") ? GsonHelper.getAsBoolean(jsonobject, "ambientocclusion") : null;
      }

      protected List<BlockElement> getElements(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         List<BlockElement> list = Lists.newArrayList();
         if (jsonobject.has("elements")) {
            for(JsonElement jsonelement : GsonHelper.getAsJsonArray(jsonobject, "elements")) {
               list.add(jsondeserializationcontext.deserialize(jsonelement, BlockElement.class));
            }
         }

         return list;
      }
   }

   public static enum GuiLight {
      FRONT("front"),
      SIDE("side");

      private final String name;

      private GuiLight(String s) {
         this.name = s;
      }

      public static BlockModel.GuiLight getByName(String s) {
         for(BlockModel.GuiLight blockmodel_guilight : values()) {
            if (blockmodel_guilight.name.equals(s)) {
               return blockmodel_guilight;
            }
         }

         throw new IllegalArgumentException("Invalid gui light: " + s);
      }

      public boolean lightLikeBlock() {
         return this == SIDE;
      }
   }

   public static class LoopException extends RuntimeException {
      public LoopException(String s) {
         super(s);
      }
   }
}
