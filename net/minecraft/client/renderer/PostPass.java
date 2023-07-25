package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;

public class PostPass implements AutoCloseable {
   private final EffectInstance effect;
   public final RenderTarget inTarget;
   public final RenderTarget outTarget;
   private final List<IntSupplier> auxAssets = Lists.newArrayList();
   private final List<String> auxNames = Lists.newArrayList();
   private final List<Integer> auxWidths = Lists.newArrayList();
   private final List<Integer> auxHeights = Lists.newArrayList();
   private Matrix4f shaderOrthoMatrix;

   public PostPass(ResourceManager resourcemanager, String s, RenderTarget rendertarget, RenderTarget rendertarget1) throws IOException {
      this.effect = new EffectInstance(resourcemanager, s);
      this.inTarget = rendertarget;
      this.outTarget = rendertarget1;
   }

   public void close() {
      this.effect.close();
   }

   public final String getName() {
      return this.effect.getName();
   }

   public void addAuxAsset(String s, IntSupplier intsupplier, int i, int j) {
      this.auxNames.add(this.auxNames.size(), s);
      this.auxAssets.add(this.auxAssets.size(), intsupplier);
      this.auxWidths.add(this.auxWidths.size(), i);
      this.auxHeights.add(this.auxHeights.size(), j);
   }

   public void setOrthoMatrix(Matrix4f matrix4f) {
      this.shaderOrthoMatrix = matrix4f;
   }

   public void process(float f) {
      this.inTarget.unbindWrite();
      float f1 = (float)this.outTarget.width;
      float f2 = (float)this.outTarget.height;
      RenderSystem.viewport(0, 0, (int)f1, (int)f2);
      this.effect.setSampler("DiffuseSampler", this.inTarget::getColorTextureId);

      for(int i = 0; i < this.auxAssets.size(); ++i) {
         this.effect.setSampler(this.auxNames.get(i), this.auxAssets.get(i));
         this.effect.safeGetUniform("AuxSize" + i).set((float)this.auxWidths.get(i).intValue(), (float)this.auxHeights.get(i).intValue());
      }

      this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
      this.effect.safeGetUniform("InSize").set((float)this.inTarget.width, (float)this.inTarget.height);
      this.effect.safeGetUniform("OutSize").set(f1, f2);
      this.effect.safeGetUniform("Time").set(f);
      Minecraft minecraft = Minecraft.getInstance();
      this.effect.safeGetUniform("ScreenSize").set((float)minecraft.getWindow().getWidth(), (float)minecraft.getWindow().getHeight());
      this.effect.apply();
      this.outTarget.clear(Minecraft.ON_OSX);
      this.outTarget.bindWrite(false);
      RenderSystem.depthFunc(519);
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
      bufferbuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
      bufferbuilder.vertex((double)f1, 0.0D, 500.0D).endVertex();
      bufferbuilder.vertex((double)f1, (double)f2, 500.0D).endVertex();
      bufferbuilder.vertex(0.0D, (double)f2, 500.0D).endVertex();
      BufferUploader.draw(bufferbuilder.end());
      RenderSystem.depthFunc(515);
      this.effect.clear();
      this.outTarget.unbindWrite();
      this.inTarget.unbindRead();

      for(Object object : this.auxAssets) {
         if (object instanceof RenderTarget) {
            ((RenderTarget)object).unbindRead();
         }
      }

   }

   public EffectInstance getEffect() {
      return this.effect;
   }
}
