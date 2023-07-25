package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockEntityType<?> type;
   @Nullable
   protected Level level;
   protected final BlockPos worldPosition;
   protected boolean remove;
   private BlockState blockState;

   public BlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate) {
      this.type = blockentitytype;
      this.worldPosition = blockpos.immutable();
      this.blockState = blockstate;
   }

   public static BlockPos getPosFromTag(CompoundTag compoundtag) {
      return new BlockPos(compoundtag.getInt("x"), compoundtag.getInt("y"), compoundtag.getInt("z"));
   }

   @Nullable
   public Level getLevel() {
      return this.level;
   }

   public void setLevel(Level level) {
      this.level = level;
   }

   public boolean hasLevel() {
      return this.level != null;
   }

   public void load(CompoundTag compoundtag) {
   }

   protected void saveAdditional(CompoundTag compoundtag) {
   }

   public final CompoundTag saveWithFullMetadata() {
      CompoundTag compoundtag = this.saveWithoutMetadata();
      this.saveMetadata(compoundtag);
      return compoundtag;
   }

   public final CompoundTag saveWithId() {
      CompoundTag compoundtag = this.saveWithoutMetadata();
      this.saveId(compoundtag);
      return compoundtag;
   }

   public final CompoundTag saveWithoutMetadata() {
      CompoundTag compoundtag = new CompoundTag();
      this.saveAdditional(compoundtag);
      return compoundtag;
   }

   private void saveId(CompoundTag compoundtag) {
      ResourceLocation resourcelocation = BlockEntityType.getKey(this.getType());
      if (resourcelocation == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         compoundtag.putString("id", resourcelocation.toString());
      }
   }

   public static void addEntityType(CompoundTag compoundtag, BlockEntityType<?> blockentitytype) {
      compoundtag.putString("id", BlockEntityType.getKey(blockentitytype).toString());
   }

   public void saveToItem(ItemStack itemstack) {
      BlockItem.setBlockEntityData(itemstack, this.getType(), this.saveWithoutMetadata());
   }

   private void saveMetadata(CompoundTag compoundtag) {
      this.saveId(compoundtag);
      compoundtag.putInt("x", this.worldPosition.getX());
      compoundtag.putInt("y", this.worldPosition.getY());
      compoundtag.putInt("z", this.worldPosition.getZ());
   }

   @Nullable
   public static BlockEntity loadStatic(BlockPos blockpos, BlockState blockstate, CompoundTag compoundtag) {
      String s = compoundtag.getString("id");
      ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
      if (resourcelocation == null) {
         LOGGER.error("Block entity has invalid type: {}", (Object)s);
         return null;
      } else {
         return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).map((blockentitytype) -> {
            try {
               return blockentitytype.create(blockpos, blockstate);
            } catch (Throwable var5) {
               LOGGER.error("Failed to create block entity {}", s, var5);
               return null;
            }
         }).map((blockentity) -> {
            try {
               blockentity.load(compoundtag);
               return blockentity;
            } catch (Throwable var4) {
               LOGGER.error("Failed to load data for block entity {}", s, var4);
               return null;
            }
         }).orElseGet(() -> {
            LOGGER.warn("Skipping BlockEntity with id {}", (Object)s);
            return null;
         });
      }
   }

   public void setChanged() {
      if (this.level != null) {
         setChanged(this.level, this.worldPosition, this.blockState);
      }

   }

   protected static void setChanged(Level level, BlockPos blockpos, BlockState blockstate) {
      level.blockEntityChanged(blockpos);
      if (!blockstate.isAir()) {
         level.updateNeighbourForOutputSignal(blockpos, blockstate.getBlock());
      }

   }

   public BlockPos getBlockPos() {
      return this.worldPosition;
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return null;
   }

   public CompoundTag getUpdateTag() {
      return new CompoundTag();
   }

   public boolean isRemoved() {
      return this.remove;
   }

   public void setRemoved() {
      this.remove = true;
   }

   public void clearRemoved() {
      this.remove = false;
   }

   public boolean triggerEvent(int i, int j) {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory crashreportcategory) {
      crashreportcategory.setDetail("Name", () -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName());
      if (this.level != null) {
         CrashReportCategory.populateBlockDetails(crashreportcategory, this.level, this.worldPosition, this.getBlockState());
         CrashReportCategory.populateBlockDetails(crashreportcategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
      }
   }

   public boolean onlyOpCanSetNbt() {
      return false;
   }

   public BlockEntityType<?> getType() {
      return this.type;
   }

   /** @deprecated */
   @Deprecated
   public void setBlockState(BlockState blockstate) {
      this.blockState = blockstate;
   }
}
