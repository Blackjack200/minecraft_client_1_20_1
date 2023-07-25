package net.minecraft.client.multiplayer.resolver;

import java.net.InetSocketAddress;

public interface ResolvedServerAddress {
   String getHostName();

   String getHostIp();

   int getPort();

   InetSocketAddress asInetSocketAddress();

   static ResolvedServerAddress from(final InetSocketAddress inetsocketaddress) {
      return new ResolvedServerAddress() {
         public String getHostName() {
            return inetsocketaddress.getAddress().getHostName();
         }

         public String getHostIp() {
            return inetsocketaddress.getAddress().getHostAddress();
         }

         public int getPort() {
            return inetsocketaddress.getPort();
         }

         public InetSocketAddress asInetSocketAddress() {
            return inetsocketaddress;
         }
      };
   }
}
