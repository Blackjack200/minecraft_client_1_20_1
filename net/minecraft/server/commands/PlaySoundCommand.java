package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PlaySoundCommand {
   private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> requiredargumentbuilder = Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS);

      for(SoundSource soundsource : SoundSource.values()) {
         requiredargumentbuilder.then(source(soundsource));
      }

      commanddispatcher.register(Commands.literal("playsound").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(requiredargumentbuilder));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource soundsource) {
      return Commands.literal(soundsource.getName()).then(Commands.argument("targets", EntityArgument.players()).executes((commandcontext4) -> playSound(commandcontext4.getSource(), EntityArgument.getPlayers(commandcontext4, "targets"), ResourceLocationArgument.getId(commandcontext4, "sound"), soundsource, commandcontext4.getSource().getPosition(), 1.0F, 1.0F, 0.0F)).then(Commands.argument("pos", Vec3Argument.vec3()).executes((commandcontext3) -> playSound(commandcontext3.getSource(), EntityArgument.getPlayers(commandcontext3, "targets"), ResourceLocationArgument.getId(commandcontext3, "sound"), soundsource, Vec3Argument.getVec3(commandcontext3, "pos"), 1.0F, 1.0F, 0.0F)).then(Commands.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((commandcontext2) -> playSound(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), ResourceLocationArgument.getId(commandcontext2, "sound"), soundsource, Vec3Argument.getVec3(commandcontext2, "pos"), commandcontext2.getArgument("volume", Float.class), 1.0F, 0.0F)).then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F)).executes((commandcontext1) -> playSound(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), ResourceLocationArgument.getId(commandcontext1, "sound"), soundsource, Vec3Argument.getVec3(commandcontext1, "pos"), commandcontext1.getArgument("volume", Float.class), commandcontext1.getArgument("pitch", Float.class), 0.0F)).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((commandcontext) -> playSound(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), ResourceLocationArgument.getId(commandcontext, "sound"), soundsource, Vec3Argument.getVec3(commandcontext, "pos"), commandcontext.getArgument("volume", Float.class), commandcontext.getArgument("pitch", Float.class), commandcontext.getArgument("minVolume", Float.class))))))));
   }

   private static int playSound(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, ResourceLocation resourcelocation, SoundSource soundsource, Vec3 vec3, float f, float f1, float f2) throws CommandSyntaxException {
      Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourcelocation));
      double d0 = (double)Mth.square(holder.value().getRange(f));
      int i = 0;
      long j = commandsourcestack.getLevel().getRandom().nextLong();
      Iterator var14 = collection.iterator();

      while(true) {
         ServerPlayer serverplayer;
         Vec3 vec31;
         float f3;
         while(true) {
            if (!var14.hasNext()) {
               if (i == 0) {
                  throw ERROR_TOO_FAR.create();
               }

               if (collection.size() == 1) {
                  commandsourcestack.sendSuccess(() -> Component.translatable("commands.playsound.success.single", resourcelocation, collection.iterator().next().getDisplayName()), true);
               } else {
                  commandsourcestack.sendSuccess(() -> Component.translatable("commands.playsound.success.multiple", resourcelocation, collection.size()), true);
               }

               return i;
            }

            serverplayer = (ServerPlayer)var14.next();
            double d1 = vec3.x - serverplayer.getX();
            double d2 = vec3.y - serverplayer.getY();
            double d3 = vec3.z - serverplayer.getZ();
            double d4 = d1 * d1 + d2 * d2 + d3 * d3;
            vec31 = vec3;
            f3 = f;
            if (!(d4 > d0)) {
               break;
            }

            if (!(f2 <= 0.0F)) {
               double d5 = Math.sqrt(d4);
               vec31 = new Vec3(serverplayer.getX() + d1 / d5 * 2.0D, serverplayer.getY() + d2 / d5 * 2.0D, serverplayer.getZ() + d3 / d5 * 2.0D);
               f3 = f2;
               break;
            }
         }

         serverplayer.connection.send(new ClientboundSoundPacket(holder, soundsource, vec31.x(), vec31.y(), vec31.z(), f3, f1, j));
         ++i;
      }
   }
}
