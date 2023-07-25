package net.minecraft.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FrostWalkerEnchantment extends Enchantment {
   public FrostWalkerEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.ARMOR_FEET, aequipmentslot);
   }

   public int getMinCost(int i) {
      return i * 10;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 15;
   }

   public boolean isTreasureOnly() {
      return true;
   }

   public int getMaxLevel() {
      return 2;
   }

   public static void onEntityMoved(LivingEntity livingentity, Level level, BlockPos blockpos, int i) {
      if (livingentity.onGround()) {
         BlockState blockstate = Blocks.FROSTED_ICE.defaultBlockState();
         int j = Math.min(16, 2 + i);
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-j, -1, -j), blockpos.offset(j, -1, j))) {
            if (blockpos1.closerToCenterThan(livingentity.position(), (double)j)) {
               blockpos_mutableblockpos.set(blockpos1.getX(), blockpos1.getY() + 1, blockpos1.getZ());
               BlockState blockstate1 = level.getBlockState(blockpos_mutableblockpos);
               if (blockstate1.isAir()) {
                  BlockState blockstate2 = level.getBlockState(blockpos1);
                  if (blockstate2 == FrostedIceBlock.meltsInto() && blockstate.canSurvive(level, blockpos1) && level.isUnobstructed(blockstate, blockpos1, CollisionContext.empty())) {
                     level.setBlockAndUpdate(blockpos1, blockstate);
                     level.scheduleTick(blockpos1, Blocks.FROSTED_ICE, Mth.nextInt(livingentity.getRandom(), 60, 120));
                  }
               }
            }
         }

      }
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      return super.checkCompatibility(enchantment) && enchantment != Enchantments.DEPTH_STRIDER;
   }
}
