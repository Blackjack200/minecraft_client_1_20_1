package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private static final int CHUNK_DIST = 2;
   private static final float BOX_HEIGHT = 0.09375F;

   public HeightMapRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      LevelAccessor levelaccessor = this.minecraft.level;
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugFilledBox());
      BlockPos blockpos = BlockPos.containing(d0, 0.0D, d2);

      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            ChunkAccess chunkaccess = levelaccessor.getChunk(blockpos.offset(i * 16, 0, j * 16));

            for(Map.Entry<Heightmap.Types, Heightmap> map_entry : chunkaccess.getHeightmaps()) {
               Heightmap.Types heightmap_types = map_entry.getKey();
               ChunkPos chunkpos = chunkaccess.getPos();
               Vector3f vector3f = this.getColor(heightmap_types);

               for(int k = 0; k < 16; ++k) {
                  for(int l = 0; l < 16; ++l) {
                     int i1 = SectionPos.sectionToBlockCoord(chunkpos.x, k);
                     int j1 = SectionPos.sectionToBlockCoord(chunkpos.z, l);
                     float f = (float)((double)((float)levelaccessor.getHeight(heightmap_types, i1, j1) + (float)heightmap_types.ordinal() * 0.09375F) - d1);
                     LevelRenderer.addChainedFilledBoxVertices(posestack, vertexconsumer, (double)((float)i1 + 0.25F) - d0, (double)f, (double)((float)j1 + 0.25F) - d2, (double)((float)i1 + 0.75F) - d0, (double)(f + 0.09375F), (double)((float)j1 + 0.75F) - d2, vector3f.x(), vector3f.y(), vector3f.z(), 1.0F);
                  }
               }
            }
         }
      }

   }

   private Vector3f getColor(Heightmap.Types heightmap_types) {
      Vector3f var10000;
      switch (heightmap_types) {
         case WORLD_SURFACE_WG:
            var10000 = new Vector3f(1.0F, 1.0F, 0.0F);
            break;
         case OCEAN_FLOOR_WG:
            var10000 = new Vector3f(1.0F, 0.0F, 1.0F);
            break;
         case WORLD_SURFACE:
            var10000 = new Vector3f(0.0F, 0.7F, 0.0F);
            break;
         case OCEAN_FLOOR:
            var10000 = new Vector3f(0.0F, 0.0F, 0.5F);
            break;
         case MOTION_BLOCKING:
            var10000 = new Vector3f(0.0F, 0.3F, 0.3F);
            break;
         case MOTION_BLOCKING_NO_LEAVES:
            var10000 = new Vector3f(0.0F, 0.5F, 0.5F);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
