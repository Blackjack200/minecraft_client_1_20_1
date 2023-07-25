package com.mojang.blaze3d.platform;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.lwjgl.glfw.GLFWVidMode;

public final class VideoMode {
   private final int width;
   private final int height;
   private final int redBits;
   private final int greenBits;
   private final int blueBits;
   private final int refreshRate;
   private static final Pattern PATTERN = Pattern.compile("(\\d+)x(\\d+)(?:@(\\d+)(?::(\\d+))?)?");

   public VideoMode(int i, int j, int k, int l, int i1, int j1) {
      this.width = i;
      this.height = j;
      this.redBits = k;
      this.greenBits = l;
      this.blueBits = i1;
      this.refreshRate = j1;
   }

   public VideoMode(GLFWVidMode.Buffer glfwvidmode_buffer) {
      this.width = glfwvidmode_buffer.width();
      this.height = glfwvidmode_buffer.height();
      this.redBits = glfwvidmode_buffer.redBits();
      this.greenBits = glfwvidmode_buffer.greenBits();
      this.blueBits = glfwvidmode_buffer.blueBits();
      this.refreshRate = glfwvidmode_buffer.refreshRate();
   }

   public VideoMode(GLFWVidMode glfwvidmode) {
      this.width = glfwvidmode.width();
      this.height = glfwvidmode.height();
      this.redBits = glfwvidmode.redBits();
      this.greenBits = glfwvidmode.greenBits();
      this.blueBits = glfwvidmode.blueBits();
      this.refreshRate = glfwvidmode.refreshRate();
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getRedBits() {
      return this.redBits;
   }

   public int getGreenBits() {
      return this.greenBits;
   }

   public int getBlueBits() {
      return this.blueBits;
   }

   public int getRefreshRate() {
      return this.refreshRate;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         VideoMode videomode = (VideoMode)object;
         return this.width == videomode.width && this.height == videomode.height && this.redBits == videomode.redBits && this.greenBits == videomode.greenBits && this.blueBits == videomode.blueBits && this.refreshRate == videomode.refreshRate;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.width, this.height, this.redBits, this.greenBits, this.blueBits, this.refreshRate);
   }

   public String toString() {
      return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
   }

   public static Optional<VideoMode> read(@Nullable String s) {
      if (s == null) {
         return Optional.empty();
      } else {
         try {
            Matcher matcher = PATTERN.matcher(s);
            if (matcher.matches()) {
               int i = Integer.parseInt(matcher.group(1));
               int j = Integer.parseInt(matcher.group(2));
               String s1 = matcher.group(3);
               int k;
               if (s1 == null) {
                  k = 60;
               } else {
                  k = Integer.parseInt(s1);
               }

               String s2 = matcher.group(4);
               int i1;
               if (s2 == null) {
                  i1 = 24;
               } else {
                  i1 = Integer.parseInt(s2);
               }

               int k1 = i1 / 3;
               return Optional.of(new VideoMode(i, j, k1, k1, k1, k));
            }
         } catch (Exception var9) {
         }

         return Optional.empty();
      }
   }

   public String write() {
      return String.format(Locale.ROOT, "%sx%s@%s:%s", this.width, this.height, this.refreshRate, this.redBits + this.greenBits + this.blueBits);
   }
}
