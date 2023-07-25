package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Locale.Category;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public final class Window implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
   private final WindowEventHandler eventHandler;
   private final ScreenManager screenManager;
   private final long window;
   private int windowedX;
   private int windowedY;
   private int windowedWidth;
   private int windowedHeight;
   private Optional<VideoMode> preferredFullscreenVideoMode;
   private boolean fullscreen;
   private boolean actuallyFullscreen;
   private int x;
   private int y;
   private int width;
   private int height;
   private int framebufferWidth;
   private int framebufferHeight;
   private int guiScaledWidth;
   private int guiScaledHeight;
   private double guiScale;
   private String errorSection = "";
   private boolean dirty;
   private int framerateLimit;
   private boolean vsync;

   public Window(WindowEventHandler windoweventhandler, ScreenManager screenmanager, DisplayData displaydata, @Nullable String s, String s1) {
      RenderSystem.assertInInitPhase();
      this.screenManager = screenmanager;
      this.setBootErrorCallback();
      this.setErrorSection("Pre startup");
      this.eventHandler = windoweventhandler;
      Optional<VideoMode> optional = VideoMode.read(s);
      if (optional.isPresent()) {
         this.preferredFullscreenVideoMode = optional;
      } else if (displaydata.fullscreenWidth.isPresent() && displaydata.fullscreenHeight.isPresent()) {
         this.preferredFullscreenVideoMode = Optional.of(new VideoMode(displaydata.fullscreenWidth.getAsInt(), displaydata.fullscreenHeight.getAsInt(), 8, 8, 8, 60));
      } else {
         this.preferredFullscreenVideoMode = Optional.empty();
      }

      this.actuallyFullscreen = this.fullscreen = displaydata.isFullscreen;
      Monitor monitor = screenmanager.getMonitor(GLFW.glfwGetPrimaryMonitor());
      this.windowedWidth = this.width = displaydata.width > 0 ? displaydata.width : 1;
      this.windowedHeight = this.height = displaydata.height > 0 ? displaydata.height : 1;
      GLFW.glfwDefaultWindowHints();
      GLFW.glfwWindowHint(139265, 196609);
      GLFW.glfwWindowHint(139275, 221185);
      GLFW.glfwWindowHint(139266, 3);
      GLFW.glfwWindowHint(139267, 2);
      GLFW.glfwWindowHint(139272, 204801);
      GLFW.glfwWindowHint(139270, 1);
      this.window = GLFW.glfwCreateWindow(this.width, this.height, s1, this.fullscreen && monitor != null ? monitor.getMonitor() : 0L, 0L);
      if (monitor != null) {
         VideoMode videomode = monitor.getPreferredVidMode(this.fullscreen ? this.preferredFullscreenVideoMode : Optional.empty());
         this.windowedX = this.x = monitor.getX() + videomode.getWidth() / 2 - this.width / 2;
         this.windowedY = this.y = monitor.getY() + videomode.getHeight() / 2 - this.height / 2;
      } else {
         int[] aint = new int[1];
         int[] aint1 = new int[1];
         GLFW.glfwGetWindowPos(this.window, aint, aint1);
         this.windowedX = this.x = aint[0];
         this.windowedY = this.y = aint1[0];
      }

      GLFW.glfwMakeContextCurrent(this.window);
      Locale locale = Locale.getDefault(Category.FORMAT);
      Locale.setDefault(Category.FORMAT, Locale.ROOT);
      GL.createCapabilities();
      Locale.setDefault(Category.FORMAT, locale);
      this.setMode();
      this.refreshFramebufferSize();
      GLFW.glfwSetFramebufferSizeCallback(this.window, this::onFramebufferResize);
      GLFW.glfwSetWindowPosCallback(this.window, this::onMove);
      GLFW.glfwSetWindowSizeCallback(this.window, this::onResize);
      GLFW.glfwSetWindowFocusCallback(this.window, this::onFocus);
      GLFW.glfwSetCursorEnterCallback(this.window, this::onEnter);
   }

   public int getRefreshRate() {
      RenderSystem.assertOnRenderThread();
      return GLX._getRefreshRate(this);
   }

   public boolean shouldClose() {
      return GLX._shouldClose(this);
   }

   public static void checkGlfwError(BiConsumer<Integer, String> biconsumer) {
      RenderSystem.assertInInitPhase();
      MemoryStack memorystack = MemoryStack.stackPush();

      try {
         PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
         int i = GLFW.glfwGetError(pointerbuffer);
         if (i != 0) {
            long j = pointerbuffer.get();
            String s = j == 0L ? "" : MemoryUtil.memUTF8(j);
            biconsumer.accept(i, s);
         }
      } catch (Throwable var8) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (memorystack != null) {
         memorystack.close();
      }

   }

   public void setIcon(PackResources packresources, IconSet iconset) throws IOException {
      RenderSystem.assertInInitPhase();
      if (Minecraft.ON_OSX) {
         MacosUtil.loadIcon(iconset.getMacIcon(packresources));
      } else {
         List<IoSupplier<InputStream>> list = iconset.getStandardIcons(packresources);
         List<ByteBuffer> list1 = new ArrayList<>(list.size());

         try {
            MemoryStack memorystack = MemoryStack.stackPush();

            try {
               GLFWImage.Buffer glfwimage_buffer = GLFWImage.malloc(list.size(), memorystack);

               for(int i = 0; i < list.size(); ++i) {
                  NativeImage nativeimage = NativeImage.read(list.get(i).get());

                  try {
                     ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeimage.getWidth() * nativeimage.getHeight() * 4);
                     list1.add(bytebuffer);
                     bytebuffer.asIntBuffer().put(nativeimage.getPixelsRGBA());
                     glfwimage_buffer.position(i);
                     glfwimage_buffer.width(nativeimage.getWidth());
                     glfwimage_buffer.height(nativeimage.getHeight());
                     glfwimage_buffer.pixels(bytebuffer);
                  } catch (Throwable var19) {
                     if (nativeimage != null) {
                        try {
                           nativeimage.close();
                        } catch (Throwable var18) {
                           var19.addSuppressed(var18);
                        }
                     }

                     throw var19;
                  }

                  if (nativeimage != null) {
                     nativeimage.close();
                  }
               }

               GLFW.glfwSetWindowIcon(this.window, glfwimage_buffer.position(0));
            } catch (Throwable var20) {
               if (memorystack != null) {
                  try {
                     memorystack.close();
                  } catch (Throwable var17) {
                     var20.addSuppressed(var17);
                  }
               }

               throw var20;
            }

            if (memorystack != null) {
               memorystack.close();
            }
         } finally {
            list1.forEach(MemoryUtil::memFree);
         }

      }
   }

   public void setErrorSection(String s) {
      this.errorSection = s;
   }

   private void setBootErrorCallback() {
      RenderSystem.assertInInitPhase();
      GLFW.glfwSetErrorCallback(Window::bootCrash);
   }

   private static void bootCrash(int i, long j) {
      RenderSystem.assertInInitPhase();
      String s = "GLFW error " + i + ": " + MemoryUtil.memUTF8(j);
      TinyFileDialogs.tinyfd_messageBox("Minecraft", s + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).", "ok", "error", false);
      throw new Window.WindowInitFailed(s);
   }

   public void defaultErrorCallback(int i, long j) {
      RenderSystem.assertOnRenderThread();
      String s = MemoryUtil.memUTF8(j);
      LOGGER.error("########## GL ERROR ##########");
      LOGGER.error("@ {}", (Object)this.errorSection);
      LOGGER.error("{}: {}", i, s);
   }

   public void setDefaultErrorCallback() {
      GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(this.defaultErrorCallback);
      if (glfwerrorcallback != null) {
         glfwerrorcallback.free();
      }

   }

   public void updateVsync(boolean flag) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.vsync = flag;
      GLFW.glfwSwapInterval(flag ? 1 : 0);
   }

   public void close() {
      RenderSystem.assertOnRenderThread();
      Callbacks.glfwFreeCallbacks(this.window);
      this.defaultErrorCallback.close();
      GLFW.glfwDestroyWindow(this.window);
      GLFW.glfwTerminate();
   }

   private void onMove(long i, int j, int k) {
      this.x = j;
      this.y = k;
   }

   private void onFramebufferResize(long l, int i1, int j1) {
      if (l == this.window) {
         int k1 = this.getWidth();
         int l1 = this.getHeight();
         if (i1 != 0 && j1 != 0) {
            this.framebufferWidth = i1;
            this.framebufferHeight = j1;
            if (this.getWidth() != k1 || this.getHeight() != l1) {
               this.eventHandler.resizeDisplay();
            }

         }
      }
   }

   private void refreshFramebufferSize() {
      RenderSystem.assertInInitPhase();
      int[] aint = new int[1];
      int[] aint1 = new int[1];
      GLFW.glfwGetFramebufferSize(this.window, aint, aint1);
      this.framebufferWidth = aint[0] > 0 ? aint[0] : 1;
      this.framebufferHeight = aint1[0] > 0 ? aint1[0] : 1;
   }

   private void onResize(long i2, int j2, int k2) {
      this.width = j2;
      this.height = k2;
   }

   private void onFocus(long l2, boolean flag) {
      if (l2 == this.window) {
         this.eventHandler.setWindowActive(flag);
      }

   }

   private void onEnter(long i3, boolean flag1) {
      if (flag1) {
         this.eventHandler.cursorEntered();
      }

   }

   public void setFramerateLimit(int i) {
      this.framerateLimit = i;
   }

   public int getFramerateLimit() {
      return this.framerateLimit;
   }

   public void updateDisplay() {
      RenderSystem.flipFrame(this.window);
      if (this.fullscreen != this.actuallyFullscreen) {
         this.actuallyFullscreen = this.fullscreen;
         this.updateFullscreen(this.vsync);
      }

   }

   public Optional<VideoMode> getPreferredFullscreenVideoMode() {
      return this.preferredFullscreenVideoMode;
   }

   public void setPreferredFullscreenVideoMode(Optional<VideoMode> optional) {
      boolean flag = !optional.equals(this.preferredFullscreenVideoMode);
      this.preferredFullscreenVideoMode = optional;
      if (flag) {
         this.dirty = true;
      }

   }

   public void changeFullscreenVideoMode() {
      if (this.fullscreen && this.dirty) {
         this.dirty = false;
         this.setMode();
         this.eventHandler.resizeDisplay();
      }

   }

   private void setMode() {
      RenderSystem.assertInInitPhase();
      boolean flag = GLFW.glfwGetWindowMonitor(this.window) != 0L;
      if (this.fullscreen) {
         Monitor monitor = this.screenManager.findBestMonitor(this);
         if (monitor == null) {
            LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
            this.fullscreen = false;
         } else {
            if (Minecraft.ON_OSX) {
               MacosUtil.toggleFullscreen(this.window);
            }

            VideoMode videomode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);
            if (!flag) {
               this.windowedX = this.x;
               this.windowedY = this.y;
               this.windowedWidth = this.width;
               this.windowedHeight = this.height;
            }

            this.x = 0;
            this.y = 0;
            this.width = videomode.getWidth();
            this.height = videomode.getHeight();
            GLFW.glfwSetWindowMonitor(this.window, monitor.getMonitor(), this.x, this.y, this.width, this.height, videomode.getRefreshRate());
         }
      } else {
         this.x = this.windowedX;
         this.y = this.windowedY;
         this.width = this.windowedWidth;
         this.height = this.windowedHeight;
         GLFW.glfwSetWindowMonitor(this.window, 0L, this.x, this.y, this.width, this.height, -1);
      }

   }

   public void toggleFullScreen() {
      this.fullscreen = !this.fullscreen;
   }

   public void setWindowed(int i, int j) {
      this.windowedWidth = i;
      this.windowedHeight = j;
      this.fullscreen = false;
      this.setMode();
   }

   private void updateFullscreen(boolean flag) {
      RenderSystem.assertOnRenderThread();

      try {
         this.setMode();
         this.eventHandler.resizeDisplay();
         this.updateVsync(flag);
         this.updateDisplay();
      } catch (Exception var3) {
         LOGGER.error("Couldn't toggle fullscreen", (Throwable)var3);
      }

   }

   public int calculateScale(int i, boolean flag) {
      int j;
      for(j = 1; j != i && j < this.framebufferWidth && j < this.framebufferHeight && this.framebufferWidth / (j + 1) >= 320 && this.framebufferHeight / (j + 1) >= 240; ++j) {
      }

      if (flag && j % 2 != 0) {
         ++j;
      }

      return j;
   }

   public void setGuiScale(double d0) {
      this.guiScale = d0;
      int i = (int)((double)this.framebufferWidth / d0);
      this.guiScaledWidth = (double)this.framebufferWidth / d0 > (double)i ? i + 1 : i;
      int j = (int)((double)this.framebufferHeight / d0);
      this.guiScaledHeight = (double)this.framebufferHeight / d0 > (double)j ? j + 1 : j;
   }

   public void setTitle(String s) {
      GLFW.glfwSetWindowTitle(this.window, s);
   }

   public long getWindow() {
      return this.window;
   }

   public boolean isFullscreen() {
      return this.fullscreen;
   }

   public int getWidth() {
      return this.framebufferWidth;
   }

   public int getHeight() {
      return this.framebufferHeight;
   }

   public void setWidth(int i) {
      this.framebufferWidth = i;
   }

   public void setHeight(int i) {
      this.framebufferHeight = i;
   }

   public int getScreenWidth() {
      return this.width;
   }

   public int getScreenHeight() {
      return this.height;
   }

   public int getGuiScaledWidth() {
      return this.guiScaledWidth;
   }

   public int getGuiScaledHeight() {
      return this.guiScaledHeight;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public double getGuiScale() {
      return this.guiScale;
   }

   @Nullable
   public Monitor findBestMonitor() {
      return this.screenManager.findBestMonitor(this);
   }

   public void updateRawMouseInput(boolean flag) {
      InputConstants.updateRawMouseInput(this.window, flag);
   }

   public static class WindowInitFailed extends SilentInitException {
      WindowInitFailed(String s) {
         super(s);
      }
   }
}
