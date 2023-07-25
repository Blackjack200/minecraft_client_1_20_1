package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
   private final ServerLevel level;

   public GameEventDispatcher(ServerLevel serverlevel) {
      this.level = serverlevel;
   }

   public void post(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context) {
      int i = gameevent.getNotificationRadius();
      BlockPos blockpos = BlockPos.containing(vec3);
      int j = SectionPos.blockToSectionCoord(blockpos.getX() - i);
      int k = SectionPos.blockToSectionCoord(blockpos.getY() - i);
      int l = SectionPos.blockToSectionCoord(blockpos.getZ() - i);
      int i1 = SectionPos.blockToSectionCoord(blockpos.getX() + i);
      int j1 = SectionPos.blockToSectionCoord(blockpos.getY() + i);
      int k1 = SectionPos.blockToSectionCoord(blockpos.getZ() + i);
      List<GameEvent.ListenerInfo> list = new ArrayList<>();
      GameEventListenerRegistry.ListenerVisitor gameeventlistenerregistry_listenervisitor = (gameeventlistener, vec32) -> {
         if (gameeventlistener.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
            list.add(new GameEvent.ListenerInfo(gameevent, vec3, gameevent_context, gameeventlistener, vec32));
         } else {
            gameeventlistener.handleGameEvent(this.level, gameevent, gameevent_context, vec3);
         }

      };
      boolean flag = false;

      for(int l1 = j; l1 <= i1; ++l1) {
         for(int i2 = l; i2 <= k1; ++i2) {
            ChunkAccess chunkaccess = this.level.getChunkSource().getChunkNow(l1, i2);
            if (chunkaccess != null) {
               for(int j2 = k; j2 <= j1; ++j2) {
                  flag |= chunkaccess.getListenerRegistry(j2).visitInRangeListeners(gameevent, vec3, gameevent_context, gameeventlistenerregistry_listenervisitor);
               }
            }
         }
      }

      if (!list.isEmpty()) {
         this.handleGameEventMessagesInQueue(list);
      }

      if (flag) {
         DebugPackets.sendGameEventInfo(this.level, gameevent, vec3);
      }

   }

   private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> list) {
      Collections.sort(list);

      for(GameEvent.ListenerInfo gameevent_listenerinfo : list) {
         GameEventListener gameeventlistener = gameevent_listenerinfo.recipient();
         gameeventlistener.handleGameEvent(this.level, gameevent_listenerinfo.gameEvent(), gameevent_listenerinfo.context(), gameevent_listenerinfo.source());
      }

   }
}
