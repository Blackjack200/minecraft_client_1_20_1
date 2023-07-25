package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BannerBlockEntity extends BlockEntity implements Nameable {
   public static final int MAX_PATTERNS = 6;
   public static final String TAG_PATTERNS = "Patterns";
   public static final String TAG_PATTERN = "Pattern";
   public static final String TAG_COLOR = "Color";
   @Nullable
   private Component name;
   private DyeColor baseColor;
   @Nullable
   private ListTag itemPatterns;
   @Nullable
   private List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

   public BannerBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BANNER, blockpos, blockstate);
      this.baseColor = ((AbstractBannerBlock)blockstate.getBlock()).getColor();
   }

   public BannerBlockEntity(BlockPos blockpos, BlockState blockstate, DyeColor dyecolor) {
      this(blockpos, blockstate);
      this.baseColor = dyecolor;
   }

   @Nullable
   public static ListTag getItemPatterns(ItemStack itemstack) {
      ListTag listtag = null;
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag != null && compoundtag.contains("Patterns", 9)) {
         listtag = compoundtag.getList("Patterns", 10).copy();
      }

      return listtag;
   }

   public void fromItem(ItemStack itemstack, DyeColor dyecolor) {
      this.baseColor = dyecolor;
      this.fromItem(itemstack);
   }

   public void fromItem(ItemStack itemstack) {
      this.itemPatterns = getItemPatterns(itemstack);
      this.patterns = null;
      this.name = itemstack.hasCustomHoverName() ? itemstack.getHoverName() : null;
   }

   public Component getName() {
      return (Component)(this.name != null ? this.name : Component.translatable("block.minecraft.banner"));
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }

   public void setCustomName(Component component) {
      this.name = component;
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (this.itemPatterns != null) {
         compoundtag.put("Patterns", this.itemPatterns);
      }

      if (this.name != null) {
         compoundtag.putString("CustomName", Component.Serializer.toJson(this.name));
      }

   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("CustomName", 8)) {
         this.name = Component.Serializer.fromJson(compoundtag.getString("CustomName"));
      }

      this.itemPatterns = compoundtag.getList("Patterns", 10);
      this.patterns = null;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public static int getPatternCount(ItemStack itemstack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      return compoundtag != null && compoundtag.contains("Patterns") ? compoundtag.getList("Patterns", 10).size() : 0;
   }

   public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns() {
      if (this.patterns == null) {
         this.patterns = createPatterns(this.baseColor, this.itemPatterns);
      }

      return this.patterns;
   }

   public static List<Pair<Holder<BannerPattern>, DyeColor>> createPatterns(DyeColor dyecolor, @Nullable ListTag listtag) {
      List<Pair<Holder<BannerPattern>, DyeColor>> list = Lists.newArrayList();
      list.add(Pair.of(BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(BannerPatterns.BASE), dyecolor));
      if (listtag != null) {
         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            Holder<BannerPattern> holder = BannerPattern.byHash(compoundtag.getString("Pattern"));
            if (holder != null) {
               int j = compoundtag.getInt("Color");
               list.add(Pair.of(holder, DyeColor.byId(j)));
            }
         }
      }

      return list;
   }

   public static void removeLastPattern(ItemStack itemstack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag != null && compoundtag.contains("Patterns", 9)) {
         ListTag listtag = compoundtag.getList("Patterns", 10);
         if (!listtag.isEmpty()) {
            listtag.remove(listtag.size() - 1);
            if (listtag.isEmpty()) {
               compoundtag.remove("Patterns");
            }

            BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, compoundtag);
         }
      }
   }

   public ItemStack getItem() {
      ItemStack itemstack = new ItemStack(BannerBlock.byColor(this.baseColor));
      if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.put("Patterns", this.itemPatterns.copy());
         BlockItem.setBlockEntityData(itemstack, this.getType(), compoundtag);
      }

      if (this.name != null) {
         itemstack.setHoverName(this.name);
      }

      return itemstack;
   }

   public DyeColor getBaseColor() {
      return this.baseColor;
   }
}
