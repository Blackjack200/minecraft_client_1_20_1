package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;

public class BundleItem extends Item {
   private static final String TAG_ITEMS = "Items";
   public static final int MAX_WEIGHT = 64;
   private static final int BUNDLE_IN_BUNDLE_WEIGHT = 4;
   private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

   public BundleItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public static float getFullnessDisplay(ItemStack itemstack) {
      return (float)getContentWeight(itemstack) / 64.0F;
   }

   public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction clickaction, Player player) {
      if (clickaction != ClickAction.SECONDARY) {
         return false;
      } else {
         ItemStack itemstack1 = slot.getItem();
         if (itemstack1.isEmpty()) {
            this.playRemoveOneSound(player);
            removeOne(itemstack).ifPresent((itemstack3) -> add(itemstack, slot.safeInsert(itemstack3)));
         } else if (itemstack1.getItem().canFitInsideContainerItems()) {
            int i = (64 - getContentWeight(itemstack)) / getWeight(itemstack1);
            int j = add(itemstack, slot.safeTake(itemstack1.getCount(), i, player));
            if (j > 0) {
               this.playInsertSound(player);
            }
         }

         return true;
      }
   }

   public boolean overrideOtherStackedOnMe(ItemStack itemstack, ItemStack itemstack1, Slot slot, ClickAction clickaction, Player player, SlotAccess slotaccess) {
      if (clickaction == ClickAction.SECONDARY && slot.allowModification(player)) {
         if (itemstack1.isEmpty()) {
            removeOne(itemstack).ifPresent((itemstack2) -> {
               this.playRemoveOneSound(player);
               slotaccess.set(itemstack2);
            });
         } else {
            int i = add(itemstack, itemstack1);
            if (i > 0) {
               this.playInsertSound(player);
               itemstack1.shrink(i);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (dropContents(itemstack, player)) {
         this.playDropContentsSound(player);
         player.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   public boolean isBarVisible(ItemStack itemstack) {
      return getContentWeight(itemstack) > 0;
   }

   public int getBarWidth(ItemStack itemstack) {
      return Math.min(1 + 12 * getContentWeight(itemstack) / 64, 13);
   }

   public int getBarColor(ItemStack itemstack) {
      return BAR_COLOR;
   }

   private static int add(ItemStack itemstack, ItemStack itemstack1) {
      if (!itemstack1.isEmpty() && itemstack1.getItem().canFitInsideContainerItems()) {
         CompoundTag compoundtag = itemstack.getOrCreateTag();
         if (!compoundtag.contains("Items")) {
            compoundtag.put("Items", new ListTag());
         }

         int i = getContentWeight(itemstack);
         int j = getWeight(itemstack1);
         int k = Math.min(itemstack1.getCount(), (64 - i) / j);
         if (k == 0) {
            return 0;
         } else {
            ListTag listtag = compoundtag.getList("Items", 10);
            Optional<CompoundTag> optional = getMatchingItem(itemstack1, listtag);
            if (optional.isPresent()) {
               CompoundTag compoundtag1 = optional.get();
               ItemStack itemstack2 = ItemStack.of(compoundtag1);
               itemstack2.grow(k);
               itemstack2.save(compoundtag1);
               listtag.remove(compoundtag1);
               listtag.add(0, (Tag)compoundtag1);
            } else {
               ItemStack itemstack3 = itemstack1.copyWithCount(k);
               CompoundTag compoundtag2 = new CompoundTag();
               itemstack3.save(compoundtag2);
               listtag.add(0, (Tag)compoundtag2);
            }

            return k;
         }
      } else {
         return 0;
      }
   }

   private static Optional<CompoundTag> getMatchingItem(ItemStack itemstack, ListTag listtag) {
      return itemstack.is(Items.BUNDLE) ? Optional.empty() : listtag.stream().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).filter((compoundtag) -> ItemStack.isSameItemSameTags(ItemStack.of(compoundtag), itemstack)).findFirst();
   }

   private static int getWeight(ItemStack itemstack) {
      if (itemstack.is(Items.BUNDLE)) {
         return 4 + getContentWeight(itemstack);
      } else {
         if ((itemstack.is(Items.BEEHIVE) || itemstack.is(Items.BEE_NEST)) && itemstack.hasTag()) {
            CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
            if (compoundtag != null && !compoundtag.getList("Bees", 10).isEmpty()) {
               return 64;
            }
         }

         return 64 / itemstack.getMaxStackSize();
      }
   }

   private static int getContentWeight(ItemStack itemstack) {
      return getContents(itemstack).mapToInt((itemstack1) -> getWeight(itemstack1) * itemstack1.getCount()).sum();
   }

   private static Optional<ItemStack> removeOne(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      if (!compoundtag.contains("Items")) {
         return Optional.empty();
      } else {
         ListTag listtag = compoundtag.getList("Items", 10);
         if (listtag.isEmpty()) {
            return Optional.empty();
         } else {
            int i = 0;
            CompoundTag compoundtag1 = listtag.getCompound(0);
            ItemStack itemstack1 = ItemStack.of(compoundtag1);
            listtag.remove(0);
            if (listtag.isEmpty()) {
               itemstack.removeTagKey("Items");
            }

            return Optional.of(itemstack1);
         }
      }
   }

   private static boolean dropContents(ItemStack itemstack, Player player) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      if (!compoundtag.contains("Items")) {
         return false;
      } else {
         if (player instanceof ServerPlayer) {
            ListTag listtag = compoundtag.getList("Items", 10);

            for(int i = 0; i < listtag.size(); ++i) {
               CompoundTag compoundtag1 = listtag.getCompound(i);
               ItemStack itemstack1 = ItemStack.of(compoundtag1);
               player.drop(itemstack1, true);
            }
         }

         itemstack.removeTagKey("Items");
         return true;
      }
   }

   private static Stream<ItemStack> getContents(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag == null) {
         return Stream.empty();
      } else {
         ListTag listtag = compoundtag.getList("Items", 10);
         return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
      }
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack itemstack) {
      NonNullList<ItemStack> nonnulllist = NonNullList.create();
      getContents(itemstack).forEach(nonnulllist::add);
      return Optional.of(new BundleTooltip(nonnulllist, getContentWeight(itemstack)));
   }

   public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag tooltipflag) {
      list.add(Component.translatable("item.minecraft.bundle.fullness", getContentWeight(itemstack), 64).withStyle(ChatFormatting.GRAY));
   }

   public void onDestroyed(ItemEntity itementity) {
      ItemUtils.onContainerDestroyed(itementity, getContents(itementity.getItem()));
   }

   private void playRemoveOneSound(Entity entity) {
      entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   private void playInsertSound(Entity entity) {
      entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }

   private void playDropContentsSound(Entity entity) {
      entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
   }
}
