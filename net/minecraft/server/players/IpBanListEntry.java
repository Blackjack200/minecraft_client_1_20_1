package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public class IpBanListEntry extends BanListEntry<String> {
   public IpBanListEntry(String s) {
      this(s, (Date)null, (String)null, (Date)null, (String)null);
   }

   public IpBanListEntry(String s, @Nullable Date date, @Nullable String s1, @Nullable Date date1, @Nullable String s2) {
      super(s, date, s1, date1, s2);
   }

   public Component getDisplayName() {
      return Component.literal(String.valueOf(this.getUser()));
   }

   public IpBanListEntry(JsonObject jsonobject) {
      super(createIpInfo(jsonobject), jsonobject);
   }

   private static String createIpInfo(JsonObject jsonobject) {
      return jsonobject.has("ip") ? jsonobject.get("ip").getAsString() : null;
   }

   protected void serialize(JsonObject jsonobject) {
      if (this.getUser() != null) {
         jsonobject.addProperty("ip", this.getUser());
         super.serialize(jsonobject);
      }
   }
}
