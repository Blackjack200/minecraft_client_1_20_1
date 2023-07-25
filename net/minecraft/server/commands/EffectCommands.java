package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EffectCommands {
   private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.give.failed"));
   private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.clear.everything.failed"));
   private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.clear.specific.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("effect").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("clear").executes((commandcontext9) -> clearEffects(commandcontext9.getSource(), ImmutableList.of(commandcontext9.getSource().getEntityOrException()))).then(Commands.argument("targets", EntityArgument.entities()).executes((commandcontext8) -> clearEffects(commandcontext8.getSource(), EntityArgument.getEntities(commandcontext8, "targets"))).then(Commands.argument("effect", ResourceArgument.resource(commandbuildcontext, Registries.MOB_EFFECT)).executes((commandcontext7) -> clearEffect(commandcontext7.getSource(), EntityArgument.getEntities(commandcontext7, "targets"), ResourceArgument.getMobEffect(commandcontext7, "effect")))))).then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("effect", ResourceArgument.resource(commandbuildcontext, Registries.MOB_EFFECT)).executes((commandcontext6) -> giveEffect(commandcontext6.getSource(), EntityArgument.getEntities(commandcontext6, "targets"), ResourceArgument.getMobEffect(commandcontext6, "effect"), (Integer)null, 0, true)).then(Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000)).executes((commandcontext5) -> giveEffect(commandcontext5.getSource(), EntityArgument.getEntities(commandcontext5, "targets"), ResourceArgument.getMobEffect(commandcontext5, "effect"), IntegerArgumentType.getInteger(commandcontext5, "seconds"), 0, true)).then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes((commandcontext4) -> giveEffect(commandcontext4.getSource(), EntityArgument.getEntities(commandcontext4, "targets"), ResourceArgument.getMobEffect(commandcontext4, "effect"), IntegerArgumentType.getInteger(commandcontext4, "seconds"), IntegerArgumentType.getInteger(commandcontext4, "amplifier"), true)).then(Commands.argument("hideParticles", BoolArgumentType.bool()).executes((commandcontext3) -> giveEffect(commandcontext3.getSource(), EntityArgument.getEntities(commandcontext3, "targets"), ResourceArgument.getMobEffect(commandcontext3, "effect"), IntegerArgumentType.getInteger(commandcontext3, "seconds"), IntegerArgumentType.getInteger(commandcontext3, "amplifier"), !BoolArgumentType.getBool(commandcontext3, "hideParticles")))))).then(Commands.literal("infinite").executes((commandcontext2) -> giveEffect(commandcontext2.getSource(), EntityArgument.getEntities(commandcontext2, "targets"), ResourceArgument.getMobEffect(commandcontext2, "effect"), -1, 0, true)).then(Commands.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes((commandcontext1) -> giveEffect(commandcontext1.getSource(), EntityArgument.getEntities(commandcontext1, "targets"), ResourceArgument.getMobEffect(commandcontext1, "effect"), -1, IntegerArgumentType.getInteger(commandcontext1, "amplifier"), true)).then(Commands.argument("hideParticles", BoolArgumentType.bool()).executes((commandcontext) -> giveEffect(commandcontext.getSource(), EntityArgument.getEntities(commandcontext, "targets"), ResourceArgument.getMobEffect(commandcontext, "effect"), -1, IntegerArgumentType.getInteger(commandcontext, "amplifier"), !BoolArgumentType.getBool(commandcontext, "hideParticles"))))))))));
   }

   private static int giveEffect(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, Holder<MobEffect> holder, @Nullable Integer integer, int i, boolean flag) throws CommandSyntaxException {
      MobEffect mobeffect = holder.value();
      int j = 0;
      int k;
      if (integer != null) {
         if (mobeffect.isInstantenous()) {
            k = integer;
         } else if (integer == -1) {
            k = -1;
         } else {
            k = integer * 20;
         }
      } else if (mobeffect.isInstantenous()) {
         k = 1;
      } else {
         k = 600;
      }

      for(Entity entity : collection) {
         if (entity instanceof LivingEntity) {
            MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, k, i, false, flag);
            if (((LivingEntity)entity).addEffect(mobeffectinstance, commandsourcestack.getEntity())) {
               ++j;
            }
         }
      }

      if (j == 0) {
         throw ERROR_GIVE_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.effect.give.success.single", mobeffect.getDisplayName(), collection.iterator().next().getDisplayName(), k / 20), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.effect.give.success.multiple", mobeffect.getDisplayName(), collection.size(), k / 20), true);
         }

         return j;
      }
   }

   private static int clearEffects(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : collection) {
         if (entity instanceof LivingEntity && ((LivingEntity)entity).removeAllEffects()) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_CLEAR_EVERYTHING_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.single", collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.multiple", collection.size()), true);
         }

         return i;
      }
   }

   private static int clearEffect(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, Holder<MobEffect> holder) throws CommandSyntaxException {
      MobEffect mobeffect = holder.value();
      int i = 0;

      for(Entity entity : collection) {
         if (entity instanceof LivingEntity && ((LivingEntity)entity).removeEffect(mobeffect)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_CLEAR_SPECIFIC_FAILED.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.effect.clear.specific.success.single", mobeffect.getDisplayName(), collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.effect.clear.specific.success.multiple", mobeffect.getDisplayName(), collection.size()), true);
         }

         return i;
      }
   }
}
