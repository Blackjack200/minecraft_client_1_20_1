package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import net.minecraft.util.FastBufferedInputStream;

public class RegionFileVersion {
   private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap<>();
   public static final RegionFileVersion VERSION_GZIP = register(new RegionFileVersion(1, (inputstream) -> new FastBufferedInputStream(new GZIPInputStream(inputstream)), (outputstream) -> new BufferedOutputStream(new GZIPOutputStream(outputstream))));
   public static final RegionFileVersion VERSION_DEFLATE = register(new RegionFileVersion(2, (inputstream) -> new FastBufferedInputStream(new InflaterInputStream(inputstream)), (outputstream) -> new BufferedOutputStream(new DeflaterOutputStream(outputstream))));
   public static final RegionFileVersion VERSION_NONE = register(new RegionFileVersion(3, (inputstream) -> inputstream, (outputstream) -> outputstream));
   private final int id;
   private final RegionFileVersion.StreamWrapper<InputStream> inputWrapper;
   private final RegionFileVersion.StreamWrapper<OutputStream> outputWrapper;

   private RegionFileVersion(int i, RegionFileVersion.StreamWrapper<InputStream> regionfileversion_streamwrapper, RegionFileVersion.StreamWrapper<OutputStream> regionfileversion_streamwrapper1) {
      this.id = i;
      this.inputWrapper = regionfileversion_streamwrapper;
      this.outputWrapper = regionfileversion_streamwrapper1;
   }

   private static RegionFileVersion register(RegionFileVersion regionfileversion) {
      VERSIONS.put(regionfileversion.id, regionfileversion);
      return regionfileversion;
   }

   @Nullable
   public static RegionFileVersion fromId(int i) {
      return VERSIONS.get(i);
   }

   public static boolean isValidVersion(int i) {
      return VERSIONS.containsKey(i);
   }

   public int getId() {
      return this.id;
   }

   public OutputStream wrap(OutputStream outputstream) throws IOException {
      return this.outputWrapper.wrap(outputstream);
   }

   public InputStream wrap(InputStream inputstream) throws IOException {
      return this.inputWrapper.wrap(inputstream);
   }

   @FunctionalInterface
   interface StreamWrapper<O> {
      O wrap(O object) throws IOException;
   }
}
