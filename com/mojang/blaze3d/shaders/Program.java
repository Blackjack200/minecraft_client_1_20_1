package com.mojang.blaze3d.shaders;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Program {
   private static final int MAX_LOG_LENGTH = 32768;
   private final Program.Type type;
   private final String name;
   private int id;

   protected Program(Program.Type program_type, int i, String s) {
      this.type = program_type;
      this.id = i;
      this.name = s;
   }

   public void attachToShader(Shader shader) {
      RenderSystem.assertOnRenderThread();
      GlStateManager.glAttachShader(shader.getId(), this.getId());
   }

   public void close() {
      if (this.id != -1) {
         RenderSystem.assertOnRenderThread();
         GlStateManager.glDeleteShader(this.id);
         this.id = -1;
         this.type.getPrograms().remove(this.name);
      }
   }

   public String getName() {
      return this.name;
   }

   public static Program compileShader(Program.Type program_type, String s, InputStream inputstream, String s1, GlslPreprocessor glslpreprocessor) throws IOException {
      RenderSystem.assertOnRenderThread();
      int i = compileShaderInternal(program_type, s, inputstream, s1, glslpreprocessor);
      Program program = new Program(program_type, i, s);
      program_type.getPrograms().put(s, program);
      return program;
   }

   protected static int compileShaderInternal(Program.Type program_type, String s, InputStream inputstream, String s1, GlslPreprocessor glslpreprocessor) throws IOException {
      String s2 = IOUtils.toString(inputstream, StandardCharsets.UTF_8);
      if (s2 == null) {
         throw new IOException("Could not load program " + program_type.getName());
      } else {
         int i = GlStateManager.glCreateShader(program_type.getGlType());
         GlStateManager.glShaderSource(i, glslpreprocessor.process(s2));
         GlStateManager.glCompileShader(i);
         if (GlStateManager.glGetShaderi(i, 35713) == 0) {
            String s3 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
            throw new IOException("Couldn't compile " + program_type.getName() + " program (" + s1 + ", " + s + ") : " + s3);
         } else {
            return i;
         }
      }
   }

   protected int getId() {
      return this.id;
   }

   public static enum Type {
      VERTEX("vertex", ".vsh", 35633),
      FRAGMENT("fragment", ".fsh", 35632);

      private final String name;
      private final String extension;
      private final int glType;
      private final Map<String, Program> programs = Maps.newHashMap();

      private Type(String s, String s1, int i) {
         this.name = s;
         this.extension = s1;
         this.glType = i;
      }

      public String getName() {
         return this.name;
      }

      public String getExtension() {
         return this.extension;
      }

      int getGlType() {
         return this.glType;
      }

      public Map<String, Program> getPrograms() {
         return this.programs;
      }
   }
}
