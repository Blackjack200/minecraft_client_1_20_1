package net.minecraft.world.level.saveddata;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public abstract class SavedData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private boolean dirty;

   public abstract CompoundTag save(CompoundTag compoundtag);

   public void setDirty() {
      this.setDirty(true);
   }

   public void setDirty(boolean flag) {
      this.dirty = flag;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void save(File file) {
      if (this.isDirty()) {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.put("data", this.save(new CompoundTag()));
         NbtUtils.addCurrentDataVersion(compoundtag);

         try {
            NbtIo.writeCompressed(compoundtag, file);
         } catch (IOException var4) {
            LOGGER.error("Could not save data {}", this, var4);
         }

         this.setDirty(false);
      }
   }
}
