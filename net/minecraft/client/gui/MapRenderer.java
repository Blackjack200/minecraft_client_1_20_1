package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

public class MapRenderer implements AutoCloseable {
   private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
   static final RenderType MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
   private static final int WIDTH = 128;
   private static final int HEIGHT = 128;
   final TextureManager textureManager;
   private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap<>();

   public MapRenderer(TextureManager texturemanager) {
      this.textureManager = texturemanager;
   }

   public void update(int i, MapItemSavedData mapitemsaveddata) {
      this.getOrCreateMapInstance(i, mapitemsaveddata).forceUpload();
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, MapItemSavedData mapitemsaveddata, boolean flag, int j) {
      this.getOrCreateMapInstance(i, mapitemsaveddata).draw(posestack, multibuffersource, flag, j);
   }

   private MapRenderer.MapInstance getOrCreateMapInstance(int i, MapItemSavedData mapitemsaveddata) {
      return this.maps.compute(i, (integer, maprenderer_mapinstance) -> {
         if (maprenderer_mapinstance == null) {
            return new MapRenderer.MapInstance(integer, mapitemsaveddata);
         } else {
            maprenderer_mapinstance.replaceMapData(mapitemsaveddata);
            return maprenderer_mapinstance;
         }
      });
   }

   public void resetData() {
      for(MapRenderer.MapInstance maprenderer_mapinstance : this.maps.values()) {
         maprenderer_mapinstance.close();
      }

      this.maps.clear();
   }

   public void close() {
      this.resetData();
   }

   class MapInstance implements AutoCloseable {
      private MapItemSavedData data;
      private final DynamicTexture texture;
      private final RenderType renderType;
      private boolean requiresUpload = true;

      MapInstance(int i, MapItemSavedData mapitemsaveddata) {
         this.data = mapitemsaveddata;
         this.texture = new DynamicTexture(128, 128, true);
         ResourceLocation resourcelocation = MapRenderer.this.textureManager.register("map/" + i, this.texture);
         this.renderType = RenderType.text(resourcelocation);
      }

      void replaceMapData(MapItemSavedData mapitemsaveddata) {
         boolean flag = this.data != mapitemsaveddata;
         this.data = mapitemsaveddata;
         this.requiresUpload |= flag;
      }

      public void forceUpload() {
         this.requiresUpload = true;
      }

      private void updateTexture() {
         for(int i = 0; i < 128; ++i) {
            for(int j = 0; j < 128; ++j) {
               int k = j + i * 128;
               this.texture.getPixels().setPixelRGBA(j, i, MapColor.getColorFromPackedId(this.data.colors[k]));
            }
         }

         this.texture.upload();
      }

      void draw(PoseStack posestack, MultiBufferSource multibuffersource, boolean flag, int i) {
         if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
         }

         int j = 0;
         int k = 0;
         float f = 0.0F;
         Matrix4f matrix4f = posestack.last().pose();
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.renderType);
         vertexconsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(i).endVertex();
         vertexconsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(i).endVertex();
         vertexconsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(i).endVertex();
         vertexconsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(i).endVertex();
         int l = 0;

         for(MapDecoration mapdecoration : this.data.getDecorations()) {
            if (!flag || mapdecoration.renderOnFrame()) {
               posestack.pushPose();
               posestack.translate(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F, 0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F, -0.02F);
               posestack.mulPose(Axis.ZP.rotationDegrees((float)(mapdecoration.getRot() * 360) / 16.0F));
               posestack.scale(4.0F, 4.0F, 3.0F);
               posestack.translate(-0.125F, 0.125F, 0.0F);
               byte b0 = mapdecoration.getImage();
               float f1 = (float)(b0 % 16 + 0) / 16.0F;
               float f2 = (float)(b0 / 16 + 0) / 16.0F;
               float f3 = (float)(b0 % 16 + 1) / 16.0F;
               float f4 = (float)(b0 / 16 + 1) / 16.0F;
               Matrix4f matrix4f1 = posestack.last().pose();
               float f5 = -0.001F;
               VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(MapRenderer.MAP_ICONS);
               vertexconsumer1.vertex(matrix4f1, -1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(f1, f2).uv2(i).endVertex();
               vertexconsumer1.vertex(matrix4f1, 1.0F, 1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(f3, f2).uv2(i).endVertex();
               vertexconsumer1.vertex(matrix4f1, 1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(f3, f4).uv2(i).endVertex();
               vertexconsumer1.vertex(matrix4f1, -1.0F, -1.0F, (float)l * -0.001F).color(255, 255, 255, 255).uv(f1, f4).uv2(i).endVertex();
               posestack.popPose();
               if (mapdecoration.getName() != null) {
                  Font font = Minecraft.getInstance().font;
                  Component component = mapdecoration.getName();
                  float f6 = (float)font.width(component);
                  float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
                  posestack.pushPose();
                  posestack.translate(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F - f6 * f7 / 2.0F, 0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F + 4.0F, -0.025F);
                  posestack.scale(f7, f7, 1.0F);
                  posestack.translate(0.0F, 0.0F, -0.1F);
                  font.drawInBatch(component, 0.0F, 0.0F, -1, false, posestack.last().pose(), multibuffersource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, i);
                  posestack.popPose();
               }

               ++l;
            }
         }

      }

      public void close() {
         this.texture.close();
      }
   }
}
