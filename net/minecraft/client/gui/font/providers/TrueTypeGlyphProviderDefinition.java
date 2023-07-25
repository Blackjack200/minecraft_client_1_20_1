package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

public record TrueTypeGlyphProviderDefinition(ResourceLocation location, float size, float oversample, TrueTypeGlyphProviderDefinition.Shift shift, String skip) implements GlyphProviderDefinition {
   private static final Codec<String> SKIP_LIST_CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf()).xmap((either) -> either.map((s) -> s, (list) -> String.join("", list)), Either::left);
   public static final MapCodec<TrueTypeGlyphProviderDefinition> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("file").forGetter(TrueTypeGlyphProviderDefinition::location), Codec.FLOAT.optionalFieldOf("size", Float.valueOf(11.0F)).forGetter(TrueTypeGlyphProviderDefinition::size), Codec.FLOAT.optionalFieldOf("oversample", Float.valueOf(1.0F)).forGetter(TrueTypeGlyphProviderDefinition::oversample), TrueTypeGlyphProviderDefinition.Shift.CODEC.optionalFieldOf("shift", TrueTypeGlyphProviderDefinition.Shift.NONE).forGetter(TrueTypeGlyphProviderDefinition::shift), SKIP_LIST_CODEC.optionalFieldOf("skip", "").forGetter(TrueTypeGlyphProviderDefinition::skip)).apply(recordcodecbuilder_instance, TrueTypeGlyphProviderDefinition::new));

   public GlyphProviderType type() {
      return GlyphProviderType.TTF;
   }

   public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
      return Either.left(this::load);
   }

   private GlyphProvider load(ResourceManager resourcemanager) throws IOException {
      STBTTFontinfo stbttfontinfo = null;
      ByteBuffer bytebuffer = null;

      try {
         InputStream inputstream = resourcemanager.open(this.location.withPrefix("font/"));

         TrueTypeGlyphProvider var5;
         try {
            stbttfontinfo = STBTTFontinfo.malloc();
            bytebuffer = TextureUtil.readResource(inputstream);
            bytebuffer.flip();
            if (!STBTruetype.stbtt_InitFont(stbttfontinfo, bytebuffer)) {
               throw new IOException("Invalid ttf");
            }

            var5 = new TrueTypeGlyphProvider(bytebuffer, stbttfontinfo, this.size, this.oversample, this.shift.x, this.shift.y, this.skip);
         } catch (Throwable var8) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var5;
      } catch (Exception var9) {
         if (stbttfontinfo != null) {
            stbttfontinfo.free();
         }

         MemoryUtil.memFree(bytebuffer);
         throw var9;
      }
   }

   public static record Shift(float x, float y) {
      final float x;
      final float y;
      public static final TrueTypeGlyphProviderDefinition.Shift NONE = new TrueTypeGlyphProviderDefinition.Shift(0.0F, 0.0F);
      public static final Codec<TrueTypeGlyphProviderDefinition.Shift> CODEC = Codec.FLOAT.listOf().comapFlatMap((list) -> Util.fixedSize(list, 2).map((list1) -> new TrueTypeGlyphProviderDefinition.Shift(list1.get(0), list1.get(1))), (truetypeglyphproviderdefinition_shift) -> List.of(truetypeglyphproviderdefinition_shift.x, truetypeglyphproviderdefinition_shift.y));
   }
}
