package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SmeltItemFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();

   SmeltItemFunction(LootItemCondition[] alootitemcondition) {
      super(alootitemcondition);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.FURNACE_SMELT;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (itemstack.isEmpty()) {
         return itemstack;
      } else {
         Optional<SmeltingRecipe> optional = lootcontext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemstack), lootcontext.getLevel());
         if (optional.isPresent()) {
            ItemStack itemstack1 = optional.get().getResultItem(lootcontext.getLevel().registryAccess());
            if (!itemstack1.isEmpty()) {
               return itemstack1.copyWithCount(itemstack.getCount());
            }
         }

         LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)itemstack);
         return itemstack;
      }
   }

   public static LootItemConditionalFunction.Builder<?> smelted() {
      return simpleBuilder(SmeltItemFunction::new);
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SmeltItemFunction> {
      public SmeltItemFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         return new SmeltItemFunction(alootitemcondition);
      }
   }
}
