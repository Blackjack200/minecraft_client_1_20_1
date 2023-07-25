package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressStructure extends Structure {
   public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3), new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4), new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4));
   public static final Codec<NetherFortressStructure> CODEC = simpleCodec(NetherFortressStructure::new);

   public NetherFortressStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 64, chunkpos.getMinBlockZ());
      return Optional.of(new Structure.GenerationStub(blockpos, (structurepiecesbuilder) -> generatePieces(structurepiecesbuilder, structure_generationcontext)));
   }

   private static void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      NetherFortressPieces.StartPiece netherfortresspieces_startpiece = new NetherFortressPieces.StartPiece(structure_generationcontext.random(), structure_generationcontext.chunkPos().getBlockX(2), structure_generationcontext.chunkPos().getBlockZ(2));
      structurepiecesbuilder.addPiece(netherfortresspieces_startpiece);
      netherfortresspieces_startpiece.addChildren(netherfortresspieces_startpiece, structurepiecesbuilder, structure_generationcontext.random());
      List<StructurePiece> list = netherfortresspieces_startpiece.pendingChildren;

      while(!list.isEmpty()) {
         int i = structure_generationcontext.random().nextInt(list.size());
         StructurePiece structurepiece = list.remove(i);
         structurepiece.addChildren(netherfortresspieces_startpiece, structurepiecesbuilder, structure_generationcontext.random());
      }

      structurepiecesbuilder.moveInsideHeights(structure_generationcontext.random(), 48, 70);
   }

   public StructureType<?> type() {
      return StructureType.FORTRESS;
   }
}
