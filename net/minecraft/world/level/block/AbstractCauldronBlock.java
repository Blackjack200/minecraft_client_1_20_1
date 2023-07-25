package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCauldronBlock extends Block {
   private static final int SIDE_THICKNESS = 2;
   private static final int LEG_WIDTH = 4;
   private static final int LEG_HEIGHT = 3;
   private static final int LEG_DEPTH = 2;
   protected static final int FLOOR_LEVEL = 4;
   private static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), BooleanOp.ONLY_FIRST);
   private final Map<Item, CauldronInteraction> interactions;

   public AbstractCauldronBlock(BlockBehaviour.Properties blockbehaviour_properties, Map<Item, CauldronInteraction> map) {
      super(blockbehaviour_properties);
      this.interactions = map;
   }

   protected double getContentHeight(BlockState blockstate) {
      return 0.0D;
   }

   protected boolean isEntityInsideContent(BlockState blockstate, BlockPos blockpos, Entity entity) {
      return entity.getY() < (double)blockpos.getY() + this.getContentHeight(blockstate) && entity.getBoundingBox().maxY > (double)blockpos.getY() + 0.25D;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      CauldronInteraction cauldroninteraction = this.interactions.get(itemstack.getItem());
      return cauldroninteraction.interact(blockstate, level, blockpos, player, interactionhand, itemstack);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public VoxelShape getInteractionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return INSIDE;
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public abstract boolean isFull(BlockState blockstate);

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockPos blockpos1 = PointedDripstoneBlock.findStalactiteTipAboveCauldron(serverlevel, blockpos);
      if (blockpos1 != null) {
         Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(serverlevel, blockpos1);
         if (fluid != Fluids.EMPTY && this.canReceiveStalactiteDrip(fluid)) {
            this.receiveStalactiteDrip(blockstate, serverlevel, blockpos, fluid);
         }

      }
   }

   protected boolean canReceiveStalactiteDrip(Fluid fluid) {
      return false;
   }

   protected void receiveStalactiteDrip(BlockState blockstate, Level level, BlockPos blockpos, Fluid fluid) {
   }
}
