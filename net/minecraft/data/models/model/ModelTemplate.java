package net.minecraft.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModelTemplate {
   private final Optional<ResourceLocation> model;
   private final Set<TextureSlot> requiredSlots;
   private final Optional<String> suffix;

   public ModelTemplate(Optional<ResourceLocation> optional, Optional<String> optional1, TextureSlot... atextureslot) {
      this.model = optional;
      this.suffix = optional1;
      this.requiredSlots = ImmutableSet.copyOf(atextureslot);
   }

   public ResourceLocation create(Block block, TextureMapping texturemapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer) {
      return this.create(ModelLocationUtils.getModelLocation(block, this.suffix.orElse("")), texturemapping, biconsumer);
   }

   public ResourceLocation createWithSuffix(Block block, String s, TextureMapping texturemapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer) {
      return this.create(ModelLocationUtils.getModelLocation(block, s + (String)this.suffix.orElse("")), texturemapping, biconsumer);
   }

   public ResourceLocation createWithOverride(Block block, String s, TextureMapping texturemapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer) {
      return this.create(ModelLocationUtils.getModelLocation(block, s), texturemapping, biconsumer);
   }

   public ResourceLocation create(ResourceLocation resourcelocation, TextureMapping texturemapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer) {
      return this.create(resourcelocation, texturemapping, biconsumer, this::createBaseTemplate);
   }

   public ResourceLocation create(ResourceLocation resourcelocation, TextureMapping texturemapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biconsumer, ModelTemplate.JsonFactory modeltemplate_jsonfactory) {
      Map<TextureSlot, ResourceLocation> map = this.createMap(texturemapping);
      biconsumer.accept(resourcelocation, () -> modeltemplate_jsonfactory.create(resourcelocation, map));
      return resourcelocation;
   }

   public JsonObject createBaseTemplate(ResourceLocation resourcelocation, Map<TextureSlot, ResourceLocation> map) {
      JsonObject jsonobject = new JsonObject();
      this.model.ifPresent((resourcelocation2) -> jsonobject.addProperty("parent", resourcelocation2.toString()));
      if (!map.isEmpty()) {
         JsonObject jsonobject1 = new JsonObject();
         map.forEach((textureslot, resourcelocation1) -> jsonobject1.addProperty(textureslot.getId(), resourcelocation1.toString()));
         jsonobject.add("textures", jsonobject1);
      }

      return jsonobject;
   }

   private Map<TextureSlot, ResourceLocation> createMap(TextureMapping texturemapping) {
      return Streams.concat(this.requiredSlots.stream(), texturemapping.getForced()).collect(ImmutableMap.toImmutableMap(Function.identity(), texturemapping::get));
   }

   public interface JsonFactory {
      JsonObject create(ResourceLocation resourcelocation, Map<TextureSlot, ResourceLocation> map);
   }
}
