package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
   private final int entityId;
   private final ServerboundInteractPacket.Action action;
   private final boolean usingSecondaryAction;
   static final ServerboundInteractPacket.Action ATTACK_ACTION = new ServerboundInteractPacket.Action() {
      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.ATTACK;
      }

      public void dispatch(ServerboundInteractPacket.Handler serverboundinteractpacket_handler) {
         serverboundinteractpacket_handler.onAttack();
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
      }
   };

   private ServerboundInteractPacket(int i, boolean flag, ServerboundInteractPacket.Action serverboundinteractpacket_action) {
      this.entityId = i;
      this.action = serverboundinteractpacket_action;
      this.usingSecondaryAction = flag;
   }

   public static ServerboundInteractPacket createAttackPacket(Entity entity, boolean flag) {
      return new ServerboundInteractPacket(entity.getId(), flag, ATTACK_ACTION);
   }

   public static ServerboundInteractPacket createInteractionPacket(Entity entity, boolean flag, InteractionHand interactionhand) {
      return new ServerboundInteractPacket(entity.getId(), flag, new ServerboundInteractPacket.InteractionAction(interactionhand));
   }

   public static ServerboundInteractPacket createInteractionPacket(Entity entity, boolean flag, InteractionHand interactionhand, Vec3 vec3) {
      return new ServerboundInteractPacket(entity.getId(), flag, new ServerboundInteractPacket.InteractionAtLocationAction(interactionhand, vec3));
   }

   public ServerboundInteractPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityId = friendlybytebuf.readVarInt();
      ServerboundInteractPacket.ActionType serverboundinteractpacket_actiontype = friendlybytebuf.readEnum(ServerboundInteractPacket.ActionType.class);
      this.action = serverboundinteractpacket_actiontype.reader.apply(friendlybytebuf);
      this.usingSecondaryAction = friendlybytebuf.readBoolean();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.entityId);
      friendlybytebuf.writeEnum(this.action.getType());
      this.action.write(friendlybytebuf);
      friendlybytebuf.writeBoolean(this.usingSecondaryAction);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleInteract(this);
   }

   @Nullable
   public Entity getTarget(ServerLevel serverlevel) {
      return serverlevel.getEntityOrPart(this.entityId);
   }

   public boolean isUsingSecondaryAction() {
      return this.usingSecondaryAction;
   }

   public void dispatch(ServerboundInteractPacket.Handler serverboundinteractpacket_handler) {
      this.action.dispatch(serverboundinteractpacket_handler);
   }

   interface Action {
      ServerboundInteractPacket.ActionType getType();

      void dispatch(ServerboundInteractPacket.Handler serverboundinteractpacket_handler);

      void write(FriendlyByteBuf friendlybytebuf);
   }

   static enum ActionType {
      INTERACT(ServerboundInteractPacket.InteractionAction::new),
      ATTACK((friendlybytebuf) -> ServerboundInteractPacket.ATTACK_ACTION),
      INTERACT_AT(ServerboundInteractPacket.InteractionAtLocationAction::new);

      final Function<FriendlyByteBuf, ServerboundInteractPacket.Action> reader;

      private ActionType(Function<FriendlyByteBuf, ServerboundInteractPacket.Action> function) {
         this.reader = function;
      }
   }

   public interface Handler {
      void onInteraction(InteractionHand interactionhand);

      void onInteraction(InteractionHand interactionhand, Vec3 vec3);

      void onAttack();
   }

   static class InteractionAction implements ServerboundInteractPacket.Action {
      private final InteractionHand hand;

      InteractionAction(InteractionHand interactionhand) {
         this.hand = interactionhand;
      }

      private InteractionAction(FriendlyByteBuf friendlybytebuf) {
         this.hand = friendlybytebuf.readEnum(InteractionHand.class);
      }

      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.INTERACT;
      }

      public void dispatch(ServerboundInteractPacket.Handler serverboundinteractpacket_handler) {
         serverboundinteractpacket_handler.onInteraction(this.hand);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeEnum(this.hand);
      }
   }

   static class InteractionAtLocationAction implements ServerboundInteractPacket.Action {
      private final InteractionHand hand;
      private final Vec3 location;

      InteractionAtLocationAction(InteractionHand interactionhand, Vec3 vec3) {
         this.hand = interactionhand;
         this.location = vec3;
      }

      private InteractionAtLocationAction(FriendlyByteBuf friendlybytebuf) {
         this.location = new Vec3((double)friendlybytebuf.readFloat(), (double)friendlybytebuf.readFloat(), (double)friendlybytebuf.readFloat());
         this.hand = friendlybytebuf.readEnum(InteractionHand.class);
      }

      public ServerboundInteractPacket.ActionType getType() {
         return ServerboundInteractPacket.ActionType.INTERACT_AT;
      }

      public void dispatch(ServerboundInteractPacket.Handler serverboundinteractpacket_handler) {
         serverboundinteractpacket_handler.onInteraction(this.hand, this.location);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeFloat((float)this.location.x);
         friendlybytebuf.writeFloat((float)this.location.y);
         friendlybytebuf.writeFloat((float)this.location.z);
         friendlybytebuf.writeEnum(this.hand);
      }
   }
}
