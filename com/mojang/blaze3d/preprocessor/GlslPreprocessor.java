package com.mojang.blaze3d.preprocessor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.util.StringUtil;

public abstract class GlslPreprocessor {
   private static final String C_COMMENT = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
   private static final String LINE_COMMENT = "//[^\\v]*";
   private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
   private static final Pattern REGEX_VERSION = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
   private static final Pattern REGEX_ENDS_WITH_WHITESPACE = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

   public List<String> process(String s) {
      GlslPreprocessor.Context glslpreprocessor_context = new GlslPreprocessor.Context();
      List<String> list = this.processImports(s, glslpreprocessor_context, "");
      list.set(0, this.setVersion(list.get(0), glslpreprocessor_context.glslVersion));
      return list;
   }

   private List<String> processImports(String s, GlslPreprocessor.Context glslpreprocessor_context, String s1) {
      int i = glslpreprocessor_context.sourceId;
      int j = 0;
      String s2 = "";
      List<String> list = Lists.newArrayList();
      Matcher matcher = REGEX_MOJ_IMPORT.matcher(s);

      while(matcher.find()) {
         if (!isDirectiveDisabled(s, matcher, j)) {
            String s3 = matcher.group(2);
            boolean flag = s3 != null;
            if (!flag) {
               s3 = matcher.group(3);
            }

            if (s3 != null) {
               String s4 = s.substring(j, matcher.start(1));
               String s5 = s1 + s3;
               String s6 = this.applyImport(flag, s5);
               if (!Strings.isNullOrEmpty(s6)) {
                  if (!StringUtil.endsWithNewLine(s6)) {
                     s6 = s6 + System.lineSeparator();
                  }

                  ++glslpreprocessor_context.sourceId;
                  int k = glslpreprocessor_context.sourceId;
                  List<String> list1 = this.processImports(s6, glslpreprocessor_context, flag ? FileUtil.getFullResourcePath(s5) : "");
                  list1.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, k, this.processVersions(list1.get(0), glslpreprocessor_context)));
                  if (!Util.isBlank(s4)) {
                     list.add(s4);
                  }

                  list.addAll(list1);
               } else {
                  String s7 = flag ? String.format(Locale.ROOT, "/*#moj_import \"%s\"*/", s3) : String.format(Locale.ROOT, "/*#moj_import <%s>*/", s3);
                  list.add(s2 + s4 + s7);
               }

               int l = StringUtil.lineCount(s.substring(0, matcher.end(1)));
               s2 = String.format(Locale.ROOT, "#line %d %d", l, i);
               j = matcher.end(1);
            }
         }
      }

      String s8 = s.substring(j);
      if (!Util.isBlank(s8)) {
         list.add(s2 + s8);
      }

      return list;
   }

   private String processVersions(String s, GlslPreprocessor.Context glslpreprocessor_context) {
      Matcher matcher = REGEX_VERSION.matcher(s);
      if (matcher.find() && isDirectiveEnabled(s, matcher)) {
         glslpreprocessor_context.glslVersion = Math.max(glslpreprocessor_context.glslVersion, Integer.parseInt(matcher.group(2)));
         return s.substring(0, matcher.start(1)) + "/*" + s.substring(matcher.start(1), matcher.end(1)) + "*/" + s.substring(matcher.end(1));
      } else {
         return s;
      }
   }

   private String setVersion(String s, int i) {
      Matcher matcher = REGEX_VERSION.matcher(s);
      return matcher.find() && isDirectiveEnabled(s, matcher) ? s.substring(0, matcher.start(2)) + Math.max(i, Integer.parseInt(matcher.group(2))) + s.substring(matcher.end(2)) : s;
   }

   private static boolean isDirectiveEnabled(String s, Matcher matcher) {
      return !isDirectiveDisabled(s, matcher, 0);
   }

   private static boolean isDirectiveDisabled(String s, Matcher matcher, int i) {
      int j = matcher.start() - i;
      if (j == 0) {
         return false;
      } else {
         Matcher matcher1 = REGEX_ENDS_WITH_WHITESPACE.matcher(s.substring(i, matcher.start()));
         if (!matcher1.find()) {
            return true;
         } else {
            int k = matcher1.end(1);
            return k == matcher.start();
         }
      }
   }

   @Nullable
   public abstract String applyImport(boolean flag, String s);

   static final class Context {
      int glslVersion;
      int sourceId;
   }
}
