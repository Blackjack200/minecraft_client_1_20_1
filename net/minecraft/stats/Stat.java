package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Stat<T> extends ObjectiveCriteria {
   private final StatFormatter formatter;
   private final T value;
   private final StatType<T> type;

   protected Stat(StatType<T> stattype, T object, StatFormatter statformatter) {
      super(buildName(stattype, object));
      this.type = stattype;
      this.formatter = statformatter;
      this.value = object;
   }

   public static <T> String buildName(StatType<T> stattype, T object) {
      return locationToKey(BuiltInRegistries.STAT_TYPE.getKey(stattype)) + ":" + locationToKey(stattype.getRegistry().getKey(object));
   }

   private static <T> String locationToKey(@Nullable ResourceLocation resourcelocation) {
      return resourcelocation.toString().replace(':', '.');
   }

   public StatType<T> getType() {
      return this.type;
   }

   public T getValue() {
      return this.value;
   }

   public String format(int i) {
      return this.formatter.format(i);
   }

   public boolean equals(Object object) {
      return this == object || object instanceof Stat && Objects.equals(this.getName(), ((Stat)object).getName());
   }

   public int hashCode() {
      return this.getName().hashCode();
   }

   public String toString() {
      return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + "}";
   }
}
