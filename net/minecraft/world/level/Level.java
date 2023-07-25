package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;

public abstract class Level implements LevelAccessor, AutoCloseable {
   public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.DIMENSION);
   public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("overworld"));
   public static final ResourceKey<Level> NETHER = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("the_nether"));
   public static final ResourceKey<Level> END = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("the_end"));
   public static final int MAX_LEVEL_SIZE = 30000000;
   public static final int LONG_PARTICLE_CLIP_RANGE = 512;
   public static final int SHORT_PARTICLE_CLIP_RANGE = 32;
   public static final int MAX_BRIGHTNESS = 15;
   public static final int TICKS_PER_DAY = 24000;
   public static final int MAX_ENTITY_SPAWN_Y = 20000000;
   public static final int MIN_ENTITY_SPAWN_Y = -20000000;
   protected final List<TickingBlockEntity> blockEntityTickers = Lists.newArrayList();
   protected final NeighborUpdater neighborUpdater;
   private final List<TickingBlockEntity> pendingBlockEntityTickers = Lists.newArrayList();
   private boolean tickingBlockEntities;
   private final Thread thread;
   private final boolean isDebug;
   private int skyDarken;
   protected int randValue = RandomSource.create().nextInt();
   protected final int addend = 1013904223;
   protected float oRainLevel;
   protected float rainLevel;
   protected float oThunderLevel;
   protected float thunderLevel;
   public final RandomSource random = RandomSource.create();
   /** @deprecated */
   @Deprecated
   private final RandomSource threadSafeRandom = RandomSource.createThreadSafe();
   private final ResourceKey<DimensionType> dimensionTypeId;
   private final Holder<DimensionType> dimensionTypeRegistration;
   protected final WritableLevelData levelData;
   private final Supplier<ProfilerFiller> profiler;
   public final boolean isClientSide;
   private final WorldBorder worldBorder;
   private final BiomeManager biomeManager;
   private final ResourceKey<Level> dimension;
   private final RegistryAccess registryAccess;
   private final DamageSources damageSources;
   private long subTickCount;

   protected Level(WritableLevelData writableleveldata, ResourceKey<Level> resourcekey, RegistryAccess registryaccess, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean flag, boolean flag1, long i, int j) {
      this.profiler = supplier;
      this.levelData = writableleveldata;
      this.dimensionTypeRegistration = holder;
      this.dimensionTypeId = holder.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Dimension must be registered, got " + holder));
      final DimensionType dimensiontype = holder.value();
      this.dimension = resourcekey;
      this.isClientSide = flag;
      if (dimensiontype.coordinateScale() != 1.0D) {
         this.worldBorder = new WorldBorder() {
            public double getCenterX() {
               return super.getCenterX() / dimensiontype.coordinateScale();
            }

            public double getCenterZ() {
               return super.getCenterZ() / dimensiontype.coordinateScale();
            }
         };
      } else {
         this.worldBorder = new WorldBorder();
      }

      this.thread = Thread.currentThread();
      this.biomeManager = new BiomeManager(this, i);
      this.isDebug = flag1;
      this.neighborUpdater = new CollectingNeighborUpdater(this, j);
      this.registryAccess = registryaccess;
      this.damageSources = new DamageSources(registryaccess);
   }

   public boolean isClientSide() {
      return this.isClientSide;
   }

   @Nullable
   public MinecraftServer getServer() {
      return null;
   }

   public boolean isInWorldBounds(BlockPos blockpos) {
      return !this.isOutsideBuildHeight(blockpos) && isInWorldBoundsHorizontal(blockpos);
   }

   public static boolean isInSpawnableBounds(BlockPos blockpos) {
      return !isOutsideSpawnableHeight(blockpos.getY()) && isInWorldBoundsHorizontal(blockpos);
   }

   private static boolean isInWorldBoundsHorizontal(BlockPos blockpos) {
      return blockpos.getX() >= -30000000 && blockpos.getZ() >= -30000000 && blockpos.getX() < 30000000 && blockpos.getZ() < 30000000;
   }

   private static boolean isOutsideSpawnableHeight(int i) {
      return i < -20000000 || i >= 20000000;
   }

   public LevelChunk getChunkAt(BlockPos blockpos) {
      return this.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
   }

   public LevelChunk getChunk(int i, int j) {
      return (LevelChunk)this.getChunk(i, j, ChunkStatus.FULL);
   }

   @Nullable
   public ChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
      ChunkAccess chunkaccess = this.getChunkSource().getChunk(i, j, chunkstatus, flag);
      if (chunkaccess == null && flag) {
         throw new IllegalStateException("Should always be able to create a chunk!");
      } else {
         return chunkaccess;
      }
   }

   public boolean setBlock(BlockPos blockpos, BlockState blockstate, int i) {
      return this.setBlock(blockpos, blockstate, i, 512);
   }

   public boolean setBlock(BlockPos blockpos, BlockState blockstate, int i, int j) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return false;
      } else if (!this.isClientSide && this.isDebug()) {
         return false;
      } else {
         LevelChunk levelchunk = this.getChunkAt(blockpos);
         Block block = blockstate.getBlock();
         BlockState blockstate1 = levelchunk.setBlockState(blockpos, blockstate, (i & 64) != 0);
         if (blockstate1 == null) {
            return false;
         } else {
            BlockState blockstate2 = this.getBlockState(blockpos);
            if (blockstate2 == blockstate) {
               if (blockstate1 != blockstate2) {
                  this.setBlocksDirty(blockpos, blockstate1, blockstate2);
               }

               if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && (this.isClientSide || levelchunk.getFullStatus() != null && levelchunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
                  this.sendBlockUpdated(blockpos, blockstate1, blockstate, i);
               }

               if ((i & 1) != 0) {
                  this.blockUpdated(blockpos, blockstate1.getBlock());
                  if (!this.isClientSide && blockstate.hasAnalogOutputSignal()) {
                     this.updateNeighbourForOutputSignal(blockpos, block);
                  }
               }

               if ((i & 16) == 0 && j > 0) {
                  int k = i & -34;
                  blockstate1.updateIndirectNeighbourShapes(this, blockpos, k, j - 1);
                  blockstate.updateNeighbourShapes(this, blockpos, k, j - 1);
                  blockstate.updateIndirectNeighbourShapes(this, blockpos, k, j - 1);
               }

               this.onBlockStateChange(blockpos, blockstate1, blockstate2);
            }

            return true;
         }
      }
   }

   public void onBlockStateChange(BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
   }

   public boolean removeBlock(BlockPos blockpos, boolean flag) {
      FluidState fluidstate = this.getFluidState(blockpos);
      return this.setBlock(blockpos, fluidstate.createLegacyBlock(), 3 | (flag ? 64 : 0));
   }

   public boolean destroyBlock(BlockPos blockpos, boolean flag, @Nullable Entity entity, int i) {
      BlockState blockstate = this.getBlockState(blockpos);
      if (blockstate.isAir()) {
         return false;
      } else {
         FluidState fluidstate = this.getFluidState(blockpos);
         if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
            this.levelEvent(2001, blockpos, Block.getId(blockstate));
         }

         if (flag) {
            BlockEntity blockentity = blockstate.hasBlockEntity() ? this.getBlockEntity(blockpos) : null;
            Block.dropResources(blockstate, this, blockpos, blockentity, entity, ItemStack.EMPTY);
         }

         boolean flag1 = this.setBlock(blockpos, fluidstate.createLegacyBlock(), 3, i);
         if (flag1) {
            this.gameEvent(GameEvent.BLOCK_DESTROY, blockpos, GameEvent.Context.of(entity, blockstate));
         }

         return flag1;
      }
   }

   public void addDestroyBlockEffect(BlockPos blockpos, BlockState blockstate) {
   }

   public boolean setBlockAndUpdate(BlockPos blockpos, BlockState blockstate) {
      return this.setBlock(blockpos, blockstate, 3);
   }

   public abstract void sendBlockUpdated(BlockPos blockpos, BlockState blockstate, BlockState blockstate1, int i);

   public void setBlocksDirty(BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
   }

   public void updateNeighborsAt(BlockPos blockpos, Block block) {
   }

   public void updateNeighborsAtExceptFromFacing(BlockPos blockpos, Block block, Direction direction) {
   }

   public void neighborChanged(BlockPos blockpos, Block block, BlockPos blockpos1) {
   }

   public void neighborChanged(BlockState blockstate, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
   }

   public void neighborShapeChanged(Direction direction, BlockState blockstate, BlockPos blockpos, BlockPos blockpos1, int i, int j) {
      this.neighborUpdater.shapeUpdate(direction, blockstate, blockpos, blockpos1, i, j);
   }

   public int getHeight(Heightmap.Types heightmap_types, int i, int j) {
      int l;
      if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
         if (this.hasChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j))) {
            l = this.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j)).getHeight(heightmap_types, i & 15, j & 15) + 1;
         } else {
            l = this.getMinBuildHeight();
         }
      } else {
         l = this.getSeaLevel() + 1;
      }

      return l;
   }

   public LevelLightEngine getLightEngine() {
      return this.getChunkSource().getLightEngine();
   }

   public BlockState getBlockState(BlockPos blockpos) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         LevelChunk levelchunk = this.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
         return levelchunk.getBlockState(blockpos);
      }
   }

   public FluidState getFluidState(BlockPos blockpos) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         LevelChunk levelchunk = this.getChunkAt(blockpos);
         return levelchunk.getFluidState(blockpos);
      }
   }

   public boolean isDay() {
      return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
   }

   public boolean isNight() {
      return !this.dimensionType().hasFixedTime() && !this.isDay();
   }

   public void playSound(@Nullable Entity entity, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
      Player var10001;
      if (entity instanceof Player player) {
         var10001 = player;
      } else {
         var10001 = null;
      }

      this.playSound(var10001, blockpos, soundevent, soundsource, f, f1);
   }

   public void playSound(@Nullable Player player, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
      this.playSound(player, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, soundevent, soundsource, f, f1);
   }

   public abstract void playSeededSound(@Nullable Player player, double d0, double d1, double d2, Holder<SoundEvent> holder, SoundSource soundsource, float f, float f1, long i);

   public void playSeededSound(@Nullable Player player, double d0, double d1, double d2, SoundEvent soundevent, SoundSource soundsource, float f, float f1, long i) {
      this.playSeededSound(player, d0, d1, d2, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundevent), soundsource, f, f1, i);
   }

   public abstract void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundsource, float f, float f1, long i);

   public void playSound(@Nullable Player player, double d0, double d1, double d2, SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
      this.playSeededSound(player, d0, d1, d2, soundevent, soundsource, f, f1, this.threadSafeRandom.nextLong());
   }

   public void playSound(@Nullable Player player, Entity entity, SoundEvent soundevent, SoundSource soundsource, float f, float f1) {
      this.playSeededSound(player, entity, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundevent), soundsource, f, f1, this.threadSafeRandom.nextLong());
   }

   public void playLocalSound(BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource, float f, float f1, boolean flag) {
      this.playLocalSound((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, soundevent, soundsource, f, f1, flag);
   }

   public void playLocalSound(double d0, double d1, double d2, SoundEvent soundevent, SoundSource soundsource, float f, float f1, boolean flag) {
   }

   public void addParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
   }

   public void addParticle(ParticleOptions particleoptions, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {
   }

   public void addAlwaysVisibleParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
   }

   public void addAlwaysVisibleParticle(ParticleOptions particleoptions, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {
   }

   public float getSunAngle(float f) {
      float f1 = this.getTimeOfDay(f);
      return f1 * ((float)Math.PI * 2F);
   }

   public void addBlockEntityTicker(TickingBlockEntity tickingblockentity) {
      (this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(tickingblockentity);
   }

   protected void tickBlockEntities() {
      ProfilerFiller profilerfiller = this.getProfiler();
      profilerfiller.push("blockEntities");
      this.tickingBlockEntities = true;
      if (!this.pendingBlockEntityTickers.isEmpty()) {
         this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
         this.pendingBlockEntityTickers.clear();
      }

      Iterator<TickingBlockEntity> iterator = this.blockEntityTickers.iterator();

      while(iterator.hasNext()) {
         TickingBlockEntity tickingblockentity = iterator.next();
         if (tickingblockentity.isRemoved()) {
            iterator.remove();
         } else if (this.shouldTickBlocksAt(tickingblockentity.getPos())) {
            tickingblockentity.tick();
         }
      }

      this.tickingBlockEntities = false;
      profilerfiller.pop();
   }

   public <T extends Entity> void guardEntityTick(Consumer<T> consumer, T entity) {
      try {
         consumer.accept(entity);
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.forThrowable(var6, "Ticking entity");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
         entity.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   public boolean shouldTickDeath(Entity entity) {
      return true;
   }

   public boolean shouldTickBlocksAt(long i) {
      return true;
   }

   public boolean shouldTickBlocksAt(BlockPos blockpos) {
      return this.shouldTickBlocksAt(ChunkPos.asLong(blockpos));
   }

   public Explosion explode(@Nullable Entity entity, double d0, double d1, double d2, float f, Level.ExplosionInteraction level_explosioninteraction) {
      return this.explode(entity, (DamageSource)null, (ExplosionDamageCalculator)null, d0, d1, d2, f, false, level_explosioninteraction);
   }

   public Explosion explode(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Level.ExplosionInteraction level_explosioninteraction) {
      return this.explode(entity, (DamageSource)null, (ExplosionDamageCalculator)null, d0, d1, d2, f, flag, level_explosioninteraction);
   }

   public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, Vec3 vec3, float f, boolean flag, Level.ExplosionInteraction level_explosioninteraction) {
      return this.explode(entity, damagesource, explosiondamagecalculator, vec3.x(), vec3.y(), vec3.z(), f, flag, level_explosioninteraction);
   }

   public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Level.ExplosionInteraction level_explosioninteraction) {
      return this.explode(entity, damagesource, explosiondamagecalculator, d0, d1, d2, f, flag, level_explosioninteraction, true);
   }

   public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Level.ExplosionInteraction level_explosioninteraction, boolean flag1) {
      Explosion.BlockInteraction var10000;
      switch (level_explosioninteraction) {
         case NONE:
            var10000 = Explosion.BlockInteraction.KEEP;
            break;
         case BLOCK:
            var10000 = this.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            break;
         case MOB:
            var10000 = this.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? this.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY) : Explosion.BlockInteraction.KEEP;
            break;
         case TNT:
            var10000 = this.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      Explosion.BlockInteraction explosion_blockinteraction = var10000;
      Explosion explosion = new Explosion(this, entity, damagesource, explosiondamagecalculator, d0, d1, d2, f, flag, explosion_blockinteraction);
      explosion.explode();
      explosion.finalizeExplosion(flag1);
      return explosion;
   }

   private Explosion.BlockInteraction getDestroyType(GameRules.Key<GameRules.BooleanValue> gamerules_key) {
      return this.getGameRules().getBoolean(gamerules_key) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
   }

   public abstract String gatherChunkSourceStats();

   @Nullable
   public BlockEntity getBlockEntity(BlockPos blockpos) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return null;
      } else {
         return !this.isClientSide && Thread.currentThread() != this.thread ? null : this.getChunkAt(blockpos).getBlockEntity(blockpos, LevelChunk.EntityCreationType.IMMEDIATE);
      }
   }

   public void setBlockEntity(BlockEntity blockentity) {
      BlockPos blockpos = blockentity.getBlockPos();
      if (!this.isOutsideBuildHeight(blockpos)) {
         this.getChunkAt(blockpos).addAndRegisterBlockEntity(blockentity);
      }
   }

   public void removeBlockEntity(BlockPos blockpos) {
      if (!this.isOutsideBuildHeight(blockpos)) {
         this.getChunkAt(blockpos).removeBlockEntity(blockpos);
      }
   }

   public boolean isLoaded(BlockPos blockpos) {
      return this.isOutsideBuildHeight(blockpos) ? false : this.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
   }

   public boolean loadedAndEntityCanStandOnFace(BlockPos blockpos, Entity entity, Direction direction) {
      if (this.isOutsideBuildHeight(blockpos)) {
         return false;
      } else {
         ChunkAccess chunkaccess = this.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()), ChunkStatus.FULL, false);
         return chunkaccess == null ? false : chunkaccess.getBlockState(blockpos).entityCanStandOnFace(this, blockpos, entity, direction);
      }
   }

   public boolean loadedAndEntityCanStandOn(BlockPos blockpos, Entity entity) {
      return this.loadedAndEntityCanStandOnFace(blockpos, entity, Direction.UP);
   }

   public void updateSkyBrightness() {
      double d0 = 1.0D - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0D;
      double d1 = 1.0D - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0D;
      double d2 = 0.5D + 2.0D * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0F) * ((float)Math.PI * 2F)), -0.25D, 0.25D);
      this.skyDarken = (int)((1.0D - d2 * d0 * d1) * 11.0D);
   }

   public void setSpawnSettings(boolean flag, boolean flag1) {
      this.getChunkSource().setSpawnSettings(flag, flag1);
   }

   public BlockPos getSharedSpawnPos() {
      BlockPos blockpos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
      if (!this.getWorldBorder().isWithinBounds(blockpos)) {
         blockpos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
      }

      return blockpos;
   }

   public float getSharedSpawnAngle() {
      return this.levelData.getSpawnAngle();
   }

   protected void prepareWeather() {
      if (this.levelData.isRaining()) {
         this.rainLevel = 1.0F;
         if (this.levelData.isThundering()) {
            this.thunderLevel = 1.0F;
         }
      }

   }

   public void close() throws IOException {
      this.getChunkSource().close();
   }

   @Nullable
   public BlockGetter getChunkForCollisions(int i, int j) {
      return this.getChunk(i, j, ChunkStatus.FULL, false);
   }

   public List<Entity> getEntities(@Nullable Entity entity, AABB aabb, Predicate<? super Entity> predicate) {
      this.getProfiler().incrementCounter("getEntities");
      List<Entity> list = Lists.newArrayList();
      this.getEntities().get(aabb, (entity2) -> {
         if (entity2 != entity && predicate.test(entity2)) {
            list.add(entity2);
         }

         if (entity2 instanceof EnderDragon) {
            for(EnderDragonPart enderdragonpart : ((EnderDragon)entity2).getSubEntities()) {
               if (entity2 != entity && predicate.test(enderdragonpart)) {
                  list.add(enderdragonpart);
               }
            }
         }

      });
      return list;
   }

   public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AABB aabb, Predicate<? super T> predicate) {
      List<T> list = Lists.newArrayList();
      this.getEntities(entitytypetest, aabb, predicate, list);
      return list;
   }

   public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entitytypetest, AABB aabb, Predicate<? super T> predicate, List<? super T> list) {
      this.getEntities(entitytypetest, aabb, predicate, list, Integer.MAX_VALUE);
   }

   public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entitytypetest, AABB aabb, Predicate<? super T> predicate, List<? super T> list, int i) {
      this.getProfiler().incrementCounter("getEntities");
      this.getEntities().get(entitytypetest, aabb, (entity) -> {
         if (predicate.test(entity)) {
            list.add(entity);
            if (list.size() >= i) {
               return AbortableIterationConsumer.Continuation.ABORT;
            }
         }

         if (entity instanceof EnderDragon enderdragon) {
            for(EnderDragonPart enderdragonpart : enderdragon.getSubEntities()) {
               T entity1 = entitytypetest.tryCast(enderdragonpart);
               if (entity1 != null && predicate.test(entity1)) {
                  list.add(entity1);
                  if (list.size() >= i) {
                     return AbortableIterationConsumer.Continuation.ABORT;
                  }
               }
            }
         }

         return AbortableIterationConsumer.Continuation.CONTINUE;
      });
   }

   @Nullable
   public abstract Entity getEntity(int i);

   public void blockEntityChanged(BlockPos blockpos) {
      if (this.hasChunkAt(blockpos)) {
         this.getChunkAt(blockpos).setUnsaved(true);
      }

   }

   public int getSeaLevel() {
      return 63;
   }

   public void disconnect() {
   }

   public long getGameTime() {
      return this.levelData.getGameTime();
   }

   public long getDayTime() {
      return this.levelData.getDayTime();
   }

   public boolean mayInteract(Player player, BlockPos blockpos) {
      return true;
   }

   public void broadcastEntityEvent(Entity entity, byte b0) {
   }

   public void broadcastDamageEvent(Entity entity, DamageSource damagesource) {
   }

   public void blockEvent(BlockPos blockpos, Block block, int i, int j) {
      this.getBlockState(blockpos).triggerEvent(this, blockpos, i, j);
   }

   public LevelData getLevelData() {
      return this.levelData;
   }

   public GameRules getGameRules() {
      return this.levelData.getGameRules();
   }

   public float getThunderLevel(float f) {
      return Mth.lerp(f, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(f);
   }

   public void setThunderLevel(float f) {
      float f1 = Mth.clamp(f, 0.0F, 1.0F);
      this.oThunderLevel = f1;
      this.thunderLevel = f1;
   }

   public float getRainLevel(float f) {
      return Mth.lerp(f, this.oRainLevel, this.rainLevel);
   }

   public void setRainLevel(float f) {
      float f1 = Mth.clamp(f, 0.0F, 1.0F);
      this.oRainLevel = f1;
      this.rainLevel = f1;
   }

   public boolean isThundering() {
      if (this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling()) {
         return (double)this.getThunderLevel(1.0F) > 0.9D;
      } else {
         return false;
      }
   }

   public boolean isRaining() {
      return (double)this.getRainLevel(1.0F) > 0.2D;
   }

   public boolean isRainingAt(BlockPos blockpos) {
      if (!this.isRaining()) {
         return false;
      } else if (!this.canSeeSky(blockpos)) {
         return false;
      } else if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > blockpos.getY()) {
         return false;
      } else {
         Biome biome = this.getBiome(blockpos).value();
         return biome.getPrecipitationAt(blockpos) == Biome.Precipitation.RAIN;
      }
   }

   @Nullable
   public abstract MapItemSavedData getMapData(String s);

   public abstract void setMapData(String s, MapItemSavedData mapitemsaveddata);

   public abstract int getFreeMapId();

   public void globalLevelEvent(int i, BlockPos blockpos, int j) {
   }

   public CrashReportCategory fillReportDetails(CrashReport crashreport) {
      CrashReportCategory crashreportcategory = crashreport.addCategory("Affected level", 1);
      crashreportcategory.setDetail("All players", () -> this.players().size() + " total; " + this.players());
      crashreportcategory.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
      crashreportcategory.setDetail("Level dimension", () -> this.dimension().location().toString());

      try {
         this.levelData.fillCrashReportCategory(crashreportcategory, this);
      } catch (Throwable var4) {
         crashreportcategory.setDetailError("Level Data Unobtainable", var4);
      }

      return crashreportcategory;
   }

   public abstract void destroyBlockProgress(int i, BlockPos blockpos, int j);

   public void createFireworks(double d0, double d1, double d2, double d3, double d4, double d5, @Nullable CompoundTag compoundtag) {
   }

   public abstract Scoreboard getScoreboard();

   public void updateNeighbourForOutputSignal(BlockPos blockpos, Block block) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         if (this.hasChunkAt(blockpos1)) {
            BlockState blockstate = this.getBlockState(blockpos1);
            if (blockstate.is(Blocks.COMPARATOR)) {
               this.neighborChanged(blockstate, blockpos1, block, blockpos, false);
            } else if (blockstate.isRedstoneConductor(this, blockpos1)) {
               blockpos1 = blockpos1.relative(direction);
               blockstate = this.getBlockState(blockpos1);
               if (blockstate.is(Blocks.COMPARATOR)) {
                  this.neighborChanged(blockstate, blockpos1, block, blockpos, false);
               }
            }
         }
      }

   }

   public DifficultyInstance getCurrentDifficultyAt(BlockPos blockpos) {
      long i = 0L;
      float f = 0.0F;
      if (this.hasChunkAt(blockpos)) {
         f = this.getMoonBrightness();
         i = this.getChunkAt(blockpos).getInhabitedTime();
      }

      return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), i, f);
   }

   public int getSkyDarken() {
      return this.skyDarken;
   }

   public void setSkyFlashTime(int i) {
   }

   public WorldBorder getWorldBorder() {
      return this.worldBorder;
   }

   public void sendPacketToServer(Packet<?> packet) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   public DimensionType dimensionType() {
      return this.dimensionTypeRegistration.value();
   }

   public ResourceKey<DimensionType> dimensionTypeId() {
      return this.dimensionTypeId;
   }

   public Holder<DimensionType> dimensionTypeRegistration() {
      return this.dimensionTypeRegistration;
   }

   public ResourceKey<Level> dimension() {
      return this.dimension;
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public boolean isStateAtPosition(BlockPos blockpos, Predicate<BlockState> predicate) {
      return predicate.test(this.getBlockState(blockpos));
   }

   public boolean isFluidAtPosition(BlockPos blockpos, Predicate<FluidState> predicate) {
      return predicate.test(this.getFluidState(blockpos));
   }

   public abstract RecipeManager getRecipeManager();

   public BlockPos getBlockRandomPos(int i, int j, int k, int l) {
      this.randValue = this.randValue * 3 + 1013904223;
      int i1 = this.randValue >> 2;
      return new BlockPos(i + (i1 & 15), j + (i1 >> 16 & l), k + (i1 >> 8 & 15));
   }

   public boolean noSave() {
      return false;
   }

   public ProfilerFiller getProfiler() {
      return this.profiler.get();
   }

   public Supplier<ProfilerFiller> getProfilerSupplier() {
      return this.profiler;
   }

   public BiomeManager getBiomeManager() {
      return this.biomeManager;
   }

   public final boolean isDebug() {
      return this.isDebug;
   }

   protected abstract LevelEntityGetter<Entity> getEntities();

   public long nextSubTickCount() {
      return (long)(this.subTickCount++);
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public DamageSources damageSources() {
      return this.damageSources;
   }

   public static enum ExplosionInteraction {
      NONE,
      BLOCK,
      MOB,
      TNT;
   }
}
