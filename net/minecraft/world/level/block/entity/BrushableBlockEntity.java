package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class BrushableBlockEntity extends BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String LOOT_TABLE_TAG = "LootTable";
   private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
   private static final String HIT_DIRECTION_TAG = "hit_direction";
   private static final String ITEM_TAG = "item";
   private static final int BRUSH_COOLDOWN_TICKS = 10;
   private static final int BRUSH_RESET_TICKS = 40;
   private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
   private int brushCount;
   private long brushCountResetsAtTick;
   private long coolDownEndsAtTick;
   private ItemStack item = ItemStack.EMPTY;
   @Nullable
   private Direction hitDirection;
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;

   public BrushableBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BRUSHABLE_BLOCK, blockpos, blockstate);
   }

   public boolean brush(long i, Player player, Direction direction) {
      if (this.hitDirection == null) {
         this.hitDirection = direction;
      }

      this.brushCountResetsAtTick = i + 40L;
      if (i >= this.coolDownEndsAtTick && this.level instanceof ServerLevel) {
         this.coolDownEndsAtTick = i + 10L;
         this.unpackLootTable(player);
         int j = this.getCompletionState();
         if (++this.brushCount >= 10) {
            this.brushingCompleted(player);
            return true;
         } else {
            this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 40);
            int k = this.getCompletionState();
            if (j != k) {
               BlockState blockstate = this.getBlockState();
               BlockState blockstate1 = blockstate.setValue(BlockStateProperties.DUSTED, Integer.valueOf(k));
               this.level.setBlock(this.getBlockPos(), blockstate1, 3);
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public void unpackLootTable(Player player) {
      if (this.lootTable != null && this.level != null && !this.level.isClientSide() && this.level.getServer() != null) {
         LootTable loottable = this.level.getServer().getLootData().getLootTable(this.lootTable);
         if (player instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            CriteriaTriggers.GENERATE_LOOT.trigger(serverplayer, this.lootTable);
         }

         LootParams lootparams = (new LootParams.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition)).withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.CHEST);
         ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, this.lootTableSeed);
         ItemStack var10001;
         switch (objectarraylist.size()) {
            case 0:
               var10001 = ItemStack.EMPTY;
               break;
            case 1:
               var10001 = objectarraylist.get(0);
               break;
            default:
               LOGGER.warn("Expected max 1 loot from loot table " + this.lootTable + " got " + objectarraylist.size());
               var10001 = objectarraylist.get(0);
         }

         this.item = var10001;
         this.lootTable = null;
         this.setChanged();
      }
   }

   private void brushingCompleted(Player player) {
      if (this.level != null && this.level.getServer() != null) {
         this.dropContent(player);
         BlockState blockstate = this.getBlockState();
         this.level.levelEvent(3008, this.getBlockPos(), Block.getId(blockstate));
         Block block = this.getBlockState().getBlock();
         Block block1;
         if (block instanceof BrushableBlock) {
            BrushableBlock brushableblock = (BrushableBlock)block;
            block1 = brushableblock.getTurnsInto();
         } else {
            block1 = Blocks.AIR;
         }

         this.level.setBlock(this.worldPosition, block1.defaultBlockState(), 3);
      }
   }

   private void dropContent(Player player) {
      if (this.level != null && this.level.getServer() != null) {
         this.unpackLootTable(player);
         if (!this.item.isEmpty()) {
            double d0 = (double)EntityType.ITEM.getWidth();
            double d1 = 1.0D - d0;
            double d2 = d0 / 2.0D;
            Direction direction = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
            BlockPos blockpos = this.worldPosition.relative(direction, 1);
            double d3 = (double)blockpos.getX() + 0.5D * d1 + d2;
            double d4 = (double)blockpos.getY() + 0.5D + (double)(EntityType.ITEM.getHeight() / 2.0F);
            double d5 = (double)blockpos.getZ() + 0.5D * d1 + d2;
            ItemEntity itementity = new ItemEntity(this.level, d3, d4, d5, this.item.split(this.level.random.nextInt(21) + 10));
            itementity.setDeltaMovement(Vec3.ZERO);
            this.level.addFreshEntity(itementity);
            this.item = ItemStack.EMPTY;
         }

      }
   }

   public void checkReset() {
      if (this.level != null) {
         if (this.brushCount != 0 && this.level.getGameTime() >= this.brushCountResetsAtTick) {
            int i = this.getCompletionState();
            this.brushCount = Math.max(0, this.brushCount - 2);
            int j = this.getCompletionState();
            if (i != j) {
               this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.DUSTED, Integer.valueOf(j)), 3);
            }

            int k = 4;
            this.brushCountResetsAtTick = this.level.getGameTime() + 4L;
         }

         if (this.brushCount == 0) {
            this.hitDirection = null;
            this.brushCountResetsAtTick = 0L;
            this.coolDownEndsAtTick = 0L;
         } else {
            this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), (int)(this.brushCountResetsAtTick - this.level.getGameTime()));
         }

      }
   }

   private boolean tryLoadLootTable(CompoundTag compoundtag) {
      if (compoundtag.contains("LootTable", 8)) {
         this.lootTable = new ResourceLocation(compoundtag.getString("LootTable"));
         this.lootTableSeed = compoundtag.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   private boolean trySaveLootTable(CompoundTag compoundtag) {
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

   public CompoundTag getUpdateTag() {
      CompoundTag compoundtag = super.getUpdateTag();
      if (this.hitDirection != null) {
         compoundtag.putInt("hit_direction", this.hitDirection.ordinal());
      }

      compoundtag.put("item", this.item.save(new CompoundTag()));
      return compoundtag;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void load(CompoundTag compoundtag) {
      if (!this.tryLoadLootTable(compoundtag) && compoundtag.contains("item")) {
         this.item = ItemStack.of(compoundtag.getCompound("item"));
      }

      if (compoundtag.contains("hit_direction")) {
         this.hitDirection = Direction.values()[compoundtag.getInt("hit_direction")];
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      if (!this.trySaveLootTable(compoundtag)) {
         compoundtag.put("item", this.item.save(new CompoundTag()));
      }

   }

   public void setLootTable(ResourceLocation resourcelocation, long i) {
      this.lootTable = resourcelocation;
      this.lootTableSeed = i;
   }

   private int getCompletionState() {
      if (this.brushCount == 0) {
         return 0;
      } else if (this.brushCount < 3) {
         return 1;
      } else {
         return this.brushCount < 6 ? 2 : 3;
      }
   }

   @Nullable
   public Direction getHitDirection() {
      return this.hitDirection;
   }

   public ItemStack getItem() {
      return this.item;
   }
}
