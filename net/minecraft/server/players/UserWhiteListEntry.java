package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserWhiteListEntry extends StoredUserEntry<GameProfile> {
   public UserWhiteListEntry(GameProfile gameprofile) {
      super(gameprofile);
   }

   public UserWhiteListEntry(JsonObject jsonobject) {
      super(createGameProfile(jsonobject));
   }

   protected void serialize(JsonObject jsonobject) {
      if (this.getUser() != null) {
         jsonobject.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
         jsonobject.addProperty("name", this.getUser().getName());
      }
   }

   private static GameProfile createGameProfile(JsonObject jsonobject) {
      if (jsonobject.has("uuid") && jsonobject.has("name")) {
         String s = jsonobject.get("uuid").getAsString();

         UUID uuid;
         try {
            uuid = UUID.fromString(s);
         } catch (Throwable var4) {
            return null;
         }

         return new GameProfile(uuid, jsonobject.get("name").getAsString());
      } else {
         return null;
      }
   }
}
