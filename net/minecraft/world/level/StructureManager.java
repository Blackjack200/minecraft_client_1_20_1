package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureManager {
   private final LevelAccessor level;
   private final WorldOptions worldOptions;
   private final StructureCheck structureCheck;

   public StructureManager(LevelAccessor levelaccessor, WorldOptions worldoptions, StructureCheck structurecheck) {
      this.level = levelaccessor;
      this.worldOptions = worldoptions;
      this.structureCheck = structurecheck;
   }

   public StructureManager forWorldGenRegion(WorldGenRegion worldgenregion) {
      if (worldgenregion.getLevel() != this.level) {
         throw new IllegalStateException("Using invalid structure manager (source level: " + worldgenregion.getLevel() + ", region: " + worldgenregion);
      } else {
         return new StructureManager(worldgenregion, this.worldOptions, this.structureCheck);
      }
   }

   public List<StructureStart> startsForStructure(ChunkPos chunkpos, Predicate<Structure> predicate) {
      Map<Structure, LongSet> map = this.level.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
      ImmutableList.Builder<StructureStart> immutablelist_builder = ImmutableList.builder();

      for(Map.Entry<Structure, LongSet> map_entry : map.entrySet()) {
         Structure structure = map_entry.getKey();
         if (predicate.test(structure)) {
            this.fillStartsForStructure(structure, map_entry.getValue(), immutablelist_builder::add);
         }
      }

      return immutablelist_builder.build();
   }

   public List<StructureStart> startsForStructure(SectionPos sectionpos, Structure structure) {
      LongSet longset = this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(structure);
      ImmutableList.Builder<StructureStart> immutablelist_builder = ImmutableList.builder();
      this.fillStartsForStructure(structure, longset, immutablelist_builder::add);
      return immutablelist_builder.build();
   }

   public void fillStartsForStructure(Structure structure, LongSet longset, Consumer<StructureStart> consumer) {
      for(long i : longset) {
         SectionPos sectionpos = SectionPos.of(new ChunkPos(i), this.level.getMinSection());
         StructureStart structurestart = this.getStartForStructure(sectionpos, structure, this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));
         if (structurestart != null && structurestart.isValid()) {
            consumer.accept(structurestart);
         }
      }

   }

   @Nullable
   public StructureStart getStartForStructure(SectionPos sectionpos, Structure structure, StructureAccess structureaccess) {
      return structureaccess.getStartForStructure(structure);
   }

   public void setStartForStructure(SectionPos sectionpos, Structure structure, StructureStart structurestart, StructureAccess structureaccess) {
      structureaccess.setStartForStructure(structure, structurestart);
   }

   public void addReferenceForStructure(SectionPos sectionpos, Structure structure, long i, StructureAccess structureaccess) {
      structureaccess.addReferenceForStructure(structure, i);
   }

   public boolean shouldGenerateStructures() {
      return this.worldOptions.generateStructures();
   }

   public StructureStart getStructureAt(BlockPos blockpos, Structure structure) {
      for(StructureStart structurestart : this.startsForStructure(SectionPos.of(blockpos), structure)) {
         if (structurestart.getBoundingBox().isInside(blockpos)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public StructureStart getStructureWithPieceAt(BlockPos blockpos, ResourceKey<Structure> resourcekey) {
      Structure structure = this.registryAccess().registryOrThrow(Registries.STRUCTURE).get(resourcekey);
      return structure == null ? StructureStart.INVALID_START : this.getStructureWithPieceAt(blockpos, structure);
   }

   public StructureStart getStructureWithPieceAt(BlockPos blockpos, TagKey<Structure> tagkey) {
      Registry<Structure> registry = this.registryAccess().registryOrThrow(Registries.STRUCTURE);

      for(StructureStart structurestart : this.startsForStructure(new ChunkPos(blockpos), (structure) -> registry.getHolder(registry.getId(structure)).map((holder_reference) -> holder_reference.is(tagkey)).orElse(false))) {
         if (this.structureHasPieceAt(blockpos, structurestart)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public StructureStart getStructureWithPieceAt(BlockPos blockpos, Structure structure) {
      for(StructureStart structurestart : this.startsForStructure(SectionPos.of(blockpos), structure)) {
         if (this.structureHasPieceAt(blockpos, structurestart)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public boolean structureHasPieceAt(BlockPos blockpos, StructureStart structurestart) {
      for(StructurePiece structurepiece : structurestart.getPieces()) {
         if (structurepiece.getBoundingBox().isInside(blockpos)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasAnyStructureAt(BlockPos blockpos) {
      SectionPos sectionpos = SectionPos.of(blockpos);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
   }

   public Map<Structure, LongSet> getAllStructuresAt(BlockPos blockpos) {
      SectionPos sectionpos = SectionPos.of(blockpos);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
   }

   public StructureCheckResult checkStructurePresence(ChunkPos chunkpos, Structure structure, boolean flag) {
      return this.structureCheck.checkStart(chunkpos, structure, flag);
   }

   public void addReference(StructureStart structurestart) {
      structurestart.addReference();
      this.structureCheck.incrementReference(structurestart.getChunkPos(), structurestart.getStructure());
   }

   public RegistryAccess registryAccess() {
      return this.level.registryAccess();
   }
}
