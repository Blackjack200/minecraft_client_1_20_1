package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity {
   public static final String LOOT_TABLE_TAG = "LootTable";
   public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
   @Nullable
   protected ResourceLocation lootTable;
   protected long lootTableSeed;

   protected RandomizableContainerBlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate) {
      super(blockentitytype, blockpos, blockstate);
   }

   public static void setLootTable(BlockGetter blockgetter, RandomSource randomsource, BlockPos blockpos, ResourceLocation resourcelocation) {
      BlockEntity blockentity = blockgetter.getBlockEntity(blockpos);
      if (blockentity instanceof RandomizableContainerBlockEntity) {
         ((RandomizableContainerBlockEntity)blockentity).setLootTable(resourcelocation, randomsource.nextLong());
      }

   }

   protected boolean tryLoadLootTable(CompoundTag compoundtag) {
      if (compoundtag.contains("LootTable", 8)) {
         this.lootTable = new ResourceLocation(compoundtag.getString("LootTable"));
         this.lootTableSeed = compoundtag.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   protected boolean trySaveLootTable(CompoundTag compoundtag) {
      if (this.lootTable == null) {
         return false;
      } else {
         compoundtag.putString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            compoundtag.putLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      }
   }

   public void unpackLootTable(@Nullable Player player) {
      if (this.lootTable != null && this.level.getServer() != null) {
         LootTable loottable = this.level.getServer().getLootData().getLootTable(this.lootTable);
         if (player instanceof ServerPlayer) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.lootTable);
         }

         this.lootTable = null;
         LootParams.Builder lootparams_builder = (new LootParams.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition));
         if (player != null) {
            lootparams_builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
         }

         loottable.fill(this, lootparams_builder.create(LootContextParamSets.CHEST), this.lootTableSeed);
      }

   }

   public void setLootTable(ResourceLocation resourcelocation, long i) {
      this.lootTable = resourcelocation;
      this.lootTableSeed = i;
   }

   public boolean isEmpty() {
      this.unpackLootTable((Player)null);
      return this.getItems().stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int i) {
      this.unpackLootTable((Player)null);
      return this.getItems().get(i);
   }

   public ItemStack removeItem(int i, int j) {
      this.unpackLootTable((Player)null);
      ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), i, j);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   public ItemStack removeItemNoUpdate(int i) {
      this.unpackLootTable((Player)null);
      return ContainerHelper.takeItem(this.getItems(), i);
   }

   public void setItem(int i, ItemStack itemstack) {
      this.unpackLootTable((Player)null);
      this.getItems().set(i, itemstack);
      if (itemstack.getCount() > this.getMaxStackSize()) {
         itemstack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public void clearContent() {
      this.getItems().clear();
   }

   protected abstract NonNullList<ItemStack> getItems();

   protected abstract void setItems(NonNullList<ItemStack> nonnulllist);

   public boolean canOpen(Player player) {
      return super.canOpen(player) && (this.lootTable == null || !player.isSpectator());
   }

   @Nullable
   public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      if (this.canOpen(player)) {
         this.unpackLootTable(inventory.player);
         return this.createMenu(i, inventory);
      } else {
         return null;
      }
   }
}
