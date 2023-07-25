package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ChestBoat extends Boat implements HasCustomInventoryScreen, ContainerEntity {
   private static final int CONTAINER_SIZE = 27;
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;

   public ChestBoat(EntityType<? extends Boat> entitytype, Level level) {
      super(entitytype, level);
   }

   public ChestBoat(Level level, double d0, double d1, double d2) {
      this(EntityType.CHEST_BOAT, level);
      this.setPos(d0, d1, d2);
      this.xo = d0;
      this.yo = d1;
      this.zo = d2;
   }

   protected float getSinglePassengerXOffset() {
      return 0.15F;
   }

   protected int getMaxPassengers() {
      return 1;
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      this.addChestVehicleSaveData(compoundtag);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.readChestVehicleSaveData(compoundtag);
   }

   public void destroy(DamageSource damagesource) {
      super.destroy(damagesource);
      this.chestVehicleDestroyed(damagesource, this.level(), this);
   }

   public void remove(Entity.RemovalReason entity_removalreason) {
      if (!this.level().isClientSide && entity_removalreason.shouldDestroy()) {
         Containers.dropContents(this.level(), this, this);
      }

      super.remove(entity_removalreason);
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
         return super.interact(player, interactionhand);
      } else {
         InteractionResult interactionresult = this.interactWithContainerVehicle(player);
         if (interactionresult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinAi.angerNearbyPiglins(player, true);
         }

         return interactionresult;
      }
   }

   public void openCustomInventoryScreen(Player player) {
      player.openMenu(this);
      if (!player.level().isClientSide) {
         this.gameEvent(GameEvent.CONTAINER_OPEN, player);
         PiglinAi.angerNearbyPiglins(player, true);
      }

   }

   public Item getDropItem() {
      Item var10000;
      switch (this.getVariant()) {
         case SPRUCE:
            var10000 = Items.SPRUCE_CHEST_BOAT;
            break;
         case BIRCH:
            var10000 = Items.BIRCH_CHEST_BOAT;
            break;
         case JUNGLE:
            var10000 = Items.JUNGLE_CHEST_BOAT;
            break;
         case ACACIA:
            var10000 = Items.ACACIA_CHEST_BOAT;
            break;
         case CHERRY:
            var10000 = Items.CHERRY_CHEST_BOAT;
            break;
         case DARK_OAK:
            var10000 = Items.DARK_OAK_CHEST_BOAT;
            break;
         case MANGROVE:
            var10000 = Items.MANGROVE_CHEST_BOAT;
            break;
         case BAMBOO:
            var10000 = Items.BAMBOO_CHEST_RAFT;
            break;
         default:
            var10000 = Items.OAK_CHEST_BOAT;
      }

      return var10000;
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   public int getContainerSize() {
      return 27;
   }

   public ItemStack getItem(int i) {
      return this.getChestVehicleItem(i);
   }

   public ItemStack removeItem(int i, int j) {
      return this.removeChestVehicleItem(i, j);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return this.removeChestVehicleItemNoUpdate(i);
   }

   public void setItem(int i, ItemStack itemstack) {
      this.setChestVehicleItem(i, itemstack);
   }

   public SlotAccess getSlot(int i) {
      return this.getChestVehicleSlot(i);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      return this.isChestVehicleStillValid(player);
   }

   @Nullable
   public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      if (this.lootTable != null && player.isSpectator()) {
         return null;
      } else {
         this.unpackLootTable(inventory.player);
         return ChestMenu.threeRows(i, inventory, this);
      }
   }

   public void unpackLootTable(@Nullable Player player) {
      this.unpackChestVehicleLootTable(player);
   }

   @Nullable
   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(@Nullable ResourceLocation resourcelocation) {
      this.lootTable = resourcelocation;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long i) {
      this.lootTableSeed = i;
   }

   public NonNullList<ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public void clearItemStacks() {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   }

   public void stopOpen(Player player) {
      this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
   }
}
