package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;

public class EffectProgram extends Program {
   private static final GlslPreprocessor PREPROCESSOR = new GlslPreprocessor() {
      public String applyImport(boolean flag, String s) {
         return "#error Import statement not supported";
      }
   };
   private int references;

   private EffectProgram(Program.Type program_type, int i, String s) {
      super(program_type, i, s);
   }

   public void attachToEffect(Effect effect) {
      RenderSystem.assertOnRenderThread();
      ++this.references;
      this.attachToShader(effect);
   }

   public void close() {
      RenderSystem.assertOnRenderThread();
      --this.references;
      if (this.references <= 0) {
         super.close();
      }

   }

   public static EffectProgram compileShader(Program.Type program_type, String s, InputStream inputstream, String s1) throws IOException {
      RenderSystem.assertOnRenderThread();
      int i = compileShaderInternal(program_type, s, inputstream, s1, PREPROCESSOR);
      EffectProgram effectprogram = new EffectProgram(program_type, i, s);
      program_type.getPrograms().put(s, effectprogram);
      return effectprogram;
   }
}
