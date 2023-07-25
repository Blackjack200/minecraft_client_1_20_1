package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
   public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
   private static final int MAX_ARGUMENT_COUNT = 8;
   private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

   public ArgumentSignatures(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
   }

   @Nullable
   public MessageSignature get(String s) {
      for(ArgumentSignatures.Entry argumentsignatures_entry : this.entries) {
         if (argumentsignatures_entry.name.equals(s)) {
            return argumentsignatures_entry.signature;
         }
      }

      return null;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(this.entries, (friendlybytebuf1, argumentsignatures_entry) -> argumentsignatures_entry.write(friendlybytebuf1));
   }

   public static ArgumentSignatures signCommand(SignableCommand<?> signablecommand, ArgumentSignatures.Signer argumentsignatures_signer) {
      List<ArgumentSignatures.Entry> list = signablecommand.arguments().stream().map((signablecommand_argument) -> {
         MessageSignature messagesignature = argumentsignatures_signer.sign(signablecommand_argument.value());
         return messagesignature != null ? new ArgumentSignatures.Entry(signablecommand_argument.name(), messagesignature) : null;
      }).filter(Objects::nonNull).toList();
      return new ArgumentSignatures(list);
   }

   public static record Entry(String name, MessageSignature signature) {
      final String name;
      final MessageSignature signature;

      public Entry(FriendlyByteBuf friendlybytebuf) {
         this(friendlybytebuf.readUtf(16), MessageSignature.read(friendlybytebuf));
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeUtf(this.name, 16);
         MessageSignature.write(friendlybytebuf, this.signature);
      }
   }

   @FunctionalInterface
   public interface Signer {
      @Nullable
      MessageSignature sign(String s);
   }
}
