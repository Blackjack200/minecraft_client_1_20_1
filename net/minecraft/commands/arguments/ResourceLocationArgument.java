package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType((object) -> Component.translatable("advancement.advancementNotFound", object));
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType((object) -> Component.translatable("recipe.notFound", object));
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType((object) -> Component.translatable("predicate.unknown", object));
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType((object) -> Component.translatable("item_modifier.unknown", object));

   public static ResourceLocationArgument id() {
      return new ResourceLocationArgument();
   }

   public static Advancement getAdvancement(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      ResourceLocation resourcelocation = getId(commandcontext, s);
      Advancement advancement = commandcontext.getSource().getServer().getAdvancements().getAdvancement(resourcelocation);
      if (advancement == null) {
         throw ERROR_UNKNOWN_ADVANCEMENT.create(resourcelocation);
      } else {
         return advancement;
      }
   }

   public static Recipe<?> getRecipe(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      RecipeManager recipemanager = commandcontext.getSource().getServer().getRecipeManager();
      ResourceLocation resourcelocation = getId(commandcontext, s);
      return recipemanager.byKey(resourcelocation).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(resourcelocation));
   }

   public static LootItemCondition getPredicate(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      ResourceLocation resourcelocation = getId(commandcontext, s);
      LootDataManager lootdatamanager = commandcontext.getSource().getServer().getLootData();
      LootItemCondition lootitemcondition = lootdatamanager.getElement(LootDataType.PREDICATE, resourcelocation);
      if (lootitemcondition == null) {
         throw ERROR_UNKNOWN_PREDICATE.create(resourcelocation);
      } else {
         return lootitemcondition;
      }
   }

   public static LootItemFunction getItemModifier(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      ResourceLocation resourcelocation = getId(commandcontext, s);
      LootDataManager lootdatamanager = commandcontext.getSource().getServer().getLootData();
      LootItemFunction lootitemfunction = lootdatamanager.getElement(LootDataType.MODIFIER, resourcelocation);
      if (lootitemfunction == null) {
         throw ERROR_UNKNOWN_ITEM_MODIFIER.create(resourcelocation);
      } else {
         return lootitemfunction;
      }
   }

   public static ResourceLocation getId(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, ResourceLocation.class);
   }

   public ResourceLocation parse(StringReader stringreader) throws CommandSyntaxException {
      return ResourceLocation.read(stringreader);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
