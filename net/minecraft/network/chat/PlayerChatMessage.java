package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record PlayerChatMessage(SignedMessageLink link, @Nullable MessageSignature signature, SignedMessageBody signedBody, @Nullable Component unsignedContent, FilterMask filterMask) {
   public static final MapCodec<PlayerChatMessage> MAP_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(SignedMessageLink.CODEC.fieldOf("link").forGetter(PlayerChatMessage::link), MessageSignature.CODEC.optionalFieldOf("signature").forGetter((playerchatmessage1) -> Optional.ofNullable(playerchatmessage1.signature)), SignedMessageBody.MAP_CODEC.forGetter(PlayerChatMessage::signedBody), ExtraCodecs.COMPONENT.optionalFieldOf("unsigned_content").forGetter((playerchatmessage) -> Optional.ofNullable(playerchatmessage.unsignedContent)), FilterMask.CODEC.optionalFieldOf("filter_mask", FilterMask.PASS_THROUGH).forGetter(PlayerChatMessage::filterMask)).apply(recordcodecbuilder_instance, (signedmessagelink, optional, signedmessagebody, optional1, filtermask) -> new PlayerChatMessage(signedmessagelink, optional.orElse((MessageSignature)null), signedmessagebody, optional1.orElse((Component)null), filtermask)));
   private static final UUID SYSTEM_SENDER = Util.NIL_UUID;
   public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
   public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

   public static PlayerChatMessage system(String s) {
      return unsigned(SYSTEM_SENDER, s);
   }

   public static PlayerChatMessage unsigned(UUID uuid, String s) {
      SignedMessageBody signedmessagebody = SignedMessageBody.unsigned(s);
      SignedMessageLink signedmessagelink = SignedMessageLink.unsigned(uuid);
      return new PlayerChatMessage(signedmessagelink, (MessageSignature)null, signedmessagebody, (Component)null, FilterMask.PASS_THROUGH);
   }

   public PlayerChatMessage withUnsignedContent(Component component) {
      Component component1 = !component.equals(Component.literal(this.signedContent())) ? component : null;
      return new PlayerChatMessage(this.link, this.signature, this.signedBody, component1, this.filterMask);
   }

   public PlayerChatMessage removeUnsignedContent() {
      return this.unsignedContent != null ? new PlayerChatMessage(this.link, this.signature, this.signedBody, (Component)null, this.filterMask) : this;
   }

   public PlayerChatMessage filter(FilterMask filtermask) {
      return this.filterMask.equals(filtermask) ? this : new PlayerChatMessage(this.link, this.signature, this.signedBody, this.unsignedContent, filtermask);
   }

   public PlayerChatMessage filter(boolean flag) {
      return this.filter(flag ? this.filterMask : FilterMask.PASS_THROUGH);
   }

   public static void updateSignature(SignatureUpdater.Output signatureupdater_output, SignedMessageLink signedmessagelink, SignedMessageBody signedmessagebody) throws SignatureException {
      signatureupdater_output.update(Ints.toByteArray(1));
      signedmessagelink.updateSignature(signatureupdater_output);
      signedmessagebody.updateSignature(signatureupdater_output);
   }

   public boolean verify(SignatureValidator signaturevalidator) {
      return this.signature != null && this.signature.verify(signaturevalidator, (signatureupdater_output) -> updateSignature(signatureupdater_output, this.link, this.signedBody));
   }

   public String signedContent() {
      return this.signedBody.content();
   }

   public Component decoratedContent() {
      return Objects.requireNonNullElseGet(this.unsignedContent, () -> Component.literal(this.signedContent()));
   }

   public Instant timeStamp() {
      return this.signedBody.timeStamp();
   }

   public long salt() {
      return this.signedBody.salt();
   }

   public boolean hasExpiredServer(Instant instant) {
      return instant.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
   }

   public boolean hasExpiredClient(Instant instant) {
      return instant.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
   }

   public UUID sender() {
      return this.link.sender();
   }

   public boolean isSystem() {
      return this.sender().equals(SYSTEM_SENDER);
   }

   public boolean hasSignature() {
      return this.signature != null;
   }

   public boolean hasSignatureFrom(UUID uuid) {
      return this.hasSignature() && this.link.sender().equals(uuid);
   }

   public boolean isFullyFiltered() {
      return this.filterMask.isFullyFiltered();
   }
}
