package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
   static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("commands.item.target.not_a_container", object, object1, object2));
   private static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("commands.item.source.not_a_container", object, object1, object2));
   static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType((object) -> Component.translatable("commands.item.target.no_such_slot", object));
   private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType((object) -> Component.translatable("commands.item.source.no_such_slot", object));
   private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType((object) -> Component.translatable("commands.item.target.no_changes", object));
   private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.item.target.no_changed.known_item", object, object1));
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (commandcontext, suggestionsbuilder) -> {
      LootDataManager lootdatamanager = commandcontext.getSource().getServer().getLootData();
      return SharedSuggestionProvider.suggestResource(lootdatamanager.getKeys(LootDataType.MODIFIER), suggestionsbuilder);
   };

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("item").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("replace").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(Commands.argument("item", ItemArgument.item(commandbuildcontext)).executes((commandcontext13) -> setBlockItem(commandcontext13.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext13, "pos"), SlotArgument.getSlot(commandcontext13, "slot"), ItemArgument.getItem(commandcontext13, "item").createItemStack(1, false))).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes((commandcontext12) -> setBlockItem(commandcontext12.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext12, "pos"), SlotArgument.getSlot(commandcontext12, "slot"), ItemArgument.getItem(commandcontext12, "item").createItemStack(IntegerArgumentType.getInteger(commandcontext12, "count"), true)))))).then(Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((commandcontext11) -> blockToBlock(commandcontext11.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext11, "source"), SlotArgument.getSlot(commandcontext11, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandcontext11, "pos"), SlotArgument.getSlot(commandcontext11, "slot"))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((commandcontext10) -> blockToBlock(commandcontext10.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext10, "source"), SlotArgument.getSlot(commandcontext10, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandcontext10, "pos"), SlotArgument.getSlot(commandcontext10, "slot"), ResourceLocationArgument.getItemModifier(commandcontext10, "modifier"))))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((commandcontext9) -> entityToBlock(commandcontext9.getSource(), EntityArgument.getEntity(commandcontext9, "source"), SlotArgument.getSlot(commandcontext9, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandcontext9, "pos"), SlotArgument.getSlot(commandcontext9, "slot"))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((commandcontext8) -> entityToBlock(commandcontext8.getSource(), EntityArgument.getEntity(commandcontext8, "source"), SlotArgument.getSlot(commandcontext8, "sourceSlot"), BlockPosArgument.getLoadedBlockPos(commandcontext8, "pos"), SlotArgument.getSlot(commandcontext8, "slot"), ResourceLocationArgument.getItemModifier(commandcontext8, "modifier"))))))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.literal("with").then(Commands.argument("item", ItemArgument.item(commandbuildcontext)).executes((commandcontext7) -> setEntityItem(commandcontext7.getSource(), EntityArgument.getEntities(commandcontext7, "targets"), SlotArgument.getSlot(commandcontext7, "slot"), ItemArgument.getItem(commandcontext7, "item").createItemStack(1, false))).then(Commands.argument("count", IntegerArgumentType.integer(1, 64)).executes((commandcontext6) -> setEntityItem(commandcontext6.getSource(), EntityArgument.getEntities(commandcontext6, "targets"), SlotArgument.getSlot(commandcontext6, "slot"), ItemArgument.getItem(commandcontext6, "item").createItemStack(IntegerArgumentType.getInteger(commandcontext6, "count"), true)))))).then(Commands.literal("from").then(Commands.literal("block").then(Commands.argument("source", BlockPosArgument.blockPos()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((commandcontext5) -> blockToEntities(commandcontext5.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext5, "source"), SlotArgument.getSlot(commandcontext5, "sourceSlot"), EntityArgument.getEntities(commandcontext5, "targets"), SlotArgument.getSlot(commandcontext5, "slot"))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((commandcontext4) -> blockToEntities(commandcontext4.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext4, "source"), SlotArgument.getSlot(commandcontext4, "sourceSlot"), EntityArgument.getEntities(commandcontext4, "targets"), SlotArgument.getSlot(commandcontext4, "slot"), ResourceLocationArgument.getItemModifier(commandcontext4, "modifier"))))))).then(Commands.literal("entity").then(Commands.argument("source", EntityArgument.entity()).then(Commands.argument("sourceSlot", SlotArgument.slot()).executes((commandcontext3) -> entityToEntities(commandcontext3.getSource(), EntityArgument.getEntity(commandcontext3, "source"), SlotArgument.getSlot(commandcontext3, "sourceSlot"), EntityArgument.getEntities(commandcontext3, "targets"), SlotArgument.getSlot(commandcontext3, "slot"))).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((commandcontext2) -> entityToEntities(commandcontext2.getSource(), EntityArgument.getEntity(commandcontext2, "source"), SlotArgument.getSlot(commandcontext2, "sourceSlot"), EntityArgument.getEntities(commandcontext2, "targets"), SlotArgument.getSlot(commandcontext2, "slot"), ResourceLocationArgument.getItemModifier(commandcontext2, "modifier")))))))))))).then(Commands.literal("modify").then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((commandcontext1) -> modifyBlockItem(commandcontext1.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext1, "pos"), SlotArgument.getSlot(commandcontext1, "slot"), ResourceLocationArgument.getItemModifier(commandcontext1, "modifier"))))))).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("slot", SlotArgument.slot()).then(Commands.argument("modifier", ResourceLocationArgument.id()).suggests(SUGGEST_MODIFIER).executes((commandcontext) -> modifyEntityItem(commandcontext.getSource(), EntityArgument.getEntities(commandcontext, "targets"), SlotArgument.getSlot(commandcontext, "slot"), ResourceLocationArgument.getItemModifier(commandcontext, "modifier")))))))));
   }

   private static int modifyBlockItem(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, LootItemFunction lootitemfunction) throws CommandSyntaxException {
      Container container = getContainer(commandsourcestack, blockpos, ERROR_TARGET_NOT_A_CONTAINER);
      if (i >= 0 && i < container.getContainerSize()) {
         ItemStack itemstack = applyModifier(commandsourcestack, lootitemfunction, container.getItem(i));
         container.setItem(i, itemstack);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.item.block.set.success", blockpos.getX(), blockpos.getY(), blockpos.getZ(), itemstack.getDisplayName()), true);
         return 1;
      } else {
         throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
      }
   }

   private static int modifyEntityItem(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, int i, LootItemFunction lootitemfunction) throws CommandSyntaxException {
      Map<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(collection.size());

      for(Entity entity : collection) {
         SlotAccess slotaccess = entity.getSlot(i);
         if (slotaccess != SlotAccess.NULL) {
            ItemStack itemstack = applyModifier(commandsourcestack, lootitemfunction, slotaccess.get().copy());
            if (slotaccess.set(itemstack)) {
               map.put(entity, itemstack);
               if (entity instanceof ServerPlayer) {
                  ((ServerPlayer)entity).containerMenu.broadcastChanges();
               }
            }
         }
      }

      if (map.isEmpty()) {
         throw ERROR_TARGET_NO_CHANGES.create(i);
      } else {
         if (map.size() == 1) {
            Map.Entry<Entity, ItemStack> map_entry = map.entrySet().iterator().next();
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.single", map_entry.getKey().getDisplayName(), map_entry.getValue().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", map.size()), true);
         }

         return map.size();
      }
   }

   private static int setBlockItem(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, ItemStack itemstack) throws CommandSyntaxException {
      Container container = getContainer(commandsourcestack, blockpos, ERROR_TARGET_NOT_A_CONTAINER);
      if (i >= 0 && i < container.getContainerSize()) {
         container.setItem(i, itemstack);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.item.block.set.success", blockpos.getX(), blockpos.getY(), blockpos.getZ(), itemstack.getDisplayName()), true);
         return 1;
      } else {
         throw ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
      }
   }

   private static Container getContainer(CommandSourceStack commandsourcestack, BlockPos blockpos, Dynamic3CommandExceptionType dynamic3commandexceptiontype) throws CommandSyntaxException {
      BlockEntity blockentity = commandsourcestack.getLevel().getBlockEntity(blockpos);
      if (!(blockentity instanceof Container)) {
         throw dynamic3commandexceptiontype.create(blockpos.getX(), blockpos.getY(), blockpos.getZ());
      } else {
         return (Container)blockentity;
      }
   }

   private static int setEntityItem(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection, int i, ItemStack itemstack) throws CommandSyntaxException {
      List<Entity> list = Lists.newArrayListWithCapacity(collection.size());

      for(Entity entity : collection) {
         SlotAccess slotaccess = entity.getSlot(i);
         if (slotaccess != SlotAccess.NULL && slotaccess.set(itemstack.copy())) {
            list.add(entity);
            if (entity instanceof ServerPlayer) {
               ((ServerPlayer)entity).containerMenu.broadcastChanges();
            }
         }
      }

      if (list.isEmpty()) {
         throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(itemstack.getDisplayName(), i);
      } else {
         if (list.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.single", list.iterator().next().getDisplayName(), itemstack.getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", list.size(), itemstack.getDisplayName()), true);
         }

         return list.size();
      }
   }

   private static int blockToEntities(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
      return setEntityItem(commandsourcestack, collection, j, getBlockItem(commandsourcestack, blockpos, i));
   }

   private static int blockToEntities(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, Collection<? extends Entity> collection, int j, LootItemFunction lootitemfunction) throws CommandSyntaxException {
      return setEntityItem(commandsourcestack, collection, j, applyModifier(commandsourcestack, lootitemfunction, getBlockItem(commandsourcestack, blockpos, i)));
   }

   private static int blockToBlock(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, BlockPos blockpos1, int j) throws CommandSyntaxException {
      return setBlockItem(commandsourcestack, blockpos1, j, getBlockItem(commandsourcestack, blockpos, i));
   }

   private static int blockToBlock(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, BlockPos blockpos1, int j, LootItemFunction lootitemfunction) throws CommandSyntaxException {
      return setBlockItem(commandsourcestack, blockpos1, j, applyModifier(commandsourcestack, lootitemfunction, getBlockItem(commandsourcestack, blockpos, i)));
   }

   private static int entityToBlock(CommandSourceStack commandsourcestack, Entity entity, int i, BlockPos blockpos, int j) throws CommandSyntaxException {
      return setBlockItem(commandsourcestack, blockpos, j, getEntityItem(entity, i));
   }

   private static int entityToBlock(CommandSourceStack commandsourcestack, Entity entity, int i, BlockPos blockpos, int j, LootItemFunction lootitemfunction) throws CommandSyntaxException {
      return setBlockItem(commandsourcestack, blockpos, j, applyModifier(commandsourcestack, lootitemfunction, getEntityItem(entity, i)));
   }

   private static int entityToEntities(CommandSourceStack commandsourcestack, Entity entity, int i, Collection<? extends Entity> collection, int j) throws CommandSyntaxException {
      return setEntityItem(commandsourcestack, collection, j, getEntityItem(entity, i));
   }

   private static int entityToEntities(CommandSourceStack commandsourcestack, Entity entity, int i, Collection<? extends Entity> collection, int j, LootItemFunction lootitemfunction) throws CommandSyntaxException {
      return setEntityItem(commandsourcestack, collection, j, applyModifier(commandsourcestack, lootitemfunction, getEntityItem(entity, i)));
   }

   private static ItemStack applyModifier(CommandSourceStack commandsourcestack, LootItemFunction lootitemfunction, ItemStack itemstack) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, commandsourcestack.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity()).create(LootContextParamSets.COMMAND);
      LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
      lootcontext.pushVisitedElement(LootContext.createVisitedEntry(lootitemfunction));
      return lootitemfunction.apply(itemstack, lootcontext);
   }

   private static ItemStack getEntityItem(Entity entity, int i) throws CommandSyntaxException {
      SlotAccess slotaccess = entity.getSlot(i);
      if (slotaccess == SlotAccess.NULL) {
         throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
      } else {
         return slotaccess.get().copy();
      }
   }

   private static ItemStack getBlockItem(CommandSourceStack commandsourcestack, BlockPos blockpos, int i) throws CommandSyntaxException {
      Container container = getContainer(commandsourcestack, blockpos, ERROR_SOURCE_NOT_A_CONTAINER);
      if (i >= 0 && i < container.getContainerSize()) {
         return container.getItem(i).copy();
      } else {
         throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(i);
      }
   }
}
