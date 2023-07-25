package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class NetherFossilStructure extends Structure {
   public static final Codec<NetherFossilStructure> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance), HeightProvider.CODEC.fieldOf("height").forGetter((netherfossilstructure) -> netherfossilstructure.height)).apply(recordcodecbuilder_instance, NetherFossilStructure::new));
   public final HeightProvider height;

   public NetherFossilStructure(Structure.StructureSettings structure_structuresettings, HeightProvider heightprovider) {
      super(structure_structuresettings);
      this.height = heightprovider;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      WorldgenRandom worldgenrandom = structure_generationcontext.random();
      int i = structure_generationcontext.chunkPos().getMinBlockX() + worldgenrandom.nextInt(16);
      int j = structure_generationcontext.chunkPos().getMinBlockZ() + worldgenrandom.nextInt(16);
      int k = structure_generationcontext.chunkGenerator().getSeaLevel();
      WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(structure_generationcontext.chunkGenerator(), structure_generationcontext.heightAccessor());
      int l = this.height.sample(worldgenrandom, worldgenerationcontext);
      NoiseColumn noisecolumn = structure_generationcontext.chunkGenerator().getBaseColumn(i, j, structure_generationcontext.heightAccessor(), structure_generationcontext.randomState());
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i, l, j);

      while(l > k) {
         BlockState blockstate = noisecolumn.getBlock(l);
         --l;
         BlockState blockstate1 = noisecolumn.getBlock(l);
         if (blockstate.isAir() && (blockstate1.is(Blocks.SOUL_SAND) || blockstate1.isFaceSturdy(EmptyBlockGetter.INSTANCE, blockpos_mutableblockpos.setY(l), Direction.UP))) {
            break;
         }
      }

      if (l <= k) {
         return Optional.empty();
      } else {
         BlockPos blockpos = new BlockPos(i, l, j);
         return Optional.of(new Structure.GenerationStub(blockpos, (structurepiecesbuilder) -> NetherFossilPieces.addPieces(structure_generationcontext.structureTemplateManager(), structurepiecesbuilder, worldgenrandom, blockpos)));
      }
   }

   public StructureType<?> type() {
      return StructureType.NETHER_FOSSIL;
   }
}
