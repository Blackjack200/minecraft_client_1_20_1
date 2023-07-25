package net.minecraft.world.level.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
   public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2).filter((blockpos) -> Math.abs(blockpos.getX()) == 2 || Math.abs(blockpos.getZ()) == 2).map(BlockPos::immutable).toList();

   protected EnchantmentTableBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public static boolean isValidBookShelf(Level level, BlockPos blockpos, BlockPos blockpos1) {
      return level.getBlockState(blockpos.offset(blockpos1)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER) && level.getBlockState(blockpos.offset(blockpos1.getX() / 2, blockpos1.getY(), blockpos1.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      super.animateTick(blockstate, level, blockpos, randomsource);

      for(BlockPos blockpos1 : BOOKSHELF_OFFSETS) {
         if (randomsource.nextInt(16) == 0 && isValidBookShelf(level, blockpos, blockpos1)) {
            level.addParticle(ParticleTypes.ENCHANT, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 2.0D, (double)blockpos.getZ() + 0.5D, (double)((float)blockpos1.getX() + randomsource.nextFloat()) - 0.5D, (double)((float)blockpos1.getY() - randomsource.nextFloat() - 1.0F), (double)((float)blockpos1.getZ() + randomsource.nextFloat()) - 0.5D);
         }
      }

   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new EnchantmentTableBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return level.isClientSide ? createTickerHelper(blockentitytype, BlockEntityType.ENCHANTING_TABLE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         player.openMenu(blockstate.getMenuProvider(level, blockpos));
         return InteractionResult.CONSUME;
      }
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof EnchantmentTableBlockEntity) {
         Component component = ((Nameable)blockentity).getDisplayName();
         return new SimpleMenuProvider((i, inventory, player) -> new EnchantmentMenu(i, inventory, ContainerLevelAccess.create(level, blockpos)), component);
      } else {
         return null;
      }
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (itemstack.hasCustomHoverName()) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof EnchantmentTableBlockEntity) {
            ((EnchantmentTableBlockEntity)blockentity).setCustomName(itemstack.getHoverName());
         }
      }

   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
