package net.minecraft.world.item.trading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class MerchantOffer {
   private final ItemStack baseCostA;
   private final ItemStack costB;
   private final ItemStack result;
   private int uses;
   private final int maxUses;
   private boolean rewardExp = true;
   private int specialPriceDiff;
   private int demand;
   private float priceMultiplier;
   private int xp = 1;

   public MerchantOffer(CompoundTag compoundtag) {
      this.baseCostA = ItemStack.of(compoundtag.getCompound("buy"));
      this.costB = ItemStack.of(compoundtag.getCompound("buyB"));
      this.result = ItemStack.of(compoundtag.getCompound("sell"));
      this.uses = compoundtag.getInt("uses");
      if (compoundtag.contains("maxUses", 99)) {
         this.maxUses = compoundtag.getInt("maxUses");
      } else {
         this.maxUses = 4;
      }

      if (compoundtag.contains("rewardExp", 1)) {
         this.rewardExp = compoundtag.getBoolean("rewardExp");
      }

      if (compoundtag.contains("xp", 3)) {
         this.xp = compoundtag.getInt("xp");
      }

      if (compoundtag.contains("priceMultiplier", 5)) {
         this.priceMultiplier = compoundtag.getFloat("priceMultiplier");
      }

      this.specialPriceDiff = compoundtag.getInt("specialPrice");
      this.demand = compoundtag.getInt("demand");
   }

   public MerchantOffer(ItemStack itemstack, ItemStack itemstack1, int i, int j, float f) {
      this(itemstack, ItemStack.EMPTY, itemstack1, i, j, f);
   }

   public MerchantOffer(ItemStack itemstack, ItemStack itemstack1, ItemStack itemstack2, int i, int j, float f) {
      this(itemstack, itemstack1, itemstack2, 0, i, j, f);
   }

   public MerchantOffer(ItemStack itemstack, ItemStack itemstack1, ItemStack itemstack2, int i, int j, int k, float f) {
      this(itemstack, itemstack1, itemstack2, i, j, k, f, 0);
   }

   public MerchantOffer(ItemStack itemstack, ItemStack itemstack1, ItemStack itemstack2, int i, int j, int k, float f, int l) {
      this.baseCostA = itemstack;
      this.costB = itemstack1;
      this.result = itemstack2;
      this.uses = i;
      this.maxUses = j;
      this.xp = k;
      this.priceMultiplier = f;
      this.demand = l;
   }

   public ItemStack getBaseCostA() {
      return this.baseCostA;
   }

   public ItemStack getCostA() {
      if (this.baseCostA.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         int i = this.baseCostA.getCount();
         int j = Math.max(0, Mth.floor((float)(i * this.demand) * this.priceMultiplier));
         return this.baseCostA.copyWithCount(Mth.clamp(i + j + this.specialPriceDiff, 1, this.baseCostA.getItem().getMaxStackSize()));
      }
   }

   public ItemStack getCostB() {
      return this.costB;
   }

   public ItemStack getResult() {
      return this.result;
   }

   public void updateDemand() {
      this.demand = this.demand + this.uses - (this.maxUses - this.uses);
   }

   public ItemStack assemble() {
      return this.result.copy();
   }

   public int getUses() {
      return this.uses;
   }

   public void resetUses() {
      this.uses = 0;
   }

   public int getMaxUses() {
      return this.maxUses;
   }

   public void increaseUses() {
      ++this.uses;
   }

   public int getDemand() {
      return this.demand;
   }

   public void addToSpecialPriceDiff(int i) {
      this.specialPriceDiff += i;
   }

   public void resetSpecialPriceDiff() {
      this.specialPriceDiff = 0;
   }

   public int getSpecialPriceDiff() {
      return this.specialPriceDiff;
   }

   public void setSpecialPriceDiff(int i) {
      this.specialPriceDiff = i;
   }

   public float getPriceMultiplier() {
      return this.priceMultiplier;
   }

   public int getXp() {
      return this.xp;
   }

   public boolean isOutOfStock() {
      return this.uses >= this.maxUses;
   }

   public void setToOutOfStock() {
      this.uses = this.maxUses;
   }

   public boolean needsRestock() {
      return this.uses > 0;
   }

   public boolean shouldRewardExp() {
      return this.rewardExp;
   }

   public CompoundTag createTag() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.put("buy", this.baseCostA.save(new CompoundTag()));
      compoundtag.put("sell", this.result.save(new CompoundTag()));
      compoundtag.put("buyB", this.costB.save(new CompoundTag()));
      compoundtag.putInt("uses", this.uses);
      compoundtag.putInt("maxUses", this.maxUses);
      compoundtag.putBoolean("rewardExp", this.rewardExp);
      compoundtag.putInt("xp", this.xp);
      compoundtag.putFloat("priceMultiplier", this.priceMultiplier);
      compoundtag.putInt("specialPrice", this.specialPriceDiff);
      compoundtag.putInt("demand", this.demand);
      return compoundtag;
   }

   public boolean satisfiedBy(ItemStack itemstack, ItemStack itemstack1) {
      return this.isRequiredItem(itemstack, this.getCostA()) && itemstack.getCount() >= this.getCostA().getCount() && this.isRequiredItem(itemstack1, this.costB) && itemstack1.getCount() >= this.costB.getCount();
   }

   private boolean isRequiredItem(ItemStack itemstack, ItemStack itemstack1) {
      if (itemstack1.isEmpty() && itemstack.isEmpty()) {
         return true;
      } else {
         ItemStack itemstack2 = itemstack.copy();
         if (itemstack2.getItem().canBeDepleted()) {
            itemstack2.setDamageValue(itemstack2.getDamageValue());
         }

         return ItemStack.isSameItem(itemstack2, itemstack1) && (!itemstack1.hasTag() || itemstack2.hasTag() && NbtUtils.compareNbt(itemstack1.getTag(), itemstack2.getTag(), false));
      }
   }

   public boolean take(ItemStack itemstack, ItemStack itemstack1) {
      if (!this.satisfiedBy(itemstack, itemstack1)) {
         return false;
      } else {
         itemstack.shrink(this.getCostA().getCount());
         if (!this.getCostB().isEmpty()) {
            itemstack1.shrink(this.getCostB().getCount());
         }

         return true;
      }
   }
}
