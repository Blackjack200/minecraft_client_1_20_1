package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {
   private static final int SONG_END_PADDING = 20;
   private final NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   private int ticksSinceLastEvent;
   private long tickCount;
   private long recordStartedTick;
   private boolean isPlaying;

   public JukeboxBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.JUKEBOX, blockpos, blockstate);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("RecordItem", 10)) {
         this.items.set(0, ItemStack.of(compoundtag.getCompound("RecordItem")));
      }

      this.isPlaying = compoundtag.getBoolean("IsPlaying");
      this.recordStartedTick = compoundtag.getLong("RecordStartTick");
      this.tickCount = compoundtag.getLong("TickCount");
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (!this.getFirstItem().isEmpty()) {
         compoundtag.put("RecordItem", this.getFirstItem().save(new CompoundTag()));
      }

      compoundtag.putBoolean("IsPlaying", this.isPlaying);
      compoundtag.putLong("RecordStartTick", this.recordStartedTick);
      compoundtag.putLong("TickCount", this.tickCount);
   }

   public boolean isRecordPlaying() {
      return !this.getFirstItem().isEmpty() && this.isPlaying;
   }

   private void setHasRecordBlockState(@Nullable Entity entity, boolean flag) {
      if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
         this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(flag)), 2);
         this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
      }

   }

   @VisibleForTesting
   public void startPlaying() {
      this.recordStartedTick = this.tickCount;
      this.isPlaying = true;
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.level.levelEvent((Player)null, 1010, this.getBlockPos(), Item.getId(this.getFirstItem().getItem()));
      this.setChanged();
   }

   private void stopPlaying() {
      this.isPlaying = false;
      this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.level.levelEvent(1011, this.getBlockPos(), 0);
      this.setChanged();
   }

   private void tick(Level level, BlockPos blockpos, BlockState blockstate) {
      ++this.ticksSinceLastEvent;
      if (this.isRecordPlaying()) {
         Item var5 = this.getFirstItem().getItem();
         if (var5 instanceof RecordItem) {
            RecordItem recorditem = (RecordItem)var5;
            if (this.shouldRecordStopPlaying(recorditem)) {
               this.stopPlaying();
            } else if (this.shouldSendJukeboxPlayingEvent()) {
               this.ticksSinceLastEvent = 0;
               level.gameEvent(GameEvent.JUKEBOX_PLAY, blockpos, GameEvent.Context.of(blockstate));
               this.spawnMusicParticles(level, blockpos);
            }
         }
      }

      ++this.tickCount;
   }

   private boolean shouldRecordStopPlaying(RecordItem recorditem) {
      return this.tickCount >= this.recordStartedTick + (long)recorditem.getLengthInTicks() + 20L;
   }

   private boolean shouldSendJukeboxPlayingEvent() {
      return this.ticksSinceLastEvent >= 20;
   }

   public ItemStack getItem(int i) {
      return this.items.get(i);
   }

   public ItemStack removeItem(int i, int j) {
      ItemStack itemstack = Objects.requireNonNullElse(this.items.get(i), ItemStack.EMPTY);
      this.items.set(i, ItemStack.EMPTY);
      if (!itemstack.isEmpty()) {
         this.setHasRecordBlockState((Entity)null, false);
         this.stopPlaying();
      }

      return itemstack;
   }

   public void setItem(int i, ItemStack itemstack) {
      if (itemstack.is(ItemTags.MUSIC_DISCS) && this.level != null) {
         this.items.set(i, itemstack);
         this.setHasRecordBlockState((Entity)null, true);
         this.startPlaying();
      }

   }

   public int getMaxStackSize() {
      return 1;
   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public boolean canPlaceItem(int i, ItemStack itemstack) {
      return itemstack.is(ItemTags.MUSIC_DISCS) && this.getItem(i).isEmpty();
   }

   public boolean canTakeItem(Container container, int i, ItemStack itemstack) {
      return container.hasAnyMatching(ItemStack::isEmpty);
   }

   private void spawnMusicParticles(Level level, BlockPos blockpos) {
      if (level instanceof ServerLevel serverlevel) {
         Vec3 vec3 = Vec3.atBottomCenterOf(blockpos).add(0.0D, (double)1.2F, 0.0D);
         float f = (float)level.getRandom().nextInt(4) / 24.0F;
         serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0D, 0.0D, 1.0D);
      }

   }

   public void popOutRecord() {
      if (this.level != null && !this.level.isClientSide) {
         BlockPos blockpos = this.getBlockPos();
         ItemStack itemstack = this.getFirstItem();
         if (!itemstack.isEmpty()) {
            this.removeFirstItem();
            Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5D, 1.01D, 0.5D).offsetRandom(this.level.random, 0.7F);
            ItemStack itemstack1 = itemstack.copy();
            ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
            itementity.setDefaultPickUpDelay();
            this.level.addFreshEntity(itementity);
         }
      }
   }

   public static void playRecordTick(Level level, BlockPos blockpos, BlockState blockstate, JukeboxBlockEntity jukeboxblockentity) {
      jukeboxblockentity.tick(level, blockpos, blockstate);
   }

   @VisibleForTesting
   public void setRecordWithoutPlaying(ItemStack itemstack) {
      this.items.set(0, itemstack);
      this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
      this.setChanged();
   }
}
