package net.minecraft.commands.arguments.blocks;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockInput implements Predicate<BlockInWorld> {
   private final BlockState state;
   private final Set<Property<?>> properties;
   @Nullable
   private final CompoundTag tag;

   public BlockInput(BlockState blockstate, Set<Property<?>> set, @Nullable CompoundTag compoundtag) {
      this.state = blockstate;
      this.properties = set;
      this.tag = compoundtag;
   }

   public BlockState getState() {
      return this.state;
   }

   public Set<Property<?>> getDefinedProperties() {
      return this.properties;
   }

   public boolean test(BlockInWorld blockinworld) {
      BlockState blockstate = blockinworld.getState();
      if (!blockstate.is(this.state.getBlock())) {
         return false;
      } else {
         for(Property<?> property : this.properties) {
            if (blockstate.getValue(property) != this.state.getValue(property)) {
               return false;
            }
         }

         if (this.tag == null) {
            return true;
         } else {
            BlockEntity blockentity = blockinworld.getEntity();
            return blockentity != null && NbtUtils.compareNbt(this.tag, blockentity.saveWithFullMetadata(), true);
         }
      }
   }

   public boolean test(ServerLevel serverlevel, BlockPos blockpos) {
      return this.test(new BlockInWorld(serverlevel, blockpos, false));
   }

   public boolean place(ServerLevel serverlevel, BlockPos blockpos, int i) {
      BlockState blockstate = Block.updateFromNeighbourShapes(this.state, serverlevel, blockpos);
      if (blockstate.isAir()) {
         blockstate = this.state;
      }

      if (!serverlevel.setBlock(blockpos, blockstate, i)) {
         return false;
      } else {
         if (this.tag != null) {
            BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
            if (blockentity != null) {
               blockentity.load(this.tag);
            }
         }

         return true;
      }
   }
}
