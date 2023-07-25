package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ParticleDescription {
   private final List<ResourceLocation> textures;

   private ParticleDescription(List<ResourceLocation> list) {
      this.textures = list;
   }

   public List<ResourceLocation> getTextures() {
      return this.textures;
   }

   public static ParticleDescription fromJson(JsonObject jsonobject) {
      JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "textures", (JsonArray)null);
      if (jsonarray == null) {
         return new ParticleDescription(List.of());
      } else {
         List<ResourceLocation> list = Streams.stream(jsonarray).map((jsonelement) -> GsonHelper.convertToString(jsonelement, "texture")).map(ResourceLocation::new).collect(ImmutableList.toImmutableList());
         return new ParticleDescription(list);
      }
   }
}
