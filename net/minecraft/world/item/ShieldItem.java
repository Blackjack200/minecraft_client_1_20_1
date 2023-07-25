package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ShieldItem extends Item implements Equipable {
   public static final int EFFECTIVE_BLOCK_DELAY = 5;
   public static final float MINIMUM_DURABILITY_DAMAGE = 3.0F;
   public static final String TAG_BASE_COLOR = "Base";

   public ShieldItem(Item.Properties item_properties) {
      super(item_properties);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
   }

   public String getDescriptionId(ItemStack itemstack) {
      return BlockItem.getBlockEntityData(itemstack) != null ? this.getDescriptionId() + "." + getColor(itemstack).getName() : super.getDescriptionId(itemstack);
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      BannerItem.appendHoverTextFromBannerBlockEntityTag(itemstack, list);
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.BLOCK;
   }

   public int getUseDuration(ItemStack itemstack) {
      return 72000;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      player.startUsingItem(interactionhand);
      return InteractionResultHolder.consume(itemstack);
   }

   public boolean isValidRepairItem(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack1.is(ItemTags.PLANKS) || super.isValidRepairItem(itemstack, itemstack1);
   }

   public static DyeColor getColor(ItemStack itemstack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      return compoundtag != null ? DyeColor.byId(compoundtag.getInt("Base")) : DyeColor.WHITE;
   }

   public EquipmentSlot getEquipmentSlot() {
      return EquipmentSlot.OFFHAND;
   }
}
