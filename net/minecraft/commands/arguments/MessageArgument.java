package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

   public static MessageArgument message() {
      return new MessageArgument();
   }

   public static Component getMessage(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      MessageArgument.Message messageargument_message = commandcontext.getArgument(s, MessageArgument.Message.class);
      return messageargument_message.resolveComponent(commandcontext.getSource());
   }

   public static void resolveChatMessage(CommandContext<CommandSourceStack> commandcontext, String s, Consumer<PlayerChatMessage> consumer) throws CommandSyntaxException {
      MessageArgument.Message messageargument_message = commandcontext.getArgument(s, MessageArgument.Message.class);
      CommandSourceStack commandsourcestack = commandcontext.getSource();
      Component component = messageargument_message.resolveComponent(commandsourcestack);
      CommandSigningContext commandsigningcontext = commandsourcestack.getSigningContext();
      PlayerChatMessage playerchatmessage = commandsigningcontext.getArgument(s);
      if (playerchatmessage != null) {
         resolveSignedMessage(consumer, commandsourcestack, playerchatmessage.withUnsignedContent(component));
      } else {
         resolveDisguisedMessage(consumer, commandsourcestack, PlayerChatMessage.system(messageargument_message.text).withUnsignedContent(component));
      }

   }

   private static void resolveSignedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandsourcestack, PlayerChatMessage playerchatmessage) {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      CompletableFuture<FilteredText> completablefuture = filterPlainText(commandsourcestack, playerchatmessage);
      CompletableFuture<Component> completablefuture1 = minecraftserver.getChatDecorator().decorate(commandsourcestack.getPlayer(), playerchatmessage.decoratedContent());
      commandsourcestack.getChatMessageChainer().append((executor) -> CompletableFuture.allOf(completablefuture, completablefuture1).thenAcceptAsync((ovoid) -> {
            PlayerChatMessage playerchatmessage3 = playerchatmessage.withUnsignedContent(completablefuture1.join()).filter(completablefuture.join().mask());
            consumer.accept(playerchatmessage3);
         }, executor));
   }

   private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandsourcestack, PlayerChatMessage playerchatmessage) {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      CompletableFuture<Component> completablefuture = minecraftserver.getChatDecorator().decorate(commandsourcestack.getPlayer(), playerchatmessage.decoratedContent());
      commandsourcestack.getChatMessageChainer().append((executor) -> completablefuture.thenAcceptAsync((component) -> consumer.accept(playerchatmessage.withUnsignedContent(component)), executor));
   }

   private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack commandsourcestack, PlayerChatMessage playerchatmessage) {
      ServerPlayer serverplayer = commandsourcestack.getPlayer();
      return serverplayer != null && playerchatmessage.hasSignatureFrom(serverplayer.getUUID()) ? serverplayer.getTextFilter().processStreamMessage(playerchatmessage.signedContent()) : CompletableFuture.completedFuture(FilteredText.passThrough(playerchatmessage.signedContent()));
   }

   public MessageArgument.Message parse(StringReader stringreader) throws CommandSyntaxException {
      return MessageArgument.Message.parseText(stringreader, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Message {
      final String text;
      private final MessageArgument.Part[] parts;

      public Message(String s, MessageArgument.Part[] amessageargument_part) {
         this.text = s;
         this.parts = amessageargument_part;
      }

      public String getText() {
         return this.text;
      }

      public MessageArgument.Part[] getParts() {
         return this.parts;
      }

      Component resolveComponent(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
         return this.toComponent(commandsourcestack, commandsourcestack.hasPermission(2));
      }

      public Component toComponent(CommandSourceStack commandsourcestack, boolean flag) throws CommandSyntaxException {
         if (this.parts.length != 0 && flag) {
            MutableComponent mutablecomponent = Component.literal(this.text.substring(0, this.parts[0].getStart()));
            int i = this.parts[0].getStart();

            for(MessageArgument.Part messageargument_part : this.parts) {
               Component component = messageargument_part.toComponent(commandsourcestack);
               if (i < messageargument_part.getStart()) {
                  mutablecomponent.append(this.text.substring(i, messageargument_part.getStart()));
               }

               if (component != null) {
                  mutablecomponent.append(component);
               }

               i = messageargument_part.getEnd();
            }

            if (i < this.text.length()) {
               mutablecomponent.append(this.text.substring(i));
            }

            return mutablecomponent;
         } else {
            return Component.literal(this.text);
         }
      }

      public static MessageArgument.Message parseText(StringReader stringreader, boolean flag) throws CommandSyntaxException {
         String s = stringreader.getString().substring(stringreader.getCursor(), stringreader.getTotalLength());
         if (!flag) {
            stringreader.setCursor(stringreader.getTotalLength());
            return new MessageArgument.Message(s, new MessageArgument.Part[0]);
         } else {
            List<MessageArgument.Part> list = Lists.newArrayList();
            int i = stringreader.getCursor();

            while(true) {
               int j;
               EntitySelector entityselector;
               while(true) {
                  if (!stringreader.canRead()) {
                     return new MessageArgument.Message(s, list.toArray(new MessageArgument.Part[0]));
                  }

                  if (stringreader.peek() == '@') {
                     j = stringreader.getCursor();

                     try {
                        EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader);
                        entityselector = entityselectorparser.parse();
                        break;
                     } catch (CommandSyntaxException var8) {
                        if (var8.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE && var8.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                           throw var8;
                        }

                        stringreader.setCursor(j + 1);
                     }
                  } else {
                     stringreader.skip();
                  }
               }

               list.add(new MessageArgument.Part(j - i, stringreader.getCursor() - i, entityselector));
            }
         }
      }
   }

   public static class Part {
      private final int start;
      private final int end;
      private final EntitySelector selector;

      public Part(int i, int j, EntitySelector entityselector) {
         this.start = i;
         this.end = j;
         this.selector = entityselector;
      }

      public int getStart() {
         return this.start;
      }

      public int getEnd() {
         return this.end;
      }

      public EntitySelector getSelector() {
         return this.selector;
      }

      @Nullable
      public Component toComponent(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
         return EntitySelector.joinNames(this.selector.findEntities(commandsourcestack));
      }
   }
}
