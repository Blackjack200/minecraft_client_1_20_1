package net.minecraft.world.item.trading;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MerchantOffers extends ArrayList<MerchantOffer> {
   public MerchantOffers() {
   }

   private MerchantOffers(int i) {
      super(i);
   }

   public MerchantOffers(CompoundTag compoundtag) {
      ListTag listtag = compoundtag.getList("Recipes", 10);

      for(int i = 0; i < listtag.size(); ++i) {
         this.add(new MerchantOffer(listtag.getCompound(i)));
      }

   }

   @Nullable
   public MerchantOffer getRecipeFor(ItemStack itemstack, ItemStack itemstack1, int i) {
      if (i > 0 && i < this.size()) {
         MerchantOffer merchantoffer = this.get(i);
         return merchantoffer.satisfiedBy(itemstack, itemstack1) ? merchantoffer : null;
      } else {
         for(int j = 0; j < this.size(); ++j) {
            MerchantOffer merchantoffer1 = this.get(j);
            if (merchantoffer1.satisfiedBy(itemstack, itemstack1)) {
               return merchantoffer1;
            }
         }

         return null;
      }
   }

   public void writeToStream(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeCollection(this, (friendlybytebuf1, merchantoffer) -> {
         friendlybytebuf1.writeItem(merchantoffer.getBaseCostA());
         friendlybytebuf1.writeItem(merchantoffer.getResult());
         friendlybytebuf1.writeItem(merchantoffer.getCostB());
         friendlybytebuf1.writeBoolean(merchantoffer.isOutOfStock());
         friendlybytebuf1.writeInt(merchantoffer.getUses());
         friendlybytebuf1.writeInt(merchantoffer.getMaxUses());
         friendlybytebuf1.writeInt(merchantoffer.getXp());
         friendlybytebuf1.writeInt(merchantoffer.getSpecialPriceDiff());
         friendlybytebuf1.writeFloat(merchantoffer.getPriceMultiplier());
         friendlybytebuf1.writeInt(merchantoffer.getDemand());
      });
   }

   public static MerchantOffers createFromStream(FriendlyByteBuf friendlybytebuf) {
      return friendlybytebuf.readCollection(MerchantOffers::new, (friendlybytebuf1) -> {
         ItemStack itemstack = friendlybytebuf1.readItem();
         ItemStack itemstack1 = friendlybytebuf1.readItem();
         ItemStack itemstack2 = friendlybytebuf1.readItem();
         boolean flag = friendlybytebuf1.readBoolean();
         int i = friendlybytebuf1.readInt();
         int j = friendlybytebuf1.readInt();
         int k = friendlybytebuf1.readInt();
         int l = friendlybytebuf1.readInt();
         float f = friendlybytebuf1.readFloat();
         int i1 = friendlybytebuf1.readInt();
         MerchantOffer merchantoffer = new MerchantOffer(itemstack, itemstack2, itemstack1, i, j, k, f, i1);
         if (flag) {
            merchantoffer.setToOutOfStock();
         }

         merchantoffer.setSpecialPriceDiff(l);
         return merchantoffer;
      });
   }

   public CompoundTag createTag() {
      CompoundTag compoundtag = new CompoundTag();
      ListTag listtag = new ListTag();

      for(int i = 0; i < this.size(); ++i) {
         MerchantOffer merchantoffer = this.get(i);
         listtag.add(merchantoffer.createTag());
      }

      compoundtag.put("Recipes", listtag);
      return compoundtag;
   }
}
