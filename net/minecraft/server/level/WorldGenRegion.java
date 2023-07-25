package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;
import org.slf4j.Logger;

public class WorldGenRegion implements WorldGenLevel {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final List<ChunkAccess> cache;
   private final ChunkAccess center;
   private final int size;
   private final ServerLevel level;
   private final long seed;
   private final LevelData levelData;
   private final RandomSource random;
   private final DimensionType dimensionType;
   private final WorldGenTickAccess<Block> blockTicks = new WorldGenTickAccess<>((blockpos1) -> this.getChunk(blockpos1).getBlockTicks());
   private final WorldGenTickAccess<Fluid> fluidTicks = new WorldGenTickAccess<>((blockpos) -> this.getChunk(blockpos).getFluidTicks());
   private final BiomeManager biomeManager;
   private final ChunkPos firstPos;
   private final ChunkPos lastPos;
   private final StructureManager structureManager;
   private final ChunkStatus generatingStatus;
   private final int writeRadiusCutoff;
   @Nullable
   private Supplier<String> currentlyGenerating;
   private final AtomicLong subTickCount = new AtomicLong();
   private static final ResourceLocation WORLDGEN_REGION_RANDOM = new ResourceLocation("worldgen_region_random");

   public WorldGenRegion(ServerLevel serverlevel, List<ChunkAccess> list, ChunkStatus chunkstatus, int i) {
      this.generatingStatus = chunkstatus;
      this.writeRadiusCutoff = i;
      int j = Mth.floor(Math.sqrt((double)list.size()));
      if (j * j != list.size()) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Cache size is not a square."));
      } else {
         this.cache = list;
         this.center = list.get(list.size() / 2);
         this.size = j;
         this.level = serverlevel;
         this.seed = serverlevel.getSeed();
         this.levelData = serverlevel.getLevelData();
         this.random = serverlevel.getChunkSource().randomState().getOrCreateRandomFactory(WORLDGEN_REGION_RANDOM).at(this.center.getPos().getWorldPosition());
         this.dimensionType = serverlevel.dimensionType();
         this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed));
         this.firstPos = list.get(0).getPos();
         this.lastPos = list.get(list.size() - 1).getPos();
         this.structureManager = serverlevel.structureManager().forWorldGenRegion(this);
      }
   }

   public boolean isOldChunkAround(ChunkPos chunkpos, int i) {
      return this.level.getChunkSource().chunkMap.isOldChunkAround(chunkpos, i);
   }

   public ChunkPos getCenter() {
      return this.center.getPos();
   }

   public void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
      this.currentlyGenerating = supplier;
   }

   public ChunkAccess getChunk(int i, int j) {
      return this.getChunk(i, j, ChunkStatus.EMPTY);
   }

   @Nullable
   public ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
      ChunkAccess chunkaccess;
      if (this.hasChunk(i, j)) {
         int k = i - this.firstPos.x;
         int l = j - this.firstPos.z;
         chunkaccess = this.cache.get(k + l * this.size);
         if (chunkaccess.getStatus().isOrAfter(chunkstatus)) {
            return chunkaccess;
         }
      } else {
         chunkaccess = null;
      }

      if (!flag) {
         return null;
      } else {
         LOGGER.error("Requested chunk : {} {}", i, j);
         LOGGER.error("Region bounds : {} {} | {} {}", this.firstPos.x, this.firstPos.z, this.lastPos.x, this.lastPos.z);
         if (chunkaccess != null) {
            throw (RuntimeException)Util.pauseInIde(new RuntimeException(String.format(Locale.ROOT, "Chunk is not of correct status. Expecting %s, got %s | %s %s", chunkstatus, chunkaccess.getStatus(), i, j)));
         } else {
            throw (RuntimeException)Util.pauseInIde(new RuntimeException(String.format(Locale.ROOT, "We are asking a region for a chunk out of bound | %s %s", i, j)));
         }
      }
   }

   public boolean hasChunk(int i, int j) {
      return i >= this.firstPos.x && i <= this.lastPos.x && j >= this.firstPos.z && j <= this.lastPos.z;
   }

   public BlockState getBlockState(BlockPos blockpos) {
      return this.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ())).getBlockState(blockpos);
   }

   public FluidState getFluidState(BlockPos blockpos) {
      return this.getChunk(blockpos).getFluidState(blockpos);
   }

   @Nullable
   public Player getNearestPlayer(double d0, double d1, double d2, double d3, Predicate<Entity> predicate) {
      return null;
   }

   public int getSkyDarken() {
      return 0;
   }

   public BiomeManager getBiomeManager() {
      return this.biomeManager;
   }

   public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
      return this.level.getUncachedNoiseBiome(i, j, k);
   }

   public float getShade(Direction direction, boolean flag) {
      return 1.0F;
   }

   public LevelLightEngine getLightEngine() {
      return this.level.getLightEngine();
   }

   public boolean destroyBlock(BlockPos blockpos, boolean flag, @Nullable Entity entity, int i) {
      BlockState blockstate = this.getBlockState(blockpos);
      if (blockstate.isAir()) {
         return false;
      } else {
         if (flag) {
            BlockEntity blockentity = blockstate.hasBlockEntity() ? this.getBlockEntity(blockpos) : null;
            Block.dropResources(blockstate, this.level, blockpos, blockentity, entity, ItemStack.EMPTY);
         }

         return this.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3, i);
      }
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      ChunkAccess chunkaccess = this.getChunk(blockpos);
      BlockEntity blockentity = chunkaccess.getBlockEntity(blockpos);
      if (blockentity != null) {
         return blockentity;
      } else {
         CompoundTag compoundtag = chunkaccess.getBlockEntityNbt(blockpos);
         BlockState blockstate = chunkaccess.getBlockState(blockpos);
         if (compoundtag != null) {
            if ("DUMMY".equals(compoundtag.getString("id"))) {
               if (!blockstate.hasBlockEntity()) {
                  return null;
               }

               blockentity = ((EntityBlock)blockstate.getBlock()).newBlockEntity(blockpos, blockstate);
            } else {
               blockentity = BlockEntity.loadStatic(blockpos, blockstate, compoundtag);
            }

            if (blockentity != null) {
               chunkaccess.setBlockEntity(blockentity);
               return blockentity;
            }
         }

         if (blockstate.hasBlockEntity()) {
            LOGGER.warn("Tried to access a block entity before it was created. {}", (Object)blockpos);
         }

         return null;
      }
   }

   public boolean ensureCanWrite(BlockPos blockpos) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX());
      int j = SectionPos.blockToSectionCoord(blockpos.getZ());
      ChunkPos chunkpos = this.getCenter();
      int k = Math.abs(chunkpos.x - i);
      int l = Math.abs(chunkpos.z - j);
      if (k <= this.writeRadiusCutoff && l <= this.writeRadiusCutoff) {
         if (this.center.isUpgrading()) {
            LevelHeightAccessor levelheightaccessor = this.center.getHeightAccessorForGeneration();
            if (blockpos.getY() < levelheightaccessor.getMinBuildHeight() || blockpos.getY() >= levelheightaccessor.getMaxBuildHeight()) {
               return false;
            }
         }

         return true;
      } else {
         Util.logAndPauseIfInIde("Detected setBlock in a far chunk [" + i + ", " + j + "], pos: " + blockpos + ", status: " + this.generatingStatus + (this.currentlyGenerating == null ? "" : ", currently generating: " + (String)this.currentlyGenerating.get()));
         return false;
      }
   }

   public boolean setBlock(BlockPos blockpos, BlockState blockstate, int i, int j) {
      if (!this.ensureCanWrite(blockpos)) {
         return false;
      } else {
         ChunkAccess chunkaccess = this.getChunk(blockpos);
         BlockState blockstate1 = chunkaccess.setBlockState(blockpos, blockstate, false);
         if (blockstate1 != null) {
            this.level.onBlockStateChange(blockpos, blockstate1, blockstate);
         }

         if (blockstate.hasBlockEntity()) {
            if (chunkaccess.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
               BlockEntity blockentity = ((EntityBlock)blockstate.getBlock()).newBlockEntity(blockpos, blockstate);
               if (blockentity != null) {
                  chunkaccess.setBlockEntity(blockentity);
               } else {
                  chunkaccess.removeBlockEntity(blockpos);
               }
            } else {
               CompoundTag compoundtag = new CompoundTag();
               compoundtag.putInt("x", blockpos.getX());
               compoundtag.putInt("y", blockpos.getY());
               compoundtag.putInt("z", blockpos.getZ());
               compoundtag.putString("id", "DUMMY");
               chunkaccess.setBlockEntityNbt(compoundtag);
            }
         } else if (blockstate1 != null && blockstate1.hasBlockEntity()) {
            chunkaccess.removeBlockEntity(blockpos);
         }

         if (blockstate.hasPostProcess(this, blockpos)) {
            this.markPosForPostprocessing(blockpos);
         }

         return true;
      }
   }

   private void markPosForPostprocessing(BlockPos blockpos) {
      this.getChunk(blockpos).markPosForPostprocessing(blockpos);
   }

   public boolean addFreshEntity(Entity entity) {
      int i = SectionPos.blockToSectionCoord(entity.getBlockX());
      int j = SectionPos.blockToSectionCoord(entity.getBlockZ());
      this.getChunk(i, j).addEntity(entity);
      return true;
   }

   public boolean removeBlock(BlockPos blockpos, boolean flag) {
      return this.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
   }

   public WorldBorder getWorldBorder() {
      return this.level.getWorldBorder();
   }

   public boolean isClientSide() {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public ServerLevel getLevel() {
      return this.level;
   }

   public RegistryAccess registryAccess() {
      return this.level.registryAccess();
   }

   public FeatureFlagSet enabledFeatures() {
      return this.level.enabledFeatures();
   }

   public LevelData getLevelData() {
      return this.levelData;
   }

   public DifficultyInstance getCurrentDifficultyAt(BlockPos blockpos) {
      if (!this.hasChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()))) {
         throw new RuntimeException("We are asking a region for a chunk out of bound");
      } else {
         return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
      }
   }

   @Nullable
   public MinecraftServer getServer() {
      return this.level.getServer();
   }

   public ChunkSource getChunkSource() {
      return this.level.getChunkSource();
   }

   public long getSeed() {
      return this.seed;
   }

   public LevelTickAccess<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public LevelTickAccess<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   public int getSeaLevel() {
      return this.level.getSeaLevel();
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public int getHeight(Heightmap.Types heightmap_types, int i, int j) {
      return this.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j)).getHeight(heightmap_types, i & 15, j & 15) + 1;
   }

   public void playSound(@Nullable Player player, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
   }

   public void addParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
   }

   public void levelEvent(@Nullable Player player, int i, BlockPos blockpos, int j) {
   }

   public void gameEvent(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context) {
   }

   public DimensionType dimensionType() {
      return this.dimensionType;
   }

   public boolean isStateAtPosition(BlockPos blockpos, Predicate<BlockState> predicate) {
      return predicate.test(this.getBlockState(blockpos));
   }

   public boolean isFluidAtPosition(BlockPos blockpos, Predicate<FluidState> predicate) {
      return predicate.test(this.getFluidState(blockpos));
   }

   public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AABB aabb, Predicate<? super T> predicate) {
      return Collections.emptyList();
   }

   public List<Entity> getEntities(@Nullable Entity entity, AABB aabb, @Nullable Predicate<? super Entity> predicate) {
      return Collections.emptyList();
   }

   public List<Player> players() {
      return Collections.emptyList();
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public int getHeight() {
      return this.level.getHeight();
   }

   public long nextSubTickCount() {
      return this.subTickCount.getAndIncrement();
   }
}
