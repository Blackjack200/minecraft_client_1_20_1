package net.minecraft.client.multiplayer.resolver;

import com.mojang.logging.LogUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import org.slf4j.Logger;

@FunctionalInterface
public interface ServerAddressResolver {
   Logger LOGGER = LogUtils.getLogger();
   ServerAddressResolver SYSTEM = (serveraddress) -> {
      try {
         InetAddress inetaddress = InetAddress.getByName(serveraddress.getHost());
         return Optional.of(ResolvedServerAddress.from(new InetSocketAddress(inetaddress, serveraddress.getPort())));
      } catch (UnknownHostException var2) {
         LOGGER.debug("Couldn't resolve server {} address", serveraddress.getHost(), var2);
         return Optional.empty();
      }
   };

   Optional<ResolvedServerAddress> resolve(ServerAddress serveraddress);
}
