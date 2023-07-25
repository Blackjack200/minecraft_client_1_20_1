package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureUtils {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
   public static String testStructuresDir = "gameteststructures";
   private static final int HOW_MANY_CHUNKS_TO_LOAD_IN_EACH_DIRECTION_OF_STRUCTURE = 4;

   public static Rotation getRotationForRotationSteps(int i) {
      switch (i) {
         case 0:
            return Rotation.NONE;
         case 1:
            return Rotation.CLOCKWISE_90;
         case 2:
            return Rotation.CLOCKWISE_180;
         case 3:
            return Rotation.COUNTERCLOCKWISE_90;
         default:
            throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + i);
      }
   }

   public static int getRotationStepsForRotation(Rotation rotation) {
      switch (rotation) {
         case NONE:
            return 0;
         case CLOCKWISE_90:
            return 1;
         case CLOCKWISE_180:
            return 2;
         case COUNTERCLOCKWISE_90:
            return 3;
         default:
            throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + rotation);
      }
   }

   public static void main(String[] astring) throws IOException {
      Bootstrap.bootStrap();
      Files.walk(Paths.get(testStructuresDir)).filter((path1) -> path1.toString().endsWith(".snbt")).forEach((path) -> {
         try {
            String s = Files.readString(path);
            CompoundTag compoundtag = NbtUtils.snbtToStructure(s);
            CompoundTag compoundtag1 = StructureUpdater.update(path.toString(), compoundtag);
            NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, path, NbtUtils.structureToSnbt(compoundtag1));
         } catch (IOException | CommandSyntaxException var4) {
            LOGGER.error("Something went wrong upgrading: {}", path, var4);
         }

      });
   }

   public static AABB getStructureBounds(StructureBlockEntity structureblockentity) {
      BlockPos blockpos = structureblockentity.getBlockPos();
      BlockPos blockpos1 = blockpos.offset(structureblockentity.getStructureSize().offset(-1, -1, -1));
      BlockPos blockpos2 = StructureTemplate.transform(blockpos1, Mirror.NONE, structureblockentity.getRotation(), blockpos);
      return new AABB(blockpos, blockpos2);
   }

   public static BoundingBox getStructureBoundingBox(StructureBlockEntity structureblockentity) {
      BlockPos blockpos = structureblockentity.getBlockPos();
      BlockPos blockpos1 = blockpos.offset(structureblockentity.getStructureSize().offset(-1, -1, -1));
      BlockPos blockpos2 = StructureTemplate.transform(blockpos1, Mirror.NONE, structureblockentity.getRotation(), blockpos);
      return BoundingBox.fromCorners(blockpos, blockpos2);
   }

   public static void addCommandBlockAndButtonToStartTest(BlockPos blockpos, BlockPos blockpos1, Rotation rotation, ServerLevel serverlevel) {
      BlockPos blockpos2 = StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, rotation, blockpos);
      serverlevel.setBlockAndUpdate(blockpos2, Blocks.COMMAND_BLOCK.defaultBlockState());
      CommandBlockEntity commandblockentity = (CommandBlockEntity)serverlevel.getBlockEntity(blockpos2);
      commandblockentity.getCommandBlock().setCommand("test runthis");
      BlockPos blockpos3 = StructureTemplate.transform(blockpos2.offset(0, 0, -1), Mirror.NONE, rotation, blockpos2);
      serverlevel.setBlockAndUpdate(blockpos3, Blocks.STONE_BUTTON.defaultBlockState().rotate(rotation));
   }

   public static void createNewEmptyStructureBlock(String s, BlockPos blockpos, Vec3i vec3i, Rotation rotation, ServerLevel serverlevel) {
      BoundingBox boundingbox = getStructureBoundingBox(blockpos, vec3i, rotation);
      clearSpaceForStructure(boundingbox, blockpos.getY(), serverlevel);
      serverlevel.setBlockAndUpdate(blockpos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
      StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos);
      structureblockentity.setIgnoreEntities(false);
      structureblockentity.setStructureName(new ResourceLocation(s));
      structureblockentity.setStructureSize(vec3i);
      structureblockentity.setMode(StructureMode.SAVE);
      structureblockentity.setShowBoundingBox(true);
   }

   public static StructureBlockEntity spawnStructure(String s, BlockPos blockpos, Rotation rotation, int i, ServerLevel serverlevel, boolean flag) {
      Vec3i vec3i = getStructureTemplate(s, serverlevel).getSize();
      BoundingBox boundingbox = getStructureBoundingBox(blockpos, vec3i, rotation);
      BlockPos blockpos1;
      if (rotation == Rotation.NONE) {
         blockpos1 = blockpos;
      } else if (rotation == Rotation.CLOCKWISE_90) {
         blockpos1 = blockpos.offset(vec3i.getZ() - 1, 0, 0);
      } else if (rotation == Rotation.CLOCKWISE_180) {
         blockpos1 = blockpos.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
      } else {
         if (rotation != Rotation.COUNTERCLOCKWISE_90) {
            throw new IllegalArgumentException("Invalid rotation: " + rotation);
         }

         blockpos1 = blockpos.offset(0, 0, vec3i.getX() - 1);
      }

      forceLoadChunks(blockpos, serverlevel);
      clearSpaceForStructure(boundingbox, blockpos.getY(), serverlevel);
      StructureBlockEntity structureblockentity = createStructureBlock(s, blockpos1, rotation, serverlevel, flag);
      serverlevel.getBlockTicks().clearArea(boundingbox);
      serverlevel.clearBlockEvents(boundingbox);
      return structureblockentity;
   }

   private static void forceLoadChunks(BlockPos blockpos, ServerLevel serverlevel) {
      ChunkPos chunkpos = new ChunkPos(blockpos);

      for(int i = -1; i < 4; ++i) {
         for(int j = -1; j < 4; ++j) {
            int k = chunkpos.x + i;
            int l = chunkpos.z + j;
            serverlevel.setChunkForced(k, l, true);
         }
      }

   }

   public static void clearSpaceForStructure(BoundingBox boundingbox, int i, ServerLevel serverlevel) {
      BoundingBox boundingbox1 = new BoundingBox(boundingbox.minX() - 2, boundingbox.minY() - 3, boundingbox.minZ() - 3, boundingbox.maxX() + 3, boundingbox.maxY() + 20, boundingbox.maxZ() + 3);
      BlockPos.betweenClosedStream(boundingbox1).forEach((blockpos) -> clearBlock(i, blockpos, serverlevel));
      serverlevel.getBlockTicks().clearArea(boundingbox1);
      serverlevel.clearBlockEvents(boundingbox1);
      AABB aabb = new AABB((double)boundingbox1.minX(), (double)boundingbox1.minY(), (double)boundingbox1.minZ(), (double)boundingbox1.maxX(), (double)boundingbox1.maxY(), (double)boundingbox1.maxZ());
      List<Entity> list = serverlevel.getEntitiesOfClass(Entity.class, aabb, (entity) -> !(entity instanceof Player));
      list.forEach(Entity::discard);
   }

   public static BoundingBox getStructureBoundingBox(BlockPos blockpos, Vec3i vec3i, Rotation rotation) {
      BlockPos blockpos1 = blockpos.offset(vec3i).offset(-1, -1, -1);
      BlockPos blockpos2 = StructureTemplate.transform(blockpos1, Mirror.NONE, rotation, blockpos);
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos2);
      int i = Math.min(boundingbox.minX(), boundingbox.maxX());
      int j = Math.min(boundingbox.minZ(), boundingbox.maxZ());
      return boundingbox.move(blockpos.getX() - i, 0, blockpos.getZ() - j);
   }

   public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos blockpos, int i, ServerLevel serverlevel) {
      return findStructureBlocks(blockpos, i, serverlevel).stream().filter((blockpos2) -> doesStructureContain(blockpos2, blockpos, serverlevel)).findFirst();
   }

   @Nullable
   public static BlockPos findNearestStructureBlock(BlockPos blockpos, int i, ServerLevel serverlevel) {
      Comparator<BlockPos> comparator = Comparator.comparingInt((blockpos2) -> blockpos2.distManhattan(blockpos));
      Collection<BlockPos> collection = findStructureBlocks(blockpos, i, serverlevel);
      Optional<BlockPos> optional = collection.stream().min(comparator);
      return optional.orElse((BlockPos)null);
   }

   public static Collection<BlockPos> findStructureBlocks(BlockPos blockpos, int i, ServerLevel serverlevel) {
      Collection<BlockPos> collection = Lists.newArrayList();
      AABB aabb = new AABB(blockpos);
      aabb = aabb.inflate((double)i);

      for(int j = (int)aabb.minX; j <= (int)aabb.maxX; ++j) {
         for(int k = (int)aabb.minY; k <= (int)aabb.maxY; ++k) {
            for(int l = (int)aabb.minZ; l <= (int)aabb.maxZ; ++l) {
               BlockPos blockpos1 = new BlockPos(j, k, l);
               BlockState blockstate = serverlevel.getBlockState(blockpos1);
               if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
                  collection.add(blockpos1);
               }
            }
         }
      }

      return collection;
   }

   private static StructureTemplate getStructureTemplate(String s, ServerLevel serverlevel) {
      StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();
      Optional<StructureTemplate> optional = structuretemplatemanager.get(new ResourceLocation(s));
      if (optional.isPresent()) {
         return optional.get();
      } else {
         String s1 = s + ".snbt";
         Path path = Paths.get(testStructuresDir, s1);
         CompoundTag compoundtag = tryLoadStructure(path);
         if (compoundtag == null) {
            throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
         } else {
            return structuretemplatemanager.readStructure(compoundtag);
         }
      }
   }

   private static StructureBlockEntity createStructureBlock(String s, BlockPos blockpos, Rotation rotation, ServerLevel serverlevel, boolean flag) {
      serverlevel.setBlockAndUpdate(blockpos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
      StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos);
      structureblockentity.setMode(StructureMode.LOAD);
      structureblockentity.setRotation(rotation);
      structureblockentity.setIgnoreEntities(false);
      structureblockentity.setStructureName(new ResourceLocation(s));
      structureblockentity.loadStructure(serverlevel, flag);
      if (structureblockentity.getStructureSize() != Vec3i.ZERO) {
         return structureblockentity;
      } else {
         StructureTemplate structuretemplate = getStructureTemplate(s, serverlevel);
         structureblockentity.loadStructure(serverlevel, flag, structuretemplate);
         if (structureblockentity.getStructureSize() == Vec3i.ZERO) {
            throw new RuntimeException("Failed to load structure " + s);
         } else {
            return structureblockentity;
         }
      }
   }

   @Nullable
   private static CompoundTag tryLoadStructure(Path path) {
      try {
         BufferedReader bufferedreader = Files.newBufferedReader(path);
         String s = IOUtils.toString((Reader)bufferedreader);
         return NbtUtils.snbtToStructure(s);
      } catch (IOException var3) {
         return null;
      } catch (CommandSyntaxException var4) {
         throw new RuntimeException("Error while trying to load structure " + path, var4);
      }
   }

   private static void clearBlock(int i, BlockPos blockpos, ServerLevel serverlevel) {
      BlockState blockstate = null;
      RegistryAccess registryaccess = serverlevel.registryAccess();
      FlatLevelGeneratorSettings flatlevelgeneratorsettings = FlatLevelGeneratorSettings.getDefault(registryaccess.lookupOrThrow(Registries.BIOME), registryaccess.lookupOrThrow(Registries.STRUCTURE_SET), registryaccess.lookupOrThrow(Registries.PLACED_FEATURE));
      List<BlockState> list = flatlevelgeneratorsettings.getLayers();
      int j = blockpos.getY() - serverlevel.getMinBuildHeight();
      if (blockpos.getY() < i && j > 0 && j <= list.size()) {
         blockstate = list.get(j - 1);
      }

      if (blockstate == null) {
         blockstate = Blocks.AIR.defaultBlockState();
      }

      BlockInput blockinput = new BlockInput(blockstate, Collections.emptySet(), (CompoundTag)null);
      blockinput.place(serverlevel, blockpos, 2);
      serverlevel.blockUpdated(blockpos, blockstate.getBlock());
   }

   private static boolean doesStructureContain(BlockPos blockpos, BlockPos blockpos1, ServerLevel serverlevel) {
      StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos);
      AABB aabb = getStructureBounds(structureblockentity).inflate(1.0D);
      return aabb.contains(Vec3.atCenterOf(blockpos1));
   }
}
