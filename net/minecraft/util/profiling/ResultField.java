package net.minecraft.util.profiling;

public final class ResultField implements Comparable<ResultField> {
   public final double percentage;
   public final double globalPercentage;
   public final long count;
   public final String name;

   public ResultField(String s, double d0, double d1, long i) {
      this.name = s;
      this.percentage = d0;
      this.globalPercentage = d1;
      this.count = i;
   }

   public int compareTo(ResultField resultfield) {
      if (resultfield.percentage < this.percentage) {
         return -1;
      } else {
         return resultfield.percentage > this.percentage ? 1 : resultfield.name.compareTo(this.name);
      }
   }

   public int getColor() {
      return (this.name.hashCode() & 11184810) + 4473924;
   }
}
