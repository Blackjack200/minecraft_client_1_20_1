package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.slf4j.Logger;

@DontObfuscate
public class RenderSystem {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
   private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
   private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
   private static boolean isReplayingQueue;
   @Nullable
   private static Thread gameThread;
   @Nullable
   private static Thread renderThread;
   private static int MAX_SUPPORTED_TEXTURE_SIZE = -1;
   private static boolean isInInit;
   private static double lastDrawTime = Double.MIN_VALUE;
   private static final RenderSystem.AutoStorageIndexBuffer sharedSequential = new RenderSystem.AutoStorageIndexBuffer(1, 1, IntConsumer::accept);
   private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialQuad = new RenderSystem.AutoStorageIndexBuffer(4, 6, (intconsumer, i) -> {
      intconsumer.accept(i + 0);
      intconsumer.accept(i + 1);
      intconsumer.accept(i + 2);
      intconsumer.accept(i + 2);
      intconsumer.accept(i + 3);
      intconsumer.accept(i + 0);
   });
   private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialLines = new RenderSystem.AutoStorageIndexBuffer(4, 6, (intconsumer, i) -> {
      intconsumer.accept(i + 0);
      intconsumer.accept(i + 1);
      intconsumer.accept(i + 2);
      intconsumer.accept(i + 3);
      intconsumer.accept(i + 2);
      intconsumer.accept(i + 1);
   });
   private static Matrix3f inverseViewRotationMatrix = (new Matrix3f()).zero();
   private static Matrix4f projectionMatrix = new Matrix4f();
   private static Matrix4f savedProjectionMatrix = new Matrix4f();
   private static VertexSorting vertexSorting = VertexSorting.DISTANCE_TO_ORIGIN;
   private static VertexSorting savedVertexSorting = VertexSorting.DISTANCE_TO_ORIGIN;
   private static final PoseStack modelViewStack = new PoseStack();
   private static Matrix4f modelViewMatrix = new Matrix4f();
   private static Matrix4f textureMatrix = new Matrix4f();
   private static final int[] shaderTextures = new int[12];
   private static final float[] shaderColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
   private static float shaderGlintAlpha = 1.0F;
   private static float shaderFogStart;
   private static float shaderFogEnd = 1.0F;
   private static final float[] shaderFogColor = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   private static FogShape shaderFogShape = FogShape.SPHERE;
   private static final Vector3f[] shaderLightDirections = new Vector3f[2];
   private static float shaderGameTime;
   private static float shaderLineWidth = 1.0F;
   private static String apiDescription = "Unknown";
   @Nullable
   private static ShaderInstance shader;
   private static final AtomicLong pollEventsWaitStart = new AtomicLong();
   private static final AtomicBoolean pollingEvents = new AtomicBoolean(false);

   public static void initRenderThread() {
      if (renderThread == null && gameThread != Thread.currentThread()) {
         renderThread = Thread.currentThread();
      } else {
         throw new IllegalStateException("Could not initialize render thread");
      }
   }

   public static boolean isOnRenderThread() {
      return Thread.currentThread() == renderThread;
   }

   public static boolean isOnRenderThreadOrInit() {
      return isInInit || isOnRenderThread();
   }

   public static void initGameThread(boolean flag) {
      boolean flag1 = renderThread == Thread.currentThread();
      if (gameThread == null && renderThread != null && flag1 != flag) {
         gameThread = Thread.currentThread();
      } else {
         throw new IllegalStateException("Could not initialize tick thread");
      }
   }

   public static boolean isOnGameThread() {
      return true;
   }

   public static void assertInInitPhase() {
      if (!isInInitPhase()) {
         throw constructThreadException();
      }
   }

   public static void assertOnGameThreadOrInit() {
      if (!isInInit && !isOnGameThread()) {
         throw constructThreadException();
      }
   }

   public static void assertOnRenderThreadOrInit() {
      if (!isInInit && !isOnRenderThread()) {
         throw constructThreadException();
      }
   }

   public static void assertOnRenderThread() {
      if (!isOnRenderThread()) {
         throw constructThreadException();
      }
   }

   public static void assertOnGameThread() {
      if (!isOnGameThread()) {
         throw constructThreadException();
      }
   }

   private static IllegalStateException constructThreadException() {
      return new IllegalStateException("Rendersystem called from wrong thread");
   }

   public static boolean isInInitPhase() {
      return true;
   }

   public static void recordRenderCall(RenderCall rendercall) {
      recordingQueue.add(rendercall);
   }

   private static void pollEvents() {
      pollEventsWaitStart.set(Util.getMillis());
      pollingEvents.set(true);
      GLFW.glfwPollEvents();
      pollingEvents.set(false);
   }

   public static boolean isFrozenAtPollEvents() {
      return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
   }

   public static void flipFrame(long i) {
      pollEvents();
      replayQueue();
      Tesselator.getInstance().getBuilder().clear();
      GLFW.glfwSwapBuffers(i);
      pollEvents();
   }

   public static void replayQueue() {
      isReplayingQueue = true;

      while(!recordingQueue.isEmpty()) {
         RenderCall rendercall = recordingQueue.poll();
         rendercall.execute();
      }

      isReplayingQueue = false;
   }

   public static void limitDisplayFPS(int i) {
      double d0 = lastDrawTime + 1.0D / (double)i;

      double d1;
      for(d1 = GLFW.glfwGetTime(); d1 < d0; d1 = GLFW.glfwGetTime()) {
         GLFW.glfwWaitEventsTimeout(d0 - d1);
      }

      lastDrawTime = d1;
   }

   public static void disableDepthTest() {
      assertOnRenderThread();
      GlStateManager._disableDepthTest();
   }

   public static void enableDepthTest() {
      assertOnGameThreadOrInit();
      GlStateManager._enableDepthTest();
   }

   public static void enableScissor(int i, int j, int k, int l) {
      assertOnGameThreadOrInit();
      GlStateManager._enableScissorTest();
      GlStateManager._scissorBox(i, j, k, l);
   }

   public static void disableScissor() {
      assertOnGameThreadOrInit();
      GlStateManager._disableScissorTest();
   }

   public static void depthFunc(int i) {
      assertOnRenderThread();
      GlStateManager._depthFunc(i);
   }

   public static void depthMask(boolean flag) {
      assertOnRenderThread();
      GlStateManager._depthMask(flag);
   }

   public static void enableBlend() {
      assertOnRenderThread();
      GlStateManager._enableBlend();
   }

   public static void disableBlend() {
      assertOnRenderThread();
      GlStateManager._disableBlend();
   }

   public static void blendFunc(GlStateManager.SourceFactor glstatemanager_sourcefactor, GlStateManager.DestFactor glstatemanager_destfactor) {
      assertOnRenderThread();
      GlStateManager._blendFunc(glstatemanager_sourcefactor.value, glstatemanager_destfactor.value);
   }

   public static void blendFunc(int i, int j) {
      assertOnRenderThread();
      GlStateManager._blendFunc(i, j);
   }

   public static void blendFuncSeparate(GlStateManager.SourceFactor glstatemanager_sourcefactor, GlStateManager.DestFactor glstatemanager_destfactor, GlStateManager.SourceFactor glstatemanager_sourcefactor1, GlStateManager.DestFactor glstatemanager_destfactor1) {
      assertOnRenderThread();
      GlStateManager._blendFuncSeparate(glstatemanager_sourcefactor.value, glstatemanager_destfactor.value, glstatemanager_sourcefactor1.value, glstatemanager_destfactor1.value);
   }

   public static void blendFuncSeparate(int i, int j, int k, int l) {
      assertOnRenderThread();
      GlStateManager._blendFuncSeparate(i, j, k, l);
   }

   public static void blendEquation(int i) {
      assertOnRenderThread();
      GlStateManager._blendEquation(i);
   }

   public static void enableCull() {
      assertOnRenderThread();
      GlStateManager._enableCull();
   }

   public static void disableCull() {
      assertOnRenderThread();
      GlStateManager._disableCull();
   }

   public static void polygonMode(int i, int j) {
      assertOnRenderThread();
      GlStateManager._polygonMode(i, j);
   }

   public static void enablePolygonOffset() {
      assertOnRenderThread();
      GlStateManager._enablePolygonOffset();
   }

   public static void disablePolygonOffset() {
      assertOnRenderThread();
      GlStateManager._disablePolygonOffset();
   }

   public static void polygonOffset(float f, float f1) {
      assertOnRenderThread();
      GlStateManager._polygonOffset(f, f1);
   }

   public static void enableColorLogicOp() {
      assertOnRenderThread();
      GlStateManager._enableColorLogicOp();
   }

   public static void disableColorLogicOp() {
      assertOnRenderThread();
      GlStateManager._disableColorLogicOp();
   }

   public static void logicOp(GlStateManager.LogicOp glstatemanager_logicop) {
      assertOnRenderThread();
      GlStateManager._logicOp(glstatemanager_logicop.value);
   }

   public static void activeTexture(int i) {
      assertOnRenderThread();
      GlStateManager._activeTexture(i);
   }

   public static void texParameter(int i, int j, int k) {
      GlStateManager._texParameter(i, j, k);
   }

   public static void deleteTexture(int i) {
      assertOnGameThreadOrInit();
      GlStateManager._deleteTexture(i);
   }

   public static void bindTextureForSetup(int i) {
      bindTexture(i);
   }

   public static void bindTexture(int i) {
      GlStateManager._bindTexture(i);
   }

   public static void viewport(int i, int j, int k, int l) {
      assertOnGameThreadOrInit();
      GlStateManager._viewport(i, j, k, l);
   }

   public static void colorMask(boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      assertOnRenderThread();
      GlStateManager._colorMask(flag, flag1, flag2, flag3);
   }

   public static void stencilFunc(int i, int j, int k) {
      assertOnRenderThread();
      GlStateManager._stencilFunc(i, j, k);
   }

   public static void stencilMask(int i) {
      assertOnRenderThread();
      GlStateManager._stencilMask(i);
   }

   public static void stencilOp(int i, int j, int k) {
      assertOnRenderThread();
      GlStateManager._stencilOp(i, j, k);
   }

   public static void clearDepth(double d0) {
      assertOnGameThreadOrInit();
      GlStateManager._clearDepth(d0);
   }

   public static void clearColor(float f, float f1, float f2, float f3) {
      assertOnGameThreadOrInit();
      GlStateManager._clearColor(f, f1, f2, f3);
   }

   public static void clearStencil(int i) {
      assertOnRenderThread();
      GlStateManager._clearStencil(i);
   }

   public static void clear(int i, boolean flag) {
      assertOnGameThreadOrInit();
      GlStateManager._clear(i, flag);
   }

   public static void setShaderFogStart(float f) {
      assertOnRenderThread();
      _setShaderFogStart(f);
   }

   private static void _setShaderFogStart(float f) {
      shaderFogStart = f;
   }

   public static float getShaderFogStart() {
      assertOnRenderThread();
      return shaderFogStart;
   }

   public static void setShaderGlintAlpha(double d0) {
      setShaderGlintAlpha((float)d0);
   }

   public static void setShaderGlintAlpha(float f) {
      assertOnRenderThread();
      _setShaderGlintAlpha(f);
   }

   private static void _setShaderGlintAlpha(float f) {
      shaderGlintAlpha = f;
   }

   public static float getShaderGlintAlpha() {
      assertOnRenderThread();
      return shaderGlintAlpha;
   }

   public static void setShaderFogEnd(float f) {
      assertOnRenderThread();
      _setShaderFogEnd(f);
   }

   private static void _setShaderFogEnd(float f) {
      shaderFogEnd = f;
   }

   public static float getShaderFogEnd() {
      assertOnRenderThread();
      return shaderFogEnd;
   }

   public static void setShaderFogColor(float f, float f1, float f2, float f3) {
      assertOnRenderThread();
      _setShaderFogColor(f, f1, f2, f3);
   }

   public static void setShaderFogColor(float f, float f1, float f2) {
      setShaderFogColor(f, f1, f2, 1.0F);
   }

   private static void _setShaderFogColor(float f, float f1, float f2, float f3) {
      shaderFogColor[0] = f;
      shaderFogColor[1] = f1;
      shaderFogColor[2] = f2;
      shaderFogColor[3] = f3;
   }

   public static float[] getShaderFogColor() {
      assertOnRenderThread();
      return shaderFogColor;
   }

   public static void setShaderFogShape(FogShape fogshape) {
      assertOnRenderThread();
      _setShaderFogShape(fogshape);
   }

   private static void _setShaderFogShape(FogShape fogshape) {
      shaderFogShape = fogshape;
   }

   public static FogShape getShaderFogShape() {
      assertOnRenderThread();
      return shaderFogShape;
   }

   public static void setShaderLights(Vector3f vector3f, Vector3f vector3f1) {
      assertOnRenderThread();
      _setShaderLights(vector3f, vector3f1);
   }

   public static void _setShaderLights(Vector3f vector3f, Vector3f vector3f1) {
      shaderLightDirections[0] = vector3f;
      shaderLightDirections[1] = vector3f1;
   }

   public static void setupShaderLights(ShaderInstance shaderinstance) {
      assertOnRenderThread();
      if (shaderinstance.LIGHT0_DIRECTION != null) {
         shaderinstance.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
      }

      if (shaderinstance.LIGHT1_DIRECTION != null) {
         shaderinstance.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
      }

   }

   public static void setShaderColor(float f, float f1, float f2, float f3) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> _setShaderColor(f, f1, f2, f3));
      } else {
         _setShaderColor(f, f1, f2, f3);
      }

   }

   private static void _setShaderColor(float f, float f1, float f2, float f3) {
      shaderColor[0] = f;
      shaderColor[1] = f1;
      shaderColor[2] = f2;
      shaderColor[3] = f3;
   }

   public static float[] getShaderColor() {
      assertOnRenderThread();
      return shaderColor;
   }

   public static void drawElements(int i, int j, int k) {
      assertOnRenderThread();
      GlStateManager._drawElements(i, j, k, 0L);
   }

   public static void lineWidth(float f) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> shaderLineWidth = f);
      } else {
         shaderLineWidth = f;
      }

   }

   public static float getShaderLineWidth() {
      assertOnRenderThread();
      return shaderLineWidth;
   }

   public static void pixelStore(int i, int j) {
      assertOnGameThreadOrInit();
      GlStateManager._pixelStore(i, j);
   }

   public static void readPixels(int i, int j, int k, int l, int i1, int j1, ByteBuffer bytebuffer) {
      assertOnRenderThread();
      GlStateManager._readPixels(i, j, k, l, i1, j1, bytebuffer);
   }

   public static void getString(int i, Consumer<String> consumer) {
      assertOnRenderThread();
      consumer.accept(GlStateManager._getString(i));
   }

   public static String getBackendDescription() {
      assertInInitPhase();
      return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
   }

   public static String getApiDescription() {
      return apiDescription;
   }

   public static TimeSource.NanoTimeSource initBackendSystem() {
      assertInInitPhase();
      return GLX._initGlfw()::getAsLong;
   }

   public static void initRenderer(int i, boolean flag) {
      assertInInitPhase();
      GLX._init(i, flag);
      apiDescription = GLX.getOpenGLVersionString();
   }

   public static void setErrorCallback(GLFWErrorCallbackI glfwerrorcallbacki) {
      assertInInitPhase();
      GLX._setGlfwErrorCallback(glfwerrorcallbacki);
   }

   public static void renderCrosshair(int i) {
      assertOnRenderThread();
      GLX._renderCrosshair(i, true, true, true);
   }

   public static String getCapsString() {
      assertOnRenderThread();
      return "Using framebuffer using OpenGL 3.2";
   }

   public static void setupDefaultState(int i, int j, int k, int l) {
      assertInInitPhase();
      GlStateManager._clearDepth(1.0D);
      GlStateManager._enableDepthTest();
      GlStateManager._depthFunc(515);
      projectionMatrix.identity();
      savedProjectionMatrix.identity();
      modelViewMatrix.identity();
      textureMatrix.identity();
      GlStateManager._viewport(i, j, k, l);
   }

   public static int maxSupportedTextureSize() {
      if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
         assertOnRenderThreadOrInit();
         int i = GlStateManager._getInteger(3379);

         for(int j = Math.max(32768, i); j >= 1024; j >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, (IntBuffer)null);
            int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (k != 0) {
               MAX_SUPPORTED_TEXTURE_SIZE = j;
               return j;
            }
         }

         MAX_SUPPORTED_TEXTURE_SIZE = Math.max(i, 1024);
         LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (int)MAX_SUPPORTED_TEXTURE_SIZE);
      }

      return MAX_SUPPORTED_TEXTURE_SIZE;
   }

   public static void glBindBuffer(int i, IntSupplier intsupplier) {
      GlStateManager._glBindBuffer(i, intsupplier.getAsInt());
   }

   public static void glBindVertexArray(Supplier<Integer> supplier) {
      GlStateManager._glBindVertexArray(supplier.get());
   }

   public static void glBufferData(int i, ByteBuffer bytebuffer, int j) {
      assertOnRenderThreadOrInit();
      GlStateManager._glBufferData(i, bytebuffer, j);
   }

   public static void glDeleteBuffers(int i) {
      assertOnRenderThread();
      GlStateManager._glDeleteBuffers(i);
   }

   public static void glDeleteVertexArrays(int i) {
      assertOnRenderThread();
      GlStateManager._glDeleteVertexArrays(i);
   }

   public static void glUniform1i(int i, int j) {
      assertOnRenderThread();
      GlStateManager._glUniform1i(i, j);
   }

   public static void glUniform1(int i, IntBuffer intbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform1(i, intbuffer);
   }

   public static void glUniform2(int i, IntBuffer intbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform2(i, intbuffer);
   }

   public static void glUniform3(int i, IntBuffer intbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform3(i, intbuffer);
   }

   public static void glUniform4(int i, IntBuffer intbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform4(i, intbuffer);
   }

   public static void glUniform1(int i, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform1(i, floatbuffer);
   }

   public static void glUniform2(int i, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform2(i, floatbuffer);
   }

   public static void glUniform3(int i, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform3(i, floatbuffer);
   }

   public static void glUniform4(int i, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniform4(i, floatbuffer);
   }

   public static void glUniformMatrix2(int i, boolean flag, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniformMatrix2(i, flag, floatbuffer);
   }

   public static void glUniformMatrix3(int i, boolean flag, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniformMatrix3(i, flag, floatbuffer);
   }

   public static void glUniformMatrix4(int i, boolean flag, FloatBuffer floatbuffer) {
      assertOnRenderThread();
      GlStateManager._glUniformMatrix4(i, flag, floatbuffer);
   }

   public static void setupOverlayColor(IntSupplier intsupplier, int i) {
      assertOnRenderThread();
      int j = intsupplier.getAsInt();
      setShaderTexture(1, j);
   }

   public static void teardownOverlayColor() {
      assertOnRenderThread();
      setShaderTexture(1, 0);
   }

   public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f1, Matrix4f matrix4f) {
      assertOnRenderThread();
      GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f1, matrix4f);
   }

   public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f1) {
      assertOnRenderThread();
      GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f1);
   }

   public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f1) {
      assertOnRenderThread();
      GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f1);
   }

   public static void beginInitialization() {
      isInInit = true;
   }

   public static void finishInitialization() {
      isInInit = false;
      if (!recordingQueue.isEmpty()) {
         replayQueue();
      }

      if (!recordingQueue.isEmpty()) {
         throw new IllegalStateException("Recorded to render queue during initialization");
      }
   }

   public static void glGenBuffers(Consumer<Integer> consumer) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> consumer.accept(GlStateManager._glGenBuffers()));
      } else {
         consumer.accept(GlStateManager._glGenBuffers());
      }

   }

   public static void glGenVertexArrays(Consumer<Integer> consumer) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> consumer.accept(GlStateManager._glGenVertexArrays()));
      } else {
         consumer.accept(GlStateManager._glGenVertexArrays());
      }

   }

   public static Tesselator renderThreadTesselator() {
      assertOnRenderThread();
      return RENDER_THREAD_TESSELATOR;
   }

   public static void defaultBlendFunc() {
      blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
   }

   /** @deprecated */
   @Deprecated
   public static void runAsFancy(Runnable runnable) {
      boolean flag = Minecraft.useShaderTransparency();
      if (!flag) {
         runnable.run();
      } else {
         OptionInstance<GraphicsStatus> optioninstance = Minecraft.getInstance().options.graphicsMode();
         GraphicsStatus graphicsstatus = optioninstance.get();
         optioninstance.set(GraphicsStatus.FANCY);
         runnable.run();
         optioninstance.set(graphicsstatus);
      }
   }

   public static void setShader(Supplier<ShaderInstance> supplier) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> shader = supplier.get());
      } else {
         shader = supplier.get();
      }

   }

   @Nullable
   public static ShaderInstance getShader() {
      assertOnRenderThread();
      return shader;
   }

   public static void setShaderTexture(int i, ResourceLocation resourcelocation) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> _setShaderTexture(i, resourcelocation));
      } else {
         _setShaderTexture(i, resourcelocation);
      }

   }

   public static void _setShaderTexture(int i, ResourceLocation resourcelocation) {
      if (i >= 0 && i < shaderTextures.length) {
         TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
         AbstractTexture abstracttexture = texturemanager.getTexture(resourcelocation);
         shaderTextures[i] = abstracttexture.getId();
      }

   }

   public static void setShaderTexture(int i, int j) {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> _setShaderTexture(i, j));
      } else {
         _setShaderTexture(i, j);
      }

   }

   public static void _setShaderTexture(int i, int j) {
      if (i >= 0 && i < shaderTextures.length) {
         shaderTextures[i] = j;
      }

   }

   public static int getShaderTexture(int i) {
      assertOnRenderThread();
      return i >= 0 && i < shaderTextures.length ? shaderTextures[i] : 0;
   }

   public static void setProjectionMatrix(Matrix4f matrix4f, VertexSorting vertexsorting) {
      Matrix4f matrix4f1 = new Matrix4f(matrix4f);
      if (!isOnRenderThread()) {
         recordRenderCall(() -> {
            projectionMatrix = matrix4f1;
            vertexSorting = vertexsorting;
         });
      } else {
         projectionMatrix = matrix4f1;
         vertexSorting = vertexsorting;
      }

   }

   public static void setInverseViewRotationMatrix(Matrix3f matrix3f) {
      Matrix3f matrix3f1 = new Matrix3f(matrix3f);
      if (!isOnRenderThread()) {
         recordRenderCall(() -> inverseViewRotationMatrix = matrix3f1);
      } else {
         inverseViewRotationMatrix = matrix3f1;
      }

   }

   public static void setTextureMatrix(Matrix4f matrix4f) {
      Matrix4f matrix4f1 = new Matrix4f(matrix4f);
      if (!isOnRenderThread()) {
         recordRenderCall(() -> textureMatrix = matrix4f1);
      } else {
         textureMatrix = matrix4f1;
      }

   }

   public static void resetTextureMatrix() {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> textureMatrix.identity());
      } else {
         textureMatrix.identity();
      }

   }

   public static void applyModelViewMatrix() {
      Matrix4f matrix4f = new Matrix4f(modelViewStack.last().pose());
      if (!isOnRenderThread()) {
         recordRenderCall(() -> modelViewMatrix = matrix4f);
      } else {
         modelViewMatrix = matrix4f;
      }

   }

   public static void backupProjectionMatrix() {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> _backupProjectionMatrix());
      } else {
         _backupProjectionMatrix();
      }

   }

   private static void _backupProjectionMatrix() {
      savedProjectionMatrix = projectionMatrix;
      savedVertexSorting = vertexSorting;
   }

   public static void restoreProjectionMatrix() {
      if (!isOnRenderThread()) {
         recordRenderCall(() -> _restoreProjectionMatrix());
      } else {
         _restoreProjectionMatrix();
      }

   }

   private static void _restoreProjectionMatrix() {
      projectionMatrix = savedProjectionMatrix;
      vertexSorting = savedVertexSorting;
   }

   public static Matrix4f getProjectionMatrix() {
      assertOnRenderThread();
      return projectionMatrix;
   }

   public static Matrix3f getInverseViewRotationMatrix() {
      assertOnRenderThread();
      return inverseViewRotationMatrix;
   }

   public static Matrix4f getModelViewMatrix() {
      assertOnRenderThread();
      return modelViewMatrix;
   }

   public static PoseStack getModelViewStack() {
      return modelViewStack;
   }

   public static Matrix4f getTextureMatrix() {
      assertOnRenderThread();
      return textureMatrix;
   }

   public static RenderSystem.AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode vertexformat_mode) {
      assertOnRenderThread();
      RenderSystem.AutoStorageIndexBuffer var10000;
      switch (vertexformat_mode) {
         case QUADS:
            var10000 = sharedSequentialQuad;
            break;
         case LINES:
            var10000 = sharedSequentialLines;
            break;
         default:
            var10000 = sharedSequential;
      }

      return var10000;
   }

   public static void setShaderGameTime(long i, float f) {
      float f1 = ((float)(i % 24000L) + f) / 24000.0F;
      if (!isOnRenderThread()) {
         recordRenderCall(() -> shaderGameTime = f1);
      } else {
         shaderGameTime = f1;
      }

   }

   public static float getShaderGameTime() {
      assertOnRenderThread();
      return shaderGameTime;
   }

   public static VertexSorting getVertexSorting() {
      assertOnRenderThread();
      return vertexSorting;
   }

   public static final class AutoStorageIndexBuffer {
      private final int vertexStride;
      private final int indexStride;
      private final RenderSystem.AutoStorageIndexBuffer.IndexGenerator generator;
      private int name;
      private VertexFormat.IndexType type = VertexFormat.IndexType.SHORT;
      private int indexCount;

      AutoStorageIndexBuffer(int i, int j, RenderSystem.AutoStorageIndexBuffer.IndexGenerator rendersystem_autostorageindexbuffer_indexgenerator) {
         this.vertexStride = i;
         this.indexStride = j;
         this.generator = rendersystem_autostorageindexbuffer_indexgenerator;
      }

      public boolean hasStorage(int i) {
         return i <= this.indexCount;
      }

      public void bind(int i) {
         if (this.name == 0) {
            this.name = GlStateManager._glGenBuffers();
         }

         GlStateManager._glBindBuffer(34963, this.name);
         this.ensureStorage(i);
      }

      private void ensureStorage(int i) {
         if (!this.hasStorage(i)) {
            i = Mth.roundToward(i * 2, this.indexStride);
            RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, i);
            VertexFormat.IndexType vertexformat_indextype = VertexFormat.IndexType.least(i);
            int j = Mth.roundToward(i * vertexformat_indextype.bytes, 4);
            GlStateManager._glBufferData(34963, (long)j, 35048);
            ByteBuffer bytebuffer = GlStateManager._glMapBuffer(34963, 35001);
            if (bytebuffer == null) {
               throw new RuntimeException("Failed to map GL buffer");
            } else {
               this.type = vertexformat_indextype;
               it.unimi.dsi.fastutil.ints.IntConsumer intconsumer = this.intConsumer(bytebuffer);

               for(int k = 0; k < i; k += this.indexStride) {
                  this.generator.accept(intconsumer, k * this.vertexStride / this.indexStride);
               }

               GlStateManager._glUnmapBuffer(34963);
               this.indexCount = i;
            }
         }
      }

      private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer bytebuffer) {
         switch (this.type) {
            case SHORT:
               return (i) -> bytebuffer.putShort((short)i);
            case INT:
            default:
               return bytebuffer::putInt;
         }
      }

      public VertexFormat.IndexType type() {
         return this.type;
      }

      interface IndexGenerator {
         void accept(it.unimi.dsi.fastutil.ints.IntConsumer intconsumer, int i);
      }
   }
}
