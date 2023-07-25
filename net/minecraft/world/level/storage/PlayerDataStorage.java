package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class PlayerDataStorage {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final File playerDir;
   protected final DataFixer fixerUpper;

   public PlayerDataStorage(LevelStorageSource.LevelStorageAccess levelstoragesource_levelstorageaccess, DataFixer datafixer) {
      this.fixerUpper = datafixer;
      this.playerDir = levelstoragesource_levelstorageaccess.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
      this.playerDir.mkdirs();
   }

   public void save(Player player) {
      try {
         CompoundTag compoundtag = player.saveWithoutId(new CompoundTag());
         File file = File.createTempFile(player.getStringUUID() + "-", ".dat", this.playerDir);
         NbtIo.writeCompressed(compoundtag, file);
         File file1 = new File(this.playerDir, player.getStringUUID() + ".dat");
         File file2 = new File(this.playerDir, player.getStringUUID() + ".dat_old");
         Util.safeReplaceFile(file1, file, file2);
      } catch (Exception var6) {
         LOGGER.warn("Failed to save player data for {}", (Object)player.getName().getString());
      }

   }

   @Nullable
   public CompoundTag load(Player player) {
      CompoundTag compoundtag = null;

      try {
         File file = new File(this.playerDir, player.getStringUUID() + ".dat");
         if (file.exists() && file.isFile()) {
            compoundtag = NbtIo.readCompressed(file);
         }
      } catch (Exception var4) {
         LOGGER.warn("Failed to load player data for {}", (Object)player.getName().getString());
      }

      if (compoundtag != null) {
         int i = NbtUtils.getDataVersion(compoundtag, -1);
         player.load(DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, compoundtag, i));
      }

      return compoundtag;
   }

   public String[] getSeenPlayers() {
      String[] astring = this.playerDir.list();
      if (astring == null) {
         astring = new String[0];
      }

      for(int i = 0; i < astring.length; ++i) {
         if (astring[i].endsWith(".dat")) {
            astring[i] = astring[i].substring(0, astring[i].length() - 4);
         }
      }

      return astring;
   }
}
