package net.minecraft.world.damagesource;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record FallLocation(String id) {
   public static final FallLocation GENERIC = new FallLocation("generic");
   public static final FallLocation LADDER = new FallLocation("ladder");
   public static final FallLocation VINES = new FallLocation("vines");
   public static final FallLocation WEEPING_VINES = new FallLocation("weeping_vines");
   public static final FallLocation TWISTING_VINES = new FallLocation("twisting_vines");
   public static final FallLocation SCAFFOLDING = new FallLocation("scaffolding");
   public static final FallLocation OTHER_CLIMBABLE = new FallLocation("other_climbable");
   public static final FallLocation WATER = new FallLocation("water");

   public static FallLocation blockToFallLocation(BlockState blockstate) {
      if (!blockstate.is(Blocks.LADDER) && !blockstate.is(BlockTags.TRAPDOORS)) {
         if (blockstate.is(Blocks.VINE)) {
            return VINES;
         } else if (!blockstate.is(Blocks.WEEPING_VINES) && !blockstate.is(Blocks.WEEPING_VINES_PLANT)) {
            if (!blockstate.is(Blocks.TWISTING_VINES) && !blockstate.is(Blocks.TWISTING_VINES_PLANT)) {
               return blockstate.is(Blocks.SCAFFOLDING) ? SCAFFOLDING : OTHER_CLIMBABLE;
            } else {
               return TWISTING_VINES;
            }
         } else {
            return WEEPING_VINES;
         }
      } else {
         return LADDER;
      }
   }

   @Nullable
   public static FallLocation getCurrentFallLocation(LivingEntity livingentity) {
      Optional<BlockPos> optional = livingentity.getLastClimbablePos();
      if (optional.isPresent()) {
         BlockState blockstate = livingentity.level().getBlockState(optional.get());
         return blockToFallLocation(blockstate);
      } else {
         return livingentity.isInWater() ? WATER : null;
      }
   }

   public String languageKey() {
      return "death.fell.accident." + this.id;
   }
}
