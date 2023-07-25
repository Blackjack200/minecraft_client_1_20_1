package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LootCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_TABLE = (commandcontext, suggestionsbuilder) -> {
      LootDataManager lootdatamanager = commandcontext.getSource().getServer().getLootData();
      return SharedSuggestionProvider.suggestResource(lootdatamanager.getKeys(LootDataType.TABLE), suggestionsbuilder);
   };
   private static final DynamicCommandExceptionType ERROR_NO_HELD_ITEMS = new DynamicCommandExceptionType((object) -> Component.translatable("commands.drop.no_held_items", object));
   private static final DynamicCommandExceptionType ERROR_NO_LOOT_TABLE = new DynamicCommandExceptionType((object) -> Component.translatable("commands.drop.no_loot_table", object));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(addTargets(Commands.literal("loot").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)), (argumentbuilder, lootcommand_dropconsumer) -> argumentbuilder.then(Commands.literal("fish").then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(SUGGEST_LOOT_TABLE).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext9) -> dropFishingLoot(commandcontext9, ResourceLocationArgument.getId(commandcontext9, "loot_table"), BlockPosArgument.getLoadedBlockPos(commandcontext9, "pos"), ItemStack.EMPTY, lootcommand_dropconsumer)).then(Commands.argument("tool", ItemArgument.item(commandbuildcontext)).executes((commandcontext8) -> dropFishingLoot(commandcontext8, ResourceLocationArgument.getId(commandcontext8, "loot_table"), BlockPosArgument.getLoadedBlockPos(commandcontext8, "pos"), ItemArgument.getItem(commandcontext8, "tool").createItemStack(1, false), lootcommand_dropconsumer))).then(Commands.literal("mainhand").executes((commandcontext7) -> dropFishingLoot(commandcontext7, ResourceLocationArgument.getId(commandcontext7, "loot_table"), BlockPosArgument.getLoadedBlockPos(commandcontext7, "pos"), getSourceHandItem(commandcontext7.getSource(), EquipmentSlot.MAINHAND), lootcommand_dropconsumer))).then(Commands.literal("offhand").executes((commandcontext6) -> dropFishingLoot(commandcontext6, ResourceLocationArgument.getId(commandcontext6, "loot_table"), BlockPosArgument.getLoadedBlockPos(commandcontext6, "pos"), getSourceHandItem(commandcontext6.getSource(), EquipmentSlot.OFFHAND), lootcommand_dropconsumer)))))).then(Commands.literal("loot").then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(SUGGEST_LOOT_TABLE).executes((commandcontext5) -> dropChestLoot(commandcontext5, ResourceLocationArgument.getId(commandcontext5, "loot_table"), lootcommand_dropconsumer)))).then(Commands.literal("kill").then(Commands.argument("target", EntityArgument.entity()).executes((commandcontext4) -> dropKillLoot(commandcontext4, EntityArgument.getEntity(commandcontext4, "target"), lootcommand_dropconsumer)))).then(Commands.literal("mine").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext3) -> dropBlockLoot(commandcontext3, BlockPosArgument.getLoadedBlockPos(commandcontext3, "pos"), ItemStack.EMPTY, lootcommand_dropconsumer)).then(Commands.argument("tool", ItemArgument.item(commandbuildcontext)).executes((commandcontext2) -> dropBlockLoot(commandcontext2, BlockPosArgument.getLoadedBlockPos(commandcontext2, "pos"), ItemArgument.getItem(commandcontext2, "tool").createItemStack(1, false), lootcommand_dropconsumer))).then(Commands.literal("mainhand").executes((commandcontext1) -> dropBlockLoot(commandcontext1, BlockPosArgument.getLoadedBlockPos(commandcontext1, "pos"), getSourceHandItem(commandcontext1.getSource(), EquipmentSlot.MAINHAND), lootcommand_dropconsumer))).then(Commands.literal("offhand").executes((commandcontext) -> dropBlockLoot(commandcontext, BlockPosArgument.getLoadedBlockPos(commandcontext, "pos"), getSourceHandItem(commandcontext.getSource(), EquipmentSlot.OFFHAND), lootcommand_dropconsumer)))))));
   }

   private static <T extends ArgumentBuilder<CommandSourceStack, T>> T addTargets(T argumentbuilder, LootCommand.TailProvider lootcommand_tailprovider) {
      return argumentbuilder.then(Commands.literal("replace").then(Commands.literal("entity").then(Commands.argument("entities", EntityArgument.entities()).then(lootcommand_tailprovider.construct(Commands.argument("slot", SlotArgument.slot()), (commandcontext6, list6, lootcommand_callback6) -> entityReplace(EntityArgument.getEntities(commandcontext6, "entities"), SlotArgument.getSlot(commandcontext6, "slot"), list6.size(), list6, lootcommand_callback6)).then(lootcommand_tailprovider.construct(Commands.argument("count", IntegerArgumentType.integer(0)), (commandcontext5, list5, lootcommand_callback5) -> entityReplace(EntityArgument.getEntities(commandcontext5, "entities"), SlotArgument.getSlot(commandcontext5, "slot"), IntegerArgumentType.getInteger(commandcontext5, "count"), list5, lootcommand_callback5)))))).then(Commands.literal("block").then(Commands.argument("targetPos", BlockPosArgument.blockPos()).then(lootcommand_tailprovider.construct(Commands.argument("slot", SlotArgument.slot()), (commandcontext4, list4, lootcommand_callback4) -> blockReplace(commandcontext4.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext4, "targetPos"), SlotArgument.getSlot(commandcontext4, "slot"), list4.size(), list4, lootcommand_callback4)).then(lootcommand_tailprovider.construct(Commands.argument("count", IntegerArgumentType.integer(0)), (commandcontext3, list3, lootcommand_callback3) -> blockReplace(commandcontext3.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext3, "targetPos"), IntegerArgumentType.getInteger(commandcontext3, "slot"), IntegerArgumentType.getInteger(commandcontext3, "count"), list3, lootcommand_callback3))))))).then(Commands.literal("insert").then(lootcommand_tailprovider.construct(Commands.argument("targetPos", BlockPosArgument.blockPos()), (commandcontext2, list2, lootcommand_callback2) -> blockDistribute(commandcontext2.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext2, "targetPos"), list2, lootcommand_callback2)))).then(Commands.literal("give").then(lootcommand_tailprovider.construct(Commands.argument("players", EntityArgument.players()), (commandcontext1, list1, lootcommand_callback1) -> playerGive(EntityArgument.getPlayers(commandcontext1, "players"), list1, lootcommand_callback1)))).then(Commands.literal("spawn").then(lootcommand_tailprovider.construct(Commands.argument("targetPos", Vec3Argument.vec3()), (commandcontext, list, lootcommand_callback) -> dropInWorld(commandcontext.getSource(), Vec3Argument.getVec3(commandcontext, "targetPos"), list, lootcommand_callback))));
   }

   private static Container getContainer(CommandSourceStack commandsourcestack, BlockPos blockpos) throws CommandSyntaxException {
      BlockEntity blockentity = commandsourcestack.getLevel().getBlockEntity(blockpos);
      if (!(blockentity instanceof Container)) {
         throw ItemCommands.ERROR_TARGET_NOT_A_CONTAINER.create(blockpos.getX(), blockpos.getY(), blockpos.getZ());
      } else {
         return (Container)blockentity;
      }
   }

   private static int blockDistribute(CommandSourceStack commandsourcestack, BlockPos blockpos, List<ItemStack> list, LootCommand.Callback lootcommand_callback) throws CommandSyntaxException {
      Container container = getContainer(commandsourcestack, blockpos);
      List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());

      for(ItemStack itemstack : list) {
         if (distributeToContainer(container, itemstack.copy())) {
            container.setChanged();
            list1.add(itemstack);
         }
      }

      lootcommand_callback.accept(list1);
      return list1.size();
   }

   private static boolean distributeToContainer(Container container, ItemStack itemstack) {
      boolean flag = false;

      for(int i = 0; i < container.getContainerSize() && !itemstack.isEmpty(); ++i) {
         ItemStack itemstack1 = container.getItem(i);
         if (container.canPlaceItem(i, itemstack)) {
            if (itemstack1.isEmpty()) {
               container.setItem(i, itemstack);
               flag = true;
               break;
            }

            if (canMergeItems(itemstack1, itemstack)) {
               int j = itemstack.getMaxStackSize() - itemstack1.getCount();
               int k = Math.min(itemstack.getCount(), j);
               itemstack.shrink(k);
               itemstack1.grow(k);
               flag = true;
            }
         }
      }

      return flag;
   }

   private static int blockReplace(CommandSourceStack commandsourcestack, BlockPos blockpos, int i, int j, List<ItemStack> list, LootCommand.Callback lootcommand_callback) throws CommandSyntaxException {
      Container container = getContainer(commandsourcestack, blockpos);
      int k = container.getContainerSize();
      if (i >= 0 && i < k) {
         List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());

         for(int l = 0; l < j; ++l) {
            int i1 = i + l;
            ItemStack itemstack = l < list.size() ? list.get(l) : ItemStack.EMPTY;
            if (container.canPlaceItem(i1, itemstack)) {
               container.setItem(i1, itemstack);
               list1.add(itemstack);
            }
         }

         lootcommand_callback.accept(list1);
         return list1.size();
      } else {
         throw ItemCommands.ERROR_TARGET_INAPPLICABLE_SLOT.create(i);
      }
   }

   private static boolean canMergeItems(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack.getCount() <= itemstack.getMaxStackSize() && ItemStack.isSameItemSameTags(itemstack, itemstack1);
   }

   private static int playerGive(Collection<ServerPlayer> collection, List<ItemStack> list, LootCommand.Callback lootcommand_callback) throws CommandSyntaxException {
      List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());

      for(ItemStack itemstack : list) {
         for(ServerPlayer serverplayer : collection) {
            if (serverplayer.getInventory().add(itemstack.copy())) {
               list1.add(itemstack);
            }
         }
      }

      lootcommand_callback.accept(list1);
      return list1.size();
   }

   private static void setSlots(Entity entity, List<ItemStack> list, int i, int j, List<ItemStack> list1) {
      for(int k = 0; k < j; ++k) {
         ItemStack itemstack = k < list.size() ? list.get(k) : ItemStack.EMPTY;
         SlotAccess slotaccess = entity.getSlot(i + k);
         if (slotaccess != SlotAccess.NULL && slotaccess.set(itemstack.copy())) {
            list1.add(itemstack);
         }
      }

   }

   private static int entityReplace(Collection<? extends Entity> collection, int i, int j, List<ItemStack> list, LootCommand.Callback lootcommand_callback) throws CommandSyntaxException {
      List<ItemStack> list1 = Lists.newArrayListWithCapacity(list.size());

      for(Entity entity : collection) {
         if (entity instanceof ServerPlayer serverplayer) {
            setSlots(entity, list, i, j, list1);
            serverplayer.containerMenu.broadcastChanges();
         } else {
            setSlots(entity, list, i, j, list1);
         }
      }

      lootcommand_callback.accept(list1);
      return list1.size();
   }

   private static int dropInWorld(CommandSourceStack commandsourcestack, Vec3 vec3, List<ItemStack> list, LootCommand.Callback lootcommand_callback) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      list.forEach((itemstack) -> {
         ItemEntity itementity = new ItemEntity(serverlevel, vec3.x, vec3.y, vec3.z, itemstack.copy());
         itementity.setDefaultPickUpDelay();
         serverlevel.addFreshEntity(itementity);
      });
      lootcommand_callback.accept(list);
      return list.size();
   }

   private static void callback(CommandSourceStack commandsourcestack, List<ItemStack> list) {
      if (list.size() == 1) {
         ItemStack itemstack = list.get(0);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.drop.success.single", itemstack.getCount(), itemstack.getDisplayName()), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.drop.success.multiple", list.size()), false);
      }

   }

   private static void callback(CommandSourceStack commandsourcestack, List<ItemStack> list, ResourceLocation resourcelocation) {
      if (list.size() == 1) {
         ItemStack itemstack = list.get(0);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.drop.success.single_with_table", itemstack.getCount(), itemstack.getDisplayName(), resourcelocation), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.drop.success.multiple_with_table", list.size(), resourcelocation), false);
      }

   }

   private static ItemStack getSourceHandItem(CommandSourceStack commandsourcestack, EquipmentSlot equipmentslot) throws CommandSyntaxException {
      Entity entity = commandsourcestack.getEntityOrException();
      if (entity instanceof LivingEntity) {
         return ((LivingEntity)entity).getItemBySlot(equipmentslot);
      } else {
         throw ERROR_NO_HELD_ITEMS.create(entity.getDisplayName());
      }
   }

   private static int dropBlockLoot(CommandContext<CommandSourceStack> commandcontext, BlockPos blockpos, ItemStack itemstack, LootCommand.DropConsumer lootcommand_dropconsumer) throws CommandSyntaxException {
      CommandSourceStack commandsourcestack = commandcontext.getSource();
      ServerLevel serverlevel = commandsourcestack.getLevel();
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
      LootParams.Builder lootparams_builder = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.BLOCK_STATE, blockstate).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity()).withParameter(LootContextParams.TOOL, itemstack);
      List<ItemStack> list = blockstate.getDrops(lootparams_builder);
      return lootcommand_dropconsumer.accept(commandcontext, list, (list1) -> callback(commandsourcestack, list1, blockstate.getBlock().getLootTable()));
   }

   private static int dropKillLoot(CommandContext<CommandSourceStack> commandcontext, Entity entity, LootCommand.DropConsumer lootcommand_dropconsumer) throws CommandSyntaxException {
      if (!(entity instanceof LivingEntity)) {
         throw ERROR_NO_LOOT_TABLE.create(entity.getDisplayName());
      } else {
         ResourceLocation resourcelocation = ((LivingEntity)entity).getLootTable();
         CommandSourceStack commandsourcestack = commandcontext.getSource();
         LootParams.Builder lootparams_builder = new LootParams.Builder(commandsourcestack.getLevel());
         Entity entity1 = commandsourcestack.getEntity();
         if (entity1 instanceof Player) {
            Player player = (Player)entity1;
            lootparams_builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
         }

         lootparams_builder.withParameter(LootContextParams.DAMAGE_SOURCE, entity.damageSources().magic());
         lootparams_builder.withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, entity1);
         lootparams_builder.withOptionalParameter(LootContextParams.KILLER_ENTITY, entity1);
         lootparams_builder.withParameter(LootContextParams.THIS_ENTITY, entity);
         lootparams_builder.withParameter(LootContextParams.ORIGIN, commandsourcestack.getPosition());
         LootParams lootparams = lootparams_builder.create(LootContextParamSets.ENTITY);
         LootTable loottable = commandsourcestack.getServer().getLootData().getLootTable(resourcelocation);
         List<ItemStack> list = loottable.getRandomItems(lootparams);
         return lootcommand_dropconsumer.accept(commandcontext, list, (list1) -> callback(commandsourcestack, list1, resourcelocation));
      }
   }

   private static int dropChestLoot(CommandContext<CommandSourceStack> commandcontext, ResourceLocation resourcelocation, LootCommand.DropConsumer lootcommand_dropconsumer) throws CommandSyntaxException {
      CommandSourceStack commandsourcestack = commandcontext.getSource();
      LootParams lootparams = (new LootParams.Builder(commandsourcestack.getLevel())).withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity()).withParameter(LootContextParams.ORIGIN, commandsourcestack.getPosition()).create(LootContextParamSets.CHEST);
      return drop(commandcontext, resourcelocation, lootparams, lootcommand_dropconsumer);
   }

   private static int dropFishingLoot(CommandContext<CommandSourceStack> commandcontext, ResourceLocation resourcelocation, BlockPos blockpos, ItemStack itemstack, LootCommand.DropConsumer lootcommand_dropconsumer) throws CommandSyntaxException {
      CommandSourceStack commandsourcestack = commandcontext.getSource();
      LootParams lootparams = (new LootParams.Builder(commandsourcestack.getLevel())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, itemstack).withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity()).create(LootContextParamSets.FISHING);
      return drop(commandcontext, resourcelocation, lootparams, lootcommand_dropconsumer);
   }

   private static int drop(CommandContext<CommandSourceStack> commandcontext, ResourceLocation resourcelocation, LootParams lootparams, LootCommand.DropConsumer lootcommand_dropconsumer) throws CommandSyntaxException {
      CommandSourceStack commandsourcestack = commandcontext.getSource();
      LootTable loottable = commandsourcestack.getServer().getLootData().getLootTable(resourcelocation);
      List<ItemStack> list = loottable.getRandomItems(lootparams);
      return lootcommand_dropconsumer.accept(commandcontext, list, (list1) -> callback(commandsourcestack, list1));
   }

   @FunctionalInterface
   interface Callback {
      void accept(List<ItemStack> list) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface DropConsumer {
      int accept(CommandContext<CommandSourceStack> commandcontext, List<ItemStack> list, LootCommand.Callback lootcommand_callback) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface TailProvider {
      ArgumentBuilder<CommandSourceStack, ?> construct(ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, LootCommand.DropConsumer lootcommand_dropconsumer);
   }
}
