package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

public class WorldBorderCommand {
   private static final SimpleCommandExceptionType ERROR_SAME_CENTER = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.center.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_SIZE = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.nochange"));
   private static final SimpleCommandExceptionType ERROR_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.small"));
   private static final SimpleCommandExceptionType ERROR_TOO_BIG = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.big", 5.9999968E7D));
   private static final SimpleCommandExceptionType ERROR_TOO_FAR_OUT = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.set.failed.far", 2.9999984E7D));
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_TIME = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.warning.time.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_WARNING_DISTANCE = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.warning.distance.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_BUFFER = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.damage.buffer.failed"));
   private static final SimpleCommandExceptionType ERROR_SAME_DAMAGE_AMOUNT = new SimpleCommandExceptionType(Component.translatable("commands.worldborder.damage.amount.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("worldborder").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("add").then(Commands.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7D, 5.9999968E7D)).executes((commandcontext9) -> setSize(commandcontext9.getSource(), commandcontext9.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(commandcontext9, "distance"), 0L)).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((commandcontext8) -> setSize(commandcontext8.getSource(), commandcontext8.getSource().getLevel().getWorldBorder().getSize() + DoubleArgumentType.getDouble(commandcontext8, "distance"), commandcontext8.getSource().getLevel().getWorldBorder().getLerpRemainingTime() + (long)IntegerArgumentType.getInteger(commandcontext8, "time") * 1000L))))).then(Commands.literal("set").then(Commands.argument("distance", DoubleArgumentType.doubleArg(-5.9999968E7D, 5.9999968E7D)).executes((commandcontext7) -> setSize(commandcontext7.getSource(), DoubleArgumentType.getDouble(commandcontext7, "distance"), 0L)).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((commandcontext6) -> setSize(commandcontext6.getSource(), DoubleArgumentType.getDouble(commandcontext6, "distance"), (long)IntegerArgumentType.getInteger(commandcontext6, "time") * 1000L))))).then(Commands.literal("center").then(Commands.argument("pos", Vec2Argument.vec2()).executes((commandcontext5) -> setCenter(commandcontext5.getSource(), Vec2Argument.getVec2(commandcontext5, "pos"))))).then(Commands.literal("damage").then(Commands.literal("amount").then(Commands.argument("damagePerBlock", FloatArgumentType.floatArg(0.0F)).executes((commandcontext4) -> setDamageAmount(commandcontext4.getSource(), FloatArgumentType.getFloat(commandcontext4, "damagePerBlock"))))).then(Commands.literal("buffer").then(Commands.argument("distance", FloatArgumentType.floatArg(0.0F)).executes((commandcontext3) -> setDamageBuffer(commandcontext3.getSource(), FloatArgumentType.getFloat(commandcontext3, "distance")))))).then(Commands.literal("get").executes((commandcontext2) -> getSize(commandcontext2.getSource()))).then(Commands.literal("warning").then(Commands.literal("distance").then(Commands.argument("distance", IntegerArgumentType.integer(0)).executes((commandcontext1) -> setWarningDistance(commandcontext1.getSource(), IntegerArgumentType.getInteger(commandcontext1, "distance"))))).then(Commands.literal("time").then(Commands.argument("time", IntegerArgumentType.integer(0)).executes((commandcontext) -> setWarningTime(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "time")))))));
   }

   private static int setDamageBuffer(CommandSourceStack commandsourcestack, float f) throws CommandSyntaxException {
      WorldBorder worldborder = commandsourcestack.getServer().overworld().getWorldBorder();
      if (worldborder.getDamageSafeZone() == (double)f) {
         throw ERROR_SAME_DAMAGE_BUFFER.create();
      } else {
         worldborder.setDamageSafeZone((double)f);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", f)), true);
         return (int)f;
      }
   }

   private static int setDamageAmount(CommandSourceStack commandsourcestack, float f) throws CommandSyntaxException {
      WorldBorder worldborder = commandsourcestack.getServer().overworld().getWorldBorder();
      if (worldborder.getDamagePerBlock() == (double)f) {
         throw ERROR_SAME_DAMAGE_AMOUNT.create();
      } else {
         worldborder.setDamagePerBlock((double)f);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", f)), true);
         return (int)f;
      }
   }

   private static int setWarningTime(CommandSourceStack commandsourcestack, int i) throws CommandSyntaxException {
      WorldBorder worldborder = commandsourcestack.getServer().overworld().getWorldBorder();
      if (worldborder.getWarningTime() == i) {
         throw ERROR_SAME_WARNING_TIME.create();
      } else {
         worldborder.setWarningTime(i);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.warning.time.success", i), true);
         return i;
      }
   }

   private static int setWarningDistance(CommandSourceStack commandsourcestack, int i) throws CommandSyntaxException {
      WorldBorder worldborder = commandsourcestack.getServer().overworld().getWorldBorder();
      if (worldborder.getWarningBlocks() == i) {
         throw ERROR_SAME_WARNING_DISTANCE.create();
      } else {
         worldborder.setWarningBlocks(i);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.warning.distance.success", i), true);
         return i;
      }
   }

   private static int getSize(CommandSourceStack commandsourcestack) {
      double d0 = commandsourcestack.getServer().overworld().getWorldBorder().getSize();
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d0)), false);
      return Mth.floor(d0 + 0.5D);
   }

   private static int setCenter(CommandSourceStack commandsourcestack, Vec2 vec2) throws CommandSyntaxException {
      WorldBorder worldborder = commandsourcestack.getServer().overworld().getWorldBorder();
      if (worldborder.getCenterX() == (double)vec2.x && worldborder.getCenterZ() == (double)vec2.y) {
         throw ERROR_SAME_CENTER.create();
      } else if (!((double)Math.abs(vec2.x) > 2.9999984E7D) && !((double)Math.abs(vec2.y) > 2.9999984E7D)) {
         worldborder.setCenter((double)vec2.x, (double)vec2.y);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", vec2.x), String.format(Locale.ROOT, "%.2f", vec2.y)), true);
         return 0;
      } else {
         throw ERROR_TOO_FAR_OUT.create();
      }
   }

   private static int setSize(CommandSourceStack commandsourcestack, double d0, long i) throws CommandSyntaxException {
      WorldBorder worldborder = commandsourcestack.getServer().overworld().getWorldBorder();
      double d1 = worldborder.getSize();
      if (d1 == d0) {
         throw ERROR_SAME_SIZE.create();
      } else if (d0 < 1.0D) {
         throw ERROR_TOO_SMALL.create();
      } else if (d0 > 5.9999968E7D) {
         throw ERROR_TOO_BIG.create();
      } else {
         if (i > 0L) {
            worldborder.lerpSizeBetween(d1, d0, i);
            if (d0 > d1) {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.set.grow", String.format(Locale.ROOT, "%.1f", d0), Long.toString(i / 1000L)), true);
            } else {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.set.shrink", String.format(Locale.ROOT, "%.1f", d0), Long.toString(i / 1000L)), true);
            }
         } else {
            worldborder.setSize(d0);
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.worldborder.set.immediate", String.format(Locale.ROOT, "%.1f", d0)), true);
         }

         return (int)(d0 - d1);
      }
   }
}
