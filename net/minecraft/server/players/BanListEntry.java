package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public abstract class BanListEntry<T> extends StoredUserEntry<T> {
   public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
   public static final String EXPIRES_NEVER = "forever";
   protected final Date created;
   protected final String source;
   @Nullable
   protected final Date expires;
   protected final String reason;

   public BanListEntry(T object, @Nullable Date date, @Nullable String s, @Nullable Date date1, @Nullable String s1) {
      super(object);
      this.created = date == null ? new Date() : date;
      this.source = s == null ? "(Unknown)" : s;
      this.expires = date1;
      this.reason = s1 == null ? "Banned by an operator." : s1;
   }

   protected BanListEntry(T object, JsonObject jsonobject) {
      super(object);

      Date date;
      try {
         date = jsonobject.has("created") ? DATE_FORMAT.parse(jsonobject.get("created").getAsString()) : new Date();
      } catch (ParseException var7) {
         date = new Date();
      }

      this.created = date;
      this.source = jsonobject.has("source") ? jsonobject.get("source").getAsString() : "(Unknown)";

      Date date2;
      try {
         date2 = jsonobject.has("expires") ? DATE_FORMAT.parse(jsonobject.get("expires").getAsString()) : null;
      } catch (ParseException var6) {
         date2 = null;
      }

      this.expires = date2;
      this.reason = jsonobject.has("reason") ? jsonobject.get("reason").getAsString() : "Banned by an operator.";
   }

   public Date getCreated() {
      return this.created;
   }

   public String getSource() {
      return this.source;
   }

   @Nullable
   public Date getExpires() {
      return this.expires;
   }

   public String getReason() {
      return this.reason;
   }

   public abstract Component getDisplayName();

   boolean hasExpired() {
      return this.expires == null ? false : this.expires.before(new Date());
   }

   protected void serialize(JsonObject jsonobject) {
      jsonobject.addProperty("created", DATE_FORMAT.format(this.created));
      jsonobject.addProperty("source", this.source);
      jsonobject.addProperty("expires", this.expires == null ? "forever" : DATE_FORMAT.format(this.expires));
      jsonobject.addProperty("reason", this.reason);
   }
}
