package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RideCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_RIDING = new DynamicCommandExceptionType((object) -> Component.translatable("commands.ride.not_riding", object));
   private static final Dynamic2CommandExceptionType ERROR_ALREADY_RIDING = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.ride.already_riding", object, object1));
   private static final Dynamic2CommandExceptionType ERROR_MOUNT_FAILED = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.ride.mount.failure.generic", object, object1));
   private static final SimpleCommandExceptionType ERROR_MOUNTING_PLAYER = new SimpleCommandExceptionType(Component.translatable("commands.ride.mount.failure.cant_ride_players"));
   private static final SimpleCommandExceptionType ERROR_MOUNTING_LOOP = new SimpleCommandExceptionType(Component.translatable("commands.ride.mount.failure.loop"));
   private static final SimpleCommandExceptionType ERROR_WRONG_DIMENSION = new SimpleCommandExceptionType(Component.translatable("commands.ride.mount.failure.wrong_dimension"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("ride").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("target", EntityArgument.entity()).then(Commands.literal("mount").then(Commands.argument("vehicle", EntityArgument.entity()).executes((commandcontext1) -> mount(commandcontext1.getSource(), EntityArgument.getEntity(commandcontext1, "target"), EntityArgument.getEntity(commandcontext1, "vehicle"))))).then(Commands.literal("dismount").executes((commandcontext) -> dismount(commandcontext.getSource(), EntityArgument.getEntity(commandcontext, "target"))))));
   }

   private static int mount(CommandSourceStack commandsourcestack, Entity entity, Entity entity1) throws CommandSyntaxException {
      Entity entity2 = entity.getVehicle();
      if (entity2 != null) {
         throw ERROR_ALREADY_RIDING.create(entity.getDisplayName(), entity2.getDisplayName());
      } else if (entity1.getType() == EntityType.PLAYER) {
         throw ERROR_MOUNTING_PLAYER.create();
      } else if (entity.getSelfAndPassengers().anyMatch((entity6) -> entity6 == entity1)) {
         throw ERROR_MOUNTING_LOOP.create();
      } else if (entity.level() != entity1.level()) {
         throw ERROR_WRONG_DIMENSION.create();
      } else if (!entity.startRiding(entity1, true)) {
         throw ERROR_MOUNT_FAILED.create(entity.getDisplayName(), entity1.getDisplayName());
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.ride.mount.success", entity.getDisplayName(), entity1.getDisplayName()), true);
         return 1;
      }
   }

   private static int dismount(CommandSourceStack commandsourcestack, Entity entity) throws CommandSyntaxException {
      Entity entity1 = entity.getVehicle();
      if (entity1 == null) {
         throw ERROR_NOT_RIDING.create(entity.getDisplayName());
      } else {
         entity.stopRiding();
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.ride.dismount.success", entity.getDisplayName(), entity1.getDisplayName()), true);
         return 1;
      }
   }
}
