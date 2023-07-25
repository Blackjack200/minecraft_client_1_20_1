package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClipContext {
   private final Vec3 from;
   private final Vec3 to;
   private final ClipContext.Block block;
   private final ClipContext.Fluid fluid;
   private final CollisionContext collisionContext;

   public ClipContext(Vec3 vec3, Vec3 vec31, ClipContext.Block clipcontext_block, ClipContext.Fluid clipcontext_fluid, Entity entity) {
      this.from = vec3;
      this.to = vec31;
      this.block = clipcontext_block;
      this.fluid = clipcontext_fluid;
      this.collisionContext = CollisionContext.of(entity);
   }

   public Vec3 getTo() {
      return this.to;
   }

   public Vec3 getFrom() {
      return this.from;
   }

   public VoxelShape getBlockShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return this.block.get(blockstate, blockgetter, blockpos, this.collisionContext);
   }

   public VoxelShape getFluidShape(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos) {
      return this.fluid.canPick(fluidstate) ? fluidstate.getShape(blockgetter, blockpos) : Shapes.empty();
   }

   public static enum Block implements ClipContext.ShapeGetter {
      COLLIDER(BlockBehaviour.BlockStateBase::getCollisionShape),
      OUTLINE(BlockBehaviour.BlockStateBase::getShape),
      VISUAL(BlockBehaviour.BlockStateBase::getVisualShape),
      FALLDAMAGE_RESETTING((blockstate, blockgetter, blockpos, collisioncontext) -> blockstate.is(BlockTags.FALL_DAMAGE_RESETTING) ? Shapes.block() : Shapes.empty());

      private final ClipContext.ShapeGetter shapeGetter;

      private Block(ClipContext.ShapeGetter clipcontext_shapegetter) {
         this.shapeGetter = clipcontext_shapegetter;
      }

      public VoxelShape get(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
         return this.shapeGetter.get(blockstate, blockgetter, blockpos, collisioncontext);
      }
   }

   public static enum Fluid {
      NONE((fluidstate) -> false),
      SOURCE_ONLY(FluidState::isSource),
      ANY((fluidstate) -> !fluidstate.isEmpty()),
      WATER((fluidstate) -> fluidstate.is(FluidTags.WATER));

      private final Predicate<FluidState> canPick;

      private Fluid(Predicate<FluidState> predicate) {
         this.canPick = predicate;
      }

      public boolean canPick(FluidState fluidstate) {
         return this.canPick.test(fluidstate);
      }
   }

   public interface ShapeGetter {
      VoxelShape get(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext);
   }
}
