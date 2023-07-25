package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class DebugScreenOverlay {
   private static final int COLOR_GREY = 14737632;
   private static final int MARGIN_RIGHT = 2;
   private static final int MARGIN_LEFT = 2;
   private static final int MARGIN_TOP = 2;
   private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Types.class), (enummap) -> {
      enummap.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
      enummap.put(Heightmap.Types.WORLD_SURFACE, "S");
      enummap.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
      enummap.put(Heightmap.Types.OCEAN_FLOOR, "O");
      enummap.put(Heightmap.Types.MOTION_BLOCKING, "M");
      enummap.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
   });
   private final Minecraft minecraft;
   private final DebugScreenOverlay.AllocationRateCalculator allocationRateCalculator;
   private final Font font;
   private HitResult block;
   private HitResult liquid;
   @Nullable
   private ChunkPos lastPos;
   @Nullable
   private LevelChunk clientChunk;
   @Nullable
   private CompletableFuture<LevelChunk> serverChunk;
   private static final int RED = -65536;
   private static final int YELLOW = -256;
   private static final int GREEN = -16711936;

   public DebugScreenOverlay(Minecraft minecraft) {
      this.minecraft = minecraft;
      this.allocationRateCalculator = new DebugScreenOverlay.AllocationRateCalculator();
      this.font = minecraft.font;
   }

   public void clearChunkCache() {
      this.serverChunk = null;
      this.clientChunk = null;
   }

   public void render(GuiGraphics guigraphics) {
      this.minecraft.getProfiler().push("debug");
      Entity entity = this.minecraft.getCameraEntity();
      this.block = entity.pick(20.0D, 0.0F, false);
      this.liquid = entity.pick(20.0D, 0.0F, true);
      guigraphics.drawManaged(() -> {
         this.drawGameInformation(guigraphics);
         this.drawSystemInformation(guigraphics);
         if (this.minecraft.options.renderFpsChart) {
            int i = guigraphics.guiWidth();
            this.drawChart(guigraphics, this.minecraft.getFrameTimer(), 0, i / 2, true);
            IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
            if (integratedserver != null) {
               this.drawChart(guigraphics, integratedserver.getFrameTimer(), i - Math.min(i / 2, 240), i / 2, false);
            }
         }

      });
      this.minecraft.getProfiler().pop();
   }

   protected void drawGameInformation(GuiGraphics guigraphics) {
      List<String> list = this.getGameInformation();
      list.add("");
      boolean flag = this.minecraft.getSingleplayerServer() != null;
      list.add("Debug: Pie [shift]: " + (this.minecraft.options.renderDebugCharts ? "visible" : "hidden") + (flag ? " FPS + TPS" : " FPS") + " [alt]: " + (this.minecraft.options.renderFpsChart ? "visible" : "hidden"));
      list.add("For help: press F3 + Q");
      this.renderLines(guigraphics, list, true);
   }

   protected void drawSystemInformation(GuiGraphics guigraphics) {
      List<String> list = this.getSystemInformation();
      this.renderLines(guigraphics, list, false);
   }

   private void renderLines(GuiGraphics guigraphics, List<String> list, boolean flag) {
      int i = 9;

      for(int j = 0; j < list.size(); ++j) {
         String s = list.get(j);
         if (!Strings.isNullOrEmpty(s)) {
            int k = this.font.width(s);
            int l = flag ? 2 : guigraphics.guiWidth() - 2 - k;
            int i1 = 2 + i * j;
            guigraphics.fill(l - 1, i1 - 1, l + k + 1, i1 + i - 1, -1873784752);
         }
      }

      for(int j1 = 0; j1 < list.size(); ++j1) {
         String s1 = list.get(j1);
         if (!Strings.isNullOrEmpty(s1)) {
            int k1 = this.font.width(s1);
            int l1 = flag ? 2 : guigraphics.guiWidth() - 2 - k1;
            int i2 = 2 + i * j1;
            guigraphics.drawString(this.font, s1, l1, i2, 14737632, false);
         }
      }

   }

   protected List<String> getGameInformation() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      Connection connection = this.minecraft.getConnection().getConnection();
      float f = connection.getAverageSentPackets();
      float f1 = connection.getAverageReceivedPackets();
      String s;
      if (integratedserver != null) {
         s = String.format(Locale.ROOT, "Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedserver.getAverageTickTime(), f, f1);
      } else {
         s = String.format(Locale.ROOT, "\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), f, f1);
      }

      BlockPos blockpos = this.minecraft.getCameraEntity().blockPosition();
      if (this.minecraft.showOnlyReducedInfo()) {
         return Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats(), "", String.format(Locale.ROOT, "Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
      } else {
         Entity entity = this.minecraft.getCameraEntity();
         Direction direction = entity.getDirection();
         String s2;
         switch (direction) {
            case NORTH:
               s2 = "Towards negative Z";
               break;
            case SOUTH:
               s2 = "Towards positive Z";
               break;
            case WEST:
               s2 = "Towards negative X";
               break;
            case EAST:
               s2 = "Towards positive X";
               break;
            default:
               s2 = "Invalid";
         }

         ChunkPos chunkpos = new ChunkPos(blockpos);
         if (!Objects.equals(this.lastPos, chunkpos)) {
            this.lastPos = chunkpos;
            this.clearChunkCache();
         }

         Level level = this.getLevel();
         LongSet longset = (LongSet)(level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET);
         List<String> list = Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats());
         String s7 = this.getServerChunkStats();
         if (s7 != null) {
            list.add(s7);
         }

         list.add(this.minecraft.level.dimension().location() + " FC: " + longset.size());
         list.add("");
         list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY(), this.minecraft.getCameraEntity().getZ()));
         list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
         list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkpos.x, SectionPos.blockToSectionCoord(blockpos.getY()), chunkpos.z, chunkpos.getRegionLocalX(), chunkpos.getRegionLocalZ(), chunkpos.getRegionX(), chunkpos.getRegionZ()));
         list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, s2, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot())));
         LevelChunk levelchunk = this.getClientChunk();
         if (levelchunk.isEmpty()) {
            list.add("Waiting for chunk...");
         } else {
            int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
            int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockpos);
            int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockpos);
            list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
            LevelChunk levelchunk1 = this.getServerChunk();
            StringBuilder stringbuilder = new StringBuilder("CH");

            for(Heightmap.Types heightmap_types : Heightmap.Types.values()) {
               if (heightmap_types.sendToClient()) {
                  stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap_types)).append(": ").append(levelchunk.getHeight(heightmap_types, blockpos.getX(), blockpos.getZ()));
               }
            }

            list.add(stringbuilder.toString());
            stringbuilder.setLength(0);
            stringbuilder.append("SH");

            for(Heightmap.Types heightmap_types1 : Heightmap.Types.values()) {
               if (heightmap_types1.keepAfterWorldgen()) {
                  stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap_types1)).append(": ");
                  if (levelchunk1 != null) {
                     stringbuilder.append(levelchunk1.getHeight(heightmap_types1, blockpos.getX(), blockpos.getZ()));
                  } else {
                     stringbuilder.append("??");
                  }
               }
            }

            list.add(stringbuilder.toString());
            if (blockpos.getY() >= this.minecraft.level.getMinBuildHeight() && blockpos.getY() < this.minecraft.level.getMaxBuildHeight()) {
               list.add("Biome: " + printBiome(this.minecraft.level.getBiome(blockpos)));
               long l = 0L;
               float f2 = 0.0F;
               if (levelchunk1 != null) {
                  f2 = level.getMoonBrightness();
                  l = levelchunk1.getInhabitedTime();
               }

               DifficultyInstance difficultyinstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, f2);
               list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getEffectiveDifficulty(), difficultyinstance.getSpecialMultiplier(), this.minecraft.level.getDayTime() / 24000L));
            }

            if (levelchunk1 != null && levelchunk1.isOldNoiseGeneration()) {
               list.add("Blending: Old");
            }
         }

         ServerLevel serverlevel = this.getServerLevel();
         if (serverlevel != null) {
            ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
            ChunkGenerator chunkgenerator = serverchunkcache.getGenerator();
            RandomState randomstate = serverchunkcache.randomState();
            chunkgenerator.addDebugScreenInfo(list, randomstate, blockpos);
            Climate.Sampler climate_sampler = randomstate.sampler();
            BiomeSource biomesource = chunkgenerator.getBiomeSource();
            biomesource.addDebugInfo(list, blockpos, climate_sampler);
            NaturalSpawner.SpawnState naturalspawner_spawnstate = serverchunkcache.getLastSpawnState();
            if (naturalspawner_spawnstate != null) {
               Object2IntMap<MobCategory> object2intmap = naturalspawner_spawnstate.getMobCategoryCounts();
               int i1 = naturalspawner_spawnstate.getSpawnableChunkCount();
               list.add("SC: " + i1 + ", " + (String)Stream.of(MobCategory.values()).map((mobcategory) -> Character.toUpperCase(mobcategory.getName().charAt(0)) + ": " + object2intmap.getInt(mobcategory)).collect(Collectors.joining(", ")));
            } else {
               list.add("SC: N/A");
            }
         }

         PostChain postchain = this.minecraft.gameRenderer.currentEffect();
         if (postchain != null) {
            list.add("Shader: " + postchain.getName());
         }

         list.add(this.minecraft.getSoundManager().getDebugString() + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F)));
         return list;
      }
   }

   private static String printBiome(Holder<Biome> holder) {
      return holder.unwrap().map((resourcekey) -> resourcekey.location().toString(), (biome) -> "[unregistered " + biome + "]");
   }

   @Nullable
   private ServerLevel getServerLevel() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      return integratedserver != null ? integratedserver.getLevel(this.minecraft.level.dimension()) : null;
   }

   @Nullable
   private String getServerChunkStats() {
      ServerLevel serverlevel = this.getServerLevel();
      return serverlevel != null ? serverlevel.gatherChunkSourceStats() : null;
   }

   private Level getLevel() {
      return DataFixUtils.orElse(Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap((integratedserver) -> Optional.ofNullable(integratedserver.getLevel(this.minecraft.level.dimension()))), this.minecraft.level);
   }

   @Nullable
   private LevelChunk getServerChunk() {
      if (this.serverChunk == null) {
         ServerLevel serverlevel = this.getServerLevel();
         if (serverlevel != null) {
            this.serverChunk = serverlevel.getChunkSource().getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false).thenApply((either) -> either.map((chunkaccess) -> (LevelChunk)chunkaccess, (chunkholder_chunkloadingfailure) -> null));
         }

         if (this.serverChunk == null) {
            this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
         }
      }

      return this.serverChunk.getNow((LevelChunk)null);
   }

   private LevelChunk getClientChunk() {
      if (this.clientChunk == null) {
         this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
      }

      return this.clientChunk;
   }

   protected List<String> getSystemInformation() {
      long i = Runtime.getRuntime().maxMemory();
      long j = Runtime.getRuntime().totalMemory();
      long k = Runtime.getRuntime().freeMemory();
      long l = j - k;
      List<String> list = Lists.newArrayList(String.format(Locale.ROOT, "Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32), String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)), String.format(Locale.ROOT, "Allocation rate: %03dMB /s", bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(l))), String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)), "", String.format(Locale.ROOT, "CPU: %s", GlUtil.getCpuInfo()), "", String.format(Locale.ROOT, "Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()), GlUtil.getRenderer(), GlUtil.getOpenGLVersion());
      if (this.minecraft.showOnlyReducedInfo()) {
         return list;
      } else {
         if (this.block.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)this.block).getBlockPos();
            BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
            list.add(String.valueOf((Object)BuiltInRegistries.BLOCK.getKey(blockstate.getBlock())));

            for(Map.Entry<Property<?>, Comparable<?>> map_entry : blockstate.getValues().entrySet()) {
               list.add(this.getPropertyValueString(map_entry));
            }

            blockstate.getTags().map((tagkey1) -> "#" + tagkey1.location()).forEach(list::add);
         }

         if (this.liquid.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos1 = ((BlockHitResult)this.liquid).getBlockPos();
            FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
            list.add(String.valueOf((Object)BuiltInRegistries.FLUID.getKey(fluidstate.getType())));

            for(Map.Entry<Property<?>, Comparable<?>> map_entry1 : fluidstate.getValues().entrySet()) {
               list.add(this.getPropertyValueString(map_entry1));
            }

            fluidstate.getTags().map((tagkey) -> "#" + tagkey.location()).forEach(list::add);
         }

         Entity entity = this.minecraft.crosshairPickEntity;
         if (entity != null) {
            list.add("");
            list.add(ChatFormatting.UNDERLINE + "Targeted Entity");
            list.add(String.valueOf((Object)BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())));
         }

         return list;
      }
   }

   private String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> map_entry) {
      Property<?> property = map_entry.getKey();
      Comparable<?> comparable = map_entry.getValue();
      String s = Util.getPropertyName(property, comparable);
      if (Boolean.TRUE.equals(comparable)) {
         s = ChatFormatting.GREEN + s;
      } else if (Boolean.FALSE.equals(comparable)) {
         s = ChatFormatting.RED + s;
      }

      return property.getName() + ": " + s;
   }

   private void drawChart(GuiGraphics guigraphics, FrameTimer frametimer, int i, int j, boolean flag) {
      int k = frametimer.getLogStart();
      int l = frametimer.getLogEnd();
      long[] along = frametimer.getLog();
      int j1 = i;
      int k1 = Math.max(0, along.length - j);
      int l1 = along.length - k1;
      int i1 = frametimer.wrapIndex(k + k1);
      long i2 = 0L;
      int j2 = Integer.MAX_VALUE;
      int k2 = Integer.MIN_VALUE;

      for(int l2 = 0; l2 < l1; ++l2) {
         int i3 = (int)(along[frametimer.wrapIndex(i1 + l2)] / 1000000L);
         j2 = Math.min(j2, i3);
         k2 = Math.max(k2, i3);
         i2 += (long)i3;
      }

      int j3 = guigraphics.guiHeight();
      guigraphics.fill(RenderType.guiOverlay(), i, j3 - 60, i + l1, j3, -1873784752);

      while(i1 != l) {
         int k3 = frametimer.scaleSampleTo(along[i1], flag ? 30 : 60, flag ? 60 : 20);
         int l3 = flag ? 100 : 60;
         int i4 = this.getSampleColor(Mth.clamp(k3, 0, l3), 0, l3 / 2, l3);
         guigraphics.fill(RenderType.guiOverlay(), j1, j3 - k3, j1 + 1, j3, i4);
         ++j1;
         i1 = frametimer.wrapIndex(i1 + 1);
      }

      if (flag) {
         guigraphics.fill(RenderType.guiOverlay(), i + 1, j3 - 30 + 1, i + 14, j3 - 30 + 10, -1873784752);
         guigraphics.drawString(this.font, "60 FPS", i + 2, j3 - 30 + 2, 14737632, false);
         guigraphics.hLine(RenderType.guiOverlay(), i, i + l1 - 1, j3 - 30, -1);
         guigraphics.fill(RenderType.guiOverlay(), i + 1, j3 - 60 + 1, i + 14, j3 - 60 + 10, -1873784752);
         guigraphics.drawString(this.font, "30 FPS", i + 2, j3 - 60 + 2, 14737632, false);
         guigraphics.hLine(RenderType.guiOverlay(), i, i + l1 - 1, j3 - 60, -1);
      } else {
         guigraphics.fill(RenderType.guiOverlay(), i + 1, j3 - 60 + 1, i + 14, j3 - 60 + 10, -1873784752);
         guigraphics.drawString(this.font, "20 TPS", i + 2, j3 - 60 + 2, 14737632, false);
         guigraphics.hLine(RenderType.guiOverlay(), i, i + l1 - 1, j3 - 60, -1);
      }

      guigraphics.hLine(RenderType.guiOverlay(), i, i + l1 - 1, j3 - 1, -1);
      guigraphics.vLine(RenderType.guiOverlay(), i, j3 - 60, j3, -1);
      guigraphics.vLine(RenderType.guiOverlay(), i + l1 - 1, j3 - 60, j3, -1);
      int j4 = this.minecraft.options.framerateLimit().get();
      if (flag && j4 > 0 && j4 <= 250) {
         guigraphics.hLine(RenderType.guiOverlay(), i, i + l1 - 1, j3 - 1 - (int)(1800.0D / (double)j4), -16711681);
      }

      String s = j2 + " ms min";
      String s1 = i2 / (long)l1 + " ms avg";
      String s2 = k2 + " ms max";
      guigraphics.drawString(this.font, s, i + 2, j3 - 60 - 9, 14737632);
      guigraphics.drawCenteredString(this.font, s1, i + l1 / 2, j3 - 60 - 9, 14737632);
      guigraphics.drawString(this.font, s2, i + l1 - this.font.width(s2), j3 - 60 - 9, 14737632);
   }

   private int getSampleColor(int i, int j, int k, int l) {
      return i < k ? this.colorLerp(-16711936, -256, (float)i / (float)k) : this.colorLerp(-256, -65536, (float)(i - k) / (float)(l - k));
   }

   private int colorLerp(int i, int j, float f) {
      int k = i >> 24 & 255;
      int l = i >> 16 & 255;
      int i1 = i >> 8 & 255;
      int j1 = i & 255;
      int k1 = j >> 24 & 255;
      int l1 = j >> 16 & 255;
      int i2 = j >> 8 & 255;
      int j2 = j & 255;
      int k2 = Mth.clamp((int)Mth.lerp(f, (float)k, (float)k1), 0, 255);
      int l2 = Mth.clamp((int)Mth.lerp(f, (float)l, (float)l1), 0, 255);
      int i3 = Mth.clamp((int)Mth.lerp(f, (float)i1, (float)i2), 0, 255);
      int j3 = Mth.clamp((int)Mth.lerp(f, (float)j1, (float)j2), 0, 255);
      return k2 << 24 | l2 << 16 | i3 << 8 | j3;
   }

   private static long bytesToMegabytes(long i) {
      return i / 1024L / 1024L;
   }

   static class AllocationRateCalculator {
      private static final int UPDATE_INTERVAL_MS = 500;
      private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
      private long lastTime = 0L;
      private long lastHeapUsage = -1L;
      private long lastGcCounts = -1L;
      private long lastRate = 0L;

      long bytesAllocatedPerSecond(long i) {
         long j = System.currentTimeMillis();
         if (j - this.lastTime < 500L) {
            return this.lastRate;
         } else {
            long k = gcCounts();
            if (this.lastTime != 0L && k == this.lastGcCounts) {
               double d0 = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(j - this.lastTime);
               long l = i - this.lastHeapUsage;
               this.lastRate = Math.round((double)l * d0);
            }

            this.lastTime = j;
            this.lastHeapUsage = i;
            this.lastGcCounts = k;
            return this.lastRate;
         }
      }

      private static long gcCounts() {
         long i = 0L;

         for(GarbageCollectorMXBean garbagecollectormxbean : GC_MBEANS) {
            i += garbagecollectormxbean.getCollectionCount();
         }

         return i;
      }
   }
}
