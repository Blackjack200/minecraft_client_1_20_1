package net.minecraft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class User {
   private final String name;
   private final String uuid;
   private final String accessToken;
   private final Optional<String> xuid;
   private final Optional<String> clientId;
   private final User.Type type;

   public User(String s, String s1, String s2, Optional<String> optional, Optional<String> optional1, User.Type user_type) {
      this.name = s;
      this.uuid = s1;
      this.accessToken = s2;
      this.xuid = optional;
      this.clientId = optional1;
      this.type = user_type;
   }

   public String getSessionId() {
      return "token:" + this.accessToken + ":" + this.uuid;
   }

   public String getUuid() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public Optional<String> getClientId() {
      return this.clientId;
   }

   public Optional<String> getXuid() {
      return this.xuid;
   }

   @Nullable
   public UUID getProfileId() {
      try {
         return UUIDTypeAdapter.fromString(this.getUuid());
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public GameProfile getGameProfile() {
      return new GameProfile(this.getProfileId(), this.getName());
   }

   public User.Type getType() {
      return this.type;
   }

   public static enum Type {
      LEGACY("legacy"),
      MOJANG("mojang"),
      MSA("msa");

      private static final Map<String, User.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((user_type) -> user_type.name, Function.identity()));
      private final String name;

      private Type(String s) {
         this.name = s;
      }

      @Nullable
      public static User.Type byName(String s) {
         return BY_NAME.get(s.toLowerCase(Locale.ROOT));
      }

      public String getName() {
         return this.name;
      }
   }
}
