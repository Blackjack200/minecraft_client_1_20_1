package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemInput implements Predicate<ItemStack> {
   private static final Dynamic2CommandExceptionType ERROR_STACK_TOO_BIG = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("arguments.item.overstacked", object, object1));
   private final Holder<Item> item;
   @Nullable
   private final CompoundTag tag;

   public ItemInput(Holder<Item> holder, @Nullable CompoundTag compoundtag) {
      this.item = holder;
      this.tag = compoundtag;
   }

   public Item getItem() {
      return this.item.value();
   }

   public boolean test(ItemStack itemstack) {
      return itemstack.is(this.item) && NbtUtils.compareNbt(this.tag, itemstack.getTag(), true);
   }

   public ItemStack createItemStack(int i, boolean flag) throws CommandSyntaxException {
      ItemStack itemstack = new ItemStack(this.item, i);
      if (this.tag != null) {
         itemstack.setTag(this.tag);
      }

      if (flag && i > itemstack.getMaxStackSize()) {
         throw ERROR_STACK_TOO_BIG.create(this.getItemName(), itemstack.getMaxStackSize());
      } else {
         return itemstack;
      }
   }

   public String serialize() {
      StringBuilder stringbuilder = new StringBuilder(this.getItemName());
      if (this.tag != null) {
         stringbuilder.append((Object)this.tag);
      }

      return stringbuilder.toString();
   }

   private String getItemName() {
      return this.item.unwrapKey().map(ResourceKey::location).orElseGet(() -> "unknown[" + this.item + "]").toString();
   }
}
