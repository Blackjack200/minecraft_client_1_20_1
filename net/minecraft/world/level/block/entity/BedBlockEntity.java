package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BedBlockEntity extends BlockEntity {
   private DyeColor color;

   public BedBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BED, blockpos, blockstate);
      this.color = ((BedBlock)blockstate.getBlock()).getColor();
   }

   public BedBlockEntity(BlockPos blockpos, BlockState blockstate, DyeColor dyecolor) {
      super(BlockEntityType.BED, blockpos, blockstate);
      this.color = dyecolor;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public DyeColor getColor() {
      return this.color;
   }

   public void setColor(DyeColor dyecolor) {
      this.color = dyecolor;
   }
}
