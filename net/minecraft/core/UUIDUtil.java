package net.minecraft.core;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UUIDTypeAdapter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.Util;

public final class UUIDUtil {
   public static final Codec<UUID> CODEC = Codec.INT_STREAM.comapFlatMap((intstream) -> Util.fixedSize(intstream, 4).map(UUIDUtil::uuidFromIntArray), (uuid) -> Arrays.stream(uuidToIntArray(uuid)));
   public static final Codec<UUID> STRING_CODEC = Codec.STRING.comapFlatMap((s) -> {
      try {
         return DataResult.success(UUID.fromString(s), Lifecycle.stable());
      } catch (IllegalArgumentException var2) {
         return DataResult.error(() -> "Invalid UUID " + s + ": " + var2.getMessage());
      }
   }, UUID::toString);
   public static Codec<UUID> AUTHLIB_CODEC = Codec.either(CODEC, Codec.STRING.comapFlatMap((s) -> {
      try {
         return DataResult.success(UUIDTypeAdapter.fromString(s), Lifecycle.stable());
      } catch (IllegalArgumentException var2) {
         return DataResult.error(() -> "Invalid UUID " + s + ": " + var2.getMessage());
      }
   }, UUIDTypeAdapter::fromUUID)).xmap((either) -> either.map((uuid1) -> uuid1, (uuid) -> uuid), Either::right);
   public static final int UUID_BYTES = 16;
   private static final String UUID_PREFIX_OFFLINE_PLAYER = "OfflinePlayer:";

   private UUIDUtil() {
   }

   public static UUID uuidFromIntArray(int[] aint) {
      return new UUID((long)aint[0] << 32 | (long)aint[1] & 4294967295L, (long)aint[2] << 32 | (long)aint[3] & 4294967295L);
   }

   public static int[] uuidToIntArray(UUID uuid) {
      long i = uuid.getMostSignificantBits();
      long j = uuid.getLeastSignificantBits();
      return leastMostToIntArray(i, j);
   }

   private static int[] leastMostToIntArray(long i, long j) {
      return new int[]{(int)(i >> 32), (int)i, (int)(j >> 32), (int)j};
   }

   public static byte[] uuidToByteArray(UUID uuid) {
      byte[] abyte = new byte[16];
      ByteBuffer.wrap(abyte).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
      return abyte;
   }

   public static UUID readUUID(Dynamic<?> dynamic) {
      int[] aint = dynamic.asIntStream().toArray();
      if (aint.length != 4) {
         throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + aint.length + ".");
      } else {
         return uuidFromIntArray(aint);
      }
   }

   public static UUID getOrCreatePlayerUUID(GameProfile gameprofile) {
      UUID uuid = gameprofile.getId();
      if (uuid == null) {
         uuid = createOfflinePlayerUUID(gameprofile.getName());
      }

      return uuid;
   }

   public static UUID createOfflinePlayerUUID(String s) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + s).getBytes(StandardCharsets.UTF_8));
   }
}
