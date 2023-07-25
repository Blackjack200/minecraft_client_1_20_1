package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class RealmsText {
   private static final String TRANSLATION_KEY = "translationKey";
   private static final String ARGS = "args";
   private final String translationKey;
   @Nullable
   private final Object[] args;

   private RealmsText(String s, @Nullable Object[] aobject) {
      this.translationKey = s;
      this.args = aobject;
   }

   public Component createComponent(Component component) {
      if (!I18n.exists(this.translationKey)) {
         return component;
      } else {
         return this.args == null ? Component.translatable(this.translationKey) : Component.translatable(this.translationKey, this.args);
      }
   }

   public static RealmsText parse(JsonObject jsonobject) {
      String s = JsonUtils.getRequiredString("translationKey", jsonobject);
      JsonElement jsonelement = jsonobject.get("args");
      String[] astring1;
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonArray jsonarray = jsonelement.getAsJsonArray();
         astring1 = new String[jsonarray.size()];

         for(int i = 0; i < jsonarray.size(); ++i) {
            astring1[i] = jsonarray.get(i).getAsString();
         }
      } else {
         astring1 = null;
      }

      return new RealmsText(s, astring1);
   }
}
