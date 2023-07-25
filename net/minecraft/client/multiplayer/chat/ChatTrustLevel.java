package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;

public enum ChatTrustLevel implements StringRepresentable {
   SECURE("secure"),
   MODIFIED("modified"),
   NOT_SECURE("not_secure");

   public static final Codec<ChatTrustLevel> CODEC = StringRepresentable.fromEnum(ChatTrustLevel::values);
   private final String serializedName;

   private ChatTrustLevel(String s) {
      this.serializedName = s;
   }

   public static ChatTrustLevel evaluate(PlayerChatMessage playerchatmessage, Component component, Instant instant) {
      if (playerchatmessage.hasSignature() && !playerchatmessage.hasExpiredClient(instant)) {
         return isModified(playerchatmessage, component) ? MODIFIED : SECURE;
      } else {
         return NOT_SECURE;
      }
   }

   private static boolean isModified(PlayerChatMessage playerchatmessage, Component component) {
      if (!component.getString().contains(playerchatmessage.signedContent())) {
         return true;
      } else {
         Component component1 = playerchatmessage.unsignedContent();
         return component1 == null ? false : containsModifiedStyle(component1);
      }
   }

   private static boolean containsModifiedStyle(Component component) {
      return component.visit((style, s) -> isModifiedStyle(style) ? Optional.of(true) : Optional.empty(), Style.EMPTY).orElse(false);
   }

   private static boolean isModifiedStyle(Style style) {
      return !style.getFont().equals(Style.DEFAULT_FONT);
   }

   public boolean isNotSecure() {
      return this == NOT_SECURE;
   }

   @Nullable
   public GuiMessageTag createTag(PlayerChatMessage playerchatmessage) {
      GuiMessageTag var10000;
      switch (this) {
         case MODIFIED:
            var10000 = GuiMessageTag.chatModified(playerchatmessage.signedContent());
            break;
         case NOT_SECURE:
            var10000 = GuiMessageTag.chatNotSecure();
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public String getSerializedName() {
      return this.serializedName;
   }
}
