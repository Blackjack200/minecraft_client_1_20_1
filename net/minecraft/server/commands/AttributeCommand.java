package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.UUID;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType((object) -> Component.translatable("commands.attribute.failed.entity", object));
   private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.attribute.failed.no_attribute", object, object1));
   private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("commands.attribute.failed.no_modifier", object1, object, object2));
   private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("commands.attribute.failed.modifier_already_present", object2, object1, object));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("attribute").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("target", EntityArgument.entity()).then(Commands.argument("attribute", ResourceArgument.resource(commandbuildcontext, Registries.ATTRIBUTE)).then(Commands.literal("get").executes((commandcontext10) -> getAttributeValue(commandcontext10.getSource(), EntityArgument.getEntity(commandcontext10, "target"), ResourceArgument.getAttribute(commandcontext10, "attribute"), 1.0D)).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes((commandcontext9) -> getAttributeValue(commandcontext9.getSource(), EntityArgument.getEntity(commandcontext9, "target"), ResourceArgument.getAttribute(commandcontext9, "attribute"), DoubleArgumentType.getDouble(commandcontext9, "scale"))))).then(Commands.literal("base").then(Commands.literal("set").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes((commandcontext8) -> setAttributeBase(commandcontext8.getSource(), EntityArgument.getEntity(commandcontext8, "target"), ResourceArgument.getAttribute(commandcontext8, "attribute"), DoubleArgumentType.getDouble(commandcontext8, "value"))))).then(Commands.literal("get").executes((commandcontext7) -> getAttributeBase(commandcontext7.getSource(), EntityArgument.getEntity(commandcontext7, "target"), ResourceArgument.getAttribute(commandcontext7, "attribute"), 1.0D)).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes((commandcontext6) -> getAttributeBase(commandcontext6.getSource(), EntityArgument.getEntity(commandcontext6, "target"), ResourceArgument.getAttribute(commandcontext6, "attribute"), DoubleArgumentType.getDouble(commandcontext6, "scale")))))).then(Commands.literal("modifier").then(Commands.literal("add").then(Commands.argument("uuid", UuidArgument.uuid()).then(Commands.argument("name", StringArgumentType.string()).then(Commands.argument("value", DoubleArgumentType.doubleArg()).then(Commands.literal("add").executes((commandcontext5) -> addModifier(commandcontext5.getSource(), EntityArgument.getEntity(commandcontext5, "target"), ResourceArgument.getAttribute(commandcontext5, "attribute"), UuidArgument.getUuid(commandcontext5, "uuid"), StringArgumentType.getString(commandcontext5, "name"), DoubleArgumentType.getDouble(commandcontext5, "value"), AttributeModifier.Operation.ADDITION))).then(Commands.literal("multiply").executes((commandcontext4) -> addModifier(commandcontext4.getSource(), EntityArgument.getEntity(commandcontext4, "target"), ResourceArgument.getAttribute(commandcontext4, "attribute"), UuidArgument.getUuid(commandcontext4, "uuid"), StringArgumentType.getString(commandcontext4, "name"), DoubleArgumentType.getDouble(commandcontext4, "value"), AttributeModifier.Operation.MULTIPLY_TOTAL))).then(Commands.literal("multiply_base").executes((commandcontext3) -> addModifier(commandcontext3.getSource(), EntityArgument.getEntity(commandcontext3, "target"), ResourceArgument.getAttribute(commandcontext3, "attribute"), UuidArgument.getUuid(commandcontext3, "uuid"), StringArgumentType.getString(commandcontext3, "name"), DoubleArgumentType.getDouble(commandcontext3, "value"), AttributeModifier.Operation.MULTIPLY_BASE))))))).then(Commands.literal("remove").then(Commands.argument("uuid", UuidArgument.uuid()).executes((commandcontext2) -> removeModifier(commandcontext2.getSource(), EntityArgument.getEntity(commandcontext2, "target"), ResourceArgument.getAttribute(commandcontext2, "attribute"), UuidArgument.getUuid(commandcontext2, "uuid"))))).then(Commands.literal("value").then(Commands.literal("get").then(Commands.argument("uuid", UuidArgument.uuid()).executes((commandcontext1) -> getAttributeModifier(commandcontext1.getSource(), EntityArgument.getEntity(commandcontext1, "target"), ResourceArgument.getAttribute(commandcontext1, "attribute"), UuidArgument.getUuid(commandcontext1, "uuid"), 1.0D)).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes((commandcontext) -> getAttributeModifier(commandcontext.getSource(), EntityArgument.getEntity(commandcontext, "target"), ResourceArgument.getAttribute(commandcontext, "attribute"), UuidArgument.getUuid(commandcontext, "uuid"), DoubleArgumentType.getDouble(commandcontext, "scale")))))))))));
   }

   private static AttributeInstance getAttributeInstance(Entity entity, Holder<Attribute> holder) throws CommandSyntaxException {
      AttributeInstance attributeinstance = getLivingEntity(entity).getAttributes().getInstance(holder);
      if (attributeinstance == null) {
         throw ERROR_NO_SUCH_ATTRIBUTE.create(entity.getName(), getAttributeDescription(holder));
      } else {
         return attributeinstance;
      }
   }

   private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
      if (!(entity instanceof LivingEntity)) {
         throw ERROR_NOT_LIVING_ENTITY.create(entity.getName());
      } else {
         return (LivingEntity)entity;
      }
   }

   private static LivingEntity getEntityWithAttribute(Entity entity, Holder<Attribute> holder) throws CommandSyntaxException {
      LivingEntity livingentity = getLivingEntity(entity);
      if (!livingentity.getAttributes().hasAttribute(holder)) {
         throw ERROR_NO_SUCH_ATTRIBUTE.create(entity.getName(), getAttributeDescription(holder));
      } else {
         return livingentity;
      }
   }

   private static int getAttributeValue(CommandSourceStack commandsourcestack, Entity entity, Holder<Attribute> holder, double d0) throws CommandSyntaxException {
      LivingEntity livingentity = getEntityWithAttribute(entity, holder);
      double d1 = livingentity.getAttributeValue(holder);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.attribute.value.get.success", getAttributeDescription(holder), entity.getName(), d1), false);
      return (int)(d1 * d0);
   }

   private static int getAttributeBase(CommandSourceStack commandsourcestack, Entity entity, Holder<Attribute> holder, double d0) throws CommandSyntaxException {
      LivingEntity livingentity = getEntityWithAttribute(entity, holder);
      double d1 = livingentity.getAttributeBaseValue(holder);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.attribute.base_value.get.success", getAttributeDescription(holder), entity.getName(), d1), false);
      return (int)(d1 * d0);
   }

   private static int getAttributeModifier(CommandSourceStack commandsourcestack, Entity entity, Holder<Attribute> holder, UUID uuid, double d0) throws CommandSyntaxException {
      LivingEntity livingentity = getEntityWithAttribute(entity, holder);
      AttributeMap attributemap = livingentity.getAttributes();
      if (!attributemap.hasModifier(holder, uuid)) {
         throw ERROR_NO_SUCH_MODIFIER.create(entity.getName(), getAttributeDescription(holder), uuid);
      } else {
         double d1 = attributemap.getModifierValue(holder, uuid);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.attribute.modifier.value.get.success", uuid, getAttributeDescription(holder), entity.getName(), d1), false);
         return (int)(d1 * d0);
      }
   }

   private static int setAttributeBase(CommandSourceStack commandsourcestack, Entity entity, Holder<Attribute> holder, double d0) throws CommandSyntaxException {
      getAttributeInstance(entity, holder).setBaseValue(d0);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.attribute.base_value.set.success", getAttributeDescription(holder), entity.getName(), d0), false);
      return 1;
   }

   private static int addModifier(CommandSourceStack commandsourcestack, Entity entity, Holder<Attribute> holder, UUID uuid, String s, double d0, AttributeModifier.Operation attributemodifier_operation) throws CommandSyntaxException {
      AttributeInstance attributeinstance = getAttributeInstance(entity, holder);
      AttributeModifier attributemodifier = new AttributeModifier(uuid, s, d0, attributemodifier_operation);
      if (attributeinstance.hasModifier(attributemodifier)) {
         throw ERROR_MODIFIER_ALREADY_PRESENT.create(entity.getName(), getAttributeDescription(holder), uuid);
      } else {
         attributeinstance.addPermanentModifier(attributemodifier);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.attribute.modifier.add.success", uuid, getAttributeDescription(holder), entity.getName()), false);
         return 1;
      }
   }

   private static int removeModifier(CommandSourceStack commandsourcestack, Entity entity, Holder<Attribute> holder, UUID uuid) throws CommandSyntaxException {
      AttributeInstance attributeinstance = getAttributeInstance(entity, holder);
      if (attributeinstance.removePermanentModifier(uuid)) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.attribute.modifier.remove.success", uuid, getAttributeDescription(holder), entity.getName()), false);
         return 1;
      } else {
         throw ERROR_NO_SUCH_MODIFIER.create(entity.getName(), getAttributeDescription(holder), uuid);
      }
   }

   private static Component getAttributeDescription(Holder<Attribute> holder) {
      return Component.translatable(holder.value().getDescriptionId());
   }
}
