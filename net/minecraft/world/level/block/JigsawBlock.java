package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends Block implements EntityBlock, GameMasterBlock {
   public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

   protected JigsawBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(ORIENTATION);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(ORIENTATION, rotation.rotation().rotate(blockstate.getValue(ORIENTATION)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.setValue(ORIENTATION, mirror.rotation().rotate(blockstate.getValue(ORIENTATION)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Direction direction = blockplacecontext.getClickedFace();
      Direction direction1;
      if (direction.getAxis() == Direction.Axis.Y) {
         direction1 = blockplacecontext.getHorizontalDirection().getOpposite();
      } else {
         direction1 = Direction.UP;
      }

      return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction1));
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new JigsawBlockEntity(blockpos, blockstate);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof JigsawBlockEntity && player.canUseGameMasterBlocks()) {
         player.openJigsawBlock((JigsawBlockEntity)blockentity);
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static boolean canAttach(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1) {
      Direction direction = getFrontFacing(structuretemplate_structureblockinfo.state());
      Direction direction1 = getFrontFacing(structuretemplate_structureblockinfo1.state());
      Direction direction2 = getTopFacing(structuretemplate_structureblockinfo.state());
      Direction direction3 = getTopFacing(structuretemplate_structureblockinfo1.state());
      JigsawBlockEntity.JointType jigsawblockentity_jointtype = JigsawBlockEntity.JointType.byName(structuretemplate_structureblockinfo.nbt().getString("joint")).orElseGet(() -> direction.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE);
      boolean flag = jigsawblockentity_jointtype == JigsawBlockEntity.JointType.ROLLABLE;
      return direction == direction1.getOpposite() && (flag || direction2 == direction3) && structuretemplate_structureblockinfo.nbt().getString("target").equals(structuretemplate_structureblockinfo1.nbt().getString("name"));
   }

   public static Direction getFrontFacing(BlockState blockstate) {
      return blockstate.getValue(ORIENTATION).front();
   }

   public static Direction getTopFacing(BlockState blockstate) {
      return blockstate.getValue(ORIENTATION).top();
   }
}
