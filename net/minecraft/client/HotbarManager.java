package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;

public class HotbarManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int NUM_HOTBAR_GROUPS = 9;
   private final File optionsFile;
   private final DataFixer fixerUpper;
   private final Hotbar[] hotbars = new Hotbar[9];
   private boolean loaded;

   public HotbarManager(File file, DataFixer datafixer) {
      this.optionsFile = new File(file, "hotbar.nbt");
      this.fixerUpper = datafixer;

      for(int i = 0; i < 9; ++i) {
         this.hotbars[i] = new Hotbar();
      }

   }

   private void load() {
      try {
         CompoundTag compoundtag = NbtIo.read(this.optionsFile);
         if (compoundtag == null) {
            return;
         }

         int i = NbtUtils.getDataVersion(compoundtag, 1343);
         compoundtag = DataFixTypes.HOTBAR.updateToCurrentVersion(this.fixerUpper, compoundtag, i);

         for(int j = 0; j < 9; ++j) {
            this.hotbars[j].fromTag(compoundtag.getList(String.valueOf(j), 10));
         }
      } catch (Exception var4) {
         LOGGER.error("Failed to load creative mode options", (Throwable)var4);
      }

   }

   public void save() {
      try {
         CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());

         for(int i = 0; i < 9; ++i) {
            compoundtag.put(String.valueOf(i), this.get(i).createTag());
         }

         NbtIo.write(compoundtag, this.optionsFile);
      } catch (Exception var3) {
         LOGGER.error("Failed to save creative mode options", (Throwable)var3);
      }

   }

   public Hotbar get(int i) {
      if (!this.loaded) {
         this.load();
         this.loaded = true;
      }

      return this.hotbars[i];
   }
}
