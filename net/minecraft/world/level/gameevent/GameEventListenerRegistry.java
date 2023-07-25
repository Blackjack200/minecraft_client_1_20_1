package net.minecraft.world.level.gameevent;

import net.minecraft.world.phys.Vec3;

public interface GameEventListenerRegistry {
   GameEventListenerRegistry NOOP = new GameEventListenerRegistry() {
      public boolean isEmpty() {
         return true;
      }

      public void register(GameEventListener gameeventlistener) {
      }

      public void unregister(GameEventListener gameeventlistener) {
      }

      public boolean visitInRangeListeners(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context, GameEventListenerRegistry.ListenerVisitor gameeventlistenerregistry_listenervisitor) {
         return false;
      }
   };

   boolean isEmpty();

   void register(GameEventListener gameeventlistener);

   void unregister(GameEventListener gameeventlistener);

   boolean visitInRangeListeners(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context, GameEventListenerRegistry.ListenerVisitor gameeventlistenerregistry_listenervisitor);

   @FunctionalInterface
   public interface ListenerVisitor {
      void visit(GameEventListener gameeventlistener, Vec3 vec3);
   }
}
