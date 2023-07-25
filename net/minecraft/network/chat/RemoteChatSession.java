package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
   public SignedMessageValidator createMessageValidator() {
      return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator());
   }

   public SignedMessageChain.Decoder createMessageDecoder(UUID uuid) {
      return (new SignedMessageChain(uuid, this.sessionId)).decoder(this.profilePublicKey);
   }

   public RemoteChatSession.Data asData() {
      return new RemoteChatSession.Data(this.sessionId, this.profilePublicKey.data());
   }

   public boolean hasExpired() {
      return this.profilePublicKey.data().hasExpired();
   }

   public static record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
      public static RemoteChatSession.Data read(FriendlyByteBuf friendlybytebuf) {
         return new RemoteChatSession.Data(friendlybytebuf.readUUID(), new ProfilePublicKey.Data(friendlybytebuf));
      }

      public static void write(FriendlyByteBuf friendlybytebuf, RemoteChatSession.Data remotechatsession_data) {
         friendlybytebuf.writeUUID(remotechatsession_data.sessionId);
         remotechatsession_data.profilePublicKey.write(friendlybytebuf);
      }

      public RemoteChatSession validate(GameProfile gameprofile, SignatureValidator signaturevalidator, Duration duration) throws ProfilePublicKey.ValidationException {
         return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(signaturevalidator, gameprofile.getId(), this.profilePublicKey, duration));
      }
   }
}
