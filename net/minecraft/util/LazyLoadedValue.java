package net.minecraft.util;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

/** @deprecated */
@Deprecated
public class LazyLoadedValue<T> {
   private final Supplier<T> factory;

   public LazyLoadedValue(Supplier<T> supplier) {
      this.factory = Suppliers.memoize(supplier::get);
   }

   public T get() {
      return this.factory.get();
   }
}
