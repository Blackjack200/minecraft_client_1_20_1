package net.minecraft.client.sounds;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;

public class LoopingAudioStream implements AudioStream {
   private final LoopingAudioStream.AudioStreamProvider provider;
   private AudioStream stream;
   private final BufferedInputStream bufferedInputStream;

   public LoopingAudioStream(LoopingAudioStream.AudioStreamProvider loopingaudiostream_audiostreamprovider, InputStream inputstream) throws IOException {
      this.provider = loopingaudiostream_audiostreamprovider;
      this.bufferedInputStream = new BufferedInputStream(inputstream);
      this.bufferedInputStream.mark(Integer.MAX_VALUE);
      this.stream = loopingaudiostream_audiostreamprovider.create(new LoopingAudioStream.NoCloseBuffer(this.bufferedInputStream));
   }

   public AudioFormat getFormat() {
      return this.stream.getFormat();
   }

   public ByteBuffer read(int i) throws IOException {
      ByteBuffer bytebuffer = this.stream.read(i);
      if (!bytebuffer.hasRemaining()) {
         this.stream.close();
         this.bufferedInputStream.reset();
         this.stream = this.provider.create(new LoopingAudioStream.NoCloseBuffer(this.bufferedInputStream));
         bytebuffer = this.stream.read(i);
      }

      return bytebuffer;
   }

   public void close() throws IOException {
      this.stream.close();
      this.bufferedInputStream.close();
   }

   @FunctionalInterface
   public interface AudioStreamProvider {
      AudioStream create(InputStream inputstream) throws IOException;
   }

   static class NoCloseBuffer extends FilterInputStream {
      NoCloseBuffer(InputStream inputstream) {
         super(inputstream);
      }

      public void close() {
      }
   }
}
