package com.mojang.blaze3d.platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.ArrayUtils;

public enum IconSet {
   RELEASE("icons"),
   SNAPSHOT("icons", "snapshot");

   private final String[] path;

   private IconSet(String... astring) {
      this.path = astring;
   }

   public List<IoSupplier<InputStream>> getStandardIcons(PackResources packresources) throws IOException {
      return List.of(this.getFile(packresources, "icon_16x16.png"), this.getFile(packresources, "icon_32x32.png"), this.getFile(packresources, "icon_48x48.png"), this.getFile(packresources, "icon_128x128.png"), this.getFile(packresources, "icon_256x256.png"));
   }

   public IoSupplier<InputStream> getMacIcon(PackResources packresources) throws IOException {
      return this.getFile(packresources, "minecraft.icns");
   }

   private IoSupplier<InputStream> getFile(PackResources packresources, String s) throws IOException {
      String[] astring = ArrayUtils.add(this.path, s);
      IoSupplier<InputStream> iosupplier = packresources.getRootResource(astring);
      if (iosupplier == null) {
         throw new FileNotFoundException(String.join("/", astring));
      } else {
         return iosupplier;
      }
   }
}
