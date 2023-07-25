package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BucketItem extends Item implements DispensibleContainerItem {
   private final Fluid content;

   public BucketItem(Fluid fluid, Item.Properties item_properties) {
      super(item_properties);
      this.content = fluid;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
      if (blockhitresult.getType() == HitResult.Type.MISS) {
         return InteractionResultHolder.pass(itemstack);
      } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
         return InteractionResultHolder.pass(itemstack);
      } else {
         BlockPos blockpos = blockhitresult.getBlockPos();
         Direction direction = blockhitresult.getDirection();
         BlockPos blockpos1 = blockpos.relative(direction);
         if (level.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos1, direction, itemstack)) {
            if (this.content == Fluids.EMPTY) {
               BlockState blockstate = level.getBlockState(blockpos);
               if (blockstate.getBlock() instanceof BucketPickup) {
                  BucketPickup bucketpickup = (BucketPickup)blockstate.getBlock();
                  ItemStack itemstack1 = bucketpickup.pickupBlock(level, blockpos, blockstate);
                  if (!itemstack1.isEmpty()) {
                     player.awardStat(Stats.ITEM_USED.get(this));
                     bucketpickup.getPickupSound().ifPresent((soundevent) -> player.playSound(soundevent, 1.0F, 1.0F));
                     level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);
                     ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, player, itemstack1);
                     if (!level.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemstack1);
                     }

                     return InteractionResultHolder.sidedSuccess(itemstack2, level.isClientSide());
                  }
               }

               return InteractionResultHolder.fail(itemstack);
            } else {
               BlockState blockstate1 = level.getBlockState(blockpos);
               BlockPos blockpos2 = blockstate1.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? blockpos : blockpos1;
               if (this.emptyContents(player, level, blockpos2, blockhitresult)) {
                  this.checkExtraContent(player, level, itemstack, blockpos2);
                  if (player instanceof ServerPlayer) {
                     CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos2, itemstack);
                  }

                  player.awardStat(Stats.ITEM_USED.get(this));
                  return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemstack, player), level.isClientSide());
               } else {
                  return InteractionResultHolder.fail(itemstack);
               }
            }
         } else {
            return InteractionResultHolder.fail(itemstack);
         }
      }
   }

   public static ItemStack getEmptySuccessItem(ItemStack itemstack, Player player) {
      return !player.getAbilities().instabuild ? new ItemStack(Items.BUCKET) : itemstack;
   }

   public void checkExtraContent(@Nullable Player player, Level level, ItemStack itemstack, BlockPos blockpos) {
   }

   public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockpos, @Nullable BlockHitResult blockhitresult) {
      if (!(this.content instanceof FlowingFluid)) {
         return false;
      } else {
         BlockState blockstate = level.getBlockState(blockpos);
         Block block = blockstate.getBlock();
         boolean flag = blockstate.canBeReplaced(this.content);
         boolean flag1 = blockstate.isAir() || flag || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(level, blockpos, blockstate, this.content);
         if (!flag1) {
            return blockhitresult != null && this.emptyContents(player, level, blockhitresult.getBlockPos().relative(blockhitresult.getDirection()), (BlockHitResult)null);
         } else if (level.dimensionType().ultraWarm() && this.content.is(FluidTags.WATER)) {
            int i = blockpos.getX();
            int j = blockpos.getY();
            int k = blockpos.getZ();
            level.playSound(player, blockpos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

            for(int l = 0; l < 8; ++l) {
               level.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
            }

            return true;
         } else if (block instanceof LiquidBlockContainer && this.content == Fluids.WATER) {
            ((LiquidBlockContainer)block).placeLiquid(level, blockpos, blockstate, ((FlowingFluid)this.content).getSource(false));
            this.playEmptySound(player, level, blockpos);
            return true;
         } else {
            if (!level.isClientSide && flag && !blockstate.liquid()) {
               level.destroyBlock(blockpos, true);
            }

            if (!level.setBlock(blockpos, this.content.defaultFluidState().createLegacyBlock(), 11) && !blockstate.getFluidState().isSource()) {
               return false;
            } else {
               this.playEmptySound(player, level, blockpos);
               return true;
            }
         }
      }
   }

   protected void playEmptySound(@Nullable Player player, LevelAccessor levelaccessor, BlockPos blockpos) {
      SoundEvent soundevent = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
      levelaccessor.playSound(player, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
      levelaccessor.gameEvent(player, GameEvent.FLUID_PLACE, blockpos);
   }
}
