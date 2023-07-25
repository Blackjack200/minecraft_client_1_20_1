package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry implements GameEventListenerRegistry {
   private final List<GameEventListener> listeners = Lists.newArrayList();
   private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
   private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
   private boolean processing;
   private final ServerLevel level;
   private final int sectionY;
   private final EuclideanGameEventListenerRegistry.OnEmptyAction onEmptyAction;

   public EuclideanGameEventListenerRegistry(ServerLevel serverlevel, int i, EuclideanGameEventListenerRegistry.OnEmptyAction euclideangameeventlistenerregistry_onemptyaction) {
      this.level = serverlevel;
      this.sectionY = i;
      this.onEmptyAction = euclideangameeventlistenerregistry_onemptyaction;
   }

   public boolean isEmpty() {
      return this.listeners.isEmpty();
   }

   public void register(GameEventListener gameeventlistener) {
      if (this.processing) {
         this.listenersToAdd.add(gameeventlistener);
      } else {
         this.listeners.add(gameeventlistener);
      }

      DebugPackets.sendGameEventListenerInfo(this.level, gameeventlistener);
   }

   public void unregister(GameEventListener gameeventlistener) {
      if (this.processing) {
         this.listenersToRemove.add(gameeventlistener);
      } else {
         this.listeners.remove(gameeventlistener);
      }

      if (this.listeners.isEmpty()) {
         this.onEmptyAction.apply(this.sectionY);
      }

   }

   public boolean visitInRangeListeners(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context, GameEventListenerRegistry.ListenerVisitor gameeventlistenerregistry_listenervisitor) {
      this.processing = true;
      boolean flag = false;

      try {
         Iterator<GameEventListener> iterator = this.listeners.iterator();

         while(iterator.hasNext()) {
            GameEventListener gameeventlistener = iterator.next();
            if (this.listenersToRemove.remove(gameeventlistener)) {
               iterator.remove();
            } else {
               Optional<Vec3> optional = getPostableListenerPosition(this.level, vec3, gameeventlistener);
               if (optional.isPresent()) {
                  gameeventlistenerregistry_listenervisitor.visit(gameeventlistener, optional.get());
                  flag = true;
               }
            }
         }
      } finally {
         this.processing = false;
      }

      if (!this.listenersToAdd.isEmpty()) {
         this.listeners.addAll(this.listenersToAdd);
         this.listenersToAdd.clear();
      }

      if (!this.listenersToRemove.isEmpty()) {
         this.listeners.removeAll(this.listenersToRemove);
         this.listenersToRemove.clear();
      }

      return flag;
   }

   private static Optional<Vec3> getPostableListenerPosition(ServerLevel serverlevel, Vec3 vec3, GameEventListener gameeventlistener) {
      Optional<Vec3> optional = gameeventlistener.getListenerSource().getPosition(serverlevel);
      if (optional.isEmpty()) {
         return Optional.empty();
      } else {
         double d0 = optional.get().distanceToSqr(vec3);
         int i = gameeventlistener.getListenerRadius() * gameeventlistener.getListenerRadius();
         return d0 > (double)i ? Optional.empty() : optional;
      }
   }

   @FunctionalInterface
   public interface OnEmptyAction {
      void apply(int i);
   }
}
