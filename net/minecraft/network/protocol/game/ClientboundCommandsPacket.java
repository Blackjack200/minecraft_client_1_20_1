package net.minecraft.network.protocol.game;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
   private static final byte MASK_TYPE = 3;
   private static final byte FLAG_EXECUTABLE = 4;
   private static final byte FLAG_REDIRECT = 8;
   private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
   private static final byte TYPE_ROOT = 0;
   private static final byte TYPE_LITERAL = 1;
   private static final byte TYPE_ARGUMENT = 2;
   private final int rootIndex;
   private final List<ClientboundCommandsPacket.Entry> entries;

   public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootcommandnode) {
      Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap = enumerateNodes(rootcommandnode);
      this.entries = createEntries(object2intmap);
      this.rootIndex = object2intmap.getInt(rootcommandnode);
   }

   public ClientboundCommandsPacket(FriendlyByteBuf friendlybytebuf) {
      this.entries = friendlybytebuf.readList(ClientboundCommandsPacket::readNode);
      this.rootIndex = friendlybytebuf.readVarInt();
      validateEntries(this.entries);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(this.entries, (friendlybytebuf1, clientboundcommandspacket_entry) -> clientboundcommandspacket_entry.write(friendlybytebuf1));
      friendlybytebuf.writeVarInt(this.rootIndex);
   }

   private static void validateEntries(List<ClientboundCommandsPacket.Entry> list, BiPredicate<ClientboundCommandsPacket.Entry, IntSet> bipredicate) {
      IntSet intset = new IntOpenHashSet(IntSets.fromTo(0, list.size()));

      while(!intset.isEmpty()) {
         boolean flag = intset.removeIf((i) -> bipredicate.test(list.get(i), intset));
         if (!flag) {
            throw new IllegalStateException("Server sent an impossible command tree");
         }
      }

   }

   private static void validateEntries(List<ClientboundCommandsPacket.Entry> list) {
      validateEntries(list, ClientboundCommandsPacket.Entry::canBuild);
      validateEntries(list, ClientboundCommandsPacket.Entry::canResolve);
   }

   private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> rootcommandnode) {
      Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap = new Object2IntOpenHashMap<>();
      Queue<CommandNode<SharedSuggestionProvider>> queue = Queues.newArrayDeque();
      queue.add(rootcommandnode);

      CommandNode<SharedSuggestionProvider> commandnode;
      while((commandnode = queue.poll()) != null) {
         if (!object2intmap.containsKey(commandnode)) {
            int i = object2intmap.size();
            object2intmap.put(commandnode, i);
            queue.addAll(commandnode.getChildren());
            if (commandnode.getRedirect() != null) {
               queue.add(commandnode.getRedirect());
            }
         }
      }

      return object2intmap;
   }

   private static List<ClientboundCommandsPacket.Entry> createEntries(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap) {
      ObjectArrayList<ClientboundCommandsPacket.Entry> objectarraylist = new ObjectArrayList<>(object2intmap.size());
      objectarraylist.size(object2intmap.size());

      for(Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> object2intmap_entry : Object2IntMaps.fastIterable(object2intmap)) {
         objectarraylist.set(object2intmap_entry.getIntValue(), createEntry(object2intmap_entry.getKey(), object2intmap));
      }

      return objectarraylist;
   }

   private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf friendlybytebuf1) {
      byte b0 = friendlybytebuf1.readByte();
      int[] aint = friendlybytebuf1.readVarIntArray();
      int i = (b0 & 8) != 0 ? friendlybytebuf1.readVarInt() : 0;
      ClientboundCommandsPacket.NodeStub clientboundcommandspacket_nodestub = read(friendlybytebuf1, b0);
      return new ClientboundCommandsPacket.Entry(clientboundcommandspacket_nodestub, b0, i, aint);
   }

   @Nullable
   private static ClientboundCommandsPacket.NodeStub read(FriendlyByteBuf friendlybytebuf, byte b0) {
      int i = b0 & 3;
      if (i == 2) {
         String s = friendlybytebuf.readUtf();
         int j = friendlybytebuf.readVarInt();
         ArgumentTypeInfo<?, ?> argumenttypeinfo = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId(j);
         if (argumenttypeinfo == null) {
            return null;
         } else {
            ArgumentTypeInfo.Template<?> argumenttypeinfo_template = argumenttypeinfo.deserializeFromNetwork(friendlybytebuf);
            ResourceLocation resourcelocation = (b0 & 16) != 0 ? friendlybytebuf.readResourceLocation() : null;
            return new ClientboundCommandsPacket.ArgumentNodeStub(s, argumenttypeinfo_template, resourcelocation);
         }
      } else if (i == 1) {
         String s1 = friendlybytebuf.readUtf();
         return new ClientboundCommandsPacket.LiteralNodeStub(s1);
      } else {
         return null;
      }
   }

   private static ClientboundCommandsPacket.Entry createEntry(CommandNode<SharedSuggestionProvider> commandnode, Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap) {
      int i = 0;
      int j;
      if (commandnode.getRedirect() != null) {
         i |= 8;
         j = object2intmap.getInt(commandnode.getRedirect());
      } else {
         j = 0;
      }

      if (commandnode.getCommand() != null) {
         i |= 4;
      }

      ClientboundCommandsPacket.NodeStub clientboundcommandspacket_nodestub;
      if (commandnode instanceof RootCommandNode) {
         i |= 0;
         clientboundcommandspacket_nodestub = null;
      } else if (commandnode instanceof ArgumentCommandNode) {
         ArgumentCommandNode<SharedSuggestionProvider, ?> argumentcommandnode = (ArgumentCommandNode)commandnode;
         clientboundcommandspacket_nodestub = new ClientboundCommandsPacket.ArgumentNodeStub(argumentcommandnode);
         i |= 2;
         if (argumentcommandnode.getCustomSuggestions() != null) {
            i |= 16;
         }
      } else {
         if (!(commandnode instanceof LiteralCommandNode)) {
            throw new UnsupportedOperationException("Unknown node type " + commandnode);
         }

         LiteralCommandNode literalcommandnode = (LiteralCommandNode)commandnode;
         clientboundcommandspacket_nodestub = new ClientboundCommandsPacket.LiteralNodeStub(literalcommandnode.getLiteral());
         i |= 1;
      }

      int[] aint = commandnode.getChildren().stream().mapToInt(object2intmap::getInt).toArray();
      return new ClientboundCommandsPacket.Entry(clientboundcommandspacket_nodestub, i, j, aint);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleCommands(this);
   }

   public RootCommandNode<SharedSuggestionProvider> getRoot(CommandBuildContext commandbuildcontext) {
      return (RootCommandNode)(new ClientboundCommandsPacket.NodeResolver(commandbuildcontext, this.entries)).resolve(this.rootIndex);
   }

   static class ArgumentNodeStub implements ClientboundCommandsPacket.NodeStub {
      private final String id;
      private final ArgumentTypeInfo.Template<?> argumentType;
      @Nullable
      private final ResourceLocation suggestionId;

      @Nullable
      private static ResourceLocation getSuggestionId(@Nullable SuggestionProvider<SharedSuggestionProvider> suggestionprovider) {
         return suggestionprovider != null ? SuggestionProviders.getName(suggestionprovider) : null;
      }

      ArgumentNodeStub(String s, ArgumentTypeInfo.Template<?> argumenttypeinfo_template, @Nullable ResourceLocation resourcelocation) {
         this.id = s;
         this.argumentType = argumenttypeinfo_template;
         this.suggestionId = resourcelocation;
      }

      public ArgumentNodeStub(ArgumentCommandNode<SharedSuggestionProvider, ?> argumentcommandnode) {
         this(argumentcommandnode.getName(), ArgumentTypeInfos.unpack(argumentcommandnode.getType()), getSuggestionId(argumentcommandnode.getCustomSuggestions()));
      }

      public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandbuildcontext) {
         ArgumentType<?> argumenttype = this.argumentType.instantiate(commandbuildcontext);
         RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredargumentbuilder = RequiredArgumentBuilder.argument(this.id, argumenttype);
         if (this.suggestionId != null) {
            requiredargumentbuilder.suggests(SuggestionProviders.getProvider(this.suggestionId));
         }

         return requiredargumentbuilder;
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeUtf(this.id);
         serializeCap(friendlybytebuf, this.argumentType);
         if (this.suggestionId != null) {
            friendlybytebuf.writeResourceLocation(this.suggestionId);
         }

      }

      private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf friendlybytebuf, ArgumentTypeInfo.Template<A> argumenttypeinfo_template) {
         serializeCap(friendlybytebuf, argumenttypeinfo_template.type(), argumenttypeinfo_template);
      }

      private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf friendlybytebuf, ArgumentTypeInfo<A, T> argumenttypeinfo, ArgumentTypeInfo.Template<A> argumenttypeinfo_template) {
         friendlybytebuf.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(argumenttypeinfo));
         argumenttypeinfo.serializeToNetwork((T)argumenttypeinfo_template, friendlybytebuf);
      }
   }

   static class Entry {
      @Nullable
      final ClientboundCommandsPacket.NodeStub stub;
      final int flags;
      final int redirect;
      final int[] children;

      Entry(@Nullable ClientboundCommandsPacket.NodeStub clientboundcommandspacket_nodestub, int i, int j, int[] aint) {
         this.stub = clientboundcommandspacket_nodestub;
         this.flags = i;
         this.redirect = j;
         this.children = aint;
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeByte(this.flags);
         friendlybytebuf.writeVarIntArray(this.children);
         if ((this.flags & 8) != 0) {
            friendlybytebuf.writeVarInt(this.redirect);
         }

         if (this.stub != null) {
            this.stub.write(friendlybytebuf);
         }

      }

      public boolean canBuild(IntSet intset) {
         if ((this.flags & 8) != 0) {
            return !intset.contains(this.redirect);
         } else {
            return true;
         }
      }

      public boolean canResolve(IntSet intset) {
         for(int i : this.children) {
            if (intset.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   static class LiteralNodeStub implements ClientboundCommandsPacket.NodeStub {
      private final String id;

      LiteralNodeStub(String s) {
         this.id = s;
      }

      public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandbuildcontext) {
         return LiteralArgumentBuilder.literal(this.id);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeUtf(this.id);
      }
   }

   static class NodeResolver {
      private final CommandBuildContext context;
      private final List<ClientboundCommandsPacket.Entry> entries;
      private final List<CommandNode<SharedSuggestionProvider>> nodes;

      NodeResolver(CommandBuildContext commandbuildcontext, List<ClientboundCommandsPacket.Entry> list) {
         this.context = commandbuildcontext;
         this.entries = list;
         ObjectArrayList<CommandNode<SharedSuggestionProvider>> objectarraylist = new ObjectArrayList<>();
         objectarraylist.size(list.size());
         this.nodes = objectarraylist;
      }

      public CommandNode<SharedSuggestionProvider> resolve(int i) {
         CommandNode<SharedSuggestionProvider> commandnode = this.nodes.get(i);
         if (commandnode != null) {
            return commandnode;
         } else {
            ClientboundCommandsPacket.Entry clientboundcommandspacket_entry = this.entries.get(i);
            CommandNode<SharedSuggestionProvider> commandnode1;
            if (clientboundcommandspacket_entry.stub == null) {
               commandnode1 = new RootCommandNode<>();
            } else {
               ArgumentBuilder<SharedSuggestionProvider, ?> argumentbuilder = clientboundcommandspacket_entry.stub.build(this.context);
               if ((clientboundcommandspacket_entry.flags & 8) != 0) {
                  argumentbuilder.redirect(this.resolve(clientboundcommandspacket_entry.redirect));
               }

               if ((clientboundcommandspacket_entry.flags & 4) != 0) {
                  argumentbuilder.executes((commandcontext) -> 0);
               }

               commandnode1 = argumentbuilder.build();
            }

            this.nodes.set(i, commandnode1);

            for(int j : clientboundcommandspacket_entry.children) {
               CommandNode<SharedSuggestionProvider> commandnode3 = this.resolve(j);
               if (!(commandnode3 instanceof RootCommandNode)) {
                  commandnode1.addChild(commandnode3);
               }
            }

            return commandnode1;
         }
      }
   }

   interface NodeStub {
      ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandbuildcontext);

      void write(FriendlyByteBuf friendlybytebuf);
   }
}
