package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class GpuWarnlistManager extends SimplePreparableReloadListener<GpuWarnlistManager.Preparations> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation GPU_WARNLIST_LOCATION = new ResourceLocation("gpu_warnlist.json");
   private ImmutableMap<String, String> warnings = ImmutableMap.of();
   private boolean showWarning;
   private boolean warningDismissed;
   private boolean skipFabulous;

   public boolean hasWarnings() {
      return !this.warnings.isEmpty();
   }

   public boolean willShowWarning() {
      return this.hasWarnings() && !this.warningDismissed;
   }

   public void showWarning() {
      this.showWarning = true;
   }

   public void dismissWarning() {
      this.warningDismissed = true;
   }

   public void dismissWarningAndSkipFabulous() {
      this.warningDismissed = true;
      this.skipFabulous = true;
   }

   public boolean isShowingWarning() {
      return this.showWarning && !this.warningDismissed;
   }

   public boolean isSkippingFabulous() {
      return this.skipFabulous;
   }

   public void resetWarnings() {
      this.showWarning = false;
      this.warningDismissed = false;
      this.skipFabulous = false;
   }

   @Nullable
   public String getRendererWarnings() {
      return this.warnings.get("renderer");
   }

   @Nullable
   public String getVersionWarnings() {
      return this.warnings.get("version");
   }

   @Nullable
   public String getVendorWarnings() {
      return this.warnings.get("vendor");
   }

   @Nullable
   public String getAllWarnings() {
      StringBuilder stringbuilder = new StringBuilder();
      this.warnings.forEach((s, s1) -> stringbuilder.append(s).append(": ").append(s1));
      return stringbuilder.length() == 0 ? null : stringbuilder.toString();
   }

   protected GpuWarnlistManager.Preparations prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      List<Pattern> list = Lists.newArrayList();
      List<Pattern> list1 = Lists.newArrayList();
      List<Pattern> list2 = Lists.newArrayList();
      profilerfiller.startTick();
      JsonObject jsonobject = parseJson(resourcemanager, profilerfiller);
      if (jsonobject != null) {
         profilerfiller.push("compile_regex");
         compilePatterns(jsonobject.getAsJsonArray("renderer"), list);
         compilePatterns(jsonobject.getAsJsonArray("version"), list1);
         compilePatterns(jsonobject.getAsJsonArray("vendor"), list2);
         profilerfiller.pop();
      }

      profilerfiller.endTick();
      return new GpuWarnlistManager.Preparations(list, list1, list2);
   }

   protected void apply(GpuWarnlistManager.Preparations gpuwarnlistmanager_preparations, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      this.warnings = gpuwarnlistmanager_preparations.apply();
   }

   private static void compilePatterns(JsonArray jsonarray, List<Pattern> list) {
      jsonarray.forEach((jsonelement) -> list.add(Pattern.compile(jsonelement.getAsString(), 2)));
   }

   @Nullable
   private static JsonObject parseJson(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      profilerfiller.push("parse_json");
      JsonObject jsonobject = null;

      try {
         Reader reader = resourcemanager.openAsReader(GPU_WARNLIST_LOCATION);

         try {
            jsonobject = JsonParser.parseReader(reader).getAsJsonObject();
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (JsonSyntaxException | IOException var8) {
         LOGGER.warn("Failed to load GPU warnlist");
      }

      profilerfiller.pop();
      return jsonobject;
   }

   protected static final class Preparations {
      private final List<Pattern> rendererPatterns;
      private final List<Pattern> versionPatterns;
      private final List<Pattern> vendorPatterns;

      Preparations(List<Pattern> list, List<Pattern> list1, List<Pattern> list2) {
         this.rendererPatterns = list;
         this.versionPatterns = list1;
         this.vendorPatterns = list2;
      }

      private static String matchAny(List<Pattern> list, String s) {
         List<String> list1 = Lists.newArrayList();

         for(Pattern pattern : list) {
            Matcher matcher = pattern.matcher(s);

            while(matcher.find()) {
               list1.add(matcher.group());
            }
         }

         return String.join(", ", list1);
      }

      ImmutableMap<String, String> apply() {
         ImmutableMap.Builder<String, String> immutablemap_builder = new ImmutableMap.Builder<>();
         String s = matchAny(this.rendererPatterns, GlUtil.getRenderer());
         if (!s.isEmpty()) {
            immutablemap_builder.put("renderer", s);
         }

         String s1 = matchAny(this.versionPatterns, GlUtil.getOpenGLVersion());
         if (!s1.isEmpty()) {
            immutablemap_builder.put("version", s1);
         }

         String s2 = matchAny(this.vendorPatterns, GlUtil.getVendor());
         if (!s2.isEmpty()) {
            immutablemap_builder.put("vendor", s2);
         }

         return immutablemap_builder.build();
      }
   }
}
