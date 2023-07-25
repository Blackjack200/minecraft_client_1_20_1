package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.shaders.EffectProgram;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public class EffectInstance implements Effect, AutoCloseable {
   private static final String EFFECT_SHADER_PATH = "shaders/program/";
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
   private static final boolean ALWAYS_REAPPLY = true;
   private static EffectInstance lastAppliedEffect;
   private static int lastProgramId = -1;
   private final Map<String, IntSupplier> samplerMap = Maps.newHashMap();
   private final List<String> samplerNames = Lists.newArrayList();
   private final List<Integer> samplerLocations = Lists.newArrayList();
   private final List<Uniform> uniforms = Lists.newArrayList();
   private final List<Integer> uniformLocations = Lists.newArrayList();
   private final Map<String, Uniform> uniformMap = Maps.newHashMap();
   private final int programId;
   private final String name;
   private boolean dirty;
   private final BlendMode blend;
   private final List<Integer> attributes;
   private final List<String> attributeNames;
   private final EffectProgram vertexProgram;
   private final EffectProgram fragmentProgram;

   public EffectInstance(ResourceManager resourcemanager, String s) throws IOException {
      ResourceLocation resourcelocation = new ResourceLocation("shaders/program/" + s + ".json");
      this.name = s;
      Resource resource = resourcemanager.getResourceOrThrow(resourcelocation);

      try {
         Reader reader = resource.openAsReader();

         try {
            JsonObject jsonobject = GsonHelper.parse(reader);
            String s1 = GsonHelper.getAsString(jsonobject, "vertex");
            String s2 = GsonHelper.getAsString(jsonobject, "fragment");
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "samplers", (JsonArray)null);
            if (jsonarray != null) {
               int i = 0;

               for(JsonElement jsonelement : jsonarray) {
                  try {
                     this.parseSamplerNode(jsonelement);
                  } catch (Exception var20) {
                     ChainedJsonException chainedjsonexception = ChainedJsonException.forException(var20);
                     chainedjsonexception.prependJsonKey("samplers[" + i + "]");
                     throw chainedjsonexception;
                  }

                  ++i;
               }
            }

            JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "attributes", (JsonArray)null);
            if (jsonarray1 != null) {
               int j = 0;
               this.attributes = Lists.newArrayListWithCapacity(jsonarray1.size());
               this.attributeNames = Lists.newArrayListWithCapacity(jsonarray1.size());

               for(JsonElement jsonelement1 : jsonarray1) {
                  try {
                     this.attributeNames.add(GsonHelper.convertToString(jsonelement1, "attribute"));
                  } catch (Exception var19) {
                     ChainedJsonException chainedjsonexception1 = ChainedJsonException.forException(var19);
                     chainedjsonexception1.prependJsonKey("attributes[" + j + "]");
                     throw chainedjsonexception1;
                  }

                  ++j;
               }
            } else {
               this.attributes = null;
               this.attributeNames = null;
            }

            JsonArray jsonarray2 = GsonHelper.getAsJsonArray(jsonobject, "uniforms", (JsonArray)null);
            if (jsonarray2 != null) {
               int k = 0;

               for(JsonElement jsonelement2 : jsonarray2) {
                  try {
                     this.parseUniformNode(jsonelement2);
                  } catch (Exception var18) {
                     ChainedJsonException chainedjsonexception2 = ChainedJsonException.forException(var18);
                     chainedjsonexception2.prependJsonKey("uniforms[" + k + "]");
                     throw chainedjsonexception2;
                  }

                  ++k;
               }
            }

            this.blend = parseBlendNode(GsonHelper.getAsJsonObject(jsonobject, "blend", (JsonObject)null));
            this.vertexProgram = getOrCreate(resourcemanager, Program.Type.VERTEX, s1);
            this.fragmentProgram = getOrCreate(resourcemanager, Program.Type.FRAGMENT, s2);
            this.programId = ProgramManager.createProgram();
            ProgramManager.linkShader(this);
            this.updateLocations();
            if (this.attributeNames != null) {
               for(String s3 : this.attributeNames) {
                  int l = Uniform.glGetAttribLocation(this.programId, s3);
                  this.attributes.add(l);
               }
            }
         } catch (Throwable var21) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var17) {
                  var21.addSuppressed(var17);
               }
            }

            throw var21;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (Exception var22) {
         ChainedJsonException chainedjsonexception3 = ChainedJsonException.forException(var22);
         chainedjsonexception3.setFilenameAndFlush(resourcelocation.getPath() + " (" + resource.sourcePackId() + ")");
         throw chainedjsonexception3;
      }

      this.markDirty();
   }

   public static EffectProgram getOrCreate(ResourceManager resourcemanager, Program.Type program_type, String s) throws IOException {
      Program program = program_type.getPrograms().get(s);
      if (program != null && !(program instanceof EffectProgram)) {
         throw new InvalidClassException("Program is not of type EffectProgram");
      } else {
         EffectProgram effectprogram;
         if (program == null) {
            ResourceLocation resourcelocation = new ResourceLocation("shaders/program/" + s + program_type.getExtension());
            Resource resource = resourcemanager.getResourceOrThrow(resourcelocation);
            InputStream inputstream = resource.open();

            try {
               effectprogram = EffectProgram.compileShader(program_type, s, inputstream, resource.sourcePackId());
            } catch (Throwable var11) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (inputstream != null) {
               inputstream.close();
            }
         } else {
            effectprogram = (EffectProgram)program;
         }

         return effectprogram;
      }
   }

   public static BlendMode parseBlendNode(@Nullable JsonObject jsonobject) {
      if (jsonobject == null) {
         return new BlendMode();
      } else {
         int i = 32774;
         int j = 1;
         int k = 0;
         int l = 1;
         int i1 = 0;
         boolean flag = true;
         boolean flag1 = false;
         if (GsonHelper.isStringValue(jsonobject, "func")) {
            i = BlendMode.stringToBlendFunc(jsonobject.get("func").getAsString());
            if (i != 32774) {
               flag = false;
            }
         }

         if (GsonHelper.isStringValue(jsonobject, "srcrgb")) {
            j = BlendMode.stringToBlendFactor(jsonobject.get("srcrgb").getAsString());
            if (j != 1) {
               flag = false;
            }
         }

         if (GsonHelper.isStringValue(jsonobject, "dstrgb")) {
            k = BlendMode.stringToBlendFactor(jsonobject.get("dstrgb").getAsString());
            if (k != 0) {
               flag = false;
            }
         }

         if (GsonHelper.isStringValue(jsonobject, "srcalpha")) {
            l = BlendMode.stringToBlendFactor(jsonobject.get("srcalpha").getAsString());
            if (l != 1) {
               flag = false;
            }

            flag1 = true;
         }

         if (GsonHelper.isStringValue(jsonobject, "dstalpha")) {
            i1 = BlendMode.stringToBlendFactor(jsonobject.get("dstalpha").getAsString());
            if (i1 != 0) {
               flag = false;
            }

            flag1 = true;
         }

         if (flag) {
            return new BlendMode();
         } else {
            return flag1 ? new BlendMode(j, k, l, i1, i) : new BlendMode(j, k, i);
         }
      }
   }

   public void close() {
      for(Uniform uniform : this.uniforms) {
         uniform.close();
      }

      ProgramManager.releaseProgram(this);
   }

   public void clear() {
      RenderSystem.assertOnRenderThread();
      ProgramManager.glUseProgram(0);
      lastProgramId = -1;
      lastAppliedEffect = null;

      for(int i = 0; i < this.samplerLocations.size(); ++i) {
         if (this.samplerMap.get(this.samplerNames.get(i)) != null) {
            GlStateManager._activeTexture('\u84c0' + i);
            GlStateManager._bindTexture(0);
         }
      }

   }

   public void apply() {
      RenderSystem.assertOnGameThread();
      this.dirty = false;
      lastAppliedEffect = this;
      this.blend.apply();
      if (this.programId != lastProgramId) {
         ProgramManager.glUseProgram(this.programId);
         lastProgramId = this.programId;
      }

      for(int i = 0; i < this.samplerLocations.size(); ++i) {
         String s = this.samplerNames.get(i);
         IntSupplier intsupplier = this.samplerMap.get(s);
         if (intsupplier != null) {
            RenderSystem.activeTexture('\u84c0' + i);
            int j = intsupplier.getAsInt();
            if (j != -1) {
               RenderSystem.bindTexture(j);
               Uniform.uploadInteger(this.samplerLocations.get(i), i);
            }
         }
      }

      for(Uniform uniform : this.uniforms) {
         uniform.upload();
      }

   }

   public void markDirty() {
      this.dirty = true;
   }

   @Nullable
   public Uniform getUniform(String s) {
      RenderSystem.assertOnRenderThread();
      return this.uniformMap.get(s);
   }

   public AbstractUniform safeGetUniform(String s) {
      RenderSystem.assertOnGameThread();
      Uniform uniform = this.getUniform(s);
      return (AbstractUniform)(uniform == null ? DUMMY_UNIFORM : uniform);
   }

   private void updateLocations() {
      RenderSystem.assertOnRenderThread();
      IntList intlist = new IntArrayList();

      for(int i = 0; i < this.samplerNames.size(); ++i) {
         String s = this.samplerNames.get(i);
         int j = Uniform.glGetUniformLocation(this.programId, s);
         if (j == -1) {
            LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, s);
            this.samplerMap.remove(s);
            intlist.add(i);
         } else {
            this.samplerLocations.add(j);
         }
      }

      for(int k = intlist.size() - 1; k >= 0; --k) {
         this.samplerNames.remove(intlist.getInt(k));
      }

      for(Uniform uniform : this.uniforms) {
         String s1 = uniform.getName();
         int l = Uniform.glGetUniformLocation(this.programId, s1);
         if (l == -1) {
            LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, s1);
         } else {
            this.uniformLocations.add(l);
            uniform.setLocation(l);
            this.uniformMap.put(s1, uniform);
         }
      }

   }

   private void parseSamplerNode(JsonElement jsonelement) {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "sampler");
      String s = GsonHelper.getAsString(jsonobject, "name");
      if (!GsonHelper.isStringValue(jsonobject, "file")) {
         this.samplerMap.put(s, (IntSupplier)null);
         this.samplerNames.add(s);
      } else {
         this.samplerNames.add(s);
      }
   }

   public void setSampler(String s, IntSupplier intsupplier) {
      if (this.samplerMap.containsKey(s)) {
         this.samplerMap.remove(s);
      }

      this.samplerMap.put(s, intsupplier);
      this.markDirty();
   }

   private void parseUniformNode(JsonElement jsonelement) throws ChainedJsonException {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "uniform");
      String s = GsonHelper.getAsString(jsonobject, "name");
      int i = Uniform.getTypeFromString(GsonHelper.getAsString(jsonobject, "type"));
      int j = GsonHelper.getAsInt(jsonobject, "count");
      float[] afloat = new float[Math.max(j, 16)];
      JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "values");
      if (jsonarray.size() != j && jsonarray.size() > 1) {
         throw new ChainedJsonException("Invalid amount of values specified (expected " + j + ", found " + jsonarray.size() + ")");
      } else {
         int k = 0;

         for(JsonElement jsonelement1 : jsonarray) {
            try {
               afloat[k] = GsonHelper.convertToFloat(jsonelement1, "value");
            } catch (Exception var13) {
               ChainedJsonException chainedjsonexception = ChainedJsonException.forException(var13);
               chainedjsonexception.prependJsonKey("values[" + k + "]");
               throw chainedjsonexception;
            }

            ++k;
         }

         if (j > 1 && jsonarray.size() == 1) {
            while(k < j) {
               afloat[k] = afloat[0];
               ++k;
            }
         }

         int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
         Uniform uniform = new Uniform(s, i + l, j, this);
         if (i <= 3) {
            uniform.setSafe((int)afloat[0], (int)afloat[1], (int)afloat[2], (int)afloat[3]);
         } else if (i <= 7) {
            uniform.setSafe(afloat[0], afloat[1], afloat[2], afloat[3]);
         } else {
            uniform.set(afloat);
         }

         this.uniforms.add(uniform);
      }
   }

   public Program getVertexProgram() {
      return this.vertexProgram;
   }

   public Program getFragmentProgram() {
      return this.fragmentProgram;
   }

   public void attachToProgram() {
      this.fragmentProgram.attachToEffect(this);
      this.vertexProgram.attachToEffect(this);
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.programId;
   }
}
