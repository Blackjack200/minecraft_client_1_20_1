package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<String, SavedData> cache = Maps.newHashMap();
   private final DataFixer fixerUpper;
   private final File dataFolder;

   public DimensionDataStorage(File file, DataFixer datafixer) {
      this.fixerUpper = datafixer;
      this.dataFolder = file;
   }

   private File getDataFile(String s) {
      return new File(this.dataFolder, s + ".dat");
   }

   public <T extends SavedData> T computeIfAbsent(Function<CompoundTag, T> function, Supplier<T> supplier, String s) {
      T saveddata = this.get(function, s);
      if (saveddata != null) {
         return saveddata;
      } else {
         T saveddata1 = supplier.get();
         this.set(s, saveddata1);
         return saveddata1;
      }
   }

   @Nullable
   public <T extends SavedData> T get(Function<CompoundTag, T> function, String s) {
      SavedData saveddata = this.cache.get(s);
      if (saveddata == null && !this.cache.containsKey(s)) {
         saveddata = this.readSavedData(function, s);
         this.cache.put(s, saveddata);
      }

      return (T)saveddata;
   }

   @Nullable
   private <T extends SavedData> T readSavedData(Function<CompoundTag, T> function, String s) {
      try {
         File file = this.getDataFile(s);
         if (file.exists()) {
            CompoundTag compoundtag = this.readTagFromDisk(s, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            return function.apply(compoundtag.getCompound("data"));
         }
      } catch (Exception var5) {
         LOGGER.error("Error loading saved data: {}", s, var5);
      }

      return (T)null;
   }

   public void set(String s, SavedData saveddata) {
      this.cache.put(s, saveddata);
   }

   public CompoundTag readTagFromDisk(String s, int i) throws IOException {
      File file = this.getDataFile(s);
      FileInputStream fileinputstream = new FileInputStream(file);

      CompoundTag var8;
      try {
         PushbackInputStream pushbackinputstream = new PushbackInputStream(fileinputstream, 2);

         try {
            CompoundTag compoundtag;
            if (this.isGzip(pushbackinputstream)) {
               compoundtag = NbtIo.readCompressed(pushbackinputstream);
            } else {
               DataInputStream datainputstream = new DataInputStream(pushbackinputstream);

               try {
                  compoundtag = NbtIo.read(datainputstream);
               } catch (Throwable var13) {
                  try {
                     datainputstream.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }

                  throw var13;
               }

               datainputstream.close();
            }

            int j = NbtUtils.getDataVersion(compoundtag, 1343);
            var8 = DataFixTypes.SAVED_DATA.update(this.fixerUpper, compoundtag, j, i);
         } catch (Throwable var14) {
            try {
               pushbackinputstream.close();
            } catch (Throwable var11) {
               var14.addSuppressed(var11);
            }

            throw var14;
         }

         pushbackinputstream.close();
      } catch (Throwable var15) {
         try {
            fileinputstream.close();
         } catch (Throwable var10) {
            var15.addSuppressed(var10);
         }

         throw var15;
      }

      fileinputstream.close();
      return var8;
   }

   private boolean isGzip(PushbackInputStream pushbackinputstream) throws IOException {
      byte[] abyte = new byte[2];
      boolean flag = false;
      int i = pushbackinputstream.read(abyte, 0, 2);
      if (i == 2) {
         int j = (abyte[1] & 255) << 8 | abyte[0] & 255;
         if (j == 35615) {
            flag = true;
         }
      }

      if (i != 0) {
         pushbackinputstream.unread(abyte, 0, i);
      }

      return flag;
   }

   public void save() {
      this.cache.forEach((s, saveddata) -> {
         if (saveddata != null) {
            saveddata.save(this.getDataFile(s));
         }

      });
   }
}
