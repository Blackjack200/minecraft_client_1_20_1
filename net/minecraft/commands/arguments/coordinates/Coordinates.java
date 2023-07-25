package net.minecraft.commands.arguments.coordinates;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface Coordinates {
   Vec3 getPosition(CommandSourceStack commandsourcestack);

   Vec2 getRotation(CommandSourceStack commandsourcestack);

   default BlockPos getBlockPos(CommandSourceStack commandsourcestack) {
      return BlockPos.containing(this.getPosition(commandsourcestack));
   }

   boolean isXRelative();

   boolean isYRelative();

   boolean isZRelative();
}
