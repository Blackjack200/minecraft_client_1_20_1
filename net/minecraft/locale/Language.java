package net.minecraft.locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringDecomposer;
import org.slf4j.Logger;

public abstract class Language {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new Gson();
   private static final Pattern UNSUPPORTED_FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
   public static final String DEFAULT = "en_us";
   private static volatile Language instance = loadDefault();

   private static Language loadDefault() {
      ImmutableMap.Builder<String, String> immutablemap_builder = ImmutableMap.builder();
      BiConsumer<String, String> biconsumer = immutablemap_builder::put;
      parseTranslations(biconsumer, "/assets/minecraft/lang/en_us.json");
      final Map<String, String> map = immutablemap_builder.build();
      return new Language() {
         public String getOrDefault(String s, String s1) {
            return map.getOrDefault(s, s1);
         }

         public boolean has(String s) {
            return map.containsKey(s);
         }

         public boolean isDefaultRightToLeft() {
            return false;
         }

         public FormattedCharSequence getVisualOrder(FormattedText formattedtext) {
            return (formattedcharsink) -> formattedtext.visit((style, s) -> StringDecomposer.iterateFormatted(s, style, formattedcharsink) ? Optional.empty() : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
         }
      };
   }

   private static void parseTranslations(BiConsumer<String, String> biconsumer, String s) {
      try {
         InputStream inputstream = Language.class.getResourceAsStream(s);

         try {
            loadFromJson(inputstream, biconsumer);
         } catch (Throwable var6) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (inputstream != null) {
            inputstream.close();
         }
      } catch (JsonParseException | IOException var7) {
         LOGGER.error("Couldn't read strings from {}", s, var7);
      }

   }

   public static void loadFromJson(InputStream inputstream, BiConsumer<String, String> biconsumer) {
      JsonObject jsonobject = GSON.fromJson(new InputStreamReader(inputstream, StandardCharsets.UTF_8), JsonObject.class);

      for(Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
         String s = UNSUPPORTED_FORMAT_PATTERN.matcher(GsonHelper.convertToString(map_entry.getValue(), map_entry.getKey())).replaceAll("%$1s");
         biconsumer.accept(map_entry.getKey(), s);
      }

   }

   public static Language getInstance() {
      return instance;
   }

   public static void inject(Language language) {
      instance = language;
   }

   public String getOrDefault(String s) {
      return this.getOrDefault(s, s);
   }

   public abstract String getOrDefault(String s, String s1);

   public abstract boolean has(String s);

   public abstract boolean isDefaultRightToLeft();

   public abstract FormattedCharSequence getVisualOrder(FormattedText formattedtext);

   public List<FormattedCharSequence> getVisualOrder(List<FormattedText> list) {
      return list.stream().map(this::getVisualOrder).collect(ImmutableList.toImmutableList());
   }
}
