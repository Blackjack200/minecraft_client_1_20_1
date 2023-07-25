package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CrashReportCategory {
   private final String title;
   private final List<CrashReportCategory.Entry> entries = Lists.newArrayList();
   private StackTraceElement[] stackTrace = new StackTraceElement[0];

   public CrashReportCategory(String s) {
      this.title = s;
   }

   public static String formatLocation(LevelHeightAccessor levelheightaccessor, double d0, double d1, double d2) {
      return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", d0, d1, d2, formatLocation(levelheightaccessor, BlockPos.containing(d0, d1, d2)));
   }

   public static String formatLocation(LevelHeightAccessor levelheightaccessor, BlockPos blockpos) {
      return formatLocation(levelheightaccessor, blockpos.getX(), blockpos.getY(), blockpos.getZ());
   }

   public static String formatLocation(LevelHeightAccessor levelheightaccessor, int i, int j, int k) {
      StringBuilder stringbuilder = new StringBuilder();

      try {
         stringbuilder.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", i, j, k));
      } catch (Throwable var19) {
         stringbuilder.append("(Error finding world loc)");
      }

      stringbuilder.append(", ");

      try {
         int l = SectionPos.blockToSectionCoord(i);
         int i1 = SectionPos.blockToSectionCoord(j);
         int j1 = SectionPos.blockToSectionCoord(k);
         int k1 = i & 15;
         int l1 = j & 15;
         int i2 = k & 15;
         int j2 = SectionPos.sectionToBlockCoord(l);
         int k2 = levelheightaccessor.getMinBuildHeight();
         int l2 = SectionPos.sectionToBlockCoord(j1);
         int i3 = SectionPos.sectionToBlockCoord(l + 1) - 1;
         int j3 = levelheightaccessor.getMaxBuildHeight() - 1;
         int k3 = SectionPos.sectionToBlockCoord(j1 + 1) - 1;
         stringbuilder.append(String.format(Locale.ROOT, "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", k1, l1, i2, l, i1, j1, j2, k2, l2, i3, j3, k3));
      } catch (Throwable var18) {
         stringbuilder.append("(Error finding chunk loc)");
      }

      stringbuilder.append(", ");

      try {
         int l3 = i >> 9;
         int i4 = k >> 9;
         int j4 = l3 << 5;
         int k4 = i4 << 5;
         int l4 = (l3 + 1 << 5) - 1;
         int i5 = (i4 + 1 << 5) - 1;
         int j5 = l3 << 9;
         int k5 = levelheightaccessor.getMinBuildHeight();
         int l5 = i4 << 9;
         int i6 = (l3 + 1 << 9) - 1;
         int j6 = levelheightaccessor.getMaxBuildHeight() - 1;
         int k6 = (i4 + 1 << 9) - 1;
         stringbuilder.append(String.format(Locale.ROOT, "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, j6, k6));
      } catch (Throwable var17) {
         stringbuilder.append("(Error finding world loc)");
      }

      return stringbuilder.toString();
   }

   public CrashReportCategory setDetail(String s, CrashReportDetail<String> crashreportdetail) {
      try {
         this.setDetail(s, crashreportdetail.call());
      } catch (Throwable var4) {
         this.setDetailError(s, var4);
      }

      return this;
   }

   public CrashReportCategory setDetail(String s, Object object) {
      this.entries.add(new CrashReportCategory.Entry(s, object));
      return this;
   }

   public void setDetailError(String s, Throwable throwable) {
      this.setDetail(s, throwable);
   }

   public int fillInStackTrace(int i) {
      StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
      if (astacktraceelement.length <= 0) {
         return 0;
      } else {
         this.stackTrace = new StackTraceElement[astacktraceelement.length - 3 - i];
         System.arraycopy(astacktraceelement, 3 + i, this.stackTrace, 0, this.stackTrace.length);
         return this.stackTrace.length;
      }
   }

   public boolean validateStackTrace(StackTraceElement stacktraceelement, StackTraceElement stacktraceelement1) {
      if (this.stackTrace.length != 0 && stacktraceelement != null) {
         StackTraceElement stacktraceelement2 = this.stackTrace[0];
         if (stacktraceelement2.isNativeMethod() == stacktraceelement.isNativeMethod() && stacktraceelement2.getClassName().equals(stacktraceelement.getClassName()) && stacktraceelement2.getFileName().equals(stacktraceelement.getFileName()) && stacktraceelement2.getMethodName().equals(stacktraceelement.getMethodName())) {
            if (stacktraceelement1 != null != this.stackTrace.length > 1) {
               return false;
            } else if (stacktraceelement1 != null && !this.stackTrace[1].equals(stacktraceelement1)) {
               return false;
            } else {
               this.stackTrace[0] = stacktraceelement;
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void trimStacktrace(int i) {
      StackTraceElement[] astacktraceelement = new StackTraceElement[this.stackTrace.length - i];
      System.arraycopy(this.stackTrace, 0, astacktraceelement, 0, astacktraceelement.length);
      this.stackTrace = astacktraceelement;
   }

   public void getDetails(StringBuilder stringbuilder) {
      stringbuilder.append("-- ").append(this.title).append(" --\n");
      stringbuilder.append("Details:");

      for(CrashReportCategory.Entry crashreportcategory_entry : this.entries) {
         stringbuilder.append("\n\t");
         stringbuilder.append(crashreportcategory_entry.getKey());
         stringbuilder.append(": ");
         stringbuilder.append(crashreportcategory_entry.getValue());
      }

      if (this.stackTrace != null && this.stackTrace.length > 0) {
         stringbuilder.append("\nStacktrace:");

         for(StackTraceElement stacktraceelement : this.stackTrace) {
            stringbuilder.append("\n\tat ");
            stringbuilder.append((Object)stacktraceelement);
         }
      }

   }

   public StackTraceElement[] getStacktrace() {
      return this.stackTrace;
   }

   public static void populateBlockDetails(CrashReportCategory crashreportcategory, LevelHeightAccessor levelheightaccessor, BlockPos blockpos, @Nullable BlockState blockstate) {
      if (blockstate != null) {
         crashreportcategory.setDetail("Block", blockstate::toString);
      }

      crashreportcategory.setDetail("Block location", () -> formatLocation(levelheightaccessor, blockpos));
   }

   static class Entry {
      private final String key;
      private final String value;

      public Entry(String s, @Nullable Object object) {
         this.key = s;
         if (object == null) {
            this.value = "~~NULL~~";
         } else if (object instanceof Throwable) {
            Throwable throwable = (Throwable)object;
            this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
         } else {
            this.value = object.toString();
         }

      }

      public String getKey() {
         return this.key;
      }

      public String getValue() {
         return this.value;
      }
   }
}
