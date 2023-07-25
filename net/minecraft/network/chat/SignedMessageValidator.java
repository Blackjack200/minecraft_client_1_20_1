package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;

@FunctionalInterface
public interface SignedMessageValidator {
   SignedMessageValidator ACCEPT_UNSIGNED = (playerchatmessage) -> !playerchatmessage.hasSignature();
   SignedMessageValidator REJECT_ALL = (playerchatmessage) -> false;

   boolean updateAndValidate(PlayerChatMessage playerchatmessage);

   public static class KeyBased implements SignedMessageValidator {
      private final SignatureValidator validator;
      @Nullable
      private PlayerChatMessage lastMessage;
      private boolean isChainValid = true;

      public KeyBased(SignatureValidator signaturevalidator) {
         this.validator = signaturevalidator;
      }

      private boolean validateChain(PlayerChatMessage playerchatmessage) {
         if (playerchatmessage.equals(this.lastMessage)) {
            return true;
         } else {
            return this.lastMessage == null || playerchatmessage.link().isDescendantOf(this.lastMessage.link());
         }
      }

      public boolean updateAndValidate(PlayerChatMessage playerchatmessage) {
         this.isChainValid = this.isChainValid && playerchatmessage.verify(this.validator) && this.validateChain(playerchatmessage);
         if (!this.isChainValid) {
            return false;
         } else {
            this.lastMessage = playerchatmessage;
            return true;
         }
      }
   }
}
