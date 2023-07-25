package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlockPositionSource implements PositionSource {
   public static final Codec<BlockPositionSource> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPos.CODEC.fieldOf("pos").forGetter((blockpositionsource) -> blockpositionsource.pos)).apply(recordcodecbuilder_instance, BlockPositionSource::new));
   final BlockPos pos;

   public BlockPositionSource(BlockPos blockpos) {
      this.pos = blockpos;
   }

   public Optional<Vec3> getPosition(Level level) {
      return Optional.of(Vec3.atCenterOf(this.pos));
   }

   public PositionSourceType<?> getType() {
      return PositionSourceType.BLOCK;
   }

   public static class Type implements PositionSourceType<BlockPositionSource> {
      public BlockPositionSource read(FriendlyByteBuf friendlybytebuf) {
         return new BlockPositionSource(friendlybytebuf.readBlockPos());
      }

      public void write(FriendlyByteBuf friendlybytebuf, BlockPositionSource blockpositionsource) {
         friendlybytebuf.writeBlockPos(blockpositionsource.pos);
      }

      public Codec<BlockPositionSource> codec() {
         return BlockPositionSource.CODEC;
      }
   }
}
