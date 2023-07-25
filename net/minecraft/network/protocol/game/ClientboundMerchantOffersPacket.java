package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final MerchantOffers offers;
   private final int villagerLevel;
   private final int villagerXp;
   private final boolean showProgress;
   private final boolean canRestock;

   public ClientboundMerchantOffersPacket(int i, MerchantOffers merchantoffers, int j, int k, boolean flag, boolean flag1) {
      this.containerId = i;
      this.offers = merchantoffers;
      this.villagerLevel = j;
      this.villagerXp = k;
      this.showProgress = flag;
      this.canRestock = flag1;
   }

   public ClientboundMerchantOffersPacket(FriendlyByteBuf friendlybytebuf) {
      this.containerId = friendlybytebuf.readVarInt();
      this.offers = MerchantOffers.createFromStream(friendlybytebuf);
      this.villagerLevel = friendlybytebuf.readVarInt();
      this.villagerXp = friendlybytebuf.readVarInt();
      this.showProgress = friendlybytebuf.readBoolean();
      this.canRestock = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.containerId);
      this.offers.writeToStream(friendlybytebuf);
      friendlybytebuf.writeVarInt(this.villagerLevel);
      friendlybytebuf.writeVarInt(this.villagerXp);
      friendlybytebuf.writeBoolean(this.showProgress);
      friendlybytebuf.writeBoolean(this.canRestock);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleMerchantOffers(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public MerchantOffers getOffers() {
      return this.offers;
   }

   public int getVillagerLevel() {
      return this.villagerLevel;
   }

   public int getVillagerXp() {
      return this.villagerXp;
   }

   public boolean showProgress() {
      return this.showProgress;
   }

   public boolean canRestock() {
      return this.canRestock;
   }
}
