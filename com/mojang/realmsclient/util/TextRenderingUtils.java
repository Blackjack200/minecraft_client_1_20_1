package com.mojang.realmsclient.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class TextRenderingUtils {
   private TextRenderingUtils() {
   }

   @VisibleForTesting
   protected static List<String> lineBreak(String s) {
      return Arrays.asList(s.split("\\n"));
   }

   public static List<TextRenderingUtils.Line> decompose(String s, TextRenderingUtils.LineSegment... atextrenderingutils_linesegment) {
      return decompose(s, Arrays.asList(atextrenderingutils_linesegment));
   }

   private static List<TextRenderingUtils.Line> decompose(String s, List<TextRenderingUtils.LineSegment> list) {
      List<String> list1 = lineBreak(s);
      return insertLinks(list1, list);
   }

   private static List<TextRenderingUtils.Line> insertLinks(List<String> list, List<TextRenderingUtils.LineSegment> list1) {
      int i = 0;
      List<TextRenderingUtils.Line> list2 = Lists.newArrayList();

      for(String s : list) {
         List<TextRenderingUtils.LineSegment> list3 = Lists.newArrayList();

         for(String s1 : split(s, "%link")) {
            if ("%link".equals(s1)) {
               list3.add(list1.get(i++));
            } else {
               list3.add(TextRenderingUtils.LineSegment.text(s1));
            }
         }

         list2.add(new TextRenderingUtils.Line(list3));
      }

      return list2;
   }

   public static List<String> split(String s, String s1) {
      if (s1.isEmpty()) {
         throw new IllegalArgumentException("Delimiter cannot be the empty string");
      } else {
         List<String> list = Lists.newArrayList();

         int i;
         int j;
         for(i = 0; (j = s.indexOf(s1, i)) != -1; i = j + s1.length()) {
            if (j > i) {
               list.add(s.substring(i, j));
            }

            list.add(s1);
         }

         if (i < s.length()) {
            list.add(s.substring(i));
         }

         return list;
      }
   }

   public static class Line {
      public final List<TextRenderingUtils.LineSegment> segments;

      Line(TextRenderingUtils.LineSegment... atextrenderingutils_linesegment) {
         this(Arrays.asList(atextrenderingutils_linesegment));
      }

      Line(List<TextRenderingUtils.LineSegment> list) {
         this.segments = list;
      }

      public String toString() {
         return "Line{segments=" + this.segments + "}";
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            TextRenderingUtils.Line textrenderingutils_line = (TextRenderingUtils.Line)object;
            return Objects.equals(this.segments, textrenderingutils_line.segments);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.segments);
      }
   }

   public static class LineSegment {
      private final String fullText;
      @Nullable
      private final String linkTitle;
      @Nullable
      private final String linkUrl;

      private LineSegment(String s) {
         this.fullText = s;
         this.linkTitle = null;
         this.linkUrl = null;
      }

      private LineSegment(String s, @Nullable String s1, @Nullable String s2) {
         this.fullText = s;
         this.linkTitle = s1;
         this.linkUrl = s2;
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            TextRenderingUtils.LineSegment textrenderingutils_linesegment = (TextRenderingUtils.LineSegment)object;
            return Objects.equals(this.fullText, textrenderingutils_linesegment.fullText) && Objects.equals(this.linkTitle, textrenderingutils_linesegment.linkTitle) && Objects.equals(this.linkUrl, textrenderingutils_linesegment.linkUrl);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.fullText, this.linkTitle, this.linkUrl);
      }

      public String toString() {
         return "Segment{fullText='" + this.fullText + "', linkTitle='" + this.linkTitle + "', linkUrl='" + this.linkUrl + "'}";
      }

      public String renderedText() {
         return this.isLink() ? this.linkTitle : this.fullText;
      }

      public boolean isLink() {
         return this.linkTitle != null;
      }

      public String getLinkUrl() {
         if (!this.isLink()) {
            throw new IllegalStateException("Not a link: " + this);
         } else {
            return this.linkUrl;
         }
      }

      public static TextRenderingUtils.LineSegment link(String s, String s1) {
         return new TextRenderingUtils.LineSegment((String)null, s, s1);
      }

      @VisibleForTesting
      protected static TextRenderingUtils.LineSegment text(String s) {
         return new TextRenderingUtils.LineSegment(s);
      }
   }
}
