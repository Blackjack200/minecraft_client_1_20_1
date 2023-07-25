package net.minecraft.commands;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext {
   <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourcekey);

   static CommandBuildContext simple(final HolderLookup.Provider holderlookup_provider, final FeatureFlagSet featureflagset) {
      return new CommandBuildContext() {
         public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourcekey) {
            return holderlookup_provider.<T>lookupOrThrow(resourcekey).filterFeatures(featureflagset);
         }
      };
   }

   static CommandBuildContext.Configurable configurable(final RegistryAccess registryaccess, final FeatureFlagSet featureflagset) {
      return new CommandBuildContext.Configurable() {
         CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

         public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy commandbuildcontext_missingtagaccesspolicy) {
            this.missingTagAccessPolicy = commandbuildcontext_missingtagaccesspolicy;
         }

         public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourcekey) {
            Registry<T> registry = registryaccess.registryOrThrow(resourcekey);
            final HolderLookup.RegistryLookup<T> holderlookup_registrylookup = registry.asLookup();
            final HolderLookup.RegistryLookup<T> holderlookup_registrylookup1 = registry.asTagAddingLookup();
            HolderLookup.RegistryLookup<T> holderlookup_registrylookup2 = new HolderLookup.RegistryLookup.Delegate<T>() {
               protected HolderLookup.RegistryLookup<T> parent() {
                  HolderLookup.RegistryLookup var10000;
                  switch (missingTagAccessPolicy) {
                     case FAIL:
                        var10000 = holderlookup_registrylookup;
                        break;
                     case CREATE_NEW:
                        var10000 = holderlookup_registrylookup1;
                        break;
                     default:
                        throw new IncompatibleClassChangeError();
                  }

                  return var10000;
               }
            };
            return holderlookup_registrylookup2.filterFeatures(featureflagset);
         }
      };
   }

   public interface Configurable extends CommandBuildContext {
      void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy commandbuildcontext_missingtagaccesspolicy);
   }

   public static enum MissingTagAccessPolicy {
      CREATE_NEW,
      FAIL;
   }
}
