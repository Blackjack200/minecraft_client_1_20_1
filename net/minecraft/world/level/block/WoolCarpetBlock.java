package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoolCarpetBlock extends CarpetBlock {
   private final DyeColor color;

   protected WoolCarpetBlock(DyeColor dyecolor, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.color = dyecolor;
   }

   public DyeColor getColor() {
      return this.color;
   }
}
