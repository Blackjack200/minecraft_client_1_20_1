package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SwordItem extends TieredItem implements Vanishable {
   private final float attackDamage;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public SwordItem(Tier tier, int i, float f, Item.Properties item_properties) {
      super(tier, item_properties);
      this.attackDamage = (float)i + tier.getAttackDamageBonus();
      ImmutableMultimap.Builder<Attribute, AttributeModifier> immutablemultimap_builder = ImmutableMultimap.builder();
      immutablemultimap_builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION));
      immutablemultimap_builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)f, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = immutablemultimap_builder.build();
   }

   public float getDamage() {
      return this.attackDamage;
   }

   public boolean canAttackBlock(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      return !player.isCreative();
   }

   public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
      if (blockstate.is(Blocks.COBWEB)) {
         return 15.0F;
      } else {
         return blockstate.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
      }
   }

   public boolean hurtEnemy(ItemStack itemstack, LivingEntity livingentity, LivingEntity livingentity1) {
      itemstack.hurtAndBreak(1, livingentity1, (livingentity2) -> livingentity2.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   public boolean mineBlock(ItemStack itemstack, Level level, BlockState blockstate, BlockPos blockpos, LivingEntity livingentity) {
      if (blockstate.getDestroySpeed(level, blockpos) != 0.0F) {
         itemstack.hurtAndBreak(2, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      }

      return true;
   }

   public boolean isCorrectToolForDrops(BlockState blockstate) {
      return blockstate.is(Blocks.COBWEB);
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentslot) {
      return equipmentslot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentslot);
   }
}
