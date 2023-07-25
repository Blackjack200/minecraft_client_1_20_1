package net.minecraft.client.multiplayer.resolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.blocklist.BlockListSupplier;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public interface AddressCheck {
   boolean isAllowed(ResolvedServerAddress resolvedserveraddress);

   boolean isAllowed(ServerAddress serveraddress);

   static AddressCheck createFromService() {
      final ImmutableList<Predicate<String>> immutablelist = Streams.stream(ServiceLoader.load(BlockListSupplier.class)).map(BlockListSupplier::createBlockList).filter(Objects::nonNull).collect(ImmutableList.toImmutableList());
      return new AddressCheck() {
         public boolean isAllowed(ResolvedServerAddress resolvedserveraddress) {
            String s = resolvedserveraddress.getHostName();
            String s1 = resolvedserveraddress.getHostIp();
            return immutablelist.stream().noneMatch((predicate) -> predicate.test(s) || predicate.test(s1));
         }

         public boolean isAllowed(ServerAddress serveraddress) {
            String s = serveraddress.getHost();
            return immutablelist.stream().noneMatch((predicate) -> predicate.test(s));
         }
      };
   }
}
