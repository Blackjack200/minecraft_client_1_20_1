package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.slf4j.Logger;

public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int CHUNK_SIZE = 16;
   private static final int HALF_CHUNK_SIZE = 8;
   private static final float SKY_DISC_RADIUS = 512.0F;
   private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
   private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0D) * 16.0D);
   private static final int MIN_FOG_DISTANCE = 32;
   private static final int RAIN_RADIUS = 10;
   private static final int RAIN_DIAMETER = 21;
   private static final int TRANSPARENT_SORT_COUNT = 15;
   private static final int HALF_A_SECOND_IN_MILLIS = 500;
   private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
   private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
   private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
   private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
   private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
   private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
   public static final Direction[] DIRECTIONS = Direction.values();
   private final Minecraft minecraft;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
   private final RenderBuffers renderBuffers;
   @Nullable
   private ClientLevel level;
   private final BlockingQueue<ChunkRenderDispatcher.RenderChunk> recentlyCompiledChunks = new LinkedBlockingQueue<>();
   private final AtomicReference<LevelRenderer.RenderChunkStorage> renderChunkStorage = new AtomicReference<>();
   private final ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum = new ObjectArrayList<>(10000);
   private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
   @Nullable
   private Future<?> lastFullRenderChunkUpdate;
   @Nullable
   private ViewArea viewArea;
   @Nullable
   private VertexBuffer starBuffer;
   @Nullable
   private VertexBuffer skyBuffer;
   @Nullable
   private VertexBuffer darkBuffer;
   private boolean generateClouds = true;
   @Nullable
   private VertexBuffer cloudBuffer;
   private final RunningTrimmedMean frameTimes = new RunningTrimmedMean(100);
   private int ticks;
   private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
   private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
   private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
   @Nullable
   private RenderTarget entityTarget;
   @Nullable
   private PostChain entityEffect;
   @Nullable
   private RenderTarget translucentTarget;
   @Nullable
   private RenderTarget itemEntityTarget;
   @Nullable
   private RenderTarget particlesTarget;
   @Nullable
   private RenderTarget weatherTarget;
   @Nullable
   private RenderTarget cloudsTarget;
   @Nullable
   private PostChain transparencyChain;
   private double lastCameraX = Double.MIN_VALUE;
   private double lastCameraY = Double.MIN_VALUE;
   private double lastCameraZ = Double.MIN_VALUE;
   private int lastCameraChunkX = Integer.MIN_VALUE;
   private int lastCameraChunkY = Integer.MIN_VALUE;
   private int lastCameraChunkZ = Integer.MIN_VALUE;
   private double prevCamX = Double.MIN_VALUE;
   private double prevCamY = Double.MIN_VALUE;
   private double prevCamZ = Double.MIN_VALUE;
   private double prevCamRotX = Double.MIN_VALUE;
   private double prevCamRotY = Double.MIN_VALUE;
   private int prevCloudX = Integer.MIN_VALUE;
   private int prevCloudY = Integer.MIN_VALUE;
   private int prevCloudZ = Integer.MIN_VALUE;
   private Vec3 prevCloudColor = Vec3.ZERO;
   @Nullable
   private CloudStatus prevCloudsType;
   @Nullable
   private ChunkRenderDispatcher chunkRenderDispatcher;
   private int lastViewDistance = -1;
   private int renderedEntities;
   private int culledEntities;
   private Frustum cullingFrustum;
   private boolean captureFrustum;
   @Nullable
   private Frustum capturedFrustum;
   private final Vector4f[] frustumPoints = new Vector4f[8];
   private final Vector3d frustumPos = new Vector3d(0.0D, 0.0D, 0.0D);
   private double xTransparentOld;
   private double yTransparentOld;
   private double zTransparentOld;
   private boolean needsFullRenderChunkUpdate = true;
   private final AtomicLong nextFullUpdateMillis = new AtomicLong(0L);
   private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);
   private int rainSoundTime;
   private final float[] rainSizeX = new float[1024];
   private final float[] rainSizeZ = new float[1024];

   public LevelRenderer(Minecraft minecraft, EntityRenderDispatcher entityrenderdispatcher, BlockEntityRenderDispatcher blockentityrenderdispatcher, RenderBuffers renderbuffers) {
      this.minecraft = minecraft;
      this.entityRenderDispatcher = entityrenderdispatcher;
      this.blockEntityRenderDispatcher = blockentityrenderdispatcher;
      this.renderBuffers = renderbuffers;

      for(int i = 0; i < 32; ++i) {
         for(int j = 0; j < 32; ++j) {
            float f = (float)(j - 16);
            float f1 = (float)(i - 16);
            float f2 = Mth.sqrt(f * f + f1 * f1);
            this.rainSizeX[i << 5 | j] = -f1 / f2;
            this.rainSizeZ[i << 5 | j] = f / f2;
         }
      }

      this.createStars();
      this.createLightSky();
      this.createDarkSky();
   }

   private void renderSnowAndRain(LightTexture lighttexture, float f, double d0, double d1, double d2) {
      float f1 = this.minecraft.level.getRainLevel(f);
      if (!(f1 <= 0.0F)) {
         lighttexture.turnOnLightLayer();
         Level level = this.minecraft.level;
         int i = Mth.floor(d0);
         int j = Mth.floor(d1);
         int k = Mth.floor(d2);
         Tesselator tesselator = Tesselator.getInstance();
         BufferBuilder bufferbuilder = tesselator.getBuilder();
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         int l = 5;
         if (Minecraft.useFancyGraphics()) {
            l = 10;
         }

         RenderSystem.depthMask(Minecraft.useShaderTransparency());
         int i1 = -1;
         float f2 = (float)this.ticks + f;
         RenderSystem.setShader(GameRenderer::getParticleShader);
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int j1 = k - l; j1 <= k + l; ++j1) {
            for(int k1 = i - l; k1 <= i + l; ++k1) {
               int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
               double d3 = (double)this.rainSizeX[l1] * 0.5D;
               double d4 = (double)this.rainSizeZ[l1] * 0.5D;
               blockpos_mutableblockpos.set((double)k1, d1, (double)j1);
               Biome biome = level.getBiome(blockpos_mutableblockpos).value();
               if (biome.hasPrecipitation()) {
                  int i2 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, k1, j1);
                  int j2 = j - l;
                  int k2 = j + l;
                  if (j2 < i2) {
                     j2 = i2;
                  }

                  if (k2 < i2) {
                     k2 = i2;
                  }

                  int l2 = i2;
                  if (i2 < j) {
                     l2 = j;
                  }

                  if (j2 != k2) {
                     RandomSource randomsource = RandomSource.create((long)(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761));
                     blockpos_mutableblockpos.set(k1, j2, j1);
                     Biome.Precipitation biome_precipitation = biome.getPrecipitationAt(blockpos_mutableblockpos);
                     if (biome_precipitation == Biome.Precipitation.RAIN) {
                        if (i1 != 0) {
                           if (i1 >= 0) {
                              tesselator.end();
                           }

                           i1 = 0;
                           RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                           bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                        }

                        int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                        float f3 = -((float)i3 + f) / 32.0F * (3.0F + randomsource.nextFloat());
                        double d5 = (double)k1 + 0.5D - d0;
                        double d6 = (double)j1 + 0.5D - d2;
                        float f4 = (float)Math.sqrt(d5 * d5 + d6 * d6) / (float)l;
                        float f5 = ((1.0F - f4 * f4) * 0.5F + 0.5F) * f1;
                        blockpos_mutableblockpos.set(k1, l2, j1);
                        int j3 = getLightColor(level, blockpos_mutableblockpos);
                        bufferbuilder.vertex((double)k1 - d0 - d3 + 0.5D, (double)k2 - d1, (double)j1 - d2 - d4 + 0.5D).uv(0.0F, (float)j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
                        bufferbuilder.vertex((double)k1 - d0 + d3 + 0.5D, (double)k2 - d1, (double)j1 - d2 + d4 + 0.5D).uv(1.0F, (float)j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
                        bufferbuilder.vertex((double)k1 - d0 + d3 + 0.5D, (double)j2 - d1, (double)j1 - d2 + d4 + 0.5D).uv(1.0F, (float)k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
                        bufferbuilder.vertex((double)k1 - d0 - d3 + 0.5D, (double)j2 - d1, (double)j1 - d2 - d4 + 0.5D).uv(0.0F, (float)k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).uv2(j3).endVertex();
                     } else if (biome_precipitation == Biome.Precipitation.SNOW) {
                        if (i1 != 1) {
                           if (i1 >= 0) {
                              tesselator.end();
                           }

                           i1 = 1;
                           RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                           bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                        }

                        float f6 = -((float)(this.ticks & 511) + f) / 512.0F;
                        float f7 = (float)(randomsource.nextDouble() + (double)f2 * 0.01D * (double)((float)randomsource.nextGaussian()));
                        float f8 = (float)(randomsource.nextDouble() + (double)(f2 * (float)randomsource.nextGaussian()) * 0.001D);
                        double d7 = (double)k1 + 0.5D - d0;
                        double d8 = (double)j1 + 0.5D - d2;
                        float f9 = (float)Math.sqrt(d7 * d7 + d8 * d8) / (float)l;
                        float f10 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * f1;
                        blockpos_mutableblockpos.set(k1, l2, j1);
                        int k3 = getLightColor(level, blockpos_mutableblockpos);
                        int l3 = k3 >> 16 & '\uffff';
                        int i4 = k3 & '\uffff';
                        int j4 = (l3 * 3 + 240) / 4;
                        int k4 = (i4 * 3 + 240) / 4;
                        bufferbuilder.vertex((double)k1 - d0 - d3 + 0.5D, (double)k2 - d1, (double)j1 - d2 - d4 + 0.5D).uv(0.0F + f7, (float)j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                        bufferbuilder.vertex((double)k1 - d0 + d3 + 0.5D, (double)k2 - d1, (double)j1 - d2 + d4 + 0.5D).uv(1.0F + f7, (float)j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                        bufferbuilder.vertex((double)k1 - d0 + d3 + 0.5D, (double)j2 - d1, (double)j1 - d2 + d4 + 0.5D).uv(1.0F + f7, (float)k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                        bufferbuilder.vertex((double)k1 - d0 - d3 + 0.5D, (double)j2 - d1, (double)j1 - d2 - d4 + 0.5D).uv(0.0F + f7, (float)k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                     }
                  }
               }
            }
         }

         if (i1 >= 0) {
            tesselator.end();
         }

         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         lighttexture.turnOffLightLayer();
      }
   }

   public void tickRain(Camera camera) {
      float f = this.minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
      if (!(f <= 0.0F)) {
         RandomSource randomsource = RandomSource.create((long)this.ticks * 312987231L);
         LevelReader levelreader = this.minecraft.level;
         BlockPos blockpos = BlockPos.containing(camera.getPosition());
         BlockPos blockpos1 = null;
         int i = (int)(100.0F * f * f) / (this.minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

         for(int j = 0; j < i; ++j) {
            int k = randomsource.nextInt(21) - 10;
            int l = randomsource.nextInt(21) - 10;
            BlockPos blockpos2 = levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos.offset(k, 0, l));
            if (blockpos2.getY() > levelreader.getMinBuildHeight() && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10) {
               Biome biome = levelreader.getBiome(blockpos2).value();
               if (biome.getPrecipitationAt(blockpos2) == Biome.Precipitation.RAIN) {
                  blockpos1 = blockpos2.below();
                  if (this.minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
                     break;
                  }

                  double d0 = randomsource.nextDouble();
                  double d1 = randomsource.nextDouble();
                  BlockState blockstate = levelreader.getBlockState(blockpos1);
                  FluidState fluidstate = levelreader.getFluidState(blockpos1);
                  VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, blockpos1);
                  double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                  double d3 = (double)fluidstate.getHeight(levelreader, blockpos1);
                  double d4 = Math.max(d2, d3);
                  ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                  this.minecraft.level.addParticle(particleoptions, (double)blockpos1.getX() + d0, (double)blockpos1.getY() + d4, (double)blockpos1.getZ() + d1, 0.0D, 0.0D, 0.0D);
               }
            }
         }

         if (blockpos1 != null && randomsource.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (blockpos1.getY() > blockpos.getY() + 1 && levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > Mth.floor((float)blockpos.getY())) {
               this.minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
            } else {
               this.minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
            }
         }

      }
   }

   public void close() {
      if (this.entityEffect != null) {
         this.entityEffect.close();
      }

      if (this.transparencyChain != null) {
         this.transparencyChain.close();
      }

   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      this.initOutline();
      if (Minecraft.useShaderTransparency()) {
         this.initTransparency();
      }

   }

   public void initOutline() {
      if (this.entityEffect != null) {
         this.entityEffect.close();
      }

      ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

      try {
         this.entityEffect = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourcelocation);
         this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         this.entityTarget = this.entityEffect.getTempTarget("final");
      } catch (IOException var3) {
         LOGGER.warn("Failed to load shader: {}", resourcelocation, var3);
         this.entityEffect = null;
         this.entityTarget = null;
      } catch (JsonSyntaxException var4) {
         LOGGER.warn("Failed to parse shader: {}", resourcelocation, var4);
         this.entityEffect = null;
         this.entityTarget = null;
      }

   }

   private void initTransparency() {
      this.deinitTransparency();
      ResourceLocation resourcelocation = new ResourceLocation("shaders/post/transparency.json");

      try {
         PostChain postchain = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourcelocation);
         postchain.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         RenderTarget rendertarget = postchain.getTempTarget("translucent");
         RenderTarget rendertarget1 = postchain.getTempTarget("itemEntity");
         RenderTarget rendertarget2 = postchain.getTempTarget("particles");
         RenderTarget rendertarget3 = postchain.getTempTarget("weather");
         RenderTarget rendertarget4 = postchain.getTempTarget("clouds");
         this.transparencyChain = postchain;
         this.translucentTarget = rendertarget;
         this.itemEntityTarget = rendertarget1;
         this.particlesTarget = rendertarget2;
         this.weatherTarget = rendertarget3;
         this.cloudsTarget = rendertarget4;
      } catch (Exception var8) {
         String s = var8 instanceof JsonSyntaxException ? "parse" : "load";
         String s1 = "Failed to " + s + " shader: " + resourcelocation;
         LevelRenderer.TransparencyShaderException levelrenderer_transparencyshaderexception = new LevelRenderer.TransparencyShaderException(s1, var8);
         if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1) {
            Component component = this.minecraft.getResourceManager().listPacks().findFirst().map((packresources) -> Component.literal(packresources.packId())).orElse((MutableComponent)null);
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
            this.minecraft.clearResourcePacksOnError(levelrenderer_transparencyshaderexception, component);
         } else {
            CrashReport crashreport = this.minecraft.fillReport(new CrashReport(s1, levelrenderer_transparencyshaderexception));
            this.minecraft.options.graphicsMode().set(GraphicsStatus.FANCY);
            this.minecraft.options.save();
            LOGGER.error(LogUtils.FATAL_MARKER, s1, (Throwable)levelrenderer_transparencyshaderexception);
            this.minecraft.emergencySave();
            Minecraft.crash(crashreport);
         }
      }

   }

   private void deinitTransparency() {
      if (this.transparencyChain != null) {
         this.transparencyChain.close();
         this.translucentTarget.destroyBuffers();
         this.itemEntityTarget.destroyBuffers();
         this.particlesTarget.destroyBuffers();
         this.weatherTarget.destroyBuffers();
         this.cloudsTarget.destroyBuffers();
         this.transparencyChain = null;
         this.translucentTarget = null;
         this.itemEntityTarget = null;
         this.particlesTarget = null;
         this.weatherTarget = null;
         this.cloudsTarget = null;
      }

   }

   public void doEntityOutline() {
      if (this.shouldShowEntityOutlines()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         this.entityTarget.blitToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), false);
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }

   }

   protected boolean shouldShowEntityOutlines() {
      return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
   }

   private void createDarkSky() {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      if (this.darkBuffer != null) {
         this.darkBuffer.close();
      }

      this.darkBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
      BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = buildSkyDisc(bufferbuilder, -16.0F);
      this.darkBuffer.bind();
      this.darkBuffer.upload(bufferbuilder_renderedbuffer);
      VertexBuffer.unbind();
   }

   private void createLightSky() {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      if (this.skyBuffer != null) {
         this.skyBuffer.close();
      }

      this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
      BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = buildSkyDisc(bufferbuilder, 16.0F);
      this.skyBuffer.bind();
      this.skyBuffer.upload(bufferbuilder_renderedbuffer);
      VertexBuffer.unbind();
   }

   private static BufferBuilder.RenderedBuffer buildSkyDisc(BufferBuilder bufferbuilder, float f) {
      float f1 = Math.signum(f) * 512.0F;
      float f2 = 512.0F;
      RenderSystem.setShader(GameRenderer::getPositionShader);
      bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
      bufferbuilder.vertex(0.0D, (double)f, 0.0D).endVertex();

      for(int i = -180; i <= 180; i += 45) {
         bufferbuilder.vertex((double)(f1 * Mth.cos((float)i * ((float)Math.PI / 180F))), (double)f, (double)(512.0F * Mth.sin((float)i * ((float)Math.PI / 180F)))).endVertex();
      }

      return bufferbuilder.end();
   }

   private void createStars() {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionShader);
      if (this.starBuffer != null) {
         this.starBuffer.close();
      }

      this.starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
      BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = this.drawStars(bufferbuilder);
      this.starBuffer.bind();
      this.starBuffer.upload(bufferbuilder_renderedbuffer);
      VertexBuffer.unbind();
   }

   private BufferBuilder.RenderedBuffer drawStars(BufferBuilder bufferbuilder) {
      RandomSource randomsource = RandomSource.create(10842L);
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

      for(int i = 0; i < 1500; ++i) {
         double d0 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
         double d1 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
         double d2 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
         double d3 = (double)(0.15F + randomsource.nextFloat() * 0.1F);
         double d4 = d0 * d0 + d1 * d1 + d2 * d2;
         if (d4 < 1.0D && d4 > 0.01D) {
            d4 = 1.0D / Math.sqrt(d4);
            d0 *= d4;
            d1 *= d4;
            d2 *= d4;
            double d5 = d0 * 100.0D;
            double d6 = d1 * 100.0D;
            double d7 = d2 * 100.0D;
            double d8 = Math.atan2(d0, d2);
            double d9 = Math.sin(d8);
            double d10 = Math.cos(d8);
            double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
            double d12 = Math.sin(d11);
            double d13 = Math.cos(d11);
            double d14 = randomsource.nextDouble() * Math.PI * 2.0D;
            double d15 = Math.sin(d14);
            double d16 = Math.cos(d14);

            for(int j = 0; j < 4; ++j) {
               double d17 = 0.0D;
               double d18 = (double)((j & 2) - 1) * d3;
               double d19 = (double)((j + 1 & 2) - 1) * d3;
               double d20 = 0.0D;
               double d21 = d18 * d16 - d19 * d15;
               double d22 = d19 * d16 + d18 * d15;
               double d24 = d21 * d12 + 0.0D * d13;
               double d25 = 0.0D * d12 - d21 * d13;
               double d26 = d25 * d9 - d22 * d10;
               double d28 = d22 * d9 + d25 * d10;
               bufferbuilder.vertex(d5 + d26, d6 + d24, d7 + d28).endVertex();
            }
         }
      }

      return bufferbuilder.end();
   }

   public void setLevel(@Nullable ClientLevel clientlevel) {
      this.lastCameraX = Double.MIN_VALUE;
      this.lastCameraY = Double.MIN_VALUE;
      this.lastCameraZ = Double.MIN_VALUE;
      this.lastCameraChunkX = Integer.MIN_VALUE;
      this.lastCameraChunkY = Integer.MIN_VALUE;
      this.lastCameraChunkZ = Integer.MIN_VALUE;
      this.entityRenderDispatcher.setLevel(clientlevel);
      this.level = clientlevel;
      if (clientlevel != null) {
         this.allChanged();
      } else {
         if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
            this.viewArea = null;
         }

         if (this.chunkRenderDispatcher != null) {
            this.chunkRenderDispatcher.dispose();
         }

         this.chunkRenderDispatcher = null;
         this.globalBlockEntities.clear();
         this.renderChunkStorage.set((LevelRenderer.RenderChunkStorage)null);
         this.renderChunksInFrustum.clear();
      }

   }

   public void graphicsChanged() {
      if (Minecraft.useShaderTransparency()) {
         this.initTransparency();
      } else {
         this.deinitTransparency();
      }

   }

   public void allChanged() {
      if (this.level != null) {
         this.graphicsChanged();
         this.level.clearTintCaches();
         if (this.chunkRenderDispatcher == null) {
            this.chunkRenderDispatcher = new ChunkRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack());
         } else {
            this.chunkRenderDispatcher.setLevel(this.level);
         }

         this.needsFullRenderChunkUpdate = true;
         this.generateClouds = true;
         this.recentlyCompiledChunks.clear();
         ItemBlockRenderTypes.setFancy(Minecraft.useFancyGraphics());
         this.lastViewDistance = this.minecraft.options.getEffectiveRenderDistance();
         if (this.viewArea != null) {
            this.viewArea.releaseAllBuffers();
         }

         this.chunkRenderDispatcher.blockUntilClear();
         synchronized(this.globalBlockEntities) {
            this.globalBlockEntities.clear();
         }

         this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.getEffectiveRenderDistance(), this);
         if (this.lastFullRenderChunkUpdate != null) {
            try {
               this.lastFullRenderChunkUpdate.get();
               this.lastFullRenderChunkUpdate = null;
            } catch (Exception var3) {
               LOGGER.warn("Full update failed", (Throwable)var3);
            }
         }

         this.renderChunkStorage.set(new LevelRenderer.RenderChunkStorage(this.viewArea.chunks.length));
         this.renderChunksInFrustum.clear();
         Entity entity = this.minecraft.getCameraEntity();
         if (entity != null) {
            this.viewArea.repositionCamera(entity.getX(), entity.getZ());
         }

      }
   }

   public void resize(int i, int j) {
      this.needsUpdate();
      if (this.entityEffect != null) {
         this.entityEffect.resize(i, j);
      }

      if (this.transparencyChain != null) {
         this.transparencyChain.resize(i, j);
      }

   }

   public String getChunkStatistics() {
      int i = this.viewArea.chunks.length;
      int j = this.countRenderedChunks();
      return String.format(Locale.ROOT, "C: %d/%d %sD: %d, %s", j, i, this.minecraft.smartCull ? "(s) " : "", this.lastViewDistance, this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats());
   }

   public ChunkRenderDispatcher getChunkRenderDispatcher() {
      return this.chunkRenderDispatcher;
   }

   public double getTotalChunks() {
      return (double)this.viewArea.chunks.length;
   }

   public double getLastViewDistance() {
      return (double)this.lastViewDistance;
   }

   public int countRenderedChunks() {
      int i = 0;

      for(LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo : this.renderChunksInFrustum) {
         if (!levelrenderer_renderchunkinfo.chunk.getCompiledChunk().hasNoRenderableLayers()) {
            ++i;
         }
      }

      return i;
   }

   public String getEntityStatistics() {
      return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities + ", SD: " + this.level.getServerSimulationDistance();
   }

   private void setupRender(Camera camera, Frustum frustum, boolean flag, boolean flag1) {
      Vec3 vec3 = camera.getPosition();
      if (this.minecraft.options.getEffectiveRenderDistance() != this.lastViewDistance) {
         this.allChanged();
      }

      this.level.getProfiler().push("camera");
      double d0 = this.minecraft.player.getX();
      double d1 = this.minecraft.player.getY();
      double d2 = this.minecraft.player.getZ();
      int i = SectionPos.posToSectionCoord(d0);
      int j = SectionPos.posToSectionCoord(d1);
      int k = SectionPos.posToSectionCoord(d2);
      if (this.lastCameraChunkX != i || this.lastCameraChunkY != j || this.lastCameraChunkZ != k) {
         this.lastCameraX = d0;
         this.lastCameraY = d1;
         this.lastCameraZ = d2;
         this.lastCameraChunkX = i;
         this.lastCameraChunkY = j;
         this.lastCameraChunkZ = k;
         this.viewArea.repositionCamera(d0, d2);
      }

      this.chunkRenderDispatcher.setCamera(vec3);
      this.level.getProfiler().popPush("cull");
      this.minecraft.getProfiler().popPush("culling");
      BlockPos blockpos = camera.getBlockPosition();
      double d3 = Math.floor(vec3.x / 8.0D);
      double d4 = Math.floor(vec3.y / 8.0D);
      double d5 = Math.floor(vec3.z / 8.0D);
      this.needsFullRenderChunkUpdate = this.needsFullRenderChunkUpdate || d3 != this.prevCamX || d4 != this.prevCamY || d5 != this.prevCamZ;
      this.nextFullUpdateMillis.updateAndGet((l) -> {
         if (l > 0L && System.currentTimeMillis() > l) {
            this.needsFullRenderChunkUpdate = true;
            return 0L;
         } else {
            return l;
         }
      });
      this.prevCamX = d3;
      this.prevCamY = d4;
      this.prevCamZ = d5;
      this.minecraft.getProfiler().popPush("update");
      boolean flag2 = this.minecraft.smartCull;
      if (flag1 && this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos)) {
         flag2 = false;
      }

      if (!flag) {
         if (this.needsFullRenderChunkUpdate && (this.lastFullRenderChunkUpdate == null || this.lastFullRenderChunkUpdate.isDone())) {
            this.minecraft.getProfiler().push("full_update_schedule");
            this.needsFullRenderChunkUpdate = false;
            boolean flag3 = flag2;
            this.lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> {
               Queue<LevelRenderer.RenderChunkInfo> queue1 = Queues.newArrayDeque();
               this.initializeQueueForFullUpdate(camera, queue1);
               LevelRenderer.RenderChunkStorage levelrenderer_renderchunkstorage1 = new LevelRenderer.RenderChunkStorage(this.viewArea.chunks.length);
               this.updateRenderChunks(levelrenderer_renderchunkstorage1.renderChunks, levelrenderer_renderchunkstorage1.renderInfoMap, vec3, queue1, flag3);
               this.renderChunkStorage.set(levelrenderer_renderchunkstorage1);
               this.needsFrustumUpdate.set(true);
            });
            this.minecraft.getProfiler().pop();
         }

         LevelRenderer.RenderChunkStorage levelrenderer_renderchunkstorage = this.renderChunkStorage.get();
         if (!this.recentlyCompiledChunks.isEmpty()) {
            this.minecraft.getProfiler().push("partial_update");
            Queue<LevelRenderer.RenderChunkInfo> queue = Queues.newArrayDeque();

            while(!this.recentlyCompiledChunks.isEmpty()) {
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = this.recentlyCompiledChunks.poll();
               LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo = levelrenderer_renderchunkstorage.renderInfoMap.get(chunkrenderdispatcher_renderchunk);
               if (levelrenderer_renderchunkinfo != null && levelrenderer_renderchunkinfo.chunk == chunkrenderdispatcher_renderchunk) {
                  queue.add(levelrenderer_renderchunkinfo);
               }
            }

            this.updateRenderChunks(levelrenderer_renderchunkstorage.renderChunks, levelrenderer_renderchunkstorage.renderInfoMap, vec3, queue, flag2);
            this.needsFrustumUpdate.set(true);
            this.minecraft.getProfiler().pop();
         }

         double d6 = Math.floor((double)(camera.getXRot() / 2.0F));
         double d7 = Math.floor((double)(camera.getYRot() / 2.0F));
         if (this.needsFrustumUpdate.compareAndSet(true, false) || d6 != this.prevCamRotX || d7 != this.prevCamRotY) {
            this.applyFrustum((new Frustum(frustum)).offsetToFullyIncludeCameraCube(8));
            this.prevCamRotX = d6;
            this.prevCamRotY = d7;
         }
      }

      this.minecraft.getProfiler().pop();
   }

   private void applyFrustum(Frustum frustum) {
      if (!Minecraft.getInstance().isSameThread()) {
         throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
      } else {
         this.minecraft.getProfiler().push("apply_frustum");
         this.renderChunksInFrustum.clear();

         for(LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo : (this.renderChunkStorage.get()).renderChunks) {
            if (frustum.isVisible(levelrenderer_renderchunkinfo.chunk.getBoundingBox())) {
               this.renderChunksInFrustum.add(levelrenderer_renderchunkinfo);
            }
         }

         this.minecraft.getProfiler().pop();
      }
   }

   private void initializeQueueForFullUpdate(Camera camera, Queue<LevelRenderer.RenderChunkInfo> queue) {
      int i = 16;
      Vec3 vec3 = camera.getPosition();
      BlockPos blockpos = camera.getBlockPosition();
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = this.viewArea.getRenderChunkAt(blockpos);
      if (chunkrenderdispatcher_renderchunk == null) {
         boolean flag = blockpos.getY() > this.level.getMinBuildHeight();
         int j = flag ? this.level.getMaxBuildHeight() - 8 : this.level.getMinBuildHeight() + 8;
         int k = Mth.floor(vec3.x / 16.0D) * 16;
         int l = Mth.floor(vec3.z / 16.0D) * 16;
         List<LevelRenderer.RenderChunkInfo> list = Lists.newArrayList();

         for(int i1 = -this.lastViewDistance; i1 <= this.lastViewDistance; ++i1) {
            for(int j1 = -this.lastViewDistance; j1 <= this.lastViewDistance; ++j1) {
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk1 = this.viewArea.getRenderChunkAt(new BlockPos(k + SectionPos.sectionToBlockCoord(i1, 8), j, l + SectionPos.sectionToBlockCoord(j1, 8)));
               if (chunkrenderdispatcher_renderchunk1 != null) {
                  list.add(new LevelRenderer.RenderChunkInfo(chunkrenderdispatcher_renderchunk1, (Direction)null, 0));
               }
            }
         }

         list.sort(Comparator.comparingDouble((levelrenderer_renderchunkinfo) -> blockpos.distSqr(levelrenderer_renderchunkinfo.chunk.getOrigin().offset(8, 8, 8))));
         queue.addAll(list);
      } else {
         queue.add(new LevelRenderer.RenderChunkInfo(chunkrenderdispatcher_renderchunk, (Direction)null, 0));
      }

   }

   public void addRecentlyCompiledChunk(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk) {
      this.recentlyCompiledChunks.add(chunkrenderdispatcher_renderchunk);
   }

   private void updateRenderChunks(LinkedHashSet<LevelRenderer.RenderChunkInfo> linkedhashset, LevelRenderer.RenderInfoMap levelrenderer_renderinfomap, Vec3 vec3, Queue<LevelRenderer.RenderChunkInfo> queue, boolean flag) {
      int i = 16;
      BlockPos blockpos = new BlockPos(Mth.floor(vec3.x / 16.0D) * 16, Mth.floor(vec3.y / 16.0D) * 16, Mth.floor(vec3.z / 16.0D) * 16);
      BlockPos blockpos1 = blockpos.offset(8, 8, 8);
      Entity.setViewScale(Mth.clamp((double)this.minecraft.options.getEffectiveRenderDistance() / 8.0D, 1.0D, 2.5D) * this.minecraft.options.entityDistanceScaling().get());

      while(!queue.isEmpty()) {
         LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo = queue.poll();
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = levelrenderer_renderchunkinfo.chunk;
         linkedhashset.add(levelrenderer_renderchunkinfo);
         boolean flag1 = Math.abs(chunkrenderdispatcher_renderchunk.getOrigin().getX() - blockpos.getX()) > 60 || Math.abs(chunkrenderdispatcher_renderchunk.getOrigin().getY() - blockpos.getY()) > 60 || Math.abs(chunkrenderdispatcher_renderchunk.getOrigin().getZ() - blockpos.getZ()) > 60;

         for(Direction direction : DIRECTIONS) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk1 = this.getRelativeFrom(blockpos, chunkrenderdispatcher_renderchunk, direction);
            if (chunkrenderdispatcher_renderchunk1 != null && (!flag || !levelrenderer_renderchunkinfo.hasDirection(direction.getOpposite()))) {
               if (flag && levelrenderer_renderchunkinfo.hasSourceDirections()) {
                  ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher_compiledchunk = chunkrenderdispatcher_renderchunk.getCompiledChunk();
                  boolean flag2 = false;

                  for(int j = 0; j < DIRECTIONS.length; ++j) {
                     if (levelrenderer_renderchunkinfo.hasSourceDirection(j) && chunkrenderdispatcher_compiledchunk.facesCanSeeEachother(DIRECTIONS[j].getOpposite(), direction)) {
                        flag2 = true;
                        break;
                     }
                  }

                  if (!flag2) {
                     continue;
                  }
               }

               if (flag && flag1) {
                  BlockPos blockpos2;
                  byte var10001;
                  label126: {
                     label125: {
                        blockpos2 = chunkrenderdispatcher_renderchunk1.getOrigin();
                        if (direction.getAxis() == Direction.Axis.X) {
                           if (blockpos1.getX() > blockpos2.getX()) {
                              break label125;
                           }
                        } else if (blockpos1.getX() < blockpos2.getX()) {
                           break label125;
                        }

                        var10001 = 0;
                        break label126;
                     }

                     var10001 = 16;
                  }

                  byte var10002;
                  label118: {
                     label117: {
                        if (direction.getAxis() == Direction.Axis.Y) {
                           if (blockpos1.getY() > blockpos2.getY()) {
                              break label117;
                           }
                        } else if (blockpos1.getY() < blockpos2.getY()) {
                           break label117;
                        }

                        var10002 = 0;
                        break label118;
                     }

                     var10002 = 16;
                  }

                  byte var10003;
                  label110: {
                     label109: {
                        if (direction.getAxis() == Direction.Axis.Z) {
                           if (blockpos1.getZ() > blockpos2.getZ()) {
                              break label109;
                           }
                        } else if (blockpos1.getZ() < blockpos2.getZ()) {
                           break label109;
                        }

                        var10003 = 0;
                        break label110;
                     }

                     var10003 = 16;
                  }

                  BlockPos blockpos3 = blockpos2.offset(var10001, var10002, var10003);
                  Vec3 vec31 = new Vec3((double)blockpos3.getX(), (double)blockpos3.getY(), (double)blockpos3.getZ());
                  Vec3 vec32 = vec3.subtract(vec31).normalize().scale(CEILED_SECTION_DIAGONAL);
                  boolean flag3 = true;

                  while(vec3.subtract(vec31).lengthSqr() > 3600.0D) {
                     vec31 = vec31.add(vec32);
                     if (vec31.y > (double)this.level.getMaxBuildHeight() || vec31.y < (double)this.level.getMinBuildHeight()) {
                        break;
                     }

                     ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk2 = this.viewArea.getRenderChunkAt(BlockPos.containing(vec31.x, vec31.y, vec31.z));
                     if (chunkrenderdispatcher_renderchunk2 == null || levelrenderer_renderinfomap.get(chunkrenderdispatcher_renderchunk2) == null) {
                        flag3 = false;
                        break;
                     }
                  }

                  if (!flag3) {
                     continue;
                  }
               }

               LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo1 = levelrenderer_renderinfomap.get(chunkrenderdispatcher_renderchunk1);
               if (levelrenderer_renderchunkinfo1 != null) {
                  levelrenderer_renderchunkinfo1.addSourceDirection(direction);
               } else if (!chunkrenderdispatcher_renderchunk1.hasAllNeighbors()) {
                  if (!this.closeToBorder(blockpos, chunkrenderdispatcher_renderchunk)) {
                     this.nextFullUpdateMillis.set(System.currentTimeMillis() + 500L);
                  }
               } else {
                  LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo2 = new LevelRenderer.RenderChunkInfo(chunkrenderdispatcher_renderchunk1, direction, levelrenderer_renderchunkinfo.step + 1);
                  levelrenderer_renderchunkinfo2.setDirections(levelrenderer_renderchunkinfo.directions, direction);
                  queue.add(levelrenderer_renderchunkinfo2);
                  levelrenderer_renderinfomap.put(chunkrenderdispatcher_renderchunk1, levelrenderer_renderchunkinfo2);
               }
            }
         }
      }

   }

   @Nullable
   private ChunkRenderDispatcher.RenderChunk getRelativeFrom(BlockPos blockpos, ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk, Direction direction) {
      BlockPos blockpos1 = chunkrenderdispatcher_renderchunk.getRelativeOrigin(direction);
      if (Mth.abs(blockpos.getX() - blockpos1.getX()) > this.lastViewDistance * 16) {
         return null;
      } else if (Mth.abs(blockpos.getY() - blockpos1.getY()) <= this.lastViewDistance * 16 && blockpos1.getY() >= this.level.getMinBuildHeight() && blockpos1.getY() < this.level.getMaxBuildHeight()) {
         return Mth.abs(blockpos.getZ() - blockpos1.getZ()) > this.lastViewDistance * 16 ? null : this.viewArea.getRenderChunkAt(blockpos1);
      } else {
         return null;
      }
   }

   private boolean closeToBorder(BlockPos blockpos, ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk) {
      int i = SectionPos.blockToSectionCoord(blockpos.getX());
      int j = SectionPos.blockToSectionCoord(blockpos.getZ());
      BlockPos blockpos1 = chunkrenderdispatcher_renderchunk.getOrigin();
      int k = SectionPos.blockToSectionCoord(blockpos1.getX());
      int l = SectionPos.blockToSectionCoord(blockpos1.getZ());
      return !ChunkMap.isChunkInRange(k, l, i, j, this.lastViewDistance - 3);
   }

   private void captureFrustum(Matrix4f matrix4f, Matrix4f matrix4f1, double d0, double d1, double d2, Frustum frustum) {
      this.capturedFrustum = frustum;
      Matrix4f matrix4f2 = new Matrix4f(matrix4f1);
      matrix4f2.mul(matrix4f);
      matrix4f2.invert();
      this.frustumPos.x = d0;
      this.frustumPos.y = d1;
      this.frustumPos.z = d2;
      this.frustumPoints[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
      this.frustumPoints[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
      this.frustumPoints[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
      this.frustumPoints[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
      this.frustumPoints[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
      this.frustumPoints[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
      this.frustumPoints[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.frustumPoints[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

      for(int i = 0; i < 8; ++i) {
         matrix4f2.transform(this.frustumPoints[i]);
         this.frustumPoints[i].div(this.frustumPoints[i].w());
      }

   }

   public void prepareCullFrustum(PoseStack posestack, Vec3 vec3, Matrix4f matrix4f) {
      Matrix4f matrix4f1 = posestack.last().pose();
      double d0 = vec3.x();
      double d1 = vec3.y();
      double d2 = vec3.z();
      this.cullingFrustum = new Frustum(matrix4f1, matrix4f);
      this.cullingFrustum.prepare(d0, d1, d2);
   }

   public void renderLevel(PoseStack posestack, float f, long i, boolean flag, Camera camera, GameRenderer gamerenderer, LightTexture lighttexture, Matrix4f matrix4f) {
      RenderSystem.setShaderGameTime(this.level.getGameTime(), f);
      this.blockEntityRenderDispatcher.prepare(this.level, camera, this.minecraft.hitResult);
      this.entityRenderDispatcher.prepare(this.level, camera, this.minecraft.crosshairPickEntity);
      ProfilerFiller profilerfiller = this.level.getProfiler();
      profilerfiller.popPush("light_update_queue");
      this.level.pollLightUpdates();
      profilerfiller.popPush("light_updates");
      this.level.getChunkSource().getLightEngine().runLightUpdates();
      Vec3 vec3 = camera.getPosition();
      double d0 = vec3.x();
      double d1 = vec3.y();
      double d2 = vec3.z();
      Matrix4f matrix4f1 = posestack.last().pose();
      profilerfiller.popPush("culling");
      boolean flag1 = this.capturedFrustum != null;
      Frustum frustum;
      if (flag1) {
         frustum = this.capturedFrustum;
         frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
      } else {
         frustum = this.cullingFrustum;
      }

      this.minecraft.getProfiler().popPush("captureFrustum");
      if (this.captureFrustum) {
         this.captureFrustum(matrix4f1, matrix4f, vec3.x, vec3.y, vec3.z, flag1 ? new Frustum(matrix4f1, matrix4f) : frustum);
         this.captureFrustum = false;
      }

      profilerfiller.popPush("clear");
      FogRenderer.setupColor(camera, f, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), gamerenderer.getDarkenWorldAmount(f));
      FogRenderer.levelFogColor();
      RenderSystem.clear(16640, Minecraft.ON_OSX);
      float f1 = gamerenderer.getRenderDistance();
      boolean flag2 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d0), Mth.floor(d1)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();
      profilerfiller.popPush("sky");
      RenderSystem.setShader(GameRenderer::getPositionShader);
      this.renderSky(posestack, matrix4f, f, camera, flag2, () -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_SKY, f1, flag2, f));
      profilerfiller.popPush("fog");
      FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(f1, 32.0F), flag2, f);
      profilerfiller.popPush("terrain_setup");
      this.setupRender(camera, frustum, flag1, this.minecraft.player.isSpectator());
      profilerfiller.popPush("compilechunks");
      this.compileChunks(camera);
      profilerfiller.popPush("terrain");
      this.renderChunkLayer(RenderType.solid(), posestack, d0, d1, d2, matrix4f);
      this.renderChunkLayer(RenderType.cutoutMipped(), posestack, d0, d1, d2, matrix4f);
      this.renderChunkLayer(RenderType.cutout(), posestack, d0, d1, d2, matrix4f);
      if (this.level.effects().constantAmbientLight()) {
         Lighting.setupNetherLevel(posestack.last().pose());
      } else {
         Lighting.setupLevel(posestack.last().pose());
      }

      profilerfiller.popPush("entities");
      this.renderedEntities = 0;
      this.culledEntities = 0;
      if (this.itemEntityTarget != null) {
         this.itemEntityTarget.clear(Minecraft.ON_OSX);
         this.itemEntityTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
         this.minecraft.getMainRenderTarget().bindWrite(false);
      }

      if (this.weatherTarget != null) {
         this.weatherTarget.clear(Minecraft.ON_OSX);
      }

      if (this.shouldShowEntityOutlines()) {
         this.entityTarget.clear(Minecraft.ON_OSX);
         this.minecraft.getMainRenderTarget().bindWrite(false);
      }

      boolean flag3 = false;
      MultiBufferSource.BufferSource multibuffersource_buffersource = this.renderBuffers.bufferSource();

      for(Entity entity : this.level.entitiesForRendering()) {
         if (this.entityRenderDispatcher.shouldRender(entity, frustum, d0, d1, d2) || entity.hasIndirectPassenger(this.minecraft.player)) {
            BlockPos blockpos = entity.blockPosition();
            if ((this.level.isOutsideBuildHeight(blockpos.getY()) || this.isChunkCompiled(blockpos)) && (entity != camera.getEntity() || camera.isDetached() || camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) && (!(entity instanceof LocalPlayer) || camera.getEntity() == entity)) {
               ++this.renderedEntities;
               if (entity.tickCount == 0) {
                  entity.xOld = entity.getX();
                  entity.yOld = entity.getY();
                  entity.zOld = entity.getZ();
               }

               MultiBufferSource multibuffersource;
               if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity)) {
                  flag3 = true;
                  OutlineBufferSource outlinebuffersource = this.renderBuffers.outlineBufferSource();
                  multibuffersource = outlinebuffersource;
                  int j = entity.getTeamColor();
                  outlinebuffersource.setColor(FastColor.ARGB32.red(j), FastColor.ARGB32.green(j), FastColor.ARGB32.blue(j), 255);
               } else {
                  multibuffersource = multibuffersource_buffersource;
               }

               this.renderEntity(entity, d0, d1, d2, f, posestack, multibuffersource);
            }
         }
      }

      multibuffersource_buffersource.endLastBatch();
      this.checkPoseStack(posestack);
      multibuffersource_buffersource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
      multibuffersource_buffersource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
      multibuffersource_buffersource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
      multibuffersource_buffersource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
      profilerfiller.popPush("blockentities");

      for(LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo : this.renderChunksInFrustum) {
         List<BlockEntity> list = levelrenderer_renderchunkinfo.chunk.getCompiledChunk().getRenderableBlockEntities();
         if (!list.isEmpty()) {
            for(BlockEntity blockentity : list) {
               BlockPos blockpos1 = blockentity.getBlockPos();
               MultiBufferSource multibuffersource2 = multibuffersource_buffersource;
               posestack.pushPose();
               posestack.translate((double)blockpos1.getX() - d0, (double)blockpos1.getY() - d1, (double)blockpos1.getZ() - d2);
               SortedSet<BlockDestructionProgress> sortedset = this.destructionProgress.get(blockpos1.asLong());
               if (sortedset != null && !sortedset.isEmpty()) {
                  int k = sortedset.last().getProgress();
                  if (k >= 0) {
                     PoseStack.Pose posestack_pose = posestack.last();
                     VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(k)), posestack_pose.pose(), posestack_pose.normal(), 1.0F);
                     multibuffersource2 = (rendertype) -> {
                        VertexConsumer vertexconsumer4 = multibuffersource_buffersource.getBuffer(rendertype);
                        return rendertype.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vertexconsumer4) : vertexconsumer4;
                     };
                  }
               }

               this.blockEntityRenderDispatcher.render(blockentity, f, posestack, multibuffersource2);
               posestack.popPose();
            }
         }
      }

      synchronized(this.globalBlockEntities) {
         for(BlockEntity blockentity1 : this.globalBlockEntities) {
            BlockPos blockpos2 = blockentity1.getBlockPos();
            posestack.pushPose();
            posestack.translate((double)blockpos2.getX() - d0, (double)blockpos2.getY() - d1, (double)blockpos2.getZ() - d2);
            this.blockEntityRenderDispatcher.render(blockentity1, f, posestack, multibuffersource_buffersource);
            posestack.popPose();
         }
      }

      this.checkPoseStack(posestack);
      multibuffersource_buffersource.endBatch(RenderType.solid());
      multibuffersource_buffersource.endBatch(RenderType.endPortal());
      multibuffersource_buffersource.endBatch(RenderType.endGateway());
      multibuffersource_buffersource.endBatch(Sheets.solidBlockSheet());
      multibuffersource_buffersource.endBatch(Sheets.cutoutBlockSheet());
      multibuffersource_buffersource.endBatch(Sheets.bedSheet());
      multibuffersource_buffersource.endBatch(Sheets.shulkerBoxSheet());
      multibuffersource_buffersource.endBatch(Sheets.signSheet());
      multibuffersource_buffersource.endBatch(Sheets.hangingSignSheet());
      multibuffersource_buffersource.endBatch(Sheets.chestSheet());
      this.renderBuffers.outlineBufferSource().endOutlineBatch();
      if (flag3) {
         this.entityEffect.process(f);
         this.minecraft.getMainRenderTarget().bindWrite(false);
      }

      profilerfiller.popPush("destroyProgress");

      for(Long2ObjectMap.Entry<SortedSet<BlockDestructionProgress>> long2objectmap_entry : this.destructionProgress.long2ObjectEntrySet()) {
         BlockPos blockpos3 = BlockPos.of(long2objectmap_entry.getLongKey());
         double d3 = (double)blockpos3.getX() - d0;
         double d4 = (double)blockpos3.getY() - d1;
         double d5 = (double)blockpos3.getZ() - d2;
         if (!(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
            SortedSet<BlockDestructionProgress> sortedset1 = long2objectmap_entry.getValue();
            if (sortedset1 != null && !sortedset1.isEmpty()) {
               int l = sortedset1.last().getProgress();
               posestack.pushPose();
               posestack.translate((double)blockpos3.getX() - d0, (double)blockpos3.getY() - d1, (double)blockpos3.getZ() - d2);
               PoseStack.Pose posestack_pose1 = posestack.last();
               VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(l)), posestack_pose1.pose(), posestack_pose1.normal(), 1.0F);
               this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockpos3), blockpos3, this.level, posestack, vertexconsumer1);
               posestack.popPose();
            }
         }
      }

      this.checkPoseStack(posestack);
      HitResult hitresult = this.minecraft.hitResult;
      if (flag && hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
         profilerfiller.popPush("outline");
         BlockPos blockpos4 = ((BlockHitResult)hitresult).getBlockPos();
         BlockState blockstate = this.level.getBlockState(blockpos4);
         if (!blockstate.isAir() && this.level.getWorldBorder().isWithinBounds(blockpos4)) {
            VertexConsumer vertexconsumer2 = multibuffersource_buffersource.getBuffer(RenderType.lines());
            this.renderHitOutline(posestack, vertexconsumer2, camera.getEntity(), d0, d1, d2, blockpos4, blockstate);
         }
      }

      this.minecraft.debugRenderer.render(posestack, multibuffersource_buffersource, d0, d1, d2);
      multibuffersource_buffersource.endLastBatch();
      PoseStack posestack1 = RenderSystem.getModelViewStack();
      RenderSystem.applyModelViewMatrix();
      multibuffersource_buffersource.endBatch(Sheets.translucentCullBlockSheet());
      multibuffersource_buffersource.endBatch(Sheets.bannerSheet());
      multibuffersource_buffersource.endBatch(Sheets.shieldSheet());
      multibuffersource_buffersource.endBatch(RenderType.armorGlint());
      multibuffersource_buffersource.endBatch(RenderType.armorEntityGlint());
      multibuffersource_buffersource.endBatch(RenderType.glint());
      multibuffersource_buffersource.endBatch(RenderType.glintDirect());
      multibuffersource_buffersource.endBatch(RenderType.glintTranslucent());
      multibuffersource_buffersource.endBatch(RenderType.entityGlint());
      multibuffersource_buffersource.endBatch(RenderType.entityGlintDirect());
      multibuffersource_buffersource.endBatch(RenderType.waterMask());
      this.renderBuffers.crumblingBufferSource().endBatch();
      if (this.transparencyChain != null) {
         multibuffersource_buffersource.endBatch(RenderType.lines());
         multibuffersource_buffersource.endBatch();
         this.translucentTarget.clear(Minecraft.ON_OSX);
         this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
         profilerfiller.popPush("translucent");
         this.renderChunkLayer(RenderType.translucent(), posestack, d0, d1, d2, matrix4f);
         profilerfiller.popPush("string");
         this.renderChunkLayer(RenderType.tripwire(), posestack, d0, d1, d2, matrix4f);
         this.particlesTarget.clear(Minecraft.ON_OSX);
         this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
         RenderStateShard.PARTICLES_TARGET.setupRenderState();
         profilerfiller.popPush("particles");
         this.minecraft.particleEngine.render(posestack, multibuffersource_buffersource, lighttexture, camera, f);
         RenderStateShard.PARTICLES_TARGET.clearRenderState();
      } else {
         profilerfiller.popPush("translucent");
         if (this.translucentTarget != null) {
            this.translucentTarget.clear(Minecraft.ON_OSX);
         }

         this.renderChunkLayer(RenderType.translucent(), posestack, d0, d1, d2, matrix4f);
         multibuffersource_buffersource.endBatch(RenderType.lines());
         multibuffersource_buffersource.endBatch();
         profilerfiller.popPush("string");
         this.renderChunkLayer(RenderType.tripwire(), posestack, d0, d1, d2, matrix4f);
         profilerfiller.popPush("particles");
         this.minecraft.particleEngine.render(posestack, multibuffersource_buffersource, lighttexture, camera, f);
      }

      posestack1.pushPose();
      posestack1.mulPoseMatrix(posestack.last().pose());
      RenderSystem.applyModelViewMatrix();
      if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
         if (this.transparencyChain != null) {
            this.cloudsTarget.clear(Minecraft.ON_OSX);
            RenderStateShard.CLOUDS_TARGET.setupRenderState();
            profilerfiller.popPush("clouds");
            this.renderClouds(posestack, matrix4f, f, d0, d1, d2);
            RenderStateShard.CLOUDS_TARGET.clearRenderState();
         } else {
            profilerfiller.popPush("clouds");
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            this.renderClouds(posestack, matrix4f, f, d0, d1, d2);
         }
      }

      if (this.transparencyChain != null) {
         RenderStateShard.WEATHER_TARGET.setupRenderState();
         profilerfiller.popPush("weather");
         this.renderSnowAndRain(lighttexture, f, d0, d1, d2);
         this.renderWorldBorder(camera);
         RenderStateShard.WEATHER_TARGET.clearRenderState();
         this.transparencyChain.process(f);
         this.minecraft.getMainRenderTarget().bindWrite(false);
      } else {
         RenderSystem.depthMask(false);
         profilerfiller.popPush("weather");
         this.renderSnowAndRain(lighttexture, f, d0, d1, d2);
         this.renderWorldBorder(camera);
         RenderSystem.depthMask(true);
      }

      posestack1.popPose();
      RenderSystem.applyModelViewMatrix();
      this.renderDebug(posestack, multibuffersource_buffersource, camera);
      multibuffersource_buffersource.endLastBatch();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      FogRenderer.setupNoFog();
   }

   private void checkPoseStack(PoseStack posestack) {
      if (!posestack.clear()) {
         throw new IllegalStateException("Pose stack not empty");
      }
   }

   private void renderEntity(Entity entity, double d0, double d1, double d2, float f, PoseStack posestack, MultiBufferSource multibuffersource) {
      double d3 = Mth.lerp((double)f, entity.xOld, entity.getX());
      double d4 = Mth.lerp((double)f, entity.yOld, entity.getY());
      double d5 = Mth.lerp((double)f, entity.zOld, entity.getZ());
      float f1 = Mth.lerp(f, entity.yRotO, entity.getYRot());
      this.entityRenderDispatcher.render(entity, d3 - d0, d4 - d1, d5 - d2, f1, f, posestack, multibuffersource, this.entityRenderDispatcher.getPackedLightCoords(entity, f));
   }

   private void renderChunkLayer(RenderType rendertype, PoseStack posestack, double d0, double d1, double d2, Matrix4f matrix4f) {
      RenderSystem.assertOnRenderThread();
      rendertype.setupRenderState();
      if (rendertype == RenderType.translucent()) {
         this.minecraft.getProfiler().push("translucent_sort");
         double d3 = d0 - this.xTransparentOld;
         double d4 = d1 - this.yTransparentOld;
         double d5 = d2 - this.zTransparentOld;
         if (d3 * d3 + d4 * d4 + d5 * d5 > 1.0D) {
            int i = SectionPos.posToSectionCoord(d0);
            int j = SectionPos.posToSectionCoord(d1);
            int k = SectionPos.posToSectionCoord(d2);
            boolean flag = i != SectionPos.posToSectionCoord(this.xTransparentOld) || k != SectionPos.posToSectionCoord(this.zTransparentOld) || j != SectionPos.posToSectionCoord(this.yTransparentOld);
            this.xTransparentOld = d0;
            this.yTransparentOld = d1;
            this.zTransparentOld = d2;
            int l = 0;

            for(LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo : this.renderChunksInFrustum) {
               if (l < 15 && (flag || levelrenderer_renderchunkinfo.isAxisAlignedWith(i, j, k)) && levelrenderer_renderchunkinfo.chunk.resortTransparency(rendertype, this.chunkRenderDispatcher)) {
                  ++l;
               }
            }
         }

         this.minecraft.getProfiler().pop();
      }

      this.minecraft.getProfiler().push("filterempty");
      this.minecraft.getProfiler().popPush(() -> "render_" + rendertype);
      boolean flag1 = rendertype != RenderType.translucent();
      ObjectListIterator<LevelRenderer.RenderChunkInfo> objectlistiterator = this.renderChunksInFrustum.listIterator(flag1 ? 0 : this.renderChunksInFrustum.size());
      ShaderInstance shaderinstance = RenderSystem.getShader();

      for(int i1 = 0; i1 < 12; ++i1) {
         int j1 = RenderSystem.getShaderTexture(i1);
         shaderinstance.setSampler("Sampler" + i1, j1);
      }

      if (shaderinstance.MODEL_VIEW_MATRIX != null) {
         shaderinstance.MODEL_VIEW_MATRIX.set(posestack.last().pose());
      }

      if (shaderinstance.PROJECTION_MATRIX != null) {
         shaderinstance.PROJECTION_MATRIX.set(matrix4f);
      }

      if (shaderinstance.COLOR_MODULATOR != null) {
         shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
      }

      if (shaderinstance.GLINT_ALPHA != null) {
         shaderinstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
      }

      if (shaderinstance.FOG_START != null) {
         shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
      }

      if (shaderinstance.FOG_END != null) {
         shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
      }

      if (shaderinstance.FOG_COLOR != null) {
         shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
      }

      if (shaderinstance.FOG_SHAPE != null) {
         shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
      }

      if (shaderinstance.TEXTURE_MATRIX != null) {
         shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
      }

      if (shaderinstance.GAME_TIME != null) {
         shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
      }

      RenderSystem.setupShaderLights(shaderinstance);
      shaderinstance.apply();
      Uniform uniform = shaderinstance.CHUNK_OFFSET;

      while(true) {
         if (flag1) {
            if (!objectlistiterator.hasNext()) {
               break;
            }
         } else if (!objectlistiterator.hasPrevious()) {
            break;
         }

         LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo1 = flag1 ? objectlistiterator.next() : objectlistiterator.previous();
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = levelrenderer_renderchunkinfo1.chunk;
         if (!chunkrenderdispatcher_renderchunk.getCompiledChunk().isEmpty(rendertype)) {
            VertexBuffer vertexbuffer = chunkrenderdispatcher_renderchunk.getBuffer(rendertype);
            BlockPos blockpos = chunkrenderdispatcher_renderchunk.getOrigin();
            if (uniform != null) {
               uniform.set((float)((double)blockpos.getX() - d0), (float)((double)blockpos.getY() - d1), (float)((double)blockpos.getZ() - d2));
               uniform.upload();
            }

            vertexbuffer.bind();
            vertexbuffer.draw();
         }
      }

      if (uniform != null) {
         uniform.set(0.0F, 0.0F, 0.0F);
      }

      shaderinstance.clear();
      VertexBuffer.unbind();
      this.minecraft.getProfiler().pop();
      rendertype.clearRenderState();
   }

   private void renderDebug(PoseStack posestack, MultiBufferSource multibuffersource, Camera camera) {
      if (this.minecraft.chunkPath || this.minecraft.chunkVisibility) {
         double d0 = camera.getPosition().x();
         double d1 = camera.getPosition().y();
         double d2 = camera.getPosition().z();

         for(LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo : this.renderChunksInFrustum) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = levelrenderer_renderchunkinfo.chunk;
            BlockPos blockpos = chunkrenderdispatcher_renderchunk.getOrigin();
            posestack.pushPose();
            posestack.translate((double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2);
            Matrix4f matrix4f = posestack.last().pose();
            if (this.minecraft.chunkPath) {
               VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lines());
               int i = levelrenderer_renderchunkinfo.step == 0 ? 0 : Mth.hsvToRgb((float)levelrenderer_renderchunkinfo.step / 50.0F, 0.9F, 0.9F);
               int j = i >> 16 & 255;
               int k = i >> 8 & 255;
               int l = i & 255;

               for(int i1 = 0; i1 < DIRECTIONS.length; ++i1) {
                  if (levelrenderer_renderchunkinfo.hasSourceDirection(i1)) {
                     Direction direction = DIRECTIONS[i1];
                     vertexconsumer.vertex(matrix4f, 8.0F, 8.0F, 8.0F).color(j, k, l, 255).normal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ()).endVertex();
                     vertexconsumer.vertex(matrix4f, (float)(8 - 16 * direction.getStepX()), (float)(8 - 16 * direction.getStepY()), (float)(8 - 16 * direction.getStepZ())).color(j, k, l, 255).normal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ()).endVertex();
                  }
               }
            }

            if (this.minecraft.chunkVisibility && !chunkrenderdispatcher_renderchunk.getCompiledChunk().hasNoRenderableLayers()) {
               VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(RenderType.lines());
               int j1 = 0;

               for(Direction direction1 : DIRECTIONS) {
                  for(Direction direction2 : DIRECTIONS) {
                     boolean flag = chunkrenderdispatcher_renderchunk.getCompiledChunk().facesCanSeeEachother(direction1, direction2);
                     if (!flag) {
                        ++j1;
                        vertexconsumer1.vertex(matrix4f, (float)(8 + 8 * direction1.getStepX()), (float)(8 + 8 * direction1.getStepY()), (float)(8 + 8 * direction1.getStepZ())).color(255, 0, 0, 255).normal((float)direction1.getStepX(), (float)direction1.getStepY(), (float)direction1.getStepZ()).endVertex();
                        vertexconsumer1.vertex(matrix4f, (float)(8 + 8 * direction2.getStepX()), (float)(8 + 8 * direction2.getStepY()), (float)(8 + 8 * direction2.getStepZ())).color(255, 0, 0, 255).normal((float)direction2.getStepX(), (float)direction2.getStepY(), (float)direction2.getStepZ()).endVertex();
                     }
                  }
               }

               if (j1 > 0) {
                  VertexConsumer vertexconsumer2 = multibuffersource.getBuffer(RenderType.debugQuads());
                  float f = 0.5F;
                  float f1 = 0.2F;
                  vertexconsumer2.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 0.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 15.5F, 0.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 15.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 15.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                  vertexconsumer2.vertex(matrix4f, 0.5F, 0.5F, 15.5F).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
               }
            }

            posestack.popPose();
         }
      }

      if (this.capturedFrustum != null) {
         posestack.pushPose();
         posestack.translate((float)(this.frustumPos.x - camera.getPosition().x), (float)(this.frustumPos.y - camera.getPosition().y), (float)(this.frustumPos.z - camera.getPosition().z));
         Matrix4f matrix4f1 = posestack.last().pose();
         VertexConsumer vertexconsumer3 = multibuffersource.getBuffer(RenderType.debugQuads());
         this.addFrustumQuad(vertexconsumer3, matrix4f1, 0, 1, 2, 3, 0, 1, 1);
         this.addFrustumQuad(vertexconsumer3, matrix4f1, 4, 5, 6, 7, 1, 0, 0);
         this.addFrustumQuad(vertexconsumer3, matrix4f1, 0, 1, 5, 4, 1, 1, 0);
         this.addFrustumQuad(vertexconsumer3, matrix4f1, 2, 3, 7, 6, 0, 0, 1);
         this.addFrustumQuad(vertexconsumer3, matrix4f1, 0, 4, 7, 3, 0, 1, 0);
         this.addFrustumQuad(vertexconsumer3, matrix4f1, 1, 5, 6, 2, 1, 0, 1);
         VertexConsumer vertexconsumer4 = multibuffersource.getBuffer(RenderType.lines());
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 0);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 1);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 1);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 2);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 2);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 3);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 3);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 0);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 4);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 5);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 5);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 6);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 6);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 7);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 7);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 4);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 0);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 4);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 1);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 5);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 2);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 6);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 3);
         this.addFrustumVertex(vertexconsumer4, matrix4f1, 7);
         posestack.popPose();
      }

   }

   private void addFrustumVertex(VertexConsumer vertexconsumer, Matrix4f matrix4f, int i) {
      vertexconsumer.vertex(matrix4f, this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z()).color(0, 0, 0, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
   }

   private void addFrustumQuad(VertexConsumer vertexconsumer, Matrix4f matrix4f, int i, int j, int k, int l, int i1, int j1, int k1) {
      float f = 0.25F;
      vertexconsumer.vertex(matrix4f, this.frustumPoints[i].x(), this.frustumPoints[i].y(), this.frustumPoints[i].z()).color((float)i1, (float)j1, (float)k1, 0.25F).endVertex();
      vertexconsumer.vertex(matrix4f, this.frustumPoints[j].x(), this.frustumPoints[j].y(), this.frustumPoints[j].z()).color((float)i1, (float)j1, (float)k1, 0.25F).endVertex();
      vertexconsumer.vertex(matrix4f, this.frustumPoints[k].x(), this.frustumPoints[k].y(), this.frustumPoints[k].z()).color((float)i1, (float)j1, (float)k1, 0.25F).endVertex();
      vertexconsumer.vertex(matrix4f, this.frustumPoints[l].x(), this.frustumPoints[l].y(), this.frustumPoints[l].z()).color((float)i1, (float)j1, (float)k1, 0.25F).endVertex();
   }

   public void captureFrustum() {
      this.captureFrustum = true;
   }

   public void killFrustum() {
      this.capturedFrustum = null;
   }

   public void tick() {
      ++this.ticks;
      if (this.ticks % 20 == 0) {
         Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

         while(iterator.hasNext()) {
            BlockDestructionProgress blockdestructionprogress = iterator.next();
            int i = blockdestructionprogress.getUpdatedRenderTick();
            if (this.ticks - i > 400) {
               iterator.remove();
               this.removeProgress(blockdestructionprogress);
            }
         }

      }
   }

   private void removeProgress(BlockDestructionProgress blockdestructionprogress) {
      long i = blockdestructionprogress.getPos().asLong();
      Set<BlockDestructionProgress> set = this.destructionProgress.get(i);
      set.remove(blockdestructionprogress);
      if (set.isEmpty()) {
         this.destructionProgress.remove(i);
      }

   }

   private void renderEndSky(PoseStack posestack) {
      RenderSystem.enableBlend();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();

      for(int i = 0; i < 6; ++i) {
         posestack.pushPose();
         if (i == 1) {
            posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
         }

         if (i == 2) {
            posestack.mulPose(Axis.XP.rotationDegrees(-90.0F));
         }

         if (i == 3) {
            posestack.mulPose(Axis.XP.rotationDegrees(180.0F));
         }

         if (i == 4) {
            posestack.mulPose(Axis.ZP.rotationDegrees(90.0F));
         }

         if (i == 5) {
            posestack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
         }

         Matrix4f matrix4f = posestack.last().pose();
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
         bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(40, 40, 40, 255).endVertex();
         bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(40, 40, 40, 255).endVertex();
         bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(40, 40, 40, 255).endVertex();
         bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(40, 40, 40, 255).endVertex();
         tesselator.end();
         posestack.popPose();
      }

      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
   }

   public void renderSky(PoseStack posestack, Matrix4f matrix4f, float f, Camera camera, boolean flag, Runnable runnable) {
      runnable.run();
      if (!flag) {
         FogType fogtype = camera.getFluidInCamera();
         if (fogtype != FogType.POWDER_SNOW && fogtype != FogType.LAVA && !this.doesMobEffectBlockSky(camera)) {
            if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END) {
               this.renderEndSky(posestack);
            } else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL) {
               Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), f);
               float f1 = (float)vec3.x;
               float f2 = (float)vec3.y;
               float f3 = (float)vec3.z;
               FogRenderer.levelFogColor();
               BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
               RenderSystem.depthMask(false);
               RenderSystem.setShaderColor(f1, f2, f3, 1.0F);
               ShaderInstance shaderinstance = RenderSystem.getShader();
               this.skyBuffer.bind();
               this.skyBuffer.drawWithShader(posestack.last().pose(), matrix4f, shaderinstance);
               VertexBuffer.unbind();
               RenderSystem.enableBlend();
               float[] afloat = this.level.effects().getSunriseColor(this.level.getTimeOfDay(f), f);
               if (afloat != null) {
                  RenderSystem.setShader(GameRenderer::getPositionColorShader);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  posestack.pushPose();
                  posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
                  float f4 = Mth.sin(this.level.getSunAngle(f)) < 0.0F ? 180.0F : 0.0F;
                  posestack.mulPose(Axis.ZP.rotationDegrees(f4));
                  posestack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                  float f5 = afloat[0];
                  float f6 = afloat[1];
                  float f7 = afloat[2];
                  Matrix4f matrix4f1 = posestack.last().pose();
                  bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                  bufferbuilder.vertex(matrix4f1, 0.0F, 100.0F, 0.0F).color(f5, f6, f7, afloat[3]).endVertex();
                  int i = 16;

                  for(int j = 0; j <= 16; ++j) {
                     float f8 = (float)j * ((float)Math.PI * 2F) / 16.0F;
                     float f9 = Mth.sin(f8);
                     float f10 = Mth.cos(f8);
                     bufferbuilder.vertex(matrix4f1, f9 * 120.0F, f10 * 120.0F, -f10 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                  }

                  BufferUploader.drawWithShader(bufferbuilder.end());
                  posestack.popPose();
               }

               RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
               posestack.pushPose();
               float f11 = 1.0F - this.level.getRainLevel(f);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f11);
               posestack.mulPose(Axis.YP.rotationDegrees(-90.0F));
               posestack.mulPose(Axis.XP.rotationDegrees(this.level.getTimeOfDay(f) * 360.0F));
               Matrix4f matrix4f2 = posestack.last().pose();
               float f12 = 30.0F;
               RenderSystem.setShader(GameRenderer::getPositionTexShader);
               RenderSystem.setShaderTexture(0, SUN_LOCATION);
               bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
               bufferbuilder.vertex(matrix4f2, -f12, 100.0F, -f12).uv(0.0F, 0.0F).endVertex();
               bufferbuilder.vertex(matrix4f2, f12, 100.0F, -f12).uv(1.0F, 0.0F).endVertex();
               bufferbuilder.vertex(matrix4f2, f12, 100.0F, f12).uv(1.0F, 1.0F).endVertex();
               bufferbuilder.vertex(matrix4f2, -f12, 100.0F, f12).uv(0.0F, 1.0F).endVertex();
               BufferUploader.drawWithShader(bufferbuilder.end());
               f12 = 20.0F;
               RenderSystem.setShaderTexture(0, MOON_LOCATION);
               int k = this.level.getMoonPhase();
               int l = k % 4;
               int i1 = k / 4 % 2;
               float f13 = (float)(l + 0) / 4.0F;
               float f14 = (float)(i1 + 0) / 2.0F;
               float f15 = (float)(l + 1) / 4.0F;
               float f16 = (float)(i1 + 1) / 2.0F;
               bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
               bufferbuilder.vertex(matrix4f2, -f12, -100.0F, f12).uv(f15, f16).endVertex();
               bufferbuilder.vertex(matrix4f2, f12, -100.0F, f12).uv(f13, f16).endVertex();
               bufferbuilder.vertex(matrix4f2, f12, -100.0F, -f12).uv(f13, f14).endVertex();
               bufferbuilder.vertex(matrix4f2, -f12, -100.0F, -f12).uv(f15, f14).endVertex();
               BufferUploader.drawWithShader(bufferbuilder.end());
               float f17 = this.level.getStarBrightness(f) * f11;
               if (f17 > 0.0F) {
                  RenderSystem.setShaderColor(f17, f17, f17, f17);
                  FogRenderer.setupNoFog();
                  this.starBuffer.bind();
                  this.starBuffer.drawWithShader(posestack.last().pose(), matrix4f, GameRenderer.getPositionShader());
                  VertexBuffer.unbind();
                  runnable.run();
               }

               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.disableBlend();
               RenderSystem.defaultBlendFunc();
               posestack.popPose();
               RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
               double d0 = this.minecraft.player.getEyePosition(f).y - this.level.getLevelData().getHorizonHeight(this.level);
               if (d0 < 0.0D) {
                  posestack.pushPose();
                  posestack.translate(0.0F, 12.0F, 0.0F);
                  this.darkBuffer.bind();
                  this.darkBuffer.drawWithShader(posestack.last().pose(), matrix4f, shaderinstance);
                  VertexBuffer.unbind();
                  posestack.popPose();
               }

               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.depthMask(true);
            }
         }
      }
   }

   private boolean doesMobEffectBlockSky(Camera camera) {
      Entity var3 = camera.getEntity();
      if (!(var3 instanceof LivingEntity livingentity)) {
         return false;
      } else {
         return livingentity.hasEffect(MobEffects.BLINDNESS) || livingentity.hasEffect(MobEffects.DARKNESS);
      }
   }

   public void renderClouds(PoseStack posestack, Matrix4f matrix4f, float f, double d0, double d1, double d2) {
      float f1 = this.level.effects().getCloudHeight();
      if (!Float.isNaN(f1)) {
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.depthMask(true);
         float f2 = 12.0F;
         float f3 = 4.0F;
         double d3 = 2.0E-4D;
         double d4 = (double)(((float)this.ticks + f) * 0.03F);
         double d5 = (d0 + d4) / 12.0D;
         double d6 = (double)(f1 - (float)d1 + 0.33F);
         double d7 = d2 / 12.0D + (double)0.33F;
         d5 -= (double)(Mth.floor(d5 / 2048.0D) * 2048);
         d7 -= (double)(Mth.floor(d7 / 2048.0D) * 2048);
         float f4 = (float)(d5 - (double)Mth.floor(d5));
         float f5 = (float)(d6 / 4.0D - (double)Mth.floor(d6 / 4.0D)) * 4.0F;
         float f6 = (float)(d7 - (double)Mth.floor(d7));
         Vec3 vec3 = this.level.getCloudColor(f);
         int i = (int)Math.floor(d5);
         int j = (int)Math.floor(d6 / 4.0D);
         int k = (int)Math.floor(d7);
         if (i != this.prevCloudX || j != this.prevCloudY || k != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4D) {
            this.prevCloudX = i;
            this.prevCloudY = j;
            this.prevCloudZ = k;
            this.prevCloudColor = vec3;
            this.prevCloudsType = this.minecraft.options.getCloudsType();
            this.generateClouds = true;
         }

         if (this.generateClouds) {
            this.generateClouds = false;
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            if (this.cloudBuffer != null) {
               this.cloudBuffer.close();
            }

            this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = this.buildClouds(bufferbuilder, d5, d6, d7, vec3);
            this.cloudBuffer.bind();
            this.cloudBuffer.upload(bufferbuilder_renderedbuffer);
            VertexBuffer.unbind();
         }

         RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
         RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
         FogRenderer.levelFogColor();
         posestack.pushPose();
         posestack.scale(12.0F, 1.0F, 12.0F);
         posestack.translate(-f4, f5, -f6);
         if (this.cloudBuffer != null) {
            this.cloudBuffer.bind();
            int l = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

            for(int i1 = l; i1 < 2; ++i1) {
               if (i1 == 0) {
                  RenderSystem.colorMask(false, false, false, false);
               } else {
                  RenderSystem.colorMask(true, true, true, true);
               }

               ShaderInstance shaderinstance = RenderSystem.getShader();
               this.cloudBuffer.drawWithShader(posestack.last().pose(), matrix4f, shaderinstance);
            }

            VertexBuffer.unbind();
         }

         posestack.popPose();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }
   }

   private BufferBuilder.RenderedBuffer buildClouds(BufferBuilder bufferbuilder, double d0, double d1, double d2, Vec3 vec3) {
      float f = 4.0F;
      float f1 = 0.00390625F;
      int i = 8;
      int j = 4;
      float f2 = 9.765625E-4F;
      float f3 = (float)Mth.floor(d0) * 0.00390625F;
      float f4 = (float)Mth.floor(d2) * 0.00390625F;
      float f5 = (float)vec3.x;
      float f6 = (float)vec3.y;
      float f7 = (float)vec3.z;
      float f8 = f5 * 0.9F;
      float f9 = f6 * 0.9F;
      float f10 = f7 * 0.9F;
      float f11 = f5 * 0.7F;
      float f12 = f6 * 0.7F;
      float f13 = f7 * 0.7F;
      float f14 = f5 * 0.8F;
      float f15 = f6 * 0.8F;
      float f16 = f7 * 0.8F;
      RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
      float f17 = (float)Math.floor(d1 / 4.0D) * 4.0F;
      if (this.prevCloudsType == CloudStatus.FANCY) {
         for(int k = -3; k <= 4; ++k) {
            for(int l = -3; l <= 4; ++l) {
               float f18 = (float)(k * 8);
               float f19 = (float)(l * 8);
               if (f17 > -5.0F) {
                  bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                  bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               }

               if (f17 <= 5.0F) {
                  bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                  bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
               }

               if (k > -1) {
                  for(int i1 = 0; i1 < 8; ++i1) {
                     bufferbuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (k <= 1) {
                  for(int j1 = 0; j1 < 8; ++j1) {
                     bufferbuilder.vertex((double)(f18 + (float)j1 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + (float)j1 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + (float)j1 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + (float)j1 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                  }
               }

               if (l > -1) {
                  for(int k1 = 0; k1 < 8; ++k1) {
                     bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k1 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k1 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k1 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k1 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                  }
               }

               if (l <= 1) {
                  for(int l1 = 0; l1 < 8; ++l1) {
                     bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l1 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l1 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l1 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                     bufferbuilder.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l1 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l1 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                  }
               }
            }
         }
      } else {
         int i2 = 1;
         int j2 = 32;

         for(int k2 = -32; k2 < 32; k2 += 32) {
            for(int l2 = -32; l2 < 32; l2 += 32) {
               bufferbuilder.vertex((double)(k2 + 0), (double)f17, (double)(l2 + 32)).uv((float)(k2 + 0) * 0.00390625F + f3, (float)(l2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               bufferbuilder.vertex((double)(k2 + 32), (double)f17, (double)(l2 + 32)).uv((float)(k2 + 32) * 0.00390625F + f3, (float)(l2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               bufferbuilder.vertex((double)(k2 + 32), (double)f17, (double)(l2 + 0)).uv((float)(k2 + 32) * 0.00390625F + f3, (float)(l2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
               bufferbuilder.vertex((double)(k2 + 0), (double)f17, (double)(l2 + 0)).uv((float)(k2 + 0) * 0.00390625F + f3, (float)(l2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            }
         }
      }

      return bufferbuilder.end();
   }

   private void compileChunks(Camera camera) {
      this.minecraft.getProfiler().push("populate_chunks_to_compile");
      LevelLightEngine levellightengine = this.level.getLightEngine();
      RenderRegionCache renderregioncache = new RenderRegionCache();
      BlockPos blockpos = camera.getBlockPosition();
      List<ChunkRenderDispatcher.RenderChunk> list = Lists.newArrayList();

      for(LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo : this.renderChunksInFrustum) {
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = levelrenderer_renderchunkinfo.chunk;
         SectionPos sectionpos = SectionPos.of(chunkrenderdispatcher_renderchunk.getOrigin());
         if (chunkrenderdispatcher_renderchunk.isDirty() && levellightengine.lightOnInSection(sectionpos)) {
            boolean flag = false;
            if (this.minecraft.options.prioritizeChunkUpdates().get() != PrioritizeChunkUpdates.NEARBY) {
               if (this.minecraft.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED) {
                  flag = chunkrenderdispatcher_renderchunk.isDirtyFromPlayer();
               }
            } else {
               BlockPos blockpos1 = chunkrenderdispatcher_renderchunk.getOrigin().offset(8, 8, 8);
               flag = blockpos1.distSqr(blockpos) < 768.0D || chunkrenderdispatcher_renderchunk.isDirtyFromPlayer();
            }

            if (flag) {
               this.minecraft.getProfiler().push("build_near_sync");
               this.chunkRenderDispatcher.rebuildChunkSync(chunkrenderdispatcher_renderchunk, renderregioncache);
               chunkrenderdispatcher_renderchunk.setNotDirty();
               this.minecraft.getProfiler().pop();
            } else {
               list.add(chunkrenderdispatcher_renderchunk);
            }
         }
      }

      this.minecraft.getProfiler().popPush("upload");
      this.chunkRenderDispatcher.uploadAllPendingUploads();
      this.minecraft.getProfiler().popPush("schedule_async_compile");

      for(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk1 : list) {
         chunkrenderdispatcher_renderchunk1.rebuildChunkAsync(this.chunkRenderDispatcher, renderregioncache);
         chunkrenderdispatcher_renderchunk1.setNotDirty();
      }

      this.minecraft.getProfiler().pop();
   }

   private void renderWorldBorder(Camera camera) {
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      WorldBorder worldborder = this.level.getWorldBorder();
      double d0 = (double)(this.minecraft.options.getEffectiveRenderDistance() * 16);
      if (!(camera.getPosition().x < worldborder.getMaxX() - d0) || !(camera.getPosition().x > worldborder.getMinX() + d0) || !(camera.getPosition().z < worldborder.getMaxZ() - d0) || !(camera.getPosition().z > worldborder.getMinZ() + d0)) {
         double d1 = 1.0D - worldborder.getDistanceToBorder(camera.getPosition().x, camera.getPosition().z) / d0;
         d1 = Math.pow(d1, 4.0D);
         d1 = Mth.clamp(d1, 0.0D, 1.0D);
         double d2 = camera.getPosition().x;
         double d3 = camera.getPosition().z;
         double d4 = (double)this.minecraft.gameRenderer.getDepthFar();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
         RenderSystem.depthMask(Minecraft.useShaderTransparency());
         PoseStack posestack = RenderSystem.getModelViewStack();
         posestack.pushPose();
         RenderSystem.applyModelViewMatrix();
         int i = worldborder.getStatus().getColor();
         float f = (float)(i >> 16 & 255) / 255.0F;
         float f1 = (float)(i >> 8 & 255) / 255.0F;
         float f2 = (float)(i & 255) / 255.0F;
         RenderSystem.setShaderColor(f, f1, f2, (float)d1);
         RenderSystem.setShader(GameRenderer::getPositionTexShader);
         RenderSystem.polygonOffset(-3.0F, -3.0F);
         RenderSystem.enablePolygonOffset();
         RenderSystem.disableCull();
         float f3 = (float)(Util.getMillis() % 3000L) / 3000.0F;
         float f4 = (float)(-Mth.frac(camera.getPosition().y * 0.5D));
         float f5 = f4 + (float)d4;
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         double d5 = Math.max((double)Mth.floor(d3 - d0), worldborder.getMinZ());
         double d6 = Math.min((double)Mth.ceil(d3 + d0), worldborder.getMaxZ());
         float f6 = (float)(Mth.floor(d5) & 1) * 0.5F;
         if (d2 > worldborder.getMaxX() - d0) {
            float f7 = f6;

            for(double d7 = d5; d7 < d6; f7 += 0.5F) {
               double d8 = Math.min(1.0D, d6 - d7);
               float f8 = (float)d8 * 0.5F;
               bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 - d3).uv(f3 - f7, f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + f4).endVertex();
               bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 - d3).uv(f3 - f7, f3 + f4).endVertex();
               ++d7;
            }
         }

         if (d2 < worldborder.getMinX() + d0) {
            float f9 = f6;

            for(double d9 = d5; d9 < d6; f9 += 0.5F) {
               double d10 = Math.min(1.0D, d6 - d9);
               float f10 = (float)d10 * 0.5F;
               bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 - d3).uv(f3 + f9, f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 + d10 - d3).uv(f3 + f10 + f9, f3 + f5).endVertex();
               bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 + d10 - d3).uv(f3 + f10 + f9, f3 + f4).endVertex();
               bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 - d3).uv(f3 + f9, f3 + f4).endVertex();
               ++d9;
            }
         }

         d5 = Math.max((double)Mth.floor(d2 - d0), worldborder.getMinX());
         d6 = Math.min((double)Mth.ceil(d2 + d0), worldborder.getMaxX());
         f6 = (float)(Mth.floor(d5) & 1) * 0.5F;
         if (d3 > worldborder.getMaxZ() - d0) {
            float f11 = f6;

            for(double d11 = d5; d11 < d6; f11 += 0.5F) {
               double d12 = Math.min(1.0D, d6 - d11);
               float f12 = (float)d12 * 0.5F;
               bufferbuilder.vertex(d11 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f11, f3 + f5).endVertex();
               bufferbuilder.vertex(d11 + d12 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f12 + f11, f3 + f5).endVertex();
               bufferbuilder.vertex(d11 + d12 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f12 + f11, f3 + f4).endVertex();
               bufferbuilder.vertex(d11 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f11, f3 + f4).endVertex();
               ++d11;
            }
         }

         if (d3 < worldborder.getMinZ() + d0) {
            float f13 = f6;

            for(double d13 = d5; d13 < d6; f13 += 0.5F) {
               double d14 = Math.min(1.0D, d6 - d13);
               float f14 = (float)d14 * 0.5F;
               bufferbuilder.vertex(d13 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - f13, f3 + f5).endVertex();
               bufferbuilder.vertex(d13 + d14 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f13), f3 + f5).endVertex();
               bufferbuilder.vertex(d13 + d14 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f13), f3 + f4).endVertex();
               bufferbuilder.vertex(d13 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - f13, f3 + f4).endVertex();
               ++d13;
            }
         }

         BufferUploader.drawWithShader(bufferbuilder.end());
         RenderSystem.enableCull();
         RenderSystem.polygonOffset(0.0F, 0.0F);
         RenderSystem.disablePolygonOffset();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         posestack.popPose();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.depthMask(true);
      }
   }

   private void renderHitOutline(PoseStack posestack, VertexConsumer vertexconsumer, Entity entity, double d0, double d1, double d2, BlockPos blockpos, BlockState blockstate) {
      renderShape(posestack, vertexconsumer, blockstate.getShape(this.level, blockpos, CollisionContext.of(entity)), (double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2, 0.0F, 0.0F, 0.0F, 0.4F);
   }

   private static Vec3 mixColor(float f) {
      float f1 = 5.99999F;
      int i = (int)(Mth.clamp(f, 0.0F, 1.0F) * 5.99999F);
      float f2 = f * 5.99999F - (float)i;
      Vec3 var10000;
      switch (i) {
         case 0:
            var10000 = new Vec3(1.0D, (double)f2, 0.0D);
            break;
         case 1:
            var10000 = new Vec3((double)(1.0F - f2), 1.0D, 0.0D);
            break;
         case 2:
            var10000 = new Vec3(0.0D, 1.0D, (double)f2);
            break;
         case 3:
            var10000 = new Vec3(0.0D, 1.0D - (double)f2, 1.0D);
            break;
         case 4:
            var10000 = new Vec3((double)f2, 0.0D, 1.0D);
            break;
         case 5:
            var10000 = new Vec3(1.0D, 0.0D, 1.0D - (double)f2);
            break;
         default:
            throw new IllegalStateException("Unexpected value: " + i);
      }

      return var10000;
   }

   private static Vec3 shiftHue(float f, float f1, float f2, float f3) {
      Vec3 vec3 = mixColor(f3).scale((double)f);
      Vec3 vec31 = mixColor((f3 + 0.33333334F) % 1.0F).scale((double)f1);
      Vec3 vec32 = mixColor((f3 + 0.6666667F) % 1.0F).scale((double)f2);
      Vec3 vec33 = vec3.add(vec31).add(vec32);
      double d0 = Math.max(Math.max(1.0D, vec33.x), Math.max(vec33.y, vec33.z));
      return new Vec3(vec33.x / d0, vec33.y / d0, vec33.z / d0);
   }

   public static void renderVoxelShape(PoseStack posestack, VertexConsumer vertexconsumer, VoxelShape voxelshape, double d0, double d1, double d2, float f, float f1, float f2, float f3, boolean flag) {
      List<AABB> list = voxelshape.toAabbs();
      if (!list.isEmpty()) {
         int i = flag ? list.size() : list.size() * 8;
         renderShape(posestack, vertexconsumer, Shapes.create(list.get(0)), d0, d1, d2, f, f1, f2, f3);

         for(int j = 1; j < list.size(); ++j) {
            AABB aabb = list.get(j);
            float f4 = (float)j / (float)i;
            Vec3 vec3 = shiftHue(f, f1, f2, f4);
            renderShape(posestack, vertexconsumer, Shapes.create(aabb), d0, d1, d2, (float)vec3.x, (float)vec3.y, (float)vec3.z, f3);
         }

      }
   }

   private static void renderShape(PoseStack posestack, VertexConsumer vertexconsumer, VoxelShape voxelshape, double d0, double d1, double d2, float f, float f1, float f2, float f3) {
      PoseStack.Pose posestack_pose = posestack.last();
      voxelshape.forAllEdges((d6, d7, d8, d9, d10, d11) -> {
         float f8 = (float)(d9 - d6);
         float f9 = (float)(d10 - d7);
         float f10 = (float)(d11 - d8);
         float f11 = Mth.sqrt(f8 * f8 + f9 * f9 + f10 * f10);
         f8 /= f11;
         f9 /= f11;
         f10 /= f11;
         vertexconsumer.vertex(posestack_pose.pose(), (float)(d6 + d0), (float)(d7 + d1), (float)(d8 + d2)).color(f, f1, f2, f3).normal(posestack_pose.normal(), f8, f9, f10).endVertex();
         vertexconsumer.vertex(posestack_pose.pose(), (float)(d9 + d0), (float)(d10 + d1), (float)(d11 + d2)).color(f, f1, f2, f3).normal(posestack_pose.normal(), f8, f9, f10).endVertex();
      });
   }

   public static void renderLineBox(VertexConsumer vertexconsumer, double d0, double d1, double d2, double d3, double d4, double d5, float f, float f1, float f2, float f3) {
      renderLineBox(new PoseStack(), vertexconsumer, d0, d1, d2, d3, d4, d5, f, f1, f2, f3, f, f1, f2);
   }

   public static void renderLineBox(PoseStack posestack, VertexConsumer vertexconsumer, AABB aabb, float f, float f1, float f2, float f3) {
      renderLineBox(posestack, vertexconsumer, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, f, f1, f2, f3, f, f1, f2);
   }

   public static void renderLineBox(PoseStack posestack, VertexConsumer vertexconsumer, double d0, double d1, double d2, double d3, double d4, double d5, float f, float f1, float f2, float f3) {
      renderLineBox(posestack, vertexconsumer, d0, d1, d2, d3, d4, d5, f, f1, f2, f3, f, f1, f2);
   }

   public static void renderLineBox(PoseStack posestack, VertexConsumer vertexconsumer, double d0, double d1, double d2, double d3, double d4, double d5, float f, float f1, float f2, float f3, float f4, float f5, float f6) {
      Matrix4f matrix4f = posestack.last().pose();
      Matrix3f matrix3f = posestack.last().normal();
      float f7 = (float)d0;
      float f8 = (float)d1;
      float f9 = (float)d2;
      float f10 = (float)d3;
      float f11 = (float)d4;
      float f12 = (float)d5;
      vertexconsumer.vertex(matrix4f, f7, f8, f9).color(f, f5, f6, f3).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f8, f9).color(f, f5, f6, f3).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f8, f9).color(f4, f1, f6, f3).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f11, f9).color(f4, f1, f6, f3).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f8, f9).color(f4, f5, f2, f3).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f8, f12).color(f4, f5, f2, f3).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f8, f9).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f11, f9).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f11, f9).color(f, f1, f2, f3).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f11, f9).color(f, f1, f2, f3).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f11, f9).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f11, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f11, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f8, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f8, f12).color(f, f1, f2, f3).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f8, f12).color(f, f1, f2, f3).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f8, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f8, f9).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f7, f11, f12).color(f, f1, f2, f3).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f11, f12).color(f, f1, f2, f3).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f8, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f11, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f11, f9).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
      vertexconsumer.vertex(matrix4f, f10, f11, f12).color(f, f1, f2, f3).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
   }

   public static void addChainedFilledBoxVertices(PoseStack posestack, VertexConsumer vertexconsumer, double d0, double d1, double d2, double d3, double d4, double d5, float f, float f1, float f2, float f3) {
      addChainedFilledBoxVertices(posestack, vertexconsumer, (float)d0, (float)d1, (float)d2, (float)d3, (float)d4, (float)d5, f, f1, f2, f3);
   }

   public static void addChainedFilledBoxVertices(PoseStack posestack, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9) {
      Matrix4f matrix4f = posestack.last().pose();
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f1, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f2).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
      vertexconsumer.vertex(matrix4f, f3, f4, f5).color(f6, f7, f8, f9).endVertex();
   }

   public void blockChanged(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, BlockState blockstate1, int i) {
      this.setBlockDirty(blockpos, (i & 8) != 0);
   }

   private void setBlockDirty(BlockPos blockpos, boolean flag) {
      for(int i = blockpos.getZ() - 1; i <= blockpos.getZ() + 1; ++i) {
         for(int j = blockpos.getX() - 1; j <= blockpos.getX() + 1; ++j) {
            for(int k = blockpos.getY() - 1; k <= blockpos.getY() + 1; ++k) {
               this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), flag);
            }
         }
      }

   }

   public void setBlocksDirty(int i, int j, int k, int l, int i1, int j1) {
      for(int k1 = k - 1; k1 <= j1 + 1; ++k1) {
         for(int l1 = i - 1; l1 <= l + 1; ++l1) {
            for(int i2 = j - 1; i2 <= i1 + 1; ++i2) {
               this.setSectionDirty(SectionPos.blockToSectionCoord(l1), SectionPos.blockToSectionCoord(i2), SectionPos.blockToSectionCoord(k1));
            }
         }
      }

   }

   public void setBlockDirty(BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
      if (this.minecraft.getModelManager().requiresRender(blockstate, blockstate1)) {
         this.setBlocksDirty(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX(), blockpos.getY(), blockpos.getZ());
      }

   }

   public void setSectionDirtyWithNeighbors(int i, int j, int k) {
      for(int l = k - 1; l <= k + 1; ++l) {
         for(int i1 = i - 1; i1 <= i + 1; ++i1) {
            for(int j1 = j - 1; j1 <= j + 1; ++j1) {
               this.setSectionDirty(i1, j1, l);
            }
         }
      }

   }

   public void setSectionDirty(int i, int j, int k) {
      this.setSectionDirty(i, j, k, false);
   }

   private void setSectionDirty(int i, int j, int k, boolean flag) {
      this.viewArea.setDirty(i, j, k, flag);
   }

   public void playStreamingMusic(@Nullable SoundEvent soundevent, BlockPos blockpos) {
      SoundInstance soundinstance = this.playingRecords.get(blockpos);
      if (soundinstance != null) {
         this.minecraft.getSoundManager().stop(soundinstance);
         this.playingRecords.remove(blockpos);
      }

      if (soundevent != null) {
         RecordItem recorditem = RecordItem.getBySound(soundevent);
         if (recorditem != null) {
            this.minecraft.gui.setNowPlaying(recorditem.getDisplayName());
         }

         SoundInstance var5 = SimpleSoundInstance.forRecord(soundevent, Vec3.atCenterOf(blockpos));
         this.playingRecords.put(blockpos, var5);
         this.minecraft.getSoundManager().play(var5);
      }

      this.notifyNearbyEntities(this.level, blockpos, soundevent != null);
   }

   private void notifyNearbyEntities(Level level, BlockPos blockpos, boolean flag) {
      for(LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, (new AABB(blockpos)).inflate(3.0D))) {
         livingentity.setRecordPlayingNearby(blockpos, flag);
      }

   }

   public void addParticle(ParticleOptions particleoptions, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {
      this.addParticle(particleoptions, flag, false, d0, d1, d2, d3, d4, d5);
   }

   public void addParticle(ParticleOptions particleoptions, boolean flag, boolean flag1, double d0, double d1, double d2, double d3, double d4, double d5) {
      try {
         this.addParticleInternal(particleoptions, flag, flag1, d0, d1, d2, d3, d4, d5);
      } catch (Throwable var19) {
         CrashReport crashreport = CrashReport.forThrowable(var19, "Exception while adding particle");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being added");
         crashreportcategory.setDetail("ID", BuiltInRegistries.PARTICLE_TYPE.getKey(particleoptions.getType()));
         crashreportcategory.setDetail("Parameters", particleoptions.writeToString());
         crashreportcategory.setDetail("Position", () -> CrashReportCategory.formatLocation(this.level, d0, d1, d2));
         throw new ReportedException(crashreport);
      }
   }

   private <T extends ParticleOptions> void addParticle(T particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
      this.addParticle(particleoptions, particleoptions.getType().getOverrideLimiter(), d0, d1, d2, d3, d4, d5);
   }

   @Nullable
   private Particle addParticleInternal(ParticleOptions particleoptions, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {
      return this.addParticleInternal(particleoptions, flag, false, d0, d1, d2, d3, d4, d5);
   }

   @Nullable
   private Particle addParticleInternal(ParticleOptions particleoptions, boolean flag, boolean flag1, double d0, double d1, double d2, double d3, double d4, double d5) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      ParticleStatus particlestatus = this.calculateParticleLevel(flag1);
      if (flag) {
         return this.minecraft.particleEngine.createParticle(particleoptions, d0, d1, d2, d3, d4, d5);
      } else if (camera.getPosition().distanceToSqr(d0, d1, d2) > 1024.0D) {
         return null;
      } else {
         return particlestatus == ParticleStatus.MINIMAL ? null : this.minecraft.particleEngine.createParticle(particleoptions, d0, d1, d2, d3, d4, d5);
      }
   }

   private ParticleStatus calculateParticleLevel(boolean flag) {
      ParticleStatus particlestatus = this.minecraft.options.particles().get();
      if (flag && particlestatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0) {
         particlestatus = ParticleStatus.DECREASED;
      }

      if (particlestatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0) {
         particlestatus = ParticleStatus.MINIMAL;
      }

      return particlestatus;
   }

   public void clear() {
   }

   public void globalLevelEvent(int i, BlockPos blockpos, int j) {
      switch (i) {
         case 1023:
         case 1028:
         case 1038:
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            if (camera.isInitialized()) {
               double d0 = (double)blockpos.getX() - camera.getPosition().x;
               double d1 = (double)blockpos.getY() - camera.getPosition().y;
               double d2 = (double)blockpos.getZ() - camera.getPosition().z;
               double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
               double d4 = camera.getPosition().x;
               double d5 = camera.getPosition().y;
               double d6 = camera.getPosition().z;
               if (d3 > 0.0D) {
                  d4 += d0 / d3 * 2.0D;
                  d5 += d1 / d3 * 2.0D;
                  d6 += d2 / d3 * 2.0D;
               }

               if (i == 1023) {
                  this.level.playLocalSound(d4, d5, d6, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
               } else if (i == 1038) {
                  this.level.playLocalSound(d4, d5, d6, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
               } else {
                  this.level.playLocalSound(d4, d5, d6, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
               }
            }
         default:
      }
   }

   public void levelEvent(int i, BlockPos blockpos, int j) {
      RandomSource randomsource = this.level.random;
      switch (i) {
         case 1000:
            this.level.playLocalSound(blockpos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1001:
            this.level.playLocalSound(blockpos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1002:
            this.level.playLocalSound(blockpos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
            break;
         case 1003:
            this.level.playLocalSound(blockpos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1004:
            this.level.playLocalSound(blockpos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
            break;
         case 1009:
            if (j == 0) {
               this.level.playLocalSound(blockpos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.8F, false);
            } else if (j == 1) {
               this.level.playLocalSound(blockpos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.4F, false);
            }
            break;
         case 1010:
            Item var73 = Item.byId(j);
            if (var73 instanceof RecordItem recorditem) {
               this.playStreamingMusic(recorditem.getSound(), blockpos);
            }
            break;
         case 1011:
            this.playStreamingMusic((SoundEvent)null, blockpos);
            break;
         case 1015:
            this.level.playLocalSound(blockpos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1016:
            this.level.playLocalSound(blockpos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1017:
            this.level.playLocalSound(blockpos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1018:
            this.level.playLocalSound(blockpos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1019:
            this.level.playLocalSound(blockpos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1020:
            this.level.playLocalSound(blockpos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1021:
            this.level.playLocalSound(blockpos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1022:
            this.level.playLocalSound(blockpos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1024:
            this.level.playLocalSound(blockpos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1025:
            this.level.playLocalSound(blockpos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1026:
            this.level.playLocalSound(blockpos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1027:
            this.level.playLocalSound(blockpos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1029:
            this.level.playLocalSound(blockpos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1030:
            this.level.playLocalSound(blockpos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1031:
            this.level.playLocalSound(blockpos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1032:
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, randomsource.nextFloat() * 0.4F + 0.8F, 0.25F));
            break;
         case 1033:
            this.level.playLocalSound(blockpos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1034:
            this.level.playLocalSound(blockpos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1035:
            this.level.playLocalSound(blockpos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 1039:
            this.level.playLocalSound(blockpos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1040:
            this.level.playLocalSound(blockpos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1041:
            this.level.playLocalSound(blockpos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1042:
            this.level.playLocalSound(blockpos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1043:
            this.level.playLocalSound(blockpos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1044:
            this.level.playLocalSound(blockpos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1045:
            this.level.playLocalSound(blockpos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1046:
            this.level.playLocalSound(blockpos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1047:
            this.level.playLocalSound(blockpos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 1048:
            this.level.playLocalSound(blockpos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.HOSTILE, 2.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F, false);
            break;
         case 1500:
            ComposterBlock.handleFill(this.level, blockpos, j > 0);
            break;
         case 1501:
            this.level.playLocalSound(blockpos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.8F, false);

            for(int k3 = 0; k3 < 8; ++k3) {
               this.level.addParticle(ParticleTypes.LARGE_SMOKE, (double)blockpos.getX() + randomsource.nextDouble(), (double)blockpos.getY() + 1.2D, (double)blockpos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
            }
            break;
         case 1502:
            this.level.playLocalSound(blockpos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (randomsource.nextFloat() - randomsource.nextFloat()) * 0.8F, false);

            for(int l3 = 0; l3 < 5; ++l3) {
               double d24 = (double)blockpos.getX() + randomsource.nextDouble() * 0.6D + 0.2D;
               double d25 = (double)blockpos.getY() + randomsource.nextDouble() * 0.6D + 0.2D;
               double d26 = (double)blockpos.getZ() + randomsource.nextDouble() * 0.6D + 0.2D;
               this.level.addParticle(ParticleTypes.SMOKE, d24, d25, d26, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 1503:
            this.level.playLocalSound(blockpos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

            for(int i4 = 0; i4 < 16; ++i4) {
               double d27 = (double)blockpos.getX() + (5.0D + randomsource.nextDouble() * 6.0D) / 16.0D;
               double d28 = (double)blockpos.getY() + 0.8125D;
               double d29 = (double)blockpos.getZ() + (5.0D + randomsource.nextDouble() * 6.0D) / 16.0D;
               this.level.addParticle(ParticleTypes.SMOKE, d27, d28, d29, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 1504:
            PointedDripstoneBlock.spawnDripParticle(this.level, blockpos, this.level.getBlockState(blockpos));
            break;
         case 1505:
            BoneMealItem.addGrowthParticles(this.level, blockpos, j);
            this.level.playLocalSound(blockpos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 2000:
            Direction direction = Direction.from3DDataValue(j);
            int k = direction.getStepX();
            int l = direction.getStepY();
            int i1 = direction.getStepZ();
            double d0 = (double)blockpos.getX() + (double)k * 0.6D + 0.5D;
            double d1 = (double)blockpos.getY() + (double)l * 0.6D + 0.5D;
            double d2 = (double)blockpos.getZ() + (double)i1 * 0.6D + 0.5D;

            for(int j1 = 0; j1 < 10; ++j1) {
               double d3 = randomsource.nextDouble() * 0.2D + 0.01D;
               double d4 = d0 + (double)k * 0.01D + (randomsource.nextDouble() - 0.5D) * (double)i1 * 0.5D;
               double d5 = d1 + (double)l * 0.01D + (randomsource.nextDouble() - 0.5D) * (double)l * 0.5D;
               double d6 = d2 + (double)i1 * 0.01D + (randomsource.nextDouble() - 0.5D) * (double)k * 0.5D;
               double d7 = (double)k * d3 + randomsource.nextGaussian() * 0.01D;
               double d8 = (double)l * d3 + randomsource.nextGaussian() * 0.01D;
               double d9 = (double)i1 * d3 + randomsource.nextGaussian() * 0.01D;
               this.addParticle(ParticleTypes.SMOKE, d4, d5, d6, d7, d8, d9);
            }
            break;
         case 2001:
            BlockState blockstate = Block.stateById(j);
            if (!blockstate.isAir()) {
               SoundType soundtype = blockstate.getSoundType();
               this.level.playLocalSound(blockpos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, false);
            }

            this.level.addDestroyBlockEffect(blockpos, blockstate);
            break;
         case 2002:
         case 2007:
            Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);

            for(int l1 = 0; l1 < 8; ++l1) {
               this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), vec3.x, vec3.y, vec3.z, randomsource.nextGaussian() * 0.15D, randomsource.nextDouble() * 0.2D, randomsource.nextGaussian() * 0.15D);
            }

            float f = (float)(j >> 16 & 255) / 255.0F;
            float f1 = (float)(j >> 8 & 255) / 255.0F;
            float f2 = (float)(j >> 0 & 255) / 255.0F;
            ParticleOptions particleoptions = i == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

            for(int i2 = 0; i2 < 100; ++i2) {
               double d14 = randomsource.nextDouble() * 4.0D;
               double d15 = randomsource.nextDouble() * Math.PI * 2.0D;
               double d16 = Math.cos(d15) * d14;
               double d17 = 0.01D + randomsource.nextDouble() * 0.5D;
               double d18 = Math.sin(d15) * d14;
               Particle particle = this.addParticleInternal(particleoptions, particleoptions.getType().getOverrideLimiter(), vec3.x + d16 * 0.1D, vec3.y + 0.3D, vec3.z + d18 * 0.1D, d16, d17, d18);
               if (particle != null) {
                  float f3 = 0.75F + randomsource.nextFloat() * 0.25F;
                  particle.setColor(f * f3, f1 * f3, f2 * f3);
                  particle.setPower((float)d14);
               }
            }

            this.level.playLocalSound(blockpos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            break;
         case 2003:
            double d10 = (double)blockpos.getX() + 0.5D;
            double d11 = (double)blockpos.getY();
            double d12 = (double)blockpos.getZ() + 0.5D;

            for(int k1 = 0; k1 < 8; ++k1) {
               this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), d10, d11, d12, randomsource.nextGaussian() * 0.15D, randomsource.nextDouble() * 0.2D, randomsource.nextGaussian() * 0.15D);
            }

            for(double d13 = 0.0D; d13 < (Math.PI * 2D); d13 += 0.15707963267948966D) {
               this.addParticle(ParticleTypes.PORTAL, d10 + Math.cos(d13) * 5.0D, d11 - 0.4D, d12 + Math.sin(d13) * 5.0D, Math.cos(d13) * -5.0D, 0.0D, Math.sin(d13) * -5.0D);
               this.addParticle(ParticleTypes.PORTAL, d10 + Math.cos(d13) * 5.0D, d11 - 0.4D, d12 + Math.sin(d13) * 5.0D, Math.cos(d13) * -7.0D, 0.0D, Math.sin(d13) * -7.0D);
            }
            break;
         case 2004:
            for(int j2 = 0; j2 < 20; ++j2) {
               double d19 = (double)blockpos.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
               double d20 = (double)blockpos.getY() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
               double d21 = (double)blockpos.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 2.0D;
               this.level.addParticle(ParticleTypes.SMOKE, d19, d20, d21, 0.0D, 0.0D, 0.0D);
               this.level.addParticle(ParticleTypes.FLAME, d19, d20, d21, 0.0D, 0.0D, 0.0D);
            }
            break;
         case 2005:
            BoneMealItem.addGrowthParticles(this.level, blockpos, j);
            break;
         case 2006:
            for(int j4 = 0; j4 < 200; ++j4) {
               float f14 = randomsource.nextFloat() * 4.0F;
               float f15 = randomsource.nextFloat() * ((float)Math.PI * 2F);
               double d30 = (double)(Mth.cos(f15) * f14);
               double d31 = 0.01D + randomsource.nextDouble() * 0.5D;
               double d32 = (double)(Mth.sin(f15) * f14);
               Particle particle1 = this.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, (double)blockpos.getX() + d30 * 0.1D, (double)blockpos.getY() + 0.3D, (double)blockpos.getZ() + d32 * 0.1D, d30, d31, d32);
               if (particle1 != null) {
                  particle1.setPower(f14);
               }
            }

            if (j == 1) {
               this.level.playLocalSound(blockpos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, randomsource.nextFloat() * 0.1F + 0.9F, false);
            }
            break;
         case 2008:
            this.level.addParticle(ParticleTypes.EXPLOSION, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            break;
         case 2009:
            for(int k4 = 0; k4 < 8; ++k4) {
               this.level.addParticle(ParticleTypes.CLOUD, (double)blockpos.getX() + randomsource.nextDouble(), (double)blockpos.getY() + 1.2D, (double)blockpos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
            }
            break;
         case 3000:
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            this.level.playLocalSound(blockpos, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
            break;
         case 3001:
            this.level.playLocalSound(blockpos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
            break;
         case 3002:
            if (j >= 0 && j < Direction.Axis.VALUES.length) {
               ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[j], this.level, blockpos, 0.125D, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
            } else {
               ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockpos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
            }
            break;
         case 3003:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockpos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
            this.level.playLocalSound(blockpos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            break;
         case 3004:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockpos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
            break;
         case 3005:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockpos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
            break;
         case 3006:
            int k2 = j >> 6;
            if (k2 > 0) {
               if (randomsource.nextFloat() < 0.3F + (float)k2 * 0.1F) {
                  float f4 = 0.15F + 0.02F * (float)k2 * (float)k2 * randomsource.nextFloat();
                  float f5 = 0.4F + 0.3F * (float)k2 * randomsource.nextFloat();
                  this.level.playLocalSound(blockpos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, f4, f5, false);
               }

               byte b0 = (byte)(j & 63);
               IntProvider intprovider = UniformInt.of(0, k2);
               float f6 = 0.005F;
               Supplier<Vec3> supplier = () -> new Vec3(Mth.nextDouble(randomsource, (double)-0.005F, (double)0.005F), Mth.nextDouble(randomsource, (double)-0.005F, (double)0.005F), Mth.nextDouble(randomsource, (double)-0.005F, (double)0.005F));
               if (b0 == 0) {
                  for(Direction direction1 : Direction.values()) {
                     float f7 = direction1 == Direction.DOWN ? (float)Math.PI : 0.0F;
                     double d22 = direction1.getAxis() == Direction.Axis.Y ? 0.65D : 0.57D;
                     ParticleUtils.spawnParticlesOnBlockFace(this.level, blockpos, new SculkChargeParticleOptions(f7), intprovider, direction1, supplier, d22);
                  }
               } else {
                  for(Direction direction2 : MultifaceBlock.unpack(b0)) {
                     float f8 = direction2 == Direction.UP ? (float)Math.PI : 0.0F;
                     double d23 = 0.35D;
                     ParticleUtils.spawnParticlesOnBlockFace(this.level, blockpos, new SculkChargeParticleOptions(f8), intprovider, direction2, supplier, 0.35D);
                  }
               }
            } else {
               this.level.playLocalSound(blockpos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
               boolean flag = this.level.getBlockState(blockpos).isCollisionShapeFullBlock(this.level, blockpos);
               int l2 = flag ? 40 : 20;
               float f9 = flag ? 0.45F : 0.25F;
               float f10 = 0.07F;

               for(int i3 = 0; i3 < l2; ++i3) {
                  float f11 = 2.0F * randomsource.nextFloat() - 1.0F;
                  float f12 = 2.0F * randomsource.nextFloat() - 1.0F;
                  float f13 = 2.0F * randomsource.nextFloat() - 1.0F;
                  this.level.addParticle(ParticleTypes.SCULK_CHARGE_POP, (double)blockpos.getX() + 0.5D + (double)(f11 * f9), (double)blockpos.getY() + 0.5D + (double)(f12 * f9), (double)blockpos.getZ() + 0.5D + (double)(f13 * f9), (double)(f11 * 0.07F), (double)(f12 * 0.07F), (double)(f13 * 0.07F));
               }
            }
            break;
         case 3007:
            for(int j3 = 0; j3 < 10; ++j3) {
               this.level.addParticle(new ShriekParticleOption(j3 * 5), false, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + SculkShriekerBlock.TOP_Y, (double)blockpos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            }

            BlockState blockstate2 = this.level.getBlockState(blockpos);
            boolean flag1 = blockstate2.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate2.getValue(BlockStateProperties.WATERLOGGED);
            if (!flag1) {
               this.level.playLocalSound((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + SculkShriekerBlock.TOP_Y, (double)blockpos.getZ() + 0.5D, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 2.0F, 0.6F + this.level.random.nextFloat() * 0.4F, false);
            }
            break;
         case 3008:
            BlockState blockstate1 = Block.stateById(j);
            Block blockstate2 = blockstate1.getBlock();
            if (blockstate2 instanceof BrushableBlock brushableblock) {
               this.level.playLocalSound(blockpos, brushableblock.getBrushCompletedSound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }

            this.level.addDestroyBlockEffect(blockpos, blockstate1);
            break;
         case 3009:
            ParticleUtils.spawnParticlesOnBlockFaces(this.level, blockpos, ParticleTypes.EGG_CRACK, UniformInt.of(3, 6));
      }

   }

   public void destroyBlockProgress(int i, BlockPos blockpos, int j) {
      if (j >= 0 && j < 10) {
         BlockDestructionProgress blockdestructionprogress1 = this.destroyingBlocks.get(i);
         if (blockdestructionprogress1 != null) {
            this.removeProgress(blockdestructionprogress1);
         }

         if (blockdestructionprogress1 == null || blockdestructionprogress1.getPos().getX() != blockpos.getX() || blockdestructionprogress1.getPos().getY() != blockpos.getY() || blockdestructionprogress1.getPos().getZ() != blockpos.getZ()) {
            blockdestructionprogress1 = new BlockDestructionProgress(i, blockpos);
            this.destroyingBlocks.put(i, blockdestructionprogress1);
         }

         blockdestructionprogress1.setProgress(j);
         blockdestructionprogress1.updateTick(this.ticks);
         this.destructionProgress.computeIfAbsent(blockdestructionprogress1.getPos().asLong(), (k) -> Sets.newTreeSet()).add(blockdestructionprogress1);
      } else {
         BlockDestructionProgress blockdestructionprogress = this.destroyingBlocks.remove(i);
         if (blockdestructionprogress != null) {
            this.removeProgress(blockdestructionprogress);
         }
      }

   }

   public boolean hasRenderedAllChunks() {
      return this.chunkRenderDispatcher.isQueueEmpty();
   }

   public void needsUpdate() {
      this.needsFullRenderChunkUpdate = true;
      this.generateClouds = true;
   }

   public void updateGlobalBlockEntities(Collection<BlockEntity> collection, Collection<BlockEntity> collection1) {
      synchronized(this.globalBlockEntities) {
         this.globalBlockEntities.removeAll(collection);
         this.globalBlockEntities.addAll(collection1);
      }
   }

   public static int getLightColor(BlockAndTintGetter blockandtintgetter, BlockPos blockpos) {
      return getLightColor(blockandtintgetter, blockandtintgetter.getBlockState(blockpos), blockpos);
   }

   public static int getLightColor(BlockAndTintGetter blockandtintgetter, BlockState blockstate, BlockPos blockpos) {
      if (blockstate.emissiveRendering(blockandtintgetter, blockpos)) {
         return 15728880;
      } else {
         int i = blockandtintgetter.getBrightness(LightLayer.SKY, blockpos);
         int j = blockandtintgetter.getBrightness(LightLayer.BLOCK, blockpos);
         int k = blockstate.getLightEmission();
         if (j < k) {
            j = k;
         }

         return i << 20 | j << 4;
      }
   }

   public boolean isChunkCompiled(BlockPos blockpos) {
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk = this.viewArea.getRenderChunkAt(blockpos);
      return chunkrenderdispatcher_renderchunk != null && chunkrenderdispatcher_renderchunk.compiled.get() != ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
   }

   @Nullable
   public RenderTarget entityTarget() {
      return this.entityTarget;
   }

   @Nullable
   public RenderTarget getTranslucentTarget() {
      return this.translucentTarget;
   }

   @Nullable
   public RenderTarget getItemEntityTarget() {
      return this.itemEntityTarget;
   }

   @Nullable
   public RenderTarget getParticlesTarget() {
      return this.particlesTarget;
   }

   @Nullable
   public RenderTarget getWeatherTarget() {
      return this.weatherTarget;
   }

   @Nullable
   public RenderTarget getCloudsTarget() {
      return this.cloudsTarget;
   }

   static class RenderChunkInfo {
      final ChunkRenderDispatcher.RenderChunk chunk;
      private byte sourceDirections;
      byte directions;
      final int step;

      RenderChunkInfo(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk, @Nullable Direction direction, int i) {
         this.chunk = chunkrenderdispatcher_renderchunk;
         if (direction != null) {
            this.addSourceDirection(direction);
         }

         this.step = i;
      }

      public void setDirections(byte b0, Direction direction) {
         this.directions = (byte)(this.directions | b0 | 1 << direction.ordinal());
      }

      public boolean hasDirection(Direction direction) {
         return (this.directions & 1 << direction.ordinal()) > 0;
      }

      public void addSourceDirection(Direction direction) {
         this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << direction.ordinal());
      }

      public boolean hasSourceDirection(int i) {
         return (this.sourceDirections & 1 << i) > 0;
      }

      public boolean hasSourceDirections() {
         return this.sourceDirections != 0;
      }

      public boolean isAxisAlignedWith(int i, int j, int k) {
         BlockPos blockpos = this.chunk.getOrigin();
         return i == blockpos.getX() / 16 || k == blockpos.getZ() / 16 || j == blockpos.getY() / 16;
      }

      public int hashCode() {
         return this.chunk.getOrigin().hashCode();
      }

      public boolean equals(Object object) {
         if (!(object instanceof LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo)) {
            return false;
         } else {
            return this.chunk.getOrigin().equals(levelrenderer_renderchunkinfo.chunk.getOrigin());
         }
      }
   }

   static class RenderChunkStorage {
      public final LevelRenderer.RenderInfoMap renderInfoMap;
      public final LinkedHashSet<LevelRenderer.RenderChunkInfo> renderChunks;

      public RenderChunkStorage(int i) {
         this.renderInfoMap = new LevelRenderer.RenderInfoMap(i);
         this.renderChunks = new LinkedHashSet<>(i);
      }
   }

   static class RenderInfoMap {
      private final LevelRenderer.RenderChunkInfo[] infos;

      RenderInfoMap(int i) {
         this.infos = new LevelRenderer.RenderChunkInfo[i];
      }

      public void put(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk, LevelRenderer.RenderChunkInfo levelrenderer_renderchunkinfo) {
         this.infos[chunkrenderdispatcher_renderchunk.index] = levelrenderer_renderchunkinfo;
      }

      @Nullable
      public LevelRenderer.RenderChunkInfo get(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk) {
         int i = chunkrenderdispatcher_renderchunk.index;
         return i >= 0 && i < this.infos.length ? this.infos[i] : null;
      }
   }

   public static class TransparencyShaderException extends RuntimeException {
      public TransparencyShaderException(String s, Throwable throwable) {
         super(s, throwable);
      }
   }
}
