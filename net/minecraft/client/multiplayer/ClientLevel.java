package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.slf4j.Logger;

public class ClientLevel extends Level {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final double FLUID_PARTICLE_SPAWN_OFFSET = 0.05D;
   private static final int NORMAL_LIGHT_UPDATES_PER_FRAME = 10;
   private static final int LIGHT_UPDATE_QUEUE_SIZE_THRESHOLD = 1000;
   final EntityTickList tickingEntities = new EntityTickList();
   private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new ClientLevel.EntityCallbacks());
   private final ClientPacketListener connection;
   private final LevelRenderer levelRenderer;
   private final ClientLevel.ClientLevelData clientLevelData;
   private final DimensionSpecialEffects effects;
   private final Minecraft minecraft = Minecraft.getInstance();
   final List<AbstractClientPlayer> players = Lists.newArrayList();
   private Scoreboard scoreboard = new Scoreboard();
   private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
   private static final long CLOUD_COLOR = 16777215L;
   private int skyFlashTime;
   private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap<>(3), (object2objectarraymap) -> {
      object2objectarraymap.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache((blockpos2) -> this.calculateBlockTint(blockpos2, BiomeColors.GRASS_COLOR_RESOLVER)));
      object2objectarraymap.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache((blockpos1) -> this.calculateBlockTint(blockpos1, BiomeColors.FOLIAGE_COLOR_RESOLVER)));
      object2objectarraymap.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache((blockpos) -> this.calculateBlockTint(blockpos, BiomeColors.WATER_COLOR_RESOLVER)));
   });
   private final ClientChunkCache chunkSource;
   private final Deque<Runnable> lightUpdateQueue = Queues.newArrayDeque();
   private int serverSimulationDistance;
   private final BlockStatePredictionHandler blockStatePredictionHandler = new BlockStatePredictionHandler();
   private static final Set<Item> MARKER_PARTICLE_ITEMS = Set.of(Items.BARRIER, Items.LIGHT);

   public void handleBlockChangedAck(int i) {
      this.blockStatePredictionHandler.endPredictionsUpTo(i, this);
   }

   public void setServerVerifiedBlockState(BlockPos blockpos, BlockState blockstate, int i) {
      if (!this.blockStatePredictionHandler.updateKnownServerState(blockpos, blockstate)) {
         super.setBlock(blockpos, blockstate, i, 512);
      }

   }

   public void syncBlockState(BlockPos blockpos, BlockState blockstate, Vec3 vec3) {
      BlockState blockstate1 = this.getBlockState(blockpos);
      if (blockstate1 != blockstate) {
         this.setBlock(blockpos, blockstate, 19);
         Player player = this.minecraft.player;
         if (this == player.level() && player.isColliding(blockpos, blockstate)) {
            player.absMoveTo(vec3.x, vec3.y, vec3.z);
         }
      }

   }

   BlockStatePredictionHandler getBlockStatePredictionHandler() {
      return this.blockStatePredictionHandler;
   }

   public boolean setBlock(BlockPos blockpos, BlockState blockstate, int i, int j) {
      if (this.blockStatePredictionHandler.isPredicting()) {
         BlockState blockstate1 = this.getBlockState(blockpos);
         boolean flag = super.setBlock(blockpos, blockstate, i, j);
         if (flag) {
            this.blockStatePredictionHandler.retainKnownServerState(blockpos, blockstate1, this.minecraft.player);
         }

         return flag;
      } else {
         return super.setBlock(blockpos, blockstate, i, j);
      }
   }

   public ClientLevel(ClientPacketListener clientpacketlistener, ClientLevel.ClientLevelData clientlevel_clientleveldata, ResourceKey<Level> resourcekey, Holder<DimensionType> holder, int i, int j, Supplier<ProfilerFiller> supplier, LevelRenderer levelrenderer, boolean flag, long k) {
      super(clientlevel_clientleveldata, resourcekey, clientpacketlistener.registryAccess(), holder, supplier, true, flag, k, 1000000);
      this.connection = clientpacketlistener;
      this.chunkSource = new ClientChunkCache(this, i);
      this.clientLevelData = clientlevel_clientleveldata;
      this.levelRenderer = levelrenderer;
      this.effects = DimensionSpecialEffects.forType(holder.value());
      this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
      this.serverSimulationDistance = j;
      this.updateSkyBrightness();
      this.prepareWeather();
   }

   public void queueLightUpdate(Runnable runnable) {
      this.lightUpdateQueue.add(runnable);
   }

   public void pollLightUpdates() {
      int i = this.lightUpdateQueue.size();
      int j = i < 1000 ? Math.max(10, i / 10) : i;

      for(int k = 0; k < j; ++k) {
         Runnable runnable = this.lightUpdateQueue.poll();
         if (runnable == null) {
            break;
         }

         runnable.run();
      }

   }

   public boolean isLightUpdateQueueEmpty() {
      return this.lightUpdateQueue.isEmpty();
   }

   public DimensionSpecialEffects effects() {
      return this.effects;
   }

   public void tick(BooleanSupplier booleansupplier) {
      this.getWorldBorder().tick();
      this.tickTime();
      if (this.skyFlashTime > 0) {
         this.setSkyFlashTime(this.skyFlashTime - 1);
      }

      this.getProfiler().push("blocks");
      this.chunkSource.tick(booleansupplier, true);
      this.getProfiler().pop();
   }

   private void tickTime() {
      this.setGameTime(this.levelData.getGameTime() + 1L);
      if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
         this.setDayTime(this.levelData.getDayTime() + 1L);
      }

   }

   public void setGameTime(long i) {
      this.clientLevelData.setGameTime(i);
   }

   public void setDayTime(long i) {
      if (i < 0L) {
         i = -i;
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
      } else {
         this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, (MinecraftServer)null);
      }

      this.clientLevelData.setDayTime(i);
   }

   public Iterable<Entity> entitiesForRendering() {
      return this.getEntities().getAll();
   }

   public void tickEntities() {
      ProfilerFiller profilerfiller = this.getProfiler();
      profilerfiller.push("entities");
      this.tickingEntities.forEach((entity) -> {
         if (!entity.isRemoved() && !entity.isPassenger()) {
            this.guardEntityTick(this::tickNonPassenger, entity);
         }
      });
      profilerfiller.pop();
      this.tickBlockEntities();
   }

   public boolean shouldTickDeath(Entity entity) {
      return entity.chunkPosition().getChessboardDistance(this.minecraft.player.chunkPosition()) <= this.serverSimulationDistance;
   }

   public void tickNonPassenger(Entity entity) {
      entity.setOldPosAndRot();
      ++entity.tickCount;
      this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
      entity.tick();
      this.getProfiler().pop();

      for(Entity entity1 : entity.getPassengers()) {
         this.tickPassenger(entity, entity1);
      }

   }

   private void tickPassenger(Entity entity, Entity entity1) {
      if (!entity1.isRemoved() && entity1.getVehicle() == entity) {
         if (entity1 instanceof Player || this.tickingEntities.contains(entity1)) {
            entity1.setOldPosAndRot();
            ++entity1.tickCount;
            entity1.rideTick();

            for(Entity entity2 : entity1.getPassengers()) {
               this.tickPassenger(entity1, entity2);
            }

         }
      } else {
         entity1.stopRiding();
      }
   }

   public void unload(LevelChunk levelchunk) {
      levelchunk.clearAllBlockEntities();
      this.chunkSource.getLightEngine().setLightEnabled(levelchunk.getPos(), false);
      this.entityStorage.stopTicking(levelchunk.getPos());
   }

   public void onChunkLoaded(ChunkPos chunkpos) {
      this.tintCaches.forEach((colorresolver, blocktintcache) -> blocktintcache.invalidateForChunk(chunkpos.x, chunkpos.z));
      this.entityStorage.startTicking(chunkpos);
   }

   public void clearTintCaches() {
      this.tintCaches.forEach((colorresolver, blocktintcache) -> blocktintcache.invalidateAll());
   }

   public boolean hasChunk(int i, int j) {
      return true;
   }

   public int getEntityCount() {
      return this.entityStorage.count();
   }

   public void addPlayer(int i, AbstractClientPlayer abstractclientplayer) {
      this.addEntity(i, abstractclientplayer);
   }

   public void putNonPlayerEntity(int i, Entity entity) {
      this.addEntity(i, entity);
   }

   private void addEntity(int i, Entity entity) {
      this.removeEntity(i, Entity.RemovalReason.DISCARDED);
      this.entityStorage.addEntity(entity);
   }

   public void removeEntity(int i, Entity.RemovalReason entity_removalreason) {
      Entity entity = this.getEntities().get(i);
      if (entity != null) {
         entity.setRemoved(entity_removalreason);
         entity.onClientRemoval();
      }

   }

   @Nullable
   public Entity getEntity(int i) {
      return this.getEntities().get(i);
   }

   public void disconnect() {
      this.connection.getConnection().disconnect(Component.translatable("multiplayer.status.quitting"));
   }

   public void animateTick(int i, int j, int k) {
      int l = 32;
      RandomSource randomsource = RandomSource.create();
      Block block = this.getMarkerParticleTarget();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i1 = 0; i1 < 667; ++i1) {
         this.doAnimateTick(i, j, k, 16, randomsource, block, blockpos_mutableblockpos);
         this.doAnimateTick(i, j, k, 32, randomsource, block, blockpos_mutableblockpos);
      }

   }

   @Nullable
   private Block getMarkerParticleTarget() {
      if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE) {
         ItemStack itemstack = this.minecraft.player.getMainHandItem();
         Item item = itemstack.getItem();
         if (MARKER_PARTICLE_ITEMS.contains(item) && item instanceof BlockItem) {
            BlockItem blockitem = (BlockItem)item;
            return blockitem.getBlock();
         }
      }

      return null;
   }

   public void doAnimateTick(int i, int j, int k, int l, RandomSource randomsource, @Nullable Block block, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      int i1 = i + this.random.nextInt(l) - this.random.nextInt(l);
      int j1 = j + this.random.nextInt(l) - this.random.nextInt(l);
      int k1 = k + this.random.nextInt(l) - this.random.nextInt(l);
      blockpos_mutableblockpos.set(i1, j1, k1);
      BlockState blockstate = this.getBlockState(blockpos_mutableblockpos);
      blockstate.getBlock().animateTick(blockstate, this, blockpos_mutableblockpos, randomsource);
      FluidState fluidstate = this.getFluidState(blockpos_mutableblockpos);
      if (!fluidstate.isEmpty()) {
         fluidstate.animateTick(this, blockpos_mutableblockpos, randomsource);
         ParticleOptions particleoptions = fluidstate.getDripParticle();
         if (particleoptions != null && this.random.nextInt(10) == 0) {
            boolean flag = blockstate.isFaceSturdy(this, blockpos_mutableblockpos, Direction.DOWN);
            BlockPos blockpos = blockpos_mutableblockpos.below();
            this.trySpawnDripParticles(blockpos, this.getBlockState(blockpos), particleoptions, flag);
         }
      }

      if (block == blockstate.getBlock()) {
         this.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, blockstate), (double)i1 + 0.5D, (double)j1 + 0.5D, (double)k1 + 0.5D, 0.0D, 0.0D, 0.0D);
      }

      if (!blockstate.isCollisionShapeFullBlock(this, blockpos_mutableblockpos)) {
         this.getBiome(blockpos_mutableblockpos).value().getAmbientParticle().ifPresent((ambientparticlesettings) -> {
            if (ambientparticlesettings.canSpawn(this.random)) {
               this.addParticle(ambientparticlesettings.getOptions(), (double)blockpos_mutableblockpos.getX() + this.random.nextDouble(), (double)blockpos_mutableblockpos.getY() + this.random.nextDouble(), (double)blockpos_mutableblockpos.getZ() + this.random.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

         });
      }

   }

   private void trySpawnDripParticles(BlockPos blockpos, BlockState blockstate, ParticleOptions particleoptions, boolean flag) {
      if (blockstate.getFluidState().isEmpty()) {
         VoxelShape voxelshape = blockstate.getCollisionShape(this, blockpos);
         double d0 = voxelshape.max(Direction.Axis.Y);
         if (d0 < 1.0D) {
            if (flag) {
               this.spawnFluidParticle((double)blockpos.getX(), (double)(blockpos.getX() + 1), (double)blockpos.getZ(), (double)(blockpos.getZ() + 1), (double)(blockpos.getY() + 1) - 0.05D, particleoptions);
            }
         } else if (!blockstate.is(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.min(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(blockpos, particleoptions, voxelshape, (double)blockpos.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos1 = blockpos.below();
               BlockState blockstate1 = this.getBlockState(blockpos1);
               VoxelShape voxelshape1 = blockstate1.getCollisionShape(this, blockpos1);
               double d2 = voxelshape1.max(Direction.Axis.Y);
               if (d2 < 1.0D && blockstate1.getFluidState().isEmpty()) {
                  this.spawnParticle(blockpos, particleoptions, voxelshape, (double)blockpos.getY() - 0.05D);
               }
            }
         }

      }
   }

   private void spawnParticle(BlockPos blockpos, ParticleOptions particleoptions, VoxelShape voxelshape, double d0) {
      this.spawnFluidParticle((double)blockpos.getX() + voxelshape.min(Direction.Axis.X), (double)blockpos.getX() + voxelshape.max(Direction.Axis.X), (double)blockpos.getZ() + voxelshape.min(Direction.Axis.Z), (double)blockpos.getZ() + voxelshape.max(Direction.Axis.Z), d0, particleoptions);
   }

   private void spawnFluidParticle(double d0, double d1, double d2, double d3, double d4, ParticleOptions particleoptions) {
      this.addParticle(particleoptions, Mth.lerp(this.random.nextDouble(), d0, d1), d4, Mth.lerp(this.random.nextDouble(), d2, d3), 0.0D, 0.0D, 0.0D);
   }

   public CrashReportCategory fillReportDetails(CrashReport crashreport) {
      CrashReportCategory crashreportcategory = super.fillReportDetails(crashreport);
      crashreportcategory.setDetail("Server brand", () -> this.minecraft.player.getServerBrand());
      crashreportcategory.setDetail("Server type", () -> this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server");
      return crashreportcategory;
   }

   public void playSeededSound(@Nullable Player player, double d0, double d1, double d2, Holder<SoundEvent> holder, SoundSource soundsource, float f, float f1, long i) {
      if (player == this.minecraft.player) {
         this.playSound(d0, d1, d2, holder.value(), soundsource, f, f1, false, i);
      }

   }

   public void playSeededSound(@Nullable Player player, Entity entity, Holder<SoundEvent> holder, SoundSource soundsource, float f, float f1, long i) {
      if (player == this.minecraft.player) {
         this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(holder.value(), soundsource, f, f1, entity, i));
      }

   }

   public void playLocalSound(double d0, double d1, double d2, SoundEvent soundevent, SoundSource soundsource, float f, float f1, boolean flag) {
      this.playSound(d0, d1, d2, soundevent, soundsource, f, f1, flag, this.random.nextLong());
   }

   private void playSound(double d0, double d1, double d2, SoundEvent soundevent, SoundSource soundsource, float f, float f1, boolean flag, long i) {
      double d3 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(d0, d1, d2);
      SimpleSoundInstance simplesoundinstance = new SimpleSoundInstance(soundevent, soundsource, f, f1, RandomSource.create(i), d0, d1, d2);
      if (flag && d3 > 100.0D) {
         double d4 = Math.sqrt(d3) / 40.0D;
         this.minecraft.getSoundManager().playDelayed(simplesoundinstance, (int)(d4 * 20.0D));
      } else {
         this.minecraft.getSoundManager().play(simplesoundinstance);
      }

   }

   public void createFireworks(double d0, double d1, double d2, double d3, double d4, double d5, @Nullable CompoundTag compoundtag) {
      this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, d0, d1, d2, d3, d4, d5, this.minecraft.particleEngine, compoundtag));
   }

   public void sendPacketToServer(Packet<?> packet) {
      this.connection.send(packet);
   }

   public RecipeManager getRecipeManager() {
      return this.connection.getRecipeManager();
   }

   public void setScoreboard(Scoreboard scoreboard) {
      this.scoreboard = scoreboard;
   }

   public LevelTickAccess<Block> getBlockTicks() {
      return BlackholeTickAccess.emptyLevelList();
   }

   public LevelTickAccess<Fluid> getFluidTicks() {
      return BlackholeTickAccess.emptyLevelList();
   }

   public ClientChunkCache getChunkSource() {
      return this.chunkSource;
   }

   @Nullable
   public MapItemSavedData getMapData(String s) {
      return this.mapData.get(s);
   }

   public void overrideMapData(String s, MapItemSavedData mapitemsaveddata) {
      this.mapData.put(s, mapitemsaveddata);
   }

   public void setMapData(String s, MapItemSavedData mapitemsaveddata) {
   }

   public int getFreeMapId() {
      return 0;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public void sendBlockUpdated(BlockPos blockpos, BlockState blockstate, BlockState blockstate1, int i) {
      this.levelRenderer.blockChanged(this, blockpos, blockstate, blockstate1, i);
   }

   public void setBlocksDirty(BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
      this.levelRenderer.setBlockDirty(blockpos, blockstate, blockstate1);
   }

   public void setSectionDirtyWithNeighbors(int i, int j, int k) {
      this.levelRenderer.setSectionDirtyWithNeighbors(i, j, k);
   }

   public void destroyBlockProgress(int i, BlockPos blockpos, int j) {
      this.levelRenderer.destroyBlockProgress(i, blockpos, j);
   }

   public void globalLevelEvent(int i, BlockPos blockpos, int j) {
      this.levelRenderer.globalLevelEvent(i, blockpos, j);
   }

   public void levelEvent(@Nullable Player player, int i, BlockPos blockpos, int j) {
      try {
         this.levelRenderer.levelEvent(i, blockpos, j);
      } catch (Throwable var8) {
         CrashReport crashreport = CrashReport.forThrowable(var8, "Playing level event");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Level event being played");
         crashreportcategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, blockpos));
         crashreportcategory.setDetail("Event source", player);
         crashreportcategory.setDetail("Event type", i);
         crashreportcategory.setDetail("Event data", j);
         throw new ReportedException(crashreport);
      }
   }

   public void addParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
      this.levelRenderer.addParticle(particleoptions, particleoptions.getType().getOverrideLimiter(), d0, d1, d2, d3, d4, d5);
   }

   public void addParticle(ParticleOptions particleoptions, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {
      this.levelRenderer.addParticle(particleoptions, particleoptions.getType().getOverrideLimiter() || flag, d0, d1, d2, d3, d4, d5);
   }

   public void addAlwaysVisibleParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
      this.levelRenderer.addParticle(particleoptions, false, true, d0, d1, d2, d3, d4, d5);
   }

   public void addAlwaysVisibleParticle(ParticleOptions particleoptions, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {
      this.levelRenderer.addParticle(particleoptions, particleoptions.getType().getOverrideLimiter() || flag, true, d0, d1, d2, d3, d4, d5);
   }

   public List<AbstractClientPlayer> players() {
      return this.players;
   }

   public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
      return this.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
   }

   public float getSkyDarken(float f) {
      float f1 = this.getTimeOfDay(f);
      float f2 = 1.0F - (Mth.cos(f1 * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
      f2 = Mth.clamp(f2, 0.0F, 1.0F);
      f2 = 1.0F - f2;
      f2 *= 1.0F - this.getRainLevel(f) * 5.0F / 16.0F;
      f2 *= 1.0F - this.getThunderLevel(f) * 5.0F / 16.0F;
      return f2 * 0.8F + 0.2F;
   }

   public Vec3 getSkyColor(Vec3 vec3, float f) {
      float f1 = this.getTimeOfDay(f);
      Vec3 vec31 = vec3.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
      BiomeManager biomemanager = this.getBiomeManager();
      Vec3 vec32 = CubicSampler.gaussianSampleVec3(vec31, (j, k, l) -> Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(j, k, l).value().getSkyColor()));
      float f2 = Mth.cos(f1 * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f2 = Mth.clamp(f2, 0.0F, 1.0F);
      float f3 = (float)vec32.x * f2;
      float f4 = (float)vec32.y * f2;
      float f5 = (float)vec32.z * f2;
      float f6 = this.getRainLevel(f);
      if (f6 > 0.0F) {
         float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
         float f8 = 1.0F - f6 * 0.75F;
         f3 = f3 * f8 + f7 * (1.0F - f8);
         f4 = f4 * f8 + f7 * (1.0F - f8);
         f5 = f5 * f8 + f7 * (1.0F - f8);
      }

      float f9 = this.getThunderLevel(f);
      if (f9 > 0.0F) {
         float f10 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
         float f11 = 1.0F - f9 * 0.75F;
         f3 = f3 * f11 + f10 * (1.0F - f11);
         f4 = f4 * f11 + f10 * (1.0F - f11);
         f5 = f5 * f11 + f10 * (1.0F - f11);
      }

      int i = this.getSkyFlashTime();
      if (i > 0) {
         float f12 = (float)i - f;
         if (f12 > 1.0F) {
            f12 = 1.0F;
         }

         f12 *= 0.45F;
         f3 = f3 * (1.0F - f12) + 0.8F * f12;
         f4 = f4 * (1.0F - f12) + 0.8F * f12;
         f5 = f5 * (1.0F - f12) + 1.0F * f12;
      }

      return new Vec3((double)f3, (double)f4, (double)f5);
   }

   public Vec3 getCloudColor(float f) {
      float f1 = this.getTimeOfDay(f);
      float f2 = Mth.cos(f1 * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
      f2 = Mth.clamp(f2, 0.0F, 1.0F);
      float f3 = 1.0F;
      float f4 = 1.0F;
      float f5 = 1.0F;
      float f6 = this.getRainLevel(f);
      if (f6 > 0.0F) {
         float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
         float f8 = 1.0F - f6 * 0.95F;
         f3 = f3 * f8 + f7 * (1.0F - f8);
         f4 = f4 * f8 + f7 * (1.0F - f8);
         f5 = f5 * f8 + f7 * (1.0F - f8);
      }

      f3 *= f2 * 0.9F + 0.1F;
      f4 *= f2 * 0.9F + 0.1F;
      f5 *= f2 * 0.85F + 0.15F;
      float f9 = this.getThunderLevel(f);
      if (f9 > 0.0F) {
         float f10 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
         float f11 = 1.0F - f9 * 0.95F;
         f3 = f3 * f11 + f10 * (1.0F - f11);
         f4 = f4 * f11 + f10 * (1.0F - f11);
         f5 = f5 * f11 + f10 * (1.0F - f11);
      }

      return new Vec3((double)f3, (double)f4, (double)f5);
   }

   public float getStarBrightness(float f) {
      float f1 = this.getTimeOfDay(f);
      float f2 = 1.0F - (Mth.cos(f1 * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
      f2 = Mth.clamp(f2, 0.0F, 1.0F);
      return f2 * f2 * 0.5F;
   }

   public int getSkyFlashTime() {
      return this.minecraft.options.hideLightningFlash().get() ? 0 : this.skyFlashTime;
   }

   public void setSkyFlashTime(int i) {
      this.skyFlashTime = i;
   }

   public float getShade(Direction direction, boolean flag) {
      boolean flag1 = this.effects().constantAmbientLight();
      if (!flag) {
         return flag1 ? 0.9F : 1.0F;
      } else {
         switch (direction) {
            case DOWN:
               return flag1 ? 0.9F : 0.5F;
            case UP:
               return flag1 ? 0.9F : 1.0F;
            case NORTH:
            case SOUTH:
               return 0.8F;
            case WEST:
            case EAST:
               return 0.6F;
            default:
               return 1.0F;
         }
      }
   }

   public int getBlockTint(BlockPos blockpos, ColorResolver colorresolver) {
      BlockTintCache blocktintcache = this.tintCaches.get(colorresolver);
      return blocktintcache.getColor(blockpos);
   }

   public int calculateBlockTint(BlockPos blockpos, ColorResolver colorresolver) {
      int i = Minecraft.getInstance().options.biomeBlendRadius().get();
      if (i == 0) {
         return colorresolver.getColor(this.getBiome(blockpos).value(), (double)blockpos.getX(), (double)blockpos.getZ());
      } else {
         int j = (i * 2 + 1) * (i * 2 + 1);
         int k = 0;
         int l = 0;
         int i1 = 0;
         Cursor3D cursor3d = new Cursor3D(blockpos.getX() - i, blockpos.getY(), blockpos.getZ() - i, blockpos.getX() + i, blockpos.getY(), blockpos.getZ() + i);

         int j1;
         for(BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(); cursor3d.advance(); i1 += j1 & 255) {
            blockpos_mutableblockpos.set(cursor3d.nextX(), cursor3d.nextY(), cursor3d.nextZ());
            j1 = colorresolver.getColor(this.getBiome(blockpos_mutableblockpos).value(), (double)blockpos_mutableblockpos.getX(), (double)blockpos_mutableblockpos.getZ());
            k += (j1 & 16711680) >> 16;
            l += (j1 & '\uff00') >> 8;
         }

         return (k / j & 255) << 16 | (l / j & 255) << 8 | i1 / j & 255;
      }
   }

   public void setDefaultSpawnPos(BlockPos blockpos, float f) {
      this.levelData.setSpawn(blockpos, f);
   }

   public String toString() {
      return "ClientLevel";
   }

   public ClientLevel.ClientLevelData getLevelData() {
      return this.clientLevelData;
   }

   public void gameEvent(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context) {
   }

   protected Map<String, MapItemSavedData> getAllMapData() {
      return ImmutableMap.copyOf(this.mapData);
   }

   protected void addMapData(Map<String, MapItemSavedData> map) {
      this.mapData.putAll(map);
   }

   protected LevelEntityGetter<Entity> getEntities() {
      return this.entityStorage.getEntityGetter();
   }

   public String gatherChunkSourceStats() {
      return "Chunks[C] W: " + this.chunkSource.gatherStats() + " E: " + this.entityStorage.gatherStats();
   }

   public void addDestroyBlockEffect(BlockPos blockpos, BlockState blockstate) {
      this.minecraft.particleEngine.destroy(blockpos, blockstate);
   }

   public void setServerSimulationDistance(int i) {
      this.serverSimulationDistance = i;
   }

   public int getServerSimulationDistance() {
      return this.serverSimulationDistance;
   }

   public FeatureFlagSet enabledFeatures() {
      return this.connection.enabledFeatures();
   }

   public static class ClientLevelData implements WritableLevelData {
      private final boolean hardcore;
      private final GameRules gameRules;
      private final boolean isFlat;
      private int xSpawn;
      private int ySpawn;
      private int zSpawn;
      private float spawnAngle;
      private long gameTime;
      private long dayTime;
      private boolean raining;
      private Difficulty difficulty;
      private boolean difficultyLocked;

      public ClientLevelData(Difficulty difficulty, boolean flag, boolean flag1) {
         this.difficulty = difficulty;
         this.hardcore = flag;
         this.isFlat = flag1;
         this.gameRules = new GameRules();
      }

      public int getXSpawn() {
         return this.xSpawn;
      }

      public int getYSpawn() {
         return this.ySpawn;
      }

      public int getZSpawn() {
         return this.zSpawn;
      }

      public float getSpawnAngle() {
         return this.spawnAngle;
      }

      public long getGameTime() {
         return this.gameTime;
      }

      public long getDayTime() {
         return this.dayTime;
      }

      public void setXSpawn(int i) {
         this.xSpawn = i;
      }

      public void setYSpawn(int i) {
         this.ySpawn = i;
      }

      public void setZSpawn(int i) {
         this.zSpawn = i;
      }

      public void setSpawnAngle(float f) {
         this.spawnAngle = f;
      }

      public void setGameTime(long i) {
         this.gameTime = i;
      }

      public void setDayTime(long i) {
         this.dayTime = i;
      }

      public void setSpawn(BlockPos blockpos, float f) {
         this.xSpawn = blockpos.getX();
         this.ySpawn = blockpos.getY();
         this.zSpawn = blockpos.getZ();
         this.spawnAngle = f;
      }

      public boolean isThundering() {
         return false;
      }

      public boolean isRaining() {
         return this.raining;
      }

      public void setRaining(boolean flag) {
         this.raining = flag;
      }

      public boolean isHardcore() {
         return this.hardcore;
      }

      public GameRules getGameRules() {
         return this.gameRules;
      }

      public Difficulty getDifficulty() {
         return this.difficulty;
      }

      public boolean isDifficultyLocked() {
         return this.difficultyLocked;
      }

      public void fillCrashReportCategory(CrashReportCategory crashreportcategory, LevelHeightAccessor levelheightaccessor) {
         WritableLevelData.super.fillCrashReportCategory(crashreportcategory, levelheightaccessor);
      }

      public void setDifficulty(Difficulty difficulty) {
         this.difficulty = difficulty;
      }

      public void setDifficultyLocked(boolean flag) {
         this.difficultyLocked = flag;
      }

      public double getHorizonHeight(LevelHeightAccessor levelheightaccessor) {
         return this.isFlat ? (double)levelheightaccessor.getMinBuildHeight() : 63.0D;
      }

      public float getClearColorScale() {
         return this.isFlat ? 1.0F : 0.03125F;
      }
   }

   final class EntityCallbacks implements LevelCallback<Entity> {
      public void onCreated(Entity entity) {
      }

      public void onDestroyed(Entity entity) {
      }

      public void onTickingStart(Entity entity) {
         ClientLevel.this.tickingEntities.add(entity);
      }

      public void onTickingEnd(Entity entity) {
         ClientLevel.this.tickingEntities.remove(entity);
      }

      public void onTrackingStart(Entity entity) {
         if (entity instanceof AbstractClientPlayer) {
            ClientLevel.this.players.add((AbstractClientPlayer)entity);
         }

      }

      public void onTrackingEnd(Entity entity) {
         entity.unRide();
         ClientLevel.this.players.remove(entity);
      }

      public void onSectionChange(Entity entity) {
      }
   }
}
