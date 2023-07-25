package net.minecraft.world.level.levelgen.structure;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.slf4j.Logger;

public final class StructureStart {
   public static final String INVALID_START_ID = "INVALID";
   public static final StructureStart INVALID_START = new StructureStart((Structure)null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Structure structure;
   private final PiecesContainer pieceContainer;
   private final ChunkPos chunkPos;
   private int references;
   @Nullable
   private volatile BoundingBox cachedBoundingBox;

   public StructureStart(Structure structure, ChunkPos chunkpos, int i, PiecesContainer piecescontainer) {
      this.structure = structure;
      this.chunkPos = chunkpos;
      this.references = i;
      this.pieceContainer = piecescontainer;
   }

   @Nullable
   public static StructureStart loadStaticStart(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag, long i) {
      String s = compoundtag.getString("id");
      if ("INVALID".equals(s)) {
         return INVALID_START;
      } else {
         Registry<Structure> registry = structurepieceserializationcontext.registryAccess().registryOrThrow(Registries.STRUCTURE);
         Structure structure = registry.get(new ResourceLocation(s));
         if (structure == null) {
            LOGGER.error("Unknown stucture id: {}", (Object)s);
            return null;
         } else {
            ChunkPos chunkpos = new ChunkPos(compoundtag.getInt("ChunkX"), compoundtag.getInt("ChunkZ"));
            int j = compoundtag.getInt("references");
            ListTag listtag = compoundtag.getList("Children", 10);

            try {
               PiecesContainer piecescontainer = PiecesContainer.load(listtag, structurepieceserializationcontext);
               if (structure instanceof OceanMonumentStructure) {
                  piecescontainer = OceanMonumentStructure.regeneratePiecesAfterLoad(chunkpos, i, piecescontainer);
               }

               return new StructureStart(structure, chunkpos, j, piecescontainer);
            } catch (Exception var11) {
               LOGGER.error("Failed Start with id {}", s, var11);
               return null;
            }
         }
      }
   }

   public BoundingBox getBoundingBox() {
      BoundingBox boundingbox = this.cachedBoundingBox;
      if (boundingbox == null) {
         boundingbox = this.structure.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
         this.cachedBoundingBox = boundingbox;
      }

      return boundingbox;
   }

   public void placeInChunk(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos) {
      List<StructurePiece> list = this.pieceContainer.pieces();
      if (!list.isEmpty()) {
         BoundingBox boundingbox1 = (list.get(0)).boundingBox;
         BlockPos blockpos = boundingbox1.getCenter();
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), boundingbox1.minY(), blockpos.getZ());

         for(StructurePiece structurepiece : list) {
            if (structurepiece.getBoundingBox().intersects(boundingbox)) {
               structurepiece.postProcess(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, blockpos1);
            }
         }

         this.structure.afterPlace(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, this.pieceContainer);
      }
   }

   public CompoundTag createTag(StructurePieceSerializationContext structurepieceserializationcontext, ChunkPos chunkpos) {
      CompoundTag compoundtag = new CompoundTag();
      if (this.isValid()) {
         compoundtag.putString("id", structurepieceserializationcontext.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this.structure).toString());
         compoundtag.putInt("ChunkX", chunkpos.x);
         compoundtag.putInt("ChunkZ", chunkpos.z);
         compoundtag.putInt("references", this.references);
         compoundtag.put("Children", this.pieceContainer.save(structurepieceserializationcontext));
         return compoundtag;
      } else {
         compoundtag.putString("id", "INVALID");
         return compoundtag;
      }
   }

   public boolean isValid() {
      return !this.pieceContainer.isEmpty();
   }

   public ChunkPos getChunkPos() {
      return this.chunkPos;
   }

   public boolean canBeReferenced() {
      return this.references < this.getMaxReferences();
   }

   public void addReference() {
      ++this.references;
   }

   public int getReferences() {
      return this.references;
   }

   protected int getMaxReferences() {
      return 1;
   }

   public Structure getStructure() {
      return this.structure;
   }

   public List<StructurePiece> getPieces() {
      return this.pieceContainer.pieces();
   }
}
