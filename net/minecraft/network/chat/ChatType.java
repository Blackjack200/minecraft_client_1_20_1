package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record ChatType(ChatTypeDecoration chat, ChatTypeDecoration narration) {
   public static final Codec<ChatType> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ChatTypeDecoration.CODEC.fieldOf("chat").forGetter(ChatType::chat), ChatTypeDecoration.CODEC.fieldOf("narration").forGetter(ChatType::narration)).apply(recordcodecbuilder_instance, ChatType::new));
   public static final ChatTypeDecoration DEFAULT_CHAT_DECORATION = ChatTypeDecoration.withSender("chat.type.text");
   public static final ResourceKey<ChatType> CHAT = create("chat");
   public static final ResourceKey<ChatType> SAY_COMMAND = create("say_command");
   public static final ResourceKey<ChatType> MSG_COMMAND_INCOMING = create("msg_command_incoming");
   public static final ResourceKey<ChatType> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");
   public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");
   public static final ResourceKey<ChatType> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");
   public static final ResourceKey<ChatType> EMOTE_COMMAND = create("emote_command");

   private static ResourceKey<ChatType> create(String s) {
      return ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation(s));
   }

   public static void bootstrap(BootstapContext<ChatType> bootstapcontext) {
      bootstapcontext.register(CHAT, new ChatType(DEFAULT_CHAT_DECORATION, ChatTypeDecoration.withSender("chat.type.text.narrate")));
      bootstapcontext.register(SAY_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.announcement"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      bootstapcontext.register(MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      bootstapcontext.register(MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      bootstapcontext.register(TEAM_MSG_COMMAND_INCOMING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.text"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      bootstapcontext.register(TEAM_MSG_COMMAND_OUTGOING, new ChatType(ChatTypeDecoration.teamMessage("chat.type.team.sent"), ChatTypeDecoration.withSender("chat.type.text.narrate")));
      bootstapcontext.register(EMOTE_COMMAND, new ChatType(ChatTypeDecoration.withSender("chat.type.emote"), ChatTypeDecoration.withSender("chat.type.emote")));
   }

   public static ChatType.Bound bind(ResourceKey<ChatType> resourcekey, Entity entity) {
      return bind(resourcekey, entity.level().registryAccess(), entity.getDisplayName());
   }

   public static ChatType.Bound bind(ResourceKey<ChatType> resourcekey, CommandSourceStack commandsourcestack) {
      return bind(resourcekey, commandsourcestack.registryAccess(), commandsourcestack.getDisplayName());
   }

   public static ChatType.Bound bind(ResourceKey<ChatType> resourcekey, RegistryAccess registryaccess, Component component) {
      Registry<ChatType> registry = registryaccess.registryOrThrow(Registries.CHAT_TYPE);
      return registry.getOrThrow(resourcekey).bind(component);
   }

   public ChatType.Bound bind(Component component) {
      return new ChatType.Bound(this, component);
   }

   public static record Bound(ChatType chatType, Component name, @Nullable Component targetName) {
      Bound(ChatType chattype, Component component) {
         this(chattype, component, (Component)null);
      }

      public Component decorate(Component component) {
         return this.chatType.chat().decorate(component, this);
      }

      public Component decorateNarration(Component component) {
         return this.chatType.narration().decorate(component, this);
      }

      public ChatType.Bound withTargetName(Component component) {
         return new ChatType.Bound(this.chatType, this.name, component);
      }

      public ChatType.BoundNetwork toNetwork(RegistryAccess registryaccess) {
         Registry<ChatType> registry = registryaccess.registryOrThrow(Registries.CHAT_TYPE);
         return new ChatType.BoundNetwork(registry.getId(this.chatType), this.name, this.targetName);
      }
   }

   public static record BoundNetwork(int chatType, Component name, @Nullable Component targetName) {
      public BoundNetwork(FriendlyByteBuf friendlybytebuf) {
         this(friendlybytebuf.readVarInt(), friendlybytebuf.readComponent(), friendlybytebuf.readNullable(FriendlyByteBuf::readComponent));
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeVarInt(this.chatType);
         friendlybytebuf.writeComponent(this.name);
         friendlybytebuf.writeNullable(this.targetName, FriendlyByteBuf::writeComponent);
      }

      public Optional<ChatType.Bound> resolve(RegistryAccess registryaccess) {
         Registry<ChatType> registry = registryaccess.registryOrThrow(Registries.CHAT_TYPE);
         ChatType chattype = registry.byId(this.chatType);
         return Optional.ofNullable(chattype).map((chattype1) -> new ChatType.Bound(chattype1, this.name, this.targetName));
      }
   }
}
