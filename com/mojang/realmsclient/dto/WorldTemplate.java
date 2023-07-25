package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class WorldTemplate extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String id = "";
   public String name = "";
   public String version = "";
   public String author = "";
   public String link = "";
   @Nullable
   public String image;
   public String trailer = "";
   public String recommendedPlayers = "";
   public WorldTemplate.WorldTemplateType type = WorldTemplate.WorldTemplateType.WORLD_TEMPLATE;

   public static WorldTemplate parse(JsonObject jsonobject) {
      WorldTemplate worldtemplate = new WorldTemplate();

      try {
         worldtemplate.id = JsonUtils.getStringOr("id", jsonobject, "");
         worldtemplate.name = JsonUtils.getStringOr("name", jsonobject, "");
         worldtemplate.version = JsonUtils.getStringOr("version", jsonobject, "");
         worldtemplate.author = JsonUtils.getStringOr("author", jsonobject, "");
         worldtemplate.link = JsonUtils.getStringOr("link", jsonobject, "");
         worldtemplate.image = JsonUtils.getStringOr("image", jsonobject, (String)null);
         worldtemplate.trailer = JsonUtils.getStringOr("trailer", jsonobject, "");
         worldtemplate.recommendedPlayers = JsonUtils.getStringOr("recommendedPlayers", jsonobject, "");
         worldtemplate.type = WorldTemplate.WorldTemplateType.valueOf(JsonUtils.getStringOr("type", jsonobject, WorldTemplate.WorldTemplateType.WORLD_TEMPLATE.name()));
      } catch (Exception var3) {
         LOGGER.error("Could not parse WorldTemplate: {}", (Object)var3.getMessage());
      }

      return worldtemplate;
   }

   public static enum WorldTemplateType {
      WORLD_TEMPLATE,
      MINIGAME,
      ADVENTUREMAP,
      EXPERIENCE,
      INSPIRATION;
   }
}
