package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LecternBlockEntity extends BlockEntity implements Clearable, MenuProvider {
   public static final int DATA_PAGE = 0;
   public static final int NUM_DATA = 1;
   public static final int SLOT_BOOK = 0;
   public static final int NUM_SLOTS = 1;
   private final Container bookAccess = new Container() {
      public int getContainerSize() {
         return 1;
      }

      public boolean isEmpty() {
         return LecternBlockEntity.this.book.isEmpty();
      }

      public ItemStack getItem(int i) {
         return i == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
      }

      public ItemStack removeItem(int i, int j) {
         if (i == 0) {
            ItemStack itemstack = LecternBlockEntity.this.book.split(j);
            if (LecternBlockEntity.this.book.isEmpty()) {
               LecternBlockEntity.this.onBookItemRemove();
            }

            return itemstack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public ItemStack removeItemNoUpdate(int i) {
         if (i == 0) {
            ItemStack itemstack = LecternBlockEntity.this.book;
            LecternBlockEntity.this.book = ItemStack.EMPTY;
            LecternBlockEntity.this.onBookItemRemove();
            return itemstack;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public void setItem(int i, ItemStack itemstack) {
      }

      public int getMaxStackSize() {
         return 1;
      }

      public void setChanged() {
         LecternBlockEntity.this.setChanged();
      }

      public boolean stillValid(Player player) {
         return Container.stillValidBlockEntity(LecternBlockEntity.this, player) && LecternBlockEntity.this.hasBook();
      }

      public boolean canPlaceItem(int i, ItemStack itemstack) {
         return false;
      }

      public void clearContent() {
      }
   };
   private final ContainerData dataAccess = new ContainerData() {
      public int get(int i) {
         return i == 0 ? LecternBlockEntity.this.page : 0;
      }

      public void set(int i, int j) {
         if (i == 0) {
            LecternBlockEntity.this.setPage(j);
         }

      }

      public int getCount() {
         return 1;
      }
   };
   ItemStack book = ItemStack.EMPTY;
   int page;
   private int pageCount;

   public LecternBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.LECTERN, blockpos, blockstate);
   }

   public ItemStack getBook() {
      return this.book;
   }

   public boolean hasBook() {
      return this.book.is(Items.WRITABLE_BOOK) || this.book.is(Items.WRITTEN_BOOK);
   }

   public void setBook(ItemStack itemstack) {
      this.setBook(itemstack, (Player)null);
   }

   void onBookItemRemove() {
      this.page = 0;
      this.pageCount = 0;
      LecternBlock.resetBookState((Entity)null, this.getLevel(), this.getBlockPos(), this.getBlockState(), false);
   }

   public void setBook(ItemStack itemstack, @Nullable Player player) {
      this.book = this.resolveBook(itemstack, player);
      this.page = 0;
      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.setChanged();
   }

   void setPage(int i) {
      int j = Mth.clamp(i, 0, this.pageCount - 1);
      if (j != this.page) {
         this.page = j;
         this.setChanged();
         LecternBlock.signalPageChange(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public int getPage() {
      return this.page;
   }

   public int getRedstoneSignal() {
      float f = this.pageCount > 1 ? (float)this.getPage() / ((float)this.pageCount - 1.0F) : 1.0F;
      return Mth.floor(f * 14.0F) + (this.hasBook() ? 1 : 0);
   }

   private ItemStack resolveBook(ItemStack itemstack, @Nullable Player player) {
      if (this.level instanceof ServerLevel && itemstack.is(Items.WRITTEN_BOOK)) {
         WrittenBookItem.resolveBookComponents(itemstack, this.createCommandSourceStack(player), player);
      }

      return itemstack;
   }

   private CommandSourceStack createCommandSourceStack(@Nullable Player player) {
      String s;
      Component component;
      if (player == null) {
         s = "Lectern";
         component = Component.literal("Lectern");
      } else {
         s = player.getName().getString();
         component = player.getDisplayName();
      }

      Vec3 vec3 = Vec3.atCenterOf(this.worldPosition);
      return new CommandSourceStack(CommandSource.NULL, vec3, Vec2.ZERO, (ServerLevel)this.level, 2, s, component, this.level.getServer(), player);
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("Book", 10)) {
         this.book = this.resolveBook(ItemStack.of(compoundtag.getCompound("Book")), (Player)null);
      } else {
         this.book = ItemStack.EMPTY;
      }

      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.page = Mth.clamp(compoundtag.getInt("Page"), 0, this.pageCount - 1);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (!this.getBook().isEmpty()) {
         compoundtag.put("Book", this.getBook().save(new CompoundTag()));
         compoundtag.putInt("Page", this.page);
      }

   }

   public void clearContent() {
      this.setBook(ItemStack.EMPTY);
   }

   public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      return new LecternMenu(i, this.bookAccess, this.dataAccess);
   }

   public Component getDisplayName() {
      return Component.translatable("container.lectern");
   }
}
