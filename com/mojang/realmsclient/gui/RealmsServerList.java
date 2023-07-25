package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;

public class RealmsServerList {
   private final Minecraft minecraft;
   private final Set<RealmsServer> removedServers = Sets.newHashSet();
   private List<RealmsServer> servers = Lists.newArrayList();

   public RealmsServerList(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public List<RealmsServer> updateServersList(List<RealmsServer> list) {
      List<RealmsServer> list1 = new ArrayList<>(list);
      list1.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
      boolean flag = list1.removeAll(this.removedServers);
      if (!flag) {
         this.removedServers.clear();
      }

      this.servers = list1;
      return List.copyOf(this.servers);
   }

   public synchronized List<RealmsServer> removeItem(RealmsServer realmsserver) {
      this.servers.remove(realmsserver);
      this.removedServers.add(realmsserver);
      return List.copyOf(this.servers);
   }
}
