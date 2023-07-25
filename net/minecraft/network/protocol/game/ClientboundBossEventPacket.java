package net.minecraft.network.protocol.game;

import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.BossEvent;

public class ClientboundBossEventPacket implements Packet<ClientGamePacketListener> {
   private static final int FLAG_DARKEN = 1;
   private static final int FLAG_MUSIC = 2;
   private static final int FLAG_FOG = 4;
   private final UUID id;
   private final ClientboundBossEventPacket.Operation operation;
   static final ClientboundBossEventPacket.Operation REMOVE_OPERATION = new ClientboundBossEventPacket.Operation() {
      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.REMOVE;
      }

      public void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
         clientboundbosseventpacket_handler.remove(uuid);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
      }
   };

   private ClientboundBossEventPacket(UUID uuid, ClientboundBossEventPacket.Operation clientboundbosseventpacket_operation) {
      this.id = uuid;
      this.operation = clientboundbosseventpacket_operation;
   }

   public ClientboundBossEventPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readUUID();
      ClientboundBossEventPacket.OperationType clientboundbosseventpacket_operationtype = friendlybytebuf.readEnum(ClientboundBossEventPacket.OperationType.class);
      this.operation = clientboundbosseventpacket_operationtype.reader.apply(friendlybytebuf);
   }

   public static ClientboundBossEventPacket createAddPacket(BossEvent bossevent) {
      return new ClientboundBossEventPacket(bossevent.getId(), new ClientboundBossEventPacket.AddOperation(bossevent));
   }

   public static ClientboundBossEventPacket createRemovePacket(UUID uuid) {
      return new ClientboundBossEventPacket(uuid, REMOVE_OPERATION);
   }

   public static ClientboundBossEventPacket createUpdateProgressPacket(BossEvent bossevent) {
      return new ClientboundBossEventPacket(bossevent.getId(), new ClientboundBossEventPacket.UpdateProgressOperation(bossevent.getProgress()));
   }

   public static ClientboundBossEventPacket createUpdateNamePacket(BossEvent bossevent) {
      return new ClientboundBossEventPacket(bossevent.getId(), new ClientboundBossEventPacket.UpdateNameOperation(bossevent.getName()));
   }

   public static ClientboundBossEventPacket createUpdateStylePacket(BossEvent bossevent) {
      return new ClientboundBossEventPacket(bossevent.getId(), new ClientboundBossEventPacket.UpdateStyleOperation(bossevent.getColor(), bossevent.getOverlay()));
   }

   public static ClientboundBossEventPacket createUpdatePropertiesPacket(BossEvent bossevent) {
      return new ClientboundBossEventPacket(bossevent.getId(), new ClientboundBossEventPacket.UpdatePropertiesOperation(bossevent.shouldDarkenScreen(), bossevent.shouldPlayBossMusic(), bossevent.shouldCreateWorldFog()));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUUID(this.id);
      friendlybytebuf.writeEnum(this.operation.getType());
      this.operation.write(friendlybytebuf);
   }

   static int encodeProperties(boolean flag, boolean flag1, boolean flag2) {
      int i = 0;
      if (flag) {
         i |= 1;
      }

      if (flag1) {
         i |= 2;
      }

      if (flag2) {
         i |= 4;
      }

      return i;
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleBossUpdate(this);
   }

   public void dispatch(ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
      this.operation.dispatch(this.id, clientboundbosseventpacket_handler);
   }

   static class AddOperation implements ClientboundBossEventPacket.Operation {
      private final Component name;
      private final float progress;
      private final BossEvent.BossBarColor color;
      private final BossEvent.BossBarOverlay overlay;
      private final boolean darkenScreen;
      private final boolean playMusic;
      private final boolean createWorldFog;

      AddOperation(BossEvent bossevent) {
         this.name = bossevent.getName();
         this.progress = bossevent.getProgress();
         this.color = bossevent.getColor();
         this.overlay = bossevent.getOverlay();
         this.darkenScreen = bossevent.shouldDarkenScreen();
         this.playMusic = bossevent.shouldPlayBossMusic();
         this.createWorldFog = bossevent.shouldCreateWorldFog();
      }

      private AddOperation(FriendlyByteBuf friendlybytebuf) {
         this.name = friendlybytebuf.readComponent();
         this.progress = friendlybytebuf.readFloat();
         this.color = friendlybytebuf.readEnum(BossEvent.BossBarColor.class);
         this.overlay = friendlybytebuf.readEnum(BossEvent.BossBarOverlay.class);
         int i = friendlybytebuf.readUnsignedByte();
         this.darkenScreen = (i & 1) > 0;
         this.playMusic = (i & 2) > 0;
         this.createWorldFog = (i & 4) > 0;
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.ADD;
      }

      public void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
         clientboundbosseventpacket_handler.add(uuid, this.name, this.progress, this.color, this.overlay, this.darkenScreen, this.playMusic, this.createWorldFog);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeComponent(this.name);
         friendlybytebuf.writeFloat(this.progress);
         friendlybytebuf.writeEnum(this.color);
         friendlybytebuf.writeEnum(this.overlay);
         friendlybytebuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
      }
   }

   public interface Handler {
      default void add(UUID uuid, Component component, float f, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay, boolean flag, boolean flag1, boolean flag2) {
      }

      default void remove(UUID uuid) {
      }

      default void updateProgress(UUID uuid, float f) {
      }

      default void updateName(UUID uuid, Component component) {
      }

      default void updateStyle(UUID uuid, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
      }

      default void updateProperties(UUID uuid, boolean flag, boolean flag1, boolean flag2) {
      }
   }

   interface Operation {
      ClientboundBossEventPacket.OperationType getType();

      void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler);

      void write(FriendlyByteBuf friendlybytebuf);
   }

   static enum OperationType {
      ADD(ClientboundBossEventPacket.AddOperation::new),
      REMOVE((friendlybytebuf) -> ClientboundBossEventPacket.REMOVE_OPERATION),
      UPDATE_PROGRESS(ClientboundBossEventPacket.UpdateProgressOperation::new),
      UPDATE_NAME(ClientboundBossEventPacket.UpdateNameOperation::new),
      UPDATE_STYLE(ClientboundBossEventPacket.UpdateStyleOperation::new),
      UPDATE_PROPERTIES(ClientboundBossEventPacket.UpdatePropertiesOperation::new);

      final Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> reader;

      private OperationType(Function<FriendlyByteBuf, ClientboundBossEventPacket.Operation> function) {
         this.reader = function;
      }
   }

   static class UpdateNameOperation implements ClientboundBossEventPacket.Operation {
      private final Component name;

      UpdateNameOperation(Component component) {
         this.name = component;
      }

      private UpdateNameOperation(FriendlyByteBuf friendlybytebuf) {
         this.name = friendlybytebuf.readComponent();
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_NAME;
      }

      public void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
         clientboundbosseventpacket_handler.updateName(uuid, this.name);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeComponent(this.name);
      }
   }

   static class UpdateProgressOperation implements ClientboundBossEventPacket.Operation {
      private final float progress;

      UpdateProgressOperation(float f) {
         this.progress = f;
      }

      private UpdateProgressOperation(FriendlyByteBuf friendlybytebuf) {
         this.progress = friendlybytebuf.readFloat();
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_PROGRESS;
      }

      public void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
         clientboundbosseventpacket_handler.updateProgress(uuid, this.progress);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeFloat(this.progress);
      }
   }

   static class UpdatePropertiesOperation implements ClientboundBossEventPacket.Operation {
      private final boolean darkenScreen;
      private final boolean playMusic;
      private final boolean createWorldFog;

      UpdatePropertiesOperation(boolean flag, boolean flag1, boolean flag2) {
         this.darkenScreen = flag;
         this.playMusic = flag1;
         this.createWorldFog = flag2;
      }

      private UpdatePropertiesOperation(FriendlyByteBuf friendlybytebuf) {
         int i = friendlybytebuf.readUnsignedByte();
         this.darkenScreen = (i & 1) > 0;
         this.playMusic = (i & 2) > 0;
         this.createWorldFog = (i & 4) > 0;
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_PROPERTIES;
      }

      public void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
         clientboundbosseventpacket_handler.updateProperties(uuid, this.darkenScreen, this.playMusic, this.createWorldFog);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeByte(ClientboundBossEventPacket.encodeProperties(this.darkenScreen, this.playMusic, this.createWorldFog));
      }
   }

   static class UpdateStyleOperation implements ClientboundBossEventPacket.Operation {
      private final BossEvent.BossBarColor color;
      private final BossEvent.BossBarOverlay overlay;

      UpdateStyleOperation(BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
         this.color = bossevent_bossbarcolor;
         this.overlay = bossevent_bossbaroverlay;
      }

      private UpdateStyleOperation(FriendlyByteBuf friendlybytebuf) {
         this.color = friendlybytebuf.readEnum(BossEvent.BossBarColor.class);
         this.overlay = friendlybytebuf.readEnum(BossEvent.BossBarOverlay.class);
      }

      public ClientboundBossEventPacket.OperationType getType() {
         return ClientboundBossEventPacket.OperationType.UPDATE_STYLE;
      }

      public void dispatch(UUID uuid, ClientboundBossEventPacket.Handler clientboundbosseventpacket_handler) {
         clientboundbosseventpacket_handler.updateStyle(uuid, this.color, this.overlay);
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeEnum(this.color);
         friendlybytebuf.writeEnum(this.overlay);
      }
   }
}
