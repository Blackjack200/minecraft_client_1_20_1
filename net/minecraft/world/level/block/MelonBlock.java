package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockBehaviour;

public class MelonBlock extends StemGrownBlock {
   protected MelonBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public StemBlock getStem() {
      return (StemBlock)Blocks.MELON_STEM;
   }

   public AttachedStemBlock getAttachedStem() {
      return (AttachedStemBlock)Blocks.ATTACHED_MELON_STEM;
   }
}
