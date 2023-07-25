package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiggerItem extends TieredItem implements Vanishable {
   private final TagKey<Block> blocks;
   protected final float speed;
   private final float attackDamageBaseline;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   protected DiggerItem(float f, float f1, Tier tier, TagKey<Block> tagkey, Item.Properties item_properties) {
      super(tier, item_properties);
      this.blocks = tagkey;
      this.speed = tier.getSpeed();
      this.attackDamageBaseline = f + tier.getAttackDamageBonus();
      ImmutableMultimap.Builder<Attribute, AttributeModifier> immutablemultimap_builder = ImmutableMultimap.builder();
      immutablemultimap_builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, AttributeModifier.Operation.ADDITION));
      immutablemultimap_builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)f1, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = immutablemultimap_builder.build();
   }

   public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
      return blockstate.is(this.blocks) ? this.speed : 1.0F;
   }

   public boolean hurtEnemy(ItemStack itemstack, LivingEntity livingentity, LivingEntity livingentity1) {
      itemstack.hurtAndBreak(2, livingentity1, (livingentity2) -> livingentity2.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   public boolean mineBlock(ItemStack itemstack, Level level, BlockState blockstate, BlockPos blockpos, LivingEntity livingentity) {
      if (!level.isClientSide && blockstate.getDestroySpeed(level, blockpos) != 0.0F) {
         itemstack.hurtAndBreak(1, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      }

      return true;
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentslot) {
      return equipmentslot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentslot);
   }

   public float getAttackDamage() {
      return this.attackDamageBaseline;
   }

   public boolean isCorrectToolForDrops(BlockState blockstate) {
      int i = this.getTier().getLevel();
      if (i < 3 && blockstate.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
         return false;
      } else if (i < 2 && blockstate.is(BlockTags.NEEDS_IRON_TOOL)) {
         return false;
      } else {
         return i < 1 && blockstate.is(BlockTags.NEEDS_STONE_TOOL) ? false : blockstate.is(this.blocks);
      }
   }
}
