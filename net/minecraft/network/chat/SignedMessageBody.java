package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
   public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content), ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp), Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt), LastSeenMessages.CODEC.optionalFieldOf("last_seen", LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)).apply(recordcodecbuilder_instance, SignedMessageBody::new));

   public static SignedMessageBody unsigned(String s) {
      return new SignedMessageBody(s, Instant.now(), 0L, LastSeenMessages.EMPTY);
   }

   public void updateSignature(SignatureUpdater.Output signatureupdater_output) throws SignatureException {
      signatureupdater_output.update(Longs.toByteArray(this.salt));
      signatureupdater_output.update(Longs.toByteArray(this.timeStamp.getEpochSecond()));
      byte[] abyte = this.content.getBytes(StandardCharsets.UTF_8);
      signatureupdater_output.update(Ints.toByteArray(abyte.length));
      signatureupdater_output.update(abyte);
      this.lastSeen.updateSignature(signatureupdater_output);
   }

   public SignedMessageBody.Packed pack(MessageSignatureCache messagesignaturecache) {
      return new SignedMessageBody.Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack(messagesignaturecache));
   }

   public static record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
      public Packed(FriendlyByteBuf friendlybytebuf) {
         this(friendlybytebuf.readUtf(256), friendlybytebuf.readInstant(), friendlybytebuf.readLong(), new LastSeenMessages.Packed(friendlybytebuf));
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeUtf(this.content, 256);
         friendlybytebuf.writeInstant(this.timeStamp);
         friendlybytebuf.writeLong(this.salt);
         this.lastSeen.write(friendlybytebuf);
      }

      public Optional<SignedMessageBody> unpack(MessageSignatureCache messagesignaturecache) {
         return this.lastSeen.unpack(messagesignaturecache).map((lastseenmessages) -> new SignedMessageBody(this.content, this.timeStamp, this.salt, lastseenmessages));
      }
   }
}
