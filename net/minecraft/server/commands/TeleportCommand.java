package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = commanddispatcher.register(Commands.literal("teleport").requires((commandsourcestack1) -> commandsourcestack1.hasPermission(2)).then(Commands.argument("location", Vec3Argument.vec3()).executes((commandcontext7) -> teleportToPos(commandcontext7.getSource(), Collections.singleton(commandcontext7.getSource().getEntityOrException()), commandcontext7.getSource().getLevel(), Vec3Argument.getCoordinates(commandcontext7, "location"), WorldCoordinates.current(), (TeleportCommand.LookAt)null))).then(Commands.argument("destination", EntityArgument.entity()).executes((commandcontext6) -> teleportToEntity(commandcontext6.getSource(), Collections.singleton(commandcontext6.getSource().getEntityOrException()), EntityArgument.getEntity(commandcontext6, "destination")))).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("location", Vec3Argument.vec3()).executes((commandcontext5) -> teleportToPos(commandcontext5.getSource(), EntityArgument.getEntities(commandcontext5, "targets"), commandcontext5.getSource().getLevel(), Vec3Argument.getCoordinates(commandcontext5, "location"), (Coordinates)null, (TeleportCommand.LookAt)null)).then(Commands.argument("rotation", RotationArgument.rotation()).executes((commandcontext4) -> teleportToPos(commandcontext4.getSource(), EntityArgument.getEntities(commandcontext4, "targets"), commandcontext4.getSource().getLevel(), Vec3Argument.getCoordinates(commandcontext4, "location"), RotationArgument.getRotation(commandcontext4, "rotation"), (TeleportCommand.LookAt)null))).then(Commands.literal("facing").then(Commands.literal("entity").then(Commands.argument("facingEntity", EntityArgument.entity()).executes((commandcontext3) -> teleportToPos(commandcontext3.getSource(), EntityArgument.getEntities(commandcontext3, "targets"), commandcontext3.getSource().getLevel(), Vec3Argument.getCoordinates(commandcontext3, "location"), (Coordinates)null, new TeleportCommand.LookAt(EntityArgument.getEntity(commandcontext3, "facingEntity"), EntityAnchorArgument.Anchor.FEET))).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes((commandcontext2) -> teleportToPos(commandcontext2.getSource(), EntityArgument.getEntities(commandcontext2, "targets"), commandcontext2.getSource().getLevel(), Vec3Argument.getCoordinates(commandcontext2, "location"), (Coordinates)null, new TeleportCommand.LookAt(EntityArgument.getEntity(commandcontext2, "facingEntity"), EntityAnchorArgument.getAnchor(commandcontext2, "facingAnchor"))))))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes((commandcontext1) -> teleportToPos(commandcontext1.getSource(), EntityArgument.getEntities(commandcontext1, "targets"), commandcontext1.getSource().getLevel(), Vec3Argument.getCoordinates(commandcontext1, "location"), (Coordinates)null, new TeleportCommand.LookAt(Vec3Argument.getVec3(commandcontext1, "facingLocation"))))))).then(Commands.argument("destination", EntityArgument.entity()).executes((commandcontext) -> teleportToEntity(commandcontext.getSource(), EntityArgument.getEntities(commandcontext, "targets"), EntityArgument.getEntity(commandcontext, "destination"))))));
      commanddispatcher.register(Commands.literal("tp").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).redirect(literalcommandnode));
   }

   private static int teleportToEntity(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, Entity entity) throws CommandSyntaxException {
      for(Entity entity1 : collection) {
         performTeleport(commandsourcestack, entity1, (ServerLevel)entity.level(), entity.getX(), entity.getY(), entity.getZ(), EnumSet.noneOf(RelativeMovement.class), entity.getYRot(), entity.getXRot(), (TeleportCommand.LookAt)null);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.single", collection.iterator().next().getDisplayName(), entity.getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", collection.size(), entity.getDisplayName()), true);
      }

      return collection.size();
   }

   private static int teleportToPos(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, ServerLevel serverlevel, Coordinates coordinates, @Nullable Coordinates coordinates1, @Nullable TeleportCommand.LookAt teleportcommand_lookat) throws CommandSyntaxException {
      Vec3 vec3 = coordinates.getPosition(commandsourcestack);
      Vec2 vec2 = coordinates1 == null ? null : coordinates1.getRotation(commandsourcestack);
      Set<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
      if (coordinates.isXRelative()) {
         set.add(RelativeMovement.X);
      }

      if (coordinates.isYRelative()) {
         set.add(RelativeMovement.Y);
      }

      if (coordinates.isZRelative()) {
         set.add(RelativeMovement.Z);
      }

      if (coordinates1 == null) {
         set.add(RelativeMovement.X_ROT);
         set.add(RelativeMovement.Y_ROT);
      } else {
         if (coordinates1.isXRelative()) {
            set.add(RelativeMovement.X_ROT);
         }

         if (coordinates1.isYRelative()) {
            set.add(RelativeMovement.Y_ROT);
         }
      }

      for(Entity entity : collection) {
         if (coordinates1 == null) {
            performTeleport(commandsourcestack, entity, serverlevel, vec3.x, vec3.y, vec3.z, set, entity.getYRot(), entity.getXRot(), teleportcommand_lookat);
         } else {
            performTeleport(commandsourcestack, entity, serverlevel, vec3.x, vec3.y, vec3.z, set, vec2.y, vec2.x, teleportcommand_lookat);
         }
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", collection.iterator().next().getDisplayName(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", collection.size(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
      }

      return collection.size();
   }

   private static String formatDouble(double d0) {
      return String.format(Locale.ROOT, "%f", d0);
   }

   private static void performTeleport(CommandSourceStack commandsourcestack, Entity entity, ServerLevel serverlevel, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1, @Nullable TeleportCommand.LookAt teleportcommand_lookat) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);
      if (!Level.isInSpawnableBounds(blockpos)) {
         throw INVALID_POSITION.create();
      } else {
         float f2 = Mth.wrapDegrees(f);
         float f3 = Mth.wrapDegrees(f1);
         if (entity.teleportTo(serverlevel, d0, d1, d2, set, f2, f3)) {
            if (teleportcommand_lookat != null) {
               teleportcommand_lookat.perform(commandsourcestack, entity);
            }

            label23: {
               if (entity instanceof LivingEntity) {
                  LivingEntity livingentity = (LivingEntity)entity;
                  if (livingentity.isFallFlying()) {
                     break label23;
                  }
               }

               entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
               entity.setOnGround(true);
            }

            if (entity instanceof PathfinderMob) {
               PathfinderMob pathfindermob = (PathfinderMob)entity;
               pathfindermob.getNavigation().stop();
            }

         }
      }
   }

   static class LookAt {
      private final Vec3 position;
      private final Entity entity;
      private final EntityAnchorArgument.Anchor anchor;

      public LookAt(Entity entity, EntityAnchorArgument.Anchor entityanchorargument_anchor) {
         this.entity = entity;
         this.anchor = entityanchorargument_anchor;
         this.position = entityanchorargument_anchor.apply(entity);
      }

      public LookAt(Vec3 vec3) {
         this.entity = null;
         this.position = vec3;
         this.anchor = null;
      }

      public void perform(CommandSourceStack commandsourcestack, Entity entity) {
         if (this.entity != null) {
            if (entity instanceof ServerPlayer) {
               ((ServerPlayer)entity).lookAt(commandsourcestack.getAnchor(), this.entity, this.anchor);
            } else {
               entity.lookAt(commandsourcestack.getAnchor(), this.position);
            }
         } else {
            entity.lookAt(commandsourcestack.getAnchor(), this.position);
         }

      }
   }
}
