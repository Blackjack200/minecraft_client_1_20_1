package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class VillageSectionsDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST_FOR_VILLAGE_SECTIONS = 60;
   private final Set<SectionPos> villageSections = Sets.newHashSet();

   VillageSectionsDebugRenderer() {
   }

   public void clear() {
      this.villageSections.clear();
   }

   public void setVillageSection(SectionPos sectionpos) {
      this.villageSections.add(sectionpos);
   }

   public void setNotVillageSection(SectionPos sectionpos) {
      this.villageSections.remove(sectionpos);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);
      this.villageSections.forEach((sectionpos) -> {
         if (blockpos.closerThan(sectionpos.center(), 60.0D)) {
            highlightVillageSection(posestack, multibuffersource, sectionpos);
         }

      });
   }

   private static void highlightVillageSection(PoseStack posestack, MultiBufferSource multibuffersource, SectionPos sectionpos) {
      int i = 1;
      BlockPos blockpos = sectionpos.center();
      BlockPos blockpos1 = blockpos.offset(-1, -1, -1);
      BlockPos blockpos2 = blockpos.offset(1, 1, 1);
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos1, blockpos2, 0.2F, 1.0F, 0.2F, 0.15F);
   }
}
