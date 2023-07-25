package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
   public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

   public TntBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         if (level.hasNeighborSignal(blockpos)) {
            explode(level, blockpos);
            level.removeBlock(blockpos, false);
         }

      }
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (level.hasNeighborSignal(blockpos)) {
         explode(level, blockpos);
         level.removeBlock(blockpos, false);
      }

   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide() && !player.isCreative() && blockstate.getValue(UNSTABLE)) {
         explode(level, blockpos);
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   public void wasExploded(Level level, BlockPos blockpos, Explosion explosion) {
      if (!level.isClientSide) {
         PrimedTnt primedtnt = new PrimedTnt(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, explosion.getIndirectSourceEntity());
         int i = primedtnt.getFuse();
         primedtnt.setFuse((short)(level.random.nextInt(i / 4) + i / 8));
         level.addFreshEntity(primedtnt);
      }
   }

   public static void explode(Level level, BlockPos blockpos) {
      explode(level, blockpos, (LivingEntity)null);
   }

   private static void explode(Level level, BlockPos blockpos, @Nullable LivingEntity livingentity) {
      if (!level.isClientSide) {
         PrimedTnt primedtnt = new PrimedTnt(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, livingentity);
         level.addFreshEntity(primedtnt);
         level.playSound((Player)null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.gameEvent(livingentity, GameEvent.PRIME_FUSE, blockpos);
      }
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (!itemstack.is(Items.FLINT_AND_STEEL) && !itemstack.is(Items.FIRE_CHARGE)) {
         return super.use(blockstate, level, blockpos, player, interactionhand, blockhitresult);
      } else {
         explode(level, blockpos, player);
         level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 11);
         Item item = itemstack.getItem();
         if (!player.isCreative()) {
            if (itemstack.is(Items.FLINT_AND_STEEL)) {
               itemstack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(interactionhand));
            } else {
               itemstack.shrink(1);
            }
         }

         player.awardStat(Stats.ITEM_USED.get(item));
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      if (!level.isClientSide) {
         BlockPos blockpos = blockhitresult.getBlockPos();
         Entity entity = projectile.getOwner();
         if (projectile.isOnFire() && projectile.mayInteract(level, blockpos)) {
            explode(level, blockpos, entity instanceof LivingEntity ? (LivingEntity)entity : null);
            level.removeBlock(blockpos, false);
         }
      }

   }

   public boolean dropFromExplosion(Explosion explosion) {
      return false;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(UNSTABLE);
   }
}
