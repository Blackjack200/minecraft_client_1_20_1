package net.minecraft.client.multiplayer.resolver;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;

public class ServerNameResolver {
   public static final ServerNameResolver DEFAULT = new ServerNameResolver(ServerAddressResolver.SYSTEM, ServerRedirectHandler.createDnsSrvRedirectHandler(), AddressCheck.createFromService());
   private final ServerAddressResolver resolver;
   private final ServerRedirectHandler redirectHandler;
   private final AddressCheck addressCheck;

   @VisibleForTesting
   ServerNameResolver(ServerAddressResolver serveraddressresolver, ServerRedirectHandler serverredirecthandler, AddressCheck addresscheck) {
      this.resolver = serveraddressresolver;
      this.redirectHandler = serverredirecthandler;
      this.addressCheck = addresscheck;
   }

   public Optional<ResolvedServerAddress> resolveAddress(ServerAddress serveraddress) {
      Optional<ResolvedServerAddress> optional = this.resolver.resolve(serveraddress);
      if ((!optional.isPresent() || this.addressCheck.isAllowed(optional.get())) && this.addressCheck.isAllowed(serveraddress)) {
         Optional<ServerAddress> optional1 = this.redirectHandler.lookupRedirect(serveraddress);
         if (optional1.isPresent()) {
            optional = this.resolver.resolve(optional1.get()).filter(this.addressCheck::isAllowed);
         }

         return optional;
      } else {
         return Optional.empty();
      }
   }
}
