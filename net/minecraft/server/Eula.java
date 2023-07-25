package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;

public class Eula {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path file;
   private final boolean agreed;

   public Eula(Path path) {
      this.file = path;
      this.agreed = SharedConstants.IS_RUNNING_IN_IDE || this.readFile();
   }

   private boolean readFile() {
      try {
         InputStream inputstream = Files.newInputStream(this.file);

         boolean var3;
         try {
            Properties properties = new Properties();
            properties.load(inputstream);
            var3 = Boolean.parseBoolean(properties.getProperty("eula", "false"));
         } catch (Throwable var5) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var3;
      } catch (Exception var6) {
         LOGGER.warn("Failed to load {}", (Object)this.file);
         this.saveDefaults();
         return false;
      }
   }

   public boolean hasAgreedToEULA() {
      return this.agreed;
   }

   private void saveDefaults() {
      if (!SharedConstants.IS_RUNNING_IN_IDE) {
         try {
            OutputStream outputstream = Files.newOutputStream(this.file);

            try {
               Properties properties = new Properties();
               properties.setProperty("eula", "false");
               properties.store(outputstream, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).");
            } catch (Throwable var5) {
               if (outputstream != null) {
                  try {
                     outputstream.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (outputstream != null) {
               outputstream.close();
            }
         } catch (Exception var6) {
            LOGGER.warn("Failed to save {}", this.file, var6);
         }

      }
   }
}
