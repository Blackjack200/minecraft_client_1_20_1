package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public class UserBanListEntry extends BanListEntry<GameProfile> {
   public UserBanListEntry(GameProfile gameprofile) {
      this(gameprofile, (Date)null, (String)null, (Date)null, (String)null);
   }

   public UserBanListEntry(GameProfile gameprofile, @Nullable Date date, @Nullable String s, @Nullable Date date1, @Nullable String s1) {
      super(gameprofile, date, s, date1, s1);
   }

   public UserBanListEntry(JsonObject jsonobject) {
      super(createGameProfile(jsonobject), jsonobject);
   }

   protected void serialize(JsonObject jsonobject) {
      if (this.getUser() != null) {
         jsonobject.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
         jsonobject.addProperty("name", this.getUser().getName());
         super.serialize(jsonobject);
      }
   }

   public Component getDisplayName() {
      GameProfile gameprofile = this.getUser();
      return Component.literal(gameprofile.getName() != null ? gameprofile.getName() : Objects.toString(gameprofile.getId(), "(Unknown)"));
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
