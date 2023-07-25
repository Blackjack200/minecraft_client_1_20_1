package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RedStoneOreBlock extends Block {
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   public RedStoneOreBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)));
   }

   public void attack(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      interact(blockstate, level, blockpos);
      super.attack(blockstate, level, blockpos, player);
   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
      if (!entity.isSteppingCarefully()) {
         interact(blockstate, level, blockpos);
      }

      super.stepOn(level, blockpos, blockstate, entity);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         spawnParticles(level, blockpos);
      } else {
         interact(blockstate, level, blockpos);
      }

      ItemStack itemstack = player.getItemInHand(interactionhand);
      return itemstack.getItem() instanceof BlockItem && (new BlockPlaceContext(player, interactionhand, itemstack, blockhitresult)).canPlace() ? InteractionResult.PASS : InteractionResult.SUCCESS;
   }

   private static void interact(BlockState blockstate, Level level, BlockPos blockpos) {
      spawnParticles(level, blockpos);
      if (!blockstate.getValue(LIT)) {
         level.setBlock(blockpos, blockstate.setValue(LIT, Boolean.valueOf(true)), 3);
      }

   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(LIT);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         serverlevel.setBlock(blockpos, blockstate.setValue(LIT, Boolean.valueOf(false)), 3);
      }

   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (flag && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
         int i = 1 + serverlevel.random.nextInt(5);
         this.popExperience(serverlevel, blockpos, i);
      }

   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         spawnParticles(level, blockpos);
      }

   }

   private static void spawnParticles(Level level, BlockPos blockpos) {
      double d0 = 0.5625D;
      RandomSource randomsource = level.random;

      for(Direction direction : Direction.values()) {
         BlockPos blockpos1 = blockpos.relative(direction);
         if (!level.getBlockState(blockpos1).isSolidRender(level, blockpos1)) {
            Direction.Axis direction_axis = direction.getAxis();
            double d1 = direction_axis == Direction.Axis.X ? 0.5D + 0.5625D * (double)direction.getStepX() : (double)randomsource.nextFloat();
            double d2 = direction_axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double)direction.getStepY() : (double)randomsource.nextFloat();
            double d3 = direction_axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double)direction.getStepZ() : (double)randomsource.nextFloat();
            level.addParticle(DustParticleOptions.REDSTONE, (double)blockpos.getX() + d1, (double)blockpos.getY() + d2, (double)blockpos.getZ() + d3, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LIT);
   }
}
