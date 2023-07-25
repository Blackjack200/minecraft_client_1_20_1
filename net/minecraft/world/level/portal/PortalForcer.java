package net.minecraft.world.level.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

public class PortalForcer {
   private static final int TICKET_RADIUS = 3;
   private static final int SEARCH_RADIUS = 128;
   private static final int CREATE_RADIUS = 16;
   private static final int FRAME_HEIGHT = 5;
   private static final int FRAME_WIDTH = 4;
   private static final int FRAME_BOX = 3;
   private static final int FRAME_HEIGHT_START = -1;
   private static final int FRAME_HEIGHT_END = 4;
   private static final int FRAME_WIDTH_START = -1;
   private static final int FRAME_WIDTH_END = 3;
   private static final int FRAME_BOX_START = -1;
   private static final int FRAME_BOX_END = 2;
   private static final int NOTHING_FOUND = -1;
   private final ServerLevel level;

   public PortalForcer(ServerLevel serverlevel) {
      this.level = serverlevel;
   }

   public Optional<BlockUtil.FoundRectangle> findPortalAround(BlockPos blockpos, boolean flag, WorldBorder worldborder) {
      PoiManager poimanager = this.level.getPoiManager();
      int i = flag ? 16 : 128;
      poimanager.ensureLoadedAndValid(this.level, blockpos, i);
      Optional<PoiRecord> optional = poimanager.getInSquare((holder) -> holder.is(PoiTypes.NETHER_PORTAL), blockpos, i, PoiManager.Occupancy.ANY).filter((poirecord4) -> worldborder.isWithinBounds(poirecord4.getPos())).sorted(Comparator.comparingDouble((poirecord3) -> poirecord3.getPos().distSqr(blockpos)).thenComparingInt((poirecord2) -> poirecord2.getPos().getY())).filter((poirecord1) -> this.level.getBlockState(poirecord1.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS)).findFirst();
      return optional.map((poirecord) -> {
         BlockPos blockpos1 = poirecord.getPos();
         this.level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockpos1), 3, blockpos1);
         BlockState blockstate = this.level.getBlockState(blockpos1);
         return BlockUtil.getLargestRectangleAround(blockpos1, blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, (blockpos2) -> this.level.getBlockState(blockpos2) == blockstate);
      });
   }

   public Optional<BlockUtil.FoundRectangle> createPortal(BlockPos blockpos, Direction.Axis direction_axis) {
      Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, direction_axis);
      double d0 = -1.0D;
      BlockPos blockpos1 = null;
      double d1 = -1.0D;
      BlockPos blockpos2 = null;
      WorldBorder worldborder = this.level.getWorldBorder();
      int i = Math.min(this.level.getMaxBuildHeight(), this.level.getMinBuildHeight() + this.level.getLogicalHeight()) - 1;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(BlockPos.MutableBlockPos blockpos_mutableblockpos1 : BlockPos.spiralAround(blockpos, 16, Direction.EAST, Direction.SOUTH)) {
         int j = Math.min(i, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockpos_mutableblockpos1.getX(), blockpos_mutableblockpos1.getZ()));
         int k = 1;
         if (worldborder.isWithinBounds(blockpos_mutableblockpos1) && worldborder.isWithinBounds(blockpos_mutableblockpos1.move(direction, 1))) {
            blockpos_mutableblockpos1.move(direction.getOpposite(), 1);

            for(int l = j; l >= this.level.getMinBuildHeight(); --l) {
               blockpos_mutableblockpos1.setY(l);
               if (this.canPortalReplaceBlock(blockpos_mutableblockpos1)) {
                  int i1;
                  for(i1 = l; l > this.level.getMinBuildHeight() && this.canPortalReplaceBlock(blockpos_mutableblockpos1.move(Direction.DOWN)); --l) {
                  }

                  if (l + 4 <= i) {
                     int j1 = i1 - l;
                     if (j1 <= 0 || j1 >= 3) {
                        blockpos_mutableblockpos1.setY(l);
                        if (this.canHostFrame(blockpos_mutableblockpos1, blockpos_mutableblockpos, direction, 0)) {
                           double d2 = blockpos.distSqr(blockpos_mutableblockpos1);
                           if (this.canHostFrame(blockpos_mutableblockpos1, blockpos_mutableblockpos, direction, -1) && this.canHostFrame(blockpos_mutableblockpos1, blockpos_mutableblockpos, direction, 1) && (d0 == -1.0D || d0 > d2)) {
                              d0 = d2;
                              blockpos1 = blockpos_mutableblockpos1.immutable();
                           }

                           if (d0 == -1.0D && (d1 == -1.0D || d1 > d2)) {
                              d1 = d2;
                              blockpos2 = blockpos_mutableblockpos1.immutable();
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (d0 == -1.0D && d1 != -1.0D) {
         blockpos1 = blockpos2;
         d0 = d1;
      }

      if (d0 == -1.0D) {
         int k1 = Math.max(this.level.getMinBuildHeight() - -1, 70);
         int l1 = i - 9;
         if (l1 < k1) {
            return Optional.empty();
         }

         blockpos1 = (new BlockPos(blockpos.getX(), Mth.clamp(blockpos.getY(), k1, l1), blockpos.getZ())).immutable();
         Direction direction1 = direction.getClockWise();
         if (!worldborder.isWithinBounds(blockpos1)) {
            return Optional.empty();
         }

         for(int i2 = -1; i2 < 2; ++i2) {
            for(int j2 = 0; j2 < 2; ++j2) {
               for(int k2 = -1; k2 < 3; ++k2) {
                  BlockState blockstate = k2 < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                  blockpos_mutableblockpos.setWithOffset(blockpos1, j2 * direction.getStepX() + i2 * direction1.getStepX(), k2, j2 * direction.getStepZ() + i2 * direction1.getStepZ());
                  this.level.setBlockAndUpdate(blockpos_mutableblockpos, blockstate);
               }
            }
         }
      }

      for(int l2 = -1; l2 < 3; ++l2) {
         for(int i3 = -1; i3 < 4; ++i3) {
            if (l2 == -1 || l2 == 2 || i3 == -1 || i3 == 3) {
               blockpos_mutableblockpos.setWithOffset(blockpos1, l2 * direction.getStepX(), i3, l2 * direction.getStepZ());
               this.level.setBlock(blockpos_mutableblockpos, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
         }
      }

      BlockState blockstate1 = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, direction_axis);

      for(int j3 = 0; j3 < 2; ++j3) {
         for(int k3 = 0; k3 < 3; ++k3) {
            blockpos_mutableblockpos.setWithOffset(blockpos1, j3 * direction.getStepX(), k3, j3 * direction.getStepZ());
            this.level.setBlock(blockpos_mutableblockpos, blockstate1, 18);
         }
      }

      return Optional.of(new BlockUtil.FoundRectangle(blockpos1.immutable(), 2, 3));
   }

   private boolean canPortalReplaceBlock(BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      BlockState blockstate = this.level.getBlockState(blockpos_mutableblockpos);
      return blockstate.canBeReplaced() && blockstate.getFluidState().isEmpty();
   }

   private boolean canHostFrame(BlockPos blockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos, Direction direction, int i) {
      Direction direction1 = direction.getClockWise();

      for(int j = -1; j < 3; ++j) {
         for(int k = -1; k < 4; ++k) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction.getStepX() * j + direction1.getStepX() * i, k, direction.getStepZ() * j + direction1.getStepZ() * i);
            if (k < 0 && !this.level.getBlockState(blockpos_mutableblockpos).isSolid()) {
               return false;
            }

            if (k >= 0 && !this.canPortalReplaceBlock(blockpos_mutableblockpos)) {
               return false;
            }
         }
      }

      return true;
   }
}
