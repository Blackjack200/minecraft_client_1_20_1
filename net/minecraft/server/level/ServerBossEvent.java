package net.minecraft.server.level;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class ServerBossEvent extends BossEvent {
   private final Set<ServerPlayer> players = Sets.newHashSet();
   private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
   private boolean visible = true;

   public ServerBossEvent(Component component, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
      super(Mth.createInsecureUUID(), component, bossevent_bossbarcolor, bossevent_bossbaroverlay);
   }

   public void setProgress(float f) {
      if (f != this.progress) {
         super.setProgress(f);
         this.broadcast(ClientboundBossEventPacket::createUpdateProgressPacket);
      }

   }

   public void setColor(BossEvent.BossBarColor bossevent_bossbarcolor) {
      if (bossevent_bossbarcolor != this.color) {
         super.setColor(bossevent_bossbarcolor);
         this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
      }

   }

   public void setOverlay(BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
      if (bossevent_bossbaroverlay != this.overlay) {
         super.setOverlay(bossevent_bossbaroverlay);
         this.broadcast(ClientboundBossEventPacket::createUpdateStylePacket);
      }

   }

   public BossEvent setDarkenScreen(boolean flag) {
      if (flag != this.darkenScreen) {
         super.setDarkenScreen(flag);
         this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
      }

      return this;
   }

   public BossEvent setPlayBossMusic(boolean flag) {
      if (flag != this.playBossMusic) {
         super.setPlayBossMusic(flag);
         this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
      }

      return this;
   }

   public BossEvent setCreateWorldFog(boolean flag) {
      if (flag != this.createWorldFog) {
         super.setCreateWorldFog(flag);
         this.broadcast(ClientboundBossEventPacket::createUpdatePropertiesPacket);
      }

      return this;
   }

   public void setName(Component component) {
      if (!Objects.equal(component, this.name)) {
         super.setName(component);
         this.broadcast(ClientboundBossEventPacket::createUpdateNamePacket);
      }

   }

   private void broadcast(Function<BossEvent, ClientboundBossEventPacket> function) {
      if (this.visible) {
         ClientboundBossEventPacket clientboundbosseventpacket = function.apply(this);

         for(ServerPlayer serverplayer : this.players) {
            serverplayer.connection.send(clientboundbosseventpacket);
         }
      }

   }

   public void addPlayer(ServerPlayer serverplayer) {
      if (this.players.add(serverplayer) && this.visible) {
         serverplayer.connection.send(ClientboundBossEventPacket.createAddPacket(this));
      }

   }

   public void removePlayer(ServerPlayer serverplayer) {
      if (this.players.remove(serverplayer) && this.visible) {
         serverplayer.connection.send(ClientboundBossEventPacket.createRemovePacket(this.getId()));
      }

   }

   public void removeAllPlayers() {
      if (!this.players.isEmpty()) {
         for(ServerPlayer serverplayer : Lists.newArrayList(this.players)) {
            this.removePlayer(serverplayer);
         }
      }

   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean flag) {
      if (flag != this.visible) {
         this.visible = flag;

         for(ServerPlayer serverplayer : this.players) {
            serverplayer.connection.send(flag ? ClientboundBossEventPacket.createAddPacket(this) : ClientboundBossEventPacket.createRemovePacket(this.getId()));
         }
      }

   }

   public Collection<ServerPlayer> getPlayers() {
      return this.unmodifiablePlayers;
   }
}
