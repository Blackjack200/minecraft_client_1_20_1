package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@DontObfuscate
public class GlStateManager {
   private static final boolean ON_LINUX = Util.getPlatform() == Util.OS.LINUX;
   public static final int TEXTURE_COUNT = 12;
   private static final GlStateManager.BlendState BLEND = new GlStateManager.BlendState();
   private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
   private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
   private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
   private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
   private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
   private static final GlStateManager.ScissorState SCISSOR = new GlStateManager.ScissorState();
   private static int activeTexture;
   private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 12).mapToObj((i) -> new GlStateManager.TextureState()).toArray((i) -> new GlStateManager.TextureState[i]);
   private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();

   public static void _disableScissorTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      SCISSOR.mode.disable();
   }

   public static void _enableScissorTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      SCISSOR.mode.enable();
   }

   public static void _scissorBox(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL20.glScissor(i, j, k, l);
   }

   public static void _disableDepthTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      DEPTH.mode.disable();
   }

   public static void _enableDepthTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      DEPTH.mode.enable();
   }

   public static void _depthFunc(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (i != DEPTH.func) {
         DEPTH.func = i;
         GL11.glDepthFunc(i);
      }

   }

   public static void _depthMask(boolean flag) {
      RenderSystem.assertOnRenderThread();
      if (flag != DEPTH.mask) {
         DEPTH.mask = flag;
         GL11.glDepthMask(flag);
      }

   }

   public static void _disableBlend() {
      RenderSystem.assertOnRenderThread();
      BLEND.mode.disable();
   }

   public static void _enableBlend() {
      RenderSystem.assertOnRenderThread();
      BLEND.mode.enable();
   }

   public static void _blendFunc(int i, int j) {
      RenderSystem.assertOnRenderThread();
      if (i != BLEND.srcRgb || j != BLEND.dstRgb) {
         BLEND.srcRgb = i;
         BLEND.dstRgb = j;
         GL11.glBlendFunc(i, j);
      }

   }

   public static void _blendFuncSeparate(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThread();
      if (i != BLEND.srcRgb || j != BLEND.dstRgb || k != BLEND.srcAlpha || l != BLEND.dstAlpha) {
         BLEND.srcRgb = i;
         BLEND.dstRgb = j;
         BLEND.srcAlpha = k;
         BLEND.dstAlpha = l;
         glBlendFuncSeparate(i, j, k, l);
      }

   }

   public static void _blendEquation(int i) {
      RenderSystem.assertOnRenderThread();
      GL14.glBlendEquation(i);
   }

   public static int glGetProgrami(int i, int j) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetProgrami(i, j);
   }

   public static void glAttachShader(int i, int j) {
      RenderSystem.assertOnRenderThread();
      GL20.glAttachShader(i, j);
   }

   public static void glDeleteShader(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glDeleteShader(i);
   }

   public static int glCreateShader(int i) {
      RenderSystem.assertOnRenderThread();
      return GL20.glCreateShader(i);
   }

   public static void glShaderSource(int i, List<String> list) {
      RenderSystem.assertOnRenderThread();
      StringBuilder stringbuilder = new StringBuilder();

      for(String s : list) {
         stringbuilder.append(s);
      }

      byte[] abyte = stringbuilder.toString().getBytes(Charsets.UTF_8);
      ByteBuffer bytebuffer = MemoryUtil.memAlloc(abyte.length + 1);
      bytebuffer.put(abyte);
      bytebuffer.put((byte)0);
      bytebuffer.flip();

      try {
         MemoryStack memorystack = MemoryStack.stackPush();

         try {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            pointerbuffer.put(bytebuffer);
            GL20C.nglShaderSource(i, 1, pointerbuffer.address0(), 0L);
         } catch (Throwable var13) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (memorystack != null) {
            memorystack.close();
         }
      } finally {
         MemoryUtil.memFree(bytebuffer);
      }

   }

   public static void glCompileShader(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glCompileShader(i);
   }

   public static int glGetShaderi(int i, int j) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetShaderi(i, j);
   }

   public static void _glUseProgram(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glUseProgram(i);
   }

   public static int glCreateProgram() {
      RenderSystem.assertOnRenderThread();
      return GL20.glCreateProgram();
   }

   public static void glDeleteProgram(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glDeleteProgram(i);
   }

   public static void glLinkProgram(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glLinkProgram(i);
   }

   public static int _glGetUniformLocation(int i, CharSequence charsequence) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetUniformLocation(i, charsequence);
   }

   public static void _glUniform1(int i, IntBuffer intbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform1iv(i, intbuffer);
   }

   public static void _glUniform1i(int i, int j) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform1i(i, j);
   }

   public static void _glUniform1(int i, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform1fv(i, floatbuffer);
   }

   public static void _glUniform2(int i, IntBuffer intbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform2iv(i, intbuffer);
   }

   public static void _glUniform2(int i, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform2fv(i, floatbuffer);
   }

   public static void _glUniform3(int i, IntBuffer intbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform3iv(i, intbuffer);
   }

   public static void _glUniform3(int i, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform3fv(i, floatbuffer);
   }

   public static void _glUniform4(int i, IntBuffer intbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform4iv(i, intbuffer);
   }

   public static void _glUniform4(int i, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform4fv(i, floatbuffer);
   }

   public static void _glUniformMatrix2(int i, boolean flag, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniformMatrix2fv(i, flag, floatbuffer);
   }

   public static void _glUniformMatrix3(int i, boolean flag, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniformMatrix3fv(i, flag, floatbuffer);
   }

   public static void _glUniformMatrix4(int i, boolean flag, FloatBuffer floatbuffer) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniformMatrix4fv(i, flag, floatbuffer);
   }

   public static int _glGetAttribLocation(int i, CharSequence charsequence) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetAttribLocation(i, charsequence);
   }

   public static void _glBindAttribLocation(int i, int j, CharSequence charsequence) {
      RenderSystem.assertOnRenderThread();
      GL20.glBindAttribLocation(i, j, charsequence);
   }

   public static int _glGenBuffers() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL15.glGenBuffers();
   }

   public static int _glGenVertexArrays() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glGenVertexArrays();
   }

   public static void _glBindBuffer(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glBindBuffer(i, j);
   }

   public static void _glBindVertexArray(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBindVertexArray(i);
   }

   public static void _glBufferData(int i, ByteBuffer bytebuffer, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glBufferData(i, bytebuffer, j);
   }

   public static void _glBufferData(int i, long j, int k) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glBufferData(i, j, k);
   }

   @Nullable
   public static ByteBuffer _glMapBuffer(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL15.glMapBuffer(i, j);
   }

   public static void _glUnmapBuffer(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glUnmapBuffer(i);
   }

   public static void _glDeleteBuffers(int i) {
      RenderSystem.assertOnRenderThread();
      if (ON_LINUX) {
         GL32C.glBindBuffer(34962, i);
         GL32C.glBufferData(34962, 0L, 35048);
         GL32C.glBindBuffer(34962, 0);
      }

      GL15.glDeleteBuffers(i);
   }

   public static void _glCopyTexSubImage2D(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL20.glCopyTexSubImage2D(i, j, k, l, i1, j1, k1, l1);
   }

   public static void _glDeleteVertexArrays(int i) {
      RenderSystem.assertOnRenderThread();
      GL30.glDeleteVertexArrays(i);
   }

   public static void _glBindFramebuffer(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBindFramebuffer(i, j);
   }

   public static void _glBlitFrameBuffer(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBlitFramebuffer(i, j, k, l, i1, j1, k1, l1, i2, j2);
   }

   public static void _glBindRenderbuffer(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBindRenderbuffer(i, j);
   }

   public static void _glDeleteRenderbuffers(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glDeleteRenderbuffers(i);
   }

   public static void _glDeleteFramebuffers(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glDeleteFramebuffers(i);
   }

   public static int glGenFramebuffers() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glGenFramebuffers();
   }

   public static int glGenRenderbuffers() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glGenRenderbuffers();
   }

   public static void _glRenderbufferStorage(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glRenderbufferStorage(i, j, k, l);
   }

   public static void _glFramebufferRenderbuffer(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glFramebufferRenderbuffer(i, j, k, l);
   }

   public static int glCheckFramebufferStatus(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glCheckFramebufferStatus(i);
   }

   public static void _glFramebufferTexture2D(int i, int j, int k, int l, int i1) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glFramebufferTexture2D(i, j, k, l, i1);
   }

   public static int getBoundFramebuffer() {
      RenderSystem.assertOnRenderThread();
      return _getInteger(36006);
   }

   public static void glActiveTexture(int i) {
      RenderSystem.assertOnRenderThread();
      GL13.glActiveTexture(i);
   }

   public static void glBlendFuncSeparate(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThread();
      GL14.glBlendFuncSeparate(i, j, k, l);
   }

   public static String glGetShaderInfoLog(int i, int j) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetShaderInfoLog(i, j);
   }

   public static String glGetProgramInfoLog(int i, int j) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetProgramInfoLog(i, j);
   }

   public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f1, Matrix4f matrix4f) {
      RenderSystem.assertOnRenderThread();
      Vector4f vector4f = matrix4f.transform(new Vector4f(vector3f, 1.0F));
      Vector4f vector4f1 = matrix4f.transform(new Vector4f(vector3f1, 1.0F));
      RenderSystem.setShaderLights(new Vector3f(vector4f.x(), vector4f.y(), vector4f.z()), new Vector3f(vector4f1.x(), vector4f1.y(), vector4f1.z()));
   }

   public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f1) {
      RenderSystem.assertOnRenderThread();
      Matrix4f matrix4f = (new Matrix4f()).scaling(1.0F, -1.0F, 1.0F).rotateY((-(float)Math.PI / 8F)).rotateX(2.3561945F);
      setupLevelDiffuseLighting(vector3f, vector3f1, matrix4f);
   }

   public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f1) {
      RenderSystem.assertOnRenderThread();
      Matrix4f matrix4f = (new Matrix4f()).rotationYXZ(1.0821041F, 3.2375858F, 0.0F).rotateYXZ((-(float)Math.PI / 8F), 2.3561945F, 0.0F);
      setupLevelDiffuseLighting(vector3f, vector3f1, matrix4f);
   }

   public static void _enableCull() {
      RenderSystem.assertOnRenderThread();
      CULL.enable.enable();
   }

   public static void _disableCull() {
      RenderSystem.assertOnRenderThread();
      CULL.enable.disable();
   }

   public static void _polygonMode(int i, int j) {
      RenderSystem.assertOnRenderThread();
      GL11.glPolygonMode(i, j);
   }

   public static void _enablePolygonOffset() {
      RenderSystem.assertOnRenderThread();
      POLY_OFFSET.fill.enable();
   }

   public static void _disablePolygonOffset() {
      RenderSystem.assertOnRenderThread();
      POLY_OFFSET.fill.disable();
   }

   public static void _polygonOffset(float f, float f1) {
      RenderSystem.assertOnRenderThread();
      if (f != POLY_OFFSET.factor || f1 != POLY_OFFSET.units) {
         POLY_OFFSET.factor = f;
         POLY_OFFSET.units = f1;
         GL11.glPolygonOffset(f, f1);
      }

   }

   public static void _enableColorLogicOp() {
      RenderSystem.assertOnRenderThread();
      COLOR_LOGIC.enable.enable();
   }

   public static void _disableColorLogicOp() {
      RenderSystem.assertOnRenderThread();
      COLOR_LOGIC.enable.disable();
   }

   public static void _logicOp(int i) {
      RenderSystem.assertOnRenderThread();
      if (i != COLOR_LOGIC.op) {
         COLOR_LOGIC.op = i;
         GL11.glLogicOp(i);
      }

   }

   public static void _activeTexture(int i) {
      RenderSystem.assertOnRenderThread();
      if (activeTexture != i - '\u84c0') {
         activeTexture = i - '\u84c0';
         glActiveTexture(i);
      }

   }

   public static void _texParameter(int i, int j, float f) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexParameterf(i, j, f);
   }

   public static void _texParameter(int i, int j, int k) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexParameteri(i, j, k);
   }

   public static int _getTexLevelParameter(int i, int j, int k) {
      RenderSystem.assertInInitPhase();
      return GL11.glGetTexLevelParameteri(i, j, k);
   }

   public static int _genTexture() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL11.glGenTextures();
   }

   public static void _genTextures(int[] aint) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glGenTextures(aint);
   }

   public static void _deleteTexture(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glDeleteTextures(i);

      for(GlStateManager.TextureState glstatemanager_texturestate : TEXTURES) {
         if (glstatemanager_texturestate.binding == i) {
            glstatemanager_texturestate.binding = -1;
         }
      }

   }

   public static void _deleteTextures(int[] aint) {
      RenderSystem.assertOnRenderThreadOrInit();

      for(GlStateManager.TextureState glstatemanager_texturestate : TEXTURES) {
         for(int i : aint) {
            if (glstatemanager_texturestate.binding == i) {
               glstatemanager_texturestate.binding = -1;
            }
         }
      }

      GL11.glDeleteTextures(aint);
   }

   public static void _bindTexture(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (i != TEXTURES[activeTexture].binding) {
         TEXTURES[activeTexture].binding = i;
         GL11.glBindTexture(3553, i);
      }

   }

   public static int _getActiveTexture() {
      return activeTexture + '\u84c0';
   }

   public static void _texImage2D(int i, int j, int k, int l, int i1, int j1, int k1, int l1, @Nullable IntBuffer intbuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexImage2D(i, j, k, l, i1, j1, k1, l1, intbuffer);
   }

   public static void _texSubImage2D(int i, int j, int k, int l, int i1, int j1, int k1, int l1, long i2) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexSubImage2D(i, j, k, l, i1, j1, k1, l1, i2);
   }

   public static void upload(int i, int j, int k, int l, int i1, NativeImage.Format nativeimage_format, IntBuffer intbuffer, Consumer<IntBuffer> consumer) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> _upload(i, j, k, l, i1, nativeimage_format, intbuffer, consumer));
      } else {
         _upload(i, j, k, l, i1, nativeimage_format, intbuffer, consumer);
      }

   }

   private static void _upload(int i, int j, int k, int l, int i1, NativeImage.Format nativeimage_format, IntBuffer intbuffer, Consumer<IntBuffer> consumer) {
      try {
         RenderSystem.assertOnRenderThreadOrInit();
         _pixelStore(3314, l);
         _pixelStore(3316, 0);
         _pixelStore(3315, 0);
         nativeimage_format.setUnpackPixelStoreState();
         GL11.glTexSubImage2D(3553, i, j, k, l, i1, nativeimage_format.glFormat(), 5121, intbuffer);
      } finally {
         consumer.accept(intbuffer);
      }

   }

   public static void _getTexImage(int i, int j, int k, int l, long i1) {
      RenderSystem.assertOnRenderThread();
      GL11.glGetTexImage(i, j, k, l, i1);
   }

   public static void _viewport(int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager.Viewport.INSTANCE.x = i;
      GlStateManager.Viewport.INSTANCE.y = j;
      GlStateManager.Viewport.INSTANCE.width = k;
      GlStateManager.Viewport.INSTANCE.height = l;
      GL11.glViewport(i, j, k, l);
   }

   public static void _colorMask(boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      RenderSystem.assertOnRenderThread();
      if (flag != COLOR_MASK.red || flag1 != COLOR_MASK.green || flag2 != COLOR_MASK.blue || flag3 != COLOR_MASK.alpha) {
         COLOR_MASK.red = flag;
         COLOR_MASK.green = flag1;
         COLOR_MASK.blue = flag2;
         COLOR_MASK.alpha = flag3;
         GL11.glColorMask(flag, flag1, flag2, flag3);
      }

   }

   public static void _stencilFunc(int i, int j, int k) {
      RenderSystem.assertOnRenderThread();
      if (i != STENCIL.func.func || i != STENCIL.func.ref || i != STENCIL.func.mask) {
         STENCIL.func.func = i;
         STENCIL.func.ref = j;
         STENCIL.func.mask = k;
         GL11.glStencilFunc(i, j, k);
      }

   }

   public static void _stencilMask(int i) {
      RenderSystem.assertOnRenderThread();
      if (i != STENCIL.mask) {
         STENCIL.mask = i;
         GL11.glStencilMask(i);
      }

   }

   public static void _stencilOp(int i, int j, int k) {
      RenderSystem.assertOnRenderThread();
      if (i != STENCIL.fail || j != STENCIL.zfail || k != STENCIL.zpass) {
         STENCIL.fail = i;
         STENCIL.zfail = j;
         STENCIL.zpass = k;
         GL11.glStencilOp(i, j, k);
      }

   }

   public static void _clearDepth(double d0) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glClearDepth(d0);
   }

   public static void _clearColor(float f, float f1, float f2, float f3) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glClearColor(f, f1, f2, f3);
   }

   public static void _clearStencil(int i) {
      RenderSystem.assertOnRenderThread();
      GL11.glClearStencil(i);
   }

   public static void _clear(int i, boolean flag) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glClear(i);
      if (flag) {
         _getError();
      }

   }

   public static void _glDrawPixels(int i, int j, int k, int l, long i1) {
      RenderSystem.assertOnRenderThread();
      GL11.glDrawPixels(i, j, k, l, i1);
   }

   public static void _vertexAttribPointer(int i, int j, int k, boolean flag, int l, long i1) {
      RenderSystem.assertOnRenderThread();
      GL20.glVertexAttribPointer(i, j, k, flag, l, i1);
   }

   public static void _vertexAttribIPointer(int i, int j, int k, int l, long i1) {
      RenderSystem.assertOnRenderThread();
      GL30.glVertexAttribIPointer(i, j, k, l, i1);
   }

   public static void _enableVertexAttribArray(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glEnableVertexAttribArray(i);
   }

   public static void _disableVertexAttribArray(int i) {
      RenderSystem.assertOnRenderThread();
      GL20.glDisableVertexAttribArray(i);
   }

   public static void _drawElements(int i, int j, int k, long l) {
      RenderSystem.assertOnRenderThread();
      GL11.glDrawElements(i, j, k, l);
   }

   public static void _pixelStore(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glPixelStorei(i, j);
   }

   public static void _readPixels(int i, int j, int k, int l, int i1, int j1, ByteBuffer bytebuffer) {
      RenderSystem.assertOnRenderThread();
      GL11.glReadPixels(i, j, k, l, i1, j1, bytebuffer);
   }

   public static void _readPixels(int i, int j, int k, int l, int i1, int j1, long k1) {
      RenderSystem.assertOnRenderThread();
      GL11.glReadPixels(i, j, k, l, i1, j1, k1);
   }

   public static int _getError() {
      RenderSystem.assertOnRenderThread();
      return GL11.glGetError();
   }

   public static String _getString(int i) {
      RenderSystem.assertOnRenderThread();
      return GL11.glGetString(i);
   }

   public static int _getInteger(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL11.glGetInteger(i);
   }

   static class BlendState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
      public int srcRgb = 1;
      public int dstRgb = 0;
      public int srcAlpha = 1;
      public int dstAlpha = 0;
   }

   static class BooleanState {
      private final int state;
      private boolean enabled;

      public BooleanState(int i) {
         this.state = i;
      }

      public void disable() {
         this.setEnabled(false);
      }

      public void enable() {
         this.setEnabled(true);
      }

      public void setEnabled(boolean flag) {
         RenderSystem.assertOnRenderThreadOrInit();
         if (flag != this.enabled) {
            this.enabled = flag;
            if (flag) {
               GL11.glEnable(this.state);
            } else {
               GL11.glDisable(this.state);
            }
         }

      }
   }

   static class ColorLogicState {
      public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
      public int op = 5379;
   }

   static class ColorMask {
      public boolean red = true;
      public boolean green = true;
      public boolean blue = true;
      public boolean alpha = true;
   }

   static class CullState {
      public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
      public int mode = 1029;
   }

   static class DepthState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
      public boolean mask = true;
      public int func = 513;
   }

   @DontObfuscate
   public static enum DestFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_COLOR(768),
      ZERO(0);

      public final int value;

      private DestFactor(int i) {
         this.value = i;
      }
   }

   public static enum LogicOp {
      AND(5377),
      AND_INVERTED(5380),
      AND_REVERSE(5378),
      CLEAR(5376),
      COPY(5379),
      COPY_INVERTED(5388),
      EQUIV(5385),
      INVERT(5386),
      NAND(5390),
      NOOP(5381),
      NOR(5384),
      OR(5383),
      OR_INVERTED(5389),
      OR_REVERSE(5387),
      SET(5391),
      XOR(5382);

      public final int value;

      private LogicOp(int i) {
         this.value = i;
      }
   }

   static class PolygonOffsetState {
      public final GlStateManager.BooleanState fill = new GlStateManager.BooleanState(32823);
      public final GlStateManager.BooleanState line = new GlStateManager.BooleanState(10754);
      public float factor;
      public float units;
   }

   static class ScissorState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3089);
   }

   @DontObfuscate
   public static enum SourceFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_ALPHA_SATURATE(776),
      SRC_COLOR(768),
      ZERO(0);

      public final int value;

      private SourceFactor(int i) {
         this.value = i;
      }
   }

   static class StencilFunc {
      public int func = 519;
      public int ref;
      public int mask = -1;
   }

   static class StencilState {
      public final GlStateManager.StencilFunc func = new GlStateManager.StencilFunc();
      public int mask = -1;
      public int fail = 7680;
      public int zfail = 7680;
      public int zpass = 7680;
   }

   static class TextureState {
      public int binding;
   }

   public static enum Viewport {
      INSTANCE;

      protected int x;
      protected int y;
      protected int width;
      protected int height;

      public static int x() {
         return INSTANCE.x;
      }

      public static int y() {
         return INSTANCE.y;
      }

      public static int width() {
         return INSTANCE.width;
      }

      public static int height() {
         return INSTANCE.height;
      }
   }
}
