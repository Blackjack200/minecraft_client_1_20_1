package net.minecraft.network.protocol.status;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public record ServerStatus(Component description, Optional<ServerStatus.Players> players, Optional<ServerStatus.Version> version, Optional<ServerStatus.Favicon> favicon, boolean enforcesSecureChat) {
   public static final Codec<ServerStatus> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.COMPONENT.optionalFieldOf("description", CommonComponents.EMPTY).forGetter(ServerStatus::description), ServerStatus.Players.CODEC.optionalFieldOf("players").forGetter(ServerStatus::players), ServerStatus.Version.CODEC.optionalFieldOf("version").forGetter(ServerStatus::version), ServerStatus.Favicon.CODEC.optionalFieldOf("favicon").forGetter(ServerStatus::favicon), Codec.BOOL.optionalFieldOf("enforcesSecureChat", Boolean.valueOf(false)).forGetter(ServerStatus::enforcesSecureChat)).apply(recordcodecbuilder_instance, ServerStatus::new));

   public static record Favicon(byte[] iconBytes) {
      private static final String PREFIX = "data:image/png;base64,";
      public static final Codec<ServerStatus.Favicon> CODEC = Codec.STRING.comapFlatMap((s) -> {
         if (!s.startsWith("data:image/png;base64,")) {
            return DataResult.error(() -> "Unknown format");
         } else {
            try {
               String s1 = s.substring("data:image/png;base64,".length()).replaceAll("\n", "");
               byte[] abyte = Base64.getDecoder().decode(s1.getBytes(StandardCharsets.UTF_8));
               return DataResult.success(new ServerStatus.Favicon(abyte));
            } catch (IllegalArgumentException var3) {
               return DataResult.error(() -> "Malformed base64 server icon");
            }
         }
      }, (serverstatus_favicon) -> "data:image/png;base64," + new String(Base64.getEncoder().encode(serverstatus_favicon.iconBytes), StandardCharsets.UTF_8));
   }

   public static record Players(int max, int online, List<GameProfile> sample) {
      private static final Codec<GameProfile> PROFILE_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(GameProfile::getId), Codec.STRING.fieldOf("name").forGetter(GameProfile::getName)).apply(recordcodecbuilder_instance, GameProfile::new));
      public static final Codec<ServerStatus.Players> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("max").forGetter(ServerStatus.Players::max), Codec.INT.fieldOf("online").forGetter(ServerStatus.Players::online), PROFILE_CODEC.listOf().optionalFieldOf("sample", List.of()).forGetter(ServerStatus.Players::sample)).apply(recordcodecbuilder_instance, ServerStatus.Players::new));
   }

   public static record Version(String name, int protocol) {
      public static final Codec<ServerStatus.Version> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.STRING.fieldOf("name").forGetter(ServerStatus.Version::name), Codec.INT.fieldOf("protocol").forGetter(ServerStatus.Version::protocol)).apply(recordcodecbuilder_instance, ServerStatus.Version::new));

      public static ServerStatus.Version current() {
         WorldVersion worldversion = SharedConstants.getCurrentVersion();
         return new ServerStatus.Version(worldversion.getName(), worldversion.getProtocolVersion());
      }
   }
}
