package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface DispensibleContainerItem {
   default void checkExtraContent(@Nullable Player player, Level level, ItemStack itemstack, BlockPos blockpos) {
   }

   boolean emptyContents(@Nullable Player player, Level level, BlockPos blockpos, @Nullable BlockHitResult blockhitresult);
}
