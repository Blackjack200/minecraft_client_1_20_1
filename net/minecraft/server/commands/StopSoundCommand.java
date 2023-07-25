package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

public class StopSoundCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      RequiredArgumentBuilder<CommandSourceStack, EntitySelector> requiredargumentbuilder = Commands.argument("targets", EntityArgument.players()).executes((commandcontext3) -> stopSound(commandcontext3.getSource(), EntityArgument.getPlayers(commandcontext3, "targets"), (SoundSource)null, (ResourceLocation)null)).then(Commands.literal("*").then(Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS).executes((commandcontext2) -> stopSound(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), (SoundSource)null, ResourceLocationArgument.getId(commandcontext2, "sound")))));

      for(SoundSource soundsource : SoundSource.values()) {
         requiredargumentbuilder.then(Commands.literal(soundsource.getName()).executes((commandcontext1) -> stopSound(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), soundsource, (ResourceLocation)null)).then(Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS).executes((commandcontext) -> stopSound(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), soundsource, ResourceLocationArgument.getId(commandcontext, "sound")))));
      }

      commanddispatcher.register(Commands.literal("stopsound").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(requiredargumentbuilder));
   }

   private static int stopSound(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, @Nullable SoundSource soundsource, @Nullable ResourceLocation resourcelocation) {
      ClientboundStopSoundPacket clientboundstopsoundpacket = new ClientboundStopSoundPacket(resourcelocation, soundsource);

      for(ServerPlayer serverplayer : collection) {
         serverplayer.connection.send(clientboundstopsoundpacket);
      }

      if (soundsource != null) {
         if (resourcelocation != null) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.sound", resourcelocation, soundsource.getName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.stopsound.success.source.any", soundsource.getName()), true);
         }
      } else if (resourcelocation != null) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.sound", resourcelocation), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.stopsound.success.sourceless.any"), true);
      }

      return collection.size();
   }
}
