package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class GameRenderer implements AutoCloseable {
   private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
   static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEPTH_BUFFER_DEBUG = false;
   public static final float PROJECTION_Z_NEAR = 0.05F;
   private static final float GUI_Z_NEAR = 1000.0F;
   final Minecraft minecraft;
   private final ResourceManager resourceManager;
   private final RandomSource random = RandomSource.create();
   private float renderDistance;
   public final ItemInHandRenderer itemInHandRenderer;
   private final MapRenderer mapRenderer;
   private final RenderBuffers renderBuffers;
   private int tick;
   private float fov;
   private float oldFov;
   private float darkenWorldAmount;
   private float darkenWorldAmountO;
   private boolean renderHand = true;
   private boolean renderBlockOutline = true;
   private long lastScreenshotAttempt;
   private boolean hasWorldScreenshot;
   private long lastActiveTime = Util.getMillis();
   private final LightTexture lightTexture;
   private final OverlayTexture overlayTexture = new OverlayTexture();
   private boolean panoramicMode;
   private float zoom = 1.0F;
   private float zoomX;
   private float zoomY;
   public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
   @Nullable
   private ItemStack itemActivationItem;
   private int itemActivationTicks;
   private float itemActivationOffX;
   private float itemActivationOffY;
   @Nullable
   PostChain postEffect;
   static final ResourceLocation[] EFFECTS = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
   public static final int EFFECT_NONE = EFFECTS.length;
   int effectIndex = EFFECT_NONE;
   private boolean effectActive;
   private final Camera mainCamera = new Camera();
   public ShaderInstance blitShader;
   private final Map<String, ShaderInstance> shaders = Maps.newHashMap();
   @Nullable
   private static ShaderInstance positionShader;
   @Nullable
   private static ShaderInstance positionColorShader;
   @Nullable
   private static ShaderInstance positionColorTexShader;
   @Nullable
   private static ShaderInstance positionTexShader;
   @Nullable
   private static ShaderInstance positionTexColorShader;
   @Nullable
   private static ShaderInstance particleShader;
   @Nullable
   private static ShaderInstance positionColorLightmapShader;
   @Nullable
   private static ShaderInstance positionColorTexLightmapShader;
   @Nullable
   private static ShaderInstance positionTexColorNormalShader;
   @Nullable
   private static ShaderInstance positionTexLightmapColorShader;
   @Nullable
   private static ShaderInstance rendertypeSolidShader;
   @Nullable
   private static ShaderInstance rendertypeCutoutMippedShader;
   @Nullable
   private static ShaderInstance rendertypeCutoutShader;
   @Nullable
   private static ShaderInstance rendertypeTranslucentShader;
   @Nullable
   private static ShaderInstance rendertypeTranslucentMovingBlockShader;
   @Nullable
   private static ShaderInstance rendertypeTranslucentNoCrumblingShader;
   @Nullable
   private static ShaderInstance rendertypeArmorCutoutNoCullShader;
   @Nullable
   private static ShaderInstance rendertypeEntitySolidShader;
   @Nullable
   private static ShaderInstance rendertypeEntityCutoutShader;
   @Nullable
   private static ShaderInstance rendertypeEntityCutoutNoCullShader;
   @Nullable
   private static ShaderInstance rendertypeEntityCutoutNoCullZOffsetShader;
   @Nullable
   private static ShaderInstance rendertypeItemEntityTranslucentCullShader;
   @Nullable
   private static ShaderInstance rendertypeEntityTranslucentCullShader;
   @Nullable
   private static ShaderInstance rendertypeEntityTranslucentShader;
   @Nullable
   private static ShaderInstance rendertypeEntityTranslucentEmissiveShader;
   @Nullable
   private static ShaderInstance rendertypeEntitySmoothCutoutShader;
   @Nullable
   private static ShaderInstance rendertypeBeaconBeamShader;
   @Nullable
   private static ShaderInstance rendertypeEntityDecalShader;
   @Nullable
   private static ShaderInstance rendertypeEntityNoOutlineShader;
   @Nullable
   private static ShaderInstance rendertypeEntityShadowShader;
   @Nullable
   private static ShaderInstance rendertypeEntityAlphaShader;
   @Nullable
   private static ShaderInstance rendertypeEyesShader;
   @Nullable
   private static ShaderInstance rendertypeEnergySwirlShader;
   @Nullable
   private static ShaderInstance rendertypeLeashShader;
   @Nullable
   private static ShaderInstance rendertypeWaterMaskShader;
   @Nullable
   private static ShaderInstance rendertypeOutlineShader;
   @Nullable
   private static ShaderInstance rendertypeArmorGlintShader;
   @Nullable
   private static ShaderInstance rendertypeArmorEntityGlintShader;
   @Nullable
   private static ShaderInstance rendertypeGlintTranslucentShader;
   @Nullable
   private static ShaderInstance rendertypeGlintShader;
   @Nullable
   private static ShaderInstance rendertypeGlintDirectShader;
   @Nullable
   private static ShaderInstance rendertypeEntityGlintShader;
   @Nullable
   private static ShaderInstance rendertypeEntityGlintDirectShader;
   @Nullable
   private static ShaderInstance rendertypeTextShader;
   @Nullable
   private static ShaderInstance rendertypeTextBackgroundShader;
   @Nullable
   private static ShaderInstance rendertypeTextIntensityShader;
   @Nullable
   private static ShaderInstance rendertypeTextSeeThroughShader;
   @Nullable
   private static ShaderInstance rendertypeTextBackgroundSeeThroughShader;
   @Nullable
   private static ShaderInstance rendertypeTextIntensitySeeThroughShader;
   @Nullable
   private static ShaderInstance rendertypeLightningShader;
   @Nullable
   private static ShaderInstance rendertypeTripwireShader;
   @Nullable
   private static ShaderInstance rendertypeEndPortalShader;
   @Nullable
   private static ShaderInstance rendertypeEndGatewayShader;
   @Nullable
   private static ShaderInstance rendertypeLinesShader;
   @Nullable
   private static ShaderInstance rendertypeCrumblingShader;
   @Nullable
   private static ShaderInstance rendertypeGuiShader;
   @Nullable
   private static ShaderInstance rendertypeGuiOverlayShader;
   @Nullable
   private static ShaderInstance rendertypeGuiTextHighlightShader;
   @Nullable
   private static ShaderInstance rendertypeGuiGhostRecipeOverlayShader;

   public GameRenderer(Minecraft minecraft, ItemInHandRenderer iteminhandrenderer, ResourceManager resourcemanager, RenderBuffers renderbuffers) {
      this.minecraft = minecraft;
      this.resourceManager = resourcemanager;
      this.itemInHandRenderer = iteminhandrenderer;
      this.mapRenderer = new MapRenderer(minecraft.getTextureManager());
      this.lightTexture = new LightTexture(this, minecraft);
      this.renderBuffers = renderbuffers;
      this.postEffect = null;
   }

   public void close() {
      this.lightTexture.close();
      this.mapRenderer.close();
      this.overlayTexture.close();
      this.shutdownEffect();
      this.shutdownShaders();
      if (this.blitShader != null) {
         this.blitShader.close();
      }

   }

   public void setRenderHand(boolean flag) {
      this.renderHand = flag;
   }

   public void setRenderBlockOutline(boolean flag) {
      this.renderBlockOutline = flag;
   }

   public void setPanoramicMode(boolean flag) {
      this.panoramicMode = flag;
   }

   public boolean isPanoramicMode() {
      return this.panoramicMode;
   }

   public void shutdownEffect() {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      this.postEffect = null;
      this.effectIndex = EFFECT_NONE;
   }

   public void togglePostEffect() {
      this.effectActive = !this.effectActive;
   }

   public void checkEntityPostEffect(@Nullable Entity entity) {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      this.postEffect = null;
      if (entity instanceof Creeper) {
         this.loadEffect(new ResourceLocation("shaders/post/creeper.json"));
      } else if (entity instanceof Spider) {
         this.loadEffect(new ResourceLocation("shaders/post/spider.json"));
      } else if (entity instanceof EnderMan) {
         this.loadEffect(new ResourceLocation("shaders/post/invert.json"));
      }

   }

   public void cycleEffect() {
      if (this.minecraft.getCameraEntity() instanceof Player) {
         if (this.postEffect != null) {
            this.postEffect.close();
         }

         this.effectIndex = (this.effectIndex + 1) % (EFFECTS.length + 1);
         if (this.effectIndex == EFFECT_NONE) {
            this.postEffect = null;
         } else {
            this.loadEffect(EFFECTS[this.effectIndex]);
         }

      }
   }

   void loadEffect(ResourceLocation resourcelocation) {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      try {
         this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), resourcelocation);
         this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         this.effectActive = true;
      } catch (IOException var3) {
         LOGGER.warn("Failed to load shader: {}", resourcelocation, var3);
         this.effectIndex = EFFECT_NONE;
         this.effectActive = false;
      } catch (JsonSyntaxException var4) {
         LOGGER.warn("Failed to parse shader: {}", resourcelocation, var4);
         this.effectIndex = EFFECT_NONE;
         this.effectActive = false;
      }

   }

   public PreparableReloadListener createReloadListener() {
      return new SimplePreparableReloadListener<GameRenderer.ResourceCache>() {
         protected GameRenderer.ResourceCache prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
            Map<ResourceLocation, Resource> map = resourcemanager.listResources("shaders", (resourcelocation1) -> {
               String s = resourcelocation1.getPath();
               return s.endsWith(".json") || s.endsWith(Program.Type.FRAGMENT.getExtension()) || s.endsWith(Program.Type.VERTEX.getExtension()) || s.endsWith(".glsl");
            });
            Map<ResourceLocation, Resource> map1 = new HashMap<>();
            map.forEach((resourcelocation, resource) -> {
               try {
                  InputStream inputstream = resource.open();

                  try {
                     byte[] abyte = inputstream.readAllBytes();
                     map1.put(resourcelocation, new Resource(resource.source(), () -> new ByteArrayInputStream(abyte)));
                  } catch (Throwable var7) {
                     if (inputstream != null) {
                        try {
                           inputstream.close();
                        } catch (Throwable var6) {
                           var7.addSuppressed(var6);
                        }
                     }

                     throw var7;
                  }

                  if (inputstream != null) {
                     inputstream.close();
                  }
               } catch (Exception var8) {
                  GameRenderer.LOGGER.warn("Failed to read resource {}", resourcelocation, var8);
               }

            });
            return new GameRenderer.ResourceCache(resourcemanager, map1);
         }

         protected void apply(GameRenderer.ResourceCache gamerenderer_resourcecache, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
            GameRenderer.this.reloadShaders(gamerenderer_resourcecache);
            if (GameRenderer.this.postEffect != null) {
               GameRenderer.this.postEffect.close();
            }

            GameRenderer.this.postEffect = null;
            if (GameRenderer.this.effectIndex == GameRenderer.EFFECT_NONE) {
               GameRenderer.this.checkEntityPostEffect(GameRenderer.this.minecraft.getCameraEntity());
            } else {
               GameRenderer.this.loadEffect(GameRenderer.EFFECTS[GameRenderer.this.effectIndex]);
            }

         }

         public String getName() {
            return "Shader Loader";
         }
      };
   }

   public void preloadUiShader(ResourceProvider resourceprovider) {
      if (this.blitShader != null) {
         throw new RuntimeException("Blit shader already preloaded");
      } else {
         try {
            this.blitShader = new ShaderInstance(resourceprovider, "blit_screen", DefaultVertexFormat.BLIT_SCREEN);
         } catch (IOException var3) {
            throw new RuntimeException("could not preload blit shader", var3);
         }

         rendertypeGuiShader = this.preloadShader(resourceprovider, "rendertype_gui", DefaultVertexFormat.POSITION_COLOR);
         rendertypeGuiOverlayShader = this.preloadShader(resourceprovider, "rendertype_gui_overlay", DefaultVertexFormat.POSITION_COLOR);
         positionShader = this.preloadShader(resourceprovider, "position", DefaultVertexFormat.POSITION);
         positionColorShader = this.preloadShader(resourceprovider, "position_color", DefaultVertexFormat.POSITION_COLOR);
         positionColorTexShader = this.preloadShader(resourceprovider, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX);
         positionTexShader = this.preloadShader(resourceprovider, "position_tex", DefaultVertexFormat.POSITION_TEX);
         positionTexColorShader = this.preloadShader(resourceprovider, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR);
         rendertypeTextShader = this.preloadShader(resourceprovider, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
      }
   }

   private ShaderInstance preloadShader(ResourceProvider resourceprovider, String s, VertexFormat vertexformat) {
      try {
         ShaderInstance shaderinstance = new ShaderInstance(resourceprovider, s, vertexformat);
         this.shaders.put(s, shaderinstance);
         return shaderinstance;
      } catch (Exception var5) {
         throw new IllegalStateException("could not preload shader " + s, var5);
      }
   }

   void reloadShaders(ResourceProvider resourceprovider) {
      RenderSystem.assertOnRenderThread();
      List<Program> list = Lists.newArrayList();
      list.addAll(Program.Type.FRAGMENT.getPrograms().values());
      list.addAll(Program.Type.VERTEX.getPrograms().values());
      list.forEach(Program::close);
      List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list1 = Lists.newArrayListWithCapacity(this.shaders.size());

      try {
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "particle", DefaultVertexFormat.PARTICLE), (shaderinstance59) -> particleShader = shaderinstance59));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position", DefaultVertexFormat.POSITION), (shaderinstance58) -> positionShader = shaderinstance58));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_color", DefaultVertexFormat.POSITION_COLOR), (shaderinstance57) -> positionColorShader = shaderinstance57));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_color_lightmap", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (shaderinstance56) -> positionColorLightmapShader = shaderinstance56));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX), (shaderinstance55) -> positionColorTexShader = shaderinstance55));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (shaderinstance54) -> positionColorTexLightmapShader = shaderinstance54));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_tex", DefaultVertexFormat.POSITION_TEX), (shaderinstance53) -> positionTexShader = shaderinstance53));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR), (shaderinstance52) -> positionTexColorShader = shaderinstance52));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_tex_color_normal", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), (shaderinstance51) -> positionTexColorNormalShader = shaderinstance51));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "position_tex_lightmap_color", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR), (shaderinstance50) -> positionTexLightmapColorShader = shaderinstance50));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_solid", DefaultVertexFormat.BLOCK), (shaderinstance49) -> rendertypeSolidShader = shaderinstance49));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_cutout_mipped", DefaultVertexFormat.BLOCK), (shaderinstance48) -> rendertypeCutoutMippedShader = shaderinstance48));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_cutout", DefaultVertexFormat.BLOCK), (shaderinstance47) -> rendertypeCutoutShader = shaderinstance47));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_translucent", DefaultVertexFormat.BLOCK), (shaderinstance46) -> rendertypeTranslucentShader = shaderinstance46));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_translucent_moving_block", DefaultVertexFormat.BLOCK), (shaderinstance45) -> rendertypeTranslucentMovingBlockShader = shaderinstance45));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_translucent_no_crumbling", DefaultVertexFormat.BLOCK), (shaderinstance44) -> rendertypeTranslucentNoCrumblingShader = shaderinstance44));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY), (shaderinstance43) -> rendertypeArmorCutoutNoCullShader = shaderinstance43));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_solid", DefaultVertexFormat.NEW_ENTITY), (shaderinstance42) -> rendertypeEntitySolidShader = shaderinstance42));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_cutout", DefaultVertexFormat.NEW_ENTITY), (shaderinstance41) -> rendertypeEntityCutoutShader = shaderinstance41));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY), (shaderinstance40) -> rendertypeEntityCutoutNoCullShader = shaderinstance40));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY), (shaderinstance39) -> rendertypeEntityCutoutNoCullZOffsetShader = shaderinstance39));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY), (shaderinstance38) -> rendertypeItemEntityTranslucentCullShader = shaderinstance38));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY), (shaderinstance37) -> rendertypeEntityTranslucentCullShader = shaderinstance37));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_translucent", DefaultVertexFormat.NEW_ENTITY), (shaderinstance36) -> rendertypeEntityTranslucentShader = shaderinstance36));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_translucent_emissive", DefaultVertexFormat.NEW_ENTITY), (shaderinstance35) -> rendertypeEntityTranslucentEmissiveShader = shaderinstance35));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY), (shaderinstance34) -> rendertypeEntitySmoothCutoutShader = shaderinstance34));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_beacon_beam", DefaultVertexFormat.BLOCK), (shaderinstance33) -> rendertypeBeaconBeamShader = shaderinstance33));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_decal", DefaultVertexFormat.NEW_ENTITY), (shaderinstance32) -> rendertypeEntityDecalShader = shaderinstance32));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_no_outline", DefaultVertexFormat.NEW_ENTITY), (shaderinstance31) -> rendertypeEntityNoOutlineShader = shaderinstance31));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_shadow", DefaultVertexFormat.NEW_ENTITY), (shaderinstance30) -> rendertypeEntityShadowShader = shaderinstance30));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_alpha", DefaultVertexFormat.NEW_ENTITY), (shaderinstance29) -> rendertypeEntityAlphaShader = shaderinstance29));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_eyes", DefaultVertexFormat.NEW_ENTITY), (shaderinstance28) -> rendertypeEyesShader = shaderinstance28));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_energy_swirl", DefaultVertexFormat.NEW_ENTITY), (shaderinstance27) -> rendertypeEnergySwirlShader = shaderinstance27));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_leash", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (shaderinstance26) -> rendertypeLeashShader = shaderinstance26));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_water_mask", DefaultVertexFormat.POSITION), (shaderinstance25) -> rendertypeWaterMaskShader = shaderinstance25));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_outline", DefaultVertexFormat.POSITION_COLOR_TEX), (shaderinstance24) -> rendertypeOutlineShader = shaderinstance24));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_armor_glint", DefaultVertexFormat.POSITION_TEX), (shaderinstance23) -> rendertypeArmorGlintShader = shaderinstance23));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_armor_entity_glint", DefaultVertexFormat.POSITION_TEX), (shaderinstance22) -> rendertypeArmorEntityGlintShader = shaderinstance22));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_glint_translucent", DefaultVertexFormat.POSITION_TEX), (shaderinstance21) -> rendertypeGlintTranslucentShader = shaderinstance21));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_glint", DefaultVertexFormat.POSITION_TEX), (shaderinstance20) -> rendertypeGlintShader = shaderinstance20));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_glint_direct", DefaultVertexFormat.POSITION_TEX), (shaderinstance19) -> rendertypeGlintDirectShader = shaderinstance19));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_glint", DefaultVertexFormat.POSITION_TEX), (shaderinstance18) -> rendertypeEntityGlintShader = shaderinstance18));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_entity_glint_direct", DefaultVertexFormat.POSITION_TEX), (shaderinstance17) -> rendertypeEntityGlintDirectShader = shaderinstance17));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (shaderinstance16) -> rendertypeTextShader = shaderinstance16));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_text_background", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (shaderinstance15) -> rendertypeTextBackgroundShader = shaderinstance15));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_text_intensity", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (shaderinstance14) -> rendertypeTextIntensityShader = shaderinstance14));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (shaderinstance13) -> rendertypeTextSeeThroughShader = shaderinstance13));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_text_background_see_through", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (shaderinstance12) -> rendertypeTextBackgroundSeeThroughShader = shaderinstance12));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_text_intensity_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (shaderinstance11) -> rendertypeTextIntensitySeeThroughShader = shaderinstance11));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_lightning", DefaultVertexFormat.POSITION_COLOR), (shaderinstance10) -> rendertypeLightningShader = shaderinstance10));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_tripwire", DefaultVertexFormat.BLOCK), (shaderinstance9) -> rendertypeTripwireShader = shaderinstance9));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_end_portal", DefaultVertexFormat.POSITION), (shaderinstance8) -> rendertypeEndPortalShader = shaderinstance8));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_end_gateway", DefaultVertexFormat.POSITION), (shaderinstance7) -> rendertypeEndGatewayShader = shaderinstance7));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL), (shaderinstance6) -> rendertypeLinesShader = shaderinstance6));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_crumbling", DefaultVertexFormat.BLOCK), (shaderinstance5) -> rendertypeCrumblingShader = shaderinstance5));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_gui", DefaultVertexFormat.POSITION_COLOR), (shaderinstance4) -> rendertypeGuiShader = shaderinstance4));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_gui_overlay", DefaultVertexFormat.POSITION_COLOR), (shaderinstance3) -> rendertypeGuiOverlayShader = shaderinstance3));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_gui_text_highlight", DefaultVertexFormat.POSITION_COLOR), (shaderinstance2) -> rendertypeGuiTextHighlightShader = shaderinstance2));
         list1.add(Pair.of(new ShaderInstance(resourceprovider, "rendertype_gui_ghost_recipe_overlay", DefaultVertexFormat.POSITION_COLOR), (shaderinstance1) -> rendertypeGuiGhostRecipeOverlayShader = shaderinstance1));
      } catch (IOException var5) {
         list1.forEach((pair1) -> pair1.getFirst().close());
         throw new RuntimeException("could not reload shaders", var5);
      }

      this.shutdownShaders();
      list1.forEach((pair) -> {
         ShaderInstance shaderinstance = pair.getFirst();
         this.shaders.put(shaderinstance.getName(), shaderinstance);
         pair.getSecond().accept(shaderinstance);
      });
   }

   private void shutdownShaders() {
      RenderSystem.assertOnRenderThread();
      this.shaders.values().forEach(ShaderInstance::close);
      this.shaders.clear();
   }

   @Nullable
   public ShaderInstance getShader(@Nullable String s) {
      return s == null ? null : this.shaders.get(s);
   }

   public void tick() {
      this.tickFov();
      this.lightTexture.tick();
      if (this.minecraft.getCameraEntity() == null) {
         this.minecraft.setCameraEntity(this.minecraft.player);
      }

      this.mainCamera.tick();
      ++this.tick;
      this.itemInHandRenderer.tick();
      this.minecraft.levelRenderer.tickRain(this.mainCamera);
      this.darkenWorldAmountO = this.darkenWorldAmount;
      if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
         this.darkenWorldAmount += 0.05F;
         if (this.darkenWorldAmount > 1.0F) {
            this.darkenWorldAmount = 1.0F;
         }
      } else if (this.darkenWorldAmount > 0.0F) {
         this.darkenWorldAmount -= 0.0125F;
      }

      if (this.itemActivationTicks > 0) {
         --this.itemActivationTicks;
         if (this.itemActivationTicks == 0) {
            this.itemActivationItem = null;
         }
      }

   }

   @Nullable
   public PostChain currentEffect() {
      return this.postEffect;
   }

   public void resize(int i, int j) {
      if (this.postEffect != null) {
         this.postEffect.resize(i, j);
      }

      this.minecraft.levelRenderer.resize(i, j);
   }

   public void pick(float f) {
      Entity entity = this.minecraft.getCameraEntity();
      if (entity != null) {
         if (this.minecraft.level != null) {
            this.minecraft.getProfiler().push("pick");
            this.minecraft.crosshairPickEntity = null;
            double d0 = (double)this.minecraft.gameMode.getPickRange();
            this.minecraft.hitResult = entity.pick(d0, f, false);
            Vec3 vec3 = entity.getEyePosition(f);
            boolean flag = false;
            int i = 3;
            double d1 = d0;
            if (this.minecraft.gameMode.hasFarPickRange()) {
               d1 = 6.0D;
               d0 = d1;
            } else {
               if (d0 > 3.0D) {
                  flag = true;
               }

               d0 = d0;
            }

            d1 *= d1;
            if (this.minecraft.hitResult != null) {
               d1 = this.minecraft.hitResult.getLocation().distanceToSqr(vec3);
            }

            Vec3 vec31 = entity.getViewVector(1.0F);
            Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
            float f1 = 1.0F;
            AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(entity, vec3, vec32, aabb, (entity2) -> !entity2.isSpectator() && entity2.isPickable(), d1);
            if (entityhitresult != null) {
               Entity entity1 = entityhitresult.getEntity();
               Vec3 vec33 = entityhitresult.getLocation();
               double d2 = vec3.distanceToSqr(vec33);
               if (flag && d2 > 9.0D) {
                  this.minecraft.hitResult = BlockHitResult.miss(vec33, Direction.getNearest(vec31.x, vec31.y, vec31.z), BlockPos.containing(vec33));
               } else if (d2 < d1 || this.minecraft.hitResult == null) {
                  this.minecraft.hitResult = entityhitresult;
                  if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrame) {
                     this.minecraft.crosshairPickEntity = entity1;
                  }
               }
            }

            this.minecraft.getProfiler().pop();
         }
      }
   }

   private void tickFov() {
      float f = 1.0F;
      if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer) {
         AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)this.minecraft.getCameraEntity();
         f = abstractclientplayer.getFieldOfViewModifier();
      }

      this.oldFov = this.fov;
      this.fov += (f - this.fov) * 0.5F;
      if (this.fov > 1.5F) {
         this.fov = 1.5F;
      }

      if (this.fov < 0.1F) {
         this.fov = 0.1F;
      }

   }

   private double getFov(Camera camera, float f, boolean flag) {
      if (this.panoramicMode) {
         return 90.0D;
      } else {
         double d0 = 70.0D;
         if (flag) {
            d0 = (double)this.minecraft.options.fov().get().intValue();
            d0 *= (double)Mth.lerp(f, this.oldFov, this.fov);
         }

         if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isDeadOrDying()) {
            float f1 = Math.min((float)((LivingEntity)camera.getEntity()).deathTime + f, 20.0F);
            d0 /= (double)((1.0F - 500.0F / (f1 + 500.0F)) * 2.0F + 1.0F);
         }

         FogType fogtype = camera.getFluidInCamera();
         if (fogtype == FogType.LAVA || fogtype == FogType.WATER) {
            d0 *= Mth.lerp(this.minecraft.options.fovEffectScale().get(), 1.0D, (double)0.85714287F);
         }

         return d0;
      }
   }

   private void bobHurt(PoseStack posestack, float f) {
      if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)this.minecraft.getCameraEntity();
         float f1 = (float)livingentity.hurtTime - f;
         if (livingentity.isDeadOrDying()) {
            float f2 = Math.min((float)livingentity.deathTime + f, 20.0F);
            posestack.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (f2 + 200.0F)));
         }

         if (f1 < 0.0F) {
            return;
         }

         f1 /= (float)livingentity.hurtDuration;
         f1 = Mth.sin(f1 * f1 * f1 * f1 * (float)Math.PI);
         float f3 = livingentity.getHurtDir();
         posestack.mulPose(Axis.YP.rotationDegrees(-f3));
         float f4 = (float)((double)(-f1) * 14.0D * this.minecraft.options.damageTiltStrength().get());
         posestack.mulPose(Axis.ZP.rotationDegrees(f4));
         posestack.mulPose(Axis.YP.rotationDegrees(f3));
      }

   }

   private void bobView(PoseStack posestack, float f) {
      if (this.minecraft.getCameraEntity() instanceof Player) {
         Player player = (Player)this.minecraft.getCameraEntity();
         float f1 = player.walkDist - player.walkDistO;
         float f2 = -(player.walkDist + f1 * f);
         float f3 = Mth.lerp(f, player.oBob, player.bob);
         posestack.translate(Mth.sin(f2 * (float)Math.PI) * f3 * 0.5F, -Math.abs(Mth.cos(f2 * (float)Math.PI) * f3), 0.0F);
         posestack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f2 * (float)Math.PI) * f3 * 3.0F));
         posestack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f2 * (float)Math.PI - 0.2F) * f3) * 5.0F));
      }
   }

   public void renderZoomed(float f, float f1, float f2) {
      this.zoom = f;
      this.zoomX = f1;
      this.zoomY = f2;
      this.setRenderBlockOutline(false);
      this.setRenderHand(false);
      this.renderLevel(1.0F, 0L, new PoseStack());
      this.zoom = 1.0F;
   }

   private void renderItemInHand(PoseStack posestack, Camera camera, float f) {
      if (!this.panoramicMode) {
         this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(camera, f, false)));
         posestack.setIdentity();
         posestack.pushPose();
         this.bobHurt(posestack, f);
         if (this.minecraft.options.bobView().get()) {
            this.bobView(posestack, f);
         }

         boolean flag = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
         if (this.minecraft.options.getCameraType().isFirstPerson() && !flag && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.lightTexture.turnOnLightLayer();
            this.itemInHandRenderer.renderHandsWithItems(f, posestack, this.renderBuffers.bufferSource(), this.minecraft.player, this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, f));
            this.lightTexture.turnOffLightLayer();
         }

         posestack.popPose();
         if (this.minecraft.options.getCameraType().isFirstPerson() && !flag) {
            ScreenEffectRenderer.renderScreenEffect(this.minecraft, posestack);
            this.bobHurt(posestack, f);
         }

         if (this.minecraft.options.bobView().get()) {
            this.bobView(posestack, f);
         }

      }
   }

   public void resetProjectionMatrix(Matrix4f matrix4f) {
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.DISTANCE_TO_ORIGIN);
   }

   public Matrix4f getProjectionMatrix(double d0) {
      PoseStack posestack = new PoseStack();
      posestack.last().pose().identity();
      if (this.zoom != 1.0F) {
         posestack.translate(this.zoomX, -this.zoomY, 0.0F);
         posestack.scale(this.zoom, this.zoom, 1.0F);
      }

      posestack.last().pose().mul((new Matrix4f()).setPerspective((float)(d0 * (double)((float)Math.PI / 180F)), (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(), 0.05F, this.getDepthFar()));
      return posestack.last().pose();
   }

   public float getDepthFar() {
      return this.renderDistance * 4.0F;
   }

   public static float getNightVisionScale(LivingEntity livingentity, float f) {
      MobEffectInstance mobeffectinstance = livingentity.getEffect(MobEffects.NIGHT_VISION);
      return !mobeffectinstance.endsWithin(200) ? 1.0F : 0.7F + Mth.sin(((float)mobeffectinstance.getDuration() - f) * (float)Math.PI * 0.2F) * 0.3F;
   }

   public void render(float f, long i, boolean flag) {
      if (!this.minecraft.isWindowActive() && this.minecraft.options.pauseOnLostFocus && (!this.minecraft.options.touchscreen().get() || !this.minecraft.mouseHandler.isRightPressed())) {
         if (Util.getMillis() - this.lastActiveTime > 500L) {
            this.minecraft.pauseGame(false);
         }
      } else {
         this.lastActiveTime = Util.getMillis();
      }

      if (!this.minecraft.noRender) {
         int j = (int)(this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth());
         int k = (int)(this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight());
         RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         if (flag && this.minecraft.level != null) {
            this.minecraft.getProfiler().push("level");
            this.renderLevel(f, i, new PoseStack());
            this.tryTakeScreenshotIfNeeded();
            this.minecraft.levelRenderer.doEntityOutline();
            if (this.postEffect != null && this.effectActive) {
               RenderSystem.disableBlend();
               RenderSystem.disableDepthTest();
               RenderSystem.resetTextureMatrix();
               this.postEffect.process(f);
            }

            this.minecraft.getMainRenderTarget().bindWrite(true);
         }

         Window window = this.minecraft.getWindow();
         RenderSystem.clear(256, Minecraft.ON_OSX);
         Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, 21000.0F);
         RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
         PoseStack posestack = RenderSystem.getModelViewStack();
         posestack.pushPose();
         posestack.setIdentity();
         posestack.translate(0.0F, 0.0F, -11000.0F);
         RenderSystem.applyModelViewMatrix();
         Lighting.setupFor3DItems();
         GuiGraphics guigraphics = new GuiGraphics(this.minecraft, this.renderBuffers.bufferSource());
         if (flag && this.minecraft.level != null) {
            this.minecraft.getProfiler().popPush("gui");
            if (this.minecraft.player != null) {
               float f1 = Mth.lerp(f, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
               float f2 = this.minecraft.options.screenEffectScale().get().floatValue();
               if (f1 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONFUSION) && f2 < 1.0F) {
                  this.renderConfusionOverlay(guigraphics, f1 * (1.0F - f2));
               }
            }

            if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
               this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), f);
               this.minecraft.gui.render(guigraphics, f);
               RenderSystem.clear(256, Minecraft.ON_OSX);
            }

            this.minecraft.getProfiler().pop();
         }

         if (this.minecraft.getOverlay() != null) {
            try {
               this.minecraft.getOverlay().render(guigraphics, j, k, this.minecraft.getDeltaFrameTime());
            } catch (Throwable var16) {
               CrashReport crashreport = CrashReport.forThrowable(var16, "Rendering overlay");
               CrashReportCategory crashreportcategory = crashreport.addCategory("Overlay render details");
               crashreportcategory.setDetail("Overlay name", () -> this.minecraft.getOverlay().getClass().getCanonicalName());
               throw new ReportedException(crashreport);
            }
         } else if (this.minecraft.screen != null) {
            try {
               this.minecraft.screen.renderWithTooltip(guigraphics, j, k, this.minecraft.getDeltaFrameTime());
            } catch (Throwable var15) {
               CrashReport crashreport1 = CrashReport.forThrowable(var15, "Rendering screen");
               CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Screen render details");
               crashreportcategory1.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
               crashreportcategory1.setDetail("Mouse location", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", j, k, this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos()));
               crashreportcategory1.setDetail("Screen size", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getWindow().getGuiScale()));
               throw new ReportedException(crashreport1);
            }

            try {
               if (this.minecraft.screen != null) {
                  this.minecraft.screen.handleDelayedNarration();
               }
            } catch (Throwable var14) {
               CrashReport crashreport2 = CrashReport.forThrowable(var14, "Narrating screen");
               CrashReportCategory crashreportcategory2 = crashreport2.addCategory("Screen details");
               crashreportcategory2.setDetail("Screen name", () -> this.minecraft.screen.getClass().getCanonicalName());
               throw new ReportedException(crashreport2);
            }
         }

         this.minecraft.getProfiler().push("toasts");
         this.minecraft.getToasts().render(guigraphics);
         this.minecraft.getProfiler().pop();
         guigraphics.flush();
         posestack.popPose();
         RenderSystem.applyModelViewMatrix();
      }
   }

   private void tryTakeScreenshotIfNeeded() {
      if (!this.hasWorldScreenshot && this.minecraft.isLocalServer()) {
         long i = Util.getMillis();
         if (i - this.lastScreenshotAttempt >= 1000L) {
            this.lastScreenshotAttempt = i;
            IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
            if (integratedserver != null && !integratedserver.isStopped()) {
               integratedserver.getWorldScreenshotFile().ifPresent((path) -> {
                  if (Files.isRegularFile(path)) {
                     this.hasWorldScreenshot = true;
                  } else {
                     this.takeAutoScreenshot(path);
                  }

               });
            }
         }
      }
   }

   private void takeAutoScreenshot(Path path) {
      if (this.minecraft.levelRenderer.countRenderedChunks() > 10 && this.minecraft.levelRenderer.hasRenderedAllChunks()) {
         NativeImage nativeimage = Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget());
         Util.ioPool().execute(() -> {
            int i = nativeimage.getWidth();
            int j = nativeimage.getHeight();
            int k = 0;
            int l = 0;
            if (i > j) {
               k = (i - j) / 2;
               i = j;
            } else {
               l = (j - i) / 2;
               j = i;
            }

            try {
               NativeImage nativeimage2 = new NativeImage(64, 64, false);

               try {
                  nativeimage.resizeSubRectTo(k, l, i, j, nativeimage2);
                  nativeimage2.writeToFile(path);
               } catch (Throwable var15) {
                  try {
                     nativeimage2.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }

                  throw var15;
               }

               nativeimage2.close();
            } catch (IOException var16) {
               LOGGER.warn("Couldn't save auto screenshot", (Throwable)var16);
            } finally {
               nativeimage.close();
            }

         });
      }

   }

   private boolean shouldRenderBlockOutline() {
      if (!this.renderBlockOutline) {
         return false;
      } else {
         Entity entity = this.minecraft.getCameraEntity();
         boolean flag = entity instanceof Player && !this.minecraft.options.hideGui;
         if (flag && !((Player)entity).getAbilities().mayBuild) {
            ItemStack itemstack = ((LivingEntity)entity).getMainHandItem();
            HitResult hitresult = this.minecraft.hitResult;
            if (hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
               BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
               BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
               if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                  flag = blockstate.getMenuProvider(this.minecraft.level, blockpos) != null;
               } else {
                  BlockInWorld blockinworld = new BlockInWorld(this.minecraft.level, blockpos, false);
                  Registry<Block> registry = this.minecraft.level.registryAccess().registryOrThrow(Registries.BLOCK);
                  flag = !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(registry, blockinworld) || itemstack.hasAdventureModePlaceTagForBlock(registry, blockinworld));
               }
            }
         }

         return flag;
      }
   }

   public void renderLevel(float f, long i, PoseStack posestack) {
      this.lightTexture.updateLightTexture(f);
      if (this.minecraft.getCameraEntity() == null) {
         this.minecraft.setCameraEntity(this.minecraft.player);
      }

      this.pick(f);
      this.minecraft.getProfiler().push("center");
      boolean flag = this.shouldRenderBlockOutline();
      this.minecraft.getProfiler().popPush("camera");
      Camera camera = this.mainCamera;
      this.renderDistance = (float)(this.minecraft.options.getEffectiveRenderDistance() * 16);
      PoseStack posestack1 = new PoseStack();
      double d0 = this.getFov(camera, f, true);
      posestack1.mulPoseMatrix(this.getProjectionMatrix(d0));
      this.bobHurt(posestack1, f);
      if (this.minecraft.options.bobView().get()) {
         this.bobView(posestack1, f);
      }

      float f1 = this.minecraft.options.screenEffectScale().get().floatValue();
      float f2 = Mth.lerp(f, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity) * f1 * f1;
      if (f2 > 0.0F) {
         int j = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
         float f3 = 5.0F / (f2 * f2 + 5.0F) - f2 * 0.04F;
         f3 *= f3;
         Axis axis = Axis.of(new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F));
         posestack1.mulPose(axis.rotationDegrees(((float)this.tick + f) * (float)j));
         posestack1.scale(1.0F / f3, 1.0F, 1.0F);
         float f4 = -((float)this.tick + f) * (float)j;
         posestack1.mulPose(axis.rotationDegrees(f4));
      }

      Matrix4f matrix4f = posestack1.last().pose();
      this.resetProjectionMatrix(matrix4f);
      camera.setup(this.minecraft.level, (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()), !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), f);
      posestack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
      posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
      Matrix3f matrix3f = (new Matrix3f(posestack.last().normal())).invert();
      RenderSystem.setInverseViewRotationMatrix(matrix3f);
      this.minecraft.levelRenderer.prepareCullFrustum(posestack, camera.getPosition(), this.getProjectionMatrix(Math.max(d0, (double)this.minecraft.options.fov().get().intValue())));
      this.minecraft.levelRenderer.renderLevel(posestack, f, i, flag, camera, this, this.lightTexture, matrix4f);
      this.minecraft.getProfiler().popPush("hand");
      if (this.renderHand) {
         RenderSystem.clear(256, Minecraft.ON_OSX);
         this.renderItemInHand(posestack, camera, f);
      }

      this.minecraft.getProfiler().pop();
   }

   public void resetData() {
      this.itemActivationItem = null;
      this.mapRenderer.resetData();
      this.mainCamera.reset();
      this.hasWorldScreenshot = false;
   }

   public MapRenderer getMapRenderer() {
      return this.mapRenderer;
   }

   public void displayItemActivation(ItemStack itemstack) {
      this.itemActivationItem = itemstack;
      this.itemActivationTicks = 40;
      this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
      this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
   }

   private void renderItemActivationAnimation(int i, int j, float f) {
      if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
         int k = 40 - this.itemActivationTicks;
         float f1 = ((float)k + f) / 40.0F;
         float f2 = f1 * f1;
         float f3 = f1 * f2;
         float f4 = 10.25F * f3 * f2 - 24.95F * f2 * f2 + 25.5F * f3 - 13.8F * f2 + 4.0F * f1;
         float f5 = f4 * (float)Math.PI;
         float f6 = this.itemActivationOffX * (float)(i / 4);
         float f7 = this.itemActivationOffY * (float)(j / 4);
         RenderSystem.enableDepthTest();
         RenderSystem.disableCull();
         PoseStack posestack = new PoseStack();
         posestack.pushPose();
         posestack.translate((float)(i / 2) + f6 * Mth.abs(Mth.sin(f5 * 2.0F)), (float)(j / 2) + f7 * Mth.abs(Mth.sin(f5 * 2.0F)), -50.0F);
         float f8 = 50.0F + 175.0F * Mth.sin(f5);
         posestack.scale(f8, -f8, f8);
         posestack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(f5))));
         posestack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(f1 * 8.0F)));
         posestack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(f1 * 8.0F)));
         MultiBufferSource.BufferSource multibuffersource_buffersource = this.renderBuffers.bufferSource();
         this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, posestack, multibuffersource_buffersource, this.minecraft.level, 0);
         posestack.popPose();
         multibuffersource_buffersource.endBatch();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
      }
   }

   private void renderConfusionOverlay(GuiGraphics guigraphics, float f) {
      int i = guigraphics.guiWidth();
      int j = guigraphics.guiHeight();
      guigraphics.pose().pushPose();
      float f1 = Mth.lerp(f, 2.0F, 1.0F);
      guigraphics.pose().translate((float)i / 2.0F, (float)j / 2.0F, 0.0F);
      guigraphics.pose().scale(f1, f1, f1);
      guigraphics.pose().translate((float)(-i) / 2.0F, (float)(-j) / 2.0F, 0.0F);
      float f2 = 0.2F * f;
      float f3 = 0.4F * f;
      float f4 = 0.2F * f;
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
      guigraphics.setColor(f2, f3, f4, 1.0F);
      guigraphics.blit(NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, i, j, i, j);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      guigraphics.pose().popPose();
   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public float getDarkenWorldAmount(float f) {
      return Mth.lerp(f, this.darkenWorldAmountO, this.darkenWorldAmount);
   }

   public float getRenderDistance() {
      return this.renderDistance;
   }

   public Camera getMainCamera() {
      return this.mainCamera;
   }

   public LightTexture lightTexture() {
      return this.lightTexture;
   }

   public OverlayTexture overlayTexture() {
      return this.overlayTexture;
   }

   @Nullable
   public static ShaderInstance getPositionShader() {
      return positionShader;
   }

   @Nullable
   public static ShaderInstance getPositionColorShader() {
      return positionColorShader;
   }

   @Nullable
   public static ShaderInstance getPositionColorTexShader() {
      return positionColorTexShader;
   }

   @Nullable
   public static ShaderInstance getPositionTexShader() {
      return positionTexShader;
   }

   @Nullable
   public static ShaderInstance getPositionTexColorShader() {
      return positionTexColorShader;
   }

   @Nullable
   public static ShaderInstance getParticleShader() {
      return particleShader;
   }

   @Nullable
   public static ShaderInstance getPositionColorLightmapShader() {
      return positionColorLightmapShader;
   }

   @Nullable
   public static ShaderInstance getPositionColorTexLightmapShader() {
      return positionColorTexLightmapShader;
   }

   @Nullable
   public static ShaderInstance getPositionTexColorNormalShader() {
      return positionTexColorNormalShader;
   }

   @Nullable
   public static ShaderInstance getPositionTexLightmapColorShader() {
      return positionTexLightmapColorShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeSolidShader() {
      return rendertypeSolidShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeCutoutMippedShader() {
      return rendertypeCutoutMippedShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeCutoutShader() {
      return rendertypeCutoutShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTranslucentShader() {
      return rendertypeTranslucentShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTranslucentMovingBlockShader() {
      return rendertypeTranslucentMovingBlockShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTranslucentNoCrumblingShader() {
      return rendertypeTranslucentNoCrumblingShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeArmorCutoutNoCullShader() {
      return rendertypeArmorCutoutNoCullShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntitySolidShader() {
      return rendertypeEntitySolidShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityCutoutShader() {
      return rendertypeEntityCutoutShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityCutoutNoCullShader() {
      return rendertypeEntityCutoutNoCullShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityCutoutNoCullZOffsetShader() {
      return rendertypeEntityCutoutNoCullZOffsetShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeItemEntityTranslucentCullShader() {
      return rendertypeItemEntityTranslucentCullShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityTranslucentCullShader() {
      return rendertypeEntityTranslucentCullShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityTranslucentShader() {
      return rendertypeEntityTranslucentShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityTranslucentEmissiveShader() {
      return rendertypeEntityTranslucentEmissiveShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntitySmoothCutoutShader() {
      return rendertypeEntitySmoothCutoutShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeBeaconBeamShader() {
      return rendertypeBeaconBeamShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityDecalShader() {
      return rendertypeEntityDecalShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityNoOutlineShader() {
      return rendertypeEntityNoOutlineShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityShadowShader() {
      return rendertypeEntityShadowShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityAlphaShader() {
      return rendertypeEntityAlphaShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEyesShader() {
      return rendertypeEyesShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEnergySwirlShader() {
      return rendertypeEnergySwirlShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeLeashShader() {
      return rendertypeLeashShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeWaterMaskShader() {
      return rendertypeWaterMaskShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeOutlineShader() {
      return rendertypeOutlineShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeArmorGlintShader() {
      return rendertypeArmorGlintShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeArmorEntityGlintShader() {
      return rendertypeArmorEntityGlintShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGlintTranslucentShader() {
      return rendertypeGlintTranslucentShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGlintShader() {
      return rendertypeGlintShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGlintDirectShader() {
      return rendertypeGlintDirectShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityGlintShader() {
      return rendertypeEntityGlintShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEntityGlintDirectShader() {
      return rendertypeEntityGlintDirectShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTextShader() {
      return rendertypeTextShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTextBackgroundShader() {
      return rendertypeTextBackgroundShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTextIntensityShader() {
      return rendertypeTextIntensityShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTextSeeThroughShader() {
      return rendertypeTextSeeThroughShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTextBackgroundSeeThroughShader() {
      return rendertypeTextBackgroundSeeThroughShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTextIntensitySeeThroughShader() {
      return rendertypeTextIntensitySeeThroughShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeLightningShader() {
      return rendertypeLightningShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeTripwireShader() {
      return rendertypeTripwireShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEndPortalShader() {
      return rendertypeEndPortalShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeEndGatewayShader() {
      return rendertypeEndGatewayShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeLinesShader() {
      return rendertypeLinesShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeCrumblingShader() {
      return rendertypeCrumblingShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGuiShader() {
      return rendertypeGuiShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGuiOverlayShader() {
      return rendertypeGuiOverlayShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGuiTextHighlightShader() {
      return rendertypeGuiTextHighlightShader;
   }

   @Nullable
   public static ShaderInstance getRendertypeGuiGhostRecipeOverlayShader() {
      return rendertypeGuiGhostRecipeOverlayShader;
   }

   public static record ResourceCache(ResourceProvider original, Map<ResourceLocation, Resource> cache) implements ResourceProvider {
      public Optional<Resource> getResource(ResourceLocation resourcelocation) {
         Resource resource = this.cache.get(resourcelocation);
         return resource != null ? Optional.of(resource) : this.original.getResource(resourcelocation);
      }
   }
}
