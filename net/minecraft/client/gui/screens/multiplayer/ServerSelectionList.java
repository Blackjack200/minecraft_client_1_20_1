package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

public class ServerSelectionList extends ObjectSelectionList<ServerSelectionList.Entry> {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).build());
   private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
   static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
   static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
   static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
   static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withStyle((style) -> style.withColor(-65536));
   static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withStyle((style) -> style.withColor(-65536));
   static final Component INCOMPATIBLE_STATUS = Component.translatable("multiplayer.status.incompatible");
   static final Component NO_CONNECTION_STATUS = Component.translatable("multiplayer.status.no_connection");
   static final Component PINGING_STATUS = Component.translatable("multiplayer.status.pinging");
   static final Component ONLINE_STATUS = Component.translatable("multiplayer.status.online");
   private final JoinMultiplayerScreen screen;
   private final List<ServerSelectionList.OnlineServerEntry> onlineServers = Lists.newArrayList();
   private final ServerSelectionList.Entry lanHeader = new ServerSelectionList.LANHeader();
   private final List<ServerSelectionList.NetworkServerEntry> networkServers = Lists.newArrayList();

   public ServerSelectionList(JoinMultiplayerScreen joinmultiplayerscreen, Minecraft minecraft, int i, int j, int k, int l, int i1) {
      super(minecraft, i, j, k, l, i1);
      this.screen = joinmultiplayerscreen;
   }

   private void refreshEntries() {
      this.clearEntries();
      this.onlineServers.forEach((abstractselectionlist_entry1) -> this.addEntry(abstractselectionlist_entry1));
      this.addEntry(this.lanHeader);
      this.networkServers.forEach((abstractselectionlist_entry) -> this.addEntry(abstractselectionlist_entry));
   }

   public void setSelected(@Nullable ServerSelectionList.Entry serverselectionlist_entry) {
      super.setSelected(serverselectionlist_entry);
      this.screen.onSelectedChange();
   }

   public boolean keyPressed(int i, int j, int k) {
      ServerSelectionList.Entry serverselectionlist_entry = this.getSelected();
      return serverselectionlist_entry != null && serverselectionlist_entry.keyPressed(i, j, k) || super.keyPressed(i, j, k);
   }

   public void updateOnlineServers(ServerList serverlist) {
      this.onlineServers.clear();

      for(int i = 0; i < serverlist.size(); ++i) {
         this.onlineServers.add(new ServerSelectionList.OnlineServerEntry(this.screen, serverlist.get(i)));
      }

      this.refreshEntries();
   }

   public void updateNetworkServers(List<LanServer> list) {
      int i = list.size() - this.networkServers.size();
      this.networkServers.clear();

      for(LanServer lanserver : list) {
         this.networkServers.add(new ServerSelectionList.NetworkServerEntry(this.screen, lanserver));
      }

      this.refreshEntries();

      for(int j = this.networkServers.size() - i; j < this.networkServers.size(); ++j) {
         ServerSelectionList.NetworkServerEntry serverselectionlist_networkserverentry = this.networkServers.get(j);
         int k = j - this.networkServers.size() + this.children().size();
         int l = this.getRowTop(k);
         int i1 = this.getRowBottom(k);
         if (i1 >= this.y0 && l <= this.y1) {
            this.minecraft.getNarrator().say(Component.translatable("multiplayer.lan.server_found", serverselectionlist_networkserverentry.getServerNarration()));
         }
      }

   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 30;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 85;
   }

   public void removed() {
   }

   public abstract static class Entry extends ObjectSelectionList.Entry<ServerSelectionList.Entry> implements AutoCloseable {
      public void close() {
      }
   }

   public static class LANHeader extends ServerSelectionList.Entry {
      private final Minecraft minecraft = Minecraft.getInstance();

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         int l1 = j + i1 / 2 - 9 / 2;
         guigraphics.drawString(this.minecraft.font, ServerSelectionList.SCANNING_LABEL, this.minecraft.screen.width / 2 - this.minecraft.font.width(ServerSelectionList.SCANNING_LABEL) / 2, l1, 16777215, false);
         String s = LoadingDotsText.get(Util.getMillis());
         guigraphics.drawString(this.minecraft.font, s, this.minecraft.screen.width / 2 - this.minecraft.font.width(s) / 2, l1 + 9, 8421504, false);
      }

      public Component getNarration() {
         return ServerSelectionList.SCANNING_LABEL;
      }
   }

   public static class NetworkServerEntry extends ServerSelectionList.Entry {
      private static final int ICON_WIDTH = 32;
      private static final Component LAN_SERVER_HEADER = Component.translatable("lanServer.title");
      private static final Component HIDDEN_ADDRESS_TEXT = Component.translatable("selectServer.hiddenAddress");
      private final JoinMultiplayerScreen screen;
      protected final Minecraft minecraft;
      protected final LanServer serverData;
      private long lastClickTime;

      protected NetworkServerEntry(JoinMultiplayerScreen joinmultiplayerscreen, LanServer lanserver) {
         this.screen = joinmultiplayerscreen;
         this.serverData = lanserver;
         this.minecraft = Minecraft.getInstance();
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         guigraphics.drawString(this.minecraft.font, LAN_SERVER_HEADER, k + 32 + 3, j + 1, 16777215, false);
         guigraphics.drawString(this.minecraft.font, this.serverData.getMotd(), k + 32 + 3, j + 12, 8421504, false);
         if (this.minecraft.options.hideServerAddress) {
            guigraphics.drawString(this.minecraft.font, HIDDEN_ADDRESS_TEXT, k + 32 + 3, j + 12 + 11, 3158064, false);
         } else {
            guigraphics.drawString(this.minecraft.font, this.serverData.getAddress(), k + 32 + 3, j + 12 + 11, 3158064, false);
         }

      }

      public boolean mouseClicked(double d0, double d1, int i) {
         this.screen.setSelected(this);
         if (Util.getMillis() - this.lastClickTime < 250L) {
            this.screen.joinSelectedServer();
         }

         this.lastClickTime = Util.getMillis();
         return false;
      }

      public LanServer getServerData() {
         return this.serverData;
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.getServerNarration());
      }

      public Component getServerNarration() {
         return Component.empty().append(LAN_SERVER_HEADER).append(CommonComponents.SPACE).append(this.serverData.getMotd());
      }
   }

   public class OnlineServerEntry extends ServerSelectionList.Entry {
      private static final int ICON_WIDTH = 32;
      private static final int ICON_HEIGHT = 32;
      private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
      private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
      private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
      private static final int ICON_OVERLAY_X_MOVE_UP = 96;
      private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
      private static final int ICON_OVERLAY_Y_SELECTED = 32;
      private final JoinMultiplayerScreen screen;
      private final Minecraft minecraft;
      private final ServerData serverData;
      private final FaviconTexture icon;
      @Nullable
      private byte[] lastIconBytes;
      private long lastClickTime;

      protected OnlineServerEntry(JoinMultiplayerScreen joinmultiplayerscreen, ServerData serverdata) {
         this.screen = joinmultiplayerscreen;
         this.serverData = serverdata;
         this.minecraft = Minecraft.getInstance();
         this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), serverdata.ip);
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         if (!this.serverData.pinged) {
            this.serverData.pinged = true;
            this.serverData.ping = -2L;
            this.serverData.motd = CommonComponents.EMPTY;
            this.serverData.status = CommonComponents.EMPTY;
            ServerSelectionList.THREAD_POOL.submit(() -> {
               try {
                  this.screen.getPinger().pingServer(this.serverData, () -> this.minecraft.execute(this::updateServerList));
               } catch (UnknownHostException var2) {
                  this.serverData.ping = -1L;
                  this.serverData.motd = ServerSelectionList.CANT_RESOLVE_TEXT;
               } catch (Exception var3) {
                  this.serverData.ping = -1L;
                  this.serverData.motd = ServerSelectionList.CANT_CONNECT_TEXT;
               }

            });
         }

         boolean flag1 = !this.isCompatible();
         guigraphics.drawString(this.minecraft.font, this.serverData.name, k + 32 + 3, j + 1, 16777215, false);
         List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, l - 32 - 2);

         for(int l1 = 0; l1 < Math.min(list.size(), 2); ++l1) {
            guigraphics.drawString(this.minecraft.font, list.get(l1), k + 32 + 3, j + 12 + 9 * l1, 8421504, false);
         }

         Component component = (Component)(flag1 ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status);
         int i2 = this.minecraft.font.width(component);
         guigraphics.drawString(this.minecraft.font, component, k + l - i2 - 15 - 2, j + 1, 8421504, false);
         int j2 = 0;
         int k2;
         List<Component> list1;
         Component component1;
         if (flag1) {
            k2 = 5;
            component1 = ServerSelectionList.INCOMPATIBLE_STATUS;
            list1 = this.serverData.playerList;
         } else if (this.pingCompleted()) {
            if (this.serverData.ping < 0L) {
               k2 = 5;
            } else if (this.serverData.ping < 150L) {
               k2 = 0;
            } else if (this.serverData.ping < 300L) {
               k2 = 1;
            } else if (this.serverData.ping < 600L) {
               k2 = 2;
            } else if (this.serverData.ping < 1000L) {
               k2 = 3;
            } else {
               k2 = 4;
            }

            if (this.serverData.ping < 0L) {
               component1 = ServerSelectionList.NO_CONNECTION_STATUS;
               list1 = Collections.emptyList();
            } else {
               component1 = Component.translatable("multiplayer.status.ping", this.serverData.ping);
               list1 = this.serverData.playerList;
            }
         } else {
            j2 = 1;
            k2 = (int)(Util.getMillis() / 100L + (long)(i * 2) & 7L);
            if (k2 > 4) {
               k2 = 8 - k2;
            }

            component1 = ServerSelectionList.PINGING_STATUS;
            list1 = Collections.emptyList();
         }

         guigraphics.blit(ServerSelectionList.GUI_ICONS_LOCATION, k + l - 15, j, (float)(j2 * 10), (float)(176 + k2 * 8), 10, 8, 256, 256);
         byte[] abyte = this.serverData.getIconBytes();
         if (!Arrays.equals(abyte, this.lastIconBytes)) {
            if (this.uploadServerIcon(abyte)) {
               this.lastIconBytes = abyte;
            } else {
               this.serverData.setIconBytes((byte[])null);
               this.updateServerList();
            }
         }

         this.drawIcon(guigraphics, k, j, this.icon.textureLocation());
         int k4 = j1 - k;
         int l4 = k1 - j;
         if (k4 >= l - 15 && k4 <= l - 5 && l4 >= 0 && l4 <= 8) {
            this.screen.setToolTip(Collections.singletonList(component1));
         } else if (k4 >= l - i2 - 15 - 2 && k4 <= l - 15 - 2 && l4 >= 0 && l4 <= 8) {
            this.screen.setToolTip(list1);
         }

         if (this.minecraft.options.touchscreen().get() || flag) {
            guigraphics.fill(k, j, k + 32, j + 32, -1601138544);
            int i5 = j1 - k;
            int j5 = k1 - j;
            if (this.canJoin()) {
               if (i5 < 32 && i5 > 16) {
                  guigraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  guigraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (i > 0) {
               if (i5 < 16 && j5 < 16) {
                  guigraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  guigraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (i < this.screen.getServers().size() - 1) {
               if (i5 < 16 && j5 > 16) {
                  guigraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  guigraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, 0.0F, 32, 32, 256, 256);
               }
            }
         }

      }

      private boolean pingCompleted() {
         return this.serverData.pinged && this.serverData.ping != -2L;
      }

      private boolean isCompatible() {
         return this.serverData.protocol == SharedConstants.getCurrentVersion().getProtocolVersion();
      }

      public void updateServerList() {
         this.screen.getServers().save();
      }

      protected void drawIcon(GuiGraphics guigraphics, int i, int j, ResourceLocation resourcelocation) {
         RenderSystem.enableBlend();
         guigraphics.blit(resourcelocation, i, j, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
      }

      private boolean canJoin() {
         return true;
      }

      private boolean uploadServerIcon(@Nullable byte[] abyte) {
         if (abyte == null) {
            this.icon.clear();
         } else {
            try {
               this.icon.upload(NativeImage.read(abyte));
            } catch (Throwable var3) {
               ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
               return false;
            }
         }

         return true;
      }

      public boolean keyPressed(int i, int j, int k) {
         if (Screen.hasShiftDown()) {
            ServerSelectionList serverselectionlist = this.screen.serverSelectionList;
            int l = serverselectionlist.children().indexOf(this);
            if (l == -1) {
               return true;
            }

            if (i == 264 && l < this.screen.getServers().size() - 1 || i == 265 && l > 0) {
               this.swap(l, i == 264 ? l + 1 : l - 1);
               return true;
            }
         }

         return super.keyPressed(i, j, k);
      }

      private void swap(int i, int j) {
         this.screen.getServers().swap(i, j);
         this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
         ServerSelectionList.Entry serverselectionlist_entry = this.screen.serverSelectionList.children().get(j);
         this.screen.serverSelectionList.setSelected(serverselectionlist_entry);
         ServerSelectionList.this.ensureVisible(serverselectionlist_entry);
      }

      public boolean mouseClicked(double d0, double d1, int i) {
         double d2 = d0 - (double)ServerSelectionList.this.getRowLeft();
         double d3 = d1 - (double)ServerSelectionList.this.getRowTop(ServerSelectionList.this.children().indexOf(this));
         if (d2 <= 32.0D) {
            if (d2 < 32.0D && d2 > 16.0D && this.canJoin()) {
               this.screen.setSelected(this);
               this.screen.joinSelectedServer();
               return true;
            }

            int j = this.screen.serverSelectionList.children().indexOf(this);
            if (d2 < 16.0D && d3 < 16.0D && j > 0) {
               this.swap(j, j - 1);
               return true;
            }

            if (d2 < 16.0D && d3 > 16.0D && j < this.screen.getServers().size() - 1) {
               this.swap(j, j + 1);
               return true;
            }
         }

         this.screen.setSelected(this);
         if (Util.getMillis() - this.lastClickTime < 250L) {
            this.screen.joinSelectedServer();
         }

         this.lastClickTime = Util.getMillis();
         return true;
      }

      public ServerData getServerData() {
         return this.serverData;
      }

      public Component getNarration() {
         MutableComponent mutablecomponent = Component.empty();
         mutablecomponent.append(Component.translatable("narrator.select", this.serverData.name));
         mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
         if (!this.isCompatible()) {
            mutablecomponent.append(ServerSelectionList.INCOMPATIBLE_STATUS);
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.version.narration", this.serverData.version));
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
         } else if (this.serverData.ping < 0L) {
            mutablecomponent.append(ServerSelectionList.NO_CONNECTION_STATUS);
         } else if (!this.pingCompleted()) {
            mutablecomponent.append(ServerSelectionList.PINGING_STATUS);
         } else {
            mutablecomponent.append(ServerSelectionList.ONLINE_STATUS);
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.ping.narration", this.serverData.ping));
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
            if (this.serverData.players != null) {
               mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
               mutablecomponent.append(Component.translatable("multiplayer.status.player_count.narration", this.serverData.players.online(), this.serverData.players.max()));
               mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
               mutablecomponent.append(ComponentUtils.formatList(this.serverData.playerList, Component.literal(", ")));
            }
         }

         return mutablecomponent;
      }

      public void close() {
         this.icon.close();
      }
   }
}
