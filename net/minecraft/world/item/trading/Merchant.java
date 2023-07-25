package net.minecraft.world.item.trading;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;

public interface Merchant {
   void setTradingPlayer(@Nullable Player player);

   @Nullable
   Player getTradingPlayer();

   MerchantOffers getOffers();

   void overrideOffers(MerchantOffers merchantoffers);

   void notifyTrade(MerchantOffer merchantoffer);

   void notifyTradeUpdated(ItemStack itemstack);

   int getVillagerXp();

   void overrideXp(int i);

   boolean showProgressBar();

   SoundEvent getNotifyTradeSound();

   default boolean canRestock() {
      return false;
   }

   default void openTradingScreen(Player player, Component component, int i) {
      OptionalInt optionalint = player.openMenu(new SimpleMenuProvider((j, inventory, player1) -> new MerchantMenu(j, inventory, this), component));
      if (optionalint.isPresent()) {
         MerchantOffers merchantoffers = this.getOffers();
         if (!merchantoffers.isEmpty()) {
            player.sendMerchantOffers(optionalint.getAsInt(), merchantoffers, i, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
         }
      }

   }

   boolean isClientSide();
}
