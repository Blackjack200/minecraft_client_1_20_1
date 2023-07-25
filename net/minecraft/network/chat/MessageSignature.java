package net.minecraft.network.chat;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
   public static final Codec<MessageSignature> CODEC = ExtraCodecs.BASE64_STRING.xmap(MessageSignature::new, MessageSignature::bytes);
   public static final int BYTES = 256;

   public MessageSignature {
      Preconditions.checkState(abyte.length == 256, "Invalid message signature size");
   }

   public static MessageSignature read(FriendlyByteBuf friendlybytebuf) {
      byte[] abyte = new byte[256];
      friendlybytebuf.readBytes(abyte);
      return new MessageSignature(abyte);
   }

   public static void write(FriendlyByteBuf friendlybytebuf, MessageSignature messagesignature) {
      friendlybytebuf.writeBytes(messagesignature.bytes);
   }

   public boolean verify(SignatureValidator signaturevalidator, SignatureUpdater signatureupdater) {
      return signaturevalidator.validate(signatureupdater, this.bytes);
   }

   public ByteBuffer asByteBuffer() {
      return ByteBuffer.wrap(this.bytes);
   }

   public boolean equals(Object object) {
      if (this != object) {
         if (object instanceof MessageSignature) {
            MessageSignature messagesignature = (MessageSignature)object;
            if (Arrays.equals(this.bytes, messagesignature.bytes)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   public String toString() {
      return Base64.getEncoder().encodeToString(this.bytes);
   }

   public MessageSignature.Packed pack(MessageSignatureCache messagesignaturecache) {
      int i = messagesignaturecache.pack(this);
      return i != -1 ? new MessageSignature.Packed(i) : new MessageSignature.Packed(this);
   }

   public static record Packed(int id, @Nullable MessageSignature fullSignature) {
      public static final int FULL_SIGNATURE = -1;

      public Packed(MessageSignature messagesignature) {
         this(-1, messagesignature);
      }

      public Packed(int i) {
         this(i, (MessageSignature)null);
      }

      public static MessageSignature.Packed read(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readVarInt() - 1;
         return i == -1 ? new MessageSignature.Packed(MessageSignature.read(friendlybytebuf)) : new MessageSignature.Packed(i);
      }

      public static void write(FriendlyByteBuf friendlybytebuf, MessageSignature.Packed messagesignature_packed) {
         friendlybytebuf.writeVarInt(messagesignature_packed.id() + 1);
         if (messagesignature_packed.fullSignature() != null) {
            MessageSignature.write(friendlybytebuf, messagesignature_packed.fullSignature());
         }

      }

      public Optional<MessageSignature> unpack(MessageSignatureCache messagesignaturecache) {
         return this.fullSignature != null ? Optional.of(this.fullSignature) : Optional.ofNullable(messagesignaturecache.unpack(this.id));
      }
   }
}
