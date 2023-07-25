package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class MineshaftStructure extends Structure {
   public static final Codec<MineshaftStructure> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance), MineshaftStructure.Type.CODEC.fieldOf("mineshaft_type").forGetter((mineshaftstructure) -> mineshaftstructure.type)).apply(recordcodecbuilder_instance, MineshaftStructure::new));
   private final MineshaftStructure.Type type;

   public MineshaftStructure(Structure.StructureSettings structure_structuresettings, MineshaftStructure.Type mineshaftstructure_type) {
      super(structure_structuresettings);
      this.type = mineshaftstructure_type;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      structure_generationcontext.random().nextDouble();
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMiddleBlockX(), 50, chunkpos.getMinBlockZ());
      StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
      int i = this.generatePiecesAndAdjust(structurepiecesbuilder, structure_generationcontext);
      return Optional.of(new Structure.GenerationStub(blockpos.offset(0, i, 0), Either.right(structurepiecesbuilder)));
   }

   private int generatePiecesAndAdjust(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      WorldgenRandom worldgenrandom = structure_generationcontext.random();
      ChunkGenerator chunkgenerator = structure_generationcontext.chunkGenerator();
      MineshaftPieces.MineShaftRoom mineshaftpieces_mineshaftroom = new MineshaftPieces.MineShaftRoom(0, worldgenrandom, chunkpos.getBlockX(2), chunkpos.getBlockZ(2), this.type);
      structurepiecesbuilder.addPiece(mineshaftpieces_mineshaftroom);
      mineshaftpieces_mineshaftroom.addChildren(mineshaftpieces_mineshaftroom, structurepiecesbuilder, worldgenrandom);
      int i = chunkgenerator.getSeaLevel();
      if (this.type == MineshaftStructure.Type.MESA) {
         BlockPos blockpos = structurepiecesbuilder.getBoundingBox().getCenter();
         int j = chunkgenerator.getBaseHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, structure_generationcontext.heightAccessor(), structure_generationcontext.randomState());
         int k = j <= i ? i : Mth.randomBetweenInclusive(worldgenrandom, i, j);
         int l = k - blockpos.getY();
         structurepiecesbuilder.offsetPiecesVertically(l);
         return l;
      } else {
         return structurepiecesbuilder.moveBelowSeaLevel(i, chunkgenerator.getMinY(), worldgenrandom, 10);
      }
   }

   public StructureType<?> type() {
      return StructureType.MINESHAFT;
   }

   public static enum Type implements StringRepresentable {
      NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
      MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

      public static final Codec<MineshaftStructure.Type> CODEC = StringRepresentable.fromEnum(MineshaftStructure.Type::values);
      private static final IntFunction<MineshaftStructure.Type> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      private final String name;
      private final BlockState woodState;
      private final BlockState planksState;
      private final BlockState fenceState;

      private Type(String s, Block block, Block block1, Block block2) {
         this.name = s;
         this.woodState = block.defaultBlockState();
         this.planksState = block1.defaultBlockState();
         this.fenceState = block2.defaultBlockState();
      }

      public String getName() {
         return this.name;
      }

      public static MineshaftStructure.Type byId(int i) {
         return BY_ID.apply(i);
      }

      public BlockState getWoodState() {
         return this.woodState;
      }

      public BlockState getPlanksState() {
         return this.planksState;
      }

      public BlockState getFenceState() {
         return this.fenceState;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
