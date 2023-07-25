package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.joml.Matrix4f;

public class PostChain implements AutoCloseable {
   private static final String MAIN_RENDER_TARGET = "minecraft:main";
   private final RenderTarget screenTarget;
   private final ResourceManager resourceManager;
   private final String name;
   private final List<PostPass> passes = Lists.newArrayList();
   private final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
   private final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
   private Matrix4f shaderOrthoMatrix;
   private int screenWidth;
   private int screenHeight;
   private float time;
   private float lastStamp;

   public PostChain(TextureManager texturemanager, ResourceManager resourcemanager, RenderTarget rendertarget, ResourceLocation resourcelocation) throws IOException, JsonSyntaxException {
      this.resourceManager = resourcemanager;
      this.screenTarget = rendertarget;
      this.time = 0.0F;
      this.lastStamp = 0.0F;
      this.screenWidth = rendertarget.viewWidth;
      this.screenHeight = rendertarget.viewHeight;
      this.name = resourcelocation.toString();
      this.updateOrthoMatrix();
      this.load(texturemanager, resourcelocation);
   }

   private void load(TextureManager texturemanager, ResourceLocation resourcelocation) throws IOException, JsonSyntaxException {
      Resource resource = this.resourceManager.getResourceOrThrow(resourcelocation);

      try {
         Reader reader = resource.openAsReader();

         try {
            JsonObject jsonobject = GsonHelper.parse(reader);
            if (GsonHelper.isArrayNode(jsonobject, "targets")) {
               JsonArray jsonarray = jsonobject.getAsJsonArray("targets");
               int i = 0;

               for(JsonElement jsonelement : jsonarray) {
                  try {
                     this.parseTargetNode(jsonelement);
                  } catch (Exception var14) {
                     ChainedJsonException chainedjsonexception = ChainedJsonException.forException(var14);
                     chainedjsonexception.prependJsonKey("targets[" + i + "]");
                     throw chainedjsonexception;
                  }

                  ++i;
               }
            }

            if (GsonHelper.isArrayNode(jsonobject, "passes")) {
               JsonArray jsonarray1 = jsonobject.getAsJsonArray("passes");
               int j = 0;

               for(JsonElement jsonelement1 : jsonarray1) {
                  try {
                     this.parsePassNode(texturemanager, jsonelement1);
                  } catch (Exception var13) {
                     ChainedJsonException chainedjsonexception1 = ChainedJsonException.forException(var13);
                     chainedjsonexception1.prependJsonKey("passes[" + j + "]");
                     throw chainedjsonexception1;
                  }

                  ++j;
               }
            }
         } catch (Throwable var15) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var12) {
                  var15.addSuppressed(var12);
               }
            }

            throw var15;
         }

         if (reader != null) {
            reader.close();
         }

      } catch (Exception var16) {
         ChainedJsonException chainedjsonexception2 = ChainedJsonException.forException(var16);
         chainedjsonexception2.setFilenameAndFlush(resourcelocation.getPath() + " (" + resource.sourcePackId() + ")");
         throw chainedjsonexception2;
      }
   }

   private void parseTargetNode(JsonElement jsonelement) throws ChainedJsonException {
      if (GsonHelper.isStringValue(jsonelement)) {
         this.addTempTarget(jsonelement.getAsString(), this.screenWidth, this.screenHeight);
      } else {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "target");
         String s = GsonHelper.getAsString(jsonobject, "name");
         int i = GsonHelper.getAsInt(jsonobject, "width", this.screenWidth);
         int j = GsonHelper.getAsInt(jsonobject, "height", this.screenHeight);
         if (this.customRenderTargets.containsKey(s)) {
            throw new ChainedJsonException(s + " is already defined");
         }

         this.addTempTarget(s, i, j);
      }

   }

   private void parsePassNode(TextureManager texturemanager, JsonElement jsonelement) throws IOException {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "pass");
      String s = GsonHelper.getAsString(jsonobject, "name");
      String s1 = GsonHelper.getAsString(jsonobject, "intarget");
      String s2 = GsonHelper.getAsString(jsonobject, "outtarget");
      RenderTarget rendertarget = this.getRenderTarget(s1);
      RenderTarget rendertarget1 = this.getRenderTarget(s2);
      if (rendertarget == null) {
         throw new ChainedJsonException("Input target '" + s1 + "' does not exist");
      } else if (rendertarget1 == null) {
         throw new ChainedJsonException("Output target '" + s2 + "' does not exist");
      } else {
         PostPass postpass = this.addPass(s, rendertarget, rendertarget1);
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "auxtargets", (JsonArray)null);
         if (jsonarray != null) {
            int i = 0;

            for(JsonElement jsonelement1 : jsonarray) {
               try {
                  JsonObject jsonobject1 = GsonHelper.convertToJsonObject(jsonelement1, "auxtarget");
                  String s3 = GsonHelper.getAsString(jsonobject1, "name");
                  String s4 = GsonHelper.getAsString(jsonobject1, "id");
                  boolean flag;
                  String s5;
                  if (s4.endsWith(":depth")) {
                     flag = true;
                     s5 = s4.substring(0, s4.lastIndexOf(58));
                  } else {
                     flag = false;
                     s5 = s4;
                  }

                  RenderTarget rendertarget2 = this.getRenderTarget(s5);
                  if (rendertarget2 == null) {
                     if (flag) {
                        throw new ChainedJsonException("Render target '" + s5 + "' can't be used as depth buffer");
                     }

                     ResourceLocation resourcelocation = new ResourceLocation("textures/effect/" + s5 + ".png");
                     this.resourceManager.getResource(resourcelocation).orElseThrow(() -> new ChainedJsonException("Render target or texture '" + s5 + "' does not exist"));
                     RenderSystem.setShaderTexture(0, resourcelocation);
                     texturemanager.bindForSetup(resourcelocation);
                     AbstractTexture abstracttexture = texturemanager.getTexture(resourcelocation);
                     int j = GsonHelper.getAsInt(jsonobject1, "width");
                     int k = GsonHelper.getAsInt(jsonobject1, "height");
                     boolean flag2 = GsonHelper.getAsBoolean(jsonobject1, "bilinear");
                     if (flag2) {
                        RenderSystem.texParameter(3553, 10241, 9729);
                        RenderSystem.texParameter(3553, 10240, 9729);
                     } else {
                        RenderSystem.texParameter(3553, 10241, 9728);
                        RenderSystem.texParameter(3553, 10240, 9728);
                     }

                     postpass.addAuxAsset(s3, abstracttexture::getId, j, k);
                  } else if (flag) {
                     postpass.addAuxAsset(s3, rendertarget2::getDepthTextureId, rendertarget2.width, rendertarget2.height);
                  } else {
                     postpass.addAuxAsset(s3, rendertarget2::getColorTextureId, rendertarget2.width, rendertarget2.height);
                  }
               } catch (Exception var26) {
                  ChainedJsonException chainedjsonexception = ChainedJsonException.forException(var26);
                  chainedjsonexception.prependJsonKey("auxtargets[" + i + "]");
                  throw chainedjsonexception;
               }

               ++i;
            }
         }

         JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "uniforms", (JsonArray)null);
         if (jsonarray1 != null) {
            int l = 0;

            for(JsonElement jsonelement2 : jsonarray1) {
               try {
                  this.parseUniformNode(jsonelement2);
               } catch (Exception var25) {
                  ChainedJsonException chainedjsonexception1 = ChainedJsonException.forException(var25);
                  chainedjsonexception1.prependJsonKey("uniforms[" + l + "]");
                  throw chainedjsonexception1;
               }

               ++l;
            }
         }

      }
   }

   private void parseUniformNode(JsonElement jsonelement) throws ChainedJsonException {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "uniform");
      String s = GsonHelper.getAsString(jsonobject, "name");
      Uniform uniform = this.passes.get(this.passes.size() - 1).getEffect().getUniform(s);
      if (uniform == null) {
         throw new ChainedJsonException("Uniform '" + s + "' does not exist");
      } else {
         float[] afloat = new float[4];
         int i = 0;

         for(JsonElement jsonelement1 : GsonHelper.getAsJsonArray(jsonobject, "values")) {
            try {
               afloat[i] = GsonHelper.convertToFloat(jsonelement1, "value");
            } catch (Exception var12) {
               ChainedJsonException chainedjsonexception = ChainedJsonException.forException(var12);
               chainedjsonexception.prependJsonKey("values[" + i + "]");
               throw chainedjsonexception;
            }

            ++i;
         }

         switch (i) {
            case 0:
            default:
               break;
            case 1:
               uniform.set(afloat[0]);
               break;
            case 2:
               uniform.set(afloat[0], afloat[1]);
               break;
            case 3:
               uniform.set(afloat[0], afloat[1], afloat[2]);
               break;
            case 4:
               uniform.set(afloat[0], afloat[1], afloat[2], afloat[3]);
         }

      }
   }

   public RenderTarget getTempTarget(String s) {
      return this.customRenderTargets.get(s);
   }

   public void addTempTarget(String s, int i, int j) {
      RenderTarget rendertarget = new TextureTarget(i, j, true, Minecraft.ON_OSX);
      rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.customRenderTargets.put(s, rendertarget);
      if (i == this.screenWidth && j == this.screenHeight) {
         this.fullSizedTargets.add(rendertarget);
      }

   }

   public void close() {
      for(RenderTarget rendertarget : this.customRenderTargets.values()) {
         rendertarget.destroyBuffers();
      }

      for(PostPass postpass : this.passes) {
         postpass.close();
      }

      this.passes.clear();
   }

   public PostPass addPass(String s, RenderTarget rendertarget, RenderTarget rendertarget1) throws IOException {
      PostPass postpass = new PostPass(this.resourceManager, s, rendertarget, rendertarget1);
      this.passes.add(this.passes.size(), postpass);
      return postpass;
   }

   private void updateOrthoMatrix() {
      this.shaderOrthoMatrix = (new Matrix4f()).setOrtho(0.0F, (float)this.screenTarget.width, 0.0F, (float)this.screenTarget.height, 0.1F, 1000.0F);
   }

   public void resize(int i, int j) {
      this.screenWidth = this.screenTarget.width;
      this.screenHeight = this.screenTarget.height;
      this.updateOrthoMatrix();

      for(PostPass postpass : this.passes) {
         postpass.setOrthoMatrix(this.shaderOrthoMatrix);
      }

      for(RenderTarget rendertarget : this.fullSizedTargets) {
         rendertarget.resize(i, j, Minecraft.ON_OSX);
      }

   }

   public void process(float f) {
      if (f < this.lastStamp) {
         this.time += 1.0F - this.lastStamp;
         this.time += f;
      } else {
         this.time += f - this.lastStamp;
      }

      for(this.lastStamp = f; this.time > 20.0F; this.time -= 20.0F) {
      }

      for(PostPass postpass : this.passes) {
         postpass.process(this.time / 20.0F);
      }

   }

   public final String getName() {
      return this.name;
   }

   @Nullable
   private RenderTarget getRenderTarget(@Nullable String s) {
      if (s == null) {
         return null;
      } else {
         return s.equals("minecraft:main") ? this.screenTarget : this.customRenderTargets.get(s);
      }
   }
}
