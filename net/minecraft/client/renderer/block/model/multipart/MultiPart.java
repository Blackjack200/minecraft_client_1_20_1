package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class MultiPart implements UnbakedModel {
   private final StateDefinition<Block, BlockState> definition;
   private final List<Selector> selectors;

   public MultiPart(StateDefinition<Block, BlockState> statedefinition, List<Selector> list) {
      this.definition = statedefinition;
      this.selectors = list;
   }

   public List<Selector> getSelectors() {
      return this.selectors;
   }

   public Set<MultiVariant> getMultiVariants() {
      Set<MultiVariant> set = Sets.newHashSet();

      for(Selector selector : this.selectors) {
         set.add(selector.getVariant());
      }

      return set;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof MultiPart)) {
         return false;
      } else {
         MultiPart multipart = (MultiPart)object;
         return Objects.equals(this.definition, multipart.definition) && Objects.equals(this.selectors, multipart.selectors);
      }
   }

   public int hashCode() {
      return Objects.hash(this.definition, this.selectors);
   }

   public Collection<ResourceLocation> getDependencies() {
      return this.getSelectors().stream().flatMap((selector) -> selector.getVariant().getDependencies().stream()).collect(Collectors.toSet());
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
      this.getSelectors().forEach((selector) -> selector.getVariant().resolveParents(function));
   }

   @Nullable
   public BakedModel bake(ModelBaker modelbaker, Function<Material, TextureAtlasSprite> function, ModelState modelstate, ResourceLocation resourcelocation) {
      MultiPartBakedModel.Builder multipartbakedmodel_builder = new MultiPartBakedModel.Builder();

      for(Selector selector : this.getSelectors()) {
         BakedModel bakedmodel = selector.getVariant().bake(modelbaker, function, modelstate, resourcelocation);
         if (bakedmodel != null) {
            multipartbakedmodel_builder.add(selector.getPredicate(this.definition), bakedmodel);
         }
      }

      return multipartbakedmodel_builder.build();
   }

   public static class Deserializer implements JsonDeserializer<MultiPart> {
      private final BlockModelDefinition.Context context;

      public Deserializer(BlockModelDefinition.Context blockmodeldefinition_context) {
         this.context = blockmodeldefinition_context;
      }

      public MultiPart deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         return new MultiPart(this.context.getDefinition(), this.getSelectors(jsondeserializationcontext, jsonelement.getAsJsonArray()));
      }

      private List<Selector> getSelectors(JsonDeserializationContext jsondeserializationcontext, JsonArray jsonarray) {
         List<Selector> list = Lists.newArrayList();

         for(JsonElement jsonelement : jsonarray) {
            list.add(jsondeserializationcontext.deserialize(jsonelement, Selector.class));
         }

         return list;
      }
   }
}
