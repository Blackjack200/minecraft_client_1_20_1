package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class ParticleEngine implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
   private static final ResourceLocation PARTICLES_ATLAS_INFO = new ResourceLocation("particles");
   private static final int MAX_PARTICLES_PER_LAYER = 16384;
   private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
   protected ClientLevel level;
   private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
   private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final RandomSource random = RandomSource.create();
   private final Int2ObjectMap<ParticleProvider<?>> providers = new Int2ObjectOpenHashMap<>();
   private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
   private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.newHashMap();
   private final TextureAtlas textureAtlas;
   private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

   public ParticleEngine(ClientLevel clientlevel, TextureManager texturemanager) {
      this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
      texturemanager.register(this.textureAtlas.location(), this.textureAtlas);
      this.level = clientlevel;
      this.textureManager = texturemanager;
      this.registerProviders();
   }

   private void registerProviders() {
      this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
      this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
      this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
      this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
      this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
      this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
      this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
      this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
      this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
      this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
      this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
      this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
      this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
      this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
      this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
      this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
      this.register(ParticleTypes.DRIPPING_LAVA, DripParticle::createLavaHangParticle);
      this.register(ParticleTypes.FALLING_LAVA, DripParticle::createLavaFallParticle);
      this.register(ParticleTypes.LANDING_LAVA, DripParticle::createLavaLandParticle);
      this.register(ParticleTypes.DRIPPING_WATER, DripParticle::createWaterHangParticle);
      this.register(ParticleTypes.FALLING_WATER, DripParticle::createWaterFallParticle);
      this.register(ParticleTypes.DUST, DustParticle.Provider::new);
      this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
      this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
      this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
      this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
      this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
      this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
      this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
      this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
      this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
      this.register(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Provider::new);
      this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
      this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
      this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
      this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
      this.register(ParticleTypes.SCULK_SOUL, SoulParticle.EmissiveProvider::new);
      this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
      this.register(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Provider::new);
      this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
      this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
      this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
      this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
      this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
      this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
      this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
      this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
      this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
      this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
      this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
      this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
      this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
      this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
      this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
      this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
      this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
      this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
      this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
      this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
      this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
      this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
      this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
      this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
      this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
      this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
      this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
      this.register(ParticleTypes.DRIPPING_HONEY, DripParticle::createHoneyHangParticle);
      this.register(ParticleTypes.FALLING_HONEY, DripParticle::createHoneyFallParticle);
      this.register(ParticleTypes.LANDING_HONEY, DripParticle::createHoneyLandParticle);
      this.register(ParticleTypes.FALLING_NECTAR, DripParticle::createNectarFallParticle);
      this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle::createSporeBlossomFallParticle);
      this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
      this.register(ParticleTypes.ASH, AshParticle.Provider::new);
      this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
      this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
      this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle::createObsidianTearHangParticle);
      this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle::createObsidianTearFallParticle);
      this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle::createObsidianTearLandParticle);
      this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
      this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
      this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
      this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterHangParticle);
      this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterFallParticle);
      this.register(ParticleTypes.CHERRY_LEAVES, (spriteset) -> (simpleparticletype, clientlevel, d0, d1, d2, d3, d4, d5) -> new CherryParticle(clientlevel, d0, d1, d2, spriteset));
      this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaHangParticle);
      this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaFallParticle);
      this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
      this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
      this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
      this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
      this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
      this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
      this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
      this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
      this.register(ParticleTypes.EGG_CRACK, SuspendedTownParticle.EggCrackProvider::new);
   }

   private <T extends ParticleOptions> void register(ParticleType<T> particletype, ParticleProvider<T> particleprovider) {
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(particletype), particleprovider);
   }

   private <T extends ParticleOptions> void register(ParticleType<T> particletype, ParticleProvider.Sprite<T> particleprovider_sprite) {
      this.register(particletype, (spriteset) -> (particleoptions, clientlevel, d0, d1, d2, d3, d4, d5) -> {
            TextureSheetParticle texturesheetparticle = particleprovider_sprite.createParticle(particleoptions, clientlevel, d0, d1, d2, d3, d4, d5);
            if (texturesheetparticle != null) {
               texturesheetparticle.pickSprite(spriteset);
            }

            return texturesheetparticle;
         });
   }

   private <T extends ParticleOptions> void register(ParticleType<T> particletype, ParticleEngine.SpriteParticleRegistration<T> particleengine_spriteparticleregistration) {
      ParticleEngine.MutableSpriteSet particleengine_mutablespriteset = new ParticleEngine.MutableSpriteSet();
      this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particletype), particleengine_mutablespriteset);
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(particletype), particleengine_spriteparticleregistration.create(particleengine_mutablespriteset));
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      CompletableFuture<List<ParticleDefinition>> completablefuture = CompletableFuture.supplyAsync(() -> PARTICLE_LISTER.listMatchingResources(resourcemanager), executor).thenCompose((map) -> {
         List<CompletableFuture<ParticleDefinition>> list1 = new ArrayList<>(map.size());
         map.forEach((resourcelocation1, resource) -> {
            ResourceLocation resourcelocation2 = PARTICLE_LISTER.fileToId(resourcelocation1);
            list1.add(CompletableFuture.supplyAsync(() -> {
               record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
               }

               return new ParticleDefinition(resourcelocation2, this.loadParticleDescription(resourcelocation2, resource));
            }, executor));
         });
         return Util.sequence(list1);
      });
      CompletableFuture<SpriteLoader.Preparations> completablefuture1 = SpriteLoader.create(this.textureAtlas).loadAndStitch(resourcemanager, PARTICLES_ATLAS_INFO, 0, executor).thenCompose(SpriteLoader.Preparations::waitForUpload);
      return CompletableFuture.allOf(completablefuture1, completablefuture).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((ovoid) -> {
         this.clearParticles();
         profilerfiller1.startTick();
         profilerfiller1.push("upload");
         SpriteLoader.Preparations spriteloader_preparations = completablefuture1.join();
         this.textureAtlas.upload(spriteloader_preparations);
         profilerfiller1.popPush("bindSpriteSets");
         Set<ResourceLocation> set = new HashSet<>();
         TextureAtlasSprite textureatlassprite = spriteloader_preparations.missing();
         completablefuture.join().forEach((particleengine_1particledefinition) -> {
            Optional<List<ResourceLocation>> optional = particleengine_1particledefinition.sprites();
            if (!optional.isEmpty()) {
               List<TextureAtlasSprite> list = new ArrayList<>();

               for(ResourceLocation resourcelocation : optional.get()) {
                  TextureAtlasSprite textureatlassprite2 = spriteloader_preparations.regions().get(resourcelocation);
                  if (textureatlassprite2 == null) {
                     set.add(resourcelocation);
                     list.add(textureatlassprite);
                  } else {
                     list.add(textureatlassprite2);
                  }
               }

               if (list.isEmpty()) {
                  list.add(textureatlassprite);
               }

               this.spriteSets.get(particleengine_1particledefinition.id()).rebind(list);
            }
         });
         if (!set.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
         }

         profilerfiller1.pop();
         profilerfiller1.endTick();
      }, executor1);
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }

   private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation resourcelocation, Resource resource) {
      if (!this.spriteSets.containsKey(resourcelocation)) {
         LOGGER.debug("Redundant texture list for particle: {}", (Object)resourcelocation);
         return Optional.empty();
      } else {
         try {
            Reader reader = resource.openAsReader();

            Optional var5;
            try {
               ParticleDescription particledescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
               var5 = Optional.of(particledescription.getTextures());
            } catch (Throwable var7) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (reader != null) {
               reader.close();
            }

            return var5;
         } catch (IOException var8) {
            throw new IllegalStateException("Failed to load description for particle " + resourcelocation, var8);
         }
      }
   }

   public void createTrackingEmitter(Entity entity, ParticleOptions particleoptions) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleoptions));
   }

   public void createTrackingEmitter(Entity entity, ParticleOptions particleoptions, int i) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleoptions, i));
   }

   @Nullable
   public Particle createParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
      Particle particle = this.makeParticle(particleoptions, d0, d1, d2, d3, d4, d5);
      if (particle != null) {
         this.add(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends ParticleOptions> Particle makeParticle(T particleoptions, double d0, double d1, double d2, double d3, double d4, double d5) {
      ParticleProvider<T> particleprovider = this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getId(particleoptions.getType()));
      return particleprovider == null ? null : particleprovider.createParticle(particleoptions, this.level, d0, d1, d2, d3, d4, d5);
   }

   public void add(Particle particle) {
      Optional<ParticleGroup> optional = particle.getParticleGroup();
      if (optional.isPresent()) {
         if (this.hasSpaceInParticleLimit(optional.get())) {
            this.particlesToAdd.add(particle);
            this.updateCount(optional.get(), 1);
         }
      } else {
         this.particlesToAdd.add(particle);
      }

   }

   public void tick() {
      this.particles.forEach((particlerendertype1, queue) -> {
         this.level.getProfiler().push(particlerendertype1.toString());
         this.tickParticleList(queue);
         this.level.getProfiler().pop();
      });
      if (!this.trackingEmitters.isEmpty()) {
         List<TrackingEmitter> list = Lists.newArrayList();

         for(TrackingEmitter trackingemitter : this.trackingEmitters) {
            trackingemitter.tick();
            if (!trackingemitter.isAlive()) {
               list.add(trackingemitter);
            }
         }

         this.trackingEmitters.removeAll(list);
      }

      Particle particle;
      if (!this.particlesToAdd.isEmpty()) {
         while((particle = this.particlesToAdd.poll()) != null) {
            this.particles.computeIfAbsent(particle.getRenderType(), (particlerendertype) -> EvictingQueue.create(16384)).add(particle);
         }
      }

   }

   private void tickParticleList(Collection<Particle> collection) {
      if (!collection.isEmpty()) {
         Iterator<Particle> iterator = collection.iterator();

         while(iterator.hasNext()) {
            Particle particle = iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               particle.getParticleGroup().ifPresent((particlegroup) -> this.updateCount(particlegroup, -1));
               iterator.remove();
            }
         }
      }

   }

   private void updateCount(ParticleGroup particlegroup, int i) {
      this.trackedParticleCounts.addTo(particlegroup, i);
   }

   private void tickParticle(Particle particle) {
      try {
         particle.tick();
      } catch (Throwable var5) {
         CrashReport crashreport = CrashReport.forThrowable(var5, "Ticking Particle");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
         crashreportcategory.setDetail("Particle", particle::toString);
         crashreportcategory.setDetail("Particle Type", particle.getRenderType()::toString);
         throw new ReportedException(crashreport);
      }
   }

   public void render(PoseStack posestack, MultiBufferSource.BufferSource multibuffersource_buffersource, LightTexture lighttexture, Camera camera, float f) {
      lighttexture.turnOnLightLayer();
      RenderSystem.enableDepthTest();
      PoseStack posestack1 = RenderSystem.getModelViewStack();
      posestack1.pushPose();
      posestack1.mulPoseMatrix(posestack.last().pose());
      RenderSystem.applyModelViewMatrix();

      for(ParticleRenderType particlerendertype : RENDER_ORDER) {
         Iterable<Particle> iterable = this.particles.get(particlerendertype);
         if (iterable != null) {
            RenderSystem.setShader(GameRenderer::getParticleShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            particlerendertype.begin(bufferbuilder, this.textureManager);

            for(Particle particle : iterable) {
               try {
                  particle.render(bufferbuilder, camera, f);
               } catch (Throwable var17) {
                  CrashReport crashreport = CrashReport.forThrowable(var17, "Rendering Particle");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                  crashreportcategory.setDetail("Particle", particle::toString);
                  crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                  throw new ReportedException(crashreport);
               }
            }

            particlerendertype.end(tesselator);
         }
      }

      posestack1.popPose();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      lighttexture.turnOffLightLayer();
   }

   public void setLevel(@Nullable ClientLevel clientlevel) {
      this.level = clientlevel;
      this.clearParticles();
      this.trackingEmitters.clear();
   }

   public void destroy(BlockPos blockpos, BlockState blockstate) {
      if (!blockstate.isAir() && blockstate.shouldSpawnParticlesOnBreak()) {
         VoxelShape voxelshape = blockstate.getShape(this.level, blockpos);
         double d0 = 0.25D;
         voxelshape.forAllBoxes((d1, d2, d3, d4, d5, d6) -> {
            double d7 = Math.min(1.0D, d4 - d1);
            double d8 = Math.min(1.0D, d5 - d2);
            double d9 = Math.min(1.0D, d6 - d3);
            int i = Math.max(2, Mth.ceil(d7 / 0.25D));
            int j = Math.max(2, Mth.ceil(d8 / 0.25D));
            int k = Math.max(2, Mth.ceil(d9 / 0.25D));

            for(int l = 0; l < i; ++l) {
               for(int i1 = 0; i1 < j; ++i1) {
                  for(int j1 = 0; j1 < k; ++j1) {
                     double d10 = ((double)l + 0.5D) / (double)i;
                     double d11 = ((double)i1 + 0.5D) / (double)j;
                     double d12 = ((double)j1 + 0.5D) / (double)k;
                     double d13 = d10 * d7 + d1;
                     double d14 = d11 * d8 + d2;
                     double d15 = d12 * d9 + d3;
                     this.add(new TerrainParticle(this.level, (double)blockpos.getX() + d13, (double)blockpos.getY() + d14, (double)blockpos.getZ() + d15, d10 - 0.5D, d11 - 0.5D, d12 - 0.5D, blockstate, blockpos));
                  }
               }
            }

         });
      }
   }

   public void crack(BlockPos blockpos, Direction direction) {
      BlockState blockstate = this.level.getBlockState(blockpos);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         int i = blockpos.getX();
         int j = blockpos.getY();
         int k = blockpos.getZ();
         float f = 0.1F;
         AABB aabb = blockstate.getShape(this.level, blockpos).bounds();
         double d0 = (double)i + this.random.nextDouble() * (aabb.maxX - aabb.minX - (double)0.2F) + (double)0.1F + aabb.minX;
         double d1 = (double)j + this.random.nextDouble() * (aabb.maxY - aabb.minY - (double)0.2F) + (double)0.1F + aabb.minY;
         double d2 = (double)k + this.random.nextDouble() * (aabb.maxZ - aabb.minZ - (double)0.2F) + (double)0.1F + aabb.minZ;
         if (direction == Direction.DOWN) {
            d1 = (double)j + aabb.minY - (double)0.1F;
         }

         if (direction == Direction.UP) {
            d1 = (double)j + aabb.maxY + (double)0.1F;
         }

         if (direction == Direction.NORTH) {
            d2 = (double)k + aabb.minZ - (double)0.1F;
         }

         if (direction == Direction.SOUTH) {
            d2 = (double)k + aabb.maxZ + (double)0.1F;
         }

         if (direction == Direction.WEST) {
            d0 = (double)i + aabb.minX - (double)0.1F;
         }

         if (direction == Direction.EAST) {
            d0 = (double)i + aabb.maxX + (double)0.1F;
         }

         this.add((new TerrainParticle(this.level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, blockpos)).setPower(0.2F).scale(0.6F));
      }
   }

   public String countParticles() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   private boolean hasSpaceInParticleLimit(ParticleGroup particlegroup) {
      return this.trackedParticleCounts.getInt(particlegroup) < particlegroup.getLimit();
   }

   private void clearParticles() {
      this.particles.clear();
      this.particlesToAdd.clear();
      this.trackingEmitters.clear();
      this.trackedParticleCounts.clear();
   }

   static class MutableSpriteSet implements SpriteSet {
      private List<TextureAtlasSprite> sprites;

      public TextureAtlasSprite get(int i, int j) {
         return this.sprites.get(i * (this.sprites.size() - 1) / j);
      }

      public TextureAtlasSprite get(RandomSource randomsource) {
         return this.sprites.get(randomsource.nextInt(this.sprites.size()));
      }

      public void rebind(List<TextureAtlasSprite> list) {
         this.sprites = ImmutableList.copyOf(list);
      }
   }

   @FunctionalInterface
   interface SpriteParticleRegistration<T extends ParticleOptions> {
      ParticleProvider<T> create(SpriteSet spriteset);
   }
}
