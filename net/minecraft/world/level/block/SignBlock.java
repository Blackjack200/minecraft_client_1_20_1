package net.minecraft.world.level.block;

import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final float AABB_OFFSET = 4.0F;
   protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   private final WoodType type;

   protected SignBlock(BlockBehaviour.Properties blockbehaviour_properties, WoodType woodtype) {
      super(blockbehaviour_properties);
      this.type = woodtype;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public boolean isPossibleToRespawnInThis(BlockState blockstate) {
      return true;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SignBlockEntity(blockpos, blockstate);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      Item item = itemstack.getItem();
      Item signblockentity = itemstack.getItem();
      SignApplicator var10000;
      if (signblockentity instanceof SignApplicator signapplicator) {
         var10000 = signapplicator;
      } else {
         var10000 = null;
      }

      SignApplicator signapplicator1 = var10000;
      boolean flag = signapplicator1 != null && player.mayBuild();
      BlockEntity flag1 = level.getBlockEntity(blockpos);
      if (flag1 instanceof SignBlockEntity signblockentity) {
         if (!level.isClientSide) {
            boolean flag1 = signblockentity.isFacingFrontText(player);
            SignText signtext = signblockentity.getText(flag1);
            boolean flag2 = signblockentity.executeClickCommandsIfPresent(player, level, blockpos, flag1);
            if (signblockentity.isWaxed()) {
               level.playSound((Player)null, signblockentity.getBlockPos(), SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.BLOCKS);
               return InteractionResult.PASS;
            } else if (flag && !this.otherPlayerIsEditingSign(player, signblockentity) && signapplicator1.canApplyToSign(signtext, player) && signapplicator1.tryApplyToSign(level, signblockentity, flag1, player)) {
               if (!player.isCreative()) {
                  itemstack.shrink(1);
               }

               level.gameEvent(GameEvent.BLOCK_CHANGE, signblockentity.getBlockPos(), GameEvent.Context.of(player, signblockentity.getBlockState()));
               player.awardStat(Stats.ITEM_USED.get(item));
               return InteractionResult.SUCCESS;
            } else if (flag2) {
               return InteractionResult.SUCCESS;
            } else if (!this.otherPlayerIsEditingSign(player, signblockentity) && player.mayBuild() && this.hasEditableText(player, signblockentity, flag1)) {
               this.openTextEdit(player, signblockentity, flag1);
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         } else {
            return !flag && !signblockentity.isWaxed() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private boolean hasEditableText(Player player, SignBlockEntity signblockentity, boolean flag) {
      SignText signtext = signblockentity.getText(flag);
      return Arrays.stream(signtext.getMessages(player.isTextFilteringEnabled())).allMatch((component) -> component.equals(CommonComponents.EMPTY) || component.getContents() instanceof LiteralContents);
   }

   public abstract float getYRotationDegrees(BlockState blockstate);

   public Vec3 getSignHitboxCenterPosition(BlockState blockstate) {
      return new Vec3(0.5D, 0.5D, 0.5D);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public WoodType type() {
      return this.type;
   }

   public static WoodType getWoodType(Block block) {
      WoodType woodtype;
      if (block instanceof SignBlock) {
         woodtype = ((SignBlock)block).type();
      } else {
         woodtype = WoodType.OAK;
      }

      return woodtype;
   }

   public void openTextEdit(Player player, SignBlockEntity signblockentity, boolean flag) {
      signblockentity.setAllowedPlayerEditor(player.getUUID());
      player.openTextEdit(signblockentity, flag);
   }

   private boolean otherPlayerIsEditingSign(Player player, SignBlockEntity signblockentity) {
      UUID uuid = signblockentity.getPlayerWhoMayEdit();
      return uuid != null && !uuid.equals(player.getUUID());
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createTickerHelper(blockentitytype, BlockEntityType.SIGN, SignBlockEntity::tick);
   }
}
