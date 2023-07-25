package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ShaderInstance implements Shader, AutoCloseable {
   public static final String SHADER_PATH = "shaders";
   private static final String SHADER_CORE_PATH = "shaders/core/";
   private static final String SHADER_INCLUDE_PATH = "shaders/include/";
   static final Logger LOGGER = LogUtils.getLogger();
   private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
   private static final boolean ALWAYS_REAPPLY = true;
   private static ShaderInstance lastAppliedShader;
   private static int lastProgramId = -1;
   private final Map<String, Object> samplerMap = Maps.newHashMap();
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
   private final Program vertexProgram;
   private final Program fragmentProgram;
   private final VertexFormat vertexFormat;
   @Nullable
   public final Uniform MODEL_VIEW_MATRIX;
   @Nullable
   public final Uniform PROJECTION_MATRIX;
   @Nullable
   public final Uniform INVERSE_VIEW_ROTATION_MATRIX;
   @Nullable
   public final Uniform TEXTURE_MATRIX;
   @Nullable
   public final Uniform SCREEN_SIZE;
   @Nullable
   public final Uniform COLOR_MODULATOR;
   @Nullable
   public final Uniform LIGHT0_DIRECTION;
   @Nullable
   public final Uniform LIGHT1_DIRECTION;
   @Nullable
   public final Uniform GLINT_ALPHA;
   @Nullable
   public final Uniform FOG_START;
   @Nullable
   public final Uniform FOG_END;
   @Nullable
   public final Uniform FOG_COLOR;
   @Nullable
   public final Uniform FOG_SHAPE;
   @Nullable
   public final Uniform LINE_WIDTH;
   @Nullable
   public final Uniform GAME_TIME;
   @Nullable
   public final Uniform CHUNK_OFFSET;

   public ShaderInstance(ResourceProvider resourceprovider, String s, VertexFormat vertexformat) throws IOException {
      this.name = s;
      this.vertexFormat = vertexformat;
      ResourceLocation resourcelocation = new ResourceLocation("shaders/core/" + s + ".json");

      try {
         Reader reader = resourceprovider.openAsReader(resourcelocation);

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
            this.vertexProgram = getOrCreate(resourceprovider, Program.Type.VERTEX, s1);
            this.fragmentProgram = getOrCreate(resourceprovider, Program.Type.FRAGMENT, s2);
            this.programId = ProgramManager.createProgram();
            if (this.attributeNames != null) {
               int l = 0;

               for(String s3 : vertexformat.getElementAttributeNames()) {
                  Uniform.glBindAttribLocation(this.programId, l, s3);
                  this.attributes.add(l);
                  ++l;
               }
            }

            ProgramManager.linkShader(this);
            this.updateLocations();
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
         chainedjsonexception3.setFilenameAndFlush(resourcelocation.getPath());
         throw chainedjsonexception3;
      }

      this.markDirty();
      this.MODEL_VIEW_MATRIX = this.getUniform("ModelViewMat");
      this.PROJECTION_MATRIX = this.getUniform("ProjMat");
      this.INVERSE_VIEW_ROTATION_MATRIX = this.getUniform("IViewRotMat");
      this.TEXTURE_MATRIX = this.getUniform("TextureMat");
      this.SCREEN_SIZE = this.getUniform("ScreenSize");
      this.COLOR_MODULATOR = this.getUniform("ColorModulator");
      this.LIGHT0_DIRECTION = this.getUniform("Light0_Direction");
      this.LIGHT1_DIRECTION = this.getUniform("Light1_Direction");
      this.GLINT_ALPHA = this.getUniform("GlintAlpha");
      this.FOG_START = this.getUniform("FogStart");
      this.FOG_END = this.getUniform("FogEnd");
      this.FOG_COLOR = this.getUniform("FogColor");
      this.FOG_SHAPE = this.getUniform("FogShape");
      this.LINE_WIDTH = this.getUniform("LineWidth");
      this.GAME_TIME = this.getUniform("GameTime");
      this.CHUNK_OFFSET = this.getUniform("ChunkOffset");
   }

   private static Program getOrCreate(final ResourceProvider resourceprovider, Program.Type program_type, String s) throws IOException {
      Program program = program_type.getPrograms().get(s);
      Program program1;
      if (program == null) {
         String s1 = "shaders/core/" + s + program_type.getExtension();
         Resource resource = resourceprovider.getResourceOrThrow(new ResourceLocation(s1));
         InputStream inputstream = resource.open();

         try {
            final String s2 = FileUtil.getFullResourcePath(s1);
            program1 = Program.compileShader(program_type, s, inputstream, resource.sourcePackId(), new GlslPreprocessor() {
               private final Set<String> importedPaths = Sets.newHashSet();

               public String applyImport(boolean flag, String s) {
                  s = FileUtil.normalizeResourcePath((flag ? s2 : "shaders/include/") + s);
                  if (!this.importedPaths.add(s)) {
                     return null;
                  } else {
                     ResourceLocation resourcelocation = new ResourceLocation(s);

                     try {
                        Reader reader = resourceprovider.openAsReader(resourcelocation);

                        String var5;
                        try {
                           var5 = IOUtils.toString(reader);
                        } catch (Throwable var8) {
                           if (reader != null) {
                              try {
                                 reader.close();
                              } catch (Throwable var7) {
                                 var8.addSuppressed(var7);
                              }
                           }

                           throw var8;
                        }

                        if (reader != null) {
                           reader.close();
                        }

                        return var5;
                     } catch (IOException var9) {
                        ShaderInstance.LOGGER.error("Could not open GLSL import {}: {}", s, var9.getMessage());
                        return "#error " + var9.getMessage();
                     }
                  }
               }
            });
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
         program1 = program;
      }

      return program1;
   }

   public static BlendMode parseBlendNode(JsonObject jsonobject) {
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
      lastAppliedShader = null;
      int i = GlStateManager._getActiveTexture();

      for(int j = 0; j < this.samplerLocations.size(); ++j) {
         if (this.samplerMap.get(this.samplerNames.get(j)) != null) {
            GlStateManager._activeTexture('\u84c0' + j);
            GlStateManager._bindTexture(0);
         }
      }

      GlStateManager._activeTexture(i);
   }

   public void apply() {
      RenderSystem.assertOnRenderThread();
      this.dirty = false;
      lastAppliedShader = this;
      this.blend.apply();
      if (this.programId != lastProgramId) {
         ProgramManager.glUseProgram(this.programId);
         lastProgramId = this.programId;
      }

      int i = GlStateManager._getActiveTexture();

      for(int j = 0; j < this.samplerLocations.size(); ++j) {
         String s = this.samplerNames.get(j);
         if (this.samplerMap.get(s) != null) {
            int k = Uniform.glGetUniformLocation(this.programId, s);
            Uniform.uploadInteger(k, j);
            RenderSystem.activeTexture('\u84c0' + j);
            Object object = this.samplerMap.get(s);
            int l = -1;
            if (object instanceof RenderTarget) {
               l = ((RenderTarget)object).getColorTextureId();
            } else if (object instanceof AbstractTexture) {
               l = ((AbstractTexture)object).getId();
            } else if (object instanceof Integer) {
               l = (Integer)object;
            }

            if (l != -1) {
               RenderSystem.bindTexture(l);
            }
         }
      }

      GlStateManager._activeTexture(i);

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
         int l = intlist.getInt(k);
         this.samplerNames.remove(l);
      }

      for(Uniform uniform : this.uniforms) {
         String s1 = uniform.getName();
         int i1 = Uniform.glGetUniformLocation(this.programId, s1);
         if (i1 == -1) {
            LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, s1);
         } else {
            this.uniformLocations.add(i1);
            uniform.setLocation(i1);
            this.uniformMap.put(s1, uniform);
         }
      }

   }

   private void parseSamplerNode(JsonElement jsonelement) {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "sampler");
      String s = GsonHelper.getAsString(jsonobject, "name");
      if (!GsonHelper.isStringValue(jsonobject, "file")) {
         this.samplerMap.put(s, (Object)null);
         this.samplerNames.add(s);
      } else {
         this.samplerNames.add(s);
      }
   }

   public void setSampler(String s, Object object) {
      this.samplerMap.put(s, object);
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
            uniform.set(Arrays.copyOfRange(afloat, 0, j));
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
      this.fragmentProgram.attachToShader(this);
      this.vertexProgram.attachToShader(this);
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.programId;
   }
}
