package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockParticleOption implements ParticleOptions {
   public static final ParticleOptions.Deserializer<BlockParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<BlockParticleOption>() {
      public BlockParticleOption fromCommand(ParticleType<BlockParticleOption> particletype, StringReader stringreader) throws CommandSyntaxException {
         stringreader.expect(' ');
         return new BlockParticleOption(particletype, BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), stringreader, false).blockState());
      }

      public BlockParticleOption fromNetwork(ParticleType<BlockParticleOption> particletype, FriendlyByteBuf friendlybytebuf) {
         return new BlockParticleOption(particletype, friendlybytebuf.readById(Block.BLOCK_STATE_REGISTRY));
      }
   };
   private final ParticleType<BlockParticleOption> type;
   private final BlockState state;

   public static Codec<BlockParticleOption> codec(ParticleType<BlockParticleOption> particletype) {
      return BlockState.CODEC.xmap((blockstate) -> new BlockParticleOption(particletype, blockstate), (blockparticleoption) -> blockparticleoption.state);
   }

   public BlockParticleOption(ParticleType<BlockParticleOption> particletype, BlockState blockstate) {
      this.type = particletype;
      this.state = blockstate;
   }

   public void writeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeId(Block.BLOCK_STATE_REGISTRY, this.state);
   }

   public String writeToString() {
      return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.state);
   }

   public ParticleType<BlockParticleOption> getType() {
      return this.type;
   }

   public BlockState getState() {
      return this.state;
   }
}
