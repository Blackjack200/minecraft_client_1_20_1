package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class ServerOpList extends StoredUserList<GameProfile, ServerOpListEntry> {
   public ServerOpList(File file) {
      super(file);
   }

   protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonobject) {
      return new ServerOpListEntry(jsonobject);
   }

   public String[] getUserList() {
      return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray((i) -> new String[i]);
   }

   public boolean canBypassPlayerLimit(GameProfile gameprofile) {
      ServerOpListEntry serveroplistentry = this.get(gameprofile);
      return serveroplistentry != null ? serveroplistentry.getBypassesPlayerLimit() : false;
   }

   protected String getKeyForUser(GameProfile gameprofile) {
      return gameprofile.getId().toString();
   }
}
