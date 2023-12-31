package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
   public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(SignedMessageLink::index), UUIDUtil.CODEC.fieldOf("sender").forGetter(SignedMessageLink::sender), UUIDUtil.CODEC.fieldOf("session_id").forGetter(SignedMessageLink::sessionId)).apply(recordcodecbuilder_instance, SignedMessageLink::new));

   public static SignedMessageLink unsigned(UUID uuid) {
      return root(uuid, Util.NIL_UUID);
   }

   public static SignedMessageLink root(UUID uuid, UUID uuid1) {
      return new SignedMessageLink(0, uuid, uuid1);
   }

   public void updateSignature(SignatureUpdater.Output signatureupdater_output) throws SignatureException {
      signatureupdater_output.update(UUIDUtil.uuidToByteArray(this.sender));
      signatureupdater_output.update(UUIDUtil.uuidToByteArray(this.sessionId));
      signatureupdater_output.update(Ints.toByteArray(this.index));
   }

   public boolean isDescendantOf(SignedMessageLink signedmessagelink) {
      return this.index > signedmessagelink.index() && this.sender.equals(signedmessagelink.sender()) && this.sessionId.equals(signedmessagelink.sessionId());
   }

   @Nullable
   public SignedMessageLink advance() {
      return this.index == Integer.MAX_VALUE ? null : new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
   }
}
