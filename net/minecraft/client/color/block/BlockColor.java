package net.minecraft.client.color.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockColor {
   int getColor(BlockState blockstate, @Nullable BlockAndTintGetter blockandtintgetter, @Nullable BlockPos blockpos, int i);
}
