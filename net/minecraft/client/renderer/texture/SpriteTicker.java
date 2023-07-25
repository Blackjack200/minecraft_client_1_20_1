package net.minecraft.client.renderer.texture;

public interface SpriteTicker extends AutoCloseable {
   void tickAndUpload(int i, int j);

   void close();
}
