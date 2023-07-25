package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class PoolElementStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final StructurePoolElement element;
   protected BlockPos position;
   private final int groundLevelDelta;
   protected final Rotation rotation;
   private final List<JigsawJunction> junctions = Lists.newArrayList();
   private final StructureTemplateManager structureTemplateManager;

   public PoolElementStructurePiece(StructureTemplateManager structuretemplatemanager, StructurePoolElement structurepoolelement, BlockPos blockpos, int i, Rotation rotation, BoundingBox boundingbox) {
      super(StructurePieceType.JIGSAW, 0, boundingbox);
      this.structureTemplateManager = structuretemplatemanager;
      this.element = structurepoolelement;
      this.position = blockpos;
      this.groundLevelDelta = i;
      this.rotation = rotation;
   }

   public PoolElementStructurePiece(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      super(StructurePieceType.JIGSAW, compoundtag);
      this.structureTemplateManager = structurepieceserializationcontext.structureTemplateManager();
      this.position = new BlockPos(compoundtag.getInt("PosX"), compoundtag.getInt("PosY"), compoundtag.getInt("PosZ"));
      this.groundLevelDelta = compoundtag.getInt("ground_level_delta");
      DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, structurepieceserializationcontext.registryAccess());
      this.element = StructurePoolElement.CODEC.parse(dynamicops, compoundtag.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
      this.rotation = Rotation.valueOf(compoundtag.getString("rotation"));
      this.boundingBox = this.element.getBoundingBox(this.structureTemplateManager, this.position, this.rotation);
      ListTag listtag = compoundtag.getList("junctions", 10);
      this.junctions.clear();
      listtag.forEach((tag) -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(dynamicops, tag))));
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      compoundtag.putInt("PosX", this.position.getX());
      compoundtag.putInt("PosY", this.position.getY());
      compoundtag.putInt("PosZ", this.position.getZ());
      compoundtag.putInt("ground_level_delta", this.groundLevelDelta);
      DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, structurepieceserializationcontext.registryAccess());
      StructurePoolElement.CODEC.encodeStart(dynamicops, this.element).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("pool_element", tag));
      compoundtag.putString("rotation", this.rotation.name());
      ListTag listtag = new ListTag();

      for(JigsawJunction jigsawjunction : this.junctions) {
         listtag.add(jigsawjunction.serialize(dynamicops).getValue());
      }

      compoundtag.put("junctions", listtag);
   }

   public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
      this.place(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, blockpos, false);
   }

   public void place(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, BlockPos blockpos, boolean flag) {
      this.element.place(this.structureTemplateManager, worldgenlevel, structuremanager, chunkgenerator, this.position, blockpos, this.rotation, boundingbox, randomsource, flag);
   }

   public void move(int i, int j, int k) {
      super.move(i, j, k);
      this.position = this.position.offset(i, j, k);
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String toString() {
      return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
   }

   public StructurePoolElement getElement() {
      return this.element;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public int getGroundLevelDelta() {
      return this.groundLevelDelta;
   }

   public void addJunction(JigsawJunction jigsawjunction) {
      this.junctions.add(jigsawjunction);
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}
