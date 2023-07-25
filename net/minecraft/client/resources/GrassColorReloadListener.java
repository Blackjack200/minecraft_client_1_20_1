package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GrassColor;

public class GrassColorReloadListener extends SimplePreparableReloadListener<int[]> {
   private static final ResourceLocation LOCATION = new ResourceLocation("textures/colormap/grass.png");

   protected int[] prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      try {
         return LegacyStuffWrapper.getPixels(resourcemanager, LOCATION);
      } catch (IOException var4) {
         throw new IllegalStateException("Failed to load grass color texture", var4);
      }
   }

   protected void apply(int[] aint, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      GrassColor.init(aint);
   }
}
