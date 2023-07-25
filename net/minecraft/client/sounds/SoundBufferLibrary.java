package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

public class SoundBufferLibrary {
   private final ResourceProvider resourceManager;
   private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

   public SoundBufferLibrary(ResourceProvider resourceprovider) {
      this.resourceManager = resourceprovider;
   }

   public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation resourcelocation) {
      return this.cache.computeIfAbsent(resourcelocation, (resourcelocation1) -> CompletableFuture.supplyAsync(() -> {
            try {
               InputStream inputstream = this.resourceManager.open(resourcelocation1);

               SoundBuffer var5;
               try {
                  OggAudioStream oggaudiostream = new OggAudioStream(inputstream);

                  try {
                     ByteBuffer bytebuffer = oggaudiostream.readAll();
                     var5 = new SoundBuffer(bytebuffer, oggaudiostream.getFormat());
                  } catch (Throwable var8) {
                     try {
                        oggaudiostream.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }

                     throw var8;
                  }

                  oggaudiostream.close();
               } catch (Throwable var9) {
                  if (inputstream != null) {
                     try {
                        inputstream.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (inputstream != null) {
                  inputstream.close();
               }

               return var5;
            } catch (IOException var10) {
               throw new CompletionException(var10);
            }
         }, Util.backgroundExecutor()));
   }

   public CompletableFuture<AudioStream> getStream(ResourceLocation resourcelocation, boolean flag) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            InputStream inputstream = this.resourceManager.open(resourcelocation);
            return (AudioStream)(flag ? new LoopingAudioStream(OggAudioStream::new, inputstream) : new OggAudioStream(inputstream));
         } catch (IOException var4) {
            throw new CompletionException(var4);
         }
      }, Util.backgroundExecutor());
   }

   public void clear() {
      this.cache.values().forEach((completablefuture) -> completablefuture.thenAccept(SoundBuffer::discardAlBuffer));
      this.cache.clear();
   }

   public CompletableFuture<?> preload(Collection<Sound> collection) {
      return CompletableFuture.allOf(collection.stream().map((sound) -> this.getCompleteBuffer(sound.getPath())).toArray((i) -> new CompletableFuture[i]));
   }
}
