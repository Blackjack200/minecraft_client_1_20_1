package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType((object) -> Component.translatable("commands.enchant.failed.entity", object));
   private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType((object) -> Component.translatable("commands.enchant.failed.itemless", object));
   private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType((object) -> Component.translatable("commands.enchant.failed.incompatible", object));
   private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.enchant.failed.level", object, object1));
   private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("enchant").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("enchantment", ResourceArgument.resource(commandbuildcontext, Registries.ENCHANTMENT)).executes((commandcontext1) -> enchant(commandcontext1.getSource(), EntityArgument.getEntities(commandcontext1, "targets"), ResourceArgument.getEnchantment(commandcontext1, "enchantment"), 1)).then(Commands.argument("level", IntegerArgumentType.integer(0)).executes((commandcontext) -> enchant(commandcontext.getSource(), EntityArgument.getEntities(commandcontext, "targets"), ResourceArgument.getEnchantment(commandcontext, "enchantment"), IntegerArgumentType.getInteger(commandcontext, "level")))))));
   }

   private static int enchant(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, Holder<Enchantment> holder, int i) throws CommandSyntaxException {
      Enchantment enchantment = holder.value();
      if (i > enchantment.getMaxLevel()) {
         throw ERROR_LEVEL_TOO_HIGH.create(i, enchantment.getMaxLevel());
      } else {
         int j = 0;

         for(Entity entity : collection) {
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity;
               ItemStack itemstack = livingentity.getMainHandItem();
               if (!itemstack.isEmpty()) {
                  if (enchantment.canEnchant(itemstack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemstack).keySet(), enchantment)) {
                     itemstack.enchant(enchantment, i);
                     ++j;
                  } else if (collection.size() == 1) {
                     throw ERROR_INCOMPATIBLE.create(itemstack.getItem().getName(itemstack).getString());
                  }
               } else if (collection.size() == 1) {
                  throw ERROR_NO_ITEM.create(livingentity.getName().getString());
               }
            } else if (collection.size() == 1) {
               throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
            }
         }

         if (j == 0) {
            throw ERROR_NOTHING_HAPPENED.create();
         } else {
            if (collection.size() == 1) {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.enchant.success.single", enchantment.getFullname(i), collection.iterator().next().getDisplayName()), true);
            } else {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.enchant.success.multiple", enchantment.getFullname(i), collection.size()), true);
            }

            return j;
         }
      }
   }
}
