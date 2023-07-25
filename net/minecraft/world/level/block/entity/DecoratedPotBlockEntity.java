package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecoratedPotBlockEntity extends BlockEntity {
   public static final String TAG_SHERDS = "sherds";
   private DecoratedPotBlockEntity.Decorations decorations = DecoratedPotBlockEntity.Decorations.EMPTY;

   public DecoratedPotBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.DECORATED_POT, blockpos, blockstate);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      this.decorations.save(compoundtag);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.decorations = DecoratedPotBlockEntity.Decorations.load(compoundtag);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public Direction getDirection() {
      return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
   }

   public DecoratedPotBlockEntity.Decorations getDecorations() {
      return this.decorations;
   }

   public void setFromItem(ItemStack itemstack) {
      this.decorations = DecoratedPotBlockEntity.Decorations.load(BlockItem.getBlockEntityData(itemstack));
   }

   public static record Decorations(Item back, Item left, Item right, Item front) {
      public static final DecoratedPotBlockEntity.Decorations EMPTY = new DecoratedPotBlockEntity.Decorations(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);

      public CompoundTag save(CompoundTag compoundtag) {
         ListTag listtag = new ListTag();
         this.sorted().forEach((item) -> listtag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString())));
         compoundtag.put("sherds", listtag);
         return compoundtag;
      }

      public Stream<Item> sorted() {
         return Stream.of(this.back, this.left, this.right, this.front);
      }

      public static DecoratedPotBlockEntity.Decorations load(@Nullable CompoundTag compoundtag) {
         if (compoundtag != null && compoundtag.contains("sherds", 9)) {
            ListTag listtag = compoundtag.getList("sherds", 8);
            return new DecoratedPotBlockEntity.Decorations(itemFromTag(listtag, 0), itemFromTag(listtag, 1), itemFromTag(listtag, 2), itemFromTag(listtag, 3));
         } else {
            return EMPTY;
         }
      }

      private static Item itemFromTag(ListTag listtag, int i) {
         if (i >= listtag.size()) {
            return Items.BRICK;
         } else {
            Tag tag = listtag.get(i);
            return BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getAsString()));
         }
      }
   }
}
