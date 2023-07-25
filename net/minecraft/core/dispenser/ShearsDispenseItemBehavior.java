package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
   protected ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
      ServerLevel serverlevel = blocksource.getLevel();
      if (!serverlevel.isClientSide()) {
         BlockPos blockpos = blocksource.getPos().relative(blocksource.getBlockState().getValue(DispenserBlock.FACING));
         this.setSuccess(tryShearBeehive(serverlevel, blockpos) || tryShearLivingEntity(serverlevel, blockpos));
         if (this.isSuccess() && itemstack.hurt(1, serverlevel.getRandom(), (ServerPlayer)null)) {
            itemstack.setCount(0);
         }
      }

      return itemstack;
   }

   private static boolean tryShearBeehive(ServerLevel serverlevel, BlockPos blockpos) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      if (blockstate.is(BlockTags.BEEHIVES, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.hasProperty(BeehiveBlock.HONEY_LEVEL) && blockbehaviour_blockstatebase.getBlock() instanceof BeehiveBlock)) {
         int i = blockstate.getValue(BeehiveBlock.HONEY_LEVEL);
         if (i >= 5) {
            serverlevel.playSound((Player)null, blockpos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            BeehiveBlock.dropHoneycomb(serverlevel, blockpos);
            ((BeehiveBlock)blockstate.getBlock()).releaseBeesAndResetHoneyLevel(serverlevel, blockstate, blockpos, (Player)null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
            serverlevel.gameEvent((Entity)null, GameEvent.SHEAR, blockpos);
            return true;
         }
      }

      return false;
   }

   private static boolean tryShearLivingEntity(ServerLevel serverlevel, BlockPos blockpos) {
      for(LivingEntity livingentity : serverlevel.getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS)) {
         if (livingentity instanceof Shearable shearable) {
            if (shearable.readyForShearing()) {
               shearable.shear(SoundSource.BLOCKS);
               serverlevel.gameEvent((Entity)null, GameEvent.SHEAR, blockpos);
               return true;
            }
         }
      }

      return false;
   }
}
