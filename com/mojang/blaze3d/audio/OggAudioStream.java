package com.mojang.blaze3d.audio;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class OggAudioStream implements AudioStream {
   private static final int EXPECTED_MAX_FRAME_SIZE = 8192;
   private long handle;
   private final AudioFormat audioFormat;
   private final InputStream input;
   private ByteBuffer buffer = MemoryUtil.memAlloc(8192);

   public OggAudioStream(InputStream inputstream) throws IOException {
      this.input = inputstream;
      this.buffer.limit(0);
      MemoryStack memorystack = MemoryStack.stackPush();

      try {
         IntBuffer intbuffer = memorystack.mallocInt(1);
         IntBuffer intbuffer1 = memorystack.mallocInt(1);

         while(this.handle == 0L) {
            if (!this.refillFromStream()) {
               throw new IOException("Failed to find Ogg header");
            }

            int i = this.buffer.position();
            this.buffer.position(0);
            this.handle = STBVorbis.stb_vorbis_open_pushdata(this.buffer, intbuffer, intbuffer1, (STBVorbisAlloc)null);
            this.buffer.position(i);
            int j = intbuffer1.get(0);
            if (j == 1) {
               this.forwardBuffer();
            } else if (j != 0) {
               throw new IOException("Failed to read Ogg file " + j);
            }
         }

         this.buffer.position(this.buffer.position() + intbuffer.get(0));
         STBVorbisInfo stbvorbisinfo = STBVorbisInfo.mallocStack(memorystack);
         STBVorbis.stb_vorbis_get_info(this.handle, stbvorbisinfo);
         this.audioFormat = new AudioFormat((float)stbvorbisinfo.sample_rate(), 16, stbvorbisinfo.channels(), true, false);
      } catch (Throwable var8) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (memorystack != null) {
         memorystack.close();
      }

   }

   private boolean refillFromStream() throws IOException {
      int i = this.buffer.limit();
      int j = this.buffer.capacity() - i;
      if (j == 0) {
         return true;
      } else {
         byte[] abyte = new byte[j];
         int k = this.input.read(abyte);
         if (k == -1) {
            return false;
         } else {
            int l = this.buffer.position();
            this.buffer.limit(i + k);
            this.buffer.position(i);
            this.buffer.put(abyte, 0, k);
            this.buffer.position(l);
            return true;
         }
      }
   }

   private void forwardBuffer() {
      boolean flag = this.buffer.position() == 0;
      boolean flag1 = this.buffer.position() == this.buffer.limit();
      if (flag1 && !flag) {
         this.buffer.position(0);
         this.buffer.limit(0);
      } else {
         ByteBuffer bytebuffer = MemoryUtil.memAlloc(flag ? 2 * this.buffer.capacity() : this.buffer.capacity());
         bytebuffer.put(this.buffer);
         MemoryUtil.memFree(this.buffer);
         bytebuffer.flip();
         this.buffer = bytebuffer;
      }

   }

   private boolean readFrame(OggAudioStream.OutputConcat oggaudiostream_outputconcat) throws IOException {
      if (this.handle == 0L) {
         return false;
      } else {
         MemoryStack memorystack = MemoryStack.stackPush();

         int i;
         label79: {
            boolean var15;
            label80: {
               try {
                  PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
                  IntBuffer intbuffer = memorystack.mallocInt(1);
                  IntBuffer intbuffer1 = memorystack.mallocInt(1);

                  while(true) {
                     i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intbuffer, pointerbuffer, intbuffer1);
                     this.buffer.position(this.buffer.position() + i);
                     int j = STBVorbis.stb_vorbis_get_error(this.handle);
                     if (j == 1) {
                        this.forwardBuffer();
                        if (!this.refillFromStream()) {
                           i = 0;
                           break label79;
                        }
                     } else {
                        if (j != 0) {
                           throw new IOException("Failed to read Ogg file " + j);
                        }

                        int k = intbuffer1.get(0);
                        if (k != 0) {
                           int l = intbuffer.get(0);
                           PointerBuffer pointerbuffer1 = pointerbuffer.getPointerBuffer(l);
                           if (l == 1) {
                              this.convertMono(pointerbuffer1.getFloatBuffer(0, k), oggaudiostream_outputconcat);
                              var15 = true;
                              break label80;
                           }

                           if (l != 2) {
                              throw new IllegalStateException("Invalid number of channels: " + l);
                           }

                           this.convertStereo(pointerbuffer1.getFloatBuffer(0, k), pointerbuffer1.getFloatBuffer(1, k), oggaudiostream_outputconcat);
                           var15 = true;
                           break;
                        }
                     }
                  }
               } catch (Throwable var13) {
                  if (memorystack != null) {
                     try {
                        memorystack.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }
                  }

                  throw var13;
               }

               if (memorystack != null) {
                  memorystack.close();
               }

               return var15;
            }

            if (memorystack != null) {
               memorystack.close();
            }

            return var15;
         }

         if (memorystack != null) {
            memorystack.close();
         }

         return (boolean)i;
      }
   }

   private void convertMono(FloatBuffer floatbuffer, OggAudioStream.OutputConcat oggaudiostream_outputconcat) {
      while(floatbuffer.hasRemaining()) {
         oggaudiostream_outputconcat.put(floatbuffer.get());
      }

   }

   private void convertStereo(FloatBuffer floatbuffer, FloatBuffer floatbuffer1, OggAudioStream.OutputConcat oggaudiostream_outputconcat) {
      while(floatbuffer.hasRemaining() && floatbuffer1.hasRemaining()) {
         oggaudiostream_outputconcat.put(floatbuffer.get());
         oggaudiostream_outputconcat.put(floatbuffer1.get());
      }

   }

   public void close() throws IOException {
      if (this.handle != 0L) {
         STBVorbis.stb_vorbis_close(this.handle);
         this.handle = 0L;
      }

      MemoryUtil.memFree(this.buffer);
      this.input.close();
   }

   public AudioFormat getFormat() {
      return this.audioFormat;
   }

   public ByteBuffer read(int i) throws IOException {
      OggAudioStream.OutputConcat oggaudiostream_outputconcat = new OggAudioStream.OutputConcat(i + 8192);

      while(this.readFrame(oggaudiostream_outputconcat) && oggaudiostream_outputconcat.byteCount < i) {
      }

      return oggaudiostream_outputconcat.get();
   }

   public ByteBuffer readAll() throws IOException {
      OggAudioStream.OutputConcat oggaudiostream_outputconcat = new OggAudioStream.OutputConcat(16384);

      while(this.readFrame(oggaudiostream_outputconcat)) {
      }

      return oggaudiostream_outputconcat.get();
   }

   static class OutputConcat {
      private final List<ByteBuffer> buffers = Lists.newArrayList();
      private final int bufferSize;
      int byteCount;
      private ByteBuffer currentBuffer;

      public OutputConcat(int i) {
         this.bufferSize = i + 1 & -2;
         this.createNewBuffer();
      }

      private void createNewBuffer() {
         this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
      }

      public void put(float f) {
         if (this.currentBuffer.remaining() == 0) {
            this.currentBuffer.flip();
            this.buffers.add(this.currentBuffer);
            this.createNewBuffer();
         }

         int i = Mth.clamp((int)(f * 32767.5F - 0.5F), -32768, 32767);
         this.currentBuffer.putShort((short)i);
         this.byteCount += 2;
      }

      public ByteBuffer get() {
         this.currentBuffer.flip();
         if (this.buffers.isEmpty()) {
            return this.currentBuffer;
         } else {
            ByteBuffer bytebuffer = BufferUtils.createByteBuffer(this.byteCount);
            this.buffers.forEach(bytebuffer::put);
            bytebuffer.put(this.currentBuffer);
            bytebuffer.flip();
            return bytebuffer;
         }
      }
   }
}
