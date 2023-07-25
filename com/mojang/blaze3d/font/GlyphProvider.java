package com.mojang.blaze3d.font;

import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nullable;

public interface GlyphProvider extends AutoCloseable {
   default void close() {
   }

   @Nullable
   default GlyphInfo getGlyph(int i) {
      return null;
   }

   IntSet getSupportedGlyphs();
}
