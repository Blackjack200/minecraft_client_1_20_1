package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DamageCommand {
   private static final SimpleCommandExceptionType ERROR_INVULNERABLE = new SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("damage").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("target", EntityArgument.entity()).then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes((commandcontext4) -> damage(commandcontext4.getSource(), EntityArgument.getEntity(commandcontext4, "target"), FloatArgumentType.getFloat(commandcontext4, "amount"), commandcontext4.getSource().getLevel().damageSources().generic())).then(Commands.argument("damageType", ResourceArgument.resource(commandbuildcontext, Registries.DAMAGE_TYPE)).executes((commandcontext3) -> damage(commandcontext3.getSource(), EntityArgument.getEntity(commandcontext3, "target"), FloatArgumentType.getFloat(commandcontext3, "amount"), new DamageSource(ResourceArgument.getResource(commandcontext3, "damageType", Registries.DAMAGE_TYPE)))).then(Commands.literal("at").then(Commands.argument("location", Vec3Argument.vec3()).executes((commandcontext2) -> damage(commandcontext2.getSource(), EntityArgument.getEntity(commandcontext2, "target"), FloatArgumentType.getFloat(commandcontext2, "amount"), new DamageSource(ResourceArgument.getResource(commandcontext2, "damageType", Registries.DAMAGE_TYPE), Vec3Argument.getVec3(commandcontext2, "location")))))).then(Commands.literal("by").then(Commands.argument("entity", EntityArgument.entity()).executes((commandcontext1) -> damage(commandcontext1.getSource(), EntityArgument.getEntity(commandcontext1, "target"), FloatArgumentType.getFloat(commandcontext1, "amount"), new DamageSource(ResourceArgument.getResource(commandcontext1, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(commandcontext1, "entity")))).then(Commands.literal("from").then(Commands.argument("cause", EntityArgument.entity()).executes((commandcontext) -> damage(commandcontext.getSource(), EntityArgument.getEntity(commandcontext, "target"), FloatArgumentType.getFloat(commandcontext, "amount"), new DamageSource(ResourceArgument.getResource(commandcontext, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(commandcontext, "entity"), EntityArgument.getEntity(commandcontext, "cause"))))))))))));
   }

   private static int damage(CommandSourceStack commandsourcestack, Entity entity, float f, DamageSource damagesource) throws CommandSyntaxException {
      if (entity.hurt(damagesource, f)) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.damage.success", f, entity.getDisplayName()), true);
         return 1;
      } else {
         throw ERROR_INVULNERABLE.create();
      }
   }
}
