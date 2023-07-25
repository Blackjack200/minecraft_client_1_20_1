package net.minecraft.world.entity.npc;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractVillager extends AgeableMob implements InventoryCarrier, Npc, Merchant {
   private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
   public static final int VILLAGER_SLOT_OFFSET = 300;
   private static final int VILLAGER_INVENTORY_SIZE = 8;
   @Nullable
   private Player tradingPlayer;
   @Nullable
   protected MerchantOffers offers;
   private final SimpleContainer inventory = new SimpleContainer(8);

   public AbstractVillager(EntityType<? extends AbstractVillager> entitytype, Level level) {
      super(entitytype, level);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverlevelaccessor, DifficultyInstance difficultyinstance, MobSpawnType mobspawntype, @Nullable SpawnGroupData spawngroupdata, @Nullable CompoundTag compoundtag) {
      if (spawngroupdata == null) {
         spawngroupdata = new AgeableMob.AgeableMobGroupData(false);
      }

      return super.finalizeSpawn(serverlevelaccessor, difficultyinstance, mobspawntype, spawngroupdata, compoundtag);
   }

   public int getUnhappyCounter() {
      return this.entityData.get(DATA_UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int i) {
      this.entityData.set(DATA_UNHAPPY_COUNTER, i);
   }

   public int getVillagerXp() {
      return 0;
   }

   protected float getStandingEyeHeight(Pose pose, EntityDimensions entitydimensions) {
      return this.isBaby() ? 0.81F : 1.62F;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
   }

   public void setTradingPlayer(@Nullable Player player) {
      this.tradingPlayer = player;
   }

   @Nullable
   public Player getTradingPlayer() {
      return this.tradingPlayer;
   }

   public boolean isTrading() {
      return this.tradingPlayer != null;
   }

   public MerchantOffers getOffers() {
      if (this.offers == null) {
         this.offers = new MerchantOffers();
         this.updateTrades();
      }

      return this.offers;
   }

   public void overrideOffers(@Nullable MerchantOffers merchantoffers) {
   }

   public void overrideXp(int i) {
   }

   public void notifyTrade(MerchantOffer merchantoffer) {
      merchantoffer.increaseUses();
      this.ambientSoundTime = -this.getAmbientSoundInterval();
      this.rewardTradeXp(merchantoffer);
      if (this.tradingPlayer instanceof ServerPlayer) {
         CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, merchantoffer.getResult());
      }

   }

   protected abstract void rewardTradeXp(MerchantOffer merchantoffer);

   public boolean showProgressBar() {
      return true;
   }

   public void notifyTradeUpdated(ItemStack itemstack) {
      if (!this.level().isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
         this.ambientSoundTime = -this.getAmbientSoundInterval();
         this.playSound(this.getTradeUpdatedSound(!itemstack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }

   protected SoundEvent getTradeUpdatedSound(boolean flag) {
      return flag ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
   }

   public void playCelebrateSound() {
      this.playSound(SoundEvents.VILLAGER_CELEBRATE, this.getSoundVolume(), this.getVoicePitch());
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      MerchantOffers merchantoffers = this.getOffers();
      if (!merchantoffers.isEmpty()) {
         compoundtag.put("Offers", merchantoffers.createTag());
      }

      this.writeInventoryToTag(compoundtag);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("Offers", 10)) {
         this.offers = new MerchantOffers(compoundtag.getCompound("Offers"));
      }

      this.readInventoryFromTag(compoundtag);
   }

   @Nullable
   public Entity changeDimension(ServerLevel serverlevel) {
      this.stopTrading();
      return super.changeDimension(serverlevel);
   }

   protected void stopTrading() {
      this.setTradingPlayer((Player)null);
   }

   public void die(DamageSource damagesource) {
      super.die(damagesource);
      this.stopTrading();
   }

   protected void addParticlesAroundSelf(ParticleOptions particleoptions) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   public boolean canBeLeashed(Player player) {
      return false;
   }

   public SimpleContainer getInventory() {
      return this.inventory;
   }

   public SlotAccess getSlot(int i) {
      int j = i - 300;
      return j >= 0 && j < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, j) : super.getSlot(i);
   }

   protected abstract void updateTrades();

   protected void addOffersFromItemListings(MerchantOffers merchantoffers, VillagerTrades.ItemListing[] avillagertrades_itemlisting, int i) {
      Set<Integer> set = Sets.newHashSet();
      if (avillagertrades_itemlisting.length > i) {
         while(set.size() < i) {
            set.add(this.random.nextInt(avillagertrades_itemlisting.length));
         }
      } else {
         for(int j = 0; j < avillagertrades_itemlisting.length; ++j) {
            set.add(j);
         }
      }

      for(Integer integer : set) {
         VillagerTrades.ItemListing villagertrades_itemlisting = avillagertrades_itemlisting[integer];
         MerchantOffer merchantoffer = villagertrades_itemlisting.getOffer(this, this.random);
         if (merchantoffer != null) {
            merchantoffers.add(merchantoffer);
         }
      }

   }

   public Vec3 getRopeHoldPosition(float f) {
      float f1 = Mth.lerp(f, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180F);
      Vec3 vec3 = new Vec3(0.0D, this.getBoundingBox().getYsize() - 1.0D, 0.2D);
      return this.getPosition(f).add(vec3.yRot(-f1));
   }

   public boolean isClientSide() {
      return this.level().isClientSide;
   }
}
