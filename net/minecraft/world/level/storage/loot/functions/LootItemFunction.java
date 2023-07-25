package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
   LootItemFunctionType getType();

   static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> bifunction, Consumer<ItemStack> consumer, LootContext lootcontext) {
      return (itemstack) -> consumer.accept(bifunction.apply(itemstack, lootcontext));
   }

   public interface Builder {
      LootItemFunction build();
   }
}
