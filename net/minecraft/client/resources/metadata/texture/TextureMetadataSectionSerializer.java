package net.minecraft.client.resources.metadata.texture;

import com.google.gson.JsonObject;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public class TextureMetadataSectionSerializer implements MetadataSectionSerializer<TextureMetadataSection> {
   public TextureMetadataSection fromJson(JsonObject jsonobject) {
      boolean flag = GsonHelper.getAsBoolean(jsonobject, "blur", false);
      boolean flag1 = GsonHelper.getAsBoolean(jsonobject, "clamp", false);
      return new TextureMetadataSection(flag, flag1);
   }

   public String getMetadataSectionName() {
      return "texture";
   }
}
