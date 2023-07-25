package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.particle.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("particle").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("name", ParticleArgument.particle(commandbuildcontext)).executes((commandcontext6) -> sendParticles(commandcontext6.getSource(), ParticleArgument.getParticle(commandcontext6, "name"), commandcontext6.getSource().getPosition(), Vec3.ZERO, 0.0F, 0, false, commandcontext6.getSource().getServer().getPlayerList().getPlayers())).then(Commands.argument("pos", Vec3Argument.vec3()).executes((commandcontext5) -> sendParticles(commandcontext5.getSource(), ParticleArgument.getParticle(commandcontext5, "name"), Vec3Argument.getVec3(commandcontext5, "pos"), Vec3.ZERO, 0.0F, 0, false, commandcontext5.getSource().getServer().getPlayerList().getPlayers())).then(Commands.argument("delta", Vec3Argument.vec3(false)).then(Commands.argument("speed", FloatArgumentType.floatArg(0.0F)).then(Commands.argument("count", IntegerArgumentType.integer(0)).executes((commandcontext4) -> sendParticles(commandcontext4.getSource(), ParticleArgument.getParticle(commandcontext4, "name"), Vec3Argument.getVec3(commandcontext4, "pos"), Vec3Argument.getVec3(commandcontext4, "delta"), FloatArgumentType.getFloat(commandcontext4, "speed"), IntegerArgumentType.getInteger(commandcontext4, "count"), false, commandcontext4.getSource().getServer().getPlayerList().getPlayers())).then(Commands.literal("force").executes((commandcontext3) -> sendParticles(commandcontext3.getSource(), ParticleArgument.getParticle(commandcontext3, "name"), Vec3Argument.getVec3(commandcontext3, "pos"), Vec3Argument.getVec3(commandcontext3, "delta"), FloatArgumentType.getFloat(commandcontext3, "speed"), IntegerArgumentType.getInteger(commandcontext3, "count"), true, commandcontext3.getSource().getServer().getPlayerList().getPlayers())).then(Commands.argument("viewers", EntityArgument.players()).executes((commandcontext2) -> sendParticles(commandcontext2.getSource(), ParticleArgument.getParticle(commandcontext2, "name"), Vec3Argument.getVec3(commandcontext2, "pos"), Vec3Argument.getVec3(commandcontext2, "delta"), FloatArgumentType.getFloat(commandcontext2, "speed"), IntegerArgumentType.getInteger(commandcontext2, "count"), true, EntityArgument.getPlayers(commandcontext2, "viewers"))))).then(Commands.literal("normal").executes((commandcontext1) -> sendParticles(commandcontext1.getSource(), ParticleArgument.getParticle(commandcontext1, "name"), Vec3Argument.getVec3(commandcontext1, "pos"), Vec3Argument.getVec3(commandcontext1, "delta"), FloatArgumentType.getFloat(commandcontext1, "speed"), IntegerArgumentType.getInteger(commandcontext1, "count"), false, commandcontext1.getSource().getServer().getPlayerList().getPlayers())).then(Commands.argument("viewers", EntityArgument.players()).executes((commandcontext) -> sendParticles(commandcontext.getSource(), ParticleArgument.getParticle(commandcontext, "name"), Vec3Argument.getVec3(commandcontext, "pos"), Vec3Argument.getVec3(commandcontext, "delta"), FloatArgumentType.getFloat(commandcontext, "speed"), IntegerArgumentType.getInteger(commandcontext, "count"), false, EntityArgument.getPlayers(commandcontext, "viewers")))))))))));
   }

   private static int sendParticles(CommandSourceStack commandsourcestack, ParticleOptions particleoptions, Vec3 vec3, Vec3 vec31, float f, int i, boolean flag, Collection<ServerPlayer> collection) throws CommandSyntaxException {
      int j = 0;

      for(ServerPlayer serverplayer : collection) {
         if (commandsourcestack.getLevel().sendParticles(serverplayer, particleoptions, flag, vec3.x, vec3.y, vec3.z, i, vec31.x, vec31.y, vec31.z, (double)f)) {
            ++j;
         }
      }

      if (j == 0) {
         throw ERROR_FAILED.create();
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.particle.success", BuiltInRegistries.PARTICLE_TYPE.getKey(particleoptions.getType()).toString()), true);
         return j;
      }
   }
}
