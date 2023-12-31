package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClientboundSetObjectivePacket implements Packet<ClientGamePacketListener> {
   public static final int METHOD_ADD = 0;
   public static final int METHOD_REMOVE = 1;
   public static final int METHOD_CHANGE = 2;
   private final String objectiveName;
   private final Component displayName;
   private final ObjectiveCriteria.RenderType renderType;
   private final int method;

   public ClientboundSetObjectivePacket(Objective objective, int i) {
      this.objectiveName = objective.getName();
      this.displayName = objective.getDisplayName();
      this.renderType = objective.getRenderType();
      this.method = i;
   }

   public ClientboundSetObjectivePacket(FriendlyByteBuf friendlybytebuf) {
      this.objectiveName = friendlybytebuf.readUtf();
      this.method = friendlybytebuf.readByte();
      if (this.method != 0 && this.method != 2) {
         this.displayName = CommonComponents.EMPTY;
         this.renderType = ObjectiveCriteria.RenderType.INTEGER;
      } else {
         this.displayName = friendlybytebuf.readComponent();
         this.renderType = friendlybytebuf.readEnum(ObjectiveCriteria.RenderType.class);
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.objectiveName);
      friendlybytebuf.writeByte(this.method);
      if (this.method == 0 || this.method == 2) {
         friendlybytebuf.writeComponent(this.displayName);
         friendlybytebuf.writeEnum(this.renderType);
      }

   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAddObjective(this);
   }

   public String getObjectiveName() {
      return this.objectiveName;
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public int getMethod() {
      return this.method;
   }

   public ObjectiveCriteria.RenderType getRenderType() {
      return this.renderType;
   }
}
