package net.minecraft.client.resources.metadata.texture;

public class TextureMetadataSection {
   public static final TextureMetadataSectionSerializer SERIALIZER = new TextureMetadataSectionSerializer();
   public static final boolean DEFAULT_BLUR = false;
   public static final boolean DEFAULT_CLAMP = false;
   private final boolean blur;
   private final boolean clamp;

   public TextureMetadataSection(boolean flag, boolean flag1) {
      this.blur = flag;
      this.clamp = flag1;
   }

   public boolean isBlur() {
      return this.blur;
   }

   public boolean isClamp() {
      return this.clamp;
   }
}
