package net.minecraft.client.multiplayer;

import com.google.common.base.Strings;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class AccountProfileKeyPairManager implements ProfileKeyPairManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Duration MINIMUM_PROFILE_KEY_REFRESH_INTERVAL = Duration.ofHours(1L);
   private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys");
   private final UserApiService userApiService;
   private final Path profileKeyPairPath;
   private CompletableFuture<Optional<ProfileKeyPair>> keyPair;
   private Instant nextProfileKeyRefreshTime = Instant.EPOCH;

   public AccountProfileKeyPairManager(UserApiService userapiservice, UUID uuid, Path path) {
      this.userApiService = userapiservice;
      this.profileKeyPairPath = path.resolve(PROFILE_KEY_PAIR_DIR).resolve(uuid + ".json");
      this.keyPair = CompletableFuture.supplyAsync(() -> this.readProfileKeyPair().filter((profilekeypair1) -> !profilekeypair1.publicKey().data().hasExpired()), Util.backgroundExecutor()).thenCompose(this::readOrFetchProfileKeyPair);
   }

   public CompletableFuture<Optional<ProfileKeyPair>> prepareKeyPair() {
      this.nextProfileKeyRefreshTime = Instant.now().plus(MINIMUM_PROFILE_KEY_REFRESH_INTERVAL);
      this.keyPair = this.keyPair.thenCompose(this::readOrFetchProfileKeyPair);
      return this.keyPair;
   }

   public boolean shouldRefreshKeyPair() {
      return this.keyPair.isDone() && Instant.now().isAfter(this.nextProfileKeyRefreshTime) ? this.keyPair.join().map(ProfileKeyPair::dueRefresh).orElse(true) : false;
   }

   private CompletableFuture<Optional<ProfileKeyPair>> readOrFetchProfileKeyPair(Optional<ProfileKeyPair> optional) {
      return CompletableFuture.supplyAsync(() -> {
         if (optional.isPresent() && !optional.get().dueRefresh()) {
            if (!SharedConstants.IS_RUNNING_IN_IDE) {
               this.writeProfileKeyPair((ProfileKeyPair)null);
            }

            return optional;
         } else {
            try {
               ProfileKeyPair profilekeypair = this.fetchProfileKeyPair(this.userApiService);
               this.writeProfileKeyPair(profilekeypair);
               return Optional.of(profilekeypair);
            } catch (CryptException | MinecraftClientException | IOException var3) {
               LOGGER.error("Failed to retrieve profile key pair", (Throwable)var3);
               this.writeProfileKeyPair((ProfileKeyPair)null);
               return optional;
            }
         }
      }, Util.backgroundExecutor());
   }

   private Optional<ProfileKeyPair> readProfileKeyPair() {
      if (Files.notExists(this.profileKeyPairPath)) {
         return Optional.empty();
      } else {
         try {
            BufferedReader bufferedreader = Files.newBufferedReader(this.profileKeyPairPath);

            Optional var2;
            try {
               var2 = ProfileKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(bufferedreader)).result();
            } catch (Throwable var5) {
               if (bufferedreader != null) {
                  try {
                     bufferedreader.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (bufferedreader != null) {
               bufferedreader.close();
            }

            return var2;
         } catch (Exception var6) {
            LOGGER.error("Failed to read profile key pair file {}", this.profileKeyPairPath, var6);
            return Optional.empty();
         }
      }
   }

   private void writeProfileKeyPair(@Nullable ProfileKeyPair profilekeypair) {
      try {
         Files.deleteIfExists(this.profileKeyPairPath);
      } catch (IOException var3) {
         LOGGER.error("Failed to delete profile key pair file {}", this.profileKeyPairPath, var3);
      }

      if (profilekeypair != null) {
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            ProfileKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, profilekeypair).result().ifPresent((jsonelement) -> {
               try {
                  Files.createDirectories(this.profileKeyPairPath.getParent());
                  Files.writeString(this.profileKeyPairPath, jsonelement.toString());
               } catch (Exception var3) {
                  LOGGER.error("Failed to write profile key pair file {}", this.profileKeyPairPath, var3);
               }

            });
         }
      }
   }

   private ProfileKeyPair fetchProfileKeyPair(UserApiService userapiservice) throws CryptException, IOException {
      KeyPairResponse keypairresponse = userapiservice.getKeyPair();
      if (keypairresponse != null) {
         ProfilePublicKey.Data profilepublickey_data = parsePublicKey(keypairresponse);
         return new ProfileKeyPair(Crypt.stringToPemRsaPrivateKey(keypairresponse.getPrivateKey()), new ProfilePublicKey(profilepublickey_data), Instant.parse(keypairresponse.getRefreshedAfter()));
      } else {
         throw new IOException("Could not retrieve profile key pair");
      }
   }

   private static ProfilePublicKey.Data parsePublicKey(KeyPairResponse keypairresponse) throws CryptException {
      if (!Strings.isNullOrEmpty(keypairresponse.getPublicKey()) && keypairresponse.getPublicKeySignature() != null && keypairresponse.getPublicKeySignature().array().length != 0) {
         try {
            Instant instant = Instant.parse(keypairresponse.getExpiresAt());
            PublicKey publickey = Crypt.stringToRsaPublicKey(keypairresponse.getPublicKey());
            ByteBuffer bytebuffer = keypairresponse.getPublicKeySignature();
            return new ProfilePublicKey.Data(instant, publickey, bytebuffer.array());
         } catch (IllegalArgumentException | DateTimeException var4) {
            throw new CryptException(var4);
         }
      } else {
         throw new CryptException(new InsecurePublicKeyException.MissingException());
      }
   }
}
