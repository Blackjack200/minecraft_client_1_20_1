package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ClientAdvancements {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final WorldSessionTelemetryManager telemetryManager;
   private final AdvancementList advancements = new AdvancementList();
   private final Map<Advancement, AdvancementProgress> progress = Maps.newHashMap();
   @Nullable
   private ClientAdvancements.Listener listener;
   @Nullable
   private Advancement selectedTab;

   public ClientAdvancements(Minecraft minecraft, WorldSessionTelemetryManager worldsessiontelemetrymanager) {
      this.minecraft = minecraft;
      this.telemetryManager = worldsessiontelemetrymanager;
   }

   public void update(ClientboundUpdateAdvancementsPacket clientboundupdateadvancementspacket) {
      if (clientboundupdateadvancementspacket.shouldReset()) {
         this.advancements.clear();
         this.progress.clear();
      }

      this.advancements.remove(clientboundupdateadvancementspacket.getRemoved());
      this.advancements.add(clientboundupdateadvancementspacket.getAdded());

      for(Map.Entry<ResourceLocation, AdvancementProgress> map_entry : clientboundupdateadvancementspacket.getProgress().entrySet()) {
         Advancement advancement = this.advancements.get(map_entry.getKey());
         if (advancement != null) {
            AdvancementProgress advancementprogress = map_entry.getValue();
            advancementprogress.update(advancement.getCriteria(), advancement.getRequirements());
            this.progress.put(advancement, advancementprogress);
            if (this.listener != null) {
               this.listener.onUpdateAdvancementProgress(advancement, advancementprogress);
            }

            if (!clientboundupdateadvancementspacket.shouldReset() && advancementprogress.isDone()) {
               if (this.minecraft.level != null) {
                  this.telemetryManager.onAdvancementDone(this.minecraft.level, advancement);
               }

               if (advancement.getDisplay() != null && advancement.getDisplay().shouldShowToast()) {
                  this.minecraft.getToasts().addToast(new AdvancementToast(advancement));
               }
            }
         } else {
            LOGGER.warn("Server informed client about progress for unknown advancement {}", map_entry.getKey());
         }
      }

   }

   public AdvancementList getAdvancements() {
      return this.advancements;
   }

   public void setSelectedTab(@Nullable Advancement advancement, boolean flag) {
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null && advancement != null && flag) {
         clientpacketlistener.send(ServerboundSeenAdvancementsPacket.openedTab(advancement));
      }

      if (this.selectedTab != advancement) {
         this.selectedTab = advancement;
         if (this.listener != null) {
            this.listener.onSelectedTabChanged(advancement);
         }
      }

   }

   public void setListener(@Nullable ClientAdvancements.Listener clientadvancements_listener) {
      this.listener = clientadvancements_listener;
      this.advancements.setListener(clientadvancements_listener);
      if (clientadvancements_listener != null) {
         for(Map.Entry<Advancement, AdvancementProgress> map_entry : this.progress.entrySet()) {
            clientadvancements_listener.onUpdateAdvancementProgress(map_entry.getKey(), map_entry.getValue());
         }

         clientadvancements_listener.onSelectedTabChanged(this.selectedTab);
      }

   }

   public interface Listener extends AdvancementList.Listener {
      void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementprogress);

      void onSelectedTabChanged(@Nullable Advancement advancement);
   }
}
