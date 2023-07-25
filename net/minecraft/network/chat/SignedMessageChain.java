package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private SignedMessageLink nextLink;

   public SignedMessageChain(UUID uuid, UUID uuid1) {
      this.nextLink = SignedMessageLink.root(uuid, uuid1);
   }

   public SignedMessageChain.Encoder encoder(Signer signer) {
      return (signedmessagebody) -> {
         SignedMessageLink signedmessagelink = this.advanceLink();
         return signedmessagelink == null ? null : new MessageSignature(signer.sign((SignatureUpdater)((signatureupdater_output) -> PlayerChatMessage.updateSignature(signatureupdater_output, signedmessagelink, signedmessagebody))));
      };
   }

   public SignedMessageChain.Decoder decoder(ProfilePublicKey profilepublickey) {
      SignatureValidator signaturevalidator = profilepublickey.createSignatureValidator();
      return (messagesignature, signedmessagebody) -> {
         SignedMessageLink signedmessagelink = this.advanceLink();
         if (signedmessagelink == null) {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.chain_broken"), false);
         } else if (profilepublickey.data().hasExpired()) {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.expiredProfileKey"), false);
         } else {
            PlayerChatMessage playerchatmessage = new PlayerChatMessage(signedmessagelink, messagesignature, signedmessagebody, (Component)null, FilterMask.PASS_THROUGH);
            if (!playerchatmessage.verify(signaturevalidator)) {
               throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.unsigned_chat"), true);
            } else {
               if (playerchatmessage.hasExpiredServer(Instant.now())) {
                  LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)signedmessagebody.content());
               }

               return playerchatmessage;
            }
         }
      };
   }

   @Nullable
   private SignedMessageLink advanceLink() {
      SignedMessageLink signedmessagelink = this.nextLink;
      if (signedmessagelink != null) {
         this.nextLink = signedmessagelink.advance();
      }

      return signedmessagelink;
   }

   public static class DecodeException extends ThrowingComponent {
      private final boolean shouldDisconnect;

      public DecodeException(Component component, boolean flag) {
         super(component);
         this.shouldDisconnect = flag;
      }

      public boolean shouldDisconnect() {
         return this.shouldDisconnect;
      }
   }

   @FunctionalInterface
   public interface Decoder {
      SignedMessageChain.Decoder REJECT_ALL = (messagesignature, signedmessagebody) -> {
         throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.missingProfileKey"), false);
      };

      static SignedMessageChain.Decoder unsigned(UUID uuid) {
         return (messagesignature, signedmessagebody) -> PlayerChatMessage.unsigned(uuid, signedmessagebody.content());
      }

      PlayerChatMessage unpack(@Nullable MessageSignature messagesignature, SignedMessageBody signedmessagebody) throws SignedMessageChain.DecodeException;
   }

   @FunctionalInterface
   public interface Encoder {
      SignedMessageChain.Encoder UNSIGNED = (signedmessagebody) -> null;

      @Nullable
      MessageSignature pack(SignedMessageBody signedmessagebody);
   }
}
