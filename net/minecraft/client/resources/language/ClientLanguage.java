package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

public class ClientLanguage extends Language {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<String, String> storage;
   private final boolean defaultRightToLeft;

   private ClientLanguage(Map<String, String> map, boolean flag) {
      this.storage = map;
      this.defaultRightToLeft = flag;
   }

   public static ClientLanguage loadFrom(ResourceManager resourcemanager, List<String> list, boolean flag) {
      Map<String, String> map = Maps.newHashMap();

      for(String s : list) {
         String s1 = String.format(Locale.ROOT, "lang/%s.json", s);

         for(String s2 : resourcemanager.getNamespaces()) {
            try {
               ResourceLocation resourcelocation = new ResourceLocation(s2, s1);
               appendFrom(s, resourcemanager.getResourceStack(resourcelocation), map);
            } catch (Exception var10) {
               LOGGER.warn("Skipped language file: {}:{} ({})", s2, s1, var10.toString());
            }
         }
      }

      return new ClientLanguage(ImmutableMap.copyOf(map), flag);
   }

   private static void appendFrom(String s, List<Resource> list, Map<String, String> map) {
      for(Resource resource : list) {
         try {
            InputStream inputstream = resource.open();

            try {
               Language.loadFromJson(inputstream, map::put);
            } catch (Throwable var9) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (inputstream != null) {
               inputstream.close();
            }
         } catch (IOException var10) {
            LOGGER.warn("Failed to load translations for {} from pack {}", s, resource.sourcePackId(), var10);
         }
      }

   }

   public String getOrDefault(String s, String s1) {
      return this.storage.getOrDefault(s, s1);
   }

   public boolean has(String s) {
      return this.storage.containsKey(s);
   }

   public boolean isDefaultRightToLeft() {
      return this.defaultRightToLeft;
   }

   public FormattedCharSequence getVisualOrder(FormattedText formattedtext) {
      return FormattedBidiReorder.reorder(formattedtext, this.defaultRightToLeft);
   }
}
