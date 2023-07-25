package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class MinecartItem extends Item {
   private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
      private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

      public ItemStack execute(BlockSource blocksource, ItemStack itemstack) {
         Direction direction = blocksource.getBlockState().getValue(DispenserBlock.FACING);
         Level level = blocksource.getLevel();
         double d0 = blocksource.x() + (double)direction.getStepX() * 1.125D;
         double d1 = Math.floor(blocksource.y()) + (double)direction.getStepY();
         double d2 = blocksource.z() + (double)direction.getStepZ() * 1.125D;
         BlockPos blockpos = blocksource.getPos().relative(direction);
         BlockState blockstate = level.getBlockState(blockpos);
         RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock ? blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
         double d3;
         if (blockstate.is(BlockTags.RAILS)) {
            if (railshape.isAscending()) {
               d3 = 0.6D;
            } else {
               d3 = 0.1D;
            }
         } else {
            if (!blockstate.isAir() || !level.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
               return this.defaultDispenseItemBehavior.dispense(blocksource, itemstack);
            }

            BlockState blockstate1 = level.getBlockState(blockpos.below());
            RailShape railshape1 = blockstate1.getBlock() instanceof BaseRailBlock ? blockstate1.getValue(((BaseRailBlock)blockstate1.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            if (direction != Direction.DOWN && railshape1.isAscending()) {
               d3 = -0.4D;
            } else {
               d3 = -0.9D;
            }
         }

         AbstractMinecart abstractminecart = AbstractMinecart.createMinecart(level, d0, d1 + d3, d2, ((MinecartItem)itemstack.getItem()).type);
         if (itemstack.hasCustomHoverName()) {
            abstractminecart.setCustomName(itemstack.getHoverName());
         }

         level.addFreshEntity(abstractminecart);
         itemstack.shrink(1);
         return itemstack;
      }

      protected void playSound(BlockSource blocksource) {
         blocksource.getLevel().levelEvent(1000, blocksource.getPos(), 0);
      }
   };
   final AbstractMinecart.Type type;

   public MinecartItem(AbstractMinecart.Type abstractminecart_type, Item.Properties item_properties) {
      super(item_properties);
      this.type = abstractminecart_type;
      DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (!blockstate.is(BlockTags.RAILS)) {
         return InteractionResult.FAIL;
      } else {
         ItemStack itemstack = useoncontext.getItemInHand();
         if (!level.isClientSide) {
            RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock ? blockstate.getValue(((BaseRailBlock)blockstate.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d0 = 0.0D;
            if (railshape.isAscending()) {
               d0 = 0.5D;
            }

            AbstractMinecart abstractminecart = AbstractMinecart.createMinecart(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.0625D + d0, (double)blockpos.getZ() + 0.5D, this.type);
            if (itemstack.hasCustomHoverName()) {
               abstractminecart.setCustomName(itemstack.getHoverName());
            }

            level.addFreshEntity(abstractminecart);
            level.gameEvent(GameEvent.ENTITY_PLACE, blockpos, GameEvent.Context.of(useoncontext.getPlayer(), level.getBlockState(blockpos.below())));
         }

         itemstack.shrink(1);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }
}
