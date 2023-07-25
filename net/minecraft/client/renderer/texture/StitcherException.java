package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Locale;

public class StitcherException extends RuntimeException {
   private final Collection<Stitcher.Entry> allSprites;

   public StitcherException(Stitcher.Entry stitcher_entry, Collection<Stitcher.Entry> collection) {
      super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", stitcher_entry.name(), stitcher_entry.width(), stitcher_entry.height()));
      this.allSprites = collection;
   }

   public Collection<Stitcher.Entry> getAllSprites() {
      return this.allSprites;
   }
}
