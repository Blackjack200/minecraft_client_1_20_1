package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Immutable
public class LockCode {
   public static final LockCode NO_LOCK = new LockCode("");
   public static final String TAG_LOCK = "Lock";
   private final String key;

   public LockCode(String s) {
      this.key = s;
   }

   public boolean unlocksWith(ItemStack itemstack) {
      return this.key.isEmpty() || !itemstack.isEmpty() && itemstack.hasCustomHoverName() && this.key.equals(itemstack.getHoverName().getString());
   }

   public void addToTag(CompoundTag compoundtag) {
      if (!this.key.isEmpty()) {
         compoundtag.putString("Lock", this.key);
      }

   }

   public static LockCode fromTag(CompoundTag compoundtag) {
      return compoundtag.contains("Lock", 8) ? new LockCode(compoundtag.getString("Lock")) : NO_LOCK;
   }
}
