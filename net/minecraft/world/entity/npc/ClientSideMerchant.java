package net.minecraft.world.entity.npc;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientSideMerchant implements Merchant {
   private final Player source;
   private MerchantOffers offers = new MerchantOffers();
   private int xp;

   public ClientSideMerchant(Player player) {
      this.source = player;
   }

   public Player getTradingPlayer() {
      return this.source;
   }

   public void setTradingPlayer(@Nullable Player player) {
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   public void overrideOffers(MerchantOffers merchantoffers) {
      this.offers = merchantoffers;
   }

   public void notifyTrade(MerchantOffer merchantoffer) {
      merchantoffer.increaseUses();
   }

   public void notifyTradeUpdated(ItemStack itemstack) {
   }

   public boolean isClientSide() {
      return this.source.level().isClientSide;
   }

   public int getVillagerXp() {
      return this.xp;
   }

   public void overrideXp(int i) {
      this.xp = i;
   }

   public boolean showProgressBar() {
      return true;
   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }
}
