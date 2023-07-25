package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST = 160;
   private final Minecraft minecraft;
   private final Map<Integer, List<GoalSelectorDebugRenderer.DebugGoal>> goalSelectors = Maps.newHashMap();

   public void clear() {
      this.goalSelectors.clear();
   }

   public void addGoalSelector(int i, List<GoalSelectorDebugRenderer.DebugGoal> list) {
      this.goalSelectors.put(i, list);
   }

   public void removeGoalSelector(int i) {
      this.goalSelectors.remove(i);
   }

   public GoalSelectorDebugRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0D, camera.getPosition().z);
      this.goalSelectors.forEach((integer, list) -> {
         for(int i = 0; i < list.size(); ++i) {
            GoalSelectorDebugRenderer.DebugGoal goalselectordebugrenderer_debuggoal = list.get(i);
            if (blockpos.closerThan(goalselectordebugrenderer_debuggoal.pos, 160.0D)) {
               double d3 = (double)goalselectordebugrenderer_debuggoal.pos.getX() + 0.5D;
               double d4 = (double)goalselectordebugrenderer_debuggoal.pos.getY() + 2.0D + (double)i * 0.25D;
               double d5 = (double)goalselectordebugrenderer_debuggoal.pos.getZ() + 0.5D;
               int j = goalselectordebugrenderer_debuggoal.isRunning ? -16711936 : -3355444;
               DebugRenderer.renderFloatingText(posestack, multibuffersource, goalselectordebugrenderer_debuggoal.name, d3, d4, d5, j);
            }
         }

      });
   }

   public static class DebugGoal {
      public final BlockPos pos;
      public final int priority;
      public final String name;
      public final boolean isRunning;

      public DebugGoal(BlockPos blockpos, int i, String s, boolean flag) {
         this.pos = blockpos;
         this.priority = i;
         this.name = s;
         this.isRunning = flag;
      }
   }
}
