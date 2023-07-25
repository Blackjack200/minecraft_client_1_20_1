package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class UserWhiteList extends StoredUserList<GameProfile, UserWhiteListEntry> {
   public UserWhiteList(File file) {
      super(file);
   }

   protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonobject) {
      return new UserWhiteListEntry(jsonobject);
   }

   public boolean isWhiteListed(GameProfile gameprofile) {
      return this.contains(gameprofile);
   }

   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray((i) -> new String[i]);
   }

   protected String getKeyForUser(GameProfile gameprofile) {
      return gameprofile.getId().toString();
   }
}
