package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

public class ServerList {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ProcessorMailbox<Runnable> IO_MAILBOX = ProcessorMailbox.create(Util.backgroundExecutor(), "server-list-io");
   private static final int MAX_HIDDEN_SERVERS = 16;
   private final Minecraft minecraft;
   private final List<ServerData> serverList = Lists.newArrayList();
   private final List<ServerData> hiddenServerList = Lists.newArrayList();

   public ServerList(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void load() {
      try {
         this.serverList.clear();
         this.hiddenServerList.clear();
         CompoundTag compoundtag = NbtIo.read(new File(this.minecraft.gameDirectory, "servers.dat"));
         if (compoundtag == null) {
            return;
         }

         ListTag listtag = compoundtag.getList("servers", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            ServerData serverdata = ServerData.read(compoundtag1);
            if (compoundtag1.getBoolean("hidden")) {
               this.hiddenServerList.add(serverdata);
            } else {
               this.serverList.add(serverdata);
            }
         }
      } catch (Exception var6) {
         LOGGER.error("Couldn't load server list", (Throwable)var6);
      }

   }

   public void save() {
      try {
         ListTag listtag = new ListTag();

         for(ServerData serverdata : this.serverList) {
            CompoundTag compoundtag = serverdata.write();
            compoundtag.putBoolean("hidden", false);
            listtag.add(compoundtag);
         }

         for(ServerData serverdata1 : this.hiddenServerList) {
            CompoundTag compoundtag1 = serverdata1.write();
            compoundtag1.putBoolean("hidden", true);
            listtag.add(compoundtag1);
         }

         CompoundTag compoundtag2 = new CompoundTag();
         compoundtag2.put("servers", listtag);
         File file = File.createTempFile("servers", ".dat", this.minecraft.gameDirectory);
         NbtIo.write(compoundtag2, file);
         File file1 = new File(this.minecraft.gameDirectory, "servers.dat_old");
         File file2 = new File(this.minecraft.gameDirectory, "servers.dat");
         Util.safeReplaceFile(file2, file, file1);
      } catch (Exception var6) {
         LOGGER.error("Couldn't save server list", (Throwable)var6);
      }

   }

   public ServerData get(int i) {
      return this.serverList.get(i);
   }

   @Nullable
   public ServerData get(String s) {
      for(ServerData serverdata : this.serverList) {
         if (serverdata.ip.equals(s)) {
            return serverdata;
         }
      }

      for(ServerData serverdata1 : this.hiddenServerList) {
         if (serverdata1.ip.equals(s)) {
            return serverdata1;
         }
      }

      return null;
   }

   @Nullable
   public ServerData unhide(String s) {
      for(int i = 0; i < this.hiddenServerList.size(); ++i) {
         ServerData serverdata = this.hiddenServerList.get(i);
         if (serverdata.ip.equals(s)) {
            this.hiddenServerList.remove(i);
            this.serverList.add(serverdata);
            return serverdata;
         }
      }

      return null;
   }

   public void remove(ServerData serverdata) {
      if (!this.serverList.remove(serverdata)) {
         this.hiddenServerList.remove(serverdata);
      }

   }

   public void add(ServerData serverdata, boolean flag) {
      if (flag) {
         this.hiddenServerList.add(0, serverdata);

         while(this.hiddenServerList.size() > 16) {
            this.hiddenServerList.remove(this.hiddenServerList.size() - 1);
         }
      } else {
         this.serverList.add(serverdata);
      }

   }

   public int size() {
      return this.serverList.size();
   }

   public void swap(int i, int j) {
      ServerData serverdata = this.get(i);
      this.serverList.set(i, this.get(j));
      this.serverList.set(j, serverdata);
      this.save();
   }

   public void replace(int i, ServerData serverdata) {
      this.serverList.set(i, serverdata);
   }

   private static boolean set(ServerData serverdata, List<ServerData> list) {
      for(int i = 0; i < list.size(); ++i) {
         ServerData serverdata1 = list.get(i);
         if (serverdata1.name.equals(serverdata.name) && serverdata1.ip.equals(serverdata.ip)) {
            list.set(i, serverdata);
            return true;
         }
      }

      return false;
   }

   public static void saveSingleServer(ServerData serverdata) {
      IO_MAILBOX.tell(() -> {
         ServerList serverlist = new ServerList(Minecraft.getInstance());
         serverlist.load();
         if (!set(serverdata, serverlist.serverList)) {
            set(serverdata, serverlist.hiddenServerList);
         }

         serverlist.save();
      });
   }
}
