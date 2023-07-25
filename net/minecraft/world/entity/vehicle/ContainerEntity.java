package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface ContainerEntity extends Container, MenuProvider {
   Vec3 position();

   @Nullable
   ResourceLocation getLootTable();

   void setLootTable(@Nullable ResourceLocation resourcelocation);

   long getLootTableSeed();

   void setLootTableSeed(long i);

   NonNullList<ItemStack> getItemStacks();

   void clearItemStacks();

   Level level();

   boolean isRemoved();

   default boolean isEmpty() {
      return this.isChestVehicleEmpty();
   }

   default void addChestVehicleSaveData(CompoundTag compoundtag) {
      if (this.getLootTable() != null) {
         compoundtag.putString("LootTable", this.getLootTable().toString());
         if (this.getLootTableSeed() != 0L) {
            compoundtag.putLong("LootTableSeed", this.getLootTableSeed());
         }
      } else {
         ContainerHelper.saveAllItems(compoundtag, this.getItemStacks());
      }

   }

   default void readChestVehicleSaveData(CompoundTag compoundtag) {
      this.clearItemStacks();
      if (compoundtag.contains("LootTable", 8)) {
         this.setLootTable(new ResourceLocation(compoundtag.getString("LootTable")));
         this.setLootTableSeed(compoundtag.getLong("LootTableSeed"));
      } else {
         ContainerHelper.loadAllItems(compoundtag, this.getItemStacks());
      }

   }

   default void chestVehicleDestroyed(DamageSource damagesource, Level level, Entity entity) {
      if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         Containers.dropContents(level, entity, this);
         if (!level.isClientSide) {
            Entity entity1 = damagesource.getDirectEntity();
            if (entity1 != null && entity1.getType() == EntityType.PLAYER) {
               PiglinAi.angerNearbyPiglins((Player)entity1, true);
            }
         }

      }
   }

   default InteractionResult interactWithContainerVehicle(Player player) {
      player.openMenu(this);
      return !player.level().isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
   }

   default void unpackChestVehicleLootTable(@Nullable Player player) {
      MinecraftServer minecraftserver = this.level().getServer();
      if (this.getLootTable() != null && minecraftserver != null) {
         LootTable loottable = minecraftserver.getLootData().getLootTable(this.getLootTable());
         if (player != null) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
         }

         this.setLootTable((ResourceLocation)null);
         LootParams.Builder lootparams_builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());
         if (player != null) {
            lootparams_builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
         }

         loottable.fill(this, lootparams_builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
      }

   }

   default void clearChestVehicleContent() {
      this.unpackChestVehicleLootTable((Player)null);
      this.getItemStacks().clear();
   }

   default boolean isChestVehicleEmpty() {
      for(ItemStack itemstack : this.getItemStacks()) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   default ItemStack removeChestVehicleItemNoUpdate(int i) {
      this.unpackChestVehicleLootTable((Player)null);
      ItemStack itemstack = this.getItemStacks().get(i);
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.getItemStacks().set(i, ItemStack.EMPTY);
         return itemstack;
      }
   }

   default ItemStack getChestVehicleItem(int i) {
      this.unpackChestVehicleLootTable((Player)null);
      return this.getItemStacks().get(i);
   }

   default ItemStack removeChestVehicleItem(int i, int j) {
      this.unpackChestVehicleLootTable((Player)null);
      return ContainerHelper.removeItem(this.getItemStacks(), i, j);
   }

   default void setChestVehicleItem(int i, ItemStack itemstack) {
      this.unpackChestVehicleLootTable((Player)null);
      this.getItemStacks().set(i, itemstack);
      if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
         itemstack.setCount(this.getMaxStackSize());
      }

   }

   default SlotAccess getChestVehicleSlot(final int i) {
      return i >= 0 && i < this.getContainerSize() ? new SlotAccess() {
         public ItemStack get() {
            return ContainerEntity.this.getChestVehicleItem(i);
         }

         public boolean set(ItemStack itemstack) {
            ContainerEntity.this.setChestVehicleItem(i, itemstack);
            return true;
         }
      } : SlotAccess.NULL;
   }

   default boolean isChestVehicleStillValid(Player player) {
      return !this.isRemoved() && this.position().closerThan(player.position(), 8.0D);
   }
}
