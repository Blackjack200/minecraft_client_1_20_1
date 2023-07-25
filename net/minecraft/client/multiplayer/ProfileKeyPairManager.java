package net.minecraft.client.multiplayer;

import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.User;
import net.minecraft.world.entity.player.ProfileKeyPair;

public interface ProfileKeyPairManager {
   ProfileKeyPairManager EMPTY_KEY_MANAGER = new ProfileKeyPairManager() {
      public CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair() {
         return CompletableFuture.completedFuture(Optional.empty());
      }

      public boolean shouldRefreshKeyPair() {
         return false;
      }
   };

   static ProfileKeyPairManager create(UserApiService userapiservice, User user, Path path) {
      return (ProfileKeyPairManager)(user.getType() == User.Type.MSA ? new AccountProfileKeyPairManager(userapiservice, user.getGameProfile().getId(), path) : EMPTY_KEY_MANAGER);
   }

   CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair();

   boolean shouldRefreshKeyPair();
}
