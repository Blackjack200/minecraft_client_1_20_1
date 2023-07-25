package net.minecraft.server.packs.metadata.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.GsonHelper;

public class PackMetadataSectionSerializer implements MetadataSectionType<PackMetadataSection> {
   public PackMetadataSection fromJson(JsonObject jsonobject) {
      Component component = Component.Serializer.fromJson(jsonobject.get("description"));
      if (component == null) {
         throw new JsonParseException("Invalid/missing description!");
      } else {
         int i = GsonHelper.getAsInt(jsonobject, "pack_format");
         return new PackMetadataSection(component, i);
      }
   }

   public JsonObject toJson(PackMetadataSection packmetadatasection) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("description", Component.Serializer.toJsonTree(packmetadatasection.getDescription()));
      jsonobject.addProperty("pack_format", packmetadatasection.getPackFormat());
      return jsonobject;
   }

   public String getMetadataSectionName() {
      return "pack";
   }
}
