package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.slf4j.Logger;

public class ChunkSerializer {
   private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TAG_UPGRADE_DATA = "UpgradeData";
   private static final String BLOCK_TICKS_TAG = "block_ticks";
   private static final String FLUID_TICKS_TAG = "fluid_ticks";
   public static final String X_POS_TAG = "xPos";
   public static final String Z_POS_TAG = "zPos";
   public static final String HEIGHTMAPS_TAG = "Heightmaps";
   public static final String IS_LIGHT_ON_TAG = "isLightOn";
   public static final String SECTIONS_TAG = "sections";
   public static final String BLOCK_LIGHT_TAG = "BlockLight";
   public static final String SKY_LIGHT_TAG = "SkyLight";

   public static ProtoChunk read(ServerLevel serverlevel, PoiManager poimanager, ChunkPos chunkpos, CompoundTag compoundtag) {
      ChunkPos chunkpos1 = new ChunkPos(compoundtag.getInt("xPos"), compoundtag.getInt("zPos"));
      if (!Objects.equals(chunkpos, chunkpos1)) {
         LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkpos, chunkpos, chunkpos1);
      }

      UpgradeData upgradedata = compoundtag.contains("UpgradeData", 10) ? new UpgradeData(compoundtag.getCompound("UpgradeData"), serverlevel) : UpgradeData.EMPTY;
      boolean flag = compoundtag.getBoolean("isLightOn");
      ListTag listtag = compoundtag.getList("sections", 10);
      int i = serverlevel.getSectionsCount();
      LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
      boolean flag1 = serverlevel.dimensionType().hasSkyLight();
      ChunkSource chunksource = serverlevel.getChunkSource();
      LevelLightEngine levellightengine = chunksource.getLightEngine();
      Registry<Biome> registry = serverlevel.registryAccess().registryOrThrow(Registries.BIOME);
      Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);
      boolean flag2 = false;

      for(int j = 0; j < listtag.size(); ++j) {
         CompoundTag compoundtag1 = listtag.getCompound(j);
         int k = compoundtag1.getByte("Y");
         int l = serverlevel.getSectionIndexFromSectionY(k);
         if (l >= 0 && l < alevelchunksection.length) {
            PalettedContainer<BlockState> palettedcontainer;
            if (compoundtag1.contains("block_states", 10)) {
               palettedcontainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundtag1.getCompound("block_states")).promotePartial((s7) -> logErrors(chunkpos, k, s7)).getOrThrow(false, LOGGER::error);
            } else {
               palettedcontainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            }

            PalettedContainerRO<Holder<Biome>> palettedcontainerro;
            if (compoundtag1.contains("biomes", 10)) {
               palettedcontainerro = codec.parse(NbtOps.INSTANCE, compoundtag1.getCompound("biomes")).promotePartial((s6) -> logErrors(chunkpos, k, s6)).getOrThrow(false, LOGGER::error);
            } else {
               palettedcontainerro = new PalettedContainer<>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
            }

            LevelChunkSection levelchunksection = new LevelChunkSection(palettedcontainer, palettedcontainerro);
            alevelchunksection[l] = levelchunksection;
            SectionPos sectionpos = SectionPos.of(chunkpos, k);
            poimanager.checkConsistencyWithBlocks(sectionpos, levelchunksection);
         }

         boolean flag3 = compoundtag1.contains("BlockLight", 7);
         boolean flag4 = flag1 && compoundtag1.contains("SkyLight", 7);
         if (flag3 || flag4) {
            if (!flag2) {
               levellightengine.retainData(chunkpos, true);
               flag2 = true;
            }

            if (flag3) {
               levellightengine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkpos, k), new DataLayer(compoundtag1.getByteArray("BlockLight")));
            }

            if (flag4) {
               levellightengine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkpos, k), new DataLayer(compoundtag1.getByteArray("SkyLight")));
            }
         }
      }

      long i1 = compoundtag.getLong("InhabitedTime");
      ChunkStatus.ChunkType chunkstatus_chunktype = getChunkTypeFromTag(compoundtag);
      BlendingData blendingdata;
      if (compoundtag.contains("blending_data", 10)) {
         blendingdata = BlendingData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.getCompound("blending_data"))).resultOrPartial(LOGGER::error).orElse((BlendingData)null);
      } else {
         blendingdata = null;
      }

      ChunkAccess chunkaccess;
      if (chunkstatus_chunktype == ChunkStatus.ChunkType.LEVELCHUNK) {
         LevelChunkTicks<Block> levelchunkticks = LevelChunkTicks.load(compoundtag.getList("block_ticks", 10), (s5) -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(s5)), chunkpos);
         LevelChunkTicks<Fluid> levelchunkticks1 = LevelChunkTicks.load(compoundtag.getList("fluid_ticks", 10), (s4) -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(s4)), chunkpos);
         chunkaccess = new LevelChunk(serverlevel.getLevel(), chunkpos, upgradedata, levelchunkticks, levelchunkticks1, i1, alevelchunksection, postLoadChunk(serverlevel, compoundtag), blendingdata);
      } else {
         ProtoChunkTicks<Block> protochunkticks = ProtoChunkTicks.load(compoundtag.getList("block_ticks", 10), (s3) -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(s3)), chunkpos);
         ProtoChunkTicks<Fluid> protochunkticks1 = ProtoChunkTicks.load(compoundtag.getList("fluid_ticks", 10), (s2) -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(s2)), chunkpos);
         ProtoChunk protochunk = new ProtoChunk(chunkpos, upgradedata, alevelchunksection, protochunkticks, protochunkticks1, serverlevel, registry, blendingdata);
         chunkaccess = protochunk;
         protochunk.setInhabitedTime(i1);
         if (compoundtag.contains("below_zero_retrogen", 10)) {
            BelowZeroRetrogen.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.getCompound("below_zero_retrogen"))).resultOrPartial(LOGGER::error).ifPresent(protochunk::setBelowZeroRetrogen);
         }

         ChunkStatus chunkstatus = ChunkStatus.byName(compoundtag.getString("Status"));
         protochunk.setStatus(chunkstatus);
         if (chunkstatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
            protochunk.setLightEngine(levellightengine);
         }
      }

      chunkaccess.setLightCorrect(flag);
      CompoundTag compoundtag2 = compoundtag.getCompound("Heightmaps");
      EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);

      for(Heightmap.Types heightmap_types : chunkaccess.getStatus().heightmapsAfter()) {
         String s = heightmap_types.getSerializationKey();
         if (compoundtag2.contains(s, 12)) {
            chunkaccess.setHeightmap(heightmap_types, compoundtag2.getLongArray(s));
         } else {
            enumset.add(heightmap_types);
         }
      }

      Heightmap.primeHeightmaps(chunkaccess, enumset);
      CompoundTag compoundtag3 = compoundtag.getCompound("structures");
      chunkaccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverlevel), compoundtag3, serverlevel.getSeed()));
      chunkaccess.setAllReferences(unpackStructureReferences(serverlevel.registryAccess(), chunkpos, compoundtag3));
      if (compoundtag.getBoolean("shouldSave")) {
         chunkaccess.setUnsaved(true);
      }

      ListTag listtag1 = compoundtag.getList("PostProcessing", 9);

      for(int j1 = 0; j1 < listtag1.size(); ++j1) {
         ListTag listtag2 = listtag1.getList(j1);

         for(int k1 = 0; k1 < listtag2.size(); ++k1) {
            chunkaccess.addPackedPostProcess(listtag2.getShort(k1), j1);
         }
      }

      if (chunkstatus_chunktype == ChunkStatus.ChunkType.LEVELCHUNK) {
         return new ImposterProtoChunk((LevelChunk)chunkaccess, false);
      } else {
         ProtoChunk protochunk1 = (ProtoChunk)chunkaccess;
         ListTag listtag3 = compoundtag.getList("entities", 10);

         for(int l1 = 0; l1 < listtag3.size(); ++l1) {
            protochunk1.addEntity(listtag3.getCompound(l1));
         }

         ListTag listtag4 = compoundtag.getList("block_entities", 10);

         for(int i2 = 0; i2 < listtag4.size(); ++i2) {
            CompoundTag compoundtag4 = listtag4.getCompound(i2);
            chunkaccess.setBlockEntityNbt(compoundtag4);
         }

         CompoundTag compoundtag5 = compoundtag.getCompound("CarvingMasks");

         for(String s1 : compoundtag5.getAllKeys()) {
            GenerationStep.Carving generationstep_carving = GenerationStep.Carving.valueOf(s1);
            protochunk1.setCarvingMask(generationstep_carving, new CarvingMask(compoundtag5.getLongArray(s1), chunkaccess.getMinBuildHeight()));
         }

         return protochunk1;
      }
   }

   private static void logErrors(ChunkPos chunkpos, int i, String s) {
      LOGGER.error("Recoverable errors when loading section [" + chunkpos.x + ", " + i + ", " + chunkpos.z + "]: " + s);
   }

   private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> registry) {
      return PalettedContainer.codecRO(registry.asHolderIdMap(), registry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, registry.getHolderOrThrow(Biomes.PLAINS));
   }

   public static CompoundTag write(ServerLevel serverlevel, ChunkAccess chunkaccess) {
      ChunkPos chunkpos = chunkaccess.getPos();
      CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
      compoundtag.putInt("xPos", chunkpos.x);
      compoundtag.putInt("yPos", chunkaccess.getMinSection());
      compoundtag.putInt("zPos", chunkpos.z);
      compoundtag.putLong("LastUpdate", serverlevel.getGameTime());
      compoundtag.putLong("InhabitedTime", chunkaccess.getInhabitedTime());
      compoundtag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(chunkaccess.getStatus()).toString());
      BlendingData blendingdata = chunkaccess.getBlendingData();
      if (blendingdata != null) {
         BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, blendingdata).resultOrPartial(LOGGER::error).ifPresent((tag1) -> compoundtag.put("blending_data", tag1));
      }

      BelowZeroRetrogen belowzeroretrogen = chunkaccess.getBelowZeroRetrogen();
      if (belowzeroretrogen != null) {
         BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, belowzeroretrogen).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("below_zero_retrogen", tag));
      }

      UpgradeData upgradedata = chunkaccess.getUpgradeData();
      if (!upgradedata.isEmpty()) {
         compoundtag.put("UpgradeData", upgradedata.write());
      }

      LevelChunkSection[] alevelchunksection = chunkaccess.getSections();
      ListTag listtag = new ListTag();
      LevelLightEngine levellightengine = serverlevel.getChunkSource().getLightEngine();
      Registry<Biome> registry = serverlevel.registryAccess().registryOrThrow(Registries.BIOME);
      Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);
      boolean flag = chunkaccess.isLightCorrect();

      for(int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
         int j = chunkaccess.getSectionIndexFromSectionY(i);
         boolean flag1 = j >= 0 && j < alevelchunksection.length;
         DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
         DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
         if (flag1 || datalayer != null || datalayer1 != null) {
            CompoundTag compoundtag1 = new CompoundTag();
            if (flag1) {
               LevelChunkSection levelchunksection = alevelchunksection[j];
               compoundtag1.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow(false, LOGGER::error));
               compoundtag1.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelchunksection.getBiomes()).getOrThrow(false, LOGGER::error));
            }

            if (datalayer != null && !datalayer.isEmpty()) {
               compoundtag1.putByteArray("BlockLight", datalayer.getData());
            }

            if (datalayer1 != null && !datalayer1.isEmpty()) {
               compoundtag1.putByteArray("SkyLight", datalayer1.getData());
            }

            if (!compoundtag1.isEmpty()) {
               compoundtag1.putByte("Y", (byte)i);
               listtag.add(compoundtag1);
            }
         }
      }

      compoundtag.put("sections", listtag);
      if (flag) {
         compoundtag.putBoolean("isLightOn", true);
      }

      ListTag listtag1 = new ListTag();

      for(BlockPos blockpos : chunkaccess.getBlockEntitiesPos()) {
         CompoundTag compoundtag2 = chunkaccess.getBlockEntityNbtForSaving(blockpos);
         if (compoundtag2 != null) {
            listtag1.add(compoundtag2);
         }
      }

      compoundtag.put("block_entities", listtag1);
      if (chunkaccess.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
         ProtoChunk protochunk = (ProtoChunk)chunkaccess;
         ListTag listtag2 = new ListTag();
         listtag2.addAll(protochunk.getEntities());
         compoundtag.put("entities", listtag2);
         CompoundTag compoundtag3 = new CompoundTag();

         for(GenerationStep.Carving generationstep_carving : GenerationStep.Carving.values()) {
            CarvingMask carvingmask = protochunk.getCarvingMask(generationstep_carving);
            if (carvingmask != null) {
               compoundtag3.putLongArray(generationstep_carving.toString(), carvingmask.toArray());
            }
         }

         compoundtag.put("CarvingMasks", compoundtag3);
      }

      saveTicks(serverlevel, compoundtag, chunkaccess.getTicksForSerialization());
      compoundtag.put("PostProcessing", packOffsets(chunkaccess.getPostProcessing()));
      CompoundTag compoundtag4 = new CompoundTag();

      for(Map.Entry<Heightmap.Types, Heightmap> map_entry : chunkaccess.getHeightmaps()) {
         if (chunkaccess.getStatus().heightmapsAfter().contains(map_entry.getKey())) {
            compoundtag4.put(map_entry.getKey().getSerializationKey(), new LongArrayTag(map_entry.getValue().getRawData()));
         }
      }

      compoundtag.put("Heightmaps", compoundtag4);
      compoundtag.put("structures", packStructureData(StructurePieceSerializationContext.fromLevel(serverlevel), chunkpos, chunkaccess.getAllStarts(), chunkaccess.getAllReferences()));
      return compoundtag;
   }

   private static void saveTicks(ServerLevel serverlevel, CompoundTag compoundtag, ChunkAccess.TicksToSave chunkaccess_tickstosave) {
      long i = serverlevel.getLevelData().getGameTime();
      compoundtag.put("block_ticks", chunkaccess_tickstosave.blocks().save(i, (block) -> BuiltInRegistries.BLOCK.getKey(block).toString()));
      compoundtag.put("fluid_ticks", chunkaccess_tickstosave.fluids().save(i, (fluid) -> BuiltInRegistries.FLUID.getKey(fluid).toString()));
   }

   public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundtag) {
      return compoundtag != null ? ChunkStatus.byName(compoundtag.getString("Status")).getChunkType() : ChunkStatus.ChunkType.PROTOCHUNK;
   }

   @Nullable
   private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverlevel, CompoundTag compoundtag) {
      ListTag listtag = getListOfCompoundsOrNull(compoundtag, "entities");
      ListTag listtag1 = getListOfCompoundsOrNull(compoundtag, "block_entities");
      return listtag == null && listtag1 == null ? null : (levelchunk) -> {
         if (listtag != null) {
            serverlevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listtag, serverlevel));
         }

         if (listtag1 != null) {
            for(int i = 0; i < listtag1.size(); ++i) {
               CompoundTag compoundtag1 = listtag1.getCompound(i);
               boolean flag = compoundtag1.getBoolean("keepPacked");
               if (flag) {
                  levelchunk.setBlockEntityNbt(compoundtag1);
               } else {
                  BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag1);
                  BlockEntity blockentity = BlockEntity.loadStatic(blockpos, levelchunk.getBlockState(blockpos), compoundtag1);
                  if (blockentity != null) {
                     levelchunk.setBlockEntity(blockentity);
                  }
               }
            }
         }

      };
   }

   @Nullable
   private static ListTag getListOfCompoundsOrNull(CompoundTag compoundtag, String s) {
      ListTag listtag = compoundtag.getList(s, 10);
      return listtag.isEmpty() ? null : listtag;
   }

   private static CompoundTag packStructureData(StructurePieceSerializationContext structurepieceserializationcontext, ChunkPos chunkpos, Map<Structure, StructureStart> map, Map<Structure, LongSet> map1) {
      CompoundTag compoundtag = new CompoundTag();
      CompoundTag compoundtag1 = new CompoundTag();
      Registry<Structure> registry = structurepieceserializationcontext.registryAccess().registryOrThrow(Registries.STRUCTURE);

      for(Map.Entry<Structure, StructureStart> map_entry : map.entrySet()) {
         ResourceLocation resourcelocation = registry.getKey(map_entry.getKey());
         compoundtag1.put(resourcelocation.toString(), map_entry.getValue().createTag(structurepieceserializationcontext, chunkpos));
      }

      compoundtag.put("starts", compoundtag1);
      CompoundTag compoundtag2 = new CompoundTag();

      for(Map.Entry<Structure, LongSet> map_entry1 : map1.entrySet()) {
         if (!map_entry1.getValue().isEmpty()) {
            ResourceLocation resourcelocation1 = registry.getKey(map_entry1.getKey());
            compoundtag2.put(resourcelocation1.toString(), new LongArrayTag(map_entry1.getValue()));
         }
      }

      compoundtag.put("References", compoundtag2);
      return compoundtag;
   }

   private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag, long i) {
      Map<Structure, StructureStart> map = Maps.newHashMap();
      Registry<Structure> registry = structurepieceserializationcontext.registryAccess().registryOrThrow(Registries.STRUCTURE);
      CompoundTag compoundtag1 = compoundtag.getCompound("starts");

      for(String s : compoundtag1.getAllKeys()) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         Structure structure = registry.get(resourcelocation);
         if (structure == null) {
            LOGGER.error("Unknown structure start: {}", (Object)resourcelocation);
         } else {
            StructureStart structurestart = StructureStart.loadStaticStart(structurepieceserializationcontext, compoundtag1.getCompound(s), i);
            if (structurestart != null) {
               map.put(structure, structurestart);
            }
         }
      }

      return map;
   }

   private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryaccess, ChunkPos chunkpos, CompoundTag compoundtag) {
      Map<Structure, LongSet> map = Maps.newHashMap();
      Registry<Structure> registry = registryaccess.registryOrThrow(Registries.STRUCTURE);
      CompoundTag compoundtag1 = compoundtag.getCompound("References");

      for(String s : compoundtag1.getAllKeys()) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         Structure structure = registry.get(resourcelocation);
         if (structure == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", resourcelocation, chunkpos);
         } else {
            long[] along = compoundtag1.getLongArray(s);
            if (along.length != 0) {
               map.put(structure, new LongOpenHashSet(Arrays.stream(along).filter((i) -> {
                  ChunkPos chunkpos2 = new ChunkPos(i);
                  if (chunkpos2.getChessboardDistance(chunkpos) > 8) {
                     LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", resourcelocation, chunkpos2, chunkpos);
                     return false;
                  } else {
                     return true;
                  }
               }).toArray()));
            }
         }
      }

      return map;
   }

   public static ListTag packOffsets(ShortList[] ashortlist) {
      ListTag listtag = new ListTag();

      for(ShortList shortlist : ashortlist) {
         ListTag listtag1 = new ListTag();
         if (shortlist != null) {
            for(Short oshort : shortlist) {
               listtag1.add(ShortTag.valueOf(oshort));
            }
         }

         listtag.add(listtag1);
      }

      return listtag;
   }
}
