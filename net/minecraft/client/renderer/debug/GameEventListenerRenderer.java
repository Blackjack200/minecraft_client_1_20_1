package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class GameEventListenerRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private static final int LISTENER_RENDER_DIST = 32;
   private static final float BOX_HEIGHT = 1.0F;
   private final List<GameEventListenerRenderer.TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
   private final List<GameEventListenerRenderer.TrackedListener> trackedListeners = Lists.newArrayList();

   public GameEventListenerRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      Level level = this.minecraft.level;
      if (level == null) {
         this.trackedGameEvents.clear();
         this.trackedListeners.clear();
      } else {
         Vec3 vec3 = new Vec3(d0, 0.0D, d2);
         this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
         this.trackedListeners.removeIf((gameeventlistenerrenderer_trackedlistener4) -> gameeventlistenerrenderer_trackedlistener4.isExpired(level, vec3));
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lines());

         for(GameEventListenerRenderer.TrackedListener gameeventlistenerrenderer_trackedlistener : this.trackedListeners) {
            gameeventlistenerrenderer_trackedlistener.getPosition(level).ifPresent((vec34) -> {
               double d16 = vec34.x() - (double)gameeventlistenerrenderer_trackedlistener.getListenerRadius();
               double d17 = vec34.y() - (double)gameeventlistenerrenderer_trackedlistener.getListenerRadius();
               double d18 = vec34.z() - (double)gameeventlistenerrenderer_trackedlistener.getListenerRadius();
               double d19 = vec34.x() + (double)gameeventlistenerrenderer_trackedlistener.getListenerRadius();
               double d20 = vec34.y() + (double)gameeventlistenerrenderer_trackedlistener.getListenerRadius();
               double d21 = vec34.z() + (double)gameeventlistenerrenderer_trackedlistener.getListenerRadius();
               LevelRenderer.renderVoxelShape(posestack, vertexconsumer, Shapes.create(new AABB(d16, d17, d18, d19, d20, d21)), -d0, -d1, -d2, 1.0F, 1.0F, 0.0F, 0.35F, true);
            });
         }

         VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(RenderType.debugFilledBox());

         for(GameEventListenerRenderer.TrackedListener gameeventlistenerrenderer_trackedlistener1 : this.trackedListeners) {
            gameeventlistenerrenderer_trackedlistener1.getPosition(level).ifPresent((vec33) -> LevelRenderer.addChainedFilledBoxVertices(posestack, vertexconsumer1, vec33.x() - 0.25D - d0, vec33.y() - d1, vec33.z() - 0.25D - d2, vec33.x() + 0.25D - d0, vec33.y() - d1 + 1.0D, vec33.z() + 0.25D - d2, 1.0F, 1.0F, 0.0F, 0.35F));
         }

         for(GameEventListenerRenderer.TrackedListener gameeventlistenerrenderer_trackedlistener2 : this.trackedListeners) {
            gameeventlistenerrenderer_trackedlistener2.getPosition(level).ifPresent((vec32) -> {
               DebugRenderer.renderFloatingText(posestack, multibuffersource, "Listener Origin", vec32.x(), vec32.y() + (double)1.8F, vec32.z(), -1, 0.025F);
               DebugRenderer.renderFloatingText(posestack, multibuffersource, BlockPos.containing(vec32).toString(), vec32.x(), vec32.y() + 1.5D, vec32.z(), -6959665, 0.025F);
            });
         }

         for(GameEventListenerRenderer.TrackedGameEvent gameeventlistenerrenderer_trackedgameevent : this.trackedGameEvents) {
            Vec3 vec31 = gameeventlistenerrenderer_trackedgameevent.position;
            double d3 = (double)0.2F;
            double d4 = vec31.x - (double)0.2F;
            double d5 = vec31.y - (double)0.2F;
            double d6 = vec31.z - (double)0.2F;
            double d7 = vec31.x + (double)0.2F;
            double d8 = vec31.y + (double)0.2F + 0.5D;
            double d9 = vec31.z + (double)0.2F;
            renderFilledBox(posestack, multibuffersource, new AABB(d4, d5, d6, d7, d8, d9), 1.0F, 1.0F, 1.0F, 0.2F);
            DebugRenderer.renderFloatingText(posestack, multibuffersource, gameeventlistenerrenderer_trackedgameevent.gameEvent.getName(), vec31.x, vec31.y + (double)0.85F, vec31.z, -7564911, 0.0075F);
         }

      }
   }

   private static void renderFilledBox(PoseStack posestack, MultiBufferSource multibuffersource, AABB aabb, float f, float f1, float f2, float f3) {
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      if (camera.isInitialized()) {
         Vec3 vec3 = camera.getPosition().reverse();
         DebugRenderer.renderFilledBox(posestack, multibuffersource, aabb.move(vec3), f, f1, f2, f3);
      }
   }

   public void trackGameEvent(GameEvent gameevent, Vec3 vec3) {
      this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), gameevent, vec3));
   }

   public void trackListener(PositionSource positionsource, int i) {
      this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(positionsource, i));
   }

   static record TrackedGameEvent(long timeStamp, GameEvent gameEvent, Vec3 position) {
      final GameEvent gameEvent;
      final Vec3 position;

      public boolean isExpired() {
         return Util.getMillis() - this.timeStamp > 3000L;
      }
   }

   static class TrackedListener implements GameEventListener {
      public final PositionSource listenerSource;
      public final int listenerRange;

      public TrackedListener(PositionSource positionsource, int i) {
         this.listenerSource = positionsource;
         this.listenerRange = i;
      }

      public boolean isExpired(Level level, Vec3 vec3) {
         return this.listenerSource.getPosition(level).filter((vec32) -> vec32.distanceToSqr(vec3) <= 1024.0D).isPresent();
      }

      public Optional<Vec3> getPosition(Level level) {
         return this.listenerSource.getPosition(level);
      }

      public PositionSource getListenerSource() {
         return this.listenerSource;
      }

      public int getListenerRadius() {
         return this.listenerRange;
      }

      public boolean handleGameEvent(ServerLevel serverlevel, GameEvent gameevent, GameEvent.Context gameevent_context, Vec3 vec3) {
         return false;
      }
   }
}
